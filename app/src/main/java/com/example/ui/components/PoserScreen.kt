package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.engine.PoserGLView
import com.example.i18n.Localization
import com.example.model.ManipulatorMode
import com.example.model.SelectedPart
import com.example.model.SideMenu
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.PoserUiState
import com.example.viewmodel.PoserViewModel

@Composable
fun PoserScreen(
    uiState: PoserUiState,
    viewModel: PoserViewModel
) {
    val strings = Localization.forLanguage(uiState.language)

    BackHandler(enabled = uiState.previewMode || uiState.sideMenu != SideMenu.NONE) {
        if (uiState.previewMode) {
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
                    onSelectCameraPreset = viewModel::setCameraPreset,
                    onToggleLock = viewModel::toggleLockSelected,
                    onToggleManipulator = viewModel::toggleManipulatorMode,
                    onDuplicate = viewModel::duplicateSelected,
                    onRemove = viewModel::removeSelected
                )

                // Floating Left Side Menu Panel
                AnimatedVisibility(
                    visible = uiState.sideMenu != SideMenu.NONE,
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
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                IconButton(
                                    onClick = viewModel::closeSideMenu,
                                    modifier = Modifier
                                        .size(32.dp)
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
                                SideMenu.POSE_PRESET_MENU -> if (uiState.selectedPart == SelectedPart.SKEL_MODEL) {
                                    PosePresetMenuContent(
                                        strings = strings,
                                        onApplyPose = viewModel::applyBodyPose
                                    )
                                }
                                SideMenu.HAND_MENU -> if (uiState.selectedPart == SelectedPart.SKEL_MODEL) {
                                    HandPoseMenuContent(
                                        strings = strings,
                                        leftSelected = uiState.handLeftSelected,
                                        onSelectSide = viewModel::setHandSide,
                                        onApplyHandPose = viewModel::applyHandPose
                                    )
                                }
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
                                        onCommitHistory = viewModel::commitHistorySnapshot
                                    )
                                }
                                SideMenu.PERSPECTIVE_MENU -> PerspectiveMenuContent(
                                    fovDeg = uiState.camera.fovDeg,
                                    strings = strings,
                                    onFovChange = viewModel::setPerspectiveFov
                                )
                                SideMenu.BRIGHTNESS_MENU -> if (uiState.selectedPart == SelectedPart.LIGHT) {
                                    BrightnessMenuContent(
                                        brightness = uiState.light.brightness,
                                        giStrength = uiState.light.giStrength,
                                        strings = strings,
                                        onBrightnessChange = viewModel::setBrightness,
                                        onGiStrengthChange = viewModel::setGiStrength
                                    )
                                }
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
                                        onCommitHistory = viewModel::commitHistorySnapshot
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
                                SideMenu.NONE -> Unit
                            }
                        }
                    }
                }
            }
        }

        // Bottom Context / Preview Pill
        val statusText = buildStatusLabel(uiState, strings)
        if (uiState.previewMode || statusText != null) {
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
