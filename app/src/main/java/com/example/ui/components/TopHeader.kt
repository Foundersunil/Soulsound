package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderOrange
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.OrangePrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun TopHeader(
    onSearchClick: () -> Unit,
    onAdminClick: () -> Unit,
    isAdminActive: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Brand and Tagline
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .background(OrangePrimary, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SOULSOUND",
                    color = TextPrimary,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.5.sp
                )
            }
            Text(
                text = "Sound & Frequency Experience",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(start = 17.dp, top = 1.dp)
            )
        }

        // Actions: Search + Admin/Studio
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier
                    .size(42.dp)
                    .testTag("header_search_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = TextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (isAdminActive) OrangePrimary.copy(alpha = 0.2f) else Color(0xFF1E1E1E))
                    .border(
                        1.dp,
                        if (isAdminActive) OrangePrimary else BorderOrange,
                        CircleShape
                    )
                    .clickable(onClick = onAdminClick)
                    .testTag("header_admin_btn"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Studio Settings",
                    tint = if (isAdminActive) OrangePrimary else GoldAccent,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}
