package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.i18n.StudioStrings
import com.example.model.CameraPreset
import com.example.model.CameraProjection
import com.example.model.CameraSettings
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioCyanContainer
import com.example.ui.theme.StudioSurfaceSolid
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

private data class CameraViewOption(
    val preset: CameraPreset,
    val label: String,
    val angleTag: String
)

@Composable
fun CameraControlPanel(
    camera: CameraSettings,
    strings: StudioStrings,
    onSelectProjection: (CameraProjection) -> Unit,
    onSelectPreset: (CameraPreset, CameraProjection?) -> Unit,
    onFovChange: (Float) -> Unit,
    onDistanceChange: (Float) -> Unit,
    onFrameSelected: () -> Unit,
    onResetCamera: () -> Unit
) {
    val isOrtho = camera.projection == CameraProjection.ORTHOGRAPHIC

    val viewOptions = listOf(
        CameraViewOption(CameraPreset.FRONT, strings.front, "0°, 0°"),
        CameraViewOption(CameraPreset.RIGHT, strings.sideView, "90°, 0°"),
        CameraViewOption(CameraPreset.TOP, strings.top, "0°, 90°"),
        CameraViewOption(CameraPreset.ISOMETRIC, strings.isometric, "45°, 35°"),
        CameraViewOption(CameraPreset.LEFT, strings.sideLeftView, "-90°, 0°"),
        CameraViewOption(CameraPreset.BACK, strings.back, "180°, 0°"),
        CameraViewOption(CameraPreset.BOTTOM, strings.bottom, "0°, -90°")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .heightIn(max = 460.dp)
            .verticalScroll(rememberScrollState())
    ) {
        SideMenuHeader(title = strings.cameraControls)

        // Projection Mode Switcher (Orthogonal vs Perspective)
        Surface(
            color = StudioSurfaceSolid,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, StudioBorder)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = strings.projectionMode,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "${camera.azimuthDeg.roundToInt()}°, ${camera.elevationDeg.roundToInt()}°",
                        style = MaterialTheme.typography.labelSmall,
                        color = StudioCyan
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { onSelectProjection(CameraProjection.PERSPECTIVE) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                        border = BorderStroke(1.dp, if (!isOrtho) StudioCyan else StudioBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (!isOrtho) StudioCyanContainer else Color.Transparent,
                            contentColor = if (!isOrtho) StudioCyan else TextPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("camera_proj_perspective")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ViewInAr,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = strings.perspectiveMode,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    OutlinedButton(
                        onClick = { onSelectProjection(CameraProjection.ORTHOGRAPHIC) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                        border = BorderStroke(1.dp, if (isOrtho) StudioCyan else StudioBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isOrtho) StudioCyanContainer else Color.Transparent,
                            contentColor = if (isOrtho) StudioCyan else TextPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("camera_proj_orthographic")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.GridOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = strings.orthographic,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Predefined View Angles Grid (Front, Side, Top, Isometric, etc.)
        Text(
            text = strings.cameraViews,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 228.dp)
                .testTag("camera_presets_grid")
        ) {
            items(viewOptions, key = { it.preset.name }) { option ->
                val isSelected = camera.activePreset == option.preset
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) StudioCyanContainer else StudioSurfaceSolid
                    ),
                    border = BorderStroke(
                        if (isSelected) 1.5.dp else 1.dp,
                        if (isSelected) StudioCyan else StudioBorder
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("camera_preset_${option.preset.name.lowercase()}")
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onSelectPreset(option.preset, null) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        CameraViewGlyph(
                            preset = option.preset,
                            isOrtho = isOrtho,
                            tint = if (isSelected) StudioCyan else TextSecondary,
                            modifier = Modifier.size(26.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = option.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) StudioCyan else TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = option.angleTag,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // FOV & Distance Sliders
        Surface(
            color = StudioSurfaceSolid,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, StudioBorder)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.padding(10.dp)
            ) {
                if (!isOrtho) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = strings.fov,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary,
                            modifier = Modifier.width(54.dp)
                        )
                        Slider(
                            value = camera.fovDeg.coerceIn(25f, 90f),
                            onValueChange = onFovChange,
                            valueRange = 25f..90f,
                            colors = SliderDefaults.colors(
                                thumbColor = StudioCyan,
                                activeTrackColor = StudioCyan
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("slider_perspective_fov")
                        )
                        Text(
                            text = "${camera.fovDeg.roundToInt()}°",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(34.dp)
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = strings.cameraZoom,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(54.dp)
                    )
                    Slider(
                        value = camera.distance.coerceIn(1.2f, 10.0f),
                        onValueChange = onDistanceChange,
                        valueRange = 1.2f..10.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = StudioCyan,
                            activeTrackColor = StudioCyan
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("slider_camera_distance")
                    )
                    Text(
                        text = "%.1fm".format(camera.distance),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(34.dp)
                    )
                }
            }
        }

        // Frame Selected & Reset Camera Actions
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = onFrameSelected,
                border = BorderStroke(1.dp, StudioBorder),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag("camera_center_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.CenterFocusStrong,
                    contentDescription = null,
                    tint = StudioCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = strings.center,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPrimary,
                    maxLines = 1
                )
            }
            OutlinedButton(
                onClick = onResetCamera,
                border = BorderStroke(1.dp, StudioBorder),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag("camera_reset_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = strings.resetCamera,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun CameraViewGlyph(
    preset: CameraPreset,
    isOrtho: Boolean,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 2.2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val dimTint = tint.copy(alpha = 0.42f)

        when (preset) {
            CameraPreset.ISOMETRIC -> {
                // Isometric cube outline
                val cx = w * 0.5f
                val r = w * 0.36f
                val top = Offset(cx, h * 0.14f)
                val upL = Offset(cx - r * 0.88f, h * 0.33f)
                val upR = Offset(cx + r * 0.88f, h * 0.33f)
                val mid = Offset(cx, h * 0.52f)
                val botL = Offset(cx - r * 0.88f, h * 0.70f)
                val botR = Offset(cx + r * 0.88f, h * 0.70f)
                val bot = Offset(cx, h * 0.88f)
                val path = Path().apply {
                    moveTo(top.x, top.y)
                    lineTo(upR.x, upR.y)
                    lineTo(botR.x, botR.y)
                    lineTo(bot.x, bot.y)
                    lineTo(botL.x, botL.y)
                    lineTo(upL.x, upL.y)
                    close()
                }
                drawPath(path, color = tint, style = stroke)
                drawLine(tint, upL, mid, strokeWidth = 2f)
                drawLine(tint, upR, mid, strokeWidth = 2f)
                drawLine(tint, mid, bot, strokeWidth = 2f)
            }
            CameraPreset.FRONT, CameraPreset.BACK -> {
                // Planar frame with filled face indicator
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.18f, h * 0.18f),
                    size = Size(w * 0.64f, h * 0.64f),
                    cornerRadius = CornerRadius(4f, 4f),
                    style = stroke
                )
                drawCircle(
                    color = if (preset == CameraPreset.FRONT) tint else dimTint,
                    radius = w * 0.13f,
                    center = Offset(w * 0.5f, h * 0.5f)
                )
            }
            CameraPreset.RIGHT, CameraPreset.LEFT -> {
                // Profile silhouette frame with active left/right edge
                drawRoundRect(
                    color = dimTint,
                    topLeft = Offset(w * 0.18f, h * 0.18f),
                    size = Size(w * 0.64f, h * 0.64f),
                    cornerRadius = CornerRadius(4f, 4f),
                    style = stroke
                )
                val edgeX = if (preset == CameraPreset.RIGHT) w * 0.82f else w * 0.18f
                drawLine(
                    color = tint,
                    start = Offset(edgeX, h * 0.18f),
                    end = Offset(edgeX, h * 0.82f),
                    strokeWidth = 4.2f,
                    cap = StrokeCap.Round
                )
            }
            CameraPreset.TOP, CameraPreset.BOTTOM -> {
                // Horizontal plane slice with highlighted top or bottom bar
                drawRoundRect(
                    color = dimTint,
                    topLeft = Offset(w * 0.18f, h * 0.18f),
                    size = Size(w * 0.64f, h * 0.64f),
                    cornerRadius = CornerRadius(4f, 4f),
                    style = stroke
                )
                val edgeY = if (preset == CameraPreset.TOP) h * 0.20f else h * 0.80f
                drawLine(
                    color = tint,
                    start = Offset(w * 0.18f, edgeY),
                    end = Offset(w * 0.82f, edgeY),
                    strokeWidth = 4.2f,
                    cap = StrokeCap.Round
                )
                if (!isOrtho) {
                    drawCircle(tint, radius = 2.5f, center = Offset(w * 0.5f, h * 0.5f))
                }
            }
        }
    }
}
