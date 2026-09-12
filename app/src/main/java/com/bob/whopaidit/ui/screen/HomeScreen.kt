package com.bob.whopaidit.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.bob.whopaidit.R
import com.bob.whopaidit.ui.theme.WhoPaidItTheme

data class ExpenseItem(
    val title: String,
    val subtitle: String,
    val amount: String,
    val time: String,
)

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    userName: String = "User",
    onProfileClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onAddExpenseClick: () -> Unit = {},
    onSettleUpClick: () -> Unit = {},
) {
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
            // Main ConstraintLayout container for Header, Greeting, Balance Card, Buttons & Section Title
            ConstraintLayout(
                modifier = Modifier.fillMaxWidth(),
            ) {
                val (
                    profileAvatar,
                    appName,
                    notificationIcon,
                    greetingText,
                    summarySubtitle,
                    balanceCard,
                    addExpenseBtn,
                    settleUpBtn,
                    recentExpensesHeader,
                ) = createRefs()

                // Profile Avatar Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        .clickable { onProfileClick() }
                        .constrainAs(profileAvatar) {
                            top.linkTo(parent.top)
                            start.linkTo(parent.start)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_person),
                        contentDescription = "Profile Image",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                }

                // App Name
                Text(
                    text = "WhoPaidIt",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.constrainAs(appName) {
                        start.linkTo(profileAvatar.end, margin = 12.dp)
                        top.linkTo(profileAvatar.top)
                        bottom.linkTo(profileAvatar.bottom)
                    },
                )

                // Notification Icon Button
                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier.constrainAs(notificationIcon) {
                        end.linkTo(parent.end)
                        top.linkTo(profileAvatar.top)
                        bottom.linkTo(profileAvatar.bottom)
                    },
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_notifications),
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(24.dp),
                    )
                }

                // Greeting Header
                Text(
                    text = "Hello, $userName! 👋",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.constrainAs(greetingText) {
                        top.linkTo(profileAvatar.bottom, margin = 16.dp)
                        start.linkTo(parent.start)
                    },
                )

                // Summary Subtitle
                Text(
                    text = "Here's your expense summary",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.constrainAs(summarySubtitle) {
                        top.linkTo(greetingText.bottom, margin = 2.dp)
                        start.linkTo(parent.start)
                    },
                )

                // Balance Summary Card
                Card(
                    modifier = Modifier.constrainAs(balanceCard) {
                        top.linkTo(summarySubtitle.bottom, margin = 20.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                ) {
                    ConstraintLayout(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                    ) {
                        val (
                            balanceLabel,
                            balanceValue,
                            divider,
                            youAreOwedLabel,
                            youAreOwedValue,
                            youOweLabel,
                            youOweValue,
                        ) = createRefs()

                        Text(
                            text = "Total Outstanding Balance",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            modifier = Modifier.constrainAs(balanceLabel) {
                                top.linkTo(parent.top)
                                start.linkTo(parent.start)
                            },
                        )

                        Text(
                            text = "+ ₹1,450.00",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.constrainAs(balanceValue) {
                                top.linkTo(balanceLabel.bottom, margin = 4.dp)
                                start.linkTo(parent.start)
                            },
                        )

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                            modifier = Modifier.constrainAs(divider) {
                                top.linkTo(balanceValue.bottom, margin = 16.dp)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                                width = Dimension.fillToConstraints
                            },
                        )

                        // You are owed section
                        Text(
                            text = "You are owed",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            modifier = Modifier.constrainAs(youAreOwedLabel) {
                                top.linkTo(divider.bottom, margin = 16.dp)
                                start.linkTo(parent.start)
                            },
                        )

                        Text(
                            text = "₹2,100.00",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.constrainAs(youAreOwedValue) {
                                top.linkTo(youAreOwedLabel.bottom, margin = 2.dp)
                                start.linkTo(parent.start)
                            },
                        )

                        // You owe section
                        Text(
                            text = "You owe",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            modifier = Modifier.constrainAs(youOweLabel) {
                                top.linkTo(divider.bottom, margin = 16.dp)
                                end.linkTo(parent.end)
                            },
                        )

                        Text(
                            text = "₹650.00",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.constrainAs(youOweValue) {
                                top.linkTo(youOweLabel.bottom, margin = 2.dp)
                                end.linkTo(parent.end)
                            },
                        )
                    }
                }

                // Add Expense Button
                Button(
                    onClick = onAddExpenseClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                    modifier = Modifier
                        .height(48.dp)
                        .constrainAs(addExpenseBtn) {
                            top.linkTo(balanceCard.bottom, margin = 20.dp)
                            start.linkTo(parent.start)
                            end.linkTo(settleUpBtn.start, margin = 6.dp)
                            width = Dimension.fillToConstraints
                        },
                ) {
                    Text(
                        text = "+ Add Expense",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }

                // Settle Up Button
                Button(
                    onClick = onSettleUpClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                    ),
                    modifier = Modifier
                        .height(48.dp)
                        .constrainAs(settleUpBtn) {
                            top.linkTo(balanceCard.bottom, margin = 20.dp)
                            start.linkTo(addExpenseBtn.end, margin = 6.dp)
                            end.linkTo(parent.end)
                            width = Dimension.fillToConstraints
                        },
                ) {
                    Text(
                        text = "Settle Up",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }

                // Recent Expenses Section Header
                Text(
                    text = "Recent Expenses",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.constrainAs(recentExpensesHeader) {
                        top.linkTo(addExpenseBtn.bottom, margin = 24.dp)
                        start.linkTo(parent.start)
                    },
                )
            }

            // Recent Expenses Items List
            val sampleExpenses = listOf(
                ExpenseItem("Dinner at Bistro", "Paid by you • Split with 4", "₹1,200.00", "Today, 8:30 PM"),
                ExpenseItem("Grocery Shopping", "Paid by Alex • You owe ₹350", "₹700.00", "Today, 4:15 PM"),
                ExpenseItem("Uber Ride to Airport", "Paid by you • Split with 2", "₹900.00", "Yesterday, 9:10 AM"),
                ExpenseItem("Weekend Movie Tickets", "Paid by Rahul • You owe ₹300", "₹600.00", "Sep 10, 7:45 PM"),
            )

            sampleExpenses.forEach { item ->
                ExpenseItemRow(
                    title = item.title,
                    subtitle = item.subtitle,
                    amount = item.amount,
                    time = item.time,
                )
            }
        }
    }
}

@Composable
fun ExpenseItemRow(
    title: String,
    subtitle: String,
    amount: String,
    time: String = "Today, 8:30 PM",
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            val (walletIcon, titleText, subtitleText, amountText, timeText) = createRefs()

            // Wallet Icon Container
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .constrainAs(walletIcon) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_wallet),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }

            // Expense Title
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.constrainAs(titleText) {
                    start.linkTo(walletIcon.end, margin = 12.dp)
                    top.linkTo(parent.top)
                    end.linkTo(amountText.start, margin = 8.dp)
                    width = Dimension.fillToConstraints
                },
            )

            // Expense Subtitle
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.constrainAs(subtitleText) {
                    start.linkTo(titleText.start)
                    top.linkTo(titleText.bottom, margin = 2.dp)
                    end.linkTo(amountText.start, margin = 8.dp)
                    width = Dimension.fillToConstraints
                },
            )

            // Amount Text
            Text(
                text = amount,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.constrainAs(amountText) {
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                },
            )

            // Time Text
            Text(
                text = time,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.constrainAs(timeText) {
                    end.linkTo(parent.end)
                    top.linkTo(amountText.bottom, margin = 2.dp)
                },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    WhoPaidItTheme {
        HomeScreen()
    }
}
