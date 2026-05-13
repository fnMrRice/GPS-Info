package cn.fnrice.gpsinfo.ui.components

import androidx.compose.animation.core.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight

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
