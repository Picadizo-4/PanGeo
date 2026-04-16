package com.example.pangeo.ui.game.culture

import android.content.Context
import androidx.compose.ui.graphics.Color
import org.json.JSONArray

/**
 * Representa una unidad de conocimiento en el modo Supervivencia.
 * * @property questionText El enunciado de la pregunta.
 * @property correctAnswer La respuesta válida.
 * @property options Lista de todas las opciones posibles (incluyendo la correcta).
 * @property difficulty Nivel de complejidad (útil para futuros algoritmos de selección).
 */
data class CultureQuestion(
    val questionText: String,
    val correctAnswer: String,
    val options: List<String>,
    val difficulty: Int
)

/**
 * Repositorio encargado de la gestión de contenidos y metadatos del modo Cultura.
 * * Este objeto actúa como un puente entre los recursos estáticos (JSON) y la lógica
 * de juego, además de definir las reglas de progresión estética de la aplicación.
 */
object CultureRepository {

    /**
     * Define la progresión cromática de la interfaz según la racha del usuario.
     * * Implementa un algoritmo de interpolación por niveles donde cada 2 aciertos
     * el fondo evoluciona de tonos pasteles a colores más intensos y oscuros,
     * reflejando visualmente el aumento de la tensión.
     * * @param streak Racha actual de aciertos sin fallos.
     * @return [Color] representativo del nivel de supervivencia alcanzado.
     */
    fun getBackgroundColorForStreak(streak: Int): Color {
        val colors = listOf(
            Color(0xFFE3F2FD), Color(0xFFBBDEFB), Color(0xFF90CAF9), Color(0xFFB2DFDB),
            Color(0xFF80CBC4), Color(0xFFC8E6C9), Color(0xFFA5D6A7), Color(0xFFFFF9C4),
            Color(0xFFFFF59D), Color(0xFFFFE082), Color(0xFFFFCC80), Color(0xFFFFB74D),
            Color(0xFFFF8A65), Color(0xFFE57373), Color(0xFFEF5350), Color(0xFFD32F2F),
            Color(0xFFCE93D8), Color(0xFFAB47BC), Color(0xFF4527A0), Color(0xFF212121), Color(0xFF000000)
        )
        // Cálculo del índice basado en la racha, limitado por el tamaño de la lista
        val level = (streak / 2).coerceAtMost(colors.size - 1)
        return colors[level]
    }

    /**
     * Mapea la racha de aciertos a un rango o título honorífico.
     * * @param streak Racha actual.
     * @return [String] con el título y emoji correspondiente al rango.
     */
    fun getTitleForStreak(streak: Int): String {
        val titles = listOf(
            "Turista Despistado 📸", "Paseante Curioso 👣", "Lector de Folletos 📖",
            "Mochilero Aficionado 🎒", "Viajero Frecuente ✈️", "Explorador Novato 🧭",
            "Buscador de Rutas 🗺️", "Aventurero Intrépido ⛺", "Trotamundos 🌍",
            "Navegante Experto 🚢", "Cartógrafo Ilustre 📜", "Erudito Geográfico 🧠",
            "Cazador de Reliquias 🏺", "Indiana Jones 🤠", "Maestro Cartógrafo 🚧",
            "Leyenda Viva 🏆", "Oráculo del Mundo 🔮", "Conquistador Geográfico ⚔️",
            "Emperador del Conocimiento 👑", "Guardián de PanGeo 🌎", "Deidad Geográfica ⚡"
        )
        val level = (streak / 2).coerceAtMost(titles.size - 1)
        return titles[level]
    }

    /**
     * Carga el dataset de preguntas desde un archivo local en la carpeta 'assets'.
     * * El proceso incluye:
     * 1. Lectura del flujo de entrada (Input Stream).
     * 2. Conversión de buffer de bytes a String UTF-8.
     * 3. Parsing del formato [JSONArray] a una lista de objetos [CultureQuestion].
     * * @param context Contexto de la aplicación necesario para acceder a los Assets.
     * @return [List] de preguntas cargadas. Devuelve una lista vacía en caso de error de lectura.
     */
    fun loadQuestionsFromJson(context: Context): List<CultureQuestion> {
        val questionsList = mutableListOf<CultureQuestion>()
        try {
            // Acceso al sistema de archivos de solo lectura de la aplicación
            val inputStream = context.assets.open("culture_questions.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()

            val jsonStr = String(buffer, Charsets.UTF_8)
            val jsonArray = JSONArray(jsonStr)

            for (i in 0 until jsonArray.length()) {
                val jsonObj = jsonArray.getJSONObject(i)
                val optionsArray = jsonObj.getJSONArray("options")
                val options = mutableListOf<String>()
                for (j in 0 until optionsArray.length()) {
                    options.add(optionsArray.getString(j))
                }

                questionsList.add(CultureQuestion(
                    questionText = jsonObj.getString("questionText"),
                    correctAnswer = jsonObj.getString("correctAnswer"),
                    options = options,
                    difficulty = jsonObj.getInt("difficulty")
                ))
            }
        } catch (e: Exception) {
            // Nota de mantenimiento: En un entorno real, se debería usar un sistema de Logs (Timber/Log)
            e.printStackTrace()
        }
        return questionsList
    }
}