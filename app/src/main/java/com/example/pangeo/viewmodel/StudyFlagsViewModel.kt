package com.example.pangeo.viewmodel

import androidx.lifecycle.ViewModel
import com.example.pangeo.ui.game.flags.FlagQuestion
import com.example.pangeo.ui.game.flags.FlagRepository

/**
 * ViewModel minimalista para el Diccionario de Banderas.
 * * Responsabilidades:
 * 1. Proveer acceso directo a los activos visuales (Banderas) del repositorio.
 * 2. Mantener la inmutabilidad de la lista de estudio para garantizar
 * consistencia en la rejilla de tarjetas (Grid).
 * 3. Facilitar el desacoplamiento entre la capa de UI y el [FlagRepository].
 */
class StudyFlagsViewModel : ViewModel() {

    /**
     * Lista inmutable de banderas europeas.
     * * Nota de Arquitectura:
     * Al ser un modo de estudio (Diccionario), se expone la lista tal cual
     * viene del repositorio. No aplicamos barajado (.shuffled()) para
     * mantener una navegación predecible y organizada para el usuario.
     */
    val flagsList: List<FlagQuestion> = FlagRepository.getEuropeQuestions()

}