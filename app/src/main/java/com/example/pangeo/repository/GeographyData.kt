package com.example.pangeo.utils

/**
 * Fuente de verdad única para la información geográfica de la aplicación.
 * Centraliza el mapeo de países y capitales para evitar la duplicidad de datos,
 * asegurando la consistencia en las diferentes pantallas de estudio y juego.
 */
object GeographyData {

    /**
     * Diccionario inmutable que vincula identificadores de países con sus capitales.
     * Se utiliza una estructura de [Map] para optimizar la recuperación de datos
     * con una complejidad temporal de O(1).
     */
    private val capitalMap = mapOf(
        "España" to "Madrid",
        "Francia" to "París",
        "Italia" to "Roma",
        "Alemania" to "Berlín",
        "Reino Unido" to "Londres",
        "Portugal" to "Lisboa",
        "Países Bajos" to "Ámsterdam",
        "Bélgica" to "Bruselas",
        "Suiza" to "Berna",
        "Austria" to "Viena",
        "República Checa" to "Praga",
        "Polonia" to "Varsovia",
        "Dinamarca" to "Copenhague",
        "Noruega" to "Oslo",
        "Suecia" to "Estocolmo",
        "Finlandia" to "Helsinki",
        "Estonia" to "Tallin",
        "Letonia" to "Riga",
        "Lituania" to "Vilna",
        "Bielorrusia" to "Minsk",
        "Ucrania" to "Kiev",
        "Rumanía" to "Bucarest",
        "Bulgaria" to "Sofía",
        "Grecia" to "Atenas",
        "Albania" to "Tirana",
        "Hungría" to "Budapest",
        "Eslovaquia" to "Bratislava",
        "Eslovenia" to "Liubliana",
        "Croacia" to "Zagreb",
        "Bosnia y Herzegovina" to "Sarajevo",
        "Serbia" to "Belgrado",
        "Montenegro" to "Podgorica",
        "Macedonia del Norte" to "Skopie",
        "Moldavia" to "Chisináu",
        "Islandia" to "Reikiavik",
        "Irlanda" to "Dublín",
        "Vaticano" to "Ciudad del Vaticano",
        "San Marino" to "San Marino",
        "Mónaco" to "Mónaco",
        "Andorra" to "Andorra la Vella",
        "Liechtenstein" to "Vaduz",
        "Malta" to "La Valeta",
        "Luxemburgo" to "Luxemburgo",
        "Chipre" to "Nicosia",
        "Kosovo" to "Pristina",
        "Georgia" to "Tiflis",
        "Armenia" to "Ereván",
        "Turquía" to "Ankara"
    )

    /**
     * Resuelve la capital correspondiente a un país dado.
     * * @param countryName Nombre exacto del país a consultar.
     * @return El nombre de la capital o "Desconocida" si no existe coincidencia.
     */
    fun getCapital(countryName: String): String {
        return capitalMap[countryName] ?: "Desconocida"
    }
}