package com.example.pangeo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import com.example.pangeo.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.runtime.State
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _userData = mutableStateOf<User?>(null)
    val userData: State<User?> = _userData

    fun register(email: String, pass: String, nickname: String) {
        _authState.value = AuthState.Loading

        // 1. COMPROBAR SI EL NICKNAME YA EXISTE
        db.collection("users")
            .whereEqualTo("nickname", nickname)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // El Nickname ya existe en Firestore
                    _authState.value = AuthState.Error("Este nickname ya está siendo usado por otro explorador.")
                } else {
                    // 2. EL NICKNAME ESTÁ LIBRE, INTENTAMOS CREAR LA CUENTA
                    auth.createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val firebaseUser = auth.currentUser

                                // 3. ENVIAR EMAIL DE VERIFICACIÓN
                                firebaseUser?.sendEmailVerification()?.addOnCompleteListener { verif ->
                                    if (verif.isSuccessful) {
                                        val uid = firebaseUser.uid
                                        val newUser = User(uid, email, nickname, 0, 1, "Recluta de Pangeo")

                                        // 4. GUARDAR EN FIRESTORE
                                        db.collection("users").document(uid).set(newUser)
                                            .addOnSuccessListener {
                                                _userData.value = newUser
                                                _authState.value = AuthState.Success
                                                auth.signOut()
                                            }
                                    }
                                }
                            } else {
                                // 5. CONTROLAR EMAIL REPETIDO ESPECÍFICAMENTE
                                val exception = task.exception
                                val msg = when (exception) {
                                    is com.google.firebase.auth.FirebaseAuthUserCollisionException ->
                                        "Este email ya está registrado. Intenta iniciar sesión."
                                    is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ->
                                        "El formato del email no es válido."
                                    else -> exception?.message ?: "Error al registrar cuenta."
                                }
                                _authState.value = AuthState.Error(msg)
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                // Este solo salta si NO hay internet o Firestore está caído
                _authState.value = AuthState.Error("Error de red: ${e.localizedMessage}")
            }
    }
    fun login(email: String, pass: String) {
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.reload()?.addOnCompleteListener {
                        if (user.isEmailVerified) {
                            fetchUserData(user.uid)
                        } else {
                            auth.signOut()
                            _authState.value = AuthState.Error("Verifica tu correo antes de entrar.")
                        }
                    }
                } else {
                    _authState.value = AuthState.Error("Email o contraseña incorrectos.")
                }
            }
    }

    fun fetchUserData(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                if (user != null) {
                    _userData.value = user
                    _authState.value = AuthState.Success
                }
            }
    }
    // Dentro de la clase AuthViewModel
    fun loginConGoogle(idToken: String) {
        _authState.value = AuthState.Loading
        val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    val uid = firebaseUser?.uid ?: ""

                    // Comprobamos si el usuario ya existe en Firestore
                    db.collection("users").document(uid).get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                fetchUserData(uid)
                            } else {
                                // Si es nuevo, creamos su perfil
                                val newUser = User(
                                    uid = uid,
                                    email = firebaseUser?.email ?: "",
                                    nickname = firebaseUser?.displayName ?: "Explorador",
                                    xp = 0,
                                    level = 1,
                                    rank = "Recluta de Pangeo"
                                )
                                db.collection("users").document(uid).set(newUser)
                                    .addOnSuccessListener {
                                        _userData.value = newUser
                                        _authState.value = AuthState.Success
                                    }
                            }
                        }
                } else {
                    _authState.value = AuthState.Error("Fallo al autenticar con Google")
                }
            }
    }
    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _authState.value = AuthState.Error("Introduce tu email para recuperar la contraseña.")
            return
        }

        _authState.value = AuthState.Loading
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Success // Usamos Success para avisar que se envió
                } else {
                    _authState.value = AuthState.Error("No pudimos enviar el correo. Revisa si el email es correcto.")
                }
            }
    }

    fun resetState() { _authState.value = AuthState.Idle }
    fun logout() { auth.signOut(); _userData.value = null; _authState.value = AuthState.Idle }
}