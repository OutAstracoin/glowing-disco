package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BackHand
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FaceRetouchingNatural
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.i18n.AppLanguage
import com.example.i18n.StudioStrings
import com.example.model.CameraPreset
import com.example.model.ManipulatorMode
import com.example.model.SelectedPart
import com.example.model.SideMenu
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioCyanContainer
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceSolid
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.PoserUiState

@Composable
fun TopToolbar(
    uiState: PoserUiState,
    strings: StudioStrings,
    onToggleMenu: (SideMenu) -> Unit,
    onToggleHandMode: () -> Unit,
    onEnablePreview: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onToggleGrid: () -> Unit,
    onToggleSky: () -> Unit,
    onToggleSportswear: () -> Unit,
    onChangeLanguage: (AppLanguage) -> Unit,
    onFrameSelected: () -> Unit,
    onSelectCameraPreset: (CameraPreset) -> Unit,
    onToggleLock: () -> Unit,
    onToggleManipulator: () -> Unit,
    onDuplicate: () -> Unit,
    onRemove: () -> Unit,
    onTogglePlaceholder3D: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLocked = uiState.selectedCharacter?.locked == true || uiState.selectedProp?.locked == true
    val isRotateMode = uiState.manipulatorMode == ManipulatorMode.ROTATE

    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Add group
        ToolbarGroup {
            ToolButtonItem(
                icon = Icons.Filled.Add,
                label = strings.add,
                contentDesc = stringResource(R.string.cd_add_object),
                active = uiState.sideMenu == SideMenu.ADD_MENU,
                testTag = "tool_add_button",
                onClick = { onToggleMenu(SideMenu.ADD_MENU) }
            )
        }

        // Preview group
        ToolbarGroup {
            ToolButtonItem(
                icon = Icons.Filled.CameraAlt,
                label = strings.preview,
                contentDesc = stringResource(R.string.cd_preview_mode),
                testTag = "tool_preview_button",
                onClick = onEnablePreview
            )
        }

        // Undo / Redo group
        ToolbarGroup {
            ToolButtonItem(
                icon = Icons.AutoMirrored.Filled.Undo,
                label = strings.undo,
                contentDesc = stringResource(R.string.cd_undo),
                enabled = uiState.canUndo,
                badgeCount = uiState.undoCount,
                testTag = "tool_undo_button",
                onClick = onUndo
            )
            ToolDivider()
            ToolButtonItem(
                icon = Icons.AutoMirrored.Filled.Redo,
                label = strings.redo,
                contentDesc = stringResource(R.string.cd_redo),
                enabled = uiState.canRedo,
                badgeCount = uiState.redoCount,
                testTag = "tool_redo_button",
                onClick = onRedo
            )
        }

        // Toggle Settings dropdown
        ToolbarGroup {
            var toggleExpanded by remember { mutableStateOf(false) }
            Box {
                ToolButtonItem(
                    icon = Icons.Filled.Settings,
                    label = strings.toggle,
                    contentDesc = stringResource(R.string.cd_toggle_settings),
                    active = toggleExpanded,
                    testTag = "tool_toggle_button",
                    onClick = { toggleExpanded = true }
                )
                DropdownMenu(
                    expanded = toggleExpanded,
                    onDismissRequest = { toggleExpanded = false },
                    modifier = Modifier.background(StudioSurfaceSolid)
                ) {
                    ToggleCheckMenuItem(
                        label = strings.groundGuideLines,
                        checked = uiState.showGrid,
                        testTag = "menu_toggle_grid",
                        onClick = { onToggleGrid(); toggleExpanded = false }
                    )
                    ToggleCheckMenuItem(
                        label = strings.sky,
                        checked = uiState.showSky,
                        testTag = "menu_toggle_sky",
                        onClick = { onToggleSky(); toggleExpanded = false }
                    )
                    ToggleCheckMenuItem(
                        label = strings.sportswear,
                        checked = uiState.showSportswear,
                        testTag = "menu_toggle_sportswear",
                        onClick = { onToggleSportswear(); toggleExpanded = false }
                    )
                    HorizontalDivider(color = StudioBorder)
                    Text(
                        text = strings.language,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                    AppLanguage.entries.forEach { lang ->
                        ToggleCheckMenuItem(
                            label = lang.displayName,
                            checked = uiState.language == lang,
                            testTag = "menu_lang_${lang.code}",
                            onClick = { onChangeLanguage(lang); toggleExpanded = false }
                        )
                    }
                }
            }
        }

        // View Settings dropdown & Camera Control Panel button
        ToolbarGroup {
            var viewExpanded by remember { mutableStateOf(false) }
            Box {
                ToolButtonItem(
                    icon = Icons.Filled.ViewInAr,
                    label = strings.view,
                    contentDesc = stringResource(R.string.cd_view_settings),
                    active = viewExpanded,
                    testTag = "tool_view_button",
                    onClick = { viewExpanded = true }
                )
                DropdownMenu(
                    expanded = viewExpanded,
                    onDismissRequest = { viewExpanded = false },
                    modifier = Modifier.background(StudioSurfaceSolid)
                ) {
                    DropdownMenuItem(
                        text = { Text(strings.center, color = TextPrimary) },
                        modifier = Modifier.testTag("menu_view_center"),
                        onClick = { onFrameSelected(); viewExpanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text(strings.cameraControls, color = TextPrimary) },
                        modifier = Modifier.testTag("menu_view_perspective"),
                        onClick = { onToggleMenu(SideMenu.CAMERA_MENU); viewExpanded = false }
                    )
                    HorizontalDivider(color = StudioBorder)
                    val presets = listOf(
                        CameraPreset.FRONT to strings.front,
                        CameraPreset.RIGHT to strings.sideView,
                        CameraPreset.LEFT to strings.sideLeftView,
                        CameraPreset.TOP to strings.top,
                        CameraPreset.ISOMETRIC to strings.isometric,
                        CameraPreset.BACK to strings.back,
                        CameraPreset.BOTTOM to strings.bottom
                    )
                    presets.forEach { (preset, label) ->
                        DropdownMenuItem(
                            text = { Text(label, color = TextPrimary) },
                            modifier = Modifier.testTag("menu_view_${preset.name.lowercase()}"),
                            onClick = { onSelectCameraPreset(preset); viewExpanded = false }
                        )
                    }
                }
            }
            ToolDivider()
            ToolButtonItem(
                icon = Icons.Filled.Videocam,
                label = strings.camera,
                contentDesc = stringResource(R.string.cd_camera_controls),
                active = uiState.sideMenu == SideMenu.CAMERA_MENU,
                testTag = "tool_camera_button",
                onClick = { onToggleMenu(SideMenu.CAMERA_MENU) }
            )
        }

        // Quick Poses, Body Shapes, Hands, Props & Scene Lighting direct studio shortcuts
        ToolbarGroup {
            ToolButtonItem(
                icon = Icons.Filled.AccessibilityNew,
                label = strings.preset,
                contentDesc = stringResource(R.string.cd_quick_poses),
                active = uiState.sideMenu == SideMenu.POSE_PRESET_MENU,
                testTag = "tool_quick_poses_button",
                onClick = { onToggleMenu(SideMenu.POSE_PRESET_MENU) }
            )
            ToolDivider()
            ToolButtonItem(
                icon = Icons.Filled.Person,
                label = strings.bodyShape,
                contentDesc = stringResource(R.string.cd_body_shape),
                active = uiState.sideMenu == SideMenu.BODY_SHAPE_MENU,
                testTag = "tool_body_shape_shortcut_button",
                onClick = { onToggleMenu(SideMenu.BODY_SHAPE_MENU) }
            )
            ToolDivider()
            ToolButtonItem(
                icon = Icons.Filled.BackHand,
                label = strings.hand,
                contentDesc = stringResource(R.string.cd_hand_poses),
                active = uiState.sideMenu == SideMenu.HAND_MENU,
                testTag = "tool_hands_shortcut_button",
                onClick = { onToggleMenu(SideMenu.HAND_MENU) }
            )
            ToolDivider()
            ToolButtonItem(
                icon = Icons.Filled.Chair,
                label = strings.propsDrawer,
                contentDesc = stringResource(R.string.cd_props),
                active = uiState.sideMenu == SideMenu.PROPS_MENU,
                testTag = "tool_props_button",
                onClick = { onToggleMenu(SideMenu.PROPS_MENU) }
            )
            ToolDivider()
            ToolButtonItem(
                icon = Icons.Filled.WbSunny,
                label = strings.lightLabel,
                contentDesc = stringResource(R.string.cd_lighting),
                active = uiState.sideMenu == SideMenu.BRIGHTNESS_MENU,
                testTag = "tool_lighting_button",
                onClick = { onToggleMenu(SideMenu.BRIGHTNESS_MENU) }
            )
        }

        // Compose 3D Placeholder Geometric Viewer button
        ToolbarGroup {
            ToolButtonItem(
                icon = Icons.Filled.Category,
                label = strings.placeholder3d,
                contentDesc = stringResource(R.string.cd_placeholder_3d),
                active = uiState.showComposePlaceholderViewer,
                testTag = "tool_placeholder_3d_button",
                onClick = onTogglePlaceholder3D
            )
        }

        // Contextual ObjectToolGroup matching ObjectToolGroup.js
        when (uiState.selectedPart) {
            SelectedPart.NOTHING -> Unit
            SelectedPart.CONTROL, SelectedPart.FINGER, SelectedPart.HAND -> {
                ToolbarGroup {
                    ToolButtonItem(
                        icon = Icons.Filled.Tune,
                        label = strings.joints,
                        contentDesc = stringResource(R.string.cd_joints),
                        active = uiState.sideMenu == SideMenu.JOINT_MENU,
                        testTag = "tool_joints_button",
                        onClick = { onToggleMenu(SideMenu.JOINT_MENU) }
                    )
                    ToolDivider()
                    ToolButtonItem(
                        icon = Icons.Filled.BackHand,
                        label = strings.hand,
                        contentDesc = stringResource(R.string.cd_hand_poses),
                        active = uiState.sideMenu == SideMenu.HAND_MENU,
                        testTag = "tool_hand_button",
                        onClick = onToggleHandMode
                    )
                    ToolDivider()
                    ToolButtonItem(
                        icon = Icons.Filled.Sync,
                        label = strings.rotate,
                        contentDesc = stringResource(R.string.cd_toggle_manipulator),
                        active = isRotateMode,
                        testTag = "tool_rotate_button",
                        onClick = onToggleManipulator
                    )
                }
            }
            SelectedPart.LIGHT -> {
                ToolbarGroup {
                    ToolButtonItem(
                        icon = Icons.Filled.WbSunny,
                        label = strings.brightness,
                        contentDesc = stringResource(R.string.cd_brightness),
                        active = uiState.sideMenu == SideMenu.BRIGHTNESS_MENU,
                        testTag = "tool_brightness_button",
                        onClick = { onToggleMenu(SideMenu.BRIGHTNESS_MENU) }
                    )
                    ToolDivider()
                    ToolButtonItem(
                        icon = Icons.Filled.Sync,
                        label = strings.rotate,
                        contentDesc = stringResource(R.string.cd_toggle_manipulator),
                        active = isRotateMode,
                        testTag = "tool_rotate_button",
                        onClick = onToggleManipulator
                    )
                }
            }
            SelectedPart.HEAD, SelectedPart.HAIR -> {
                ToolbarGroup {
                    ToolButtonItem(
                        icon = Icons.Filled.Palette,
                        label = strings.color,
                        contentDesc = stringResource(R.string.cd_material_color),
                        active = uiState.sideMenu == SideMenu.MATERIAL_MENU,
                        testTag = "tool_color_button",
                        onClick = { onToggleMenu(SideMenu.MATERIAL_MENU) }
                    )
                    ToolDivider()
                    ToolButtonItem(
                        icon = Icons.Filled.FaceRetouchingNatural,
                        label = strings.hair,
                        contentDesc = stringResource(R.string.cd_hair_style),
                        active = uiState.sideMenu == SideMenu.HAIR_MENU,
                        testTag = "tool_hair_button",
                        onClick = { onToggleMenu(SideMenu.HAIR_MENU) }
                    )
                    ToolDivider()
                    ToolButtonItem(
                        icon = Icons.Filled.Tune,
                        label = strings.joints,
                        contentDesc = stringResource(R.string.cd_joints),
                        active = uiState.sideMenu == SideMenu.JOINT_MENU,
                        testTag = "tool_joints_button",
                        onClick = { onToggleMenu(SideMenu.JOINT_MENU) }
                    )
                    ToolDivider()
                    ToolButtonItem(
                        icon = Icons.Filled.Sync,
                        label = strings.rotate,
                        contentDesc = stringResource(R.string.cd_toggle_manipulator),
                        active = isRotateMode,
                        testTag = "tool_rotate_button",
                        onClick = onToggleManipulator
                    )
                }
            }
            SelectedPart.MODEL -> {
                ToolbarGroup {
                    ToolButtonItem(
                        icon = Icons.Filled.Palette,
                        label = strings.color,
                        contentDesc = stringResource(R.string.cd_material_color),
                        active = uiState.sideMenu == SideMenu.MATERIAL_MENU,
                        testTag = "tool_color_button",
                        onClick = { onToggleMenu(SideMenu.MATERIAL_MENU) }
                    )
                    ToolDivider()
                    ToolButtonItem(
                        icon = if (isLocked) Icons.Filled.Lock else Icons.Filled.LockOpen,
                        label = strings.lock,
                        contentDesc = stringResource(R.string.cd_lock_object),
                        active = isLocked,
                        testTag = "tool_lock_button",
                        onClick = onToggleLock
                    )
                    ToolDivider()
                    ToolButtonItem(
                        icon = Icons.Filled.OpenWith,
                        label = strings.transform,
                        contentDesc = stringResource(R.string.cd_transform),
                        active = uiState.sideMenu == SideMenu.TRANSFORM_MENU,
                        testTag = "tool_transform_button",
                        onClick = { onToggleMenu(SideMenu.TRANSFORM_MENU) }
                    )
                    ToolDivider()
                    ToolButtonItem(
                        icon = Icons.Filled.Sync,
                        label = if (isRotateMode) strings.rotate else strings.translate,
                        contentDesc = stringResource(R.string.cd_toggle_manipulator),
                        active = isRotateMode,
                        testTag = "tool_rotate_button",
                        onClick = onToggleManipulator
                    )
                    ToolDivider()
                    ToolButtonItem(
                        icon = Icons.Filled.ContentCopy,
                        label = strings.duplicate,
                        contentDesc = stringResource(R.string.cd_duplicate),
                        testTag = "tool_duplicate_button",
                        onClick = onDuplicate
                    )
                    ToolDivider()
                    ToolButtonItem(
                        icon = Icons.Filled.Delete,
                        label = strings.remove,
                        contentDesc = stringResource(R.string.cd_remove),
                        testTag = "tool_remove_button",
                        onClick = onRemove
                    )
                }
            }
            SelectedPart.SKEL_MODEL -> {
                ToolbarGroup {
                    ToolButtonItem(
                        icon = Icons.Filled.Palette,
                        label = strings.color,
                        contentDesc = stringResource(R.string.cd_material_color),
                        active = uiState.sideMenu == SideMenu.MATERIAL_MENU,
                        testTag = "tool_color_button",
                        onClick = { onToggleMenu(SideMenu.MATERIAL_MENU) }
                    )
                    ToolDivider()
                    ToolButtonItem(
                        icon = Icons.Filled.FaceRetouchingNatural,
                        label = strings.hair,
                        contentDesc = stringResource(R.string.cd_hair_style),
                        active = uiState.sideMenu == SideMenu.HAIR_MENU,
                        testTag = "tool_hair_button",
                        onClick = { onToggleMenu(SideMenu.HAIR_MENU) }
                    )
                    ToolDivider()
                    ToolButtonItem(
                        icon = if (isLocked) Icons.Filled.Lock else Icons.Filled.LockOpen,
                        label = strings.lock,
                        contentDesc = stringResource(R.string.cd_lock_object),
                        active = isLocked,
                        testTag = "tool_lock_button",
                        onClick = onToggleLock
                    )
                    ToolDivider()
                    ToolButtonItem(
                        icon = Icons.Filled.OpenWith,
                        label = strings.transform,
                        contentDesc = stringResource(R.string.cd_transform),
                        active = uiState.sideMenu == SideMenu.TRANSFORM_MENU,
                        testTag = "tool_transform_button",
                        onClick = { onToggleMenu(SideMenu.TRANSFORM_MENU) }
                    )
                    ToolDivider()
                    ToolButtonItem(
                        icon = Icons.Filled.Sync,
                        label = if (isRotateMode) strings.rotate else strings.translate,
                        contentDesc = stringResource(R.string.cd_toggle_manipulator),
                        active = isRotateMode,
                        testTag = "tool_rotate_button",
                        onClick = onToggleManipulator
                    )
                    ToolDivider()
                    ToolButtonItem(
                        icon = Icons.Filled.AccessibilityNew,
                        label = strings.preset,
                        contentDesc = stringResource(R.string.cd_pose_presets),
                        active = uiState.sideMenu == SideMenu.POSE_PRESET_MENU,
                        testTag = "tool_preset_button",
                        onClick = { onToggleMenu(SideMenu.POSE_PRESET_MENU) }
                    )
                    ToolDivider()
                    ToolButtonItem(
                        icon = Icons.Filled.Person,
                        label = strings.bodyShape,
                        contentDesc = stringResource(R.string.cd_body_shape),
                        active = uiState.sideMenu == SideMenu.BODY_SHAPE_MENU,
                        testTag = "tool_body_shape_button",
                        onClick = { onToggleMenu(SideMenu.BODY_SHAPE_MENU) }
                    )
                    ToolDivider()
                    ToolButtonItem(
                        icon = Icons.Filled.Tune,
                        label = strings.joints,
                        contentDesc = stringResource(R.string.cd_joints),
                        active = uiState.sideMenu == SideMenu.JOINT_MENU,
                        testTag = "tool_joints_button",
                        onClick = { onToggleMenu(SideMenu.JOINT_MENU) }
                    )
                    ToolDivider()
                    ToolButtonItem(
                        icon = Icons.Filled.BackHand,
                        label = strings.hand,
                        contentDesc = stringResource(R.string.cd_hand_poses),
                        active = uiState.sideMenu == SideMenu.HAND_MENU,
                        testTag = "tool_hand_button",
                        onClick = onToggleHandMode
                    )
                    ToolDivider()
                    ToolButtonItem(
                        icon = Icons.Filled.ContentCopy,
                        label = strings.duplicate,
                        contentDesc = stringResource(R.string.cd_duplicate),
                        testTag = "tool_duplicate_button",
                        onClick = onDuplicate
                    )
                    ToolDivider()
                    ToolButtonItem(
                        icon = Icons.Filled.SwapHoriz,
                        label = strings.replace,
                        contentDesc = stringResource(R.string.cd_replace_model),
                        active = uiState.sideMenu == SideMenu.SWAP_MODEL_MENU,
                        testTag = "tool_replace_button",
                        onClick = { onToggleMenu(SideMenu.SWAP_MODEL_MENU) }
                    )
                    ToolDivider()
                    ToolButtonItem(
                        icon = Icons.Filled.Delete,
                        label = strings.remove,
                        contentDesc = stringResource(R.string.cd_remove),
                        testTag = "tool_remove_button",
                        onClick = onRemove
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolbarGroup(content: @Composable () -> Unit) {
    Surface(
        color = StudioSurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, StudioBorder)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}

@Composable
private fun ToolDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(28.dp)
            .background(StudioBorder)
    )
}

@Composable
private fun ToolButtonItem(
    icon: ImageVector,
    label: String,
    contentDesc: String,
    testTag: String,
    active: Boolean = false,
    enabled: Boolean = true,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    val bg = if (active) StudioCyanContainer else androidx.compose.ui.graphics.Color.Transparent
    val fg = when {
        !enabled -> TextMuted
        active -> StudioCyan
        else -> TextPrimary
    }

    Box(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .width(56.dp)
            .height(52.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .alpha(if (enabled) 1f else 0.45f)
            .testTag(testTag)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDesc,
                tint = fg,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = fg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (badgeCount > 0) {
            Text(
                text = badgeCount.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = StudioCyan,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 2.dp)
            )
        }
    }
}

@Composable
private fun ToggleCheckMenuItem(
    label: String,
    checked: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(label, color = if (checked) StudioCyan else TextSecondary) },
        trailingIcon = if (checked) {
            {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = StudioCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else null,
        modifier = Modifier.testTag(testTag),
        onClick = onClick
    )
}
