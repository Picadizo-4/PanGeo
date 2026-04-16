package com.example.pangeo.ui.game.capitals

import com.example.pangeo.R

/**
 * Modelo de datos que representa una unidad de desafío en el juego de capitales.
 * * @property countryName Nombre del país que se presenta como pregunta.
 * @property flagRes Identificador del recurso gráfico (Drawable) de la bandera nacional.
 * @property correctAnswer El nombre de la capital correcta (usado para validación).
 * @property options Lista de alternativas (distractores) para el modo de selección múltiple.
 */
data class CapitalQuestion(
    val countryName: String,
    val flagRes: Int,
    val correctAnswer: String,
    val options: List<String>
)

/**
 * Repositorio de contenidos para el módulo de juegos de capitales.
 * * Provee los datasets necesarios para las diferentes regiones geográficas.
 * Actualmente centrado en el catálogo europeo.
 */
class CapitalsRepository {

    /**
     * Genera el conjunto de datos para la expedición europea.
     * * Contiene 45 ítems validados con sus respectivos recursos visuales.
     * * Nota de diseño: El orden de las [options] debe ser aleatorizado en el ViewModel
     * para evitar patrones de respuesta predecibles.
     * * @return [List] de [CapitalQuestion] con la base de datos de Europa.
     */
    fun getEuropeCapitals(): List<CapitalQuestion> {
        return listOf(
            CapitalQuestion("Albania", R.drawable.eu_albania, "Tirana", listOf("Sofía", "Skopie", "Pristina", "Tirana")),
            CapitalQuestion("Alemania", R.drawable.eu_germany, "Berlín", listOf("Múnich", "Berlín", "Fráncfort", "Viena")),
            CapitalQuestion("Andorra", R.drawable.eu_andorra, "Andorra la Vieja", listOf("Mónaco", "San Marino", "Andorra la Vieja", "Vaduz")),
            CapitalQuestion("Austria", R.drawable.eu_austria, "Viena", listOf("Zúrich", "Viena", "Salzburgo", "Bratislava")),
            CapitalQuestion("Bélgica", R.drawable.eu_belgium, "Bruselas", listOf("Ámsterdam", "Brujas", "Bruselas", "Luxemburgo")),
            CapitalQuestion("Bielorrusia", R.drawable.eu_belarus, "Minsk", listOf("Kiev", "Minsk", "Riga", "Vilna")),
            CapitalQuestion("Bosnia y Herzegovina", R.drawable.eu_bosnia, "Sarajevo", listOf("Belgrado", "Zagreb", "Sarajevo", "Podgorica")),
            CapitalQuestion("Bulgaria", R.drawable.eu_bulgaria, "Sofía", listOf("Bucarest", "Sofía", "Atenas", "Tirana")),
            CapitalQuestion("Chipre", R.drawable.eu_cyprus, "Nicosia", listOf("Atenas", "Nicosia", "La Valeta", "Ankara")),
            CapitalQuestion("Croacia", R.drawable.eu_croatia, "Zagreb", listOf("Liubliana", "Sarajevo", "Zagreb", "Belgrado")),
            CapitalQuestion("Dinamarca", R.drawable.eu_denmark, "Copenhague", listOf("Oslo", "Estocolmo", "Copenhague", "Helsinki")),
            CapitalQuestion("Eslovaquia", R.drawable.eu_slovakia, "Bratislava", listOf("Praga", "Viena", "Bratislava", "Budapest")),
            CapitalQuestion("Eslovenia", R.drawable.eu_slovenia, "Liubliana", listOf("Zagreb", "Liubliana", "Bratislava", "Viena")),
            CapitalQuestion("España", R.drawable.eu_spain, "Madrid", listOf("Barcelona", "Lisboa", "Madrid", "Roma")),
            CapitalQuestion("Estonia", R.drawable.eu_estonia, "Tallin", listOf("Riga", "Tallin", "Helsinki", "Vilna")),
            CapitalQuestion("Finlandia", R.drawable.eu_finland, "Helsinki", listOf("Oslo", "Estocolmo", "Tallin", "Helsinki")),
            CapitalQuestion("Francia", R.drawable.eu_france, "París", listOf("Lyon", "Marsella", "Bruselas", "París")),
            CapitalQuestion("Grecia", R.drawable.eu_greece, "Atenas", listOf("Nicosia", "Atenas", "Sofía", "Roma")),
            CapitalQuestion("Hungría", R.drawable.eu_hungary, "Budapest", listOf("Bucarest", "Viena", "Budapest", "Bratislava")),
            CapitalQuestion("Irlanda", R.drawable.eu_ireland, "Dublín", listOf("Belfast", "Dublín", "Londres", "Edimburgo")),
            CapitalQuestion("Islandia", R.drawable.eu_iceland, "Reikiavik", listOf("Oslo", "Nuuk", "Helsinki", "Reikiavik")),
            CapitalQuestion("Italia", R.drawable.eu_italy, "Roma", listOf("Milán", "Nápoles", "Roma", "Venecia")),
            CapitalQuestion("Kosovo", R.drawable.eu_kosovo, "Pristina", listOf("Tirana", "Skopie", "Podgorica", "Pristina")),
            CapitalQuestion("Letonia", R.drawable.eu_latvia, "Riga", listOf("Vilna", "Riga", "Tallin", "Minsk")),
            CapitalQuestion("Liechtenstein", R.drawable.eu_liechtenstein, "Vaduz", listOf("Berna", "Viena", "Vaduz", "Mónaco")),
            CapitalQuestion("Lituania", R.drawable.eu_lithuania, "Vilna", listOf("Riga", "Tallin", "Varsovia", "Vilna")),
            CapitalQuestion("Luxemburgo", R.drawable.eu_luxembourg, "Luxemburgo", listOf("Bruselas", "Luxemburgo", "Estrasburgo", "Ámsterdam")),
            CapitalQuestion("Macedonia del Norte", R.drawable.eu_macedonia, "Skopie", listOf("Sofía", "Tirana", "Skopie", "Atenas")),
            CapitalQuestion("Malta", R.drawable.eu_malta, "La Valeta", listOf("Nicosia", "Roma", "La Valeta", "Atenas")),
            CapitalQuestion("Moldavia", R.drawable.eu_moldova, "Chisináu", listOf("Bucarest", "Kiev", "Minsk", "Chisináu")),
            CapitalQuestion("Montenegro", R.drawable.eu_montenegro, "Podgorica", listOf("Sarajevo", "Belgrado", "Pristina", "Podgorica")),
            CapitalQuestion("Noruega", R.drawable.eu_norway, "Oslo", listOf("Estocolmo", "Copenhague", "Oslo", "Helsinki")),
            CapitalQuestion("Países Bajos", R.drawable.eu_netherlands, "Ámsterdam", listOf("La Haya", "Bruselas", "Copenhague", "Ámsterdam")),
            CapitalQuestion("Polonia", R.drawable.eu_poland, "Varsovia", listOf("Cracovia", "Praga", "Varsovia", "Berlín")),
            CapitalQuestion("Portugal", R.drawable.eu_portugal, "Lisboa", listOf("Oporto", "Lisboa", "Madrid", "Sevilla")),
            CapitalQuestion("Reino Unido", R.drawable.eu_greatbritain, "Londres", listOf("Dublín", "Edimburgo", "Mánchester", "Londres")),
            CapitalQuestion("República Checa", R.drawable.eu_czechrepublic, "Praga", listOf("Bratislava", "Viena", "Varsovia", "Praga")),
            CapitalQuestion("Rumanía", R.drawable.eu_romania, "Bucarest", listOf("Budapest", "Sofía", "Chisináu", "Bucarest")),
            CapitalQuestion("Rusia", R.drawable.eu_russia, "Moscú", listOf("San Petersburgo", "Kiev", "Moscú", "Minsk")),
            CapitalQuestion("San Marino", R.drawable.eu_sanmarino, "San Marino", listOf("Vaticano", "Roma", "Mónaco", "San Marino")),
            CapitalQuestion("Serbia", R.drawable.eu_serbia, "Belgrado", listOf("Sarajevo", "Zagreb", "Podgorica", "Belgrado")),
            CapitalQuestion("Suecia", R.drawable.eu_sweden, "Estocolmo", listOf("Oslo", "Helsinki", "Estocolmo", "Copenhague")),
            CapitalQuestion("Suiza", R.drawable.eu_switzerland, "Berna", listOf("Zúrich", "Ginebra", "Berna", "Viena")),
            CapitalQuestion("Ucrania", R.drawable.eu_ukraine, "Kiev", listOf("Minsk", "Moscú", "Chisináu", "Kiev")),
            CapitalQuestion("Vaticano", R.drawable.eu_vatikan, "Ciudad del Vaticano", listOf("Roma", "San Marino", "Ciudad del Vaticano", "Milán"))
        )
    }
}