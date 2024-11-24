package fr.swiftapp.territorymanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import fr.swiftapp.territorymanager.ui.nav.NavPage
import fr.swiftapp.territorymanager.ui.theme.TerritoryManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TerritoryManagerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    NavPage()
                }
            }
        }
    }
}