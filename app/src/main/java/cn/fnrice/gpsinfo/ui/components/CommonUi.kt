package cn.fnrice.gpsinfo.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

fun getConstellationColor(name: String): Color {
    return when {
        name.contains("GPS") -> Color(0xFF4CAF50)
        name.contains("GLONASS") -> Color(0xFFFF9800)
        name.contains("Galileo") || name.contains("伽利略") -> Color(0xFF2196F3)
        name.contains("BDS") || name.contains("北斗") -> Color(0xFFF44336)
        name.contains("QZSS") -> Color(0xFF9C27B0)
        name.contains("SBAS") -> Color(0xFF795548)
        name.contains("NavIC") || name.contains("IRNSS") -> Color(0xFF00BCD4)
        else -> Color.Gray
    }
}

@Composable
fun ShimmerPlaceholder(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
    fontWeight: FontWeight? = FontWeight.Medium
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    Text(
        text = text,
        modifier = modifier.graphicsLayer(alpha = alpha),
        style = style,
        fontWeight = fontWeight
    )
}

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    title: String,
    isExpandable: Boolean = false,
    isExpanded: Boolean = false,
    onExpandChange: (Boolean) -> Unit = {},
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    headerExtra: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        )
    ) {
        Column(modifier = Modifier.animateContentSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (isExpandable) Modifier.clickable { onExpandChange(!isExpanded) } else Modifier)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = iconTint
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (isExpandable) {
                        val rotation by animateFloatAsState(
                            targetValue = if (isExpanded) 180f else 0f,
                            label = "rotation"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier
                                .size(18.dp)
                                .graphicsLayer { rotationZ = rotation },
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
                headerExtra()
            }
            
            if (isExpandable) {
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .padding(bottom = 12.dp)
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.padding(bottom = 8.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        content()
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 12.dp)
                ) {
                    content()
                }
            }
        }
    }
}
