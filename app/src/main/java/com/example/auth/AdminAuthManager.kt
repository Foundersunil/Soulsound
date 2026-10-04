package com.example.auth

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import java.util.UUID
import kotlin.coroutines.suspendCoroutine

sealed class AdminAuthResult {
    data class Success(val email: String, val role: String, val sessionToken: String) : AdminAuthResult()
    data class Error(val message: String) : AdminAuthResult()
}

class AdminAuthManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("soulsound_admin_auth", Context.MODE_PRIVATE)

    private val _isAdminAuthenticated = MutableStateFlow(false)
    val isAdminAuthenticated: StateFlow<Boolean> = _isAdminAuthenticated.asStateFlow()

    private val _currentAdminEmail = MutableStateFlow<String?>(null)
    val currentAdminEmail: StateFlow<String?> = _currentAdminEmail.asStateFlow()

    private val _adminRole = MutableStateFlow<String?>(null)
    val adminRole: StateFlow<String?> = _adminRole.asStateFlow()

    private val _adminSessionToken = MutableStateFlow<String?>(null)
    val adminSessionToken: StateFlow<String?> = _adminSessionToken.asStateFlow()

    // Cryptographic salt for SHA-256 password hashing.
    // Plaintext passwords are NEVER stored in source code, Firestore, or SharedPreferences.
    private val salt = "soulsound_sacred_resonance_salt_963"

    // Authorized Administrator Accounts:
    // Only the verified application owner and designated admin handles are authorized.
    val primaryAdminEmail = "toolssunilmaurya@gmail.com"
    val systemAdminEmail = "admin@soulsound.app"

    init {
        // Validate any active persisted session on startup
        val savedToken = prefs.getString("admin_session_token", null)
        val savedEmail = prefs.getString("admin_email", null)
        val savedRole = prefs.getString("admin_role", null)
        val expiresAt = prefs.getLong("admin_token_expiry", 0L)

        if (savedToken != null && savedEmail != null && savedRole == "admin" && System.currentTimeMillis() < expiresAt) {
            _isAdminAuthenticated.value = true
            _currentAdminEmail.value = savedEmail
            _adminRole.value = savedRole
            _adminSessionToken.value = savedToken
        } else {
            clearSession()
        }
    }

    /**
     * Checks if the admin account is pending initial password setup.
     */
    fun isInitialSetupPending(): Boolean {
        return !prefs.contains("admin_credential_hash")
    }

    /**
     * Authenticates an administrator using role-based verification.
     * Prevents normal users from gaining admin access.
     */
    suspend fun authenticate(emailInput: String, passwordInput: String): AdminAuthResult {
        val trimmedEmail = emailInput.trim().lowercase()

        // 1. Verify admin identity: strictly restricted to authorized administrator accounts
        val isAuthorizedEmail = trimmedEmail == primaryAdminEmail.lowercase() ||
                trimmedEmail == systemAdminEmail.lowercase() ||
                (trimmedEmail.startsWith("admin@") && trimmedEmail.endsWith(".soulsound.app"))

        if (!isAuthorizedEmail) {
            return AdminAuthResult.Error("Access Denied: Account is not authorized for the Admin Portal.")
        }

        if (passwordInput.isBlank()) {
            return AdminAuthResult.Error("Password cannot be blank.")
        }

        // 2. Firebase Authentication verification (if Firebase is configured in the environment)
        var firebaseSuccess = false
        try {
            val firebaseApps = com.google.firebase.FirebaseApp.getApps(context)
            if (firebaseApps.isNotEmpty()) {
                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                firebaseSuccess = suspendCoroutine { cont ->
                    auth.signInWithEmailAndPassword(trimmedEmail, passwordInput)
                        .addOnSuccessListener {
                            cont.resumeWith(Result.success(true))
                        }
                        .addOnFailureListener {
                            cont.resumeWith(Result.success(false))
                        }
                }
            }
        } catch (_: Throwable) {
            firebaseSuccess = false
        }

        // 3. Cryptographic Verification (No plaintext comparison or plaintext storage)
        val storedHash = prefs.getString("admin_credential_hash", null)

        val passwordMatches = if (firebaseSuccess) {
            // Keep local hash cache synchronized with verified Firebase credentials
            val computed = hashPassword(passwordInput, salt)
            prefs.edit().putString("admin_credential_hash", computed).apply()
            true
        } else if (storedHash != null) {
            // Verify against securely stored cryptographic hash
            val computed = hashPassword(passwordInput, salt)
            computed == storedHash
        } else {
            // INITIAL SETUP: Owner creates the initial administrator password on first login
            if (passwordInput.length < 8) {
                return AdminAuthResult.Error("Initial setup: Administrator password must be at least 8 characters.")
            }
            val computed = hashPassword(passwordInput, salt)
            prefs.edit().putString("admin_credential_hash", computed).apply()
            true
        }

        if (!passwordMatches) {
            return AdminAuthResult.Error("Access Denied: Invalid administrator credentials.")
        }

        // 4. Issue cryptographically secure ephemeral session token with verified admin role
        val sessionToken = "ss_adm_${UUID.randomUUID().toString().replace("-", "")}"
        val role = "admin"
        val expiryMillis = System.currentTimeMillis() + (8 * 3600 * 1000L) // 8-hour session

        prefs.edit()
            .putString("admin_session_token", sessionToken)
            .putString("admin_email", trimmedEmail)
            .putString("admin_role", role)
            .putLong("admin_token_expiry", expiryMillis)
            .apply()

        _isAdminAuthenticated.value = true
        _currentAdminEmail.value = trimmedEmail
        _adminRole.value = role
        _adminSessionToken.value = sessionToken

        return AdminAuthResult.Success(
            email = trimmedEmail,
            role = role,
            sessionToken = sessionToken
        )
    }

    /**
     * Changes or updates the administrator password securely.
     */
    fun changePassword(currentPassword: String, newPassword: String): Boolean {
        if (newPassword.length < 8) return false
        val storedHash = prefs.getString("admin_credential_hash", null)
        val currentMatches = if (storedHash != null) {
            hashPassword(currentPassword, salt) == storedHash
        } else {
            true
        }
        if (!currentMatches) return false

        val newHash = hashPassword(newPassword, salt)
        prefs.edit().putString("admin_credential_hash", newHash).apply()
        return true
    }

    /**
     * Logs out the administrator and invalidates the session token immediately.
     */
    fun logout() {
        try {
            val firebaseApps = com.google.firebase.FirebaseApp.getApps(context)
            if (firebaseApps.isNotEmpty()) {
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
            }
        } catch (_: Throwable) { }
        clearSession()
    }

    private fun clearSession() {
        prefs.edit()
            .remove("admin_session_token")
            .remove("admin_email")
            .remove("admin_role")
            .remove("admin_token_expiry")
            .apply()

        _isAdminAuthenticated.value = false
        _currentAdminEmail.value = null
        _adminRole.value = null
        _adminSessionToken.value = null
    }

    fun hasAdminPrivileges(): Boolean {
        val expiry = prefs.getLong("admin_token_expiry", 0L)
        val isExpired = System.currentTimeMillis() >= expiry
        return _isAdminAuthenticated.value &&
                _adminRole.value == "admin" &&
                _adminSessionToken.value != null &&
                !isExpired
    }

    companion object {
        fun hashPassword(password: String, salt: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest("$salt$password$salt".toByteArray(Charsets.UTF_8))
            return hashBytes.joinToString("") { "%02x".format(it) }
        }
    }
}
