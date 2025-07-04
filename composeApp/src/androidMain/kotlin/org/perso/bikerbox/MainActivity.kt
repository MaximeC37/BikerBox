package org.perso.bikerbox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.perso.bikerbox.data.repository.LockersProvider
import org.perso.bikerbox.data.repository.firebase.FirebaseLockersRepository

class MainActivity : ComponentActivity() {

    companion object {
        private val repositoryInstance = FirebaseLockersRepository()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        LockersProvider.initialize(repositoryInstance)

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}