package com.example.pangeo.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pangeo.R
import com.example.pangeo.viewmodel.AuthViewModel
import com.example.pangeo.viewmodel.AuthState

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    // Estados de los campos
    var nickname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Visibilidad de contraseñas
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Estado para el mensaje de éxito (Diálogo)
    var showSuccessDialog by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsState()

    // Tipografías
    val caveatFamily = remember { try { FontFamily(Font(R.font.caveat)) } catch (e: Exception) { FontFamily.Serif } }

    // Control del éxito en el registro
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            showSuccessDialog = true
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFDFDFD))) {
        // Fondo con mapa
        Image(
            painter = painterResource(id = R.drawable.mapamundo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().alpha(0.10f)
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Botón superior de atrás
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text("Nueva Expedición", fontSize = 55.sp, fontFamily = caveatFamily, fontWeight = FontWeight.ExtraBold, color = Color.Black)
            Text("Crea tu perfil de explorador", fontSize = 28.sp, fontFamily = caveatFamily, color = Color.Gray, modifier = Modifier.padding(bottom = 20.dp))

            // CAMPO NICKNAME
            OutlinedTextField(
                value = nickname,
                onValueChange = { if (it.length <= 15) nickname = it },
                label = { Text("¿Cómo te llamas?", fontFamily = caveatFamily, fontSize = 18.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // CAMPO EMAIL
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico", fontFamily = caveatFamily, fontSize = 18.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // CAMPO CONTRASEÑA
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña", fontFamily = caveatFamily, fontSize = 18.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = null, tint = Color.Gray)
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // CAMPO CONFIRMAR CONTRASEÑA
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirma tu contraseña", fontFamily = caveatFamily, fontSize = 18.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(imageVector = image, contentDescription = null, tint = Color.Gray)
                    }
                }
            )

            // Validación de errores
            val passMismatch = password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword

            if (passMismatch || authState is AuthState.Error) {
                Text(
                    text = if (passMismatch) "Las contraseñas no coinciden" else (authState as AuthState.Error).message,
                    color = Color.Red,
                    fontFamily = caveatFamily,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // BOTÓN REGISTRAR
            Button(
                onClick = {
                    if (nickname.isNotBlank() && email.isNotBlank() && !passMismatch) {
                        viewModel.register(email, password, nickname)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(16.dp),
                enabled = authState !is AuthState.Loading && password.isNotEmpty() && !passMismatch
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Empezar aventura", fontSize = 24.sp, fontFamily = caveatFamily, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Botón para volver al Login
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onNavigateBack() }
            ) {
                Text("¿Ya tienes cuenta? ", fontSize = 18.sp, color = Color.DarkGray, fontFamily = caveatFamily)
                Text("Inicia sesión", fontSize = 18.sp, color = Color(0xFF4A69FF), fontWeight = FontWeight.Bold, fontFamily = caveatFamily)
            }
        }

        // --- DIÁLOGO DE ÉXITO Y VERIFICACIÓN ---
        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = { },
                confirmButton = {
                    Button(
                        onClick = {
                            showSuccessDialog = false
                            viewModel.resetState() // Importante limpiar estado
                            onNavigateBack() // Ir al login
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                    ) {
                        Text("Entendido", fontFamily = caveatFamily)
                    }
                },
                title = { Text("¡Casi eres un Explorador!", fontFamily = caveatFamily, fontSize = 26.sp) },
                text = {
                    Text(
                        "Hemos enviado un enlace a $email.\n\nPor favor, verifica tu correo antes de iniciar sesión para activar tu perfil.",
                        fontFamily = caveatFamily,
                        fontSize = 18.sp
                    )
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color.White
            )
        }
    }
}