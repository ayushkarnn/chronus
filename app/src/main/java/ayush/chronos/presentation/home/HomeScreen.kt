package ayush.chronos.presentation.home

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import ayush.chronos.domain.Reminder
import ayush.chronos.domain.formattedDateTime
import ayush.chronos.util.uploadImageToFirebaseStorage
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val remindersState = viewModel.reminders.collectAsState()
    val reminders = remindersState.value
    val scheduledIds by viewModel.scheduledReminderIds.collectAsState()
    val showDialog = remember { mutableStateOf(false) }
    val editReminder = remember { mutableStateOf<Reminder?>(null) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    val openActionCardId = remember { mutableStateOf<String?>(null) }
    val aiDialogVisible = remember { mutableStateOf(false) }
    val userPrompt = remember { mutableStateOf("") }
    val context = LocalContext.current
    val aiWishState by viewModel.aiResponseState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadScheduledReminderIds()
    }

    Scaffold(
        floatingActionButton = {
            Column {
                FloatingActionButton(onClick = {
                    editReminder.value = null; showDialog.value = true
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Reminder")
                }
                Spacer(Modifier.height(22.dp))
                FloatingActionButton(onClick = {
                    aiDialogVisible.value = true
                    userPrompt.value = ""
                    viewModel.resetAiResponseState()
                }) {
                    Icon(Icons.Outlined.Star, contentDescription = "Ai")
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (reminders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Reminder Found",
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = innerPadding,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(reminders, key = { it.id }) { reminder ->
                        Column {
                            ReminderCard(
                                reminder = reminder,
                                modifier = Modifier.clickable {
                                    openActionCardId.value =
                                        if (openActionCardId.value == reminder.id) null else reminder.id
                                }
                            )
                            if (reminder.id in scheduledIds && reminder.dateTime > System.currentTimeMillis()) {
                                val timeStr = remember(reminder.dateTime) {
                                    SimpleDateFormat("MMM d, yyyy h:mm a").format(
                                        Date(reminder.dateTime)
                                    )
                                }
                                Text(
                                    text = "Notification will be sent: $timeStr",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = 24.dp, bottom = 2.dp)
                                )
                            }
                            AnimatedVisibility(
                                visible = openActionCardId.value == reminder.id,
                                enter = androidx.compose.animation.slideInVertically(),
                                exit = androidx.compose.animation.slideOutVertically()
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        Button(onClick = {
                                            editReminder.value = reminder
                                            showDialog.value = true
                                            openActionCardId.value = null
                                        }) {
                                            Text("Edit")
                                        }
                                        Button(
                                            onClick = {
                                                viewModel.deleteReminder(reminder.id)
                                                openActionCardId.value = null
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                                        ) {
                                            Text(
                                                "Delete",
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (showDialog.value) {
                AddReminderDialog(
                    onSave = { reminder ->
                        if (editReminder.value == null) {
                            viewModel.addReminder(reminder)
                        } else {
                            viewModel.updateReminder(reminder)
                        }
                        showDialog.value = false
                        editReminder.value = null
                    },
                    onCancel = {
                        showDialog.value = false
                        editReminder.value = null
                    },
                    userId = userId,
                    initialReminder = editReminder.value
                )
            }
            if (aiDialogVisible.value) {
                AiWishDialog(
                    aiResponseState = aiWishState,
                    onFetchWish = {
                        viewModel.fetchAiResponse(userPrompt.value.trim().replace(' ', '+'))
                    },
                    onDismiss = {
                        aiDialogVisible.value = false
                        viewModel.resetAiResponseState()
                    },
                    userPrompt = userPrompt,
                    onShare = { wish ->
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, wish)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    }
                )
            }
        }
    }
}

@Composable
fun ReminderCard(reminder: Reminder, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!reminder.imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = reminder.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.Gray),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(reminder.title, style = MaterialTheme.typography.titleLarge)
                Text(
                    reminder.formattedDateTime(),
                    style = MaterialTheme.typography.bodyMedium
                )
                if (!reminder.notes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(reminder.notes, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderDialog(
    onSave: (Reminder) -> Unit,
    onCancel: () -> Unit,
    userId: String,
    initialReminder: Reminder? = null
) {
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf(initialReminder?.title ?: "") }
    var notes by remember { mutableStateOf(initialReminder?.notes ?: "") }
    var imageUrl by remember { mutableStateOf(initialReminder?.imageUrl) }
    var dateTime by remember {
        mutableStateOf(
            initialReminder?.dateTime ?: System.currentTimeMillis()
        )
    }
    var loading by remember { mutableStateOf(false) }

    val imagePickerLauncher =
        androidx.activity.compose.rememberLauncherForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                loading = true
                scope.launch {
                    val url = uploadImageToFirebaseStorage(uri, userId)
                    imageUrl = url
                    loading = false
                }
            }
        }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val formattedDate = remember(dateTime) {
        SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(Date(dateTime))
    }
    val formattedTime = remember(dateTime) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(dateTime))
    }

    AlertDialog(
        onDismissRequest = onCancel,
        confirmButton = {
            Button(
                onClick = {
                    val reminder = Reminder(
                        id = initialReminder?.id ?: UUID.randomUUID().toString(),
                        userId = userId,
                        title = title,
                        dateTime = dateTime,
                        notes = notes,
                        imageUrl = imageUrl
                    )
                    onSave(reminder)
                },
                enabled = title.isNotBlank() && !loading && userId.isNotBlank()
            ) { Text(if (initialReminder == null) "Save" else "Update") }
        },
        dismissButton = {
            OutlinedButton(onClick = onCancel) {
                Text("Cancel")
            }
        },
        title = { Text(if (initialReminder == null) "Add Reminder" else "Edit Reminder") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.Center) {
                    if (imageUrl != null) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Surface(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                                .clickable { imagePickerLauncher.launch("image/*") },
                            color = Color.Transparent
                        ) {
                            Box(
                                Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }
                    if (loading) CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title*") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = { showDatePicker = true }) {
                        Text(formattedDate)
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = { showTimePicker = true }) {
                        Text(formattedTime)
                    }
                }
                if (userId.isBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "User not authenticated. Please login again.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            initialDateMillis = dateTime,
            onDismissRequest = { showDatePicker = false },
            onDateChange = { year, month, day ->
                val cal = Calendar.getInstance().apply { timeInMillis = dateTime }
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.DAY_OF_MONTH, day)
                dateTime = cal.timeInMillis
                showDatePicker = false
            }
        )
    }
    if (showTimePicker) {
        TimePickerDialog(
            initialTimeMillis = dateTime,
            onDismissRequest = { showTimePicker = false },
            onTimeChange = { hour, minute ->
                val cal = Calendar.getInstance().apply { timeInMillis = dateTime }
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                dateTime = cal.timeInMillis
                showTimePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    initialDateMillis: Long,
    onDismissRequest: () -> Unit,
    onDateChange: (year: Int, month: Int, day: Int) -> Unit
) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp,
            modifier = Modifier.widthIn(max = 340.dp)
        ) {
            Column(
                Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Select Date",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                DatePicker(
                    state = datePickerState,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val cal = Calendar.getInstance().apply { timeInMillis = millis }
                                onDateChange(
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                )
                            }
                            onDismissRequest()
                        },
                        enabled = datePickerState.selectedDateMillis != null
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialTimeMillis: Long,
    onDismissRequest: () -> Unit,
    onTimeChange: (hour: Int, minute: Int) -> Unit
) {
    val cal = remember { Calendar.getInstance().apply { timeInMillis = initialTimeMillis } }
    val timePickerState = rememberTimePickerState(
        initialHour = cal.get(Calendar.HOUR_OF_DAY),
        initialMinute = cal.get(Calendar.MINUTE),
        is24Hour = true
    )
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(onClick = {
                onTimeChange(timePickerState.hour, timePickerState.minute)
                onDismissRequest()
            }) { Text("OK") }
        },
        title = { Text("Select Time") },
        text = {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TimePicker(state = timePickerState)
            }
        }
    )
}

@Composable
fun AiWishDialog(
    aiResponseState: AiResponseState,
    onFetchWish: () -> Unit,
    onDismiss: () -> Unit,
    userPrompt: MutableState<String>,
    onShare: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            if (aiResponseState is AiResponseState.Idle || aiResponseState is AiResponseState.Error) {
                Button(
                    onClick = onFetchWish,
                    enabled = userPrompt.value.trim().isNotBlank()
                ) {
                    Text("Generate Ai Response")
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text("AI Greeting Generator") },
        text = {
            Column {
                if (aiResponseState is AiResponseState.Loading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generating greeting...")
                    }
                } else if (aiResponseState is AiResponseState.Success) {
                    Text(aiResponseState.wish)
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = { onShare(aiResponseState.wish) }) {
                        Text("Share")
                    }
                } else if (aiResponseState is AiResponseState.Error) {
                    Text("Error: ${aiResponseState.message}", color = MaterialTheme.colorScheme.error)
                }
                if (aiResponseState is AiResponseState.Idle || aiResponseState is AiResponseState.Error) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = userPrompt.value,
                        onValueChange = { userPrompt.value = it },
                        label = { Text("Prompt for AI wish birthday to ayush") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    )
}
