package com.bob.whopaidit.ui.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bob.whopaidit.ui.theme.WhoPaidItTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginScreen_displaysAllInitialUiElements() {
        composeTestRule.setContent {
            WhoPaidItTheme {
                LoginScreen()
            }
        }

        composeTestRule.onNodeWithContentDescription("WhoPaidIt Logo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Welcome Back").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign in to manage and split group expenses").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email Address").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Remember Me").assertIsDisplayed()
        composeTestRule.onNodeWithText("Forgot Password?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Log In").assertIsDisplayed()
        composeTestRule.onNodeWithText("Continue with Google").assertIsDisplayed()
        composeTestRule.onNodeWithText("Don't have an account?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign Up").assertIsDisplayed()
    }

    @Test
    fun loginScreen_displaysPrefilledSavedValues() {
        composeTestRule.setContent {
            WhoPaidItTheme {
                LoginScreen(
                    savedEmailPref = "saved@example.com",
                    savedPasswordPref = "savedPassword123",
                    rememberMePref = true,
                )
            }
        }

        composeTestRule.onNodeWithText("saved@example.com").assertIsDisplayed()
        composeTestRule.onNodeWithText("savedPassword123").assertIsDisplayed()
    }

    @Test
    fun loginScreen_enteringEmailAndPassword_triggersLoginClick() {
        var loggedInEmail = ""
        var loggedInPassword = ""
        var loggedInRememberMe = false

        composeTestRule.setContent {
            WhoPaidItTheme {
                LoginScreen(
                    onLoginClick = { email, password, rememberMe ->
                        loggedInEmail = email
                        loggedInPassword = password
                        loggedInRememberMe = rememberMe
                    },
                )
            }
        }

        composeTestRule.onNodeWithText("example@domain.com").performTextInput("user@whopaidit.com")
        composeTestRule.onNodeWithText("Enter your password").performTextInput("mySecretPass")
        composeTestRule.onNodeWithText("Log In").performClick()

        assertEquals("user@whopaidit.com", loggedInEmail)
        assertEquals("mySecretPass", loggedInPassword)
        assertFalse(loggedInRememberMe)
    }

    @Test
    fun loginScreen_togglingRememberMe_passesTrueOnLogin() {
        var loggedInRememberMe = false

        composeTestRule.setContent {
            WhoPaidItTheme {
                LoginScreen(
                    onLoginClick = { _, _, rememberMe ->
                        loggedInRememberMe = rememberMe
                    },
                )
            }
        }

        composeTestRule.onNodeWithText("Remember Me").performClick()
        composeTestRule.onNodeWithText("Log In").performClick()

        assertTrue(loggedInRememberMe)
    }

    @Test
    fun loginScreen_clickingForgotPassword_triggersCallback() {
        var clicked = false

        composeTestRule.setContent {
            WhoPaidItTheme {
                LoginScreen(
                    onForgotPasswordClick = { clicked = true },
                )
            }
        }

        composeTestRule.onNodeWithText("Forgot Password?").performClick()

        assertTrue(clicked)
    }

    @Test
    fun loginScreen_clickingGoogleLogin_triggersCallback() {
        var clicked = false

        composeTestRule.setContent {
            WhoPaidItTheme {
                LoginScreen(
                    onGoogleLoginClick = { clicked = true },
                )
            }
        }

        composeTestRule.onNodeWithText("Continue with Google").performClick()

        assertTrue(clicked)
    }

    @Test
    fun loginScreen_clickingSignUp_triggersCallback() {
        var clicked = false

        composeTestRule.setContent {
            WhoPaidItTheme {
                LoginScreen { clicked = true }
            }
        }

        composeTestRule.onNodeWithText("Sign Up").performClick()

        assertTrue(clicked)
    }

    @Test
    fun loginScreen_isLoadingTrue_disablesInteractiveElementsAndHidesLoginText() {
        composeTestRule.setContent {
            WhoPaidItTheme {
                LoginScreen(isLoading = true)
            }
        }

        // When loading, "Log In" text is replaced by CircularProgressIndicator
        composeTestRule.onNodeWithText("Log In").assertDoesNotExist()
        // Buttons should be disabled
        composeTestRule.onNodeWithText("Forgot Password?").assertIsNotEnabled()
        composeTestRule.onNodeWithText("Continue with Google").assertIsNotEnabled()
        composeTestRule.onNodeWithText("Sign Up").assertIsNotEnabled()
    }
}
