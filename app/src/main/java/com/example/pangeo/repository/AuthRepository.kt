package com.example.pangeo.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

/**
 * Repositorio encargado de la gestión de autenticación de usuarios.
 * * Actúa como abstracción sobre Firebase Auth, centralizando las operaciones de
 * registro, inicio de sesión tradicional y autenticación federada con Google.
 */
class AuthRepository {

    /** Instancia única de Firebase Auth para la gestión de sesiones. */
    private val auth = FirebaseAuth.getInstance()

    /**
     * Registra una nueva cuenta de usuario en Firebase y dispara un correo de verificación.
     *
     * @param email Dirección de correo electrónico del aspirante.
     * @param contrasena Clave de acceso (debe cumplir los requisitos de seguridad de Firebase).
     * @return [Result] con [Unit] en caso de éxito o la excepción correspondiente en fallo.
     */
    suspend fun registrarUsuario(email: String, contrasena: String): Result<Unit> {
        return try {
            // Se utiliza .await() para convertir el Task de Firebase en una operación suspendida no bloqueante
            auth.createUserWithEmailAndPassword(email, contrasena).await()
            auth.currentUser?.sendEmailVerification()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Valida las credenciales del usuario y verifica el estado de su correo electrónico.
     * * Nota de seguridad: Si el correo no está verificado, se cierra la sesión
     * automáticamente para cumplir con las políticas de acceso de la aplicación.
     *
     * @param email Correo del usuario.
     * @param contrasena Contraseña asociada.
     * @return [Result.success] si el usuario es válido y está verificado.
     */
    suspend fun iniciarSesion(email: String, contrasena: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, contrasena).await()
            val usuario = auth.currentUser

            if (usuario != null && usuario.isEmailVerified) {
                Result.success(Unit)
            } else {
                auth.signOut() // Prevención de acceso no autorizado
                Result.failure(Exception("Por favor, verifica tu correo electrónico antes de entrar."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Realiza la autenticación mediante el ecosistema de Google.
     *
     * @param idToken Token de identidad obtenido tras el flujo exitoso de Google Sign-In en la UI.
     * @return [Result] indicando el éxito de la vinculación con Firebase.
     */
    suspend fun loginConGoogle(idToken: String): Result<Unit> {
        return try {
            // Conversión del ID Token de Google en credenciales nativas de Firebase
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}