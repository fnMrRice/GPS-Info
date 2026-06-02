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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 3D 坐标轴 + 加速度箭头可视化
 * X=红(右), Y=绿(上), Z=蓝(斜前方)
 */
@Composable
fun MotionAxesView(
    x: Float,
    y: Float,
    z: Float,
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
            Canvas(
                modifier = Modifier
                    .size(200.dp)
                    .graphicsLayer {
                        rotationX = 15f
                        rotationY = -25f
                        cameraDistance = 10f * density
                    }
            ) {
                drawAxesWithArrows(x, y, z)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AxisValue("X", x, Color(0xFFEF4444))
                AxisValue("Y", y, Color(0xFF22C55E))
                AxisValue("Z", z, Color(0xFF3B82F6))
            }
        }
    }
}

@Composable
private fun AxisValue(label: String, value: Float, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = "%.2f".format(value),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun DrawScope.drawAxesWithArrows(x: Float, y: Float, z: Float) {
    val cx = size.width / 2
    val cy = size.height / 2
    val axisLen = size.width * 0.35f
    val arrowHead = size.width * 0.06f

    // Z 轴（斜前方）— 蓝色，45度斜向右上
    val zAngle = Math.toRadians(-45.0)
    val zEndX = cx + axisLen * kotlin.math.cos(zAngle).toFloat()
    val zEndY = cy - axisLen * kotlin.math.sin(zAngle).toFloat()
    drawAxisLine(Offset(cx, cy), Offset(zEndX, zEndY), Color(0xFF3B82F6).copy(alpha = 0.3f), 2f)
    drawArrowHead(Offset(zEndX, zEndY), zAngle, arrowHead, Color(0xFF3B82F6).copy(alpha = 0.5f))

    // X 轴（右）— 红色
    val xEnd = Offset(cx + axisLen, cy)
    drawAxisLine(Offset(cx, cy), xEnd, Color(0xFFEF4444).copy(alpha = 0.3f), 2f)
    drawArrowHead(xEnd, 0.0, arrowHead, Color(0xFFEF4444).copy(alpha = 0.5f))

    // Y 轴（上）— 绿色
    val yEnd = Offset(cx, cy - axisLen)
    drawAxisLine(Offset(cx, cy), yEnd, Color(0xFF22C55E).copy(alpha = 0.3f), 2f)
    drawArrowHead(yEnd, Math.toRadians(90.0), arrowHead, Color(0xFF22C55E).copy(alpha = 0.5f))

    // 原点
    drawCircle(Color.White.copy(alpha = 0.5f), radius = 3.dp.toPx(), center = Offset(cx, cy))

    // 数据箭头 — 归一化到轴长度
    val maxVal = 20f
    val normX = (x.coerceIn(-maxVal, maxVal) / maxVal) * axisLen
    val normY = (y.coerceIn(-maxVal, maxVal) / maxVal) * axisLen
    val normZ = (z.coerceIn(-maxVal, maxVal) / maxVal) * axisLen

    // X 数据箭头
    val xDataEnd = Offset(cx + normX, cy)
    drawDataArrow(Offset(cx, cy), xDataEnd, Color(0xFFEF4444))

    // Y 数据箭头
    val yDataEnd = Offset(cx, cy - normY)
    drawDataArrow(Offset(cx, cy), yDataEnd, Color(0xFF22C55E))

    // Z 数据箭头
    val zDataEndX = cx + normZ * kotlin.math.cos(zAngle).toFloat()
    val zDataEndY = cy - normZ * kotlin.math.sin(zAngle).toFloat()
    drawDataArrow(Offset(cx, cy), Offset(zDataEndX, zDataEndY), Color(0xFF3B82F6))
}

private fun DrawScope.drawAxisLine(start: Offset, end: Offset, color: Color, widthDp: Float) {
    drawLine(color, start, end, strokeWidth = widthDp.dp.toPx(), cap = StrokeCap.Round)
}

private fun DrawScope.drawArrowHead(tip: Offset, angle: Double, size: Float, color: Color) {
    val path = Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(
            tip.x - size * kotlin.math.cos(angle - 0.4).toFloat(),
            tip.y + size * kotlin.math.sin(angle - 0.4).toFloat()
        )
        lineTo(
            tip.x - size * kotlin.math.cos(angle + 0.4).toFloat(),
            tip.y + size * kotlin.math.sin(angle + 0.4).toFloat()
        )
        close()
    }
    drawPath(path, color, style = Fill)
}

private fun DrawScope.drawDataArrow(start: Offset, end: Offset, color: Color) {
    drawLine(color, start, end, strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
    val dx = end.x - start.x
    val dy = end.y - start.y
    val len = kotlin.math.sqrt(dx * dx + dy * dy)
    if (len < 1f) return
    val angle = kotlin.math.atan2(dy.toDouble(), dx.toDouble())
    val headSize = 8.dp.toPx()
    drawArrowHead(end, angle, headSize, color)
}
