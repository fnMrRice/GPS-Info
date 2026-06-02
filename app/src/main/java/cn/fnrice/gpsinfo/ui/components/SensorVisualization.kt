package cn.fnrice.gpsinfo.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 3D 手机姿态可视化
 * pitch = 俯仰角 (rotationX), roll = 翻滚角 (rotationZ)
 */
@Composable
fun Phone3DView(
    pitch: Float,
    roll: Float,
    azimuth: Float,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 3D 手机
            Canvas(
                modifier = Modifier
                    .size(width = 120.dp, height = 200.dp)
                    .graphicsLayer {
                        rotationX = pitch
                        rotationZ = roll
                        // cameraDistance 单位是 px，需要转为 dp 再乘 8
                        cameraDistance = 8f * density
                    }
            ) {
                drawPhone()
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 数值显示
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OrientationValue("方位", "%.1f°".format(azimuth))
                OrientationValue("俯仰", "%.1f°".format(pitch))
                OrientationValue("翻滚", "%.1f°".format(roll))
            }
        }
    }
}

@Composable
private fun OrientationValue(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun DrawScope.drawPhone() {
    val w = size.width
    val h = size.height
    val cornerRadius = CornerRadius(w * 0.08f, w * 0.08f)
    val screenPadding = w * 0.06f

    // 手机外壳
    drawRoundRect(
        color = Color(0xFF1A1A2E),
        topLeft = Offset.Zero,
        size = Size(w, h),
        cornerRadius = cornerRadius
    )

    // 屏幕
    drawRoundRect(
        color = Color(0xFF16213E),
        topLeft = Offset(screenPadding, screenPadding),
        size = Size(w - screenPadding * 2, h - screenPadding * 2.5f),
        cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
    )

    // 状态栏图标区域 (顶部小绿点模拟GPS)
    drawCircle(
        color = Color(0xFF4ADE80),
        radius = w * 0.025f,
        center = Offset(w * 0.5f, screenPadding + w * 0.06f)
    )

    // 屏幕中央十字准星
    val cx = w * 0.5f
    val cy = h * 0.45f
    val crossLen = w * 0.12f
    val crossColor = Color(0xFF4ADE80).copy(alpha = 0.6f)

    // 水平线
    drawLine(crossColor, Offset(cx - crossLen, cy), Offset(cx + crossLen, cy), strokeWidth = 1.5f)
    // 垂直线
    drawLine(crossColor, Offset(cx, cy - crossLen), Offset(cx, cy + crossLen), strokeWidth = 1.5f)
    // 中心圆
    drawCircle(crossColor, radius = w * 0.02f, center = Offset(cx, cy), style = Fill)

    // 指北针箭头 (在屏幕上方)
    val arrowY = h * 0.25f
    val arrowLen = w * 0.1f
    val arrowPath = Path().apply {
        moveTo(cx, arrowY - arrowLen)
        lineTo(cx - arrowLen * 0.4f, arrowY)
        lineTo(cx + arrowLen * 0.4f, arrowY)
        close()
    }
    drawPath(arrowPath, Color(0xFFEF4444))

    // 底部横线 (Home indicator)
    drawRoundRect(
        color = Color.White.copy(alpha = 0.3f),
        topLeft = Offset(w * 0.3f, h - screenPadding * 1.5f),
        size = Size(w * 0.4f, 2.dp.toPx()),
        cornerRadius = CornerRadius(1.dp.toPx())
    )
}

/**
 * 运动箭头可视化 — 三轴加速度/力的方向和大小
 */
@Composable
fun MotionArrowsView(
    axes: List<MotionAxisData>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            axes.forEach { axis ->
                MotionArrowItem(axis)
            }
        }
    }
}

data class MotionAxisData(
    val label: String,
    val value: Float,
    val color: Color,
    val unit: String = ""
)

@Composable
private fun MotionArrowItem(data: MotionAxisData) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(modifier = Modifier.size(width = 40.dp, height = 60.dp)) {
            drawMotionArrow(data.value, data.color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = data.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = data.color
        )
        Text(
            text = "%.2f".format(data.value),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun DrawScope.drawMotionArrow(value: Float, color: Color) {
    val w = size.width
    val h = size.height
    val cx = w / 2
    // 箭头长度与数值成正比，限制在画布范围内
    val maxLen = h * 0.4f
    val normalizedLen = (value.coerceIn(-20f, 20f) / 20f) * maxLen
    val arrowTop = h / 2 - normalizedLen
    val arrowBottom = h / 2
    val arrowWidth = w * 0.25f

    // 箭头杆
    drawLine(
        color = color,
        start = Offset(cx, arrowBottom),
        end = Offset(cx, arrowTop),
        strokeWidth = 3.dp.toPx()
    )

    // 箭头头部
    val headSize = w * 0.3f
    val direction = if (value >= 0) -1f else 1f
    val headPath = Path().apply {
        moveTo(cx, arrowTop)
        lineTo(cx - headSize * 0.5f, arrowTop + headSize * direction)
        lineTo(cx + headSize * 0.5f, arrowTop + headSize * direction)
        close()
    }
    drawPath(headPath, color, style = Fill)

    // 原点标记
    drawCircle(
        color = color.copy(alpha = 0.4f),
        radius = 3.dp.toPx(),
        center = Offset(cx, h / 2)
    )
}
