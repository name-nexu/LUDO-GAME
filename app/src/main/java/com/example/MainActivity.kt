package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.AchievementsScreen
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.LobbyScreen
import com.example.ui.screens.MatchHistoryScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ResultScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.NexuDarkBackground
import com.example.ui.theme.NexuLudoTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NexuLudoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = NexuDarkBackground
                ) {
                    NexuMainNavigation(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun NexuMainNavigation(viewModel: GameViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()

    // Handle Android system back press
    BackHandler(enabled = currentScreen != AppScreen.HOME) {
        when (currentScreen) {
            AppScreen.MATCH_HISTORY -> viewModel.navigateTo(AppScreen.PROFILE)
            AppScreen.ADMIN -> viewModel.navigateTo(AppScreen.SETTINGS)
            AppScreen.GAME -> {
                // Return to home from game
                viewModel.navigateTo(AppScreen.HOME)
            }
            else -> viewModel.navigateTo(AppScreen.HOME)
        }
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "screen_transition"
    ) { screen ->
        when (screen) {
            AppScreen.HOME -> HomeScreen(viewModel = viewModel)
            AppScreen.LOBBY -> LobbyScreen(viewModel = viewModel)
            AppScreen.GAME -> GameScreen(viewModel = viewModel)
            AppScreen.RESULT -> ResultScreen(viewModel = viewModel)
            AppScreen.LEADERBOARD -> LeaderboardScreen(viewModel = viewModel)
            AppScreen.PROFILE -> ProfileScreen(viewModel = viewModel)
            AppScreen.ACHIEVEMENTS -> AchievementsScreen(viewModel = viewModel)
            AppScreen.MATCH_HISTORY -> MatchHistoryScreen(viewModel = viewModel)
            AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
            AppScreen.ADMIN -> AdminDashboardScreen(viewModel = viewModel)
        }
    }
}
