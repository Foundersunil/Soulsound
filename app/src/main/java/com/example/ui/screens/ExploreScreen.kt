package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FrequencyItem
import com.example.ui.components.FrequencyCard
import com.example.ui.theme.BorderOrange
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DarkCard
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.OrangePrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.viewmodel.SortOption

@Composable
fun ExploreScreen(
    frequencies: List<FrequencyItem>,
    selectedCategory: String,
    selectedSortOption: SortOption,
    currentTrack: FrequencyItem?,
    isPlaying: Boolean,
    favoriteIds: Set<String>,
    onCategorySelected: (String) -> Unit,
    onSortSelected: (SortOption) -> Unit,
    onFrequencyClick: (FrequencyItem) -> Unit,
    onPlayClick: (FrequencyItem) -> Unit,
    onFavoriteToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        "All", "Meditation", "Focus", "Sleep", "Relaxation",
        "Energy", "Balance", "Abundance", "Manifestation", "Mindfulness"
    )

    var showSortMenu by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        // Screen Title Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Explore Frequencies",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${frequencies.size} sound and frequency experiences",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            // Sort Selector Button
            Box {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = BorderStroke(1.dp, BorderOrange),
                    modifier = Modifier
                        .clickable { showSortMenu = true }
                        .testTag("sort_menu_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort",
                            tint = OrangePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = selectedSortOption.label,
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    SortOption.values().forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option.label,
                                    color = if (option == selectedSortOption) OrangePrimary else TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = if (option == selectedSortOption) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                onSortSelected(option)
                                showSortMenu = false
                            }
                        )
                    }
                }
            }
        }

        // Horizontal Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                val isSelected = selectedCategory.equals(category, ignoreCase = true)
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) OrangePrimary else DarkCard
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) OrangePrimary else BorderSubtle
                    ),
                    modifier = Modifier
                        .clickable { onCategorySelected(category) }
                        .testTag("filter_chip_$category")
                ) {
                    Text(
                        text = category,
                        color = if (isSelected) Color.Black else TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grid of Frequency Cards
        if (frequencies.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "No Frequencies Found",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Try switching filters to 'All' or exploring another category.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 120.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(frequencies, key = { it.id }) { frequency ->
                    FrequencyCard(
                        frequency = frequency,
                        isCurrentTrack = currentTrack?.id == frequency.id,
                        isPlaying = isPlaying && currentTrack?.id == frequency.id,
                        isFavorite = favoriteIds.contains(frequency.id),
                        onCardClick = { onFrequencyClick(frequency) },
                        onPlayClick = { onPlayClick(frequency) },
                        onFavoriteClick = { onFavoriteToggle(frequency.id) }
                    )
                }
            }
        }
    }
}
