package com.bob.whopaidit.ui.screen

import androidx.compose.ui.test.assertIsDisplayed
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
class SignupScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun signupScreen_displaysAllInitialUiElements() {
        composeTestRule.setContent {
            WhoPaidItTheme {
                SignupScreen()
            }
        }

        composeTestRule.onNodeWithContentDescription("WhoPaidIt Logo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Create Account").assertIsDisplayed()
        composeTestRule.onNodeWithText("Join WhoPaidIt to track and split expenses with ease").assertIsDisplayed()
        composeTestRule.onNodeWithText("Full Name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email Address").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Confirm Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign Up").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign up with Google").assertIsDisplayed()
        composeTestRule.onNodeWithText("Already have an account?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Log In").assertIsDisplayed()
    }

    @Test
    fun signupScreen_validInput_triggersSignupClick() {
        var signedUpName = ""
        var signedUpEmail = ""
        var signedUpPassword = ""

        composeTestRule.setContent {
            WhoPaidItTheme {
                SignupScreen(
                    onSignupClick = { name, email, password ->
                        signedUpName = name
                        signedUpEmail = email
                        signedUpPassword = password
                    },
                )
            }
        }

        composeTestRule.onNodeWithText("John Doe").performTextInput("Jane Doe")
        composeTestRule.onNodeWithText("example@domain.com").performTextInput("jane@example.com")
        composeTestRule.onNodeWithText("Create a password").performTextInput("securePassword123")
        composeTestRule.onNodeWithText("Re-enter your password").performTextInput("securePassword123")

        composeTestRule.onNodeWithText("Sign Up").performClick()

        assertEquals("Jane Doe", signedUpName)
        assertEquals("jane@example.com", signedUpEmail)
        assertEquals("securePassword123", signedUpPassword)
    }

    @Test
    fun signupScreen_passwordMismatch_showsErrorAndDoesNotTriggerSignup() {
        var signupTriggered = false

        composeTestRule.setContent {
            WhoPaidItTheme {
                SignupScreen(
                    onSignupClick = { _, _, _ ->
                        signupTriggered = true
                    },
                )
            }
        }

        composeTestRule.onNodeWithText("John Doe").performTextInput("Jane Doe")
        composeTestRule.onNodeWithText("example@domain.com").performTextInput("jane@example.com")
        composeTestRule.onNodeWithText("Create a password").performTextInput("password123")
        composeTestRule.onNodeWithText("Re-enter your password").performTextInput("mismatchedPassword")

        composeTestRule.onNodeWithText("Sign Up").performClick()

        composeTestRule.onNodeWithText("Passwords do not match").assertIsDisplayed()
        assertFalse(signupTriggered)
    }

    @Test
    fun signupScreen_clickingGoogleSignup_triggersCallback() {
        var clicked = false

        composeTestRule.setContent {
            WhoPaidItTheme {
                SignupScreen(
                    onGoogleSignupClick = { clicked = true },
                )
            }
        }

        composeTestRule.onNodeWithText("Sign up with Google").performClick()

        assertTrue(clicked)
    }

    @Test
    fun signupScreen_clickingLogin_triggersCallback() {
        var clicked = false

        composeTestRule.setContent {
            WhoPaidItTheme {
                SignupScreen { clicked = true }
            }
        }

        composeTestRule.onNodeWithText("Log In").performClick()

        assertTrue(clicked)
    }
}
