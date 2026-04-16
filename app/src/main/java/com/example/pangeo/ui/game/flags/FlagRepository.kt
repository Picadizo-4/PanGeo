package com.example.pangeo.ui.game.flags

import com.example.pangeo.R

/**
 * Modelo de datos para las preguntas del juego de reconocimiento de banderas.
 * * @property id Identificador único de la pregunta.
 * @property flagImageRes Referencia al recurso gráfico de la bandera en la carpeta drawable.
 * @property options Lista de nombres de países propuestos como opciones.
 * @property correctAnswer El nombre del país que corresponde exactamente a la bandera mostrada.
 */
data class FlagQuestion(
    val id: Int,
    val flagImageRes: Int,
    val options: List<String>,
    val correctAnswer: String
)

/**
 * Repositorio de activos visuales para el módulo de banderas.
 * * Este objeto centraliza el dataset de Europa. El diseño de las preguntas sigue
 * un criterio de dificultad pedagógica, incluyendo como distractores a países
 * con banderas visualmente similares (ej. banderas tricolores o con cruces nórdicas).
 */
object FlagRepository {

    /**
     * Genera la lista de desafíos para el continente europeo.
     * * Nota técnica: Se recomienda que el ViewModel consuma esta lista y aplique
     * una función de barajado (.shuffled()) tanto a la lista de preguntas como
     * a las opciones internas para maximizar la rejugabilidad.
     * * @return [List] de [FlagQuestion] con los 44 países de Europa y sus recursos asociados.
     */
    fun getEuropeQuestions(): List<FlagQuestion> {
        return listOf(
            FlagQuestion(1, R.drawable.eu_albania, listOf("Albania", "Montenegro", "Macedonia del Norte", "Serbia"), "Albania"),
            FlagQuestion(2, R.drawable.eu_andorra, listOf("Andorra", "Rumanía", "Moldavia", "Bélgica"), "Andorra"),
            FlagQuestion(3, R.drawable.eu_austria, listOf("Austria", "Letonia", "Polonia", "Mónaco"), "Austria"),
            FlagQuestion(4, R.drawable.eu_belarus, listOf("Bielorrusia", "Ucrania", "Lituania", "Rusia"), "Bielorrusia"),
            FlagQuestion(5, R.drawable.eu_belgium, listOf("Bélgica", "Alemania", "Andorra", "Francia"), "Bélgica"),
            FlagQuestion(6, R.drawable.eu_bosnia, listOf("Bosnia y Herzegovina", "Kosovo", "Macedonia del Norte", "Eslovenia"), "Bosnia y Herzegovina"),
            FlagQuestion(7, R.drawable.eu_bulgaria, listOf("Bulgaria", "Hungría", "Italia", "Lituania"), "Bulgaria"),
            FlagQuestion(8, R.drawable.eu_croatia, listOf("Croacia", "Serbia", "Eslovenia", "Eslovaquia"), "Croacia"),
            FlagQuestion(9, R.drawable.eu_czechrepublic, listOf("República Checa", "Eslovaquia", "Polonia", "Eslovenia"), "República Checa"),
            FlagQuestion(10, R.drawable.eu_denmark, listOf("Dinamarca", "Noruega", "Suiza", "Finlandia"), "Dinamarca"),
            FlagQuestion(11, R.drawable.eu_england, listOf("Inglaterra", "Escocia", "Georgia", "Irlanda del Norte"), "Inglaterra"),
            FlagQuestion(12, R.drawable.eu_estonia, listOf("Estonia", "Finlandia", "Letonia", "Rusia"), "Estonia"),
            FlagQuestion(13, R.drawable.eu_finland, listOf("Finlandia", "Suecia", "Noruega", "Islandia"), "Finlandia"),
            FlagQuestion(14, R.drawable.eu_greatbritain, listOf("Reino Unido", "Islandia", "Noruega", "Irlanda"), "Reino Unido"),
            FlagQuestion(15, R.drawable.eu_greece, listOf("Grecia", "Chipre", "Uruguay", "Finlandia"), "Grecia"),
            FlagQuestion(16, R.drawable.eu_hungary, listOf("Hungría", "Bulgaria", "Italia", "Rumanía"), "Hungría"),
            FlagQuestion(17, R.drawable.eu_iceland, listOf("Islandia", "Noruega", "Reino Unido", "Islas Feroe"), "Islandia"),
            FlagQuestion(18, R.drawable.eu_ireland, listOf("Irlanda", "Italia", "Costa de Marfil", "Francia"), "Irlanda"),
            FlagQuestion(19, R.drawable.eu_kosovo, listOf("Kosovo", "Albania", "Serbia", "Bosnia y Herzegovina"), "Kosovo"),
            FlagQuestion(20, R.drawable.eu_latvia, listOf("Letonia", "Austria", "Estonia", "Lituania"), "Letonia"),
            FlagQuestion(21, R.drawable.eu_liechtenstein, listOf("Liechtenstein", "Luxemburgo", "San Marino", "Suiza"), "Liechtenstein"),
            FlagQuestion(22, R.drawable.eu_lithuania, listOf("Lituania", "Letonia", "Estonia", "Bulgaria"), "Lituania"),
            FlagQuestion(23, R.drawable.eu_luxembourg, listOf("Luxemburgo", "Países Bajos", "Francia", "Rusia"), "Luxemburgo"),
            FlagQuestion(24, R.drawable.eu_macedonia, listOf("Macedonia del Norte", "Albania", "Montenegro", "Grecia"), "Macedonia del Norte"),
            FlagQuestion(25, R.drawable.eu_malta, listOf("Malta", "Polonia", "Mónaco", "Suiza"), "Malta"),
            FlagQuestion(26, R.drawable.eu_moldova, listOf("Moldavia", "Rumanía", "Andorra", "Ucrania"), "Moldavia"),
            FlagQuestion(27, R.drawable.eu_monaco, listOf("Mónaco", "Polonia", "Indonesia", "Singapur"), "Mónaco"),
            FlagQuestion(28, R.drawable.eu_montenegro, listOf("Montenegro", "Albania", "Serbia", "Macedonia del Norte"), "Montenegro"),
            FlagQuestion(29, R.drawable.eu_netherlands, listOf("Países Bajos", "Luxemburgo", "Francia", "Rusia"), "Países Bajos"),
            FlagQuestion(30, R.drawable.eu_norway, listOf("Noruega", "Islandia", "Dinamarca", "Suecia"), "Noruega"),
            FlagQuestion(31, R.drawable.eu_poland, listOf("Polonia", "Mónaco", "Indonesia", "Vaticano"), "Polonia"),
            FlagQuestion(32, R.drawable.eu_portugal, listOf("Portugal", "España", "Italia", "Francia"), "Portugal"),
            FlagQuestion(33, R.drawable.eu_romania, listOf("Rumanía", "Moldavia", "Andorra", "Bélgica"), "Rumanía"),
            FlagQuestion(34, R.drawable.eu_russia, listOf("Rusia", "Eslovaquia", "Eslovenia", "Serbia"), "Rusia"),
            FlagQuestion(35, R.drawable.eu_sanmarino, listOf("San Marino", "Vaticano", "Liechtenstein", "Malta"), "San Marino"),
            FlagQuestion(36, R.drawable.eu_scotland, listOf("Escocia", "Inglaterra", "Georgia", "Finlandia"), "Escocia"),
            FlagQuestion(37, R.drawable.eu_serbia, listOf("Serbia", "Eslovaquia", "Eslovenia", "Rusia"), "Serbia"),
            FlagQuestion(38, R.drawable.eu_slovakia, listOf("Eslovaquia", "Eslovenia", "Rusia", "República Checa"), "Eslovaquia"),
            FlagQuestion(39, R.drawable.eu_slovenia, listOf("Eslovenia", "Eslovaquia", "Rusia", "Croacia"), "Eslovenia"),
            FlagQuestion(40, R.drawable.eu_spain, listOf("España", "Portugal", "Andorra", "Italia"), "España"),
            FlagQuestion(41, R.drawable.eu_sweden, listOf("Suecia", "Finlandia", "Dinamarca", "Ucrania"), "Suecia"),
            FlagQuestion(42, R.drawable.eu_switzerland, listOf("Suiza", "Dinamarca", "Austria", "Liechtenstein"), "Suiza"),
            FlagQuestion(43, R.drawable.eu_ukraine, listOf("Ucrania", "Suecia", "Polonia", "Rumanía"), "Ucrania"),
            FlagQuestion(44, R.drawable.eu_vatikan, listOf("Vaticano", "San Marino", "Malta", "Mónaco"), "Vaticano")
        )
    }
}