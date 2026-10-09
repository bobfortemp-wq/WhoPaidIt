package com.bob.whopaidit.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bob.whopaidit.R
import com.bob.whopaidit.ui.theme.WhoPaidItTheme

@Composable
fun ActivityScreen(
    modifier: Modifier = Modifier,
) {
    val activities = emptyList<String>()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = "Activity Log",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (activities.isEmpty()) {
                EmptyStateCard(
                    iconRes = R.drawable.ic_activity,
                    title = "No Activity Recorded",
                    description = "Recent payments, settlements, and expense updates will appear here",
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ActivityScreenPreview() {
    WhoPaidItTheme {
        ActivityScreen()
    }
}
