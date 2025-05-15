package org.perso.bikerbox.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.perso.bikerbox.data.models.Resource
import org.perso.bikerbox.ui.viewmodel.AuthViewModel
import org.perso.bikerbox.utils.isValidEmail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }

    val authOperation by authViewModel.authOperation.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Effet pour gérer les résultats des opérations d'authentification
    LaunchedEffect(authOperation) {
        when (authOperation) {
            is Resource.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Email de réinitialisation envoyé. Vérifiez votre boîte de réception.")
                }
                // Réinitialiser l'état après affichage du message
                authViewModel.resetOperationState()
            }
            is Resource.Error -> {
                val errorMessage = (authOperation as Resource.Error).message
                scope.launch {
                    snackbarHostState.showSnackbar(errorMessage)
                }
                // Réinitialiser l'état après affichage du message d'erreur
                authViewModel.resetOperationState()
            }
            else -> { /* Ne rien faire pour Loading ou null */ }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mot de passe oublié") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Réinitialisation du mot de passe",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Text(
                text = "Entrez votre adresse email pour recevoir un lien de réinitialisation de mot de passe.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Champ email
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null // Effacer l'erreur lorsque l'utilisateur commence à taper
                },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                isError = emailError != null,
                supportingText = emailError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Bouton d'envoi
            Button(
                onClick = {
                    // Validation de l'email
                    if (email.isBlank()) {
                        emailError = "L'email ne peut pas être vide"
                        return@Button
                    }

                    if (!isValidEmail(email)) {
                        emailError = "Format d'email invalide"
                        return@Button
                    }
                    authViewModel.sendPasswordReset(email)
                },
                enabled = authOperation !is Resource.Loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (authOperation is Resource.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Envoyer le lien de réinitialisation")
                }
            }
        }
    }
}
