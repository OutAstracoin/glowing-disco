package com.example.ui.components

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.i18n.StudioStrings
import com.example.model.PosePresets
import com.example.model.RgbColor
import com.example.model.SelectedPart
import com.example.model.Vec3
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioSurfaceSolid
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.PoserUiState
import kotlin.math.roundToInt

@Composable
fun TransformMenuContent(
    uiState: PoserUiState,
    strings: StudioStrings,
    onPositionChange: (Vec3) -> Unit,
    onCharScaleChange: (Float) -> Unit,
    onPropScaleChange: (Vec3) -> Unit,
    onCommitHistory: () -> Unit
) {
    val isProp = uiState.selectedPart == SelectedPart.MODEL
    val currentPos = if (isProp) {
        uiState.selectedProp?.position ?: Vec3()
    } else {
        uiState.selectedCharacter?.position ?: Vec3()
    }

    var historySaved by remember { mutableStateOf(false) }
    fun ensureHistory() {
        if (!historySaved) {
            onCommitHistory()
            historySaved = true
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        SideMenuHeader(title = strings.move)
        Surface(
            color = StudioSurfaceSolid,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, StudioBorder)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(10.dp)
            ) {
                AxisSliderRow(
                    axisLabel = "X",
                    value = currentPos.x,
                    valueRange = -5f..5f,
                    testTag = "slider_move_x",
                    onValueChange = {
                        ensureHistory()
                        onPositionChange(currentPos.copy(x = it))
                    },
                    onValueChangeFinished = { historySaved = false }
                )
                AxisSliderRow(
                    axisLabel = "Y",
                    value = currentPos.y,
                    valueRange = -3f..5f,
                    testTag = "slider_move_y",
                    onValueChange = {
                        ensureHistory()
                        onPositionChange(currentPos.copy(y = it))
                    },
                    onValueChangeFinished = { historySaved = false }
                )
                AxisSliderRow(
                    axisLabel = "Z",
                    value = currentPos.z,
                    valueRange = -5f..5f,
                    testTag = "slider_move_z",
                    onValueChange = {
                        ensureHistory()
                        onPositionChange(currentPos.copy(z = it))
                    },
                    onValueChangeFinished = { historySaved = false }
                )
            }
        }

        SideMenuHeader(title = strings.scale)
        Surface(
            color = StudioSurfaceSolid,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, StudioBorder)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(10.dp)
            ) {
                if (isProp) {
                    val propScale = uiState.selectedProp?.scale ?: Vec3(1f, 1f, 1f)
                    AxisSliderRow(
                        axisLabel = "X",
                        value = propScale.x,
                        valueRange = 0.2f..3.5f,
                        testTag = "slider_scale_x",
                        onValueChange = {
                            ensureHistory()
                            onPropScaleChange(propScale.copy(x = it))
                        },
                        onValueChangeFinished = { historySaved = false }
                    )
                    AxisSliderRow(
                        axisLabel = "Y",
                        value = propScale.y,
                        valueRange = 0.2f..3.5f,
                        testTag = "slider_scale_y",
                        onValueChange = {
                            ensureHistory()
                            onPropScaleChange(propScale.copy(y = it))
                        },
                        onValueChangeFinished = { historySaved = false }
                    )
                    AxisSliderRow(
                        axisLabel = "Z",
                        value = propScale.z,
                        valueRange = 0.2f..3.5f,
                        testTag = "slider_scale_z",
                        onValueChange = {
                            ensureHistory()
                            onPropScaleChange(propScale.copy(z = it))
                        },
                        onValueChangeFinished = { historySaved = false }
                    )
                } else {
                    val charScale = uiState.selectedCharacter?.scale ?: 1f
                    AxisSliderRow(
                        axisLabel = "S",
                        value = charScale,
                        valueRange = 0.3f..2.5f,
                        testTag = "slider_scale_s",
                        onValueChange = {
                            ensureHistory()
                            onCharScaleChange(it)
                        },
                        onValueChangeFinished = { historySaved = false }
                    )
                }
            }
        }
    }
}

@Composable
fun PerspectiveMenuContent(
    fovDeg: Float,
    strings: StudioStrings,
    onFovChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SideMenuHeader(title = strings.perspectiveFov)
        Surface(
            color = StudioSurfaceSolid,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, StudioBorder)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                AxisSliderRow(
                    axisLabel = strings.fov,
                    value = fovDeg,
                    valueRange = 30f..90f,
                    formatAsInt = true,
                    testTag = "slider_perspective_fov",
                    onValueChange = onFovChange,
                    onValueChangeFinished = {}
                )
            }
        }
    }
}

@Composable
fun BrightnessMenuContent(
    brightness: Float,
    giStrength: Float,
    strings: StudioStrings,
    onBrightnessChange: (Float) -> Unit,
    onGiStrengthChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SideMenuHeader(title = strings.brightnessTitle)
        Surface(
            color = StudioSurfaceSolid,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, StudioBorder)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                AxisSliderRow(
                    axisLabel = "L",
                    value = brightness,
                    valueRange = 0f..1f,
                    testTag = "slider_brightness",
                    onValueChange = onBrightnessChange,
                    onValueChangeFinished = {}
                )
            }
        }

        SideMenuHeader(title = strings.giStrength)
        Surface(
            color = StudioSurfaceSolid,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, StudioBorder)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                AxisSliderRow(
                    axisLabel = "GI",
                    value = giStrength,
                    valueRange = 0f..1f,
                    testTag = "slider_gi_strength",
                    onValueChange = onGiStrengthChange,
                    onValueChangeFinished = {}
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MaterialMenuContent(
    uiState: PoserUiState,
    strings: StudioStrings,
    onToggleBodyHair: (Boolean) -> Unit,
    onColorChange: (RgbColor) -> Unit,
    onCommitHistory: () -> Unit
) {
    val activeColor: RgbColor = when {
        uiState.selectedPart == SelectedPart.MODEL -> uiState.selectedProp?.color ?: RgbColor()
        uiState.selectedPart == SelectedPart.HEAD || uiState.selectedPart == SelectedPart.HAIR || !uiState.materialEditBody ->
            uiState.selectedCharacter?.hairColor ?: RgbColor()
        else -> uiState.selectedCharacter?.bodyColor ?: RgbColor()
    }

    val hsv = remember(activeColor) {
        FloatArray(3).also {
            AndroidColor.RGBToHSV(
                (activeColor.r * 255).roundToInt().coerceIn(0, 255),
                (activeColor.g * 255).roundToInt().coerceIn(0, 255),
                (activeColor.b * 255).roundToInt().coerceIn(0, 255),
                it
            )
        }
    }

    var hue by remember(activeColor) { mutableFloatStateOf(hsv[0]) }
    var sat by remember(activeColor) { mutableFloatStateOf(hsv[1]) }
    var valBright by remember(activeColor) { mutableFloatStateOf(hsv[2]) }
    var historySaved by remember { mutableStateOf(false) }

    fun pushHsv(h: Float, s: Float, v: Float) {
        if (!historySaved) {
            onCommitHistory()
            historySaved = true
        }
        val argb = AndroidColor.HSVToColor(floatArrayOf(h, s, v))
        onColorChange(
            RgbColor(
                r = AndroidColor.red(argb) / 255f,
                g = AndroidColor.green(argb) / 255f,
                b = AndroidColor.blue(argb) / 255f
            )
        )
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        SideMenuHeader(title = strings.material)

        if (uiState.selectedPart == SelectedPart.SKEL_MODEL) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { onToggleBodyHair(true) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.materialEditBody) StudioCyan else StudioSurfaceSolid,
                        contentColor = if (uiState.materialEditBody) MaterialTheme.colorScheme.onPrimary else TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("material_tab_body")
                ) {
                    Text(strings.body, style = MaterialTheme.typography.labelMedium)
                }
                Button(
                    onClick = { onToggleBodyHair(false) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!uiState.materialEditBody) StudioCyan else StudioSurfaceSolid,
                        contentColor = if (!uiState.materialEditBody) MaterialTheme.colorScheme.onPrimary else TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("material_tab_hair")
                ) {
                    Text(strings.hair, style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        Surface(
            color = StudioSurfaceSolid,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, StudioBorder)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(10.dp)
            ) {
                // Current Color Preview Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(activeColor.r, activeColor.g, activeColor.b))
                            .border(1.5.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                    )
                    Text(
                        text = "#%02X%02X%02X".format(
                            (activeColor.r * 255).roundToInt(),
                            (activeColor.g * 255).roundToInt(),
                            (activeColor.b * 255).roundToInt()
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                AxisSliderRow(
                    axisLabel = "H",
                    value = hue,
                    valueRange = 0f..360f,
                    formatAsInt = true,
                    testTag = "slider_color_hue",
                    onValueChange = {
                        hue = it
                        pushHsv(hue, sat, valBright)
                    },
                    onValueChangeFinished = { historySaved = false }
                )
                AxisSliderRow(
                    axisLabel = "S",
                    value = sat,
                    valueRange = 0f..1f,
                    testTag = "slider_color_sat",
                    onValueChange = {
                        sat = it
                        pushHsv(hue, sat, valBright)
                    },
                    onValueChangeFinished = { historySaved = false }
                )
                AxisSliderRow(
                    axisLabel = "V",
                    value = valBright,
                    valueRange = 0f..1f,
                    testTag = "slider_color_val",
                    onValueChange = {
                        valBright = it
                        pushHsv(hue, sat, valBright)
                    },
                    onValueChangeFinished = { historySaved = false }
                )

                Spacer(modifier = Modifier.height(2.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PosePresets.materialSwatches.forEachIndexed { idx, swatch ->
                        SwatchCircle(
                            color = swatch,
                            testTag = "swatch_color_$idx",
                            onClick = {
                                onCommitHistory()
                                onColorChange(swatch)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PosePresets.skinToneSwatches.forEachIndexed { idx, swatch ->
                        SwatchCircle(
                            color = swatch,
                            testTag = "swatch_skin_$idx",
                            onClick = {
                                onCommitHistory()
                                onColorChange(swatch)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SwatchCircle(
    color: RgbColor,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(Color(color.r, color.g, color.b))
            .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
            .testTag(testTag)
            .clickable(onClick = onClick)
    )
}

@Composable
private fun AxisSliderRow(
    axisLabel: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    testTag: String,
    formatAsInt: Boolean = false,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = axisLabel,
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimary,
            modifier = Modifier.width(30.dp)
        )
        Slider(
            value = value.coerceIn(valueRange.start, valueRange.endInclusive),
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = StudioCyan,
                activeTrackColor = StudioCyan
            ),
            modifier = Modifier
                .weight(1f)
                .testTag(testTag)
        )
        Text(
            text = if (formatAsInt) value.roundToInt().toString() else "%.2f".format(value),
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            modifier = Modifier.width(36.dp)
        )
    }
}
