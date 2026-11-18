package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.i18n.StudioStrings
import com.example.model.Vec3
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCoral
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.StudioSurfaceSolid
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

enum class PlaceholderShape(val label: String) {
    CUBE("Cube"),
    PYRAMID("Pyramid"),
    OCTAHEDRON("Octahedron")
}

private data class FaceQuad(
    val indices: IntArray,
    val baseColor: Color
)

@Composable
fun ComposePlaceholder3DDialog(
    strings: StudioStrings,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = StudioSurfaceElevated,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, StudioBorder),
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .testTag("compose_placeholder_3d_dialog")
        ) {
            ComposePlaceholder3DViewer(
                strings = strings,
                onClose = onDismiss,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
fun ComposePlaceholder3DViewer(
    strings: StudioStrings,
    onClose: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var rotationXDeg by remember { mutableFloatStateOf(-22f) }
    var rotationYDeg by remember { mutableFloatStateOf(34f) }
    var scale by remember { mutableFloatStateOf(1.0f) }
    var selectedShape by remember { mutableStateOf(PlaceholderShape.CUBE) }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = strings.placeholder3d,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            if (onClose != null) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("close_placeholder_3d_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.cd_close_menu),
                        tint = TextSecondary
                    )
                }
            }
        }

        // Shape Switcher Row
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            PlaceholderShape.entries.forEach { shape ->
                val active = selectedShape == shape
                Button(
                    onClick = { selectedShape = shape },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (active) StudioCyan else StudioSurfaceSolid,
                        contentColor = if (active) MaterialTheme.colorScheme.onPrimary else TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("placeholder_shape_${shape.name.lowercase()}")
                ) {
                    Text(
                        text = shape.label,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                }
            }
        }

        // Interactive 3D Compose Canvas Viewport
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1A2233), Color(0xFF10131A))
                    )
                )
                .testTag("compose_3d_canvas")
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        rotationYDeg = (rotationYDeg + pan.x * 0.45f) % 360f
                        rotationXDeg = (rotationXDeg - pan.y * 0.45f).coerceIn(-89f, 89f)
                        scale = (scale * zoom).coerceIn(0.45f, 2.2f)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width * 0.5f
                val cy = size.height * 0.5f
                val baseSize = min(size.width, size.height) * 0.34f * scale

                val (vertices, faces) = buildPlaceholderMesh(selectedShape)

                val rx = rotationXDeg * PI.toFloat() / 180f
                val ry = rotationYDeg * PI.toFloat() / 180f
                val cosX = cos(rx); val sinX = sin(rx)
                val cosY = cos(ry); val sinY = sin(ry)

                val transformed = vertices.map { v ->
                    // Rotate Y then Rotate X
                    val x1 = v.x * cosY + v.z * sinY
                    val z1 = -v.x * sinY + v.z * cosY
                    val y2 = v.y * cosX - z1 * sinX
                    val z2 = v.y * sinX + z1 * cosX
                    Vec3(x1, y2, z2)
                }

                val lightDir = Vec3(0.45f, 0.72f, 0.52f).normalized()
                val focal = 4.2f

                val projected = transformed.map { v ->
                    val perspective = focal / (focal - v.z).coerceAtLeast(0.5f)
                    Offset(
                        x = cx + v.x * baseSize * perspective,
                        y = cy - v.y * baseSize * perspective
                    )
                }

                // Painter's algorithm: sort faces back-to-front by average Z
                val sortedFaces = faces.map { face ->
                    val pts3D = face.indices.map { transformed[it] }
                    val avgZ = pts3D.sumOf { it.z.toDouble() }.toFloat() / pts3D.size
                    val e1 = pts3D[1] - pts3D[0]
                    val e2 = pts3D[2] - pts3D[0]
                    val normal = e1.cross(e2).normalized()
                    Triple(face, avgZ, normal)
                }.sortedBy { it.second }

                for ((face, _, normal) in sortedFaces) {
                    // Back-face culling against viewer +Z
                    if (normal.z <= -0.05f) continue

                    val shade = (0.38f + 0.62f * normal.dot(lightDir).coerceIn(0f, 1f))
                    val shadedColor = Color(
                        red = (face.baseColor.red * shade).coerceIn(0f, 1f),
                        green = (face.baseColor.green * shade).coerceIn(0f, 1f),
                        blue = (face.baseColor.blue * shade).coerceIn(0f, 1f),
                        alpha = 0.96f
                    )

                    val path = Path().apply {
                        val first = projected[face.indices[0]]
                        moveTo(first.x, first.y)
                        for (k in 1 until face.indices.size) {
                            val pt = projected[face.indices[k]]
                            lineTo(pt.x, pt.y)
                        }
                        close()
                    }
                    drawPath(path = path, color = shadedColor)
                    drawPath(
                        path = path,
                        color = Color.White.copy(alpha = 0.32f),
                        style = Stroke(width = 1.6f)
                    )
                }
            }

            Text(
                text = strings.placeholder3dHint,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
            )
        }

        // Precision Rotation & Scale Sliders
        Surface(
            color = StudioSurfaceSolid,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, StudioBorder)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                PlaceholderSliderRow(
                    label = "Rot X",
                    value = rotationXDeg,
                    range = -89f..89f,
                    valueText = "${rotationXDeg.roundToInt()}°",
                    testTag = "slider_placeholder_rotx",
                    onValueChange = { rotationXDeg = it }
                )
                PlaceholderSliderRow(
                    label = "Rot Y",
                    value = rotationYDeg,
                    range = -180f..180f,
                    valueText = "${rotationYDeg.roundToInt()}°",
                    testTag = "slider_placeholder_roty",
                    onValueChange = { rotationYDeg = it }
                )
                PlaceholderSliderRow(
                    label = strings.scale,
                    value = scale,
                    range = 0.45f..2.2f,
                    valueText = "%.2fx".format(scale),
                    testTag = "slider_placeholder_scale",
                    onValueChange = { scale = it }
                )
            }
        }

        OutlinedButton(
            onClick = {
                rotationXDeg = -22f
                rotationYDeg = 34f
                scale = 1.0f
            },
            border = BorderStroke(1.dp, StudioBorder),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("button_reset_placeholder_3d")
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = null,
                tint = StudioCyan,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(strings.resetView, style = MaterialTheme.typography.labelMedium, color = TextPrimary)
        }
    }
}

@Composable
private fun PlaceholderSliderRow(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    valueText: String,
    testTag: String,
    onValueChange: (Float) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimary,
            modifier = Modifier.width(44.dp)
        )
        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = StudioCyan,
                activeTrackColor = StudioCyan
            ),
            modifier = Modifier
                .weight(1f)
                .testTag(testTag)
        )
        Text(
            text = valueText,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            modifier = Modifier.width(42.dp)
        )
    }
}

private fun buildPlaceholderMesh(shape: PlaceholderShape): Pair<List<Vec3>, List<FaceQuad>> {
    return when (shape) {
        PlaceholderShape.CUBE -> {
            val s = 0.75f
            val verts = listOf(
                Vec3(-s, -s, s),  // 0
                Vec3(s, -s, s),   // 1
                Vec3(s, s, s),    // 2
                Vec3(-s, s, s),   // 3
                Vec3(-s, -s, -s), // 4
                Vec3(s, -s, -s),  // 5
                Vec3(s, s, -s),   // 6
                Vec3(-s, s, -s)   // 7
            )
            val faces = listOf(
                FaceQuad(intArrayOf(0, 1, 2, 3), StudioCyan),
                FaceQuad(intArrayOf(5, 4, 7, 6), StudioCyan),
                FaceQuad(intArrayOf(4, 0, 3, 7), Color(0xFF64B5F6)),
                FaceQuad(intArrayOf(1, 5, 6, 2), Color(0xFF64B5F6)),
                FaceQuad(intArrayOf(3, 2, 6, 7), StudioCoral),
                FaceQuad(intArrayOf(4, 5, 1, 0), Color(0xFF90A4AE))
            )
            verts to faces
        }
        PlaceholderShape.PYRAMID -> {
            val s = 0.85f
            val h = 0.95f
            val verts = listOf(
                Vec3(0f, h, 0f),      // 0 apex
                Vec3(-s, -0.6f, s),   // 1
                Vec3(s, -0.6f, s),    // 2
                Vec3(s, -0.6f, -s),   // 3
                Vec3(-s, -0.6f, -s)   // 4
            )
            val faces = listOf(
                FaceQuad(intArrayOf(1, 2, 0), StudioCyan),
                FaceQuad(intArrayOf(2, 3, 0), Color(0xFF64B5F6)),
                FaceQuad(intArrayOf(3, 4, 0), StudioCoral),
                FaceQuad(intArrayOf(4, 1, 0), Color(0xFFFFCA28)),
                FaceQuad(intArrayOf(4, 3, 2, 1), Color(0xFF78909C))
            )
            verts to faces
        }
        PlaceholderShape.OCTAHEDRON -> {
            val r = 0.95f
            val verts = listOf(
                Vec3(0f, r, 0f),   // 0 top
                Vec3(0f, -r, 0f),  // 1 bottom
                Vec3(0f, 0f, r),   // 2 front
                Vec3(r, 0f, 0f),   // 3 right
                Vec3(0f, 0f, -r),  // 4 back
                Vec3(-r, 0f, 0f)   // 5 left
            )
            val faces = listOf(
                FaceQuad(intArrayOf(2, 3, 0), StudioCyan),
                FaceQuad(intArrayOf(3, 4, 0), Color(0xFF64B5F6)),
                FaceQuad(intArrayOf(4, 5, 0), StudioCoral),
                FaceQuad(intArrayOf(5, 2, 0), Color(0xFFFFCA28)),
                FaceQuad(intArrayOf(3, 2, 1), Color(0xFF26A69A)),
                FaceQuad(intArrayOf(4, 3, 1), Color(0xFF5C6BC0)),
                FaceQuad(intArrayOf(5, 4, 1), Color(0xFFEC407A)),
                FaceQuad(intArrayOf(2, 5, 1), Color(0xFFAB47BC))
            )
            verts to faces
        }
    }
}
