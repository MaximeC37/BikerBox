package org.perso.bikerbox.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.perso.bikerbox.data.models.Resource
import org.perso.bikerbox.ui.viewmodel.AuthViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    val authOperation by authViewModel.authOperation.collectAsState()
    val currentUser = if (authState is Resource.Success) (authState as Resource.Success).data else null

    var displayName by remember { mutableStateOf(currentUser?.displayName ?: "") }
    var phoneNumber by remember { mutableStateOf(currentUser?.phoneNumber ?: "") }
    var isEditing by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Mettre à jour les champs si l'utilisateur change
    LaunchedEffect(currentUser) {
        currentUser?.let {
            displayName = it.displayName ?: ""
            phoneNumber = it.phoneNumber ?: ""
        }
    }

    // Gérer les résultats des opérations
    LaunchedEffect(authOperation) {
        when (authOperation) {
            is Resource.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Profil mis à jour avec succès")
                }
                isEditing = false
                authViewModel.resetOperationState()
            }
            is Resource.Error -> {
                val errorMessage = (authOperation as Resource.Error).message
                scope.launch {
                    snackbarHostState.showSnackbar(errorMessage)
                }
                authViewModel.resetOperationState()
            }
            else -> { /* Ne rien faire pour Loading ou null */ }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mon Profil") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { isEditing = !isEditing }) {
                        Icon(Icons.Default.Edit, contentDescription = "Modifier")
                    }
                }
            )
        },
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
            // Photo de profil (avec placehoder si aucune photo n'est disponible)
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = "Photo de profil",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Affichage de l'email (non modifiable)
            Text(
                text = "Email",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )
            Text(
                text = currentUser?.email ?: "",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Nom d'affichage
            if (isEditing) {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Nom d'affichage") },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = "Nom d'affichage",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                )
                Text(
                    text = displayName.ifEmpty { "Non défini" },
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Numéro de téléphone
            if (isEditing) {
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Numéro de téléphone") },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = "Numéro de téléphone",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                )
                Text(
                    text = phoneNumber.ifEmpty { "Non défini" },
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Bouton de sauvegarde (visible uniquement en mode édition)
            if (isEditing) {
                Button(
                    onClick = {
                        authViewModel.updateUserProfile(
                            displayName = displayName.takeIf { it.isNotEmpty() },
                            phoneNumber = phoneNumber.takeIf { it.isNotEmpty() }
                        )
                    },
                    enabled = authOperation !is Resource.Loading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (authOperation is Resource.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Enregistrer")
                    }
                }
            }
        }
    }
}
