package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CreateChallengeWizard(
    allFrequencies: List<FrequencyItem>,
    onDismiss: () -> Unit,
    onStartChallenge: (
        durationDays: Int,
        goal: String,
        frequency: FrequencyItem,
        dailyTargetMinutes: Int,
        reminderTime: String,
        reminderEnabled: Boolean
    ) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }

    // Form states
    var selectedDuration by remember { mutableIntStateOf(30) }
    var customDaysText by remember { mutableStateOf("21") }
    var isCustomDuration by remember { mutableStateOf(false) }

    var selectedGoal by remember { mutableStateOf("Meditation") }
    var selectedFrequency by remember {
        mutableStateOf(allFrequencies.find { it.hz == 528.0f } ?: allFrequencies.firstOrNull())
    }

    var selectedDailyMinutes by remember { mutableIntStateOf(20) }
    var selectedReminderTime by remember { mutableStateOf("08:00 PM") }
    var reminderEnabled by remember { mutableStateOf(true) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Wizard Navigation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (step > 1) {
                        IconButton(onClick = { step-- }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(48.dp))
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "START A CHALLENGE",
                            color = GoldAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "Step $step of 5",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted
                        )
                    }
                }

                // Step content
                Box(modifier = Modifier.weight(1f)) {
                    when (step) {
                        1 -> StepDuration(
                            selectedDuration = selectedDuration,
                            isCustom = isCustomDuration,
                            customText = customDaysText,
                            onSelectPreset = {
                                selectedDuration = it
                                isCustomDuration = false
                            },
                            onCustomChange = {
                                customDaysText = it
                                isCustomDuration = true
                                val days = it.toIntOrNull()?.coerceIn(3, 3650) ?: 21
                                selectedDuration = days
                            }
                        )
                        2 -> StepGoal(
                            selectedGoal = selectedGoal,
                            onSelectGoal = { goal ->
                                selectedGoal = goal
                                // Automatically pick best matching frequency from library
                                val match = allFrequencies.find {
                                    it.category.equals(goal, true) || it.tags.any { t -> t.equals(goal, true) }
                                } ?: allFrequencies.firstOrNull()
                                if (match != null) {
                                    selectedFrequency = match
                                }
                            }
                        )
                        3 -> StepPractice(
                            selectedGoal = selectedGoal,
                            allFrequencies = allFrequencies,
                            selectedFrequency = selectedFrequency,
                            onSelectFrequency = { selectedFrequency = it }
                        )
                        4 -> StepDailyTarget(
                            selectedMinutes = selectedDailyMinutes,
                            reminderTime = selectedReminderTime,
                            reminderEnabled = reminderEnabled,
                            onSelectMinutes = { selectedDailyMinutes = it },
                            onSelectReminderTime = { selectedReminderTime = it },
                            onToggleReminder = { reminderEnabled = it }
                        )
                        5 -> StepSummary(
                            durationDays = selectedDuration,
                            goal = selectedGoal,
                            frequency = selectedFrequency ?: allFrequencies.first(),
                            dailyMinutes = selectedDailyMinutes,
                            reminderTime = selectedReminderTime,
                            onEditClick = { step = 1 }
                        )
                    }
                }

                // Bottom CTA bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (step < 5) {
                        Button(
                            onClick = { step++ },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OrangePrimary,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("wizard_next_btn")
                        ) {
                            Text(
                                text = "Continue",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                        }
                    } else {
                        Button(
                            onClick = {
                                val freq = selectedFrequency ?: allFrequencies.first()
                                onStartChallenge(
                                    selectedDuration,
                                    selectedGoal,
                                    freq,
                                    selectedDailyMinutes,
                                    selectedReminderTime,
                                    reminderEnabled
                                )
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OrangePrimary,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("wizard_start_challenge_cta")
                        ) {
                            Text(
                                text = "START MY CHALLENGE",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepDuration(
    selectedDuration: Int,
    isCustom: Boolean,
    customText: String,
    onSelectPreset: (Int) -> Unit,
    onCustomChange: (String) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text(
                text = "Choose Challenge Duration",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "How many consecutive days do you want to commit to your practice?",
                color = TextMuted,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(18.dp))

            // Duration Banner preview
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
                border = BorderStroke(1.dp, BorderOrange),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$selectedDuration DAYS",
                        color = OrangePrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "“Your $selectedDuration-day consistency journey starts here.”",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "POPULAR DURATIONS",
                color = GoldAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        val presets = listOf(
            7 to "7 Days (Kickstart)",
            15 to "15 Days (Momentum)",
            30 to "30 Days (Habit Mastery)"
        )

        items(presets) { (days, label) ->
            val isSelected = !isCustom && selectedDuration == days
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFF2B1608) else DarkCard
                ),
                border = BorderStroke(
                    1.dp,
                    if (isSelected) OrangePrimary else BorderSubtle
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clickable { onSelectPreset(days) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = label,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = when (days) {
                                7 -> "Great for beginners to feel initial calm."
                                15 -> "Anchor mindful frequency listening."
                                else -> "Build lasting daily neuroplastic resonance."
                            },
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(OrangePrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "LONG-TERM CHALLENGES",
                color = GoldAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(90, 180, 365).forEach { days ->
                    val isSelected = !isCustom && selectedDuration == days
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFF2B1608) else DarkCard
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) OrangePrimary else BorderSubtle
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectPreset(days) }
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$days Days",
                                color = if (isSelected) OrangePrimary else TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "CUSTOM COMMITMENT",
                color = GoldAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCustom) Color(0xFF2B1608) else DarkCard
                ),
                border = BorderStroke(
                    1.dp,
                    if (isCustom) OrangePrimary else BorderSubtle
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enter Custom Days",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Commit between 3 and 3650 days",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                val current = selectedDuration
                                if (current > 3) {
                                    val next = current - 1
                                    onCustomChange(next.toString())
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", tint = OrangePrimary)
                        }

                        OutlinedTextField(
                            value = if (isCustom) customText else selectedDuration.toString(),
                            onValueChange = { onCustomChange(it) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.width(70.dp)
                        )

                        IconButton(
                            onClick = {
                                val current = selectedDuration
                                if (current < 3650) {
                                    val next = current + 1
                                    onCustomChange(next.toString())
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", tint = OrangePrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepGoal(
    selectedGoal: String,
    onSelectGoal: (String) -> Unit
) {
    val goals = listOf(
        GoalOption("Meditation", "Cultivate quiet awareness, presence, and stillness.", Icons.Default.SelfImprovement),
        GoalOption("Focus", "Deep work, study concentration, and mental shielding.", Icons.Default.Psychology),
        GoalOption("Relaxation", "Unwind physical tension and release somatic stress.", Icons.Default.Spa),
        GoalOption("Sleep", "Nighttime restoration and slow delta sleep induction.", Icons.Default.NightsStay),
        GoalOption("Energy", "Vitality reboot, cellular charge, and alert freshness.", Icons.Default.Bolt),
        GoalOption("Balance", "Harmonious resonance and emotional equilibrium.", Icons.Default.Waves),
        GoalOption("Money", "Abundance mindset, gratitude alignment, and prosperity.", Icons.Default.Star),
        GoalOption("Manifestation", "Intentional visualization and visionary focus.", Icons.Default.Visibility)
    )

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text(
                text = "What Do You Want To Practice?",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Select your core daily intention. Your sound library will adapt to this goal.",
                color = TextMuted,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(18.dp))
        }

        items(goals) { goal ->
            val isSelected = selectedGoal.equals(goal.name, true)
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFF2B1608) else DarkCard
                ),
                border = BorderStroke(
                    1.dp,
                    if (isSelected) OrangePrimary else BorderSubtle
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clickable { onSelectGoal(goal.name) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(if (isSelected) OrangePrimary else Color(0xFF211409), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = goal.icon,
                            contentDescription = null,
                            tint = if (isSelected) Color.Black else OrangePrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = goal.name,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = goal.description,
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = OrangePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

private data class GoalOption(val name: String, val description: String, val icon: ImageVector)

@Composable
private fun StepPractice(
    selectedGoal: String,
    allFrequencies: List<FrequencyItem>,
    selectedFrequency: FrequencyItem?,
    onSelectFrequency: (FrequencyItem) -> Unit
) {
    val relevantFrequencies = allFrequencies.filter { item ->
        item.category.equals(selectedGoal, true) ||
        item.tags.any { it.equals(selectedGoal, true) }
    }.ifEmpty { allFrequencies }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text(
                text = "Choose Your Daily Sound",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tuned for your '$selectedGoal' practice from the SoulSound Library.",
                color = TextMuted,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(18.dp))
        }

        items(relevantFrequencies, key = { it.id }) { freq ->
            val isSelected = selectedFrequency?.id == freq.id
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFF2B1608) else DarkCard
                ),
                border = BorderStroke(
                    1.dp,
                    if (isSelected) OrangePrimary else BorderSubtle
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clickable { onSelectFrequency(freq) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color(0xFF221509), CircleShape)
                            .border(1.dp, if (isSelected) OrangePrimary else BorderOrange, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = freq.displayHz,
                            color = OrangePrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = freq.name,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = freq.shortDesc,
                            color = TextMuted,
                            fontSize = 12.sp,
                            maxLines = 2
                        )
                    }

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = OrangePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepDailyTarget(
    selectedMinutes: Int,
    reminderTime: String,
    reminderEnabled: Boolean,
    onSelectMinutes: (Int) -> Unit,
    onSelectReminderTime: (String) -> Unit,
    onToggleReminder: (Boolean) -> Unit
) {
    val durationOptions = listOf(5, 10, 15, 20, 30, 45, 60)
    val reminderOptions = listOf("07:00 AM", "08:00 AM", "12:00 PM", "06:00 PM", "08:00 PM", "09:30 PM")

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text(
                text = "Daily Practice Target",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Select how many minutes you will listen each day to complete your session.",
                color = TextMuted,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "DAILY LISTENING DURATION",
                color = GoldAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                durationOptions.take(4).forEach { mins ->
                    val isSelected = selectedMinutes == mins
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) OrangePrimary else DarkCard
                        ),
                        border = BorderStroke(1.dp, if (isSelected) OrangePrimary else BorderSubtle),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectMinutes(mins) }
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$mins m",
                                color = if (isSelected) Color.Black else TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                durationOptions.drop(4).forEach { mins ->
                    val isSelected = selectedMinutes == mins
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) OrangePrimary else DarkCard
                        ),
                        border = BorderStroke(1.dp, if (isSelected) OrangePrimary else BorderSubtle),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectMinutes(mins) }
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$mins m",
                                color = if (isSelected) Color.Black else TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Reminder setup
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
                border = BorderStroke(1.dp, BorderOrange),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Alarm, contentDescription = null, tint = OrangePrimary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Daily Practice Reminder",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Consistent timing reinforces daily neuroplastic habits.",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = onToggleReminder,
                            colors = SwitchDefaults.colors(checkedThumbColor = OrangePrimary, checkedTrackColor = Color(0xFF2E1708))
                        )
                    }

                    if (reminderEnabled) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Select Preferred Time:",
                            color = GoldAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            reminderOptions.take(3).forEach { time ->
                                val isSelected = reminderTime == time
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) OrangePrimary else DarkCard
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onSelectReminderTime(time) }
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = time,
                                            color = if (isSelected) Color.Black else TextPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            reminderOptions.drop(3).forEach { time ->
                                val isSelected = reminderTime == time
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) OrangePrimary else DarkCard
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onSelectReminderTime(time) }
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = time,
                                            color = if (isSelected) Color.Black else TextPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepSummary(
    durationDays: Int,
    goal: String,
    frequency: FrequencyItem,
    dailyMinutes: Int,
    reminderTime: String,
    onEditClick: () -> Unit
) {
    val now = System.currentTimeMillis()
    val endMillis = now + (durationDays.toLong() * 24L * 60 * 60 * 1000)
    val startStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(now))
    val endStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(endMillis))

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text(
                text = "Review Your Challenge",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Confirm your daily consistency blueprint before embarking.",
                color = TextMuted,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(18.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
                border = BorderStroke(1.5.dp, BorderOrange),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "YOUR CHALLENGE",
                            color = GoldAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )

                        Text(
                            text = "Edit",
                            color = OrangePrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable(onClick = onEditClick)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "$durationDays-DAY $goal".uppercase(),
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SummaryRow(label = "Daily Sound", value = "${frequency.displayHz} • ${frequency.name}")
                    SummaryRow(label = "Daily Target", value = "$dailyMinutes Minutes / day")
                    SummaryRow(label = "Start Date", value = "Today ($startStr)")
                    SummaryRow(label = "Expected Completion", value = endStr)
                    SummaryRow(label = "Daily Reminder", value = reminderTime)
                    SummaryRow(label = "Recovery Days", value = "1 Protected Missed Day")
                    SummaryRow(label = "Soul Points on Finish", value = "+${durationDays * 10 + 300} Points")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💡",
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Consistency Rule: Complete at least $dailyMinutes minutes of frequency listening each calendar day to maintain your streak.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextMuted, fontSize = 13.sp)
        Text(text = value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}
