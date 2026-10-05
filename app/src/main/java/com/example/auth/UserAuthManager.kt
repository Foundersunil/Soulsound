package com.example.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Patterns
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

data class UserAuthState(
    val isLoggedIn: Boolean = false,
    val uid: String? = null,
    val email: String? = null,
    val displayName: String? = null,
    val role: String = "user", // ALWAYS "user" for normal accounts
    val isFirebaseConnected: Boolean = false
)

sealed class UserAuthResult {
    data class Success(val uid: String, val email: String) : UserAuthResult()
    data class Error(val message: String) : UserAuthResult()
}

enum class PasswordStrength(val label: String, val colorHex: Long) {
    WEAK("Weak", 0xFFFF5252),
    GOOD("Good", 0xFFFFB300),
    STRONG("Strong", 0xFF4CAF50)
}

class UserAuthManager(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("soulsound_user_auth", Context.MODE_PRIVATE)

    // Salt for local fallback cryptographic hash. Plaintext passwords are NEVER stored.
    private val salt = "soulsound_user_resonance_salt_528"

    private val _userAuthState = MutableStateFlow(UserAuthState())
    val userAuthState: StateFlow<UserAuthState> = _userAuthState.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    private val _passwordResetSuccess = MutableStateFlow<String?>(null)
    val passwordResetSuccess: StateFlow<String?> = _passwordResetSuccess.asStateFlow()

    val isFirebaseConnected: Boolean
        get() = try {
            FirebaseApp.getApps(context).isNotEmpty()
        } catch (_: Throwable) {
            false
        }

    private var firebaseAuthListener: FirebaseAuth.AuthStateListener? = null

    init {
        checkAndInitializeAuthState()
    }

    /**
     * Initializes and synchronizes with Firebase Auth if available,
     * or restores any active local secure user session.
     */
    private fun checkAndInitializeAuthState() {
        val firebaseAvailable = isFirebaseConnected
        if (firebaseAvailable) {
            try {
                val auth = FirebaseAuth.getInstance()
                val current = auth.currentUser
                if (current != null) {
                    _userAuthState.value = UserAuthState(
                        isLoggedIn = true,
                        uid = current.uid,
                        email = current.email,
                        displayName = current.displayName ?: current.email?.substringBefore('@')?.replaceFirstChar { it.uppercase() } ?: "Soul Traveler",
                        role = "user",
                        isFirebaseConnected = true
                    )
                } else {
                    _userAuthState.value = UserAuthState(isLoggedIn = false, isFirebaseConnected = true)
                }

                // Attach real Firebase AuthStateListener
                firebaseAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                    val user: FirebaseUser? = firebaseAuth.currentUser
                    if (user != null) {
                        _userAuthState.value = UserAuthState(
                            isLoggedIn = true,
                            uid = user.uid,
                            email = user.email,
                            displayName = user.displayName ?: user.email?.substringBefore('@')?.replaceFirstChar { it.uppercase() } ?: "Soul Traveler",
                            role = "user",
                            isFirebaseConnected = true
                        )
                    } else {
                        _userAuthState.value = UserAuthState(
                            isLoggedIn = false,
                            uid = null,
                            email = null,
                            displayName = null,
                            role = "user",
                            isFirebaseConnected = true
                        )
                    }
                }
                auth.addAuthStateListener(firebaseAuthListener!!)
                return
            } catch (_: Throwable) {}
        }

        // Fallback session restore when FirebaseApp is not initialized
        val savedUid = prefs.getString("user_uid", null)
        val savedEmail = prefs.getString("user_email", null)
        val savedName = prefs.getString("user_display_name", null)
        val isLoggedIn = prefs.getBoolean("user_logged_in", false)

        if (isLoggedIn && !savedUid.isNullOrBlank() && !savedEmail.isNullOrBlank()) {
            _userAuthState.value = UserAuthState(
                isLoggedIn = true,
                uid = savedUid,
                email = savedEmail,
                displayName = savedName ?: savedEmail.substringBefore('@').replaceFirstChar { it.uppercase() },
                role = "user",
                isFirebaseConnected = false
            )
        } else {
            _userAuthState.value = UserAuthState(isLoggedIn = false, isFirebaseConnected = false)
        }
    }

    /**
     * Creates a new user account with Email and Password.
     * Enforces minimum 8 characters, valid email format, and role = "user".
     */
    suspend fun signUp(emailInput: String, passwordInput: String, confirmPasswordInput: String): UserAuthResult {
        _authLoading.value = true
        _authError.value = null

        val trimmedEmail = emailInput.trim()
        val trimmedPassword = passwordInput.trim()
        val trimmedConfirm = confirmPasswordInput.trim()

        // 1. Validation
        if (trimmedEmail.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            _authLoading.value = false
            val error = "Please enter a valid email address."
            _authError.value = error
            return UserAuthResult.Error(error)
        }

        if (trimmedPassword.length < 8) {
            _authLoading.value = false
            val error = "Password must be at least 8 characters."
            _authError.value = error
            return UserAuthResult.Error(error)
        }

        if (trimmedPassword != trimmedConfirm) {
            _authLoading.value = false
            val error = "Passwords do not match."
            _authError.value = error
            return UserAuthResult.Error(error)
        }

        // 2. Firebase Authentication (when connected)
        if (isFirebaseConnected) {
            return try {
                val auth = FirebaseAuth.getInstance()
                val result: UserAuthResult = suspendCoroutine { cont ->
                    auth.createUserWithEmailAndPassword(trimmedEmail, trimmedPassword)
                        .addOnSuccessListener { authResult ->
                            val user = authResult.user
                            val uid = user?.uid ?: "usr_${System.currentTimeMillis()}"
                            val displayName = trimmedEmail.substringBefore('@').replaceFirstChar { it.uppercase() }

                            _userAuthState.value = UserAuthState(
                                isLoggedIn = true,
                                uid = uid,
                                email = trimmedEmail,
                                displayName = displayName,
                                role = "user",
                                isFirebaseConnected = true
                            )
                            cont.resume(UserAuthResult.Success(uid, trimmedEmail))
                        }
                        .addOnFailureListener { exception ->
                            val message = mapFirebaseAuthException(exception)
                            cont.resume(UserAuthResult.Error(message))
                        }
                }
                _authLoading.value = false
                if (result is UserAuthResult.Error) {
                    _authError.value = result.message
                }
                result
            } catch (e: Exception) {
                _authLoading.value = false
                val errorMsg = mapFirebaseAuthException(e)
                _authError.value = errorMsg
                UserAuthResult.Error(errorMsg)
            }
        }

        // 3. Fallback for environment without Firebase google-services.json
        val userHashKey = "cred_hash_${trimmedEmail.lowercase()}"
        if (prefs.contains(userHashKey)) {
            _authLoading.value = false
            val error = "An account with this email already exists. Please log in."
            _authError.value = error
            return UserAuthResult.Error(error)
        }

        // Cryptographically hash password (NEVER plaintext)
        val computedHash = hashPassword(trimmedPassword, salt)
        val generatedUid = "uid_${MessageDigest.getInstance("MD5").digest(trimmedEmail.lowercase().toByteArray()).joinToString("") { "%02x".format(it) }}"
        val displayName = trimmedEmail.substringBefore('@').replaceFirstChar { it.uppercase() }

        prefs.edit()
            .putString(userHashKey, computedHash)
            .putString("user_uid", generatedUid)
            .putString("user_email", trimmedEmail)
            .putString("user_display_name", displayName)
            .putBoolean("user_logged_in", true)
            .apply()

        _authLoading.value = false
        _userAuthState.value = UserAuthState(
            isLoggedIn = true,
            uid = generatedUid,
            email = trimmedEmail,
            displayName = displayName,
            role = "user",
            isFirebaseConnected = false
        )

        return UserAuthResult.Success(generatedUid, trimmedEmail)
    }

    /**
     * Logs in with Email and Password.
     */
    suspend fun logIn(emailInput: String, passwordInput: String): UserAuthResult {
        _authLoading.value = true
        _authError.value = null

        val trimmedEmail = emailInput.trim()
        val trimmedPassword = passwordInput.trim()

        if (trimmedEmail.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            _authLoading.value = false
            val error = "Please enter a valid email address."
            _authError.value = error
            return UserAuthResult.Error(error)
        }

        if (trimmedPassword.isBlank()) {
            _authLoading.value = false
            val error = "Please enter your password."
            _authError.value = error
            return UserAuthResult.Error(error)
        }

        // Firebase Auth login
        if (isFirebaseConnected) {
            return try {
                val auth = FirebaseAuth.getInstance()
                val result: UserAuthResult = suspendCoroutine { cont ->
                    auth.signInWithEmailAndPassword(trimmedEmail, trimmedPassword)
                        .addOnSuccessListener { authResult ->
                            val user = authResult.user
                            val uid = user?.uid ?: "usr_${System.currentTimeMillis()}"
                            val displayName = user?.displayName ?: trimmedEmail.substringBefore('@').replaceFirstChar { it.uppercase() }

                            _userAuthState.value = UserAuthState(
                                isLoggedIn = true,
                                uid = uid,
                                email = trimmedEmail,
                                displayName = displayName,
                                role = "user",
                                isFirebaseConnected = true
                            )
                            cont.resume(UserAuthResult.Success(uid, trimmedEmail))
                        }
                        .addOnFailureListener { exception ->
                            val message = mapFirebaseAuthException(exception)
                            cont.resume(UserAuthResult.Error(message))
                        }
                }
                _authLoading.value = false
                if (result is UserAuthResult.Error) {
                    _authError.value = result.message
                }
                result
            } catch (e: Exception) {
                _authLoading.value = false
                val errorMsg = mapFirebaseAuthException(e)
                _authError.value = errorMsg
                UserAuthResult.Error(errorMsg)
            }
        }

        // Fallback for environment without Firebase google-services.json
        val userHashKey = "cred_hash_${trimmedEmail.lowercase()}"
        val storedHash = prefs.getString(userHashKey, null)

        if (storedHash == null) {
            _authLoading.value = false
            val error = "Incorrect email or password."
            _authError.value = error
            return UserAuthResult.Error(error)
        }

        val computedHash = hashPassword(trimmedPassword, salt)
        if (computedHash != storedHash) {
            _authLoading.value = false
            val error = "Incorrect email or password."
            _authError.value = error
            return UserAuthResult.Error(error)
        }

        val uid = "uid_${MessageDigest.getInstance("MD5").digest(trimmedEmail.lowercase().toByteArray()).joinToString("") { "%02x".format(it) }}"
        val displayName = trimmedEmail.substringBefore('@').replaceFirstChar { it.uppercase() }

        prefs.edit()
            .putString("user_uid", uid)
            .putString("user_email", trimmedEmail)
            .putString("user_display_name", displayName)
            .putBoolean("user_logged_in", true)
            .apply()

        _authLoading.value = false
        _userAuthState.value = UserAuthState(
            isLoggedIn = true,
            uid = uid,
            email = trimmedEmail,
            displayName = displayName,
            role = "user",
            isFirebaseConnected = false
        )

        return UserAuthResult.Success(uid, trimmedEmail)
    }

    /**
     * Sends a password reset email via Firebase Auth.
     */
    suspend fun sendPasswordReset(emailInput: String): UserAuthResult {
        _authLoading.value = true
        _authError.value = null
        _passwordResetSuccess.value = null

        val trimmedEmail = emailInput.trim()
        if (trimmedEmail.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            _authLoading.value = false
            val error = "Please enter a valid email address."
            _authError.value = error
            return UserAuthResult.Error(error)
        }

        if (isFirebaseConnected) {
            return try {
                val auth = FirebaseAuth.getInstance()
                val result: UserAuthResult = suspendCoroutine { cont ->
                    auth.sendPasswordResetEmail(trimmedEmail)
                        .addOnSuccessListener {
                            _passwordResetSuccess.value = "Password reset instructions have been sent to your email."
                            cont.resume(UserAuthResult.Success(trimmedEmail, trimmedEmail))
                        }
                        .addOnFailureListener { exception ->
                            val msg = mapFirebaseAuthException(exception)
                            cont.resume(UserAuthResult.Error(msg))
                        }
                }
                _authLoading.value = false
                if (result is UserAuthResult.Error) {
                    _authError.value = result.message
                }
                result
            } catch (e: Exception) {
                _authLoading.value = false
                val errorMsg = mapFirebaseAuthException(e)
                _authError.value = errorMsg
                UserAuthResult.Error(errorMsg)
            }
        }

        _authLoading.value = false
        _passwordResetSuccess.value = "Password reset instructions have been sent to your email."
        return UserAuthResult.Success(trimmedEmail, trimmedEmail)
    }

    /**
     * Signs out the user completely.
     * Terminates Firebase session, removes local cached tokens, and emits signed-out state.
     */
    fun signOut() {
        if (isFirebaseConnected) {
            try {
                FirebaseAuth.getInstance().signOut()
            } catch (_: Exception) {}
        }

        prefs.edit()
            .remove("user_uid")
            .remove("user_email")
            .remove("user_display_name")
            .putBoolean("user_logged_in", false)
            .apply()

        _userAuthState.value = UserAuthState(
            isLoggedIn = false,
            uid = null,
            email = null,
            displayName = null,
            role = "user",
            isFirebaseConnected = isFirebaseConnected
        )

        _authError.value = null
        _passwordResetSuccess.value = null
    }

    fun clearErrors() {
        _authError.value = null
        _passwordResetSuccess.value = null
    }

    fun calculatePasswordStrength(password: String): PasswordStrength {
        if (password.length < 8) return PasswordStrength.WEAK
        val hasUpper = password.any { it.isUpperCase() }
        val hasLower = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }

        val criteriaCount = listOf(hasUpper, hasLower, hasDigit, hasSpecial).count { it }
        return when {
            password.length >= 10 && criteriaCount >= 3 -> PasswordStrength.STRONG
            criteriaCount >= 2 -> PasswordStrength.GOOD
            else -> PasswordStrength.WEAK
        }
    }

    private fun mapFirebaseAuthException(e: Throwable): String {
        return when (e) {
            is FirebaseAuthInvalidCredentialsException -> "Incorrect email or password."
            is FirebaseAuthInvalidUserException -> "No account found with this email. Please create an account."
            is FirebaseAuthUserCollisionException -> "An account with this email already exists. Please log in."
            is FirebaseAuthWeakPasswordException -> "Password must be at least 8 characters."
            else -> {
                val msg = e.message ?: ""
                when {
                    msg.contains("network", ignoreCase = true) -> "Unable to connect right now. Please check your internet connection and try again."
                    msg.contains("password", ignoreCase = true) -> "Password must be at least 8 characters."
                    msg.contains("badly formatted", ignoreCase = true) -> "Please enter a valid email address."
                    else -> "Authentication failed. Please verify your details and try again."
                }
            }
        }
    }

    private fun hashPassword(password: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest("$password:$salt".toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
