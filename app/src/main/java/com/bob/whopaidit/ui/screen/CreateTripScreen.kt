package com.bob.whopaidit.ui.screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bob.whopaidit.R
import com.bob.whopaidit.data.model.Tour
import com.bob.whopaidit.ui.component.AppTextField
import com.bob.whopaidit.ui.theme.WhoPaidItTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatMillisToDate(millis: Long?): String {
    if (millis == null) return ""
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CreateTripBottomSheet(
    onDismissRequest: () -> Unit = {},
    onCreateTripSuccess: (Tour, String, String) -> Unit = { _, _, _ -> },
) {
    val context = LocalContext.current

    var tripTitle by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val startDatePickerState = rememberDatePickerState()
    val endDatePickerState = rememberDatePickerState()

    val currencies = listOf("INR (₹)", "EUR (€)", "USD ($)", "GBP (£)")
    var selectedCurrency by remember { mutableStateOf(currencies[0]) }

    var participantInput by remember { mutableStateOf("") }
    val participants = remember { mutableStateListOf<String>() }

    var isTitleError by remember { mutableStateOf(value = false) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() },
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
                    text = "Create New Trip",
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

                    val newTour = Tour(
                        id = System.currentTimeMillis().toString(),
                        name = tripTitle.trim(),
                        description = description.trim(),
                        currency = selectedCurrency,
                        memberIds = participants.toList(),
                        createdAt = System.currentTimeMillis(),
                    )

                    Toast.makeText(context, "Trip '${newTour.name}' created!", Toast.LENGTH_SHORT).show()
                    onCreateTripSuccess(newTour, startDate, endDate)
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
                    text = "Create Trip",
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CreateTripScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onCreateTripSuccess: (Tour, String, String) -> Unit = { _, _, _ -> },
) {
    val context = LocalContext.current

    var tripTitle by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val startDatePickerState = rememberDatePickerState()
    val endDatePickerState = rememberDatePickerState()

    val currencies = listOf("INR (₹)", "EUR (€)", "USD ($)", "GBP (£)")
    var selectedCurrency by remember { mutableStateOf(currencies[0]) }

    var participantInput by remember { mutableStateOf("") }
    val participants = remember { mutableStateListOf<String>() }

    var isTitleError by remember { mutableStateOf(value = false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Button(
                        onClick = {
                            if (tripTitle.isBlank()) {
                                isTitleError = true
                                Toast.makeText(context, "Please enter a trip title", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val newTour = Tour(
                                id = System.currentTimeMillis().toString(),
                                name = tripTitle.trim(),
                                description = description.trim(),
                                currency = selectedCurrency,
                                memberIds = participants.toList(),
                                createdAt = System.currentTimeMillis(),
                            )

                            Toast.makeText(context, "Trip '${newTour.name}' created!", Toast.LENGTH_SHORT).show()
                            onCreateTripSuccess(newTour, startDate, endDate)
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
                            text = "Create Trip",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
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
                    Text(
                        text = "New Trip / Tour",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }

                // Section 1: Trip Key Data
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Text(
                            text = "1. Key Data",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )

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
                    }
                }

                // Section 2: Participants
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Text(
                            text = "2. Participants",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
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
                    }
                }
            }
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
fun CreateTripScreenPreview() {
    WhoPaidItTheme {
        CreateTripBottomSheet()
    }
}
