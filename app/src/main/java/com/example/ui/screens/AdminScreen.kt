package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FrequencyItem
import com.example.ui.theme.BorderOrange
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.OrangePrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSubtle

@Composable
fun AdminScreen(
    allFrequencies: List<FrequencyItem>,
    isAdminMode: Boolean,
    onToggleAdminMode: () -> Unit,
    onSaveFrequency: (FrequencyItem) -> Unit,
    onDeleteFrequency: (String) -> Unit,
    onRestoreDefaults: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    // Form inputs for new frequency
    var hzInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var categoryInput by remember { mutableStateOf("Meditation") }
    var shortDescInput by remember { mutableStateOf("") }
    var longDescInput by remember { mutableStateOf("") }
    var tagsInput by remember { mutableStateOf("") }
    var intendedInput by remember { mutableStateOf("") }
    var isFeaturedInput by remember { mutableStateOf(false) }
    var isPremiumInput by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Title Bar
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Sound Studio Admin",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Manage frequencies, metadata, and sound architecture",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isAdminMode) "Admin" else "Viewer",
                        color = if (isAdminMode) OrangePrimary else TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Switch(
                        checked = isAdminMode,
                        onCheckedChange = { onToggleAdminMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = OrangePrimary,
                            checkedTrackColor = Color(0xFF2A1608)
                        )
                    )
                }
            }
        }

        // Analytics / Library Overview Card
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
                border = BorderStroke(1.dp, BorderOrange),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    AdminStatItem(
                        label = "Total Frequencies",
                        value = "${allFrequencies.size}"
                    )
                    AdminStatItem(
                        label = "Featured Tones",
                        value = "${allFrequencies.count { it.isFeatured }}"
                    )
                    AdminStatItem(
                        label = "Premium Tracks",
                        value = "${allFrequencies.count { it.isPremium }}"
                    )
                }
            }
        }

        // Action Buttons Row
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { showAddDialog = true },
                    enabled = isAdminMode,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangePrimary,
                        contentColor = Color.Black,
                        disabledContainerColor = DarkCard,
                        disabledContentColor = TextSubtle
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("admin_add_freq_btn")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Add Frequency", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = onRestoreDefaults,
                    enabled = isAdminMode,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldAccent),
                    border = BorderStroke(1.dp, if (isAdminMode) BorderOrange else BorderSubtle),
                    modifier = Modifier.height(46.dp)
                ) {
                    Icon(imageVector = Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("Reset Starter", fontSize = 12.sp)
                }
            }
        }

        // Library Listing for Admin Management
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "MANAGE REPOSITORY FREQUENCIES",
                color = GoldAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        items(allFrequencies, key = { it.id }) { freq ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = freq.displayHz,
                                color = OrangePrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = freq.name,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "${freq.category} • ${if (freq.isPremium) "Premium" else "Free"} • ${if (freq.isFeatured) "Featured" else "Standard"}",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    if (isAdminMode) {
                        IconButton(
                            onClick = { onDeleteFrequency(freq.id) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = TextMuted,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Frequency Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "Add Custom Sound Frequency",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    item {
                        OutlinedTextField(
                            value = hzInput,
                            onValueChange = { hzInput = it },
                            label = { Text("Frequency Hz (e.g. 528)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Experience Title (e.g. Heart & Harmony)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = categoryInput,
                            onValueChange = { categoryInput = it },
                            label = { Text("Category (Meditation, Focus, Sleep...)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = shortDescInput,
                            onValueChange = { shortDescInput = it },
                            label = { Text("Short Description") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = longDescInput,
                            onValueChange = { longDescInput = it },
                            label = { Text("Long Description (Non-diagnostic context)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = tagsInput,
                            onValueChange = { tagsInput = it },
                            label = { Text("Tags (comma-separated)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = intendedInput,
                            onValueChange = { intendedInput = it },
                            label = { Text("Intended Experience (comma-separated)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isFeaturedInput,
                                onCheckedChange = { isFeaturedInput = it },
                                colors = CheckboxDefaults.colors(checkedColor = OrangePrimary)
                            )
                            Text("Feature on Home Dashboard", color = TextPrimary, fontSize = 12.sp)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isPremiumInput,
                                onCheckedChange = { isPremiumInput = it },
                                colors = CheckboxDefaults.colors(checkedColor = OrangePrimary)
                            )
                            Text("Mark as Premium Track", color = TextPrimary, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedHz = hzInput.toFloatOrNull() ?: 432f
                        val newFreq = FrequencyItem(
                            id = "custom_${System.currentTimeMillis()}",
                            hz = parsedHz,
                            name = nameInput.ifBlank { "$parsedHz Hz Harmonic" },
                            shortDesc = shortDescInput.ifBlank { "Acoustic frequency exploration for mindful listening." },
                            longDesc = longDescInput.ifBlank { "Intended as an acoustic backdrop to accompany mindful meditation and relaxed attention." },
                            category = categoryInput.ifBlank { "Meditation" },
                            tags = if (tagsInput.isBlank()) listOf(categoryInput) else tagsInput.split(",").map { it.trim() },
                            intendedExperience = if (intendedInput.isBlank()) listOf("Mindful Listening") else intendedInput.split(",").map { it.trim() },
                            suggestedContext = "Comfortable listening session with headphones.",
                            durationMinutes = 30,
                            isFeatured = isFeaturedInput,
                            isPremium = isPremiumInput,
                            binauralBeatHz = 6f,
                            harmonicWarmth = 0.4f
                        )
                        onSaveFrequency(newFreq)
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black)
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = DarkCardElevated
        )
    }
}

@Composable
private fun AdminStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = OrangePrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = label,
            color = TextMuted,
            fontSize = 11.sp
        )
    }
}
