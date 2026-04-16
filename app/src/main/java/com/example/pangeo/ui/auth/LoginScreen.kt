package com.example.pangeo.ui.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pangeo.R
import com.example.pangeo.viewmodel.AuthState
import com.example.pangeo.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.airbnb.lottie.compose.*

/**
 * Pantalla de autenticación principal de PanGeo.
 * * Gestiona el flujo de entrada mediante credenciales tradicionales (Email/Password)
 * y autenticación de terceros a través de Google Sign-In. Utiliza animaciones Lottie
 * para mejorar la experiencia de usuario (UX) y mantiene una identidad visual
 * coherente mediante tipografías personalizadas.
 *
 * @param viewModel Instancia de [AuthViewModel] para la gestión de estados y lógica de Firebase.
 * @param onNavigateToRegister Callback para redirigir al flujo de creación de cuenta.
 * @param onLoginSuccess Callback que se dispara tras una autenticación exitosa.
 */
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    // Estados locales para el control de formularios y diálogos informativos
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showResetDialog by remember { mutableStateOf(false) }

    // Observación del flujo de estado de autenticación desde el ViewModel
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    /**
     * Gestión de recursos tipográficos:
     * Se implementa un manejo de excepciones para prevenir fallos en el renderizado
     * si los recursos de fuente no están disponibles en tiempo de ejecución.
     */
    val molleFamily = remember { try { FontFamily(Font(R.font.molle)) } catch (e: Exception) { FontFamily.Serif } }
    val caveatFamily = remember { try { FontFamily(Font(R.font.caveat)) } catch (e: Exception) { FontFamily.Serif } }

    /**
     * Configuración de Animación Lottie:
     * Renderiza un globo terráqueo 3D en bucle infinito para reforzar el branding geográfico.
     */
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.globe_3d))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    /**
     * Configuración de Google Sign-In:
     * El requestIdToken es esencial para intercambiar el éxito de Google por
     * una credencial válida en Firebase Auth.
     */
    val googleSignInOptions = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, googleSignInOptions) }

    /**
     * Launcher para el resultado de la actividad de Google.
     * Maneja el retorno del flujo externo y extrae el ID Token necesario para el backend.
     */
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let { token ->
                viewModel.loginConGoogle(token)
            }
        } catch (e: ApiException) {
            // Error en la comunicación con los servicios de Google
            println("Error de Google Sign In: ${e.message}")
        }
    }

    /**
     * Efecto secundario (Side Effect) para la navegación reactiva.
     * Reacciona a cambios en [AuthState]. Si el éxito se debe a un login normal, navega.
     * Si se detecta un éxito sin contraseña, se asume un flujo de recuperación de cuenta.
     */
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            if (password.isEmpty() && email.isNotEmpty()) {
                showResetDialog = true
                viewModel.resetState()
            } else if (password.isNotEmpty()) {
                onLoginSuccess()
            }
        }
    }

    // Modal informativo para la recuperación de contraseña o avisos del sistema
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            confirmButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Entendido", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold)
                }
            },
            title = { Text("Información", fontFamily = caveatFamily, fontSize = 28.sp, fontWeight = FontWeight.Bold) },
            text = { Text("Revisa tu email si has solicitado un cambio, o disfruta de tu sesión.", fontFamily = FontFamily.Default, fontSize = 16.sp) },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF2F2F2)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.6f))

            // Branding de PanGeo: Combinación de fuentes Serif y Script para identidad visual
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Text("Pan", fontSize = 55.sp, fontFamily = molleFamily, color = Color.Black)
                Text("Geo", fontSize = 70.sp, fontFamily = caveatFamily, color = Color.Black, modifier = Modifier.offset(y = (-4).dp))
            }

            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(160.dp).offset(y = (-15).dp)
            )

            Text("Inicia Sesión", fontSize = 32.sp, fontWeight = FontWeight.Bold, fontFamily = caveatFamily, color = Color.Black, modifier = Modifier.offset(y = (-15).dp))

            Spacer(modifier = Modifier.weight(0.4f))

            // Formulario de entrada: Prioriza legibilidad con SansSerif
            TextField(
                value = email, onValueChange = { email = it },
                placeholder = { Text("email@domain.com", fontFamily = FontFamily.SansSerif, fontSize = 16.sp) },
                modifier = Modifier.fillMaxWidth().border(1.dp, Color.LightGray, RoundedCornerShape(10.dp)),
                textStyle = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp),
                shape = RoundedCornerShape(10.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            TextField(
                value = password, onValueChange = { password = it },
                placeholder = { Text("Contraseña", fontFamily = FontFamily.SansSerif, fontSize = 16.sp) },
                modifier = Modifier.fillMaxWidth().border(1.dp, Color.LightGray, RoundedCornerShape(10.dp)),
                textStyle = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp),
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(10.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )

            // Lógica de recuperación de contraseña vía ViewModel
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                Text(
                    text = "¿Olvidaste tu contraseña?",
                    modifier = Modifier.padding(top = 8.dp).clickable { viewModel.resetPassword(email) },
                    fontSize = 14.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = Color.Gray,
                    textDecoration = TextDecoration.Underline
                )
            }

            Spacer(modifier = Modifier.weight(0.5f))

            // Feedback visual de errores de autenticación
            if (authState is AuthState.Error) {
                Text(
                    text = (authState as AuthState.Error).message,
                    color = Color.Red,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Acción principal de Login
            Button(
                onClick = { viewModel.login(email, password) },
                enabled = authState !is AuthState.Loading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text(
                    text = if (authState is AuthState.Loading) "Cargando..." else "Continuar",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Separador visual para métodos alternativos de login
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.Black, thickness = 1.dp)
                Text(" o ", modifier = Modifier.padding(horizontal = 8.dp), color = Color.Gray, fontFamily = FontFamily.SansSerif, fontSize = 14.sp)
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.Black, thickness = 1.dp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Autenticación con Google (OAuth 2.0)
            OutlinedButton(
                onClick = { launcher.launch(googleSignInClient.signInIntent) },
                enabled = authState !is AuthState.Loading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(2.dp, Color.Black)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(painter = painterResource(id = R.drawable.ic_google), contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Continuar con Google", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black, fontFamily = FontFamily.SansSerif)
                }
            }

            Spacer(modifier = Modifier.weight(0.4f))

            // Link de navegación hacia el registro
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onNavigateToRegister() }) {
                Text("¿Primera vez? ", fontFamily = FontFamily.SansSerif, fontSize = 15.sp, color = Color.DarkGray)
                Text("Crea una cuenta", fontSize = 15.sp, color = Color(0xFF4A69FF), fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
            }

            Spacer(modifier = Modifier.weight(0.3f))
        }
    }
}