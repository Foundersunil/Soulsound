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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
fun SearchScreen(
    query: String,
    results: List<FrequencyItem>,
    currentTrack: FrequencyItem?,
    isPlaying: Boolean,
    onQueryChange: (String) -> Unit,
    onFrequencyClick: (FrequencyItem) -> Unit,
    onPlayClick: (FrequencyItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val quickSuggestions = listOf(
        "528 Hz", "432 Hz", "Deep Meditation", "Focus",
        "Sleep", "Money", "Abundance", "Energy", "Stress Relief"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Search Frequency Library",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search text input
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = {
                Text(
                    text = "Search frequencies, goals, sleep, focus…",
                    color = TextSubtle,
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = OrangePrimary
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = TextMuted
                        )
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkCard,
                unfocusedContainerColor = DarkCard,
                focusedBorderColor = OrangePrimary,
                unfocusedBorderColor = BorderSubtle,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_text_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Quick suggestions row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickSuggestions.forEach { suggestion ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.clickable { onQueryChange(suggestion) }
                ) {
                    Text(
                        text = suggestion,
                        color = TextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (query.isBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 120.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = TextSubtle,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Find Your Frequency",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Search by frequency number (e.g. 528, 432)\nor intended goal (e.g. sleep, focus, abundance)",
                        color = TextMuted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else if (results.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 120.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No Search Results",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Try searching for another frequency, goal or experience.",
                        color = TextMuted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Text(
                text = "${results.size} results for \"$query\"",
                color = GoldAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(results, key = { it.id }) { frequency ->
                    SearchResultRow(
                        frequency = frequency,
                        isCurrent = currentTrack?.id == frequency.id,
                        isPlaying = isPlaying && currentTrack?.id == frequency.id,
                        onClick = { onFrequencyClick(frequency) },
                        onPlayClick = { onPlayClick(frequency) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    frequency: FrequencyItem,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onPlayClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
        border = BorderStroke(1.dp, if (isCurrent) OrangePrimary else BorderOrange),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(46.dp)
                        .background(Color(0xFF261608), CircleShape)
                        .border(1.dp, OrangePrimary.copy(alpha = 0.3f), CircleShape)
                ) {
                    Text(
                        text = frequency.displayHz,
                        color = OrangePrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = frequency.name,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "${frequency.category} • ${frequency.tags.take(3).joinToString(", ")}",
                        color = TextMuted,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                    Text(
                        text = frequency.shortDesc,
                        color = TextSubtle,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }

            IconButton(
                onClick = onPlayClick,
                modifier = Modifier
                    .size(38.dp)
                    .background(if (isPlaying) OrangePrimary else Color(0xFF26190E), CircleShape)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = if (isPlaying) Color.Black else OrangePrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
