package org.perso.bikerbox.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import bikerbox.composeapp.generated.resources.Email
import bikerbox.composeapp.generated.resources.Enter_email_to_receive_password_reset_link
import bikerbox.composeapp.generated.resources.Forgotten_password
import bikerbox.composeapp.generated.resources.Password_Reset
import bikerbox.composeapp.generated.resources.Res
import bikerbox.composeapp.generated.resources.Send_reset_link
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
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

    LaunchedEffect(authOperation) {
        when (authOperation) {
            is Resource.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Reset email sent. Check your inbox.")
                }
                authViewModel.resetOperationState()
            }
            is Resource.Error -> {
                val errorMessage = (authOperation as Resource.Error).message
                scope.launch {
                    snackbarHostState.showSnackbar(errorMessage)
                }
                authViewModel.resetOperationState()
            }
            else -> { /* Do nothing for Loading or null */ }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.Forgotten_password)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                text = stringResource(Res.string.Password_Reset),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Text(
                text = stringResource(Res.string.Enter_email_to_receive_password_reset_link),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null
                },
                label = { Text(stringResource(Res.string.Email)) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                isError = emailError != null,
                supportingText = emailError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Send button
            Button(
                onClick = {
                    // Email validation
                    if (email.isBlank()) {
                        emailError = "Email cannot be empty"
                        return@Button
                    }

                    if (!isValidEmail(email)) {
                        emailError = "Invalid email format"
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
                    Text(stringResource(Res.string.Send_reset_link))
                }
            }
        }
    }
}
