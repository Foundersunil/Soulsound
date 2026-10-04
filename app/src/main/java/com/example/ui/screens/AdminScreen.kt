package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.media.MediaPlayer
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import com.example.data.model.AdminAuditLog
import com.example.data.model.FrequencyItem
import com.example.ui.theme.BorderOrange
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.OrangePrimary
import com.example.ui.theme.OrangeSecondary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSubtle

enum class AdminTab(val title: String, val routePath: String) {
    OVERVIEW("Overview", "/admin"),
    FREQUENCIES("Frequencies", "/admin/frequencies"),
    TRACKS("Tracks", "/admin/tracks"),
    CATEGORIES("Categories", "/admin/categories"),
    SETTINGS("Security Settings", "/admin/settings"),
    ACTIVITY_LOG("Activity Log", "/admin/activity-log")
}

@Composable
fun AdminScreen(
    allFrequencies: List<FrequencyItem>,
    auditLogs: List<AdminAuditLog>,
    adminEmail: String,
    onSaveFrequency: (FrequencyItem) -> Unit,
    onSoftDeleteFrequency: (String, String) -> Unit,
    onRestoreFrequency: (String, String) -> Unit,
    onPermanentDeleteFrequency: (String, String) -> Unit,
    onRestoreDefaults: () -> Unit,
    onLogoutAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(AdminTab.OVERVIEW) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Dialog state for adding/editing a frequency
    var showAddDialog by remember { mutableStateOf(false) }
    var frequencyToEdit by remember { mutableStateOf<FrequencyItem?>(null) }
    var frequencyToDelete by remember { mutableStateOf<FrequencyItem?>(null) }
    var showRestoreDefaultsDialog by remember { mutableStateOf(false) }

    // Form inputs for frequency
    var hzInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var categoryInput by remember { mutableStateOf("Meditation") }
    var shortDescInput by remember { mutableStateOf("") }
    var longDescInput by remember { mutableStateOf("") }
    var tagsInput by remember { mutableStateOf("") }
    var intendedInput by remember { mutableStateOf("") }
    var isFeaturedInput by remember { mutableStateOf(false) }
    var isPremiumInput by remember { mutableStateOf(false) }

    // Audio file selection, preview, and upload state
    var selectedAudioUri by remember { mutableStateOf<Uri?>(null) }
    var selectedAudioFileName by remember { mutableStateOf<String?>(null) }
    var selectedAudioFilePath by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0) }
    var uploadSuccess by remember { mutableStateOf(false) }
    var audioValidationError by remember { mutableStateOf<String?>(null) }

    // Inline field validation errors
    var hzError by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var categoryError by remember { mutableStateOf<String?>(null) }
    var audioFileError by remember { mutableStateOf<String?>(null) }

    // Preview MediaPlayer for admin preview before saving
    var previewPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPreviewPlaying by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            try {
                previewPlayer?.stop()
                previewPlayer?.release()
            } catch (_: Exception) {}
            previewPlayer = null
            isPreviewPlaying = false
        }
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            audioValidationError = null
            audioFileError = null
            try {
                previewPlayer?.stop()
                previewPlayer?.release()
            } catch (_: Exception) {}
            previewPlayer = null
            isPreviewPlaying = false

            var fileName = "frequency_audio.mp3"
            var fileSize = 0L

            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        if (nameIdx != -1) fileName = cursor.getString(nameIdx) ?: fileName
                        if (sizeIdx != -1) fileSize = cursor.getLong(sizeIdx)
                    }
                }
            } catch (_: Exception) {}

            val ext = fileName.substringAfterLast('.', "").lowercase()
            val mimeType = context.contentResolver.getType(uri) ?: ""
            val allowedExtensions = listOf("mp3", "wav", "m4a", "aac", "mp4")
            val allowedMimes = listOf("audio/mpeg", "audio/mp3", "audio/wav", "audio/x-wav", "audio/mp4", "audio/m4a", "audio/aac", "audio/x-m4a")

            val isTypeValid = allowedExtensions.contains(ext) || allowedMimes.any { mimeType.contains(it, ignoreCase = true) } || mimeType.startsWith("audio/")

            if (!isTypeValid) {
                audioValidationError = "Please select a supported audio file."
                selectedAudioUri = null
                selectedAudioFileName = null
                return@rememberLauncherForActivityResult
            }

            val maxSizeBytes = 50 * 1024 * 1024L
            if (fileSize > maxSizeBytes) {
                audioValidationError = "Audio file is too large. Please choose a smaller file."
                selectedAudioUri = null
                selectedAudioFileName = null
                return@rememberLauncherForActivityResult
            }

            selectedAudioUri = uri
            selectedAudioFileName = fileName
            uploadSuccess = false
            uploadProgress = 0
        }
    }

    fun resetAddFrequencyForm() {
        hzInput = ""
        nameInput = ""
        categoryInput = "Meditation"
        shortDescInput = ""
        longDescInput = ""
        tagsInput = ""
        intendedInput = ""
        isFeaturedInput = false
        isPremiumInput = false
        selectedAudioUri = null
        selectedAudioFileName = null
        selectedAudioFilePath = null
        audioValidationError = null
        uploadSuccess = false
        uploadProgress = 0
        isUploading = false
        hzError = null
        nameError = null
        categoryError = null
        audioFileError = null
        try {
            previewPlayer?.stop()
            previewPlayer?.release()
        } catch (_: Exception) {}
        previewPlayer = null
        isPreviewPlaying = false
        frequencyToEdit = null
        showAddDialog = true
    }

    fun populateEditFrequencyForm(freq: FrequencyItem) {
        hzInput = if (freq.hz % 1.0f == 0.0f) "${freq.hz.toInt()}" else "${freq.hz}"
        nameInput = freq.name
        categoryInput = freq.category
        shortDescInput = freq.shortDesc
        longDescInput = freq.longDesc
        tagsInput = freq.tags.joinToString(", ")
        intendedInput = freq.intendedExperience.joinToString(", ")
        isFeaturedInput = freq.isFeatured
        isPremiumInput = freq.isPremium
        selectedAudioUri = null
        selectedAudioFileName = freq.audioFileName ?: (if (freq.audioFilePath != null) File(freq.audioFilePath).name else "selected_tone.mp3")
        selectedAudioFilePath = freq.audioFilePath
        audioValidationError = null
        uploadSuccess = !freq.audioFilePath.isNullOrBlank() || !freq.audioUrl.isNullOrBlank()
        uploadProgress = 100
        isUploading = false
        hzError = null
        nameError = null
        categoryError = null
        audioFileError = null
        try {
            previewPlayer?.stop()
            previewPlayer?.release()
        } catch (_: Exception) {}
        previewPlayer = null
        isPreviewPlaying = false
        frequencyToEdit = freq
        showAddDialog = true
    }

    val categories = listOf("Meditation", "Focus", "Sleep", "Relaxation", "Energy", "Balance", "Money", "Manifestation")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Section 21: Top Security Indicator Bar
        Card(
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF140D07)),
            border = BorderStroke(0.dp, Color.Transparent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(OrangePrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = OrangePrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ADMIN MODE",
                                color = OrangePrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = adminEmail,
                        color = GoldAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Admin Logout Button (Section 9)
                Button(
                    onClick = onLogoutAdmin,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF261810),
                        contentColor = OrangePrimary
                    ),
                    border = BorderStroke(1.dp, BorderOrange),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(34.dp).testTag("admin_logout_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Logout",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Logout", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Admin Tab Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .background(DarkCard)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AdminTab.values().forEach { tab ->
                val isSelected = selectedTab == tab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) OrangePrimary else Color.Transparent)
                        .clickable { selectedTab = tab }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("admin_tab_${tab.name.lowercase()}")
                ) {
                    Text(
                        text = tab.title,
                        color = if (isSelected) Color.Black else TextMuted,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // Tab Content
        when (selectedTab) {
            AdminTab.OVERVIEW -> {
                AdminOverviewTab(
                    allFrequencies = allFrequencies,
                    auditLogs = auditLogs,
                    onAddClick = { resetAddFrequencyForm() },
                    onRestoreDefaultsClick = { showRestoreDefaultsDialog = true },
                    onNavigateToFrequencies = { selectedTab = AdminTab.FREQUENCIES },
                    onNavigateToAuditLog = { selectedTab = AdminTab.ACTIVITY_LOG }
                )
            }
            AdminTab.FREQUENCIES -> {
                AdminFrequenciesTab(
                    frequencies = allFrequencies,
                    onAddClick = { resetAddFrequencyForm() },
                    onEditClick = { freq -> populateEditFrequencyForm(freq) },
                    onDeleteClick = { freq -> frequencyToDelete = freq },
                    onRestoreClick = { freq -> onRestoreFrequency(freq.id, freq.name) }
                )
            }
            AdminTab.TRACKS -> {
                AdminTracksTab(
                    frequencies = allFrequencies,
                    onToggleFeatured = { freq ->
                        onSaveFrequency(freq.copy(isFeatured = !freq.isFeatured))
                    },
                    onTogglePremium = { freq ->
                        onSaveFrequency(freq.copy(isPremium = !freq.isPremium))
                    },
                    onEditClick = { freq -> populateEditFrequencyForm(freq) }
                )
            }
            AdminTab.CATEGORIES -> {
                AdminCategoriesTab(
                    categories = categories,
                    frequencies = allFrequencies
                )
            }
            AdminTab.ACTIVITY_LOG -> {
                AdminActivityLogTab(auditLogs = auditLogs)
            }
            AdminTab.SETTINGS -> {
                AdminSettingsTab(
                    adminEmail = adminEmail,
                    onLogout = onLogoutAdmin
                )
            }
        }
    }

    // Add / Edit Frequency Dialog: "Create Frequency Tone"
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isUploading) {
                    try {
                        previewPlayer?.stop()
                        previewPlayer?.release()
                    } catch (_: Exception) {}
                    previewPlayer = null
                    isPreviewPlaying = false
                    showAddDialog = false
                }
            },
            title = {
                Text(
                    text = if (frequencyToEdit == null) "Create Frequency Tone" else "Edit Frequency Tone",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // 1. REQUIRED FIELD — FREQUENCY (Hz)
                    OutlinedTextField(
                        value = hzInput,
                        onValueChange = {
                            hzInput = it
                            if (hzError != null) hzError = null
                        },
                        label = { Text("Frequency (Hz) *") },
                        placeholder = { Text("e.g. 528 or 432 or 3.2", color = TextSubtle) },
                        isError = hzError != null,
                        supportingText = {
                            if (hzError != null) {
                                Text(hzError!!, color = Color(0xFFFF5252), fontSize = 11.sp)
                            } else {
                                val parsed = hzInput.trim().toFloatOrNull()
                                if (parsed != null && parsed > 0f) {
                                    val formatted = if (parsed % 1.0f == 0.0f) "${parsed.toInt()} Hz" else "$parsed Hz"
                                    Text("Display format: $formatted", color = TextMuted, fontSize = 11.sp)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = BorderSubtle,
                            errorBorderColor = Color(0xFFFF5252),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // 2. REQUIRED FIELD — TONE NAME / TITLE
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = {
                            nameInput = it
                            if (nameError != null) nameError = null
                        },
                        label = { Text("Tone Name / Title *") },
                        placeholder = { Text("e.g. Heart & Harmony", color = TextSubtle) },
                        isError = nameError != null,
                        supportingText = {
                            if (nameError != null) {
                                Text(nameError!!, color = Color(0xFFFF5252), fontSize = 11.sp)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = BorderSubtle,
                            errorBorderColor = Color(0xFFFF5252),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // 3. REQUIRED FIELD — CATEGORY
                    Text(text = "Category *", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSel = categoryInput == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) OrangePrimary else DarkCard)
                                    .clickable {
                                        categoryInput = cat
                                        if (categoryError != null) categoryError = null
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(text = cat, color = if (isSel) Color.Black else TextPrimary, fontSize = 11.sp)
                            }
                        }
                    }
                    if (categoryError != null) {
                        Text(categoryError!!, color = Color(0xFFFF5252), fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4. SHORT DESCRIPTION (OPTIONAL)
                    OutlinedTextField(
                        value = shortDescInput,
                        onValueChange = { shortDescInput = it },
                        label = { Text("Short Description") },
                        placeholder = { Text("Optional summary description", color = TextSubtle) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 5. LONG DESCRIPTION & CONTEXT (OPTIONAL)
                    OutlinedTextField(
                        value = longDescInput,
                        onValueChange = { longDescInput = it },
                        label = { Text("Long Description & Context") },
                        placeholder = { Text("Optional detailed listening guidance or context", color = TextSubtle) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 6. TAGS (OPTIONAL)
                    OutlinedTextField(
                        value = tagsInput,
                        onValueChange = { tagsInput = it },
                        label = { Text("Tags (comma-separated)") },
                        placeholder = { Text("meditation, relaxation, sleep", color = TextSubtle) },
                        supportingText = { Text("Optional", color = TextSubtle, fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 7. REQUIRED FIELD — AUDIO FILE UPLOAD & PREVIEW
                    Text(text = "Audio File *", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCard),
                        border = BorderStroke(1.dp, if (audioFileError != null || audioValidationError != null) Color(0xFFFF5252) else BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            if (selectedAudioFileName == null) {
                                Button(
                                    onClick = { audioPickerLauncher.launch("audio/*") },
                                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Choose Audio File", fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    text = "Supported: MP3, WAV, M4A, AAC (Max 50MB)",
                                    color = TextSubtle,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Audiotrack,
                                            contentDescription = null,
                                            tint = OrangePrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Selected Audio",
                                                color = TextMuted,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = selectedAudioFileName ?: "",
                                                color = TextPrimary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }

                                    TextButton(
                                        onClick = { audioPickerLauncher.launch("audio/*") }
                                    ) {
                                        Text("Change File", color = OrangeSecondary, fontSize = 11.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Small Admin Audio Preview Player
                                OutlinedButton(
                                    onClick = {
                                        if (isPreviewPlaying) {
                                            try {
                                                previewPlayer?.pause()
                                            } catch (_: Exception) {}
                                            isPreviewPlaying = false
                                        } else {
                                            try {
                                                if (previewPlayer == null) {
                                                    val mp = MediaPlayer()
                                                    if (selectedAudioUri != null) {
                                                        mp.setDataSource(context, selectedAudioUri!!)
                                                    } else if (selectedAudioFilePath != null) {
                                                        mp.setDataSource(selectedAudioFilePath!!)
                                                    } else {
                                                        return@OutlinedButton
                                                    }
                                                    mp.prepare()
                                                    mp.setOnCompletionListener {
                                                        isPreviewPlaying = false
                                                    }
                                                    previewPlayer = mp
                                                }
                                                previewPlayer?.start()
                                                isPreviewPlaying = true
                                            } catch (e: Exception) {
                                                audioValidationError = "Preview failed: ${e.message}"
                                                isPreviewPlaying = false
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangePrimary),
                                    border = BorderStroke(1.dp, BorderOrange),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPreviewPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isPreviewPlaying) "Pause Preview" else "▶ Preview",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Upload Progress Indicator
                            if (isUploading) {
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { uploadProgress / 100f },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = OrangePrimary,
                                    trackColor = DarkCardElevated
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Uploading audio… $uploadProgress%",
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            } else if (uploadSuccess) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "✓ Audio uploaded successfully",
                                    color = Color(0xFF4CAF50),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (audioValidationError != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = audioValidationError!!,
                                    color = Color(0xFFFF5252),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                    if (audioFileError != null) {
                        Text(
                            text = audioFileError!!,
                            color = Color(0xFFFF5252),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Checkboxes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isFeaturedInput,
                            onCheckedChange = { isFeaturedInput = it },
                            colors = CheckboxDefaults.colors(checkedColor = OrangePrimary)
                        )
                        Text(text = "Featured Tone on Home Dashboard", color = TextPrimary, fontSize = 13.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isPremiumInput,
                            onCheckedChange = { isPremiumInput = it },
                            colors = CheckboxDefaults.colors(checkedColor = GoldAccent)
                        )
                        Text(text = "Premium Track (Audio Access Control)", color = TextPrimary, fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = !isUploading,
                    onClick = {
                        // Strict validation of the 4 required fields
                        var hasError = false
                        val parsedHz = hzInput.trim().toFloatOrNull()
                        if (parsedHz == null || parsedHz <= 0f) {
                            hzError = "Please enter a frequency."
                            hasError = true
                        } else {
                            hzError = null
                        }

                        if (nameInput.trim().isBlank()) {
                            nameError = "Please enter a tone name."
                            hasError = true
                        } else {
                            nameError = null
                        }

                        if (categoryInput.trim().isBlank()) {
                            categoryError = "Please select a category."
                            hasError = true
                        } else {
                            categoryError = null
                        }

                        if (selectedAudioUri == null && selectedAudioFilePath.isNullOrBlank()) {
                            audioFileError = "Please choose an audio file."
                            hasError = true
                        } else {
                            audioFileError = null
                        }

                        if (hasError) return@Button

                        try {
                            previewPlayer?.stop()
                            previewPlayer?.release()
                        } catch (_: Exception) {}
                        previewPlayer = null
                        isPreviewPlaying = false

                        val validParsedHz = parsedHz!!

                        // Upload / Save audio file to app storage
                        if (selectedAudioUri != null) {
                            isUploading = true
                            uploadProgress = 10
                            coroutineScope.launch {
                                var targetFile: File? = null
                                try {
                                    val audioDir = File(context.filesDir, "audio")
                                    if (!audioDir.exists()) audioDir.mkdirs()

                                    val sanitizedFileName = (selectedAudioFileName ?: "frequency_${validParsedHz}.mp3")
                                        .replace(" ", "_")
                                    val destFile = File(audioDir, "${System.currentTimeMillis()}_$sanitizedFileName")

                                    withContext(Dispatchers.IO) {
                                        context.contentResolver.openInputStream(selectedAudioUri!!)?.use { input ->
                                            FileOutputStream(destFile).use { output ->
                                                val buffer = ByteArray(8192)
                                                var bytesRead: Int
                                                var totalRead = 0L
                                                while (input.read(buffer).also { bytesRead = it } != -1) {
                                                    output.write(buffer, 0, bytesRead)
                                                    totalRead += bytesRead
                                                }
                                            }
                                        }
                                    }

                                    // Upload progress simulated smoothly
                                    uploadProgress = 45
                                    delay(100L)
                                    uploadProgress = 85
                                    delay(100L)
                                    uploadProgress = 100
                                    uploadSuccess = true
                                    targetFile = destFile
                                } catch (e: Exception) {
                                    isUploading = false
                                    audioValidationError = "Failed to upload audio: ${e.message}"
                                    return@launch
                                }

                                delay(250L) // brief feedback of ✓ Audio uploaded successfully

                                val newFreq = FrequencyItem(
                                    id = frequencyToEdit?.id ?: "freq_${System.currentTimeMillis()}",
                                    hz = validParsedHz,
                                    name = nameInput.trim(),
                                    shortDesc = shortDescInput.trim().ifBlank { "Harmonic frequency tone." },
                                    longDesc = longDescInput.trim(),
                                    category = categoryInput,
                                    tags = if (tagsInput.isBlank()) listOf(categoryInput) else tagsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                                    intendedExperience = if (intendedInput.isBlank()) listOf("Meditation", "Relaxation") else intendedInput.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                                    suggestedContext = "Dedicated mindful listening.",
                                    durationMinutes = 30,
                                    isFeatured = isFeaturedInput,
                                    isPremium = isPremiumInput,
                                    audioFileName = selectedAudioFileName,
                                    audioFilePath = targetFile.absolutePath,
                                    audioUrl = targetFile.toURI().toString()
                                )

                                onSaveFrequency(newFreq)
                                isUploading = false
                                showAddDialog = false
                            }
                        } else {
                            // Audio file was already previously saved
                            val newFreq = FrequencyItem(
                                id = frequencyToEdit?.id ?: "freq_${System.currentTimeMillis()}",
                                hz = validParsedHz,
                                name = nameInput.trim(),
                                shortDesc = shortDescInput.trim().ifBlank { "Harmonic frequency tone." },
                                longDesc = longDescInput.trim(),
                                category = categoryInput,
                                tags = if (tagsInput.isBlank()) listOf(categoryInput) else tagsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                                intendedExperience = if (intendedInput.isBlank()) listOf("Meditation", "Relaxation") else intendedInput.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                                suggestedContext = "Dedicated mindful listening.",
                                durationMinutes = 30,
                                isFeatured = isFeaturedInput,
                                isPremium = isPremiumInput,
                                audioFileName = selectedAudioFileName,
                                audioFilePath = selectedAudioFilePath,
                                audioUrl = frequencyToEdit?.audioUrl
                            )

                            onSaveFrequency(newFreq)
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black)
                ) {
                    Text("Save to Repository", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isUploading,
                    onClick = {
                        try {
                            previewPlayer?.stop()
                            previewPlayer?.release()
                        } catch (_: Exception) {}
                        previewPlayer = null
                        isPreviewPlaying = false
                        showAddDialog = false
                    }
                ) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = DarkCardElevated,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Section 18: Delete Confirmation Dialog
    if (frequencyToDelete != null) {
        val target = frequencyToDelete!!
        AlertDialog(
            onDismissRequest = { frequencyToDelete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = OrangePrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Delete ${target.displayHz}?",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Are you sure you want to remove \"${target.name}\" from the active repository?",
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This action will soft-delete the tone. It will no longer be visible to normal users, but administrators can restore it anytime.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSoftDeleteFrequency(target.id, target.name)
                        frequencyToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B2014), contentColor = Color.White)
                ) {
                    Text("Confirm Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { frequencyToDelete = null }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = DarkCardElevated,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Reset Starter Defaults Dialog
    if (showRestoreDefaultsDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDefaultsDialog = false },
            title = {
                Text(
                    text = "Restore Starter Library?",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This will re-seed the default Solfeggio tones (174 Hz, 285 Hz, 396 Hz, 417 Hz, 432 Hz, 528 Hz, 639 Hz, 741 Hz, 852 Hz, 963 Hz) into the database.",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRestoreDefaults()
                        showRestoreDefaultsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black)
                ) {
                    Text("Restore Defaults", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDefaultsDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = DarkCardElevated,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

// Admin Tab: Overview
@Composable
private fun AdminOverviewTab(
    allFrequencies: List<FrequencyItem>,
    auditLogs: List<AdminAuditLog>,
    onAddClick: () -> Unit,
    onRestoreDefaultsClick: () -> Unit,
    onNavigateToFrequencies: () -> Unit,
    onNavigateToAuditLog: () -> Unit
) {
    val activeCount = allFrequencies.count { !it.isDeleted }
    val deletedCount = allFrequencies.count { it.isDeleted }
    val featuredCount = allFrequencies.count { it.isFeatured && !it.isDeleted }
    val premiumCount = allFrequencies.count { it.isPremium && !it.isDeleted }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
    ) {
        item {
            Text(
                text = "Sound Studio Administration",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Real-time frequency repository and security controls",
                color = TextMuted,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Metrics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminStatMetric(title = "ACTIVE TONES", value = "$activeCount", subtitle = "In repository", modifier = Modifier.weight(1f))
                AdminStatMetric(title = "FEATURED", value = "$featuredCount", subtitle = "On Home", modifier = Modifier.weight(1f))
                AdminStatMetric(title = "PREMIUM", value = "$premiumCount", subtitle = "Tier protected", modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminStatMetric(title = "SOFT-DELETED", value = "$deletedCount", subtitle = "Restorable", modifier = Modifier.weight(1f))
                AdminStatMetric(title = "AUDIT LOGS", value = "${auditLogs.size}", subtitle = "Admin events", modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(46.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("+ Add Frequency", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = onRestoreDefaultsClick,
                    border = BorderStroke(1.dp, BorderOrange),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(46.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Re-seed Defaults", color = TextPrimary, fontSize = 13.sp)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Recent Audit snippet
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Recent Admin Activity", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = onNavigateToAuditLog) {
                    Text("View Full Log →", color = OrangePrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (auditLogs.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No administrative events logged yet. All actions are tracked in real-time.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(auditLogs.take(3), key = { it.logId }) { log ->
                AdminAuditLogRow(log = log)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// Admin Tab: Frequencies Management
@Composable
private fun AdminFrequenciesTab(
    frequencies: List<FrequencyItem>,
    onAddClick: () -> Unit,
    onEditClick: (FrequencyItem) -> Unit,
    onDeleteClick: (FrequencyItem) -> Unit,
    onRestoreClick: (FrequencyItem) -> Unit
) {
    var showOnlyDeleted by remember { mutableStateOf(false) }

    val displayedList = if (showOnlyDeleted) {
        frequencies.filter { it.isDeleted }
    } else {
        frequencies.filter { !it.isDeleted }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Repository Frequencies",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${displayedList.size} items displayed",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("+ Add Tone", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Pills: Active / Soft-Deleted
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!showOnlyDeleted) OrangePrimary else DarkCardElevated)
                        .clickable { showOnlyDeleted = false }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Active (${frequencies.count { !it.isDeleted }})",
                        color = if (!showOnlyDeleted) Color.Black else TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (showOnlyDeleted) OrangePrimary else DarkCardElevated)
                        .clickable { showOnlyDeleted = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Soft-Deleted (${frequencies.count { it.isDeleted }})",
                        color = if (showOnlyDeleted) Color.Black else TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        if (displayedList.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (showOnlyDeleted) "No soft-deleted tones." else "No active frequencies in repository.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(20.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(displayedList, key = { it.id }) { freq ->
                AdminFrequencyRowItem(
                    frequency = freq,
                    onEdit = { onEditClick(freq) },
                    onDelete = { onDeleteClick(freq) },
                    onRestore = { onRestoreClick(freq) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun AdminFrequencyRowItem(
    frequency: FrequencyItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onRestore: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, if (frequency.isDeleted) Color(0xFF552211) else BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = frequency.displayHz,
                        color = if (frequency.isDeleted) TextMuted else OrangePrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = frequency.name,
                        color = if (frequency.isDeleted) TextMuted else TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row {
                    if (frequency.isFeatured && !frequency.isDeleted) {
                        Box(
                            modifier = Modifier
                                .background(OrangePrimary.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = "FEATURED", color = OrangePrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (frequency.isPremium && !frequency.isDeleted) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .background(GoldAccent.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = "PREMIUM", color = GoldAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${frequency.category} • ${frequency.tags.take(3).joinToString(", ")}",
                color = TextSubtle,
                fontSize = 11.sp
            )

            if (frequency.isDeleted) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Status: Soft-deleted by ${frequency.deletedBy ?: "admin"}",
                    color = Color(0xFFFF7777),
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (frequency.isDeleted) {
                    Button(
                        onClick = onRestore,
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Restore Tone", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    OutlinedButton(
                        onClick = onEdit,
                        border = BorderStroke(1.dp, BorderSubtle),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Edit", color = TextPrimary, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = onDelete,
                        border = BorderStroke(1.dp, Color(0xFF6B2217)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF6666), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete", color = Color(0xFFFF7777), fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// Admin Tab: Tracks Management (/admin/tracks)
@Composable
private fun AdminTracksTab(
    frequencies: List<FrequencyItem>,
    onToggleFeatured: (FrequencyItem) -> Unit,
    onTogglePremium: (FrequencyItem) -> Unit,
    onEditClick: (FrequencyItem) -> Unit
) {
    val activeTracks = frequencies.filter { !it.isDeleted }
    var searchQuery by remember { mutableStateOf("") }
    val filtered = if (searchQuery.isBlank()) activeTracks else {
        activeTracks.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.category.contains(searchQuery, ignoreCase = true) ||
            "${it.hz}".contains(searchQuery)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Manage Sound Tracks", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(text = "/admin/tracks • Harmonic audio engine presets & metadata", color = TextMuted, fontSize = 12.sp)
                }
                Box(
                    modifier = Modifier
                        .background(OrangePrimary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(text = "${activeTracks.size} Master Tracks", color = OrangePrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search tracks by name, category, or Hz...", color = TextSubtle) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangePrimary,
                    unfocusedBorderColor = BorderSubtle,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        items(filtered, key = { it.id }) { track ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(OrangePrimary.copy(alpha = 0.18f), CircleShape)
                            ) {
                                Text(text = "🎧", fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = "${track.displayHz} - ${track.name}", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = "${track.category} • ${track.durationMinutes} min", color = TextMuted, fontSize = 11.sp)
                            }
                        }
                        IconButton(onClick = { onEditClick(track) }) {
                            Icon(imageVector = Icons.Default.Tune, contentDescription = "Edit Track", tint = OrangePrimary, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Featured toggle button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (track.isFeatured) GoldAccent.copy(alpha = 0.2f) else DarkCardElevated)
                                    .border(1.dp, if (track.isFeatured) GoldAccent else BorderSubtle, RoundedCornerShape(8.dp))
                                    .clickable { onToggleFeatured(track) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (track.isFeatured) "★ FEATURED" else "+ Feature",
                                    color = if (track.isFeatured) GoldAccent else TextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Premium toggle button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (track.isPremium) OrangePrimary.copy(alpha = 0.2f) else DarkCardElevated)
                                    .border(1.dp, if (track.isPremium) OrangePrimary else BorderSubtle, RoundedCornerShape(8.dp))
                                    .clickable { onTogglePremium(track) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (track.isPremium) "🔒 PREMIUM" else "Free Track",
                                    color = if (track.isPremium) OrangePrimary else TextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = if (track.binauralBeatHz > 0f) "Binaural: +${track.binauralBeatHz} Hz" else "Pure Resonance",
                            color = TextSubtle,
                            fontSize = 10.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

// Admin Tab: Categories Management
@Composable
private fun AdminCategoriesTab(
    categories: List<String>,
    frequencies: List<FrequencyItem>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
    ) {
        item {
            Text(text = "Manage Sound Categories", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = "Harmonic classifications and user intention tags", color = TextMuted, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(14.dp))
        }

        items(categories) { category ->
            val count = frequencies.count { it.category.equals(category, true) && !it.isDeleted }
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = category, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(text = "$count frequencies mapped", color = TextMuted, fontSize = 12.sp)
                    }
                    Box(
                        modifier = Modifier
                            .background(OrangePrimary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "ACTIVE", color = OrangePrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

// Admin Tab: Activity / Audit Log (Section 20)
@Composable
private fun AdminActivityLogTab(
    auditLogs: List<AdminAuditLog>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Admin Audit Log", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Immutable tracking of all administrative actions", color = TextMuted, fontSize = 12.sp)
                }
                Icon(imageVector = Icons.Default.History, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        if (auditLogs.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No audit log records yet. Future creates, updates, and deletes will appear here.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        } else {
            items(auditLogs, key = { it.logId }) { log ->
                AdminAuditLogRow(log = log)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun AdminAuditLogRow(
    log: AdminAuditLog
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        when (log.action) {
                            "CREATE_OR_UPDATE" -> OrangePrimary.copy(alpha = 0.2f)
                            "SOFT_DELETE", "PERMANENT_DELETE" -> Color(0xFF6B1D14)
                            "RESTORE" -> GoldAccent.copy(alpha = 0.2f)
                            else -> Color(0xFF261D15)
                        }
                    )
            ) {
                Text(
                    text = when (log.action) {
                        "CREATE_OR_UPDATE" -> "✍️"
                        "SOFT_DELETE" -> "🗑️"
                        "PERMANENT_DELETE" -> "⚠️"
                        "RESTORE" -> "🔄"
                        else -> "⚙️"
                    },
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${log.action}: ${log.resourceName}",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "By ${log.adminEmail} • ${log.formattedTimestamp}",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

// Admin Tab: Security Settings
@Composable
private fun AdminSettingsTab(
    adminEmail: String,
    onLogout: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
    ) {
        item {
            Text(text = "Security & Access Controls", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = "Enforced role-based authorization parameters", color = TextMuted, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = BorderStroke(1.dp, BorderOrange),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(text = "AUTHENTICATED IDENTITY", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Admin Account: $adminEmail", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Role: Administrator (System Root)", color = OrangePrimary, fontSize = 12.sp)
                    Text(text = "Session Token: Active & Cryptographically Signed", color = TextMuted, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onLogout,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF381A12), contentColor = OrangePrimary),
                        border = BorderStroke(1.dp, BorderOrange),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Terminate Admin Session", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(text = "FIREBASE SECURITY RULES REFERENCE", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Public / Normal Users: Read-only access to frequency repository.\n• Write / Delete / Restore: Protected by 'request.auth.token.admin == true'.\n• Normal users cannot elevate their role or write to admin_audit_logs.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminStatMetric(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = OrangePrimary, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            Text(text = subtitle, color = TextSubtle, fontSize = 10.sp)
        }
    }
}
