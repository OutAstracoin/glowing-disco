package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.i18n.StudioStrings
import com.example.model.BodyMeshStyle
import com.example.model.BodyProportions
import com.example.model.BodyShapePreset
import com.example.model.PosePresets
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioCyanContainer
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.StudioSurfaceSolid
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.PoserUiState
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BodyShapePanel(
    uiState: PoserUiState,
    strings: StudioStrings,
    onApplyPreset: (String) -> Unit,
    onSelectMeshStyle: (BodyMeshStyle) -> Unit,
    onProportionsChange: (BodyProportions) -> Unit,
    onResetBodyShape: () -> Unit,
    onBeginHistory: (String) -> Unit,
    onEndHistory: () -> Unit
) {
    val ch = uiState.selectedCharacter ?: uiState.characters.firstOrNull() ?: return
    val effectiveStyle = ch.effectiveMeshStyle
    val effectiveProps = ch.effectiveProportions
    var activeTab by remember { mutableIntStateOf(0) } // 0 = Presets, 1 = Sculpt sliders

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SideMenuHeader(title = "${strings.bodyShape} • ${strings.meshGeometry}")

        // Presets | Sculpt Mode Tabs
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = {
                    onEndHistory()
                    activeTab = 0
                },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, if (activeTab == 0) StudioCyan else StudioBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (activeTab == 0) StudioCyanContainer else StudioSurfaceSolid,
                    contentColor = if (activeTab == 0) StudioCyan else TextPrimary
                ),
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag("body_shape_tab_presets")
            ) {
                Text(strings.presetsTab, style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }
            OutlinedButton(
                onClick = {
                    onEndHistory()
                    activeTab = 1
                },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, if (activeTab == 1) StudioCyan else StudioBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (activeTab == 1) StudioCyanContainer else StudioSurfaceSolid,
                    contentColor = if (activeTab == 1) StudioCyan else TextPrimary
                ),
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag("body_shape_tab_sculpt")
            ) {
                Text(strings.customizeTab, style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }
        }

        // Quick 3D Mesh Geometry Selector Strip
        Surface(
            color = StudioSurfaceSolid,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, StudioBorder)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = strings.meshGeometry,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BodyMeshStyle.entries.forEach { style ->
                        val selected = effectiveStyle == style
                        OutlinedButton(
                            onClick = { onSelectMeshStyle(style) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (selected) StudioCyan else StudioBorder),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selected) StudioCyanContainer else Color.Transparent,
                                contentColor = if (selected) StudioCyan else TextPrimary
                            ),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("mesh_style_${style.key}")
                        ) {
                            Text(style.label(strings), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        if (activeTab == 0) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .testTag("body_shape_presets_grid")
            ) {
                items(PosePresets.bodyShapePresets, key = { it.id }) { preset ->
                    val isSelected = ch.bodyShapePresetId == preset.id &&
                        ch.bodyMeshStyleOverride == null &&
                        ch.bodyProportionsOverride == null
                    BodyShapePresetCard(
                        preset = preset,
                        strings = strings,
                        selected = isSelected,
                        onClick = { onApplyPreset(preset.id) }
                    )
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Surface(
                    color = StudioSurfaceSolid,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, StudioBorder)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.padding(10.dp)
                    ) {
                        ProportionSliderRow(
                            label = strings.heightScale,
                            value = effectiveProps.heightScale,
                            range = 0.70f..1.30f,
                            testTag = "slider_body_height",
                            onValueChange = {
                                onBeginHistory(strings.heightScale)
                                onProportionsChange(effectiveProps.copy(heightScale = it))
                            },
                            onValueChangeFinished = onEndHistory
                        )
                        ProportionSliderRow(
                            label = strings.shoulderWidth,
                            value = effectiveProps.shoulderWidth,
                            range = 0.70f..1.45f,
                            testTag = "slider_body_shoulders",
                            onValueChange = {
                                onBeginHistory(strings.shoulderWidth)
                                onProportionsChange(effectiveProps.copy(shoulderWidth = it))
                            },
                            onValueChangeFinished = onEndHistory
                        )
                        ProportionSliderRow(
                            label = strings.hipWidth,
                            value = effectiveProps.hipWidth,
                            range = 0.70f..1.40f,
                            testTag = "slider_body_hips",
                            onValueChange = {
                                onBeginHistory(strings.hipWidth)
                                onProportionsChange(effectiveProps.copy(hipWidth = it))
                            },
                            onValueChangeFinished = onEndHistory
                        )
                        ProportionSliderRow(
                            label = strings.limbThickness,
                            value = effectiveProps.limbThickness,
                            range = 0.65f..1.45f,
                            testTag = "slider_body_limbs",
                            onValueChange = {
                                onBeginHistory(strings.limbThickness)
                                onProportionsChange(effectiveProps.copy(limbThickness = it))
                            },
                            onValueChangeFinished = onEndHistory
                        )
                        ProportionSliderRow(
                            label = strings.headScale,
                            value = effectiveProps.headScale,
                            range = 0.75f..1.50f,
                            testTag = "slider_body_head",
                            onValueChange = {
                                onBeginHistory(strings.headScale)
                                onProportionsChange(effectiveProps.copy(headScale = it))
                            },
                            onValueChangeFinished = onEndHistory
                        )
                        ProportionSliderRow(
                            label = strings.chestDepth,
                            value = effectiveProps.chestDepth,
                            range = 0.70f..1.40f,
                            testTag = "slider_body_depth",
                            onValueChange = {
                                onBeginHistory(strings.chestDepth)
                                onProportionsChange(effectiveProps.copy(chestDepth = it))
                            },
                            onValueChangeFinished = onEndHistory
                        )
                    }
                }

                OutlinedButton(
                    onClick = onResetBodyShape,
                    border = BorderStroke(1.dp, StudioBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("body_shape_reset_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.resetProportions,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun BodyShapePresetCard(
    preset: BodyShapePreset,
    strings: StudioStrings,
    selected: Boolean,
    onClick: () -> Unit
) {
    val accent = Color(preset.accentHex)
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (selected) StudioCyanContainer else StudioSurfaceSolid
        ),
        border = BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) StudioCyan else StudioBorder
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("body_shape_${preset.id}")
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.15f)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                accent.copy(alpha = 0.18f),
                                StudioSurfaceElevated
                            )
                        )
                    )
            ) {
                BodySilhouettePreview(
                    preset = preset,
                    accent = accent,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                )
                Surface(
                    color = StudioSurfaceSolid.copy(alpha = 0.88f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    Text(
                        text = preset.meshStyle.label(strings),
                        style = MaterialTheme.typography.labelSmall,
                        color = accent,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
            }
            Text(
                text = preset.label(strings),
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) StudioCyan else TextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun BodySilhouettePreview(
    preset: BodyShapePreset,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val p = preset.proportions
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f

        val heightFactor = (p.heightScale / 1.12f).coerceIn(0.65f, 1.05f)
        val bodyH = h * (0.78f + 0.16f * heightFactor)
        val topY = (h - bodyH) * 0.5f + h * 0.04f

        val headR = (h * 0.105f * p.headScale).coerceIn(h * 0.07f, h * 0.16f)
        val shoulderHalf = w * 0.18f * p.shoulderWidth
        val hipHalf = w * 0.145f * p.hipWidth
        val limbW = (w * 0.065f * p.limbThickness).coerceIn(3f, w * 0.13f)

        val headCy = topY + headR
        val neckY = headCy + headR + h * 0.02f
        val waistY = neckY + bodyH * 0.28f
        val pelvisBottomY = waistY + bodyH * 0.14f
        val footY = (topY + bodyH).coerceAtMost(h * 0.96f)

        val bodyTint = accent.copy(alpha = 0.88f)
        val limbTint = accent.copy(alpha = 0.68f)

        val angular = preset.meshStyle == BodyMeshStyle.PLANAR || preset.meshStyle == BodyMeshStyle.ROBOTIC
        val cylindrical = preset.meshStyle == BodyMeshStyle.CYLINDRICAL

        // Head
        if (angular) {
            drawRoundRect(
                color = bodyTint,
                topLeft = Offset(cx - headR * 0.85f, headCy - headR),
                size = Size(headR * 1.7f, headR * 1.9f),
                cornerRadius = CornerRadius(3f, 3f)
            )
        } else {
            drawCircle(
                color = bodyTint,
                radius = headR,
                center = Offset(cx, headCy)
            )
        }

        // Torso trapezoid / silhouette
        val torsoPath = Path().apply {
            moveTo(cx - shoulderHalf, neckY)
            lineTo(cx + shoulderHalf, neckY)
            lineTo(cx + hipHalf * 0.88f, waistY)
            lineTo(cx + hipHalf, pelvisBottomY)
            lineTo(cx - hipHalf, pelvisBottomY)
            lineTo(cx - hipHalf * 0.88f, waistY)
            close()
        }
        drawPath(path = torsoPath, color = bodyTint)

        // Arms & Legs
        val armLen = bodyH * 0.36f
        val armRadius = if (angular) CornerRadius(2f, 2f) else if (cylindrical) CornerRadius(4f, 4f) else CornerRadius(limbW, limbW)

        // Left & Right Arms
        drawRoundRect(
            color = limbTint,
            topLeft = Offset(cx - shoulderHalf - limbW * 1.1f, neckY + 2f),
            size = Size(limbW, armLen),
            cornerRadius = armRadius
        )
        drawRoundRect(
            color = limbTint,
            topLeft = Offset(cx + shoulderHalf + limbW * 0.1f, neckY + 2f),
            size = Size(limbW, armLen),
            cornerRadius = armRadius
        )

        // Left & Right Legs
        val legLen = (footY - pelvisBottomY).coerceAtLeast(10f)
        val legW = limbW * 1.18f
        drawRoundRect(
            color = limbTint,
            topLeft = Offset(cx - hipHalf * 0.78f, pelvisBottomY + 2f),
            size = Size(legW, legLen),
            cornerRadius = armRadius
        )
        drawRoundRect(
            color = limbTint,
            topLeft = Offset(cx + hipHalf * 0.78f - legW, pelvisBottomY + 2f),
            size = Size(legW, legLen),
            cornerRadius = armRadius
        )
    }
}

@Composable
private fun ProportionSliderRow(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    testTag: String,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(66.dp)
        )
        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
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
            text = "${(value * 100).roundToInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            modifier = Modifier.width(36.dp)
        )
    }
}
