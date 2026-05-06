package com.example.pangeo.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

/**
 * Pantalla de registro de nuevos usuarios.
 * * Provee un formulario completo para la creación de perfiles de "explorador",
 * incluyendo validaciones de seguridad local (coincidencia de contraseñas)
 * y gestión de registro en Firebase Auth.
 *
 * @param viewModel Instancia de [AuthViewModel] para procesar el registro y observar el estado.
 * @param onNavigateBack Callback para regresar a la pantalla de Login tras el registro o cancelación.
 */
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    // Estados reactivos para los campos del formulario
    var nickname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Control de visibilidad de campos sensibles y diálogos
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsState()
    val scrollState = rememberScrollState()

    // Configuración de tipografía con fallback de seguridad
    val caveatFamily = remember {
        try { FontFamily(Font(R.font.caveat)) } catch (e: Exception) { FontFamily.Serif }
    }

    /**
     * Observador de éxito en el registro:
     * Al recibir [AuthState.Success], se activa el diálogo que instruye al usuario
     * sobre la verificación de correo electrónico.
     */
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            showSuccessDialog = true
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFDFDFD))) {
        // Fondo decorativo con opacidad reducida para no comprometer la legibilidad
        Image(
            painter = painterResource(id = R.drawable.mapamundo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().alpha(0.10f)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState) // Soporte para pantallas pequeñas o teclados abiertos
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            }

            Spacer(modifier = Modifier.height(5.dp))

            // Branding y Call to Action (CTA)
            Text("Nueva Expedición", fontSize = 48.sp, fontFamily = caveatFamily, fontWeight = FontWeight.ExtraBold, color = Color.Black)
            Text("Crea tu perfil de explorador", fontSize = 24.sp, fontFamily = caveatFamily, color = Color.Gray)

            Spacer(modifier = Modifier.height(20.dp))

            // Entrada de datos del perfil
            OutlinedTextField(
                value = nickname,
                onValueChange = { if (it.length <= 15) nickname = it }, // Limitación de caracteres local
                label = { Text("¿Cómo te llamas?", fontFamily = FontFamily.SansSerif) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Default, fontSize = 16.sp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico", fontFamily = FontFamily.SansSerif) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Default, fontSize = 16.sp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña", fontFamily = FontFamily.SansSerif) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Default, fontSize = 16.sp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = null, tint = Color.Gray)
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirma tu contraseña", fontFamily = FontFamily.SansSerif) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Default, fontSize = 16.sp),
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(imageVector = image, contentDescription = null, tint = Color.Gray)
                    }
                }
            )

            /**
             * Lógica de Validación de Seguridad:
             * Se comprueba que la contraseña cumpla los requisitos de fuerza
             * y que ambas coincidan.
             */
            val isPasswordStrong = password.length >= 8 && 
                                  password.any { it.isUpperCase() } && 
                                  password.any { it.isDigit() }
            val passMismatch = password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword

            if ((password.isNotEmpty() && !isPasswordStrong) || passMismatch || authState is AuthState.Error) {
                val errorMsg = when {
                    password.isNotEmpty() && !isPasswordStrong -> "La contraseña debe tener al menos 8 caracteres, una mayúscula y un número"
                    passMismatch -> "Las contraseñas no coinciden"
                    authState is AuthState.Error -> (authState as AuthState.Error).message
                    else -> ""
                }
                
                if (errorMsg.isNotEmpty()) {
                    Text(
                        text = errorMsg,
                        color = Color.Red,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(25.dp))

            // Ejecución del registro subordinado a las validaciones locales
            Button(
                onClick = {
                    if (nickname.isNotBlank() && email.isNotBlank() && isPasswordStrong && !passMismatch) {
                        viewModel.register(email, password, nickname)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(16.dp),
                enabled = authState !is AuthState.Loading && password.isNotEmpty() && isPasswordStrong && !passMismatch
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Empezar aventura", fontSize = 18.sp, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onNavigateBack() }
            ) {
                Text("¿Ya tienes cuenta? ", fontSize = 15.sp, color = Color.DarkGray, fontFamily = FontFamily.SansSerif)
                Text("Inicia sesión", fontSize = 15.sp, color = Color(0xFF4A69FF), fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        // Modal de éxito y flujo de activación de cuenta (Email Verification)
        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = { },
                confirmButton = {
                    Button(
                        onClick = {
                            showSuccessDialog = false
                            viewModel.resetState() // Limpiar estado antes de navegar
                            onNavigateBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                    ) {
                        Text("Entendido", fontFamily = FontFamily.SansSerif)
                    }
                },
                title = { Text("¡Casi eres un Explorador!", fontFamily = caveatFamily, fontSize = 26.sp, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Hemos enviado un enlace a $email.\n\nPor favor, verifica tu correo antes de iniciar sesión para activar tu perfil.",
                        fontFamily = FontFamily.Default,
                        fontSize = 15.sp
                    )
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color.White
            )
        }
    }
}