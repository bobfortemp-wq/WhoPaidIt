package com.bob.whopaidit.ui.navigation

import android.widget.Toast
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.bob.whopaidit.ui.screen.CreateTripBottomSheet
import com.bob.whopaidit.ui.screen.DashboardScreen
import com.bob.whopaidit.ui.screen.ForgotPasswordScreen
import com.bob.whopaidit.ui.screen.LoginScreen
import com.bob.whopaidit.ui.screen.SignupScreen
import com.bob.whopaidit.ui.screen.SplashScreen
import com.bob.whopaidit.ui.screen.TripDetailScreen
import com.bob.whopaidit.ui.screen.TripItem
import com.bob.whopaidit.viewModel.AuthState
import com.bob.whopaidit.viewModel.AuthViewModel
import com.bob.whopaidit.viewModel.HomeViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()
    val savedEmail by authViewModel.savedEmail.collectAsState()
    val savedPassword by authViewModel.savedPassword.collectAsState()
    val rememberMe by authViewModel.rememberMe.collectAsState()

    val isLoading = authState is AuthState.Loading
    var selectedTripItem by remember { mutableStateOf<TripItem?>(null) }

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(300),
            )
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth },
                animationSpec = tween(300),
            )
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth },
                animationSpec = tween(300),
            )
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(300),
            )
        },
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
                savedEmailPref = savedEmail,
                savedPasswordPref = savedPassword,
                rememberMePref = rememberMe,
                isLoading = isLoading,
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
                            Toast.makeText(
                                context,
                                "Account created successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
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
                            Toast.makeText(
                                context,
                                "Account created successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
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
                            Toast.makeText(
                                context,
                                "Password reset link sent to your email",
                                Toast.LENGTH_LONG
                            ).show()
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

            DashboardScreen(
                userName = userName,
                userEmail = userEmail,
                onLogoutClick = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onNewTripClick = {
                    navController.navigate(Routes.CREATE_TRIP)
                },
                onTripClick = { trip ->
                    selectedTripItem = trip
                    navController.navigate(Routes.TRIP_DETAIL)
                },
            )
        }

        composable(Routes.CREATE_TRIP) {
            val homeViewModel: HomeViewModel = hiltViewModel()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            ) {
                CreateTripBottomSheet(
                    onDismissRequest = {
                        navController.popBackStack()
                    },
                    onCreateTripSuccess = { newTour, startDate, endDate ->
                        homeViewModel.createTrip(
                            id = newTour.id,
                            title = newTour.name,
                            description = newTour.description,
                            currency = newTour.currency,
                            startDate = startDate,
                            endDate = endDate,
                            members = newTour.memberIds,
                        )
                        navController.popBackStack()
                    },
                )
            }
        }

        composable(Routes.TRIP_DETAIL) {
            val homeViewModel: HomeViewModel = hiltViewModel()
            val trip = selectedTripItem
            val savedExpenses by homeViewModel.getExpensesForTrip(trip?.id.orEmpty()).collectAsState(initial = emptyList())

            TripDetailScreen(
                tripId = trip?.id.orEmpty(),
                tripTitle = trip?.title.orEmpty().ifEmpty { "Trip Details" },
                membersCount = trip?.numberOfPeople ?: 1,
                startDate = trip?.startDate.orEmpty().ifEmpty { "Start Date" },
                endDate = trip?.endDate.orEmpty().ifEmpty { "End Date" },
                description = trip?.description.orEmpty(),
                currency = trip?.currency.orEmpty().ifEmpty { "INR (₹)" },
                participants = trip?.participants ?: listOf("You"),
                savedExpenses = savedExpenses,
                onBackClick = {
                    navController.popBackStack()
                },
                onEditTripClick = { id, title, desc, curr, start, end, newParticipants ->
                    homeViewModel.updateTrip(
                        id = id.ifEmpty { trip?.id.orEmpty() },
                        title = title,
                        description = desc,
                        currency = curr,
                        startDate = start,
                        endDate = end,
                        members = newParticipants,
                    )
                },
                onAddExpenseSubmit = { newExpense ->
                    homeViewModel.addExpense(newExpense)
                },
                onDeleteExpenseSubmit = { expenseId ->
                    homeViewModel.deleteExpense(expenseId, trip?.id.orEmpty())
                },
            )
        }
    }
}
