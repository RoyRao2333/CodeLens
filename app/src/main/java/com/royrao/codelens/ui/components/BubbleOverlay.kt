package com.royrao.codelens.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.royrao.codelens.utils.ScanParsedResult
import com.royrao.codelens.utils.ScanType

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BubbleOverlay(bubbles: List<ScanParsedResult>, onBubbleClick: (ScanParsedResult) -> Unit) {
    LazyColumn(
        reverseLayout = true,
        modifier =
            Modifier.fillMaxWidth().padding(bottom = 80.dp), // Lift up a bit from very bottom
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Bottom),
    ) {
        items(bubbles, key = { it.rawValue }) { result ->
            // Wrap in Box for animation scope if needed, but LazyItemScope provides
            // animateItemPlacement
            BubbleItem(
                result = result,
                onClick = { onBubbleClick(result) },
                modifier =
                    Modifier.animateItemPlacement(
                        animationSpec =
                            spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessLow,
                            )
                    ),
            )
        }
    }
}

@Composable
fun BubbleItem(result: ScanParsedResult, onClick: () -> Unit, modifier: Modifier = Modifier) {
    // Initial entrance animation state
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { it } + fadeIn(),
        modifier = modifier,
    ) {
        Surface(
            modifier = Modifier.clip(CircleShape).clickable(onClick = onClick),
            color = Color(0xFFE0E0E0).copy(alpha = 0.9f), // Light gray bubble
            contentColor = Color.Black,
            shape = CircleShape,
            shadowElevation = 4.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Icon
                if (result.icon != null) {
                    val bitmap = remember(result.icon) { result.icon.toBitmap().asImageBitmap() }
                    Image(
                        bitmap = bitmap,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                } else {
                    Icon(
                        imageVector =
                            if (result.type == ScanType.TEXT) Icons.Default.Search
                            else Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color.Gray,
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Text
                Text(
                    text = result.label,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                // Show a snippet of content if it's text?
                // Detailed "Open XXX" is usually enough.
            }
        }
    }
}
