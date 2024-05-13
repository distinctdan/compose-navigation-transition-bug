package com.example.navigationtransitionbug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.navigationtransitionbug.ui.theme.NavigationTransitionBugTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

private const val ROUTE_PAGE_1 = "ROUTE_PAGE_1"
private const val ROUTE_PAGE_2 = "ROUTE_PAGE_2"

private class NavActions(private val navController: NavController) {
    val navigateToPage2 = {
        navController.navigate(ROUTE_PAGE_2) {
            launchSingleTop = true
        }
    }

    val navigateBackPreventPopRoot: () -> Unit = {
        // Only pop if we have at least 1 other page and it's not page 1
        val backStack = navController.currentBackStack.value
        if (backStack.size > 1 && backStack.last().destination.route != ROUTE_PAGE_1) {
            navController.popBackStack()
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val navActions = remember(navController) { NavActions(navController) }

            NavigationTransitionBugTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(
                        navController,
                        startDestination = ROUTE_PAGE_1,
                        enterTransition = remember {{ fadeIn(tween(300)) }},
                        exitTransition = remember {{ fadeOut(tween(300)) }},
                        contentAlignment = Alignment.TopStart,
                    ) {
                        composable(
                            route = ROUTE_PAGE_1,
                        ) { backStackEntry ->
                            Page1(
                                navigateToPage2 = navActions.navigateToPage2,
                            )
                        }
                        composable(
                            route = ROUTE_PAGE_2,
                        ) { backStackEntry ->
                            Page2(
                                navigateBack = navActions.navigateBackPreventPopRoot,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NavBar(
    title: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun Page1(
    navigateToPage2: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Blue)
    ) {
        NavBar("Page 1")
        Button(onClick = navigateToPage2) {
            Text("Go To Page 2 + BLOCK 1 SECOND")
        }
    }

    // Simulate lag when leaving the page. In our real app, ExoPlayer can block the main thread for
    // up to 2 seconds per player due to surface detach bugs.
    DisposableEffect(Unit) {
        onDispose {
            runBlocking { delay(1000) }
        }
    }
}

@Composable
fun Page2(
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Red)
    ) {
        NavBar("Page 2")
        Button(onClick = navigateBack) {
            Text("Back To Page 1")
        }
    }
}