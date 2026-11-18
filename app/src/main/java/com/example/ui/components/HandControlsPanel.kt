package com.example.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.i18n.StudioStrings
import com.example.model.HandFingerState
import com.example.model.JointId
import com.example.model.PosePresets
import com.example.model.Vec3
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioCyanContainer
import com.example.ui.theme.StudioSurfaceSolid
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.PoserUiState
import kotlin.math.roundToInt

@Composable
fun HandControlsPanel(
    uiState: PoserUiState,
    strings: StudioStrings,
    onSelectSide: (Boolean) -> Unit,
    onApplyHandPose: (Int, Boolean) -> Unit,
    onFingerStateChange: (Boolean, HandFingerState) -> Unit,
    onWristRotationChange: (JointId, Vec3) -> Unit,
    onMirrorHand: (Boolean) -> Unit,
    onResetHand: (Boolean) -> Unit,
    onBeginHistory: (String) -> Unit,
    onEndHistory: () -> Unit
) {
    val ch = uiState.selectedCharacter ?: uiState.characters.firstOrNull() ?: return
    val leftSelected = uiState.handLeftSelected
    val fingerState = if (leftSelected) ch.leftHand else ch.rightHand
    val wristJoint = if (leftSelected) JointId.L_HAND else JointId.R_HAND
    val wristEuler = ch.jointRotations[wristJoint] ?: Vec3()

    var activeTab by remember { mutableIntStateOf(0) } // 0 = Manual Sliders, 1 = Presets

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SideMenuHeader(title = strings.handPose)

        // Left / Right Hand Toggle
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    onEndHistory()
                    onSelectSide(true)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (leftSelected) StudioCyan else StudioSurfaceSolid,
                    contentColor = if (leftSelected) MaterialTheme.colorScheme.onPrimary else TextPrimary
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag("hand_side_left_button")
            ) {
                Text(strings.left, style = MaterialTheme.typography.labelMedium)
            }
            Button(
                onClick = {
                    onEndHistory()
                    onSelectSide(false)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!leftSelected) StudioCyan else StudioSurfaceSolid,
                    contentColor = if (!leftSelected) MaterialTheme.colorScheme.onPrimary else TextPrimary
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag("hand_side_right_button")
            ) {
                Text(strings.right, style = MaterialTheme.typography.labelMedium)
            }
        }

        // Manual Sliders / Presets Mode Tabs
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
                    .testTag("hand_tab_sliders")
            ) {
                Text(strings.manualSliders, style = MaterialTheme.typography.labelSmall, maxLines = 1)
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
                    .testTag("hand_tab_presets")
            ) {
                Text(strings.presetsTab, style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }
        }

        if (activeTab == 0) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .heightIn(max = 320.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Individual Finger Curl & Spread Sliders
                Surface(
                    color = StudioSurfaceSolid,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, StudioBorder)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.padding(10.dp)
                    ) {
                        FingerCurlSliderRow(
                            label = strings.thumb,
                            value = fingerState.thumbCurl,
                            testTag = "slider_finger_thumb",
                            onValueChange = {
                                onBeginHistory("${strings.thumb}: ${if (leftSelected) strings.left else strings.right}")
                                onFingerStateChange(leftSelected, fingerState.copy(thumbCurl = it))
                            },
                            onValueChangeFinished = onEndHistory
                        )
                        FingerCurlSliderRow(
                            label = strings.indexFinger,
                            value = fingerState.indexCurl,
                            testTag = "slider_finger_index",
                            onValueChange = {
                                onBeginHistory("${strings.indexFinger}: ${if (leftSelected) strings.left else strings.right}")
                                onFingerStateChange(leftSelected, fingerState.copy(indexCurl = it))
                            },
                            onValueChangeFinished = onEndHistory
                        )
                        FingerCurlSliderRow(
                            label = strings.middleFinger,
                            value = fingerState.middleCurl,
                            testTag = "slider_finger_middle",
                            onValueChange = {
                                onBeginHistory("${strings.middleFinger}: ${if (leftSelected) strings.left else strings.right}")
                                onFingerStateChange(leftSelected, fingerState.copy(middleCurl = it))
                            },
                            onValueChangeFinished = onEndHistory
                        )
                        FingerCurlSliderRow(
                            label = strings.ringFinger,
                            value = fingerState.ringCurl,
                            testTag = "slider_finger_ring",
                            onValueChange = {
                                onBeginHistory("${strings.ringFinger}: ${if (leftSelected) strings.left else strings.right}")
                                onFingerStateChange(leftSelected, fingerState.copy(ringCurl = it))
                            },
                            onValueChangeFinished = onEndHistory
                        )
                        FingerCurlSliderRow(
                            label = strings.pinkyFinger,
                            value = fingerState.pinkyCurl,
                            testTag = "slider_finger_pinky",
                            onValueChange = {
                                onBeginHistory("${strings.pinkyFinger}: ${if (leftSelected) strings.left else strings.right}")
                                onFingerStateChange(leftSelected, fingerState.copy(pinkyCurl = it))
                            },
                            onValueChangeFinished = onEndHistory
                        )
                        FingerCurlSliderRow(
                            label = strings.fingerSpread,
                            value = fingerState.spread,
                            testTag = "slider_finger_spread",
                            onValueChange = {
                                onBeginHistory(strings.fingerSpread)
                                onFingerStateChange(leftSelected, fingerState.copy(spread = it))
                            },
                            onValueChangeFinished = onEndHistory
                        )
                    }
                }

                // Wrist Rotation Sliders
                Surface(
                    color = StudioSurfaceSolid,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, StudioBorder)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Text(
                            text = strings.wristRotation,
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                        WristAngleRow(
                            axisLabel = "Flex",
                            valueDeg = wristEuler.x,
                            testTag = "slider_wrist_flex",
                            onValueChange = {
                                onBeginHistory("${strings.wristRotation}: ${wristJoint.label}")
                                onWristRotationChange(wristJoint, wristEuler.copy(x = it))
                            },
                            onValueChangeFinished = onEndHistory
                        )
                        WristAngleRow(
                            axisLabel = "Tilt",
                            valueDeg = wristEuler.z,
                            testTag = "slider_wrist_tilt",
                            onValueChange = {
                                onBeginHistory("${strings.wristRotation}: ${wristJoint.label}")
                                onWristRotationChange(wristJoint, wristEuler.copy(z = it))
                            },
                            onValueChangeFinished = onEndHistory
                        )
                    }
                }

                // Mirror & Reset Hand Actions
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { onMirrorHand(leftSelected) },
                        border = BorderStroke(1.dp, StudioBorder),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("hand_mirror_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CompareArrows,
                            contentDescription = null,
                            tint = StudioCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = strings.mirrorHand,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    OutlinedButton(
                        onClick = { onResetHand(leftSelected) },
                        border = BorderStroke(1.dp, StudioBorder),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("hand_reset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = strings.resetHand,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp)
                    .testTag("hand_presets_grid")
            ) {
                items(PosePresets.handPoses, key = { it.index }) { pose ->
                    val isSelected = fingerState == pose.fingerState
                    StudioItemCard(
                        assetPath = pose.assetPath,
                        label = pose.name,
                        selected = isSelected,
                        testTag = "hand_pose_${pose.index}",
                        onClick = { onApplyHandPose(pose.index, leftSelected) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FingerCurlSliderRow(
    label: String,
    value: Float,
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
            modifier = Modifier.width(48.dp)
        )
        Slider(
            value = value.coerceIn(0f, 1f),
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = 0f..1f,
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
            modifier = Modifier.width(34.dp)
        )
    }
}

@Composable
private fun WristAngleRow(
    axisLabel: String,
    valueDeg: Float,
    testTag: String,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = axisLabel,
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary,
            modifier = Modifier.width(48.dp)
        )
        Slider(
            value = valueDeg.coerceIn(-75f, 75f),
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = -75f..75f,
            colors = SliderDefaults.colors(
                thumbColor = StudioCyan,
                activeTrackColor = StudioCyan
            ),
            modifier = Modifier
                .weight(1f)
                .testTag(testTag)
        )
        Text(
            text = "${valueDeg.roundToInt()}°",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            modifier = Modifier.width(34.dp)
        )
    }
}
