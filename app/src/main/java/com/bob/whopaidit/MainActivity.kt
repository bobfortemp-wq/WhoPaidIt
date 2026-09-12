package com.bob.whopaidit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.bob.whopaidit.ui.navigation.AppNavigation
import com.bob.whopaidit.ui.screen.LoginScreen
import com.bob.whopaidit.ui.theme.WhoPaidItTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WhoPaidItTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    AppNavigation(modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    WhoPaidItTheme {
        LoginScreen()
    }
}
