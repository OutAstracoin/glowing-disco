package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.Math3D
import com.example.engine.PickHit
import com.example.engine.StudioRenderSnapshot
import com.example.i18n.AppLanguage
import com.example.model.BodyMeshStyle
import com.example.model.BodyProportions
import com.example.model.CameraPreset
import com.example.model.CameraProjection
import com.example.model.CameraSettings
import com.example.model.CharacterEntity
import com.example.model.CharacterType
import com.example.model.HairStyle
import com.example.model.HandFingerState
import com.example.model.JointId
import com.example.model.LightSettings
import com.example.model.ManipulatorMode
import com.example.model.PosePresets
import com.example.model.PropEntity
import com.example.model.PropType
import com.example.model.RgbColor
import com.example.model.SavedStance
import com.example.model.SceneSnapshot
import com.example.model.SelectedPart
import com.example.model.SideMenu
import com.example.model.Vec3
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class PoserUiState(
    val characters: List<CharacterEntity> = listOf(
        CharacterEntity(
            id = "char_initial",
            type = CharacterType.ANIME_KYO_FEMALE,
            hairStyle = HairStyle.MIKU,
            hairColor = RgbColor(0.24f, 0.76f, 0.82f),
            jointRotations = PosePresets.bodyPoses[0].jointRotations
        )
    ),
    val props: List<PropEntity> = emptyList(),
    val light: LightSettings = LightSettings(),
    val camera: CameraSettings = CameraSettings(),
    val selectedEntityId: String? = "char_initial",
    val selectedJoint: JointId? = JointId.PELVIS,
    val selectedPart: SelectedPart = SelectedPart.SKEL_MODEL,
    val sideMenu: SideMenu = SideMenu.NONE,
    val previewMode: Boolean = false,
    val manipulatorMode: ManipulatorMode = ManipulatorMode.ROTATE,
    val activeGizmoAxis: Int = -1,
    val showGrid: Boolean = true,
    val showSky: Boolean = true,
    val showSportswear: Boolean = true,
    val language: AppLanguage = AppLanguage.EN,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val undoCount: Int = 0,
    val redoCount: Int = 0,
    val lastUndoLabel: String? = null,
    val lastRedoLabel: String? = null,
    val handLeftSelected: Boolean = true,
    val materialEditBody: Boolean = true,
    val loadingProgress: Float = 0.0f,
    val isLoadingOverlayVisible: Boolean = true,
    val showComposePlaceholderViewer: Boolean = false,
    val savedStances: List<SavedStance> = emptyList(),
    val activePoseId: String? = "preset_0"
) {
    val selectedCharacter: CharacterEntity?
        get() = characters.firstOrNull { it.id == selectedEntityId }

    val selectedProp: PropEntity?
        get() = props.firstOrNull { it.id == selectedEntityId }

    fun toRenderSnapshot(): StudioRenderSnapshot = StudioRenderSnapshot(
        characters = characters,
        props = props,
        light = light,
        camera = camera,
        showGrid = showGrid,
        showSky = showSky,
        showSportswear = showSportswear,
        previewMode = previewMode,
        selectedEntityId = selectedEntityId,
        selectedJoint = selectedJoint,
        selectedPart = selectedPart,
        manipulatorMode = manipulatorMode,
        activeGizmoAxis = activeGizmoAxis
    )
}

class PoserViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PoserUiState())
    val uiState: StateFlow<PoserUiState> = _uiState.asStateFlow()

    private val undoStack = ArrayDeque<SceneSnapshot>()
    private val redoStack = ArrayDeque<SceneSnapshot>()
    private var gizmoHistoryCaptured = false
    private var inContinuousBatch = false

    @Volatile
    private var reportedEngineProgress = 0.1f

    init {
        viewModelScope.launch {
            var display = 0.0f
            while (display < 1.0f) {
                val target = reportedEngineProgress.coerceIn(0.1f, 1.0f)
                display = if (display < target) {
                    (display + 0.08f).coerceAtMost(target)
                } else if (reportedEngineProgress >= 1.0f) {
                    1.0f
                } else {
                    (display + 0.02f).coerceAtMost(0.92f)
                }
                _uiState.update { it.copy(loadingProgress = display) }
                if (display >= 1.0f && reportedEngineProgress >= 1.0f) break
                delay(45)
            }
            delay(260)
            _uiState.update { it.copy(loadingProgress = 1.0f, isLoadingOverlayVisible = false) }
        }
    }

    fun onEngineInitProgress(progress: Float) {
        reportedEngineProgress = maxOf(reportedEngineProgress, progress.coerceIn(0f, 1f))
    }

    fun toggleComposePlaceholderViewer() {
        _uiState.update { it.copy(showComposePlaceholderViewer = !it.showComposePlaceholderViewer) }
    }

    fun closeComposePlaceholderViewer() {
        _uiState.update { it.copy(showComposePlaceholderViewer = false) }
    }

    private fun snapshotOf(s: PoserUiState, label: String = ""): SceneSnapshot = SceneSnapshot(
        characters = s.characters,
        props = s.props,
        light = s.light,
        selectedEntityId = s.selectedEntityId,
        selectedJoint = s.selectedJoint,
        selectedPart = s.selectedPart,
        activePoseId = s.activePoseId,
        actionLabel = label
    )

    private fun PoserUiState.withHistoryMeta(): PoserUiState = copy(
        canUndo = undoStack.isNotEmpty(),
        canRedo = redoStack.isNotEmpty(),
        undoCount = undoStack.size,
        redoCount = redoStack.size,
        lastUndoLabel = undoStack.lastOrNull()?.actionLabel?.takeIf { it.isNotEmpty() },
        lastRedoLabel = redoStack.lastOrNull()?.actionLabel?.takeIf { it.isNotEmpty() }
    )

    private fun pushHistory(s: PoserUiState, label: String = "") {
        if (undoStack.size >= 50) undoStack.removeFirst()
        undoStack.addLast(snapshotOf(s, label))
        redoStack.clear()
    }

    private fun applySnapshot(snap: SceneSnapshot) {
        _uiState.update { cur ->
            cur.copy(
                characters = snap.characters,
                props = snap.props,
                light = snap.light,
                selectedEntityId = snap.selectedEntityId,
                selectedJoint = snap.selectedJoint,
                selectedPart = snap.selectedPart,
                activePoseId = snap.activePoseId
            ).withHistoryMeta()
        }
    }

    fun beginHistoryBatch(label: String = "") {
        if (inContinuousBatch) return
        inContinuousBatch = true
        _uiState.update { cur ->
            pushHistory(cur, label)
            cur.withHistoryMeta()
        }
    }

    fun endHistoryBatch() {
        inContinuousBatch = false
    }

    fun undo() {
        endHistoryBatch()
        gizmoHistoryCaptured = false
        if (undoStack.isEmpty()) return
        val cur = _uiState.value
        val prev = undoStack.removeLast()
        redoStack.addLast(snapshotOf(cur, prev.actionLabel))
        applySnapshot(prev)
    }

    fun redo() {
        endHistoryBatch()
        gizmoHistoryCaptured = false
        if (redoStack.isEmpty()) return
        val cur = _uiState.value
        val next = redoStack.removeLast()
        undoStack.addLast(snapshotOf(cur, next.actionLabel))
        applySnapshot(next)
    }

    fun enablePreviewMode() {
        endHistoryBatch()
        _uiState.update { it.copy(previewMode = true, sideMenu = SideMenu.NONE) }
    }

    fun disablePreviewMode() {
        _uiState.update { it.copy(previewMode = false) }
    }

    fun toggleMenu(menu: SideMenu) {
        endHistoryBatch()
        _uiState.update { cur ->
            val next = if (cur.sideMenu == menu) SideMenu.NONE else menu
            cur.copy(sideMenu = next)
        }
    }

    fun closeSideMenu() {
        endHistoryBatch()
        _uiState.update { it.copy(sideMenu = SideMenu.NONE) }
    }

    fun toggleHandMode() {
        toggleMenu(SideMenu.HAND_MENU)
    }

    fun setHandSide(left: Boolean) {
        _uiState.update { it.copy(handLeftSelected = left) }
    }

    fun setMaterialEditBody(body: Boolean) {
        _uiState.update { it.copy(materialEditBody = body) }
    }

    fun onTapHit(hit: PickHit) {
        _uiState.update { cur ->
            val exitedPreview = if (cur.previewMode) false else cur.previewMode
            when (hit) {
                is PickHit.Nothing -> cur.copy(
                    previewMode = exitedPreview,
                    sideMenu = SideMenu.NONE,
                    selectedEntityId = null,
                    selectedJoint = null,
                    selectedPart = SelectedPart.NOTHING
                )
                is PickHit.Light -> cur.copy(
                    previewMode = exitedPreview,
                    sideMenu = SideMenu.NONE,
                    selectedEntityId = null,
                    selectedJoint = null,
                    selectedPart = SelectedPart.LIGHT
                )
                is PickHit.Prop -> cur.copy(
                    previewMode = exitedPreview,
                    sideMenu = SideMenu.NONE,
                    selectedEntityId = hit.entityId,
                    selectedJoint = null,
                    selectedPart = SelectedPart.MODEL
                )
                is PickHit.CharacterPart -> cur.copy(
                    previewMode = exitedPreview,
                    sideMenu = SideMenu.NONE,
                    selectedEntityId = hit.entityId,
                    selectedJoint = hit.jointId,
                    selectedPart = hit.part,
                    handLeftSelected = when (hit.jointId) {
                        JointId.L_HAND -> true
                        JointId.R_HAND -> false
                        else -> cur.handLeftSelected
                    },
                    materialEditBody = hit.part != SelectedPart.HEAD && hit.part != SelectedPart.HAIR
                )
                is PickHit.GizmoAxis -> cur.copy(previewMode = exitedPreview)
            }
        }
    }

    fun addModel(type: CharacterType) {
        endHistoryBatch()
        _uiState.update { cur ->
            pushHistory(cur, "Add Character")
            val offset = cur.characters.size * 0.55f
            val newChar = CharacterEntity(
                id = "char_${UUID.randomUUID().toString().take(8)}",
                type = type,
                position = Vec3(offset, 0f, 0f),
                hairStyle = if (type == CharacterType.MAN || type == CharacterType.ANIME_KYO_MALE) HairStyle.NARUTO else HairStyle.MIKU,
                hairColor = RgbColor(0.28f, 0.32f, 0.42f),
                jointRotations = PosePresets.bodyPoses[0].jointRotations
            )
            cur.copy(
                characters = cur.characters + newChar,
                selectedEntityId = newChar.id,
                selectedJoint = JointId.PELVIS,
                selectedPart = SelectedPart.SKEL_MODEL
            ).withHistoryMeta()
        }
    }

    fun addProp(type: PropType) {
        endHistoryBatch()
        _uiState.update { cur ->
            pushHistory(cur, "Add Prop")
            val offset = (cur.props.size % 5) * 0.55f - 0.8f
            val newProp = PropEntity(
                id = "prop_${UUID.randomUUID().toString().take(8)}",
                type = type,
                position = Vec3(offset, type.restingY, 0.35f),
                color = type.defaultColor
            )
            cur.copy(
                props = cur.props + newProp,
                selectedEntityId = newProp.id,
                selectedJoint = null,
                selectedPart = SelectedPart.MODEL,
                manipulatorMode = ManipulatorMode.TRANSLATE
            ).withHistoryMeta()
        }
    }

    fun dropPropAt(type: PropType, groundPos: Vec3) {
        endHistoryBatch()
        _uiState.update { cur ->
            pushHistory(cur, "Drop ${type.key.substringBefore('_').replaceFirstChar { it.uppercase() }}")
            val newProp = PropEntity(
                id = "prop_${UUID.randomUUID().toString().take(8)}",
                type = type,
                position = Vec3(groundPos.x, type.restingY, groundPos.z),
                color = type.defaultColor
            )
            cur.copy(
                props = cur.props + newProp,
                selectedEntityId = newProp.id,
                selectedJoint = null,
                selectedPart = SelectedPart.MODEL,
                manipulatorMode = ManipulatorMode.TRANSLATE
            ).withHistoryMeta()
        }
    }

    fun changeModel(type: CharacterType) {
        endHistoryBatch()
        _uiState.update { cur ->
            val targetId = cur.selectedEntityId ?: return@update cur
            pushHistory(cur, "Replace Model")
            cur.copy(
                characters = cur.characters.map {
                    if (it.id == targetId) {
                        it.copy(
                            type = type,
                            bodyShapePresetId = null,
                            bodyMeshStyleOverride = null,
                            bodyProportionsOverride = null
                        )
                    } else it
                }
            ).withHistoryMeta()
        }
    }

    fun duplicateSelected() {
        endHistoryBatch()
        _uiState.update { cur ->
            val selId = cur.selectedEntityId ?: return@update cur
            when (cur.selectedPart) {
                SelectedPart.MODEL -> {
                    val src = cur.props.firstOrNull { it.id == selId } ?: return@update cur
                    pushHistory(cur, "Duplicate Prop")
                    val dup = src.copy(
                        id = "prop_${UUID.randomUUID().toString().take(8)}",
                        position = src.position + Vec3(0.4f, 0f, 0.2f)
                    )
                    cur.copy(
                        props = cur.props + dup,
                        selectedEntityId = dup.id
                    ).withHistoryMeta()
                }
                SelectedPart.SKEL_MODEL, SelectedPart.HEAD, SelectedPart.HAIR, SelectedPart.CONTROL, SelectedPart.HAND -> {
                    val src = cur.characters.firstOrNull { it.id == selId } ?: return@update cur
                    pushHistory(cur, "Duplicate Character")
                    val dup = src.copy(
                        id = "char_${UUID.randomUUID().toString().take(8)}",
                        position = src.position + Vec3(0.55f, 0f, 0f)
                    )
                    cur.copy(
                        characters = cur.characters + dup,
                        selectedEntityId = dup.id,
                        selectedPart = SelectedPart.SKEL_MODEL,
                        selectedJoint = JointId.PELVIS
                    ).withHistoryMeta()
                }
                else -> cur
            }
        }
    }

    fun removeSelected() {
        endHistoryBatch()
        _uiState.update { cur ->
            val selId = cur.selectedEntityId ?: return@update cur
            pushHistory(cur, "Remove")
            val newChars = cur.characters.filterNot { it.id == selId }
            val newProps = cur.props.filterNot { it.id == selId }
            cur.copy(
                characters = newChars,
                props = newProps,
                selectedEntityId = null,
                selectedJoint = null,
                selectedPart = SelectedPart.NOTHING,
                sideMenu = SideMenu.NONE
            ).withHistoryMeta()
        }
    }

    fun toggleLockSelected() {
        endHistoryBatch()
        _uiState.update { cur ->
            val selId = cur.selectedEntityId ?: return@update cur
            pushHistory(cur, "Toggle Lock")
            cur.copy(
                characters = cur.characters.map { if (it.id == selId) it.copy(locked = !it.locked) else it },
                props = cur.props.map { if (it.id == selId) it.copy(locked = !it.locked) else it }
            ).withHistoryMeta()
        }
    }

    fun toggleManipulatorMode() {
        _uiState.update { cur ->
            val next = if (cur.manipulatorMode == ManipulatorMode.ROTATE) {
                ManipulatorMode.TRANSLATE
            } else {
                ManipulatorMode.ROTATE
            }
            cur.copy(manipulatorMode = next)
        }
    }

    fun applyBodyPose(poseIndex: Int) {
        val preset = PosePresets.bodyPoses.getOrNull(poseIndex) ?: return
        endHistoryBatch()
        _uiState.update { cur ->
            val ch = cur.selectedCharacter ?: cur.characters.firstOrNull() ?: return@update cur
            if (ch.locked) return@update cur
            pushHistory(cur, "Pose: ${preset.name}")
            cur.copy(
                selectedEntityId = ch.id,
                selectedPart = if (cur.selectedPart == SelectedPart.NOTHING || cur.selectedPart == SelectedPart.LIGHT || cur.selectedPart == SelectedPart.MODEL) {
                    SelectedPart.SKEL_MODEL
                } else {
                    cur.selectedPart
                },
                activePoseId = "preset_$poseIndex",
                characters = cur.characters.map {
                    if (it.id == ch.id) {
                        it.copy(
                            pelvisOffsetY = preset.pelvisOffsetY,
                            jointRotations = preset.jointRotations
                        )
                    } else it
                }
            ).withHistoryMeta()
        }
    }

    fun saveCurrentStance() {
        _uiState.update { cur ->
            val ch = cur.selectedCharacter ?: cur.characters.firstOrNull() ?: return@update cur
            val nextNumber = cur.savedStances.size + 1
            val stanceId = "saved_${UUID.randomUUID().toString().take(8)}"
            val stance = SavedStance(
                id = stanceId,
                name = "Stance $nextNumber",
                pelvisOffsetY = ch.pelvisOffsetY,
                jointRotations = ch.jointRotations.toMap()
            )
            cur.copy(
                savedStances = listOf(stance) + cur.savedStances,
                activePoseId = stanceId
            )
        }
    }

    fun applySavedStance(stanceId: String) {
        endHistoryBatch()
        _uiState.update { cur ->
            val stance = cur.savedStances.firstOrNull { it.id == stanceId } ?: return@update cur
            val ch = cur.selectedCharacter ?: cur.characters.firstOrNull() ?: return@update cur
            if (ch.locked) return@update cur
            pushHistory(cur, "Stance: ${stance.name}")
            cur.copy(
                selectedEntityId = ch.id,
                selectedPart = if (cur.selectedPart == SelectedPart.NOTHING || cur.selectedPart == SelectedPart.LIGHT || cur.selectedPart == SelectedPart.MODEL) {
                    SelectedPart.SKEL_MODEL
                } else {
                    cur.selectedPart
                },
                activePoseId = stance.id,
                characters = cur.characters.map {
                    if (it.id == ch.id) {
                        it.copy(
                            pelvisOffsetY = stance.pelvisOffsetY,
                            jointRotations = stance.jointRotations
                        )
                    } else it
                }
            ).withHistoryMeta()
        }
    }

    fun deleteSavedStance(stanceId: String) {
        _uiState.update { cur ->
            cur.copy(
                savedStances = cur.savedStances.filterNot { it.id == stanceId },
                activePoseId = if (cur.activePoseId == stanceId) null else cur.activePoseId
            )
        }
    }

    fun applyHandPose(poseIndex: Int, left: Boolean) {
        val preset = PosePresets.handPoses.getOrNull(poseIndex) ?: return
        endHistoryBatch()
        _uiState.update { cur ->
            val ch = cur.selectedCharacter ?: cur.characters.firstOrNull() ?: return@update cur
            if (ch.locked) return@update cur
            pushHistory(cur, "Hand: ${preset.name}")
            cur.copy(
                selectedEntityId = ch.id,
                characters = cur.characters.map {
                    if (it.id == ch.id) {
                        if (left) it.copy(leftHand = preset.fingerState) else it.copy(rightHand = preset.fingerState)
                    } else it
                }
            ).withHistoryMeta()
        }
    }

    fun setHandFingerState(left: Boolean, fingerState: HandFingerState) {
        _uiState.update { cur ->
            val ch = cur.selectedCharacter ?: cur.characters.firstOrNull() ?: return@update cur
            if (ch.locked) return@update cur
            val clamped = HandFingerState(
                thumbCurl = fingerState.thumbCurl.coerceIn(0f, 1f),
                indexCurl = fingerState.indexCurl.coerceIn(0f, 1f),
                middleCurl = fingerState.middleCurl.coerceIn(0f, 1f),
                ringCurl = fingerState.ringCurl.coerceIn(0f, 1f),
                pinkyCurl = fingerState.pinkyCurl.coerceIn(0f, 1f),
                spread = fingerState.spread.coerceIn(0f, 1f)
            )
            cur.copy(
                selectedEntityId = ch.id,
                characters = cur.characters.map {
                    if (it.id == ch.id) {
                        if (left) it.copy(leftHand = clamped) else it.copy(rightHand = clamped)
                    } else it
                }
            )
        }
    }

    fun mirrorHand(fromLeft: Boolean) {
        endHistoryBatch()
        _uiState.update { cur ->
            val ch = cur.selectedCharacter ?: cur.characters.firstOrNull() ?: return@update cur
            if (ch.locked) return@update cur
            pushHistory(cur, "Mirror Hand")
            cur.copy(
                selectedEntityId = ch.id,
                characters = cur.characters.map {
                    if (it.id == ch.id) {
                        if (fromLeft) it.copy(rightHand = it.leftHand) else it.copy(leftHand = it.rightHand)
                    } else it
                }
            ).withHistoryMeta()
        }
    }

    fun resetHand(left: Boolean) {
        endHistoryBatch()
        _uiState.update { cur ->
            val ch = cur.selectedCharacter ?: cur.characters.firstOrNull() ?: return@update cur
            if (ch.locked) return@update cur
            val wristJoint = if (left) JointId.L_HAND else JointId.R_HAND
            pushHistory(cur, "Reset Hand")
            cur.copy(
                selectedEntityId = ch.id,
                characters = cur.characters.map {
                    if (it.id == ch.id) {
                        val updatedJoints = it.jointRotations.toMutableMap().apply {
                            remove(wristJoint)
                        }
                        if (left) {
                            it.copy(leftHand = HandFingerState(), jointRotations = updatedJoints)
                        } else {
                            it.copy(rightHand = HandFingerState(), jointRotations = updatedJoints)
                        }
                    } else it
                }
            ).withHistoryMeta()
        }
    }

    fun applyBodyShapePreset(presetId: String) {
        val preset = PosePresets.bodyShapePresetById(presetId) ?: return
        endHistoryBatch()
        _uiState.update { cur ->
            val ch = cur.selectedCharacter ?: cur.characters.firstOrNull() ?: return@update cur
            if (ch.locked) return@update cur
            pushHistory(cur, "Body Shape")
            cur.copy(
                selectedEntityId = ch.id,
                characters = cur.characters.map {
                    if (it.id == ch.id) {
                        it.copy(
                            bodyShapePresetId = preset.id,
                            bodyMeshStyleOverride = null,
                            bodyProportionsOverride = null
                        )
                    } else it
                }
            ).withHistoryMeta()
        }
    }

    fun setBodyMeshStyle(style: BodyMeshStyle) {
        endHistoryBatch()
        _uiState.update { cur ->
            val ch = cur.selectedCharacter ?: cur.characters.firstOrNull() ?: return@update cur
            if (ch.locked) return@update cur
            if (ch.effectiveMeshStyle == style) return@update cur
            pushHistory(cur, "Mesh: ${style.key}")
            cur.copy(
                selectedEntityId = ch.id,
                characters = cur.characters.map {
                    if (it.id == ch.id) {
                        it.copy(bodyMeshStyleOverride = style)
                    } else it
                }
            ).withHistoryMeta()
        }
    }

    fun setBodyProportions(proportions: BodyProportions) {
        _uiState.update { cur ->
            val ch = cur.selectedCharacter ?: cur.characters.firstOrNull() ?: return@update cur
            if (ch.locked) return@update cur
            val clamped = BodyProportions(
                heightScale = proportions.heightScale.coerceIn(0.65f, 1.35f),
                shoulderWidth = proportions.shoulderWidth.coerceIn(0.65f, 1.50f),
                hipWidth = proportions.hipWidth.coerceIn(0.65f, 1.45f),
                headScale = proportions.headScale.coerceIn(0.70f, 1.55f),
                limbThickness = proportions.limbThickness.coerceIn(0.60f, 1.50f),
                chestDepth = proportions.chestDepth.coerceIn(0.65f, 1.45f)
            )
            cur.copy(
                selectedEntityId = ch.id,
                characters = cur.characters.map {
                    if (it.id == ch.id) it.copy(bodyProportionsOverride = clamped) else it
                }
            )
        }
    }

    fun resetBodyShape() {
        endHistoryBatch()
        _uiState.update { cur ->
            val ch = cur.selectedCharacter ?: cur.characters.firstOrNull() ?: return@update cur
            if (ch.locked) return@update cur
            if (ch.bodyShapePresetId == null && ch.bodyMeshStyleOverride == null && ch.bodyProportionsOverride == null) {
                return@update cur
            }
            pushHistory(cur, "Reset Body Shape")
            cur.copy(
                selectedEntityId = ch.id,
                characters = cur.characters.map {
                    if (it.id == ch.id) {
                        it.copy(
                            bodyShapePresetId = null,
                            bodyMeshStyleOverride = null,
                            bodyProportionsOverride = null
                        )
                    } else it
                }
            ).withHistoryMeta()
        }
    }

    fun selectHair(style: HairStyle) {
        endHistoryBatch()
        _uiState.update { cur ->
            val selId = cur.selectedEntityId ?: return@update cur
            pushHistory(cur, "Hair Style")
            cur.copy(
                characters = cur.characters.map {
                    if (it.id == selId) it.copy(hairStyle = style) else it
                }
            ).withHistoryMeta()
        }
    }

    fun updateSelectedColor(color: RgbColor) {
        _uiState.update { cur ->
            val selId = cur.selectedEntityId ?: return@update cur
            if (cur.selectedPart == SelectedPart.MODEL) {
                cur.copy(
                    props = cur.props.map { if (it.id == selId) it.copy(color = color) else it }
                )
            } else {
                val editHair = (cur.selectedPart == SelectedPart.HEAD || cur.selectedPart == SelectedPart.HAIR) || !cur.materialEditBody
                cur.copy(
                    characters = cur.characters.map {
                        if (it.id == selId) {
                            if (editHair) it.copy(hairColor = color) else it.copy(bodyColor = color)
                        } else it
                    }
                )
            }
        }
    }

    fun commitHistorySnapshot(label: String = "") {
        pushHistory(_uiState.value, label)
        _uiState.update { it.withHistoryMeta() }
    }

    fun setObjectPosition(pos: Vec3) {
        _uiState.update { cur ->
            val selId = cur.selectedEntityId ?: return@update cur
            if (cur.selectedPart == SelectedPart.MODEL) {
                val prop = cur.props.firstOrNull { it.id == selId } ?: return@update cur
                if (prop.locked) return@update cur
                cur.copy(props = cur.props.map { if (it.id == selId) it.copy(position = pos) else it })
            } else {
                val ch = cur.characters.firstOrNull { it.id == selId } ?: return@update cur
                if (ch.locked) return@update cur
                cur.copy(characters = cur.characters.map { if (it.id == selId) it.copy(position = pos) else it })
            }
        }
    }

    fun setCharacterScale(scale: Float) {
        _uiState.update { cur ->
            val selId = cur.selectedEntityId ?: return@update cur
            val ch = cur.characters.firstOrNull { it.id == selId } ?: return@update cur
            if (ch.locked) return@update cur
            cur.copy(
                characters = cur.characters.map {
                    if (it.id == selId) it.copy(scale = scale.coerceIn(0.2f, 3.0f)) else it
                }
            )
        }
    }

    fun setJointRotation(jointId: JointId, eulerDeg: Vec3) {
        _uiState.update { cur ->
            val selId = cur.selectedEntityId ?: return@update cur
            val ch = cur.characters.firstOrNull { it.id == selId } ?: return@update cur
            if (ch.locked) return@update cur
            cur.copy(
                selectedJoint = jointId,
                activePoseId = null,
                characters = cur.characters.map {
                    if (it.id == selId) {
                        val updated = it.jointRotations.toMutableMap().apply {
                            put(
                                jointId,
                                Vec3(
                                    eulerDeg.x.coerceIn(-180f, 180f),
                                    eulerDeg.y.coerceIn(-180f, 180f),
                                    eulerDeg.z.coerceIn(-180f, 180f)
                                )
                            )
                        }
                        it.copy(jointRotations = updated)
                    } else it
                }
            )
        }
    }

    fun resetJoint(jointId: JointId) {
        endHistoryBatch()
        _uiState.update { cur ->
            val selId = cur.selectedEntityId ?: return@update cur
            val ch = cur.characters.firstOrNull { it.id == selId } ?: return@update cur
            if (ch.locked) return@update cur
            val defaultRot = PosePresets.bodyPoses[0].jointRotations[jointId] ?: Vec3()
            if ((ch.jointRotations[jointId] ?: Vec3()) == defaultRot) return@update cur
            pushHistory(cur, "Reset ${jointId.label}")
            cur.copy(
                activePoseId = null,
                characters = cur.characters.map {
                    if (it.id == selId) {
                        val updated = it.jointRotations.toMutableMap().apply {
                            put(jointId, defaultRot)
                        }
                        it.copy(jointRotations = updated)
                    } else it
                }
            ).withHistoryMeta()
        }
    }

    fun resetAllJoints() {
        applyBodyPose(0)
    }

    fun setPropScale(scale: Vec3) {
        _uiState.update { cur ->
            val selId = cur.selectedEntityId ?: return@update cur
            val prop = cur.props.firstOrNull { it.id == selId } ?: return@update cur
            if (prop.locked) return@update cur
            cur.copy(
                props = cur.props.map {
                    if (it.id == selId) it.copy(
                        scale = Vec3(
                            scale.x.coerceIn(0.1f, 5.0f),
                            scale.y.coerceIn(0.1f, 5.0f),
                            scale.z.coerceIn(0.1f, 5.0f)
                        )
                    ) else it
                }
            )
        }
    }

    fun setBrightness(v: Float) {
        _uiState.update { it.copy(light = it.light.copy(brightness = v.coerceIn(0f, 1.5f))) }
    }

    fun setGiStrength(v: Float) {
        _uiState.update { it.copy(light = it.light.copy(giStrength = v.coerceIn(0f, 1f))) }
    }

    fun setColorTemperature(kelvin: Float) {
        _uiState.update { it.copy(light = it.light.copy(colorTemperatureK = kelvin.coerceIn(2000f, 10000f))) }
    }

    fun applyColorTempPreset(kelvin: Float) {
        val target = kelvin.coerceIn(2000f, 10000f)
        endHistoryBatch()
        _uiState.update { cur ->
            if (kotlin.math.abs(cur.light.colorTemperatureK - target) < 0.5f) return@update cur
            pushHistory(cur, "Color Temp ${target.toInt()}K")
            cur.copy(light = cur.light.copy(colorTemperatureK = target)).withHistoryMeta()
        }
    }

    fun setLightDirection(azimuthDeg: Float, elevationDeg: Float) {
        _uiState.update {
            it.copy(
                light = it.light.copy(
                    azimuthDeg = azimuthDeg.coerceIn(-180f, 180f),
                    elevationDeg = elevationDeg.coerceIn(5f, 88f)
                )
            )
        }
    }

    fun setPerspectiveFov(fov: Float) {
        _uiState.update { it.copy(camera = it.camera.copy(fovDeg = fov.coerceIn(25f, 90f))) }
    }

    fun setCameraProjection(projection: CameraProjection) {
        _uiState.update { it.copy(camera = it.camera.copy(projection = projection)) }
    }

    fun setCameraDistance(distance: Float) {
        _uiState.update { it.copy(camera = it.camera.copy(distance = distance.coerceIn(1.1f, 12.0f))) }
    }

    fun resetCamera() {
        _uiState.update { it.copy(camera = CameraSettings()) }
    }

    fun frameSelected() {
        _uiState.update { cur ->
            val newTarget = when (cur.selectedPart) {
                SelectedPart.MODEL -> cur.selectedProp?.position ?: Vec3(0f, 0.9f, 0f)
                SelectedPart.SKEL_MODEL, SelectedPart.CONTROL, SelectedPart.HEAD, SelectedPart.HAIR, SelectedPart.HAND -> {
                    val ch = cur.selectedCharacter
                    if (ch != null) ch.position + Vec3(0f, 0.95f * ch.scale, 0f) else Vec3(0f, 0.95f, 0f)
                }
                else -> Vec3(0f, 0.95f, 0f)
            }
            cur.copy(camera = cur.camera.copy(target = newTarget, distance = 3.6f))
        }
    }

    fun setCameraPreset(preset: CameraPreset, projection: CameraProjection? = null) {
        val (az, el) = when (preset) {
            CameraPreset.LEFT -> -90f to 0f
            CameraPreset.RIGHT -> 90f to 0f
            CameraPreset.BOTTOM -> 0f to -89.5f
            CameraPreset.TOP -> 0f to 89.5f
            CameraPreset.FRONT -> 0f to 0f
            CameraPreset.BACK -> 180f to 0f
            CameraPreset.ISOMETRIC -> 45f to 35.264f
        }
        _uiState.update { cur ->
            cur.copy(
                camera = cur.camera.copy(
                    azimuthDeg = az,
                    elevationDeg = el,
                    projection = projection ?: cur.camera.projection,
                    activePreset = preset
                )
            )
        }
    }

    fun toggleGrid() {
        _uiState.update { it.copy(showGrid = !it.showGrid) }
    }

    fun toggleSky() {
        _uiState.update { it.copy(showSky = !it.showSky) }
    }

    fun toggleSportswear() {
        _uiState.update { it.copy(showSportswear = !it.showSportswear) }
    }

    fun setLanguage(lang: AppLanguage) {
        _uiState.update { it.copy(language = lang) }
    }

    fun orbitCamera(dx: Float, dy: Float) {
        _uiState.update { cur ->
            val az = (cur.camera.azimuthDeg - dx * 0.35f) % 360f
            val el = (cur.camera.elevationDeg + dy * 0.35f).coerceIn(-89.5f, 89.5f)
            cur.copy(camera = cur.camera.copy(azimuthDeg = az, elevationDeg = el, activePreset = null))
        }
    }

    fun zoomCamera(scaleFactor: Float) {
        _uiState.update { cur ->
            val d = (cur.camera.distance / scaleFactor).coerceIn(1.1f, 12.0f)
            cur.copy(camera = cur.camera.copy(distance = d))
        }
    }

    fun panCamera(dx: Float, dy: Float) {
        _uiState.update { cur ->
            val cam = cur.camera
            val az = cam.azimuthDeg * PI.toFloat() / 180f
            val right = Vec3(cos(az), 0f, -sin(az))
            val speed = cam.distance * 0.0012f
            val newTarget = cam.target - right * (dx * speed) + Vec3(0f, dy * speed, 0f)
            cur.copy(camera = cam.copy(target = newTarget))
        }
    }

    fun onGizmoDragStart(axis: Int) {
        gizmoHistoryCaptured = false
        _uiState.update { it.copy(activeGizmoAxis = axis) }
    }

    fun onGizmoDragEnd() {
        gizmoHistoryCaptured = false
        _uiState.update { it.copy(activeGizmoAxis = -1) }
    }

    fun onGizmoDrag(
        axis: Int,
        dx: Float,
        dy: Float,
        viewProj: FloatArray,
        viewW: Float,
        viewH: Float
    ) {
        val cur = _uiState.value
        if (cur.selectedPart == SelectedPart.LIGHT) {
            if (!gizmoHistoryCaptured) {
                pushHistory(cur, "Light Direction")
                gizmoHistoryCaptured = true
            }
            _uiState.update { s ->
                val az = (s.light.azimuthDeg + dx * 0.45f) % 360f
                val el = (s.light.elevationDeg - dy * 0.45f).coerceIn(5f, 88f)
                s.copy(
                    light = s.light.copy(azimuthDeg = az, elevationDeg = el)
                ).withHistoryMeta()
            }
            return
        }

        val selId = cur.selectedEntityId ?: return
        val isLocked = cur.selectedCharacter?.locked == true || cur.selectedProp?.locked == true
        if (isLocked) return

        if (!gizmoHistoryCaptured) {
            val gizmoLabel = when (cur.selectedPart) {
                SelectedPart.CONTROL, SelectedPart.HAND, SelectedPart.HEAD, SelectedPart.HAIR ->
                    "Joint: ${(cur.selectedJoint ?: JointId.HEAD).label}"
                else -> "Transform"
            }
            pushHistory(cur, gizmoLabel)
            gizmoHistoryCaptured = true
        }

        val useTranslate = cur.manipulatorMode == ManipulatorMode.TRANSLATE &&
            (cur.selectedPart == SelectedPart.MODEL || cur.selectedPart == SelectedPart.SKEL_MODEL)

        val axisVec = when (axis) {
            0 -> Vec3(1f, 0f, 0f)
            1 -> Vec3(0f, 1f, 0f)
            else -> Vec3(0f, 0f, 1f)
        }

        val origin = when (cur.selectedPart) {
            SelectedPart.MODEL -> cur.selectedProp?.position ?: Vec3()
            else -> cur.selectedCharacter?.position?.plus(Vec3(0f, 0.9f, 0f)) ?: Vec3()
        }
        val p0 = Math3D.projectToScreen(origin, viewProj, viewW, viewH)
        val p1 = Math3D.projectToScreen(origin + axisVec * 0.5f, viewProj, viewW, viewH)
        val projectedDelta = if (p0 != null && p1 != null) {
            val ax = p1.first - p0.first
            val ay = p1.second - p0.second
            val len = kotlin.math.hypot(ax, ay).coerceAtLeast(1f)
            (dx * (ax / len) + dy * (ay / len))
        } else {
            dx - dy
        }

        if (useTranslate) {
            val moveAmt = projectedDelta * 0.0045f * cur.camera.distance
            val delta = axisVec * moveAmt
            _uiState.update { s ->
                when (s.selectedPart) {
                    SelectedPart.MODEL -> s.copy(
                        props = s.props.map { if (it.id == selId) it.copy(position = it.position + delta) else it }
                    ).withHistoryMeta()
                    SelectedPart.SKEL_MODEL -> s.copy(
                        characters = s.characters.map { if (it.id == selId) it.copy(position = it.position + delta) else it }
                    ).withHistoryMeta()
                    else -> s
                }
            }
        } else {
            val rotAmt = (dx - dy * 0.6f) * 0.45f
            val rotDelta = axisVec * rotAmt
            _uiState.update { s ->
                when (s.selectedPart) {
                    SelectedPart.MODEL -> s.copy(
                        props = s.props.map {
                            if (it.id == selId) it.copy(rotationEuler = it.rotationEuler + rotDelta) else it
                        }
                    ).withHistoryMeta()
                    SelectedPart.SKEL_MODEL -> s.copy(
                        characters = s.characters.map {
                            if (it.id == selId) it.copy(rotationEuler = it.rotationEuler + rotDelta) else it
                        }
                    ).withHistoryMeta()
                    SelectedPart.CONTROL, SelectedPart.HAND, SelectedPart.HEAD, SelectedPart.HAIR -> {
                        val jId = s.selectedJoint ?: JointId.HEAD
                        s.copy(
                            activePoseId = null,
                            characters = s.characters.map { ch ->
                                if (ch.id == selId) {
                                    val prev = ch.jointRotations[jId] ?: Vec3()
                                    val updated = ch.jointRotations.toMutableMap().apply {
                                        put(jId, prev + rotDelta)
                                    }
                                    ch.copy(jointRotations = updated)
                                } else ch
                            }
                        ).withHistoryMeta()
                    }
                    else -> s
                }
            }
        }
    }
}
