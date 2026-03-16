package com.geostampcamera.ui.template

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.geostampcamera.data.model.StampTemplate
import com.geostampcamera.ui.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateSelectorScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stamp Templates") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(StampTemplate.entries.toList()) { template ->
                TemplateCard(
                    template = template,
                    isSelected = template == settings.stampTemplate,
                    onClick = {
                        viewModel.setStampTemplate(template)
                        onBack()
                    }
                )
            }
        }
    }
}

@Composable
private fun TemplateCard(
    template: StampTemplate,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.surfaceVariant

    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = template.displayName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = getTemplateDescription(template),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            // Preview bar showing approximate stamp layout
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getTemplatePreviewText(template),
                    fontSize = 8.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Selection check mark
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    }
}

// Brief description for each template
private fun getTemplateDescription(template: StampTemplate): String = when (template) {
    StampTemplate.CLASSIC -> "Traditional layout with map on right, text on left"
    StampTemplate.MODERN -> "Clean layout with map on left and bold text"
    StampTemplate.MINIMAL -> "Compact stamp with essential info only"
    StampTemplate.SURVEY -> "Detailed data layout for site documentation"
    StampTemplate.TRAVEL -> "Top banner with centered map for travel photos"
    StampTemplate.PROFESSIONAL -> "Right-aligned text with left map for reports"
    StampTemplate.ELEGANT -> "Sophisticated typography with centered map"
    StampTemplate.DATA_OVERLAY -> "Semi-transparent overlay with full technical data"
    StampTemplate.SIDEBAR -> "Vertical data column along the edge"
    StampTemplate.COMPACT -> "Smallest footprint with key info"
}

// Preview text hint
private fun getTemplatePreviewText(template: StampTemplate): String = when (template) {
    StampTemplate.CLASSIC -> "Lat | Lng | Address | Map"
    StampTemplate.MODERN -> "Map | Location | Weather"
    StampTemplate.MINIMAL -> "Coords | Date"
    StampTemplate.SURVEY -> "All Fields | Map | Notes"
    StampTemplate.TRAVEL -> "Map | City | Weather | Date"
    StampTemplate.PROFESSIONAL -> "Full Data | Map | Report"
    StampTemplate.ELEGANT -> "Location | Map | Date"
    StampTemplate.DATA_OVERLAY -> "Overlaid Tech Data | Map"
    StampTemplate.SIDEBAR -> "Sidebar Data | Map"
    StampTemplate.COMPACT -> "Key Coords | Date"
}
