package org.perso.bikerbox.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

class SimpleNavigationController {
    private var _currentRoute = mutableStateOf<Route>("login")
    val currentRoute get() = _currentRoute.value

    private val backStack = mutableListOf<Route>()

    // Navigation vers une nouvelle route
    fun navigateTo(route: Route) {
        backStack.add(_currentRoute.value)
        _currentRoute.value = route
    }

    // Navigation avec remplacement de la route actuelle (ne l'ajoute pas à la pile)
    fun navigateAndReplace(route: Route) {
        _currentRoute.value = route
    }

    // Navigation avec effacement de toute la pile et remplacement par une nouvelle route
    fun navigateAndClearBackStack(route: Route) {
        backStack.clear()
        _currentRoute.value = route
    }

    // Retourne à l'écran précédent s'il existe
    fun popBackStack(): Boolean {
        if (backStack.isEmpty()) return false

        _currentRoute.value = backStack.removeAt(backStack.size - 1)
        return true
    }
}

// Type Route simple - soit une chaîne, soit un objet avec des paramètres
typealias Route = String

// Fonction pour créer et mémoriser le contrôleur de navigation
@Composable
fun rememberNavigationController(): SimpleNavigationController {
    return remember { SimpleNavigationController() }
}
