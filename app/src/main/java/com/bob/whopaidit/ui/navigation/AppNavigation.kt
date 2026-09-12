package com.bob.whopaidit.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bob.whopaidit.ui.screen.ForgotPasswordScreen
import com.bob.whopaidit.ui.screen.LoginScreen
import com.bob.whopaidit.ui.screen.SignupScreen
import com.bob.whopaidit.ui.screen.SplashScreen

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val FORGOT_PASSWORD = "forgot_password"
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        modifier = modifier,
    ) {
        composable(Routes.SPLASH) {
            SplashScreen {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            }
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginClick = { _, _ ->
                    // Login action
                },
                onGoogleLoginClick = {
                    // Google login action
                },
                onForgotPasswordClick = {
                    navController.navigate(Routes.FORGOT_PASSWORD)
                },
                onSignUpClick = {
                    navController.navigate(Routes.SIGNUP)
                },
            )
        }

        composable(Routes.SIGNUP) {
            SignupScreen(
                onSignupClick = { _, _, _ ->
                    // Signup action
                },
                onGoogleSignupClick = {
                    // Google signup action
                },
                onLoginClick = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onSendResetLinkClick = { _ ->
                    // Send reset link action
                },
                onBackClick = {
                    navController.popBackStack()
                },
                onLoginClick = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
            )
        }
    }
}
