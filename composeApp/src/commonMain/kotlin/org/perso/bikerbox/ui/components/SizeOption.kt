package org.perso.bikerbox.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.perso.bikerbox.data.models.LockerSize

@Composable
fun SizeOption(
    size: LockerSize,
    availableCount: Int,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEnabled = availableCount > 0

    Card(
        modifier = modifier.clickable(
            enabled = isEnabled,
            onClick = onSelected
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = when(size) {
                    LockerSize.SINGLE -> "Casier individuel (1 personne)"
                    LockerSize.DOUBLE -> "Casier double (2 personnes)"
                },
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when(size) {
                    LockerSize.SINGLE -> "1 casque + 1 manteau"
                    LockerSize.DOUBLE -> "2 casques + 2 manteaux"
                },
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isEnabled)
                    "Disponible: $availableCount"
                else
                    "Aucun disponible",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isEnabled)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error
            )
        }
    }
}