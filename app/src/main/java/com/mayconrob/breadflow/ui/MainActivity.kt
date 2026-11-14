package com.mayconrob.breadflow.ui

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mayconrob.breadflow.ui.navigation.BreadFlowBottomBar
import com.mayconrob.breadflow.ui.navigation.BreadFlowNavHost
import com.mayconrob.breadflow.ui.navigation.Screen
import com.mayconrob.breadflow.ui.theme.BreadFlowTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Sobrescrevendo o comportamento padrão da Splash Screen e sua animação de saída
        splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
            // Se desejar ocultar o ícone padrão ou personalizar o comportamento:
            // splashScreenViewProvider.iconView.visibility = View.GONE

            val view = splashScreenViewProvider.view
            val slideOut = ObjectAnimator.ofFloat(
                view,
                View.ALPHA,
                1f,
                0f
            ).apply {
                duration = 350L
                doOnEnd {
                    splashScreenViewProvider.remove()
                }
            }
            slideOut.start()
        }

        enableEdgeToEdge()
        setContent {
            BreadFlowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BreadFlowApp()
                }
            }
        }
    }
}

@Composable
fun BreadFlowApp(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = Screen.bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        bottomBar = {
            if (showBottomBar) {
                BreadFlowBottomBar(navController = navController)
            }
        }
    ) { paddingValues ->
        BreadFlowNavHost(
            navController = navController,
            modifier = Modifier.padding(paddingValues)
        )
    }
}
