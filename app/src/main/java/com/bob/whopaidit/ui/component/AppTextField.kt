package com.bob.whopaidit.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bob.whopaidit.R
import com.bob.whopaidit.ui.theme.WhoPaidItTheme

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isPassword: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    var isPasswordVisible by remember { mutableStateOf(value = false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val actualVisualTransformation = when {
        isPassword && !isPasswordVisible -> PasswordVisualTransformation()
        else -> VisualTransformation.None
    }

    val borderColor = when {
        isError -> MaterialTheme.colorScheme.error
        isFocused -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    }

    Column(modifier = modifier) {
        // Label displayed JUST ABOVE the textfield
        if (!label.isNullOrBlank()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onBackground
                },
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }

        // BasicTextField Container Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
//                .background(
//                    color = MaterialTheme.colorScheme.surface,
//                    shape = RoundedCornerShape(12.dp),
//                )
                .border(
                    width = if (isFocused || isError) 1.5.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(12.dp),
                )
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Leading Icon
                if (leadingIcon != null) {
                    leadingIcon()
                    Spacer(modifier = Modifier.padding(start = 10.dp))
                }

                // Core BasicTextField
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                        )
                    }

                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = enabled,
                        readOnly = readOnly,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onBackground,
                        ),
                        singleLine = singleLine,
                        visualTransformation = actualVisualTransformation,
                        keyboardOptions = if (isPassword) {
                            keyboardOptions.copy(keyboardType = KeyboardType.Password)
                        } else {
                            keyboardOptions
                        },
                        keyboardActions = keyboardActions,
                        interactionSource = interactionSource,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    )
                }

                // Trailing Icon or Password Visibility Toggle
                val effectiveTrailingIcon: @Composable (() -> Unit)? = when {
                    isPassword -> {
                        {
                            IconButton(
                                onClick = { isPasswordVisible = !isPasswordVisible },
                                modifier = Modifier.size(24.dp),
                            ) {
                                Icon(
                                    painter = painterResource(
                                        id = if (isPasswordVisible) {
                                            R.drawable.ic_visibility
                                        } else {
                                            R.drawable.ic_visibility_off
                                        },
                                    ),
                                    contentDescription = if (isPasswordVisible) "Hide Password" else "Show Password",
                                    tint = MaterialTheme.colorScheme.outline,
                                )
                            }
                        }
                    }

                    trailingIcon != null -> trailingIcon
                    else -> null
                }

                if (effectiveTrailingIcon != null) {
                    Spacer(modifier = Modifier.padding(start = 10.dp))
                    effectiveTrailingIcon()
                }
            }
        }

        // Error Message Helper Text
        if (isError && !errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppTextFieldPreview() {
    WhoPaidItTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Name Field
            AppTextField(
                value = "",
                onValueChange = {},
                label = "Full Name",
                placeholder = "John Doe",
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )

            // Email Field
            AppTextField(
                value = "john@example.com",
                onValueChange = {},
                label = "Email Address",
                placeholder = "example@domain.com",
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_email),
                        contentDescription = "Email Icon",
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
            )

            // Password Field
            AppTextField(
                value = "secret123",
                onValueChange = {},
                label = "Password",
                placeholder = "Enter your password",
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lock),
                        contentDescription = "Lock Icon",
                    )
                },
                isPassword = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            )
        }
    }
}
