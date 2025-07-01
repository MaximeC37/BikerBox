package org.perso.bikerbox.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import bikerbox.composeapp.generated.resources.Email
import bikerbox.composeapp.generated.resources.Forgot_your_password
import bikerbox.composeapp.generated.resources.Invalid_email
import bikerbox.composeapp.generated.resources.Log_in
import bikerbox.composeapp.generated.resources.New_user_create_account
import bikerbox.composeapp.generated.resources.Password
import bikerbox.composeapp.generated.resources.Res
import bikerbox.composeapp.generated.resources._6_characters_min
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.perso.bikerbox.data.models.Resource
import org.perso.bikerbox.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onNavigateToSignUp: () -> Unit,
    onNavigateToMain: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isEmailValid by remember { mutableStateOf(true) }
    var isPasswordValid by remember { mutableStateOf(true) }

    val authState by authViewModel.authState.collectAsState()
    val authOperation by authViewModel.authOperation.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(authState) {
        if (authState is Resource.Success && (authState as Resource.Success).data != null) {
            onNavigateToMain()
        }
    }

    LaunchedEffect(authOperation) {
        when (authOperation) {
            is Resource.Success -> {
                authViewModel.resetOperationState()
                onNavigateToMain()
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Connexion",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    isEmailValid = it.isEmpty() || isValidEmail(it)
                },
                label = { Text(stringResource(Res.string.Email)) },
                isError = !isEmailValid,
                supportingText = {
                    if (!isEmailValid) Text(stringResource(Res.string.Invalid_email))
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    isPasswordValid = it.length >= 6
                },
                label = { Text(stringResource(Res.string.Password)) },
                isError = !isPasswordValid,
                supportingText = {
                    if (!isPasswordValid) Text(stringResource(Res.string._6_characters_min))
                },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onNavigateToForgotPassword,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(Res.string.Forgot_your_password))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    authViewModel.signIn(email, password)
                },
                enabled = email.isNotEmpty() && isEmailValid && password.isNotEmpty() && isPasswordValid &&
                        authOperation !is Resource.Loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (authOperation is Resource.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(Res.string.Log_in))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onNavigateToSignUp
            ) {
                Text(stringResource(Res.string.New_user_create_account))
            }
        }
    }
}

private fun isValidEmail(email: String): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"
    return email.matches(emailRegex.toRegex())
}
