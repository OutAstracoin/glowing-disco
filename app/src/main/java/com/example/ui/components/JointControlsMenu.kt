package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.i18n.StudioStrings
import com.example.model.JointId
import com.example.model.Vec3
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioCyanContainer
import com.example.ui.theme.StudioSurfaceSolid
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.PoserUiState
import kotlin.math.roundToInt

private enum class LimbGroup { ARMS, LEGS, TORSO }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JointControlsMenuContent(
    uiState: PoserUiState,
    strings: StudioStrings,
    onJointRotationChange: (JointId, Vec3) -> Unit,
    onResetJoint: (JointId) -> Unit,
    onResetAll: () -> Unit,
    onBeginHistory: (String) -> Unit,
    onEndHistory: () -> Unit
) {
    val ch = uiState.selectedCharacter ?: return

    val initialGroup = when (uiState.selectedJoint) {
        JointId.L_THIGH, JointId.L_SHIN, JointId.L_FOOT,
        JointId.R_THIGH, JointId.R_SHIN, JointId.R_FOOT -> LimbGroup.LEGS
        JointId.PELVIS, JointId.SPINE, JointId.CHEST, JointId.NECK, JointId.HEAD -> LimbGroup.TORSO
        else -> LimbGroup.ARMS
    }

    var selectedGroupIdx by remember(uiState.selectedEntityId) {
        mutableIntStateOf(initialGroup.ordinal)
    }
    val activeGroup = LimbGroup.entries[selectedGroupIdx]

    val groupJoints = when (activeGroup) {
        LimbGroup.ARMS -> listOf(
            JointId.L_UPPER_ARM,
            JointId.L_FOREARM,
            JointId.R_UPPER_ARM,
            JointId.R_FOREARM
        )
        LimbGroup.LEGS -> listOf(
            JointId.L_THIGH,
            JointId.L_SHIN,
            JointId.R_THIGH,
            JointId.R_SHIN
        )
        LimbGroup.TORSO -> listOf(
            JointId.PELVIS,
            JointId.SPINE,
            JointId.CHEST,
            JointId.HEAD
        )
    }

    var activeJoint by remember(activeGroup, uiState.selectedEntityId) {
        val current = uiState.selectedJoint
        mutableStateOf(if (current != null && current in groupJoints) current else groupJoints.first())
    }

    val currentEuler = ch.jointRotations[activeJoint] ?: Vec3()

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        SideMenuHeader(title = strings.joints)

        // Category Tabs: Arms / Legs / Torso
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val tabs = listOf(
                LimbGroup.ARMS to strings.arms,
                LimbGroup.LEGS to strings.legs,
                LimbGroup.TORSO to strings.torso
            )
            tabs.forEach { (grp, label) ->
                val selected = activeGroup == grp
                Button(
                    onClick = {
                        onEndHistory()
                        selectedGroupIdx = grp.ordinal
                        activeJoint = when (grp) {
                            LimbGroup.ARMS -> JointId.L_UPPER_ARM
                            LimbGroup.LEGS -> JointId.L_THIGH
                            LimbGroup.TORSO -> JointId.SPINE
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected) StudioCyan else StudioSurfaceSolid,
                        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("joint_tab_${grp.name.lowercase()}")
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                }
            }
        }

        // Joint Selector Pills
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            groupJoints.forEach { joint ->
                val isSelected = activeJoint == joint
                Surface(
                    color = if (isSelected) StudioCyanContainer else StudioSurfaceSolid,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isSelected) StudioCyan else StudioBorder),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .testTag("joint_select_${joint.name.lowercase()}")
                        .clickable {
                            onEndHistory()
                            activeJoint = joint
                        }
                ) {
                    Text(
                        text = joint.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) StudioCyan else TextPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                    )
                }
            }
        }

        // X / Y / Z Rotation Sliders
        Surface(
            color = StudioSurfaceSolid,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, StudioBorder)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(10.dp)
            ) {
                Text(
                    text = activeJoint.label,
                    style = MaterialTheme.typography.titleSmall,
                    color = StudioCyan
                )
                JointAngleSliderRow(
                    axis = "X",
                    valueDeg = currentEuler.x,
                    testTag = "slider_joint_x",
                    onValueChange = {
                        onBeginHistory("Joint: ${activeJoint.label}")
                        onJointRotationChange(activeJoint, currentEuler.copy(x = it))
                    },
                    onValueChangeFinished = onEndHistory
                )
                JointAngleSliderRow(
                    axis = "Y",
                    valueDeg = currentEuler.y,
                    testTag = "slider_joint_y",
                    onValueChange = {
                        onBeginHistory("Joint: ${activeJoint.label}")
                        onJointRotationChange(activeJoint, currentEuler.copy(y = it))
                    },
                    onValueChangeFinished = onEndHistory
                )
                JointAngleSliderRow(
                    axis = "Z",
                    valueDeg = currentEuler.z,
                    testTag = "slider_joint_z",
                    onValueChange = {
                        onBeginHistory("Joint: ${activeJoint.label}")
                        onJointRotationChange(activeJoint, currentEuler.copy(z = it))
                    },
                    onValueChangeFinished = onEndHistory
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = { onResetJoint(activeJoint) },
                border = BorderStroke(1.dp, StudioBorder),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag("button_reset_joint")
            ) {
                Text(strings.resetJoint, style = MaterialTheme.typography.labelSmall, color = TextPrimary)
            }
            OutlinedButton(
                onClick = onResetAll,
                border = BorderStroke(1.dp, StudioBorder),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag("button_reset_pose")
            ) {
                Text(strings.resetPose, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun JointAngleSliderRow(
    axis: String,
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
            text = axis,
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimary,
            modifier = Modifier.padding(end = 6.dp)
        )
        Slider(
            value = valueDeg.coerceIn(-180f, 180f),
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = -180f..180f,
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
            color = TextSecondary
        )
    }
}
