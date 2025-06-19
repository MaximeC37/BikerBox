package org.perso.bikerbox.ui.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import bikerbox.composeapp.generated.resources.*
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.perso.bikerbox.data.models.PaymentMethod

@OptIn(ExperimentalResourceApi::class)
@Composable
fun OfficialPaymentMethodIcon(
    method: PaymentMethod,
    modifier: Modifier = Modifier
) {
    Log.e("🔥 PAYMENT_ICON", "Called with method: ${method.displayName}")

    var showFallback by remember { mutableStateOf(false) }

    if (showFallback) {
        Text(
            text = method.icon,
            style = MaterialTheme.typography.headlineMedium,
            modifier = modifier
        )
    } else {
        when (method) {
            PaymentMethod.PAYPAL -> {
                PngImage(
                    resource = Res.drawable.pp_cc_mark_74x46,
                    contentDescription = "PayPal",
                    modifier = modifier.size(48.dp, 32.dp),
                    onError = { showFallback = true }
                )
            }
            PaymentMethod.APPLE_PAY -> {
                PngImage(
                    resource = Res.drawable.Apple_Pay_Mark_RGB_041619,
                    contentDescription = "Apple Pay",
                    modifier = modifier.size(48.dp, 32.dp),
                    onError = { showFallback = true }
                )
            }
            PaymentMethod.GOOGLE_PAY -> {
                PngImage(
                    resource = Res.drawable.google_pay_mark_800,
                    contentDescription = "Google Pay",
                    modifier = modifier.size(48.dp, 32.dp),
                    onError = { showFallback = true }
                )
            }
            PaymentMethod.CREDIT_CARD -> {
                Text(
                    text = method.icon,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = modifier
                )
            }
            PaymentMethod.BANK_TRANSFER -> {
                Text(
                    text = method.icon,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = modifier
                )
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun OfficialCardTypeIcon(
    cardType: org.perso.bikerbox.data.models.CardType,
    modifier: Modifier = Modifier
) {
    var showFallback by remember { mutableStateOf(false) }

    if (showFallback) {
        Text(
            text = cardType.icon,
            style = MaterialTheme.typography.headlineMedium,
            modifier = modifier
        )
    } else {
        when (cardType) {
            org.perso.bikerbox.data.models.CardType.VISA -> {
                PngImage(
                    resource = Res.drawable.visa_logo,
                    contentDescription = "Visa",
                    modifier = modifier.size(40.dp, 24.dp),
                    onError = { showFallback = true }
                )
            }
            org.perso.bikerbox.data.models.CardType.MASTERCARD -> {
                PngImage(
                    resource = Res.drawable.Mastercard_logo,
                    contentDescription = "Mastercard",
                    modifier = modifier.size(40.dp, 24.dp),
                    onError = { showFallback = true }
                )
            }
            org.perso.bikerbox.data.models.CardType.AMERICAN_EXPRESS,
            org.perso.bikerbox.data.models.CardType.DISCOVER -> {
                Text(
                    text = cardType.icon,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = modifier
                )
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun PngImage(
    resource: org.jetbrains.compose.resources.DrawableResource,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onError: () -> Unit
) {
    Image(
        painter = painterResource(resource),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}