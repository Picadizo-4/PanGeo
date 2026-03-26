package com.example.pangeo.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()

    // Función 1: Crear un usuario nuevo
    suspend fun registrarUsuario(email: String, contrasena: String): Result<Unit> {
        return try {
            auth.createUserWithEmailAndPassword(email, contrasena).await()
            auth.currentUser?.sendEmailVerification()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Función 2: Iniciar sesión con Email y Contraseña
    suspend fun iniciarSesion(email: String, contrasena: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, contrasena).await()
            val usuario = auth.currentUser
            if (usuario != null && usuario.isEmailVerified) {
                Result.success(Unit)
            } else {
                auth.signOut()
                Result.failure(Exception("Por favor, verifica tu correo electrónico antes de entrar."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- NUEVO: Función 3: Iniciar sesión con Google ---
    suspend fun loginConGoogle(idToken: String): Result<Unit> {
        return try {
            // Transformamos el ticket de Google en una credencial de Firebase
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}