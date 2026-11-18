package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.engine.Math3D
import com.example.engine.PickHit
import com.example.engine.StudioRenderSnapshot
import com.example.i18n.AppLanguage
import com.example.model.CameraPreset
import com.example.model.CameraSettings
import com.example.model.CharacterEntity
import com.example.model.CharacterType
import com.example.model.HairStyle
import com.example.model.JointId
import com.example.model.LightSettings
import com.example.model.ManipulatorMode
import com.example.model.PosePresets
import com.example.model.PropEntity
import com.example.model.PropType
import com.example.model.RgbColor
import com.example.model.SceneSnapshot
import com.example.model.SelectedPart
import com.example.model.SideMenu
import com.example.model.Vec3
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    val handLeftSelected: Boolean = true,
    val materialEditBody: Boolean = true
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

    private fun snapshotOf(s: PoserUiState): SceneSnapshot = SceneSnapshot(
        characters = s.characters,
        props = s.props,
        light = s.light,
        selectedEntityId = s.selectedEntityId,
        selectedJoint = s.selectedJoint,
        selectedPart = s.selectedPart
    )

    private fun pushHistory(s: PoserUiState) {
        if (undoStack.size >= 40) undoStack.removeFirst()
        undoStack.addLast(snapshotOf(s))
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
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val cur = _uiState.value
        redoStack.addLast(snapshotOf(cur))
        val prev = undoStack.removeLast()
        applySnapshot(prev)
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val cur = _uiState.value
        undoStack.addLast(snapshotOf(cur))
        val next = redoStack.removeLast()
        applySnapshot(next)
    }

    fun enablePreviewMode() {
        _uiState.update { it.copy(previewMode = true, sideMenu = SideMenu.NONE) }
    }

    fun disablePreviewMode() {
        _uiState.update { it.copy(previewMode = false) }
    }

    fun toggleMenu(menu: SideMenu) {
        _uiState.update { cur ->
            val next = if (cur.sideMenu == menu) SideMenu.NONE else menu
            cur.copy(sideMenu = next)
        }
    }

    fun closeSideMenu() {
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
                    materialEditBody = hit.part != SelectedPart.HEAD && hit.part != SelectedPart.HAIR
                )
                is PickHit.GizmoAxis -> cur.copy(previewMode = exitedPreview)
            }
        }
    }

    fun addModel(type: CharacterType) {
        _uiState.update { cur ->
            pushHistory(cur)
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
                selectedPart = SelectedPart.SKEL_MODEL,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
    }

    fun addProp(type: PropType) {
        _uiState.update { cur ->
            pushHistory(cur)
            val offset = (cur.props.size % 5) * 0.45f - 0.6f
            val newProp = PropEntity(
                id = "prop_${UUID.randomUUID().toString().take(8)}",
                type = type,
                position = Vec3(offset, 0.5f, 0.25f)
            )
            cur.copy(
                props = cur.props + newProp,
                selectedEntityId = newProp.id,
                selectedJoint = null,
                selectedPart = SelectedPart.MODEL,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
    }

    fun changeModel(type: CharacterType) {
        _uiState.update { cur ->
            val targetId = cur.selectedEntityId ?: return@update cur
            pushHistory(cur)
            cur.copy(
                characters = cur.characters.map {
                    if (it.id == targetId) it.copy(type = type) else it
                },
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
    }

    fun duplicateSelected() {
        _uiState.update { cur ->
            val selId = cur.selectedEntityId ?: return@update cur
            when (cur.selectedPart) {
                SelectedPart.MODEL -> {
                    val src = cur.props.firstOrNull { it.id == selId } ?: return@update cur
                    pushHistory(cur)
                    val dup = src.copy(
                        id = "prop_${UUID.randomUUID().toString().take(8)}",
                        position = src.position + Vec3(0.4f, 0f, 0.2f)
                    )
                    cur.copy(
                        props = cur.props + dup,
                        selectedEntityId = dup.id,
                        canUndo = undoStack.isNotEmpty(),
                        canRedo = redoStack.isNotEmpty()
                    )
                }
                SelectedPart.SKEL_MODEL, SelectedPart.HEAD, SelectedPart.HAIR, SelectedPart.CONTROL, SelectedPart.HAND -> {
                    val src = cur.characters.firstOrNull { it.id == selId } ?: return@update cur
                    pushHistory(cur)
                    val dup = src.copy(
                        id = "char_${UUID.randomUUID().toString().take(8)}",
                        position = src.position + Vec3(0.55f, 0f, 0f)
                    )
                    cur.copy(
                        characters = cur.characters + dup,
                        selectedEntityId = dup.id,
                        selectedPart = SelectedPart.SKEL_MODEL,
                        selectedJoint = JointId.PELVIS,
                        canUndo = undoStack.isNotEmpty(),
                        canRedo = redoStack.isNotEmpty()
                    )
                }
                else -> cur
            }
        }
    }

    fun removeSelected() {
        _uiState.update { cur ->
            val selId = cur.selectedEntityId ?: return@update cur
            pushHistory(cur)
            val newChars = cur.characters.filterNot { it.id == selId }
            val newProps = cur.props.filterNot { it.id == selId }
            cur.copy(
                characters = newChars,
                props = newProps,
                selectedEntityId = null,
                selectedJoint = null,
                selectedPart = SelectedPart.NOTHING,
                sideMenu = SideMenu.NONE,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
    }

    fun toggleLockSelected() {
        _uiState.update { cur ->
            val selId = cur.selectedEntityId ?: return@update cur
            pushHistory(cur)
            cur.copy(
                characters = cur.characters.map { if (it.id == selId) it.copy(locked = !it.locked) else it },
                props = cur.props.map { if (it.id == selId) it.copy(locked = !it.locked) else it },
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
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
        _uiState.update { cur ->
            val selId = cur.selectedEntityId ?: return@update cur
            val ch = cur.characters.firstOrNull { it.id == selId } ?: return@update cur
            if (ch.locked) return@update cur
            pushHistory(cur)
            cur.copy(
                characters = cur.characters.map {
                    if (it.id == selId) {
                        it.copy(
                            pelvisOffsetY = preset.pelvisOffsetY,
                            jointRotations = preset.jointRotations
                        )
                    } else it
                },
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
    }

    fun applyHandPose(poseIndex: Int, left: Boolean) {
        val preset = PosePresets.handPoses.getOrNull(poseIndex) ?: return
        _uiState.update { cur ->
            val selId = cur.selectedEntityId ?: return@update cur
            val ch = cur.characters.firstOrNull { it.id == selId } ?: return@update cur
            if (ch.locked) return@update cur
            pushHistory(cur)
            cur.copy(
                characters = cur.characters.map {
                    if (it.id == selId) {
                        if (left) it.copy(leftHand = preset.fingerState) else it.copy(rightHand = preset.fingerState)
                    } else it
                },
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
    }

    fun selectHair(style: HairStyle) {
        _uiState.update { cur ->
            val selId = cur.selectedEntityId ?: return@update cur
            pushHistory(cur)
            cur.copy(
                characters = cur.characters.map {
                    if (it.id == selId) it.copy(hairStyle = style) else it
                },
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
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

    fun commitHistorySnapshot() {
        pushHistory(_uiState.value)
        _uiState.update { it.copy(canUndo = undoStack.isNotEmpty(), canRedo = redoStack.isNotEmpty()) }
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
        _uiState.update { it.copy(light = it.light.copy(brightness = v.coerceIn(0f, 1f))) }
    }

    fun setGiStrength(v: Float) {
        _uiState.update { it.copy(light = it.light.copy(giStrength = v.coerceIn(0f, 1f))) }
    }

    fun setPerspectiveFov(fov: Float) {
        _uiState.update { it.copy(camera = it.camera.copy(fovDeg = fov.coerceIn(30f, 90f))) }
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

    fun setCameraPreset(preset: CameraPreset) {
        val (az, el) = when (preset) {
            CameraPreset.LEFT -> -90f to 4f
            CameraPreset.RIGHT -> 90f to 4f
            CameraPreset.BOTTOM -> 0f to -85f
            CameraPreset.TOP -> 0f to 85f
            CameraPreset.FRONT -> 0f to 4f
            CameraPreset.BACK -> 180f to 4f
        }
        _uiState.update { cur ->
            cur.copy(camera = cur.camera.copy(azimuthDeg = az, elevationDeg = el))
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
            val el = (cur.camera.elevationDeg + dy * 0.35f).coerceIn(-85f, 85f)
            cur.copy(camera = cur.camera.copy(azimuthDeg = az, elevationDeg = el))
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
                pushHistory(cur)
                gizmoHistoryCaptured = true
            }
            _uiState.update { s ->
                val az = (s.light.azimuthDeg + dx * 0.45f) % 360f
                val el = (s.light.elevationDeg - dy * 0.45f).coerceIn(5f, 88f)
                s.copy(
                    light = s.light.copy(azimuthDeg = az, elevationDeg = el),
                    canUndo = undoStack.isNotEmpty(),
                    canRedo = redoStack.isNotEmpty()
                )
            }
            return
        }

        val selId = cur.selectedEntityId ?: return
        val isLocked = cur.selectedCharacter?.locked == true || cur.selectedProp?.locked == true
        if (isLocked) return

        if (!gizmoHistoryCaptured) {
            pushHistory(cur)
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
                        props = s.props.map { if (it.id == selId) it.copy(position = it.position + delta) else it },
                        canUndo = undoStack.isNotEmpty(),
                        canRedo = redoStack.isNotEmpty()
                    )
                    SelectedPart.SKEL_MODEL -> s.copy(
                        characters = s.characters.map { if (it.id == selId) it.copy(position = it.position + delta) else it },
                        canUndo = undoStack.isNotEmpty(),
                        canRedo = redoStack.isNotEmpty()
                    )
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
                        },
                        canUndo = undoStack.isNotEmpty(),
                        canRedo = redoStack.isNotEmpty()
                    )
                    SelectedPart.SKEL_MODEL -> s.copy(
                        characters = s.characters.map {
                            if (it.id == selId) it.copy(rotationEuler = it.rotationEuler + rotDelta) else it
                        },
                        canUndo = undoStack.isNotEmpty(),
                        canRedo = redoStack.isNotEmpty()
                    )
                    SelectedPart.CONTROL, SelectedPart.HAND, SelectedPart.HEAD, SelectedPart.HAIR -> {
                        val jId = s.selectedJoint ?: JointId.HEAD
                        s.copy(
                            characters = s.characters.map { ch ->
                                if (ch.id == selId) {
                                    val prev = ch.jointRotations[jId] ?: Vec3()
                                    val updated = ch.jointRotations.toMutableMap().apply {
                                        put(jId, prev + rotDelta)
                                    }
                                    ch.copy(jointRotations = updated)
                                } else ch
                            },
                            canUndo = undoStack.isNotEmpty(),
                            canRedo = redoStack.isNotEmpty()
                        )
                    }
                    else -> s
                }
            }
        }
    }
}
