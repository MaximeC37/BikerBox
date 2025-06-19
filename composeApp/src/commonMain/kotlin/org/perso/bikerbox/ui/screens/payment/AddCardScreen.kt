package org.perso.bikerbox.ui.screens.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.perso.bikerbox.data.models.CardType
import org.perso.bikerbox.data.models.PaymentCard
import org.perso.bikerbox.ui.viewmodel.PaymentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardScreen(
    viewModel: PaymentViewModel,
    onNavigateBack: () -> Unit
) {
    var cardNumber by remember { mutableStateOf("") }
    var cardHolderName by remember { mutableStateOf("") }
    var expiryMonth by remember { mutableStateOf("") }
    var expiryYear by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var isDefault by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Card") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = cardNumber,
                onValueChange = { value ->
                    if (value.length <= 19) { // 16 chiffres + 3 tirets
                        cardNumber = formatCardNumber(value)
                    }
                },
                label = { Text("Card Number") },
                placeholder = { Text("1234 5678 9012 3456") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = cardHolderName,
                onValueChange = { cardHolderName = it },
                label = { Text("Cardholder Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = expiryMonth,
                    onValueChange = { value ->
                        if (value.length <= 2 && value.all { it.isDigit() }) {
                            expiryMonth = value
                        }
                    },
                    label = { Text("Month") },
                    placeholder = { Text("MM") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = expiryYear,
                    onValueChange = { value ->
                        if (value.length <= 4 && value.all { it.isDigit() }) {
                            expiryYear = value
                        }
                    },
                    label = { Text("Year") },
                    placeholder = { Text("YYYY") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = cvv,
                    onValueChange = { value ->
                        if (value.length <= 4 && value.all { it.isDigit() }) {
                            cvv = value
                        }
                    },
                    label = { Text("CVV") },
                    placeholder = { Text("123") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isDefault,
                    onCheckedChange = { isDefault = it }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Set as default card")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val card = PaymentCard(
                        id = "",
                        cardNumber = "****-****-****-${cardNumber.takeLast(4)}",
                        cardHolderName = cardHolderName,
                        expiryMonth = expiryMonth.toIntOrNull() ?: 0,
                        expiryYear = expiryYear.toIntOrNull() ?: 0,
                        cardType = detectCardType(cardNumber),
                        isDefault = isDefault
                    )
                    viewModel.addNewCard(card)
                },
                enabled = cardNumber.length >= 16 && cardHolderName.isNotBlank() &&
                        expiryMonth.isNotBlank() && expiryYear.isNotBlank() && cvv.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Card")
            }
        }
    }
}

private fun formatCardNumber(input: String): String {
    val digitsOnly = input.filter { it.isDigit() }
    return digitsOnly.chunked(4).joinToString(" ")
}

private fun detectCardType(cardNumber: String): CardType {
    val digits = cardNumber.filter { it.isDigit() }
    return when {
        digits.startsWith("4") -> CardType.VISA
        digits.startsWith("5") -> CardType.MASTERCARD
        digits.startsWith("34") || digits.startsWith("37") -> CardType.AMERICAN_EXPRESS
        digits.startsWith("6") -> CardType.DISCOVER
        else -> CardType.VISA
    }
}
