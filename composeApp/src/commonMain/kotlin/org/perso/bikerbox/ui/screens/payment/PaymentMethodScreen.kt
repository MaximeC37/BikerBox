package org.perso.bikerbox.ui.screens.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.perso.bikerbox.data.models.PaymentCard
import org.perso.bikerbox.data.models.PaymentMethod
import org.perso.bikerbox.ui.components.OfficialCardTypeIcon
import org.perso.bikerbox.ui.components.OfficialPaymentMethodIcon
import org.perso.bikerbox.ui.viewmodel.PaymentState
import org.perso.bikerbox.ui.viewmodel.PaymentViewModel
import org.perso.bikerbox.utils.formatDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodScreen(
    viewModel: PaymentViewModel,
    state: PaymentState.PaymentMethodSelection,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment Method") },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Amount summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Amount to pay",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${state.amount.formatDecimal(2)} €",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Saved cards
            if (state.availableCards.isNotEmpty()) {
                item {
                    Text(
                        text = "Saved cards",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(state.availableCards) { card ->
                    PaymentCardItem(
                        card = card,
                        onCardSelected = {
                            viewModel.selectPaymentMethod(PaymentMethod.CREDIT_CARD, card.id)
                        }
                    )
                }
            }

            // Add new card
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.selectPaymentMethod(PaymentMethod.CREDIT_CARD, null)
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Ajouter",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Add new card",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Other payment methods
            item {
                Text(
                    text = "Other methods",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(
                listOf(
                    PaymentMethod.PAYPAL,
                    PaymentMethod.APPLE_PAY,
                    PaymentMethod.GOOGLE_PAY,
                    PaymentMethod.BANK_TRANSFER
                )
            ) { method ->
                PaymentMethodItem(
                    method = method,
                    onMethodSelected = {
                        viewModel.selectPaymentMethod(method)
                    }
                )
            }
        }
    }
}

@Composable
fun PaymentCardItem(
    card: PaymentCard,
    onCardSelected: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onCardSelected
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OfficialCardTypeIcon(
                cardType = card.cardType,
                modifier = Modifier.size(40.dp, 24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = card.cardNumber,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${card.cardType.displayName} • ${card.cardHolderName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (card.isDefault) {
                AssistChip(
                    onClick = { },
                    label = { Text("Default") }
                )
            }
        }
    }
}

@Composable
fun PaymentMethodItem(
    method: PaymentMethod,
    onMethodSelected: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onMethodSelected
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OfficialPaymentMethodIcon(
                method = method,
                modifier = Modifier.size(48.dp, 32.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = method.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = when (method) {
                        PaymentMethod.PAYPAL -> "Secure payment via PayPal"
                        PaymentMethod.APPLE_PAY -> "Touch ID or Face ID"
                        PaymentMethod.GOOGLE_PAY -> "Fast and secure payment"
                        PaymentMethod.CREDIT_CARD -> "Visa, Mastercard, etc."
                        PaymentMethod.BANK_TRANSFER -> "Instant SEPA transfer"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
