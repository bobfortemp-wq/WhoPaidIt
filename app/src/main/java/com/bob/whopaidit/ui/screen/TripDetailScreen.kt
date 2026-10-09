package com.bob.whopaidit.ui.screen

import android.Manifest
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.bob.whopaidit.data.model.Expense
import com.bob.whopaidit.data.model.SettlementPayment
import com.bob.whopaidit.ui.theme.StatusNegative
import com.bob.whopaidit.ui.theme.StatusPositive
import com.bob.whopaidit.util.SettlementCalculator
import java.util.Calendar
import kotlin.math.abs
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bob.whopaidit.R
import com.bob.whopaidit.ui.component.AppTextField
import com.bob.whopaidit.ui.theme.WhoPaidItTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun fetchCurrentLocation(
    context: Context,
    onLocationFetched: (String) -> Unit,
) {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    val hasFinePermission = ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED
    val hasCoarsePermission = ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED

    if (!hasFinePermission && !hasCoarsePermission) {
        Toast.makeText(context, "Location permission not granted", Toast.LENGTH_SHORT).show()
        onLocationFetched("Current Location")
        return
    }

    try {
        var lastLocation: Location? = null
        if (locationManager != null) {
            val providers = locationManager.getProviders(true)
            for (provider in providers) {
                val loc = locationManager.getLastKnownLocation(provider) ?: continue
                if (lastLocation == null || loc.accuracy < lastLocation.accuracy) {
                    lastLocation = loc
                }
            }
        }

        if (lastLocation != null) {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(lastLocation.latitude, lastLocation.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val addressLine = address.getAddressLine(0)
                if (!addressLine.isNullOrBlank()) {
                    onLocationFetched(addressLine)
                    return
                }
                val parts = listOfNotNull(
                    address.featureName,
                    address.thoroughfare,
                    address.subLocality,
                    address.locality,
                    address.adminArea,
                ).distinct().filter { it.isNotBlank() }
                val fullAddress = parts.joinToString(", ")
                if (fullAddress.isNotBlank()) {
                    onLocationFetched(fullAddress)
                    return
                }
            }
            onLocationFetched("Loc: ${String.format(Locale.US, "%.4f, %.4f", lastLocation.latitude, lastLocation.longitude)}")
        } else {
            onLocationFetched("Current Location")
        }
    } catch (e: Exception) {
        onLocationFetched("Current Location")
    }
}

enum class DetailTab(val label: String) {
    EXPENSES("Expenses"),
    ANALYSIS("Analysis"),
    SETTLEMENTS("Settlements"),
}

enum class ChartType(val label: String) {
    PIE_CHART("Pie Chart"),
    LINE_GRAPH("Line Graph"),
}

val chartColors = listOf(
    Color(0xFF6750A4),
    Color(0xFF006874),
    Color(0xFF984061),
    Color(0xFF7D5260),
    Color(0xFF436533),
    Color(0xFF8B4A00),
    Color(0xFF385CA9),
)

@Composable
fun PieChartCanvas(
    data: Map<String, Double>,
    colors: List<Color>,
    modifier: Modifier = Modifier,
) {
    val totalSum = data.values.sum()
    if (totalSum <= 0) return

    val entries = data.entries.toList()

    Box(
        modifier = modifier.size(180.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            var startAngle = -90f
            entries.forEachIndexed { index, entry ->
                val sweepAngle = ((entry.value / totalSum) * 360f).toFloat()
                val color = colors[index % colors.size]

                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = 32.dp.toPx(), cap = StrokeCap.Round),
                )
                startAngle += sweepAngle
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Total",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "₹${String.format(Locale.US, "%.0f", totalSum)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
fun LineGraphCanvas(
    data: Map<String, Double>,
    colors: List<Color>,
    modifier: Modifier = Modifier,
) {
    val totalSum = data.values.sum()
    if (totalSum <= 0) return

    val entries = data.entries.toList()
    val maxVal = (entries.maxOfOrNull { it.value } ?: 1.0).coerceAtLeast(1.0)
    val primaryColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(vertical = 12.dp, horizontal = 16.dp),
        ) {
            val width = size.width
            val height = size.height
            val spacing = if (entries.size > 1) width / (entries.size - 1) else width / 2

            for (i in 0..3) {
                val y = height - (height * i / 3)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx(),
                )
            }

            val points = entries.mapIndexed { index, entry ->
                val x = if (entries.size > 1) index * spacing else width / 2
                val y = height - ((entry.value / maxVal) * height).toFloat()
                Offset(x, y.coerceIn(0f, height))
            }

            if (points.size > 1) {
                val path = Path().apply {
                    moveTo(points[0].x, points[0].y)
                    for (i in 1 until points.size) {
                        val p1 = points[i - 1]
                        val p2 = points[i]
                        val cx = (p1.x + p2.x) / 2
                        cubicTo(cx, p1.y, cx, p2.y, p2.x, p2.y)
                    }
                }
                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
                )
            }

            points.forEachIndexed { index, pt ->
                val pointColor = colors[index % colors.size]
                drawCircle(
                    color = pointColor,
                    radius = 6.dp.toPx(),
                    center = pt,
                )
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = pt,
                )
            }
        }
    }
}

@Composable
fun ChartLegendList(
    data: Map<String, Double>,
    colors: List<Color>,
    modifier: Modifier = Modifier,
) {
    val totalSum = data.values.sum()
    if (totalSum <= 0) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        data.entries.forEachIndexed { index, entry ->
            val color = colors[index % colors.size]
            val pct = (entry.value / totalSum * 100).toInt()

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(color),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = entry.key,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }

                    Text(
                        text = "₹${String.format(Locale.US, "%.2f", entry.value)} ($pct%)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

enum class SplitMode(val label: String) {
    EQUAL("Equal"),
    EXACT("Exact"),
    PERCENTAGE("% Share"),
    SHARES("Shares"),
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    modifier: Modifier = Modifier,
    tripId: String = "",
    tripTitle: String = "Trip Details",
    membersCount: Int = 1,
    startDate: String = "Start Date",
    endDate: String = "End Date",
    description: String = "",
    currency: String = "INR (₹)",
    participants: List<String> = listOf("You"),
    savedExpenses: List<com.bob.whopaidit.data.local.entity.ExpenseEntity> = emptyList(),
    onBackClick: () -> Unit = {},
    onEditTripClick: (
        id: String,
        title: String,
        description: String,
        currency: String,
        startDate: String,
        endDate: String,
        participants: List<String>,
    ) -> Unit = { _, _, _, _, _, _, _ -> },
    onAddExpenseSubmit: (com.bob.whopaidit.data.local.entity.ExpenseEntity) -> Unit = {},
    onDeleteExpenseSubmit: (String) -> Unit = {},
) {
    val context = LocalContext.current

    var currentTitle by remember(tripTitle) { mutableStateOf(tripTitle) }
    var currentStartDate by remember(startDate) { mutableStateOf(startDate) }
    var currentEndDate by remember(endDate) { mutableStateOf(endDate) }
    var currentDescription by remember(description) { mutableStateOf(description) }
    var currentCurrency by remember(currency) { mutableStateOf(currency) }

    val currentParticipants = remember(participants) {
        mutableStateListOf(*participants.ifEmpty { listOf("You") }.toTypedArray())
    }

    var isHeaderExpanded by remember { mutableStateOf(value = false) }
    var isEditTripSheetOpen by remember { mutableStateOf(value = false) }
    var selectedTab by remember { mutableStateOf(DetailTab.EXPENSES) }
    var selectedChartType by remember { mutableStateOf(ChartType.PIE_CHART) }

    var isAddExpenseDialogOpen by remember { mutableStateOf(value = false) }
    BackHandler(enabled = true) {
        if (isAddExpenseDialogOpen) {
            isAddExpenseDialogOpen = false
        } else if (isEditTripSheetOpen) {
            isEditTripSheetOpen = false
        } else {
            onBackClick()
        }
    }

    val tripExpenses = remember { mutableStateListOf<ExpenseItem>() }

    val displayExpenses = remember(savedExpenses, tripExpenses.toList()) {
        val fromDb = savedExpenses.map { entity ->
            val splitList = entity.splitParticipants.split(",").map { it.trim() }.filter { it.isNotBlank() }
            ExpenseItem(
                id = entity.id,
                title = entity.title,
                subtitle = entity.subtitle.ifEmpty { "Paid by ${entity.paidBy}" },
                amount = if (entity.amount > 0) "₹${String.format(Locale.US, "%.2f", entity.amount)}" else "₹0.00",
                time = entity.dateTime.ifEmpty { "Recently" },
                location = entity.location,
                splitParticipants = splitList,
                rawAmount = entity.amount,
                category = entity.category,
                merchant = entity.merchant,
                notes = entity.notes,
                paidBy = entity.paidBy,
                splitMode = entity.splitMode,
            )
        }
        (fromDb + tripExpenses).distinctBy { it.id }
    }

    val categoryData = remember(displayExpenses) {
        displayExpenses.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.rawAmount } }
            .filter { it.value > 0 }
    }

    val personData = remember(displayExpenses) {
        displayExpenses.groupBy { it.paidBy }
            .mapValues { entry -> entry.value.sumOf { it.rawAmount } }
            .filter { it.value > 0 }
    }

    val participantMap = remember(currentParticipants.toList()) {
        currentParticipants.associateWith { it }
    }

    val calculatedExpenses = remember(displayExpenses) {
        displayExpenses.map { item ->
            Expense(
                id = item.id,
                tourId = tripId,
                title = item.title,
                amount = item.rawAmount,
                paidByUserId = item.paidBy,
                paidByUserName = item.paidBy,
                splitUserIds = item.splitParticipants.ifEmpty { currentParticipants.toList() },
                category = item.category,
            )
        }
    }

    val netBalances = remember(participantMap, calculatedExpenses) {
        SettlementCalculator.calculateNetBalances(
            participantNames = participantMap,
            expenses = calculatedExpenses,
            stages = emptyList(),
        )
    }

    val minimalSettlements = remember(participantMap, netBalances) {
        SettlementCalculator.calculateMinimalSettlements(
            participantNames = participantMap,
            netBalances = netBalances,
        )
    }

    // Add / Edit Expense Form States
    var editingExpenseItem by remember { mutableStateOf<ExpenseItem?>(null) }
    var expenseTitle by remember { mutableStateOf("") }
    var expenseAmount by remember { mutableStateOf("") }
    val categories = listOf("Food", "Transport", "Shopping", "Stays", "Entertainment", "General")
    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var merchant by remember { mutableStateOf("") }

    val currentFormattedDateTime = remember {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
    }
    var dateTimeInput by remember { mutableStateOf(currentFormattedDateTime) }
    var showExpenseDatePicker by remember { mutableStateOf(false) }
    val expenseDatePickerState = rememberDatePickerState()
    var tempSelectedDate by remember { mutableStateOf("") }

    var notes by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (isGranted) {
            Toast.makeText(context, "Fetching location...", Toast.LENGTH_SHORT).show()
            fetchCurrentLocation(context) { fetchedLoc ->
                location = fetchedLoc
                Toast.makeText(context, "Address set: $fetchedLoc", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }
    var selectedPaidBy by remember { mutableStateOf(if (currentParticipants.isNotEmpty()) currentParticipants[0] else "You") }

    var selectedSplitMode by remember { mutableStateOf(SplitMode.EQUAL) }

    val selectedSplitParticipants = remember {
        mutableStateMapOf<String, Boolean>().apply {
            currentParticipants.forEach { this[it] = true }
        }
    }

    val exactAmounts = remember {
        mutableStateMapOf<String, String>().apply {
            currentParticipants.forEach { this[it] = "" }
        }
    }

    val percentages = remember {
        mutableStateMapOf<String, String>().apply {
            currentParticipants.forEach { this[it] = "" }
        }
    }

    val shares = remember {
        mutableStateMapOf<String, String>().apply {
            currentParticipants.forEach { this[it] = "1" }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isAddExpenseDialogOpen = true },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "Add Expense FAB",
                    modifier = Modifier.size(24.dp),
                )
            }
        },
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                // Expandable Top Title Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shadowElevation = 4.dp,
                    tonalElevation = 2.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isHeaderExpanded = !isHeaderExpanded },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f),
                            ) {
                                IconButton(onClick = onBackClick) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_arrow_back),
                                        contentDescription = "Back",
                                        tint = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.size(24.dp),
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Column {
                                    Text(
                                        text = currentTitle,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground,
                                    )
                                    Text(
                                        text = "${currentParticipants.size.coerceAtLeast(membersCount)} Members • ${if (isHeaderExpanded) "Tap to collapse" else "Tap for details"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                IconButton(onClick = { isEditTripSheetOpen = true }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_edit),
                                        contentDescription = "Edit Trip",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }

                                IconButton(onClick = { isHeaderExpanded = !isHeaderExpanded }) {
                                    Icon(
                                        painter = painterResource(
                                            id = if (isHeaderExpanded) R.drawable.ic_chevron_up else R.drawable.ic_chevron_down,
                                        ),
                                        contentDescription = "Toggle Group Details",
                                        tint = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                            }
                        }

                        // Expanded Full Group Details Card
                        AnimatedVisibility(
                            visible = isHeaderExpanded,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically(),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Column {
                                        Text(
                                            text = "Travel Period",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        Text(
                                            text = "$currentStartDate - $currentEndDate",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onBackground,
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Currency",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        Text(
                                            text = currentCurrency,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onBackground,
                                        )
                                    }
                                }

                                if (currentDescription.isNotBlank()) {
                                    Column {
                                        Text(
                                            text = "Description",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        Text(
                                            text = currentDescription,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onBackground,
                                        )
                                    }
                                }

                                Text(
                                    text = "Participants",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    currentParticipants.forEach { member ->
                                        Surface(
                                            shape = RoundedCornerShape(999.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                        ) {
                                            Text(
                                                text = member,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onBackground,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Button(
                                    onClick = { isEditTripSheetOpen = true },
                                    shape = RoundedCornerShape(999.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        contentColor = MaterialTheme.colorScheme.primary,
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(42.dp),
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_edit),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Edit Trip Details",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Bar: Add Expense & Segmented Tabs
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Button(
                        onClick = { isAddExpenseDialogOpen = true },
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                    ) {
                        Text(
                            text = "+ Add Expense",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    // Segmented Filter Tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        DetailTab.entries.forEach { tab ->
                            val isSelected = tab == selectedTab
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedTab = tab },
                                shape = RoundedCornerShape(999.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                ),
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = tab.label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Content List for Selected Tab
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                ) {
                    when (selectedTab) {
                        DetailTab.EXPENSES -> {
                            if (displayExpenses.isEmpty()) {
                                item(key = "empty_expenses") {
                                    EmptyStateCard(
                                        iconRes = R.drawable.ic_wallet,
                                        title = "No Expenses Added Yet",
                                        description = "Tap + Add Expense above or the FAB button to add shared bills",
                                    )
                                }
                            } else {
                                items(
                                    items = displayExpenses,
                                    key = { item -> item.id },
                                ) { expense ->
                                    ExpenseItemRow(
                                        title = expense.title,
                                        subtitle = expense.subtitle,
                                        amount = expense.amount,
                                        time = expense.time,
                                        location = expense.location,
                                        splitParticipants = expense.splitParticipants,
                                        allParticipants = currentParticipants.toList(),
                                        onEditClick = {
                                            editingExpenseItem = expense
                                            expenseTitle = expense.title
                                            expenseAmount = if (expense.rawAmount > 0) String.format(Locale.US, "%.2f", expense.rawAmount) else expense.amount.replace("₹", "").trim()
                                            selectedCategory = if (expense.category in categories) expense.category else categories[0]
                                            merchant = expense.merchant
                                            dateTimeInput = expense.time
                                            location = expense.location
                                            notes = expense.notes
                                            selectedPaidBy = expense.paidBy
                                            selectedSplitMode = when (expense.splitMode) {
                                                "Exact" -> SplitMode.EXACT
                                                "% Share" -> SplitMode.PERCENTAGE
                                                "Shares" -> SplitMode.SHARES
                                                else -> SplitMode.EQUAL
                                            }
                                            selectedSplitParticipants.clear()
                                            currentParticipants.forEach { person ->
                                                selectedSplitParticipants[person] = expense.splitParticipants.isEmpty() || person in expense.splitParticipants
                                            }
                                            isAddExpenseDialogOpen = true
                                        },
                                        onDeleteClick = {
                                            onDeleteExpenseSubmit(expense.id)
                                            tripExpenses.removeAll { it.id == expense.id }
                                            Toast.makeText(context, "'${expense.title}' deleted", Toast.LENGTH_SHORT).show()
                                        },
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }

                        DetailTab.ANALYSIS -> {
                            val hasData = categoryData.isNotEmpty() || personData.isNotEmpty()
                            if (!hasData) {
                                item(key = "empty_analysis") {
                                    EmptyStateCard(
                                        iconRes = R.drawable.ic_activity,
                                        title = "No Expense Data Yet",
                                        description = "Add expenses to view category and person spending charts",
                                    )
                                }
                            } else {
                                item(key = "chart_type_selector") {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        ChartType.entries.forEach { chartType ->
                                            val isSelected = chartType == selectedChartType
                                            Surface(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable { selectedChartType = chartType },
                                                shape = RoundedCornerShape(999.dp),
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                                border = BorderStroke(
                                                    1.dp,
                                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                                ),
                                            ) {
                                                Box(
                                                    modifier = Modifier.padding(vertical = 8.dp),
                                                    contentAlignment = Alignment.Center,
                                                ) {
                                                    Text(
                                                        text = chartType.label,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }

                                if (categoryData.isNotEmpty()) {
                                    item(key = "category_chart") {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(20.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                            ) {
                                                Text(
                                                    text = "Spending by Category",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onBackground,
                                                )

                                                if (selectedChartType == ChartType.PIE_CHART) {
                                                    PieChartCanvas(data = categoryData, colors = chartColors)
                                                } else {
                                                    LineGraphCanvas(data = categoryData, colors = chartColors)
                                                }

                                                ChartLegendList(data = categoryData, colors = chartColors)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }

                                if (personData.isNotEmpty()) {
                                    item(key = "person_chart") {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(20.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                            ) {
                                                Text(
                                                    text = "Amount Spent by Person",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onBackground,
                                                )

                                                if (selectedChartType == ChartType.PIE_CHART) {
                                                    PieChartCanvas(data = personData, colors = chartColors)
                                                } else {
                                                    LineGraphCanvas(data = personData, colors = chartColors)
                                                }

                                                ChartLegendList(data = personData, colors = chartColors)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
                            }
                        }

                        DetailTab.SETTLEMENTS -> {
                            val isAllZero = netBalances.values.all { abs(it) < 0.01 }
                            if (minimalSettlements.isEmpty() && isAllZero) {
                                item(key = "empty_settlements") {
                                    EmptyStateCard(
                                        iconRes = R.drawable.ic_wallet,
                                        title = "All Settled Up!",
                                        description = "No outstanding debts found for this trip",
                                    )
                                }
                            } else {
                                item(key = "balances_header") {
                                    Text(
                                        text = "Net Balances",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.padding(vertical = 4.dp),
                                    )
                                }

                                items(
                                    items = netBalances.entries.toList(),
                                    key = { entry -> entry.key },
                                ) { entry ->
                                    NetBalanceCardRow(memberName = entry.key, balance = entry.value)
                                    Spacer(modifier = Modifier.height(6.dp))
                                }

                                if (minimalSettlements.isNotEmpty()) {
                                    item(key = "settlements_header") {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Suggested Settlements",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            modifier = Modifier.padding(vertical = 4.dp),
                                        )
                                    }

                                    items(
                                        items = minimalSettlements,
                                        key = { payment -> "${payment.fromUserId}_${payment.toUserId}" },
                                    ) { payment ->
                                        SettlementPaymentCardRow(
                                            payment = payment,
                                            onSettleUpClick = {
                                                Toast.makeText(
                                                    context,
                                                    "Settlement of ₹${String.format(Locale.US, "%.2f", payment.amount)} between ${payment.fromUserName} and ${payment.toUserName} marked as completed!",
                                                    Toast.LENGTH_SHORT,
                                                ).show()
                                            },
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Full Add Expense Form Modal Bottom Sheet
            if (isAddExpenseDialogOpen) {
                ModalBottomSheet(
                    onDismissRequest = { isAddExpenseDialogOpen = false },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    containerColor = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        // Header Title & Close Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = if (editingExpenseItem != null) "Edit Expense" else "Add Expense to $tripTitle",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            IconButton(onClick = { isAddExpenseDialogOpen = false }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_delete),
                                    contentDescription = "Close Sheet",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                        AppTextField(
                            value = expenseTitle,
                            onValueChange = { expenseTitle = it },
                            label = "Title *",
                            placeholder = "e.g. Dinner, Grocery, Tolls",
                        )

                        AppTextField(
                            value = expenseAmount,
                            onValueChange = { expenseAmount = it },
                            label = "Amount (₹) *",
                            placeholder = "0.00",
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next,
                            ),
                        )

                        // Category Selector
                        Text(
                            text = "Category",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            categories.forEach { cat ->
                                val isSelected = cat == selectedCategory
                                Surface(
                                    modifier = Modifier.clickable { selectedCategory = cat },
                                    shape = RoundedCornerShape(999.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    ),
                                ) {
                                    Text(
                                        text = cat,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    )
                                }
                            }
                        }

                        AppTextField(
                            value = merchant,
                            onValueChange = { merchant = it },
                            label = "Merchant / Place",
                            placeholder = "e.g. Bistro Cafe",
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showExpenseDatePicker = true },
                        ) {
                            AppTextField(
                                value = dateTimeInput,
                                onValueChange = { dateTimeInput = it },
                                label = "Date & Time",
                                placeholder = "e.g. 12 Oct 2026, 08:30 PM",
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_calendar),
                                        contentDescription = "Select Date & Time",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.clickable { showExpenseDatePicker = true },
                                    )
                                },
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            AppTextField(
                                value = location,
                                onValueChange = { location = it },
                                label = "Location",
                                placeholder = "e.g. Baga Beach, Goa",
                                modifier = Modifier.weight(1f),
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_location),
                                        contentDescription = "Location Icon",
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                },
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = {
                                    val hasFine = ActivityCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                    ) == PackageManager.PERMISSION_GRANTED
                                    val hasCoarse = ActivityCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (hasFine || hasCoarse) {
                                        Toast.makeText(context, "Fetching current location...", Toast.LENGTH_SHORT).show()
                                        fetchCurrentLocation(context) { fetchedLoc ->
                                            location = fetchedLoc
                                            Toast.makeText(context, "Address set: $fetchedLoc", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        locationPermissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                            ),
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .padding(top = 16.dp)
                                    .size(48.dp),
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_location),
                                    contentDescription = "Fetch Current Location",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        }

                        AppTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = "Notes",
                            placeholder = "Shared dinner & drinks bill",
                            singleLine = false,
                        )

                        // Paid By Selector
                        Text(
                            text = "Paid By",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            participants.forEach { person ->
                                val isSelected = person == selectedPaidBy
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedPaidBy = person },
                                    shape = RoundedCornerShape(999.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    ),
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = person,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }

                        // Split Between Header & Mode Selector
                        Text(
                            text = "Split Between",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            SplitMode.entries.forEach { mode ->
                                val isSelected = mode == selectedSplitMode
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedSplitMode = mode },
                                    shape = RoundedCornerShape(999.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    ),
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = mode.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }

                        // Dynamic Split Mode Input Fields
                        when (selectedSplitMode) {
                            SplitMode.EQUAL -> {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    participants.forEach { person ->
                                        val isChecked = selectedSplitParticipants[person] ?: true
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { selectedSplitParticipants[person] = !isChecked }
                                                .padding(vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Checkbox(
                                                checked = isChecked,
                                                onCheckedChange = { checked ->
                                                    selectedSplitParticipants[person] = checked
                                                },
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = MaterialTheme.colorScheme.primary,
                                                    checkmarkColor = MaterialTheme.colorScheme.onPrimary,
                                                ),
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = person,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onBackground,
                                            )
                                        }
                                    }
                                }
                            }

                            SplitMode.EXACT -> {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    participants.forEach { person ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                        ) {
                                            Text(
                                                text = person,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onBackground,
                                                modifier = Modifier.weight(1f),
                                            )
                                            AppTextField(
                                                value = exactAmounts[person] ?: "",
                                                onValueChange = { exactAmounts[person] = it },
                                                placeholder = "₹0.00",
                                                modifier = Modifier.width(120.dp),
                                                keyboardOptions = KeyboardOptions(
                                                    keyboardType = KeyboardType.Number,
                                                    imeAction = ImeAction.Next,
                                                ),
                                            )
                                        }
                                    }
                                }
                            }

                            SplitMode.PERCENTAGE -> {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    participants.forEach { person ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                        ) {
                                            Text(
                                                text = person,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onBackground,
                                                modifier = Modifier.weight(1f),
                                            )
                                            AppTextField(
                                                value = percentages[person] ?: "",
                                                onValueChange = { percentages[person] = it },
                                                placeholder = "0 %",
                                                modifier = Modifier.width(100.dp),
                                                keyboardOptions = KeyboardOptions(
                                                    keyboardType = KeyboardType.Number,
                                                    imeAction = ImeAction.Next,
                                                ),
                                            )
                                        }
                                    }
                                }
                            }

                            SplitMode.SHARES -> {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    participants.forEach { person ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                        ) {
                                            Text(
                                                text = person,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onBackground,
                                                modifier = Modifier.weight(1f),
                                            )
                                            AppTextField(
                                                value = shares[person] ?: "1",
                                                onValueChange = { shares[person] = it },
                                                placeholder = "1 share",
                                                modifier = Modifier.width(100.dp),
                                                keyboardOptions = KeyboardOptions(
                                                    keyboardType = KeyboardType.Number,
                                                    imeAction = ImeAction.Next,
                                                ),
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Submit Primary Button
                        Button(
                            onClick = {
                                if (expenseTitle.isNotBlank() && expenseAmount.isNotBlank()) {
                                    val countSplit = selectedSplitParticipants.values.count { it }
                                    val splitList = selectedSplitParticipants.filter { it.value }.keys.toList()
                                    val splitDescription = when (selectedSplitMode) {
                                        SplitMode.EQUAL -> "Split Equally with $countSplit"
                                        SplitMode.EXACT -> "Split by Exact Amounts"
                                        SplitMode.PERCENTAGE -> "Split by % Shares"
                                        SplitMode.SHARES -> "Split by Share Units"
                                    }
                                    val parsedAmount = expenseAmount.trim().toDoubleOrNull() ?: 0.0
                                    val formattedAmount = "₹${String.format(Locale.US, "%.2f", parsedAmount)}"
                                    val expenseId = editingExpenseItem?.id ?: System.currentTimeMillis().toString()

                                    val newExpenseEntity = com.bob.whopaidit.data.local.entity.ExpenseEntity(
                                        id = expenseId,
                                        tripId = tripId,
                                        title = expenseTitle.trim(),
                                        amount = parsedAmount,
                                        category = selectedCategory,
                                        merchant = merchant.trim(),
                                        dateTime = dateTimeInput.ifEmpty { "Just now" },
                                        location = location.trim(),
                                        notes = notes.trim(),
                                        paidBy = selectedPaidBy,
                                        splitMode = selectedSplitMode.label,
                                        splitParticipants = splitList.joinToString(","),
                                        subtitle = "Paid by $selectedPaidBy • $splitDescription",
                                        createdAt = editingExpenseItem?.id?.toLongOrNull() ?: System.currentTimeMillis(),
                                    )

                                    val isEditing = editingExpenseItem != null
                                    onAddExpenseSubmit(newExpenseEntity)

                                    tripExpenses.removeAll { it.id == expenseId }
                                    tripExpenses.add(
                                        0,
                                        ExpenseItem(
                                            id = newExpenseEntity.id,
                                            title = expenseTitle.trim(),
                                            subtitle = "Paid by $selectedPaidBy • $splitDescription",
                                            amount = formattedAmount,
                                            time = dateTimeInput.ifEmpty { "Just now" },
                                            location = location.trim(),
                                            splitParticipants = splitList,
                                            rawAmount = parsedAmount,
                                            category = selectedCategory,
                                            merchant = merchant.trim(),
                                            notes = notes.trim(),
                                            paidBy = selectedPaidBy,
                                            splitMode = selectedSplitMode.label,
                                        ),
                                    )
                                    editingExpenseItem = null
                                    expenseTitle = ""
                                    expenseAmount = ""
                                    merchant = ""
                                    notes = ""
                                    location = ""
                                    isAddExpenseDialogOpen = false
                                    Toast.makeText(context, if (isEditing) "Expense updated!" else "Expense added successfully!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Please enter title and amount", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(999.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                        ) {
                            Text(
                                text = if (editingExpenseItem != null) "Save Changes" else "Add Expense",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }

            // Expense Date & Time Picker Dialog
            if (showExpenseDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showExpenseDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val selectedMillis = expenseDatePickerState.selectedDateMillis
                                if (selectedMillis != null) {
                                    tempSelectedDate = formatMillisToDate(selectedMillis)
                                    showExpenseDatePicker = false

                                    val cal = Calendar.getInstance()
                                    val timePickerDialog = TimePickerDialog(
                                        context,
                                        { _, hourOfDay, minute ->
                                            val isPm = hourOfDay >= 12
                                            val hour12 = when {
                                                hourOfDay == 0 -> 12
                                                hourOfDay > 12 -> hourOfDay - 12
                                                else -> hourOfDay
                                            }
                                            val formattedTime = String.format(
                                                Locale.getDefault(),
                                                "%02d:%02d %s",
                                                hour12,
                                                minute,
                                                if (isPm) "PM" else "AM",
                                            )
                                            dateTimeInput = "$tempSelectedDate, $formattedTime"
                                            Toast.makeText(context, "Date & Time set: $dateTimeInput", Toast.LENGTH_SHORT).show()
                                        },
                                        cal.get(Calendar.HOUR_OF_DAY),
                                        cal.get(Calendar.MINUTE),
                                        false,
                                    )
                                    timePickerDialog.show()
                                } else {
                                    showExpenseDatePicker = false
                                }
                            },
                        ) {
                            Text("Next", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showExpenseDatePicker = false }) {
                            Text("Cancel")
                        }
                    },
                ) {
                    DatePicker(state = expenseDatePickerState)
                }
            }

            // Edit Trip Modal Bottom Sheet
            if (isEditTripSheetOpen) {
                EditTripBottomSheet(
                    initialTitle = currentTitle,
                    initialStartDate = currentStartDate,
                    initialEndDate = currentEndDate,
                    initialDescription = currentDescription,
                    initialCurrency = currentCurrency,
                    initialParticipants = currentParticipants.toList(),
                    onDismissRequest = { isEditTripSheetOpen = false },
                    onSaveTrip = { newTitle, newDesc, newCurr, newStart, newEnd, newParticipants ->
                        currentTitle = newTitle
                        currentDescription = newDesc
                        currentCurrency = newCurr
                        currentStartDate = newStart
                        currentEndDate = newEnd
                        currentParticipants.clear()
                        currentParticipants.addAll(newParticipants)

                        onEditTripClick(
                            tripId,
                            newTitle,
                            newDesc,
                            newCurr,
                            newStart,
                            newEnd,
                            newParticipants,
                        )

                        isEditTripSheetOpen = false
                        Toast.makeText(context, "Trip details updated!", Toast.LENGTH_SHORT).show()
                    },
                )
            }
        }
    }
}

@Composable
fun NetBalanceCardRow(
    memberName: String,
    balance: Double,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = memberName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )

            val isPositive = balance > 0.01
            val isNegative = balance < -0.01

            val balanceText = when {
                isPositive -> "gets back ₹${String.format(Locale.US, "%.2f", balance)}"
                isNegative -> "owes ₹${String.format(Locale.US, "%.2f", abs(balance))}"
                else -> "Settled up"
            }

            Text(
                text = balanceText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = when {
                    isPositive -> StatusPositive
                    isNegative -> StatusNegative
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

@Composable
fun SettlementPaymentCardRow(
    payment: SettlementPayment,
    onSettleUpClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = payment.fromUserName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = StatusNegative,
                    )
                    Text(
                        text = "  ➔  ",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = payment.toUserName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = StatusPositive,
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Amount: ₹${String.format(Locale.US, "%.2f", payment.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Button(
                onClick = onSettleUpClick,
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(
                    text = "Settle Up",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EditTripBottomSheet(
    initialTitle: String,
    initialStartDate: String,
    initialEndDate: String,
    initialDescription: String,
    initialCurrency: String,
    initialParticipants: List<String>,
    onDismissRequest: () -> Unit = {},
    onSaveTrip: (
        title: String,
        description: String,
        currency: String,
        startDate: String,
        endDate: String,
        participants: List<String>,
    ) -> Unit = { _, _, _, _, _, _ -> },
) {
    val context = LocalContext.current

    var tripTitle by remember { mutableStateOf(initialTitle) }
    var startDate by remember { mutableStateOf(initialStartDate) }
    var endDate by remember { mutableStateOf(initialEndDate) }
    var description by remember { mutableStateOf(initialDescription) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val startDatePickerState = rememberDatePickerState()
    val endDatePickerState = rememberDatePickerState()

    val currencies = listOf("INR (₹)", "EUR (€)", "USD ($)", "GBP (£)")
    var selectedCurrency by remember { mutableStateOf(if (initialCurrency in currencies) initialCurrency else currencies[0]) }

    var participantInput by remember { mutableStateOf("") }
    val participants = remember { mutableStateListOf(*initialParticipants.ifEmpty { listOf("You") }.toTypedArray()) }

    var isTitleError by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header Title & Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Edit Trip Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                IconButton(onClick = onDismissRequest) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_delete),
                        contentDescription = "Close Sheet",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            AppTextField(
                value = tripTitle,
                onValueChange = {
                    tripTitle = it
                    isTitleError = false
                },
                label = "Trip Title *",
                placeholder = "e.g. Goa Roadtrip 2026",
                isError = isTitleError,
                errorMessage = if (isTitleError) "Title is required" else null,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showStartDatePicker = true },
                ) {
                    AppTextField(
                        value = startDate,
                        onValueChange = {},
                        label = "Start Date",
                        placeholder = "Select date",
                        readOnly = true,
                        enabled = false,
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_calendar),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showEndDatePicker = true },
                ) {
                    AppTextField(
                        value = endDate,
                        onValueChange = {},
                        label = "End Date",
                        placeholder = "Select date",
                        readOnly = true,
                        enabled = false,
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_calendar),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                    )
                }
            }

            AppTextField(
                value = description,
                onValueChange = { description = it },
                label = "Description / Notes",
                placeholder = "Kayak tour, beach stay & shared bills",
                singleLine = false,
            )

            // Currency Selector
            Text(
                text = "Billing Currency",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                currencies.forEach { curr ->
                    val isSelected = curr == selectedCurrency
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedCurrency = curr },
                        shape = RoundedCornerShape(999.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        ),
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = curr.split(" ")[0],
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            // Participants
            Text(
                text = "Participants",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppTextField(
                    value = participantInput,
                    onValueChange = { participantInput = it },
                    placeholder = "Enter name",
                    modifier = Modifier.weight(1f),
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (participantInput.isNotBlank()) {
                            participants.add(participantInput.trim())
                            participantInput = ""
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    modifier = Modifier.height(52.dp),
                ) {
                    Text(text = "+ Add", fontWeight = FontWeight.Bold)
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                participants.forEach { person ->
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = person,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                painter = painterResource(id = R.drawable.ic_delete),
                                contentDescription = "Remove",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { participants.remove(person) },
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Submit Button
            Button(
                onClick = {
                    if (tripTitle.isBlank()) {
                        isTitleError = true
                        Toast.makeText(context, "Please enter a trip title", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    onSaveTrip(
                        tripTitle.trim(),
                        description.trim(),
                        selectedCurrency,
                        startDate,
                        endDate,
                        participants.toList(),
                    )
                },
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                Text(
                    text = "Save Changes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Start Date Picker Dialog
        if (showStartDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showStartDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val selectedMillis = startDatePickerState.selectedDateMillis
                            if (selectedMillis != null) {
                                startDate = formatMillisToDate(selectedMillis)
                            }
                            showStartDatePicker = false
                        },
                    ) {
                        Text("OK", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showStartDatePicker = false }) {
                        Text("Cancel")
                    }
                },
            ) {
                DatePicker(state = startDatePickerState)
            }
        }

        // End Date Picker Dialog
        if (showEndDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showEndDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val selectedMillis = endDatePickerState.selectedDateMillis
                            if (selectedMillis != null) {
                                endDate = formatMillisToDate(selectedMillis)
                            }
                            showEndDatePicker = false
                        },
                    ) {
                        Text("OK", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEndDatePicker = false }) {
                        Text("Cancel")
                    }
                },
            ) {
                DatePicker(state = endDatePickerState)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TripDetailScreenPreview() {
    WhoPaidItTheme {
        TripDetailScreen()
    }
}
