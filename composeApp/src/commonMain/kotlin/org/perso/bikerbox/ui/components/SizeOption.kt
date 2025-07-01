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
import bikerbox.composeapp.generated.resources.Available
import bikerbox.composeapp.generated.resources.Double_locker
import bikerbox.composeapp.generated.resources.Individual_locker
import bikerbox.composeapp.generated.resources.None_available
import bikerbox.composeapp.generated.resources.Res
import bikerbox.composeapp.generated.resources.double_helmet_and_double_coat
import bikerbox.composeapp.generated.resources.one_helmet_and_one_coat
import org.jetbrains.compose.resources.stringResource
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
                    LockerSize.SINGLE -> stringResource(Res.string.Individual_locker)
                    LockerSize.DOUBLE -> stringResource(Res.string.Double_locker)
                },
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when(size) {
                    LockerSize.SINGLE -> stringResource(Res.string.one_helmet_and_one_coat)
                    LockerSize.DOUBLE -> stringResource(Res.string.double_helmet_and_double_coat)
                },
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isEnabled)
                    "${stringResource(Res.string.Available)}: $availableCount"
                else
                    stringResource(Res.string.None_available),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isEnabled)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error
            )
        }
    }
}