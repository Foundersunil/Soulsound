package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.PasswordStrength
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.OrangePrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSubtle

@Composable
fun UserSignUpScreen(
    onSignUpSubmit: (email: String, password: String, confirmPassword: String) -> Unit,
    onNavigateToLogin: () -> Unit,
    onContinueAsGuest: () -> Unit,
    errorMessage: String?,
    isLoading: Boolean,
    onCalculateStrength: (String) -> PasswordStrength,
    modifier: Modifier = Modifier
) {
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var emailInlineError by remember { mutableStateOf<String?>(null) }
    var passwordInlineError by remember { mutableStateOf<String?>(null) }
    var confirmInlineError by remember { mutableStateOf<String?>(null) }

    val passwordStrength = remember(passwordInput) { onCalculateStrength(passwordInput) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // App Emblem
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(DarkCardElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = OrangePrimary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Title & Subtitle
            Text(
                text = "Create Your SoulSound Account",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Start your personal sound & frequency journey.",
                color = TextMuted,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Server / Auth Error Banner
            AnimatedVisibility(visible = !errorMessage.isNullOrBlank()) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1010)),
                    border = BorderStroke(1.dp, Color(0xFF8B2020)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFFFCDD2),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Registration Form Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Email Field
                    Text(
                        text = "Email Address",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = {
                            emailInput = it
                            if (emailInlineError != null) emailInlineError = null
                        },
                        placeholder = { Text("you@example.com", color = TextSubtle) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = OrangePrimary)
                        },
                        isError = emailInlineError != null,
                        supportingText = {
                            if (emailInlineError != null) {
                                Text(emailInlineError!!, color = Color(0xFFFF5252), fontSize = 11.sp)
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("user_signup_email_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = BorderSubtle,
                            errorBorderColor = Color(0xFFFF5252),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Password Field
                    Text(
                        text = "Password",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = {
                            passwordInput = it
                            if (passwordInlineError != null) passwordInlineError = null
                            if (confirmInlineError != null && confirmPasswordInput == it) confirmInlineError = null
                        },
                        placeholder = { Text("At least 8 characters", color = TextSubtle) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = OrangePrimary)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = TextMuted
                                )
                            }
                        },
                        isError = passwordInlineError != null,
                        supportingText = {
                            if (passwordInlineError != null) {
                                Text(passwordInlineError!!, color = Color(0xFFFF5252), fontSize = 11.sp)
                            } else {
                                Text("Password must be at least 8 characters.", color = TextSubtle, fontSize = 11.sp)
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("user_signup_password_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = BorderSubtle,
                            errorBorderColor = Color(0xFFFF5252),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Password Strength Indicator
                    if (passwordInput.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Strength: ${passwordStrength.label}",
                                color = Color(passwordStrength.colorHex),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            val progress = when (passwordStrength) {
                                PasswordStrength.WEAK -> 0.33f
                                PasswordStrength.GOOD -> 0.66f
                                PasswordStrength.STRONG -> 1.0f
                            }
                            LinearProgressIndicator(
                                progress = { progress },
                                color = Color(passwordStrength.colorHex),
                                trackColor = DarkCardElevated,
                                modifier = Modifier
                                    .width(90.dp)
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Confirm Password Field
                    Text(
                        text = "Confirm Password",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = confirmPasswordInput,
                        onValueChange = {
                            confirmPasswordInput = it
                            if (confirmInlineError != null) confirmInlineError = null
                        },
                        placeholder = { Text("Re-enter your password", color = TextSubtle) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = OrangePrimary)
                        },
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                                    tint = TextMuted
                                )
                            }
                        },
                        isError = confirmInlineError != null,
                        supportingText = {
                            if (confirmInlineError != null) {
                                Text(confirmInlineError!!, color = Color(0xFFFF5252), fontSize = 11.sp)
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("user_signup_confirm_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = BorderSubtle,
                            errorBorderColor = Color(0xFFFF5252),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Submit Button
                    Button(
                        onClick = {
                            var valid = true
                            val email = emailInput.trim()
                            val pass = passwordInput.trim()
                            val confirm = confirmPasswordInput.trim()

                            if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                emailInlineError = "Please enter a valid email address."
                                valid = false
                            }

                            if (pass.length < 8) {
                                passwordInlineError = "Password must be at least 8 characters."
                                valid = false
                            }

                            if (pass != confirm) {
                                confirmInlineError = "Passwords do not match."
                                valid = false
                            }

                            if (valid) {
                                onSignUpSubmit(email, pass, confirm)
                            }
                        },
                        enabled = !isLoading && emailInput.isNotBlank() && passwordInput.isNotBlank() && confirmPasswordInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("user_signup_submit_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.Black, strokeWidth = 2.5.dp)
                        } else {
                            Text("Create Account", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Navigation to Login
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Already have an account?", color = TextMuted, fontSize = 13.sp)
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.testTag("go_to_login_button")
                ) {
                    Text("Log In", color = OrangePrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            // Guest Mode Option
            OutlinedButton(
                onClick = onContinueAsGuest,
                border = BorderStroke(1.dp, BorderSubtle),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("signup_continue_as_guest_button")
            ) {
                Text("Explore as Guest", fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}
