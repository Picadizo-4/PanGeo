package com.example.pangeo.ui.study

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pangeo.R
import com.example.pangeo.utils.GeographyData
import com.example.pangeo.viewmodel.StudyCapitalsViewModel

/**
 * Pantalla de estudio de capitales europeas.
 * * Implementa una interfaz de búsqueda reactiva y una lista de tarjetas interactivas.
 * Esta pantalla se enfoca en el "active recall", permitiendo al usuario ocultar y
 * mostrar nombres de capitales para facilitar la memorización.
 *
 * @param onNavigateBack Callback para gestionar la navegación de retorno.
 * @param viewModel Instancia de [StudyCapitalsViewModel] para la lógica de negocio y filtrado.
 */
@Composable
fun StudyCapitalsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StudyCapitalsViewModel = viewModel()
) {
    // Definición de tipografía manuscrita con fallback seguro a Serif
    val caveatFamily = remember {
        try { FontFamily(Font(R.font.caveat)) } catch (e: Exception) { FontFamily.Serif }
    }

    // Observación de estados del ViewModel mediante delegación 'by'
    val countries by viewModel.filteredCountries
    val searchText by viewModel.searchQuery

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F7FB))
            .padding(horizontal = 16.dp)
    ) {
        // --- CABECERA ---
        Row(
            modifier = Modifier.padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .shadow(4.dp, CircleShape)
                    .background(Color.White, CircleShape)
            ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Guía de Capitales",
                fontSize = 32.sp,
                fontFamily = caveatFamily,
                fontWeight = FontWeight.Bold
            )
        }

        /**
         * Buscador optimizado:
         * Implementa KeyboardOptions para evitar autocorrecciones que puedan
         * entorpecer la búsqueda de nombres propios geográficos específicos.
         */
        OutlinedTextField(
            value = searchText,
            onValueChange = { viewModel.onSearchChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            placeholder = { Text("Busca un país... (ej: España)") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                autoCorrect = false
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color(0xFFE1B44B)
            )
        )

        /**
         * Lista eficiente de tarjetas.
         * Se utiliza [LazyColumn] con [spacedBy] para gestionar el reciclaje de vistas
         * y el espaciado vertical de forma declarativa.
         */
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(countries) { country ->
                CapitalStudyCard(country.name)
            }
        }
    }
}

/**
 * Componente de tarjeta de estudio individual.
 * * Gestiona localmente su propio estado de visibilidad y recupera la información
 * desde la fuente de verdad centralizada [GeographyData].
 *
 * @param countryName Identificador del país para realizar el mapeo de la capital.
 */
@Composable
fun CapitalStudyCard(countryName: String) {
    // Estado local para controlar la revelación de la respuesta (capital)
    var isVisible by remember { mutableStateOf(false) }

    /**
     * Recuperación de datos desde el repositorio.
     * Se encapsula en [remember] vinculándolo a [countryName] para optimizar
     * las recomposiciones y no acceder al diccionario innecesariamente.
     */
    val capital = remember(countryName) {
        GeographyData.getCapital(countryName)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isVisible = !isVisible } // Alternar visibilidad al tocar la tarjeta
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = countryName,
                    fontSize = 20.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )

                /**
                 * Contenedor de la Capital:
                 * Se utiliza una [Column] para proveer el Scope necesario a [AnimatedVisibility]
                 * y asegurar que las animaciones de expansión vertical funcionen correctamente.
                 */
                Column(
                    modifier = Modifier.heightIn(min = 35.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    // Placeholder mientras la respuesta está oculta
                    if (!isVisible) {
                        Text(
                            text = "••••••••",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFE1B44B)
                        )
                    }

                    // Transición animada para revelar la capital
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Text(
                            text = capital,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF2D3142)
                        )
                    }
                }
            }

            // Indicador visual del estado de visibilidad
            Icon(
                imageVector = if (isVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = if (isVisible) "Ocultar capital" else "Mostrar capital",
                tint = if (isVisible) Color.Gray else Color(0xFFE1B44B)
            )
        }
    }
}