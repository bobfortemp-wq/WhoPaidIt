package com.bob.whopaidit.ui.screen

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.bob.whopaidit.R
import com.bob.whopaidit.ui.component.AppTextField
import com.bob.whopaidit.ui.theme.StatusNegative
import com.bob.whopaidit.ui.theme.StatusPositive
import com.bob.whopaidit.ui.theme.WhoPaidItTheme
import com.bob.whopaidit.viewModel.HomeViewModel
import java.util.Locale

data class ExpenseItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val amount: String,
    val time: String,
    val location: String = "",
    val splitParticipants: List<String> = emptyList(),
    val rawAmount: Double = 0.0,
    val category: String = "General",
    val merchant: String = "",
    val notes: String = "",
    val paidBy: String = "You",
    val splitMode: String = "Equal",
)

data class TripItem(
    val id: String,
    val title: String,
    val startDate: String,
    val endDate: String,
    val numberOfPeople: Int,
    val totalExpense: String,
    val status: String = "Active",
    val isSettled: Boolean = false,
    val description: String = "",
    val currency: String = "INR (₹)",
    val participants: List<String> = emptyList(),
)

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    userName: String = "User",
    onProfileClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onAddExpenseClick: () -> Unit = {},
    onSettleUpClick: () -> Unit = {},
    onTripClick: (TripItem) -> Unit = {},
    onNewTripClick: () -> Unit = {},
    onJoinTripClick: () -> Unit = {},
    homeViewModel: HomeViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    var isFabExpanded by remember { mutableStateOf(value = false) }
    var isCreateTripSheetOpen by remember { mutableStateOf(value = false) }

    val tripEntities by homeViewModel.trips.collectAsState()

    val displayTrips = tripEntities.map { entity ->
        val memberList = entity.members.split(",").map { it.trim() }.filter { it.isNotBlank() }
        TripItem(
            id = entity.id,
            title = entity.title,
            startDate = entity.startDate,
            endDate = entity.endDate,
            numberOfPeople = entity.memberCount,
            totalExpense = entity.totalExpense,
            status = entity.status,
            isSettled = entity.isSettled,
            description = entity.description,
            currency = entity.currency,
            participants = if (memberList.isNotEmpty()) memberList else listOf("You"),
        )
    }

    var searchQuery by remember { mutableStateOf("") }

    val filteredTrips = remember(displayTrips, searchQuery) {
        if (searchQuery.isBlank()) {
            displayTrips
        } else {
            displayTrips.filter { trip ->
                trip.title.contains(searchQuery, ignoreCase = true) ||
                        trip.description.contains(searchQuery, ignoreCase = true) ||
                        trip.participants.any { it.contains(searchQuery, ignoreCase = true) }
            }
        }
    }

    val totalOverallExpensesSum = remember(displayTrips) {
        displayTrips.sumOf { trip ->
            trip.totalExpense.replace("₹", "").trim().toDoubleOrNull() ?: 0.0
        }
    }
    val formattedOverallTotal = remember(totalOverallExpensesSum) {
        String.format(Locale.US, "₹%.2f", totalOverallExpensesSum)
    }

    val sampleExpenses = remember { mutableStateListOf<ExpenseItem>() }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
        ) {
            // Header & Summary Balance Card
            item(key = "header_section") {
                ConstraintLayout(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    val (
                        profileAvatar,
                        appName,
                        notificationIcon,
                        greetingText,
                        summarySubtitle,
                        searchBar,
                        balanceCard,
                        addExpenseBtn,
                        settleUpBtn,
                        yourTripsHeader,
                    ) = createRefs()

                    // Profile Avatar Icon
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
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
                            top.linkTo(profileAvatar.bottom, margin = 12.dp)
                            start.linkTo(parent.start)
                        },
                    )

                    // Summary Subtitle
                    Text(
                        text = "Here's your expense summary",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.constrainAs(summarySubtitle) {
                            top.linkTo(greetingText.bottom, margin = 2.dp)
                            start.linkTo(parent.start)
                        },
                    )

                    // Group Search Option with Dropdown Suggestions
                    Column(
                        modifier = Modifier.constrainAs(searchBar) {
                            top.linkTo(summarySubtitle.bottom, margin = 14.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            width = Dimension.fillToConstraints
                        },
                    ) {
                        AppTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = "Search groups or members...",
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_search),
                                    contentDescription = "Search Groups",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp),
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_delete),
                                            contentDescription = "Clear Search",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                }
                            },
                        )

                        AnimatedVisibility(
                            visible = searchQuery.isNotBlank() && filteredTrips.isNotEmpty(),
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically(),
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                ) {
                                    filteredTrips.take(5).forEachIndexed { index, trip ->
                                        if (index > 0) {
                                            HorizontalDivider(
                                                color = MaterialTheme.colorScheme.outline.copy(
                                                    alpha = 0.2f
                                                )
                                            )
                                        }
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    onTripClick(trip)
                                                    searchQuery = ""
                                                }
                                                .padding(horizontal = 14.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f),
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            MaterialTheme.colorScheme.primary.copy(
                                                                alpha = 0.15f
                                                            )
                                                        ),
                                                    contentAlignment = Alignment.Center,
                                                ) {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.ic_groups),
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(18.dp),
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(
                                                        text = trip.title,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onBackground,
                                                    )
                                                    Text(
                                                        text = "${trip.numberOfPeople} Members • ${trip.totalExpense}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Balance Summary Card
                    Card(
                        modifier = Modifier.constrainAs(balanceCard) {
                            top.linkTo(searchBar.bottom, margin = 16.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            width = Dimension.fillToConstraints
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
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
                                text = formattedOverallTotal,
                                style = MaterialTheme.typography.headlineLarge,
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
                                text = "₹0.00",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.constrainAs(youAreOwedValue) {
                                    top.linkTo(youAreOwedLabel.bottom, margin = 2.dp)
                                    start.linkTo(parent.start)
                                },
                            )

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
                                text = "₹0.00",
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

                    // Add Expense Button (Pill CTA)
                    Button(
                        onClick = onAddExpenseClick,
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        modifier = Modifier
                            .height(52.dp)
                            .constrainAs(addExpenseBtn) {
                                top.linkTo(balanceCard.bottom, margin = 16.dp)
                                start.linkTo(parent.start)
                                end.linkTo(settleUpBtn.start, margin = 6.dp)
                                width = Dimension.fillToConstraints
                            },
                    ) {
                        Text(
                            text = "+ Add Expense",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    // Settle Up Button (Secondary CTA)
                    Button(
                        onClick = onSettleUpClick,
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .height(52.dp)
                            .constrainAs(settleUpBtn) {
                                top.linkTo(balanceCard.bottom, margin = 16.dp)
                                start.linkTo(addExpenseBtn.end, margin = 6.dp)
                                end.linkTo(parent.end)
                                width = Dimension.fillToConstraints
                            },
                    ) {
                        Text(
                            text = "Settle Up",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    // Active Groups Section Header
                    Text(
                        text = "Active Groups",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.constrainAs(yourTripsHeader) {
                            top.linkTo(addExpenseBtn.bottom, margin = 20.dp)
                            start.linkTo(parent.start)
                        },
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Groups Section: Items or Empty State Placeholder
            if (filteredTrips.isEmpty()) {
                item(key = "empty_trips") {
                    EmptyStateCard(
                        iconRes = if (searchQuery.isNotBlank()) R.drawable.ic_search else R.drawable.ic_groups,
                        title = if (searchQuery.isNotBlank()) "No Groups Found" else "No Active Groups Yet",
                        description = if (searchQuery.isNotBlank()) "No groups match '$searchQuery'" else "Tap the + button below to create or join a group",
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            } else {
                items(
                    items = filteredTrips,
                    key = { trip -> trip.id },
                ) { trip ->
                    TripCardItem(
                        trip = trip,
                        onClick = { onTripClick(trip) },
                        onShareClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Join my trip '${trip.title}' on WhoPaidIt! Total Expense: ${trip.totalExpense}",
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Trip"))
                        },
                        onDeleteClick = {
                            homeViewModel.deleteTrip(trip.id)
                            Toast.makeText(context, "'${trip.title}' deleted", Toast.LENGTH_SHORT)
                                .show()
                        },
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // Recent Expenses Section Header
            item(key = "recent_expenses_header") {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Recent Expenses",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Expenses Section: Items or Empty State Placeholder
            if (sampleExpenses.isEmpty()) {
                item(key = "empty_expenses") {
                    EmptyStateCard(
                        iconRes = R.drawable.ic_wallet,
                        title = "No Expenses Recorded",
                        description = "Expenses added to your trips will appear here",
                    )
                }
            } else {
                items(
                    items = sampleExpenses,
                    key = { item -> item.id },
                ) { item ->
                    ExpenseItemRow(
                        title = item.title,
                        subtitle = item.subtitle,
                        amount = item.amount,
                        time = item.time,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // Floating Action Button Overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AnimatedVisibility(
                visible = isFabExpanded,
                enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
                exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom),
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // Join Trip Action Button
                    ExtendedFloatingActionButton(
                        text = {
                            Text(
                                text = "Join Trip",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_join),
                                contentDescription = "Join Trip",
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                        onClick = {
                            isFabExpanded = false
                            onJoinTripClick()
                            Toast.makeText(context, "Join Trip selected", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(999.dp),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onBackground,
                    )

                    // New Trip Action Button
                    ExtendedFloatingActionButton(
                        text = {
                            Text(
                                text = "New Trip",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_add),
                                contentDescription = "New Trip",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                        onClick = {
                            isFabExpanded = false
                            isCreateTripSheetOpen = true
                        },
                        shape = RoundedCornerShape(999.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }

            // Main Speed Dial Trigger FAB
            FloatingActionButton(
                onClick = { isFabExpanded = !isFabExpanded },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isFabExpanded) R.drawable.ic_delete else R.drawable.ic_add,
                    ),
                    contentDescription = "Toggle Trip Actions",
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }

    // Create New Trip Modal Bottom Sheet
    if (isCreateTripSheetOpen) {
        CreateTripBottomSheet(
            onDismissRequest = { isCreateTripSheetOpen = false },
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
                isCreateTripSheetOpen = false
                Toast.makeText(context, "Trip '${newTour.name}' created!", Toast.LENGTH_SHORT)
                    .show()
            },
        )
    }
}

@Composable
fun EmptyStateCard(
    iconRes: Int,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun TripCardItem(
    trip: TripItem,
    onClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            // Header Row: Group Icon + Title & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_groups),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = trip.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (trip.isSettled) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        StatusPositive.copy(alpha = 0.15f)
                    },
                ) {
                    Text(
                        text = trip.status,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (trip.isSettled) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            StatusPositive
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))

            // Details Row: Dates & Number of People
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Dates",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${trip.startDate} - ${trip.endDate}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Participants",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${trip.numberOfPeople} People",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Total Expense & Icon Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Total Trip Expense",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = trip.totalExpense,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                // Icon-Only Action Buttons: Share & Delete
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Share Icon Button
                    IconButton(
                        onClick = onShareClick,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_share),
                            contentDescription = "Share Trip",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    // Delete Icon Button
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete),
                            contentDescription = "Delete Trip",
                            tint = StatusNegative,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
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
    location: String = "",
    splitParticipants: List<String> = emptyList(),
    allParticipants: List<String> = emptyList(),
    onEditClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_wallet),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = amount,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = time,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    if (onEditClick != null || onDeleteClick != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (onEditClick != null) {
                                IconButton(
                                    onClick = onEditClick,
                                    modifier = Modifier.size(28.dp),
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_edit),
                                        contentDescription = "Edit Expense",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                            if (onDeleteClick != null) {
                                IconButton(
                                    onClick = onDeleteClick,
                                    modifier = Modifier.size(28.dp),
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_delete),
                                        contentDescription = "Delete Expense",
                                        tint = StatusNegative,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            val excludedParticipants =
                if (allParticipants.isNotEmpty() && splitParticipants.isNotEmpty() && splitParticipants.size < allParticipants.size) {
                    allParticipants - splitParticipants.toSet()
                } else {
                    emptyList()
                }

            if (location.isNotBlank() || excludedParticipants.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (location.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_location),
                                contentDescription = "Location",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = location,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    if (excludedParticipants.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StatusNegative.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, StatusNegative.copy(alpha = 0.25f)),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_groups),
                                    contentDescription = "Excluded",
                                    tint = StatusNegative,
                                    modifier = Modifier.size(14.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Excludes: ${excludedParticipants.joinToString(", ")}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = StatusNegative,
                                )
                            }
                        }
                    }
                }
            }
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
