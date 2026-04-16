package com.example.pangeo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import com.example.pangeo.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.runtime.State
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.sqrt
import com.google.firebase.firestore.SetOptions

/**
 * Representa los posibles estados de la autenticación para control de UI.
 */
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

/**
 * ViewModel central de Autenticación, Perfil y Progresión de PanGeo.
 */
class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Estado reactivo para la navegación y feedback de UI
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    // Estado de los datos del usuario logueado
    private val _userData = mutableStateOf<User?>(null)
    val userData: State<User?> = _userData

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            // Verificamos si el email está validado o si es una cuenta de Google
            if (firebaseUser.isEmailVerified || firebaseUser.providerData.any { it.providerId == "google.com" }) {
                fetchUserData(firebaseUser.uid)
            } else {
                _authState.value = AuthState.Idle
            }
        } else {
            _authState.value = AuthState.Idle
        }
    }

    // --- FLUJOS DE AUTENTICACIÓN ---

    fun register(email: String, pass: String, nickname: String) {
        _authState.value = AuthState.Loading

        db.collection("users")
            .whereEqualTo("nickname", nickname)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    _authState.value = AuthState.Error("Este nickname ya está siendo usado.")
                } else {
                    auth.createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val firebaseUser = auth.currentUser
                                firebaseUser?.sendEmailVerification()?.addOnCompleteListener { verif ->
                                    if (verif.isSuccessful) {
                                        val uid = firebaseUser.uid
                                        val newUser = User(uid, email, nickname, 0, 1, "Recluta de Pangeo")
                                        db.collection("users").document(uid).set(newUser)
                                            .addOnSuccessListener {
                                                auth.signOut()
                                                _authState.value = AuthState.Error("Cuenta creada. Verifica tu email para entrar.")
                                            }
                                    }
                                }
                            } else {
                                val msg = when (task.exception) {
                                    is com.google.firebase.auth.FirebaseAuthUserCollisionException -> "Este email ya está registrado."
                                    else -> "Error al registrar cuenta."
                                }
                                _authState.value = AuthState.Error(msg)
                            }
                        }
                }
            }
    }

    fun login(email: String, pass: String) {
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.reload()?.addOnCompleteListener {
                        if (user?.isEmailVerified == true) {
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

    fun loginConGoogle(idToken: String) {
        _authState.value = AuthState.Loading
        val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    val uid = firebaseUser?.uid ?: ""
                    db.collection("users").document(uid).get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                fetchUserData(uid)
                            } else {
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
                    _authState.value = AuthState.Success
                } else {
                    _authState.value = AuthState.Error("No pudimos enviar el correo de recuperación.")
                }
            }
    }

    fun fetchUserData(uid: String) {
        _authState.value = AuthState.Loading
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                if (user != null) {
                    _userData.value = user
                    _authState.value = AuthState.Success
                }
            }
            .addOnFailureListener {
                _authState.value = AuthState.Error("Error al cargar datos del perfil.")
            }
    }

    fun logout() {
        auth.signOut()
        _userData.value = null
        _authState.value = AuthState.Idle
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    // --- MOTOR DE PROGRESIÓN (XP Y NIVELES) ---

    fun addXP(gainedXP: Int) {
        val currentUser = _userData.value ?: return
        val newXP = currentUser.xp + gainedXP
        var newLevel = sqrt(newXP / 100.0).toInt() + 1
        if (newLevel > 100) newLevel = 100

        val newRank = when (newLevel) {
            in 1..10 -> "Recluta de Pangeo"
            in 11..25 -> "Rastreador de Caminos"
            in 26..45 -> "Cartógrafo Real"
            in 46..65 -> "Navegante de Horizontes"
            in 66..80 -> "Guardián de Fronteras"
            in 81..95 -> "Maestro de la Tierra"
            else -> "Leyenda Universal"
        }

        val uid = auth.currentUser?.uid ?: return
        val updates = mapOf("xp" to newXP, "level" to newLevel, "rank" to newRank)

        db.collection("users").document(uid).update(updates)
            .addOnSuccessListener {
                _userData.value = currentUser.copy(xp = newXP, level = newLevel, rank = newRank)
            }
    }

    // --- GESTIÓN DE RÉCORDS Y ESTADÍSTICAS ---

    fun saveGameRecord(gameId: String, score: Int, time: Int, onNewTimeRecord: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        val docRef = db.collection("records").document(uid)

        docRef.get().addOnSuccessListener { document ->
            var isNewRecord = false
            val currentBestScore = document.getLong("${gameId}_score")?.toInt() ?: -1
            val currentBestTime = document.getLong("${gameId}_time")?.toInt() ?: Int.MAX_VALUE

            val data = mutableMapOf<String, Any>()

            if (score > currentBestScore) {
                data["${gameId}_score"] = score
                data["${gameId}_time"] = time
                isNewRecord = true
            } else if (score == currentBestScore && time < currentBestTime) {
                data["${gameId}_time"] = time
                isNewRecord = true
            }

            if (data.isNotEmpty()) {
                docRef.set(data, SetOptions.merge())
            }
            onNewTimeRecord(isNewRecord)
        }
    }

    fun saveCultureStreakRecord(newStreak: Int) {
        val uid = auth.currentUser?.uid ?: return
        val docRef = db.collection("records").document(uid)

        docRef.get().addOnSuccessListener { document ->
            val currentBestStreak = document.getLong("cultura_supervivencia_streak")?.toInt() ?: -1
            if (newStreak > currentBestStreak) {
                val data = mapOf("cultura_supervivencia_streak" to newStreak)
                docRef.set(data, SetOptions.merge())
            }
        }
    }

    fun updateNickname(newNickname: String, onResult: (Boolean, String) -> Unit) {
        if (newNickname.isBlank()) {
            onResult(false, "El nombre no puede estar vacío")
            return
        }

        val uid = auth.currentUser?.uid ?: return
        _authState.value = AuthState.Loading

        db.collection("users")
            .whereEqualTo("nickname", newNickname)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    _authState.value = AuthState.Idle
                    onResult(false, "Este nickname ya está en uso")
                } else {
                    db.collection("users").document(uid).update("nickname", newNickname)
                        .addOnSuccessListener {
                            _userData.value = _userData.value?.copy(nickname = newNickname)
                            _authState.value = AuthState.Success
                            onResult(true, "Nickname actualizado")
                        }
                        .addOnFailureListener {
                            _authState.value = AuthState.Error("Error al actualizar")
                            onResult(false, "Error al actualizar")
                        }
                }
            }
    }
}