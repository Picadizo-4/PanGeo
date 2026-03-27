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
// --- IMPORTANTE: Lottie ---
import com.airbnb.lottie.compose.*

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showResetDialog by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    val molleFamily = remember { try { FontFamily(Font(R.font.molle)) } catch (e: Exception) { FontFamily.Serif } }
    val caveatFamily = remember { try { FontFamily(Font(R.font.caveat)) } catch (e: Exception) { FontFamily.Serif } }

    // --- CONFIGURACIÓN LOTTIE ---
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.globe_3d))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    val googleSignInOptions = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, googleSignInOptions) }

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
            println("Error de Google Sign In: ${e.message}")
        }
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            if (password.isEmpty() && email.isNotEmpty()) {
                // Si el login es por Google o reset (password vacío)
                showResetDialog = true
                viewModel.resetState()
            } else if (password.isNotEmpty()) {
                onLoginSuccess()
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            confirmButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Entendido", fontFamily = caveatFamily, fontSize = 20.sp)
                }
            },
            title = { Text("Información", fontFamily = caveatFamily, fontSize = 28.sp) },
            text = { Text("Revisa tu email si has solicitado un cambio, o disfruta de tu sesión.", fontFamily = caveatFamily, fontSize = 20.sp) },
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

            // CABECERA LOGO
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Text("Pan", fontSize = 55.sp, fontFamily = molleFamily, color = Color.Black)
                Text("Geo", fontSize = 70.sp, fontFamily = caveatFamily, color = Color.Black, modifier = Modifier.offset(y = (-4).dp))
            }

            // SUSTITUCIÓN DE IMAGE POR LOTTIEANIMATION
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier
                    .size(160.dp) // Ajustado para que el layout no se rompa
                    .offset(y = (-15).dp)
            )

            Text("Inicia Sesión", fontSize = 30.sp, fontWeight = FontWeight.Bold, fontFamily = caveatFamily, color = Color.Black, modifier = Modifier.offset(y = (-15).dp))

            Spacer(modifier = Modifier.weight(0.4f))

            // CAMPO EMAIL
            TextField(
                value = email, onValueChange = { email = it },
                placeholder = { Text("email@domain.com", fontFamily = caveatFamily, fontSize = 18.sp) },
                modifier = Modifier.fillMaxWidth().border(1.dp, Color.LightGray, RoundedCornerShape(10.dp)),
                textStyle = TextStyle(fontFamily = caveatFamily, fontSize = 19.sp),
                shape = RoundedCornerShape(10.dp),
                colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // CAMPO CONTRASEÑA
            TextField(
                value = password, onValueChange = { password = it },
                placeholder = { Text("Contraseña", fontFamily = caveatFamily, fontSize = 18.sp) },
                modifier = Modifier.fillMaxWidth().border(1.dp, Color.LightGray, RoundedCornerShape(10.dp)),
                textStyle = TextStyle(fontFamily = caveatFamily, fontSize = 19.sp),
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(10.dp),
                colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
                singleLine = true
            )

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                Text(
                    text = "¿Olvidaste tu contraseña?",
                    modifier = Modifier.padding(top = 8.dp).clickable { viewModel.resetPassword(email) },
                    fontSize = 17.sp,
                    fontFamily = caveatFamily,
                    color = Color.Gray,
                    textDecoration = TextDecoration.Underline
                )
            }

            Spacer(modifier = Modifier.weight(0.5f))

            if (authState is AuthState.Error) {
                Text(text = (authState as AuthState.Error).message, color = Color.Red, fontSize = 17.sp, fontFamily = caveatFamily, textAlign = TextAlign.Center, modifier = Modifier.padding(bottom = 8.dp))
            }

            Button(
                onClick = { viewModel.login(email, password) },
                enabled = authState !is AuthState.Loading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text(if (authState is AuthState.Loading) "Cargando..." else "Continuar", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, fontFamily = caveatFamily)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.Black, thickness = 1.dp)
                Text(" o ", modifier = Modifier.padding(horizontal = 8.dp), color = Color.Gray, fontFamily = caveatFamily, fontSize = 18.sp)
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.Black, thickness = 1.dp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedButton(
                onClick = { launcher.launch(googleSignInClient.signInIntent) },
                enabled = authState !is AuthState.Loading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(2.dp, Color.Black)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(painter = painterResource(id = R.drawable.ic_google), contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Continuar con Google", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black, fontFamily = caveatFamily)
                }
            }

            Spacer(modifier = Modifier.weight(0.4f))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onNavigateToRegister() }) {
                Text("¿Primera vez? ", fontFamily = caveatFamily, fontSize = 19.sp, color = Color.DarkGray)
                Text("Crea una cuenta", fontSize = 19.sp, color = Color(0xFF4A69FF), fontWeight = FontWeight.Bold, fontFamily = caveatFamily)
            }

            Spacer(modifier = Modifier.weight(0.3f))
        }
    }
}