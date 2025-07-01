package org.perso.bikerbox.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

class SimpleNavigationController {
    private var _currentRoute = mutableStateOf<Route>("login")
    val currentRoute get() = _currentRoute.value

    private val backStack = mutableListOf<Route>()

    fun navigateTo(route: Route) {
        backStack.add(_currentRoute.value)
        _currentRoute.value = route
    }

    fun navigateAndReplace(route: Route) {
        _currentRoute.value = route
    }

    fun navigateAndClearBackStack(route: Route) {
        backStack.clear()
        _currentRoute.value = route
    }

    fun popBackStack(): Boolean {
        if (backStack.isEmpty()) return false

        _currentRoute.value = backStack.removeAt(backStack.size - 1)
        return true
    }
}

typealias Route = String

@Composable
fun rememberNavigationController(): SimpleNavigationController {
    return remember { SimpleNavigationController() }
}
