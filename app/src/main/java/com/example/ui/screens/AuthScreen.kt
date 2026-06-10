package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.MainViewModel
import com.example.ui.theme.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.example.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(viewModel: MainViewModel) {
    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    val error by viewModel.authError.collectAsState()
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
            account?.idToken?.let { idToken ->
                viewModel.loginWithGoogle(idToken)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            viewModel.setError("Google Sign-In failed: ${e.message}")
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = FintechBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isSignUp) "Create Account" else "Welcome Back",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 32.sp,
                color = FintechOnSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Manage your finances effortlessly",
                fontSize = 14.sp,
                color = FintechOnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(48.dp))

            if (isSignUp) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = FintechOnSurface,
                        unfocusedTextColor = FintechOnSurfaceVariant,
                        focusedBorderColor = FintechPrimary,
                        unfocusedBorderColor = FintechOutline.copy(alpha = 0.3f),
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = FintechOnSurface,
                    unfocusedTextColor = FintechOnSurfaceVariant,
                    focusedBorderColor = FintechPrimary,
                    unfocusedBorderColor = FintechOutline.copy(alpha = 0.3f),
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = FintechOnSurface,
                    unfocusedTextColor = FintechOnSurfaceVariant,
                    focusedBorderColor = FintechPrimary,
                    unfocusedBorderColor = FintechOutline.copy(alpha = 0.3f),
                )
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (error != null) {
                Text(text = error ?: "", color = FintechError, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = {
                    if (isSignUp) viewModel.signup(name, email, password)
                    else viewModel.login(email, password)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FintechPrimary, contentColor = FintechOnPrimary)
            ) {
                Text(if (isSignUp) "Sign Up" else "Login", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                        .requestEmail()
                        .build()
                    val googleSignInClient = GoogleSignIn.getClient(context, gso)
                    launcher.launch(googleSignInClient.signInIntent)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, FintechOutline.copy(alpha = 0.3f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = FintechOnSurface)
            ) {
                Text("Continue with Google", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))

            TextButton(onClick = {
                isSignUp = !isSignUp
                viewModel.clearError()
            }) {
                Text(
                    text = if (isSignUp) "Already have an account? Login" else "Don't have an account? Sign Up",
                    color = FintechSecondary
                )
            }

            if (!isSignUp) {
                TextButton(onClick = { 
                    if (email.isNotBlank()) {
                        viewModel.resetPassword(email)
                    } else {
                        viewModel.setError("Please enter your email address to reset your password.")
                    }
                }) {
                    Text("Forgot Password?", color = FintechOnSurfaceVariant)
                }
            }
        }
    }
}
