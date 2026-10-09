package com.bob.whopaidit.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bob.whopaidit.R
import com.bob.whopaidit.ui.theme.WhoPaidItTheme

@Composable
fun GroupScreen(
    modifier: Modifier = Modifier,
    onCreateGroupClick: () -> Unit = {},
) {
    val groups = emptyList<Triple<String, String, String>>()

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Your Groups",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Button(
                    onClick = onCreateGroupClick,
                    shape = RoundedCornerShape(999.dp),
                ) {
                    Text("+ New Group", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (groups.isEmpty()) {
                EmptyStateCard(
                    iconRes = R.drawable.ic_groups,
                    title = "No Active Groups",
                    description = "Tap + New Group to start splitting with friends & family",
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GroupScreenPreview() {
    WhoPaidItTheme {
        GroupScreen()
    }
}
