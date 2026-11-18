package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.example.i18n.StudioStrings
import com.example.model.CharacterType
import com.example.model.HairStyle
import com.example.model.PosePresets
import com.example.model.PropType
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioCyanContainer
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.StudioSurfaceSolid
import com.example.ui.theme.TextPrimary

@Composable
fun SideMenuHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = StudioSurfaceElevated,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, StudioBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        )
    }
}

@Composable
fun AddObjectMenuContent(
    strings: StudioStrings,
    onAddCharacter: (CharacterType) -> Unit,
    onAddProp: (PropType) -> Unit
) {
    var unfoldedSection by remember { mutableIntStateOf(0) } // 0 = Characters, 1 = Geometry, -1 = none

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AccordionSectionButton(
            title = strings.characters,
            expanded = unfoldedSection == 0,
            testTag = "add_section_characters",
            onClick = { unfoldedSection = if (unfoldedSection == 0) -1 else 0 }
        )
        AnimatedVisibility(visible = unfoldedSection == 0) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp)
            ) {
                items(CharacterType.entries) { model ->
                    StudioItemCard(
                        assetPath = model.assetPath,
                        label = model.label(strings),
                        testTag = "add_model_${model.key}",
                        onClick = { onAddCharacter(model) }
                    )
                }
            }
        }

        AccordionSectionButton(
            title = strings.geometry,
            expanded = unfoldedSection == 1,
            testTag = "add_section_geometry",
            onClick = { unfoldedSection = if (unfoldedSection == 1) -1 else 1 }
        )
        AnimatedVisibility(visible = unfoldedSection == 1) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp)
            ) {
                items(PropType.entries) { prop ->
                    StudioItemCard(
                        assetPath = prop.assetPath,
                        label = prop.label(strings),
                        testTag = "add_prop_${prop.key}",
                        onClick = { onAddProp(prop) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AccordionSectionButton(
    title: String,
    expanded: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        color = if (expanded) StudioCyanContainer else StudioSurfaceSolid,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, if (expanded) StudioCyan else StudioBorder),
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .testTag(testTag)
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = if (expanded) StudioCyan else TextPrimary
            )
            Icon(
                imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = title,
                tint = if (expanded) StudioCyan else TextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ReplaceModelMenuContent(
    strings: StudioStrings,
    onReplaceModel: (CharacterType) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SideMenuHeader(title = strings.replaceModel)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 380.dp)
        ) {
            items(CharacterType.entries) { model ->
                StudioItemCard(
                    assetPath = model.assetPath,
                    label = model.label(strings),
                    testTag = "replace_model_${model.key}",
                    onClick = { onReplaceModel(model) }
                )
            }
        }
    }
}

@Composable
fun PosePresetMenuContent(
    strings: StudioStrings,
    onApplyPose: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SideMenuHeader(title = strings.posePresets)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 380.dp)
        ) {
            items(PosePresets.bodyPoses) { pose ->
                StudioItemCard(
                    assetPath = pose.assetPath,
                    label = "#${pose.index + 1}",
                    testTag = "body_pose_${pose.index}",
                    onClick = { onApplyPose(pose.index) }
                )
            }
        }
    }
}

@Composable
fun HandPoseMenuContent(
    strings: StudioStrings,
    leftSelected: Boolean,
    onSelectSide: (Boolean) -> Unit,
    onApplyHandPose: (Int, Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SideMenuHeader(title = strings.handPose)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { onSelectSide(true) },
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
                onClick = { onSelectSide(false) },
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
        Spacer(modifier = Modifier.height(2.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 340.dp)
        ) {
            items(PosePresets.handPoses) { pose ->
                StudioItemCard(
                    assetPath = pose.assetPath,
                    label = "#${pose.index + 1}",
                    testTag = "hand_pose_${pose.index}",
                    onClick = { onApplyHandPose(pose.index, leftSelected) }
                )
            }
        }
    }
}

@Composable
fun HairMenuContent(
    strings: StudioStrings,
    onSelectHair: (HairStyle) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SideMenuHeader(title = strings.hairs)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 380.dp)
        ) {
            items(HairStyle.entries) { style ->
                StudioItemCard(
                    assetPath = style.assetPath,
                    label = style.displayTag,
                    testTag = "hair_style_${style.key}",
                    onClick = { onSelectHair(style) }
                )
            }
        }
    }
}
