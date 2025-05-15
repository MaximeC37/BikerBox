package org.perso.bikerbox.ui.screens.LockerSelection

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.perso.bikerbox.data.models.Locker
import org.perso.bikerbox.ui.components.LockerCard

@Composable
fun LockerSelectionScreen(
    lockers: List<Locker>,
    onLockerSelected: (Locker) -> Unit
) {
    Log.d("LockerSelectionScreen", "Entrée dans LockerSelectionScreen avec ${lockers.size} casiers")
    lockers.forEachIndexed { index, locker ->
        Log.d("LockerSelectionScreen", "Casier $index: ${locker.name}, ${locker.location}, tailles: ${locker.availableSizes}")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
        text = "Choisissez un casier",
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(bottom = 16.dp)
        )

        Log.d("LockerSelectionScreen", "Préparation de l'affichage de ${lockers.size} casiers dans LazyColumn")

        LazyColumn {
            items(lockers) { locker ->

                Log.d("LockerSelectionScreen", "Création de LockerCard pour le casier: ${locker.name}")

                LockerCard(
                    locker = locker,
                    onClick = { onLockerSelected(locker) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
        }
    }
}