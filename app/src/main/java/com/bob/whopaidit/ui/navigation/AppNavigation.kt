package com.bob.whopaidit.ui.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bob.whopaidit.viewModel.AuthViewModel
import com.bob.whopaidit.ui.screen.ForgotPasswordScreen
import com.bob.whopaidit.ui.screen.HomeScreen
import com.bob.whopaidit.ui.screen.LoginScreen
import com.bob.whopaidit.ui.screen.SignupScreen
import com.bob.whopaidit.ui.screen.SplashScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        modifier = modifier,
    ) {
        composable(Routes.SPLASH) {
            SplashScreen {
                if (authViewModel.isUserLoggedIn()) {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                } else {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            }
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginClick = { email, password, isRememberMeChecked ->
                    authViewModel.login(
                        email = email,
                        password = password,
                        rememberMe = isRememberMeChecked,
                        onSuccess = { user ->
                            Toast.makeText(
                                context,
                                "Welcome back, ${user.fullName.ifEmpty { "User" }}!",
                                Toast.LENGTH_SHORT,
                            ).show()
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                            }
                        },
                        onError = { error ->
                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                        },
                    )
                },
                onGoogleLoginClick = {
                    authViewModel.signInWithGoogle(
                        context = context,
                        onSuccess = { user ->
                            Toast.makeText(
                                context,
                                "Welcome, ${user.fullName.ifEmpty { "User" }}!",
                                Toast.LENGTH_SHORT,
                            ).show()
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                            }
                        },
                        onError = { error ->
                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                        },
                    )
                },
                onForgotPasswordClick = {
                    authViewModel.resetState()
                    navController.navigate(Routes.FORGOT_PASSWORD)
                },
                onSignUpClick = {
                    authViewModel.resetState()
                    navController.navigate(Routes.SIGNUP)
                },
            )
        }

        composable(Routes.SIGNUP) {
            SignupScreen(
                onSignupClick = { fullName, email, password ->
                    authViewModel.signUp(
                        fullName = fullName,
                        email = email,
                        password = password,
                        onSuccess = { _ ->
                            Toast.makeText(context, "Account created successfully!", Toast.LENGTH_SHORT).show()
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.SIGNUP) { inclusive = true }
                            }
                        },
                        onError = { error ->
                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                        },
                    )
                },
                onGoogleSignupClick = {
                    authViewModel.signInWithGoogle(
                        context = context,
                        onSuccess = { _ ->
                            Toast.makeText(context, "Account created successfully!", Toast.LENGTH_SHORT).show()
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.SIGNUP) { inclusive = true }
                            }
                        },
                        onError = { error ->
                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                        },
                    )
                },
                onLoginClick = {
                    authViewModel.resetState()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onSendResetLinkClick = { email ->
                    authViewModel.sendPasswordResetEmail(
                        email = email,
                        onSuccess = {
                            Toast.makeText(context, "Password reset link sent to your email", Toast.LENGTH_LONG).show()
                        },
                        onError = { error ->
                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                        },
                    )
                },
                onBackClick = {
                    navController.popBackStack()
                },
                onLoginClick = {
                    authViewModel.resetState()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.HOME) {
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            val userName = firebaseUser?.displayName.orEmpty().ifEmpty { "User" }
            val userEmail = firebaseUser?.email.orEmpty()

            HomeScreen(
                userName = userName,
                userEmail = userEmail,
                onLogoutClick = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
            )
        }
    }
}
