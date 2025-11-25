package com.hsl.wear.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hsl.wear.ui.theme.HslBlue

// Transport mode colors
val BusColor = Color(0xFF007AC9)      // HSL Blue
val TramColor = Color(0xFF00C853)     // Green
val MetroColor = Color(0xFFFF6F00)    // Orange
val TrainColor = Color(0xFF7B1FA2)    // Purple
val FerryColor = Color(0xFF0091EA)    // Light Blue
val WalkColor = Color(0xFF9E9E9E)     // Gray

@Composable
fun TransportModeIcon(
    mode: String,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp
) {
    val color = when (mode.uppercase()) {
        "BUS" -> BusColor
        "TRAM" -> TramColor
        "SUBWAY" -> MetroColor
        "RAIL" -> TrainColor
        "FERRY" -> FerryColor
        "WALK" -> WalkColor
        else -> HslBlue
    }

    Canvas(modifier = modifier.size(size)) {
        when (mode.uppercase()) {
            "BUS" -> drawBusIcon(color)
            "TRAM" -> drawTramIcon(color)
            "SUBWAY" -> drawMetroIcon(color)
            "RAIL" -> drawTrainIcon(color)
            "FERRY" -> drawFerryIcon(color)
            "WALK" -> drawWalkIcon(color)
            else -> drawBusIcon(color) // Default fallback
        }
    }
}

private fun DrawScope.drawBusIcon(color: Color) {
    // Simple bus shape - rounded rectangle with windows
    val width = size.width
    val height = size.height

    // Bus body
    drawRoundRect(
        color = color,
        topLeft = Offset(width * 0.15f, height * 0.2f),
        size = Size(width * 0.7f, height * 0.6f),
        cornerRadius = CornerRadius(width * 0.1f, height * 0.1f)
    )

    // Windows (two rectangles)
    drawRect(
        color = Color.Black,
        topLeft = Offset(width * 0.25f, height * 0.3f),
        size = Size(width * 0.2f, height * 0.25f)
    )
    drawRect(
        color = Color.Black,
        topLeft = Offset(width * 0.55f, height * 0.3f),
        size = Size(width * 0.2f, height * 0.25f)
    )

    // Wheels
    drawCircle(
        color = color,
        radius = width * 0.08f,
        center = Offset(width * 0.3f, height * 0.85f)
    )
    drawCircle(
        color = color,
        radius = width * 0.08f,
        center = Offset(width * 0.7f, height * 0.85f)
    )
}

private fun DrawScope.drawTramIcon(color: Color) {
    // Tram shape - trapezoid with pantograph line
    val width = size.width
    val height = size.height

    // Pantograph line on top
    drawLine(
        color = color,
        start = Offset(width * 0.5f, height * 0.1f),
        end = Offset(width * 0.5f, height * 0.25f),
        strokeWidth = width * 0.05f
    )

    // Tram body (trapezoid)
    val path = Path().apply {
        moveTo(width * 0.25f, height * 0.25f)  // Top left
        lineTo(width * 0.75f, height * 0.25f)  // Top right
        lineTo(width * 0.85f, height * 0.7f)   // Bottom right
        lineTo(width * 0.15f, height * 0.7f)   // Bottom left
        close()
    }
    drawPath(path = path, color = color)

    // Windows
    drawRect(
        color = Color.Black,
        topLeft = Offset(width * 0.35f, height * 0.35f),
        size = Size(width * 0.3f, height * 0.2f)
    )

    // Wheels
    drawCircle(
        color = color,
        radius = width * 0.08f,
        center = Offset(width * 0.35f, height * 0.8f)
    )
    drawCircle(
        color = color,
        radius = width * 0.08f,
        center = Offset(width * 0.65f, height * 0.8f)
    )
}

private fun DrawScope.drawMetroIcon(color: Color) {
    // Metro - circle with "M" letter
    val width = size.width
    val height = size.height

    // Circle background
    drawCircle(
        color = color,
        radius = width * 0.4f,
        center = Offset(width * 0.5f, height * 0.5f)
    )

    // Letter "M" using lines
    val mWidth = width * 0.4f
    val mHeight = height * 0.35f
    val centerX = width * 0.5f
    val centerY = height * 0.5f

    // Left vertical line
    drawLine(
        color = Color.White,
        start = Offset(centerX - mWidth * 0.5f, centerY - mHeight * 0.5f),
        end = Offset(centerX - mWidth * 0.5f, centerY + mHeight * 0.5f),
        strokeWidth = width * 0.08f
    )

    // Left diagonal
    drawLine(
        color = Color.White,
        start = Offset(centerX - mWidth * 0.5f, centerY - mHeight * 0.5f),
        end = Offset(centerX, centerY),
        strokeWidth = width * 0.08f
    )

    // Right diagonal
    drawLine(
        color = Color.White,
        start = Offset(centerX, centerY),
        end = Offset(centerX + mWidth * 0.5f, centerY - mHeight * 0.5f),
        strokeWidth = width * 0.08f
    )

    // Right vertical line
    drawLine(
        color = Color.White,
        start = Offset(centerX + mWidth * 0.5f, centerY - mHeight * 0.5f),
        end = Offset(centerX + mWidth * 0.5f, centerY + mHeight * 0.5f),
        strokeWidth = width * 0.08f
    )
}

private fun DrawScope.drawTrainIcon(color: Color) {
    // Train - longer rectangle with multiple windows
    val width = size.width
    val height = size.height

    // Train body
    drawRoundRect(
        color = color,
        topLeft = Offset(width * 0.1f, height * 0.2f),
        size = Size(width * 0.8f, height * 0.6f),
        cornerRadius = CornerRadius(width * 0.08f, height * 0.08f)
    )

    // Three windows
    drawRect(
        color = Color.Black,
        topLeft = Offset(width * 0.15f, height * 0.3f),
        size = Size(width * 0.18f, height * 0.25f)
    )
    drawRect(
        color = Color.Black,
        topLeft = Offset(width * 0.41f, height * 0.3f),
        size = Size(width * 0.18f, height * 0.25f)
    )
    drawRect(
        color = Color.Black,
        topLeft = Offset(width * 0.67f, height * 0.3f),
        size = Size(width * 0.18f, height * 0.25f)
    )

    // Wheels
    drawCircle(
        color = color,
        radius = width * 0.08f,
        center = Offset(width * 0.25f, height * 0.85f)
    )
    drawCircle(
        color = color,
        radius = width * 0.08f,
        center = Offset(width * 0.5f, height * 0.85f)
    )
    drawCircle(
        color = color,
        radius = width * 0.08f,
        center = Offset(width * 0.75f, height * 0.85f)
    )
}

private fun DrawScope.drawFerryIcon(color: Color) {
    // Ferry - boat shape with waves
    val width = size.width
    val height = size.height

    // Boat hull (trapezoid)
    val path = Path().apply {
        moveTo(width * 0.2f, height * 0.5f)   // Top left
        lineTo(width * 0.8f, height * 0.5f)   // Top right
        lineTo(width * 0.7f, height * 0.7f)   // Bottom right
        lineTo(width * 0.3f, height * 0.7f)   // Bottom left
        close()
    }
    drawPath(path = path, color = color)

    // Cabin (rectangle on top)
    drawRect(
        color = color,
        topLeft = Offset(width * 0.4f, height * 0.3f),
        size = Size(width * 0.2f, height * 0.2f)
    )

    // Waves (curved lines)
    val wavePath = Path().apply {
        moveTo(width * 0.1f, height * 0.8f)
        cubicTo(
            width * 0.2f, height * 0.75f,
            width * 0.3f, height * 0.85f,
            width * 0.4f, height * 0.8f
        )
        moveTo(width * 0.5f, height * 0.85f)
        cubicTo(
            width * 0.6f, height * 0.8f,
            width * 0.7f, height * 0.9f,
            width * 0.8f, height * 0.85f
        )
    }
    drawPath(
        path = wavePath,
        color = color,
        style = Stroke(width = width * 0.04f)
    )
}

private fun DrawScope.drawWalkIcon(color: Color) {
    // Walking person icon
    val width = size.width
    val height = size.height

    // Head (circle)
    drawCircle(
        color = color,
        radius = width * 0.12f,
        center = Offset(width * 0.5f, height * 0.25f)
    )

    // Body (line)
    drawLine(
        color = color,
        start = Offset(width * 0.5f, height * 0.37f),
        end = Offset(width * 0.5f, height * 0.6f),
        strokeWidth = width * 0.08f
    )

    // Left leg (bent forward)
    drawLine(
        color = color,
        start = Offset(width * 0.5f, height * 0.6f),
        end = Offset(width * 0.35f, height * 0.85f),
        strokeWidth = width * 0.08f
    )

    // Right leg (back)
    drawLine(
        color = color,
        start = Offset(width * 0.5f, height * 0.6f),
        end = Offset(width * 0.65f, height * 0.75f),
        strokeWidth = width * 0.08f
    )

    // Left arm (forward)
    drawLine(
        color = color,
        start = Offset(width * 0.5f, height * 0.45f),
        end = Offset(width * 0.65f, height * 0.55f),
        strokeWidth = width * 0.06f
    )

    // Right arm (back)
    drawLine(
        color = color,
        start = Offset(width * 0.5f, height * 0.45f),
        end = Offset(width * 0.35f, height * 0.5f),
        strokeWidth = width * 0.06f
    )
}
