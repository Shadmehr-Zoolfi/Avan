package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AvanBottomBar
import com.example.ui.components.AvanTopBar
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InteractiveLessonScreen
import com.example.ui.screens.LearningPathScreen
import com.example.ui.screens.OnboardingAssessmentScreen
import com.example.ui.screens.PracticeScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.SongLibraryScreen
import com.example.ui.screens.SubscriptionScreen
import com.example.ui.screens.TeacherChatScreen
import com.example.ui.theme.AvanTheme
import com.example.ui.theme.ObsidianDeep
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.AvanViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AvanTheme {
                // Persian RTL Direction across the entire application
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val viewModel: AvanViewModel = viewModel()
                    AvanAppContent(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AvanAppContent(viewModel: AvanViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    // Handle back button: return to Home screen if on secondary screen
    if (currentScreen != AppScreen.HOME && currentScreen != AppScreen.ONBOARDING_ASSESSMENT) {
        BackHandler {
            viewModel.navigateTo(AppScreen.HOME)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianDeep),
        topBar = {
            if (currentScreen != AppScreen.ONBOARDING_ASSESSMENT) {
                AvanTopBar(
                    userProfile = userProfile,
                    onChatClick = { viewModel.navigateTo(AppScreen.TEACHER_CHAT) },
                    onSubscriptionClick = { viewModel.navigateTo(AppScreen.SUBSCRIPTION) }
                )
            }
        },
        bottomBar = {
            if (currentScreen != AppScreen.ONBOARDING_ASSESSMENT &&
                currentScreen != AppScreen.TEACHER_CHAT &&
                currentScreen != AppScreen.SUBSCRIPTION
            ) {
                AvanBottomBar(
                    currentScreen = currentScreen,
                    onTabSelected = { screen ->
                        viewModel.navigateTo(screen)
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ObsidianDeep)
        ) {
            Crossfade(targetState = currentScreen, label = "screenTransition") { screen ->
                when (screen) {
                    AppScreen.ONBOARDING_ASSESSMENT -> OnboardingAssessmentScreen(viewModel = viewModel)
                    AppScreen.HOME -> HomeScreen(viewModel = viewModel)
                    AppScreen.LEARN -> InteractiveLessonScreen(viewModel = viewModel)
                    AppScreen.PRACTICE -> PracticeScreen(viewModel = viewModel)
                    AppScreen.SONGS -> SongLibraryScreen(viewModel = viewModel)
                    AppScreen.PROGRESS -> ProgressScreen(viewModel = viewModel)
                    AppScreen.TEACHER_CHAT -> TeacherChatScreen(viewModel = viewModel)
                    AppScreen.SUBSCRIPTION -> SubscriptionScreen(viewModel = viewModel)
                }
            }
        }
    }
}

