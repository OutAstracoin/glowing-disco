package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.engine.PoserGLView
import com.example.engine.ViewportProjector
import com.example.i18n.Localization
import com.example.model.ManipulatorMode
import com.example.model.SelectedPart
import com.example.model.SideMenu
import com.example.model.Vec3
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.PoserUiState
import com.example.viewmodel.PoserViewModel
import kotlin.math.roundToInt

@Composable
fun PoserScreen(
    uiState: PoserUiState,
    viewModel: PoserViewModel
) {
    val strings = Localization.forLanguage(uiState.language)
    val viewportProjector = remember { ViewportProjector() }
    var activePropDrag by remember { mutableStateOf<PropDragSession?>(null) }
    var propsDrawerTopY by remember { mutableFloatStateOf(Float.MAX_VALUE) }

    BackHandler(enabled = uiState.showComposePlaceholderViewer || uiState.previewMode || uiState.sideMenu != SideMenu.NONE) {
        if (uiState.showComposePlaceholderViewer) {
            viewModel.closeComposePlaceholderViewer()
        } else if (uiState.previewMode) {
            viewModel.disablePreviewMode()
        } else if (uiState.sideMenu != SideMenu.NONE) {
            viewModel.closeSideMenu()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Full-bleed OpenGL ES 3.0 3D Posing Viewport
        PoserGLView(
            renderSnapshot = uiState.toRenderSnapshot(),
            modifier = Modifier.fillMaxSize(),
            projector = viewportProjector,
            onInitProgress = viewModel::onEngineInitProgress,
            onTapHit = viewModel::onTapHit,
            onOrbitCamera = viewModel::orbitCamera,
            onZoomCamera = viewModel::zoomCamera,
            onPanCamera = viewModel::panCamera,
            onGizmoDragStart = viewModel::onGizmoDragStart,
            onGizmoDrag = viewModel::onGizmoDrag,
            onGizmoDragEnd = viewModel::onGizmoDragEnd
        )

        // Top Floating Studio Toolbar (hidden in Preview Mode)
        AnimatedVisibility(
            visible = !uiState.previewMode,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .fillMaxWidth()
        ) {
            Column {
                TopToolbar(
                    uiState = uiState,
                    strings = strings,
                    onToggleMenu = viewModel::toggleMenu,
                    onToggleHandMode = viewModel::toggleHandMode,
                    onEnablePreview = viewModel::enablePreviewMode,
                    onUndo = viewModel::undo,
                    onRedo = viewModel::redo,
                    onToggleGrid = viewModel::toggleGrid,
                    onToggleSky = viewModel::toggleSky,
                    onToggleSportswear = viewModel::toggleSportswear,
                    onChangeLanguage = viewModel::setLanguage,
                    onFrameSelected = viewModel::frameSelected,
                    onSelectCameraPreset = { preset -> viewModel.setCameraPreset(preset) },
                    onToggleLock = viewModel::toggleLockSelected,
                    onToggleManipulator = viewModel::toggleManipulatorMode,
                    onDuplicate = viewModel::duplicateSelected,
                    onRemove = viewModel::removeSelected,
                    onTogglePlaceholder3D = viewModel::toggleComposePlaceholderViewer
                )

                // Floating Left Side Menu Panel
                AnimatedVisibility(
                    visible = uiState.sideMenu != SideMenu.NONE && uiState.sideMenu != SideMenu.PROPS_MENU,
                    enter = slideInHorizontally(initialOffsetX = { -it / 2 }) + fadeIn(),
                    exit = slideOutHorizontally(targetOffsetX = { -it / 2 }) + fadeOut()
                ) {
                    Surface(
                        color = StudioSurface,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, StudioBorder),
                        modifier = Modifier
                            .padding(start = 10.dp, top = 4.dp, bottom = 68.dp)
                            .widthIn(min = 216.dp, max = 264.dp)
                            .navigationBarsPadding()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    IconButton(
                                        onClick = viewModel::undo,
                                        enabled = uiState.canUndo,
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("panel_undo_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Undo,
                                            contentDescription = stringResource(R.string.cd_undo),
                                            tint = if (uiState.canUndo) StudioCyan else TextSecondary.copy(alpha = 0.35f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = viewModel::redo,
                                        enabled = uiState.canRedo,
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("panel_redo_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Redo,
                                            contentDescription = stringResource(R.string.cd_redo),
                                            tint = if (uiState.canRedo) StudioCyan else TextSecondary.copy(alpha = 0.35f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    val historyHint = uiState.lastUndoLabel
                                        ?: uiState.lastRedoLabel?.let { "${strings.redo}: $it" }
                                    if (!historyHint.isNullOrBlank()) {
                                        Text(
                                            text = historyHint,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.padding(end = 4.dp)
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = viewModel::closeSideMenu,
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("close_side_menu_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = stringResource(R.string.cd_close_menu),
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            when (uiState.sideMenu) {
                                SideMenu.ADD_MENU -> AddObjectMenuContent(
                                    strings = strings,
                                    onAddCharacter = viewModel::addModel,
                                    onAddProp = viewModel::addProp
                                )
                                SideMenu.POSE_PRESET_MENU -> PosePresetMenuContent(
                                    strings = strings,
                                    savedStances = uiState.savedStances,
                                    activePoseId = uiState.activePoseId,
                                    onApplyPose = viewModel::applyBodyPose,
                                    onSaveCurrentStance = viewModel::saveCurrentStance,
                                    onApplySavedStance = viewModel::applySavedStance,
                                    onDeleteSavedStance = viewModel::deleteSavedStance
                                )
                                SideMenu.BODY_SHAPE_MENU -> BodyShapePanel(
                                    uiState = uiState,
                                    strings = strings,
                                    onApplyPreset = viewModel::applyBodyShapePreset,
                                    onSelectMeshStyle = viewModel::setBodyMeshStyle,
                                    onProportionsChange = viewModel::setBodyProportions,
                                    onResetBodyShape = viewModel::resetBodyShape,
                                    onBeginHistory = viewModel::beginHistoryBatch,
                                    onEndHistory = viewModel::endHistoryBatch
                                )
                                SideMenu.HAND_MENU -> HandControlsPanel(
                                    uiState = uiState,
                                    strings = strings,
                                    onSelectSide = viewModel::setHandSide,
                                    onApplyHandPose = viewModel::applyHandPose,
                                    onFingerStateChange = viewModel::setHandFingerState,
                                    onWristRotationChange = viewModel::setJointRotation,
                                    onMirrorHand = viewModel::mirrorHand,
                                    onResetHand = viewModel::resetHand,
                                    onBeginHistory = viewModel::beginHistoryBatch,
                                    onEndHistory = viewModel::endHistoryBatch
                                )
                                SideMenu.SWAP_MODEL_MENU -> if (uiState.selectedPart == SelectedPart.SKEL_MODEL) {
                                    ReplaceModelMenuContent(
                                        strings = strings,
                                        onReplaceModel = viewModel::changeModel
                                    )
                                }
                                SideMenu.TRANSFORM_MENU -> if (uiState.selectedPart == SelectedPart.SKEL_MODEL || uiState.selectedPart == SelectedPart.MODEL) {
                                    TransformMenuContent(
                                        uiState = uiState,
                                        strings = strings,
                                        onPositionChange = viewModel::setObjectPosition,
                                        onCharScaleChange = viewModel::setCharacterScale,
                                        onPropScaleChange = viewModel::setPropScale,
                                        onBeginHistory = viewModel::beginHistoryBatch,
                                        onEndHistory = viewModel::endHistoryBatch
                                    )
                                }
                                SideMenu.CAMERA_MENU -> CameraControlPanel(
                                    camera = uiState.camera,
                                    strings = strings,
                                    onSelectProjection = viewModel::setCameraProjection,
                                    onSelectPreset = viewModel::setCameraPreset,
                                    onFovChange = viewModel::setPerspectiveFov,
                                    onDistanceChange = viewModel::setCameraDistance,
                                    onFrameSelected = viewModel::frameSelected,
                                    onResetCamera = viewModel::resetCamera
                                )
                                SideMenu.BRIGHTNESS_MENU -> BrightnessMenuContent(
                                    brightness = uiState.light.brightness,
                                    colorTemperatureK = uiState.light.colorTemperatureK,
                                    giStrength = uiState.light.giStrength,
                                    azimuthDeg = uiState.light.azimuthDeg,
                                    elevationDeg = uiState.light.elevationDeg,
                                    strings = strings,
                                    onBrightnessChange = viewModel::setBrightness,
                                    onColorTempChange = viewModel::setColorTemperature,
                                    onColorTempPreset = viewModel::applyColorTempPreset,
                                    onGiStrengthChange = viewModel::setGiStrength,
                                    onDirectionChange = viewModel::setLightDirection,
                                    onBeginHistory = viewModel::beginHistoryBatch,
                                    onEndHistory = viewModel::endHistoryBatch
                                )
                                SideMenu.MATERIAL_MENU -> if (
                                    uiState.selectedPart == SelectedPart.MODEL ||
                                    uiState.selectedPart == SelectedPart.SKEL_MODEL ||
                                    uiState.selectedPart == SelectedPart.HEAD ||
                                    uiState.selectedPart == SelectedPart.HAIR
                                ) {
                                    MaterialMenuContent(
                                        uiState = uiState,
                                        strings = strings,
                                        onToggleBodyHair = viewModel::setMaterialEditBody,
                                        onColorChange = viewModel::updateSelectedColor,
                                        onBeginHistory = viewModel::beginHistoryBatch,
                                        onEndHistory = viewModel::endHistoryBatch,
                                        onCommitSwatchHistory = viewModel::commitHistorySnapshot
                                    )
                                }
                                SideMenu.HAIR_MENU -> if (
                                    uiState.selectedPart == SelectedPart.SKEL_MODEL ||
                                    uiState.selectedPart == SelectedPart.HEAD ||
                                    uiState.selectedPart == SelectedPart.HAIR
                                ) {
                                    HairMenuContent(
                                        strings = strings,
                                        onSelectHair = viewModel::selectHair
                                    )
                                }
                                SideMenu.JOINT_MENU -> if (
                                    uiState.selectedPart == SelectedPart.SKEL_MODEL ||
                                    uiState.selectedPart == SelectedPart.CONTROL ||
                                    uiState.selectedPart == SelectedPart.HAND ||
                                    uiState.selectedPart == SelectedPart.HEAD ||
                                    uiState.selectedPart == SelectedPart.HAIR
                                ) {
                                    JointControlsMenuContent(
                                        uiState = uiState,
                                        strings = strings,
                                        onJointRotationChange = viewModel::setJointRotation,
                                        onResetJoint = viewModel::resetJoint,
                                        onResetAll = viewModel::resetAllJoints,
                                        onBeginHistory = viewModel::beginHistoryBatch,
                                        onEndHistory = viewModel::endHistoryBatch
                                    )
                                }
                                SideMenu.NONE, SideMenu.PROPS_MENU -> Unit
                            }
                        }
                    }
                }
            }
        }

        // Bottom Drag-and-Drop Props Drawer
        AnimatedVisibility(
            visible = !uiState.previewMode && uiState.sideMenu == SideMenu.PROPS_MENU,
            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 8.dp)
                .onGloballyPositioned { coords ->
                    propsDrawerTopY = coords.positionInRoot().y
                }
        ) {
            PropsDrawer(
                strings = strings,
                onClose = viewModel::closeSideMenu,
                onTapProp = viewModel::addProp,
                onDragPropStart = { prop, pos ->
                    activePropDrag = PropDragSession(propType = prop, screenPosition = pos)
                },
                onDragPropMove = { pos ->
                    activePropDrag = activePropDrag?.copy(screenPosition = pos)
                },
                onDragPropEnd = { prop, pos ->
                    if (pos.y < propsDrawerTopY) {
                        val ground = viewportProjector.projectScreenToGround?.invoke(pos.x, pos.y) ?: Vec3()
                        viewModel.dropPropAt(prop, ground)
                    }
                    activePropDrag = null
                },
                onDragPropCancel = {
                    activePropDrag = null
                }
            )
        }

        // Bottom Context / Preview Pill
        val statusText = buildStatusLabel(uiState, strings)
        val showStatusPill = uiState.previewMode || (statusText != null && uiState.sideMenu != SideMenu.PROPS_MENU)
        if (showStatusPill) {
            Surface(
                color = StudioSurface,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, StudioBorder),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 12.dp)
                    .testTag("studio_status_pill")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    val isLocked = uiState.selectedCharacter?.locked == true || uiState.selectedProp?.locked == true
                    if (!uiState.previewMode && isLocked) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = StudioAmber,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = if (uiState.previewMode) strings.tapToExitPreview else statusText.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (uiState.previewMode) StudioCyan else TextPrimary
                    )
                }
            }
        }

        // Pure Compose 3D Geometric Placeholder Viewer Dialog
        if (uiState.showComposePlaceholderViewer) {
            ComposePlaceholder3DDialog(
                strings = strings,
                onDismiss = viewModel::closeComposePlaceholderViewer
            )
        }

        // Polished Asset & Engine Download Progress Overlay
        EngineLoadingOverlay(
            visible = uiState.isLoadingOverlayVisible,
            progress = uiState.loadingProgress,
            strings = strings
        )

        // Floating Drag Ghost when dragging a prop from the Props Drawer onto the 3D scene
        val dragSession = activePropDrag
        if (dragSession != null) {
            PropDragGhost(
                session = dragSession,
                isOverScene = dragSession.screenPosition.y < propsDrawerTopY,
                strings = strings,
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = (dragSession.screenPosition.x - 160).roundToInt(),
                            y = (dragSession.screenPosition.y - 110).roundToInt()
                        )
                    }
                    .testTag("prop_drag_ghost")
            )
        }
    }
}

private fun buildStatusLabel(uiState: PoserUiState, strings: com.example.i18n.StudioStrings): String? {
    val modeLabel = if (uiState.manipulatorMode == ManipulatorMode.ROTATE) strings.rotate else strings.translate
    return when (uiState.selectedPart) {
        SelectedPart.NOTHING -> null
        SelectedPart.LIGHT -> "${strings.lightLabel} • $modeLabel"
        SelectedPart.MODEL -> {
            val name = uiState.selectedProp?.type?.label(strings) ?: return null
            "$name • $modeLabel"
        }
        SelectedPart.SKEL_MODEL -> {
            val name = uiState.selectedCharacter?.type?.label(strings) ?: return null
            "$name • $modeLabel"
        }
        SelectedPart.HEAD, SelectedPart.HAIR -> {
            val name = uiState.selectedCharacter?.type?.label(strings) ?: return null
            "$name • ${strings.hair} / Head"
        }
        SelectedPart.CONTROL, SelectedPart.HAND, SelectedPart.FINGER -> {
            val jointName = uiState.selectedJoint?.label ?: "Joint"
            "$jointName • ${strings.rotate}"
        }
    }
}
