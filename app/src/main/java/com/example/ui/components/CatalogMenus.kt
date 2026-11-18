package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.i18n.StudioStrings
import com.example.model.CharacterType
import com.example.model.HairStyle
import com.example.model.PosePresets
import com.example.model.PropCategory
import com.example.model.PropType
import com.example.model.SavedStance
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioCyanContainer
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.StudioSurfaceSolid
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

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
            val shapeProps = remember { PropType.entries.filter { it.category == PropCategory.SHAPE } }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp)
            ) {
                items(shapeProps) { prop ->
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
    savedStances: List<SavedStance>,
    activePoseId: String?,
    onApplyPose: (Int) -> Unit,
    onSaveCurrentStance: () -> Unit,
    onApplySavedStance: (String) -> Unit,
    onDeleteSavedStance: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SideMenuHeader(title = strings.quickPoses)
        OutlinedButton(
            onClick = onSaveCurrentStance,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, StudioCyan),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("save_current_stance_button")
        ) {
            Icon(
                imageVector = Icons.Filled.BookmarkAdd,
                contentDescription = null,
                tint = StudioCyan,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = strings.saveCurrentPose,
                style = MaterialTheme.typography.labelMedium,
                color = StudioCyan
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 360.dp)
                .testTag("quick_poses_grid")
        ) {
            if (savedStances.isNotEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Text(
                        text = strings.savedStances,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp)
                    )
                }
                items(savedStances, key = { it.id }) { stance ->
                    val isSelected = activePoseId == stance.id
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) StudioCyanContainer else StudioSurfaceSolid
                        ),
                        border = BorderStroke(
                            if (isSelected) 2.dp else 1.dp,
                            if (isSelected) StudioCyan else StudioBorder
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("saved_stance_${stance.id}")
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onApplySavedStance(stance.id) }
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1.2f)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AccessibilityNew,
                                    contentDescription = stance.name,
                                    tint = if (isSelected) StudioCyan else TextSecondary,
                                    modifier = Modifier.size(34.dp)
                                )
                                IconButton(
                                    onClick = { onDeleteSavedStance(stance.id) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .minimumInteractiveComponentSize()
                                        .testTag("delete_stance_${stance.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = strings.remove,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Text(
                                text = stance.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) StudioCyan else TextPrimary,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            items(PosePresets.bodyPoses, key = { it.index }) { pose ->
                StudioItemCard(
                    assetPath = pose.assetPath,
                    label = pose.name,
                    selected = activePoseId == "preset_${pose.index}",
                    testTag = "body_pose_${pose.index}",
                    onClick = { onApplyPose(pose.index) }
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
