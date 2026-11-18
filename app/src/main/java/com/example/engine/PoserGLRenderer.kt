package com.example.engine

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.model.CameraProjection
import com.example.model.CameraSettings
import com.example.model.CharacterEntity
import com.example.model.HairStyle
import com.example.model.JointId
import com.example.model.LightSettings
import com.example.model.ManipulatorMode
import com.example.model.PropEntity
import com.example.model.PropType
import com.example.model.RgbColor
import com.example.model.SelectedPart
import com.example.model.Vec3
import com.example.model.kelvinToRgb
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

data class StudioRenderSnapshot(
    val characters: List<CharacterEntity> = emptyList(),
    val props: List<PropEntity> = emptyList(),
    val light: LightSettings = LightSettings(),
    val camera: CameraSettings = CameraSettings(),
    val showGrid: Boolean = true,
    val showSky: Boolean = true,
    val showSportswear: Boolean = true,
    val previewMode: Boolean = false,
    val selectedEntityId: String? = null,
    val selectedJoint: JointId? = null,
    val selectedPart: SelectedPart = SelectedPart.NOTHING,
    val manipulatorMode: ManipulatorMode = ManipulatorMode.ROTATE,
    val activeGizmoAxis: Int = -1
)

sealed interface PickHit {
    data class GizmoAxis(val axis: Int) : PickHit
    data object Light : PickHit
    data class CharacterPart(val entityId: String, val part: SelectedPart, val jointId: JointId?) : PickHit
    data class Prop(val entityId: String) : PickHit
    data object Nothing : PickHit
}

private class GpuMesh(data: MeshData) {
    val vao: Int
    val vbo: Int
    val ibo: Int
    val indexCount: Int = data.indices.size
    val isLines: Boolean = data.isLines

    init {
        val vaos = IntArray(1)
        val bufs = IntArray(2)
        GLES30.glGenVertexArrays(1, vaos, 0)
        GLES30.glGenBuffers(2, bufs, 0)
        vao = vaos[0]
        vbo = bufs[0]
        ibo = bufs[1]

        val vBuf: FloatBuffer = ByteBuffer.allocateDirect(data.vertices.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
                put(data.vertices)
                position(0)
            }
        val iBuf: ShortBuffer = ByteBuffer.allocateDirect(data.indices.size * 2)
            .order(ByteOrder.nativeOrder()).asShortBuffer().apply {
                put(data.indices)
                position(0)
            }

        GLES30.glBindVertexArray(vao)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo)
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, data.vertices.size * 4, vBuf, GLES30.GL_STATIC_DRAW)

        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, ibo)
        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, data.indices.size * 2, iBuf, GLES30.GL_STATIC_DRAW)

        val stride = 6 * 4
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, stride, 0)
        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(1, 3, GLES30.GL_FLOAT, false, stride, 3 * 4)

        GLES30.glBindVertexArray(0)
    }

    fun draw() {
        GLES30.glBindVertexArray(vao)
        val mode = if (isLines) GLES30.GL_LINES else GLES30.GL_TRIANGLES
        GLES30.glDrawElements(mode, indexCount, GLES30.GL_UNSIGNED_SHORT, 0)
        GLES30.glBindVertexArray(0)
    }
}

class PoserGLRenderer(
    private val onInitProgress: (Float) -> Unit = {}
) : GLSurfaceView.Renderer {

    @Volatile
    var snapshot: StudioRenderSnapshot = StudioRenderSnapshot()

    private var viewWidth: Int = 1
    private var viewHeight: Int = 1

    private val viewMatrix = FloatArray(16)
    private val projMatrix = FloatArray(16)
    private val viewProjMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    @Volatile
    private var lastViewProj = FloatArray(16)

    private var mainProgram = 0
    private var skyProgram = 0
    private var skyVao = 0

    private val meshCache = HashMap<RenderMeshKey, GpuMesh>()
    private val hairCache = HashMap<HairStyle, GpuMesh>()
    private var gridMesh: GpuMesh? = null
    private val ringMeshes = arrayOfNulls<GpuMesh>(3)
    private val arrowMeshes = arrayOfNulls<GpuMesh>(3)

    // Uniforms
    private var uMvpLoc = 0
    private var uModelLoc = 0
    private var uColorLoc = 0
    private var uLightDirLoc = 0
    private var uCamPosLoc = 0
    private var uBrightnessLoc = 0
    private var uLightColorLoc = 0
    private var uGiLoc = 0
    private var uUnlitLoc = 0
    private var uHighlightLoc = 0

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glEnable(GLES30.GL_CULL_FACE)
        GLES30.glCullFace(GLES30.GL_BACK)
        GLES30.glLineWidth(2.0f)

        mainProgram = buildProgram(VERTEX_SHADER, FRAGMENT_SHADER)
        skyProgram = buildProgram(SKY_VERTEX_SHADER, SKY_FRAGMENT_SHADER)
        onInitProgress(0.25f)

        uMvpLoc = GLES30.glGetUniformLocation(mainProgram, "uMVP")
        uModelLoc = GLES30.glGetUniformLocation(mainProgram, "uModel")
        uColorLoc = GLES30.glGetUniformLocation(mainProgram, "uColor")
        uLightDirLoc = GLES30.glGetUniformLocation(mainProgram, "uLightDir")
        uCamPosLoc = GLES30.glGetUniformLocation(mainProgram, "uCamPos")
        uBrightnessLoc = GLES30.glGetUniformLocation(mainProgram, "uBrightness")
        uLightColorLoc = GLES30.glGetUniformLocation(mainProgram, "uLightColor")
        uGiLoc = GLES30.glGetUniformLocation(mainProgram, "uGI")
        uUnlitLoc = GLES30.glGetUniformLocation(mainProgram, "uUnlit")
        uHighlightLoc = GLES30.glGetUniformLocation(mainProgram, "uHighlight")

        meshCache[RenderMeshKey.SPHERE] = GpuMesh(MeshGenerator.sphere(0.5f))
        meshCache[RenderMeshKey.CUBE] = GpuMesh(MeshGenerator.box(1f, 1f, 1f))
        meshCache[RenderMeshKey.CONE] = GpuMesh(MeshGenerator.cone())
        meshCache[RenderMeshKey.CYLINDER] = GpuMesh(MeshGenerator.cylinder())
        meshCache[RenderMeshKey.HEX] = GpuMesh(MeshGenerator.hexPrism())
        meshCache[RenderMeshKey.PYRAMID] = GpuMesh(MeshGenerator.pyramid())
        meshCache[RenderMeshKey.RECTANGLE] = GpuMesh(MeshGenerator.rectangle())
        meshCache[RenderMeshKey.STAIRS] = GpuMesh(MeshGenerator.stairs())
        meshCache[RenderMeshKey.TORUS] = GpuMesh(MeshGenerator.torus())
        meshCache[RenderMeshKey.WALL] = GpuMesh(MeshGenerator.wall())
        meshCache[RenderMeshKey.CHAIR] = GpuMesh(MeshGenerator.chair())
        meshCache[RenderMeshKey.STOOL] = GpuMesh(MeshGenerator.stool())
        meshCache[RenderMeshKey.TABLE] = GpuMesh(MeshGenerator.table())
        meshCache[RenderMeshKey.SOFA] = GpuMesh(MeshGenerator.sofa())
        meshCache[RenderMeshKey.LAMP] = GpuMesh(MeshGenerator.lamp())
        meshCache[RenderMeshKey.PEDESTAL] = GpuMesh(MeshGenerator.pedestal())
        meshCache[RenderMeshKey.HEAD] = GpuMesh(MeshGenerator.headSculpt())
        meshCache[RenderMeshKey.PLANAR_HEAD] = GpuMesh(MeshGenerator.planarHead())
        meshCache[RenderMeshKey.LIMB_CAPSULE] = GpuMesh(MeshGenerator.limbCapsule())
        meshCache[RenderMeshKey.TORSO_WEDGE] = GpuMesh(MeshGenerator.torsoWedge())
        onInitProgress(0.55f)

        HairStyle.entries.forEach { style ->
            if (style != HairStyle.NAKED) {
                hairCache[style] = GpuMesh(MeshGenerator.hairMesh(style))
            }
        }
        onInitProgress(0.85f)

        gridMesh = GpuMesh(MeshGenerator.gridLines(6))
        for (axis in 0..2) {
            ringMeshes[axis] = GpuMesh(MeshGenerator.gizmoRing(axis, 0.26f))
            arrowMeshes[axis] = GpuMesh(MeshGenerator.gizmoArrow(axis, 0.38f))
        }

        val vaos = IntArray(1)
        GLES30.glGenVertexArrays(1, vaos, 0)
        skyVao = vaos[0]
        onInitProgress(1.0f)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        viewWidth = width.coerceAtLeast(1)
        viewHeight = height.coerceAtLeast(1)
        GLES30.glViewport(0, 0, viewWidth, viewHeight)
    }

    override fun onDrawFrame(gl: GL10?) {
        val state = snapshot

        if (state.showSky) {
            GLES30.glClearColor(0.26f, 0.42f, 0.62f, 1f)
        } else {
            GLES30.glClearColor(0.11f, 0.125f, 0.16f, 1f)
        }
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        if (state.showSky) {
            GLES30.glDisable(GLES30.GL_DEPTH_TEST)
            GLES30.glUseProgram(skyProgram)
            GLES30.glBindVertexArray(skyVao)
            GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3)
            GLES30.glBindVertexArray(0)
            GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        }

        val camPos = computeCameraPosition(state.camera)
        val up = if (kotlin.math.abs(state.camera.elevationDeg) > 88f) {
            val azRad = state.camera.azimuthDeg * PI.toFloat() / 180f
            Vec3(-sin(azRad), 0f, -cos(azRad))
        } else {
            Vec3(0f, 1f, 0f)
        }
        Matrix.setLookAtM(
            viewMatrix, 0,
            camPos.x, camPos.y, camPos.z,
            state.camera.target.x, state.camera.target.y, state.camera.target.z,
            up.x, up.y, up.z
        )
        val aspect = viewWidth.toFloat() / viewHeight.toFloat()
        if (state.camera.projection == CameraProjection.ORTHOGRAPHIC) {
            val halfH = (state.camera.distance * tan((state.camera.fovDeg * 0.5f * PI.toFloat() / 180f).toDouble()).toFloat())
                .coerceIn(0.45f, 8.0f)
            val halfW = halfH * aspect
            Matrix.orthoM(projMatrix, 0, -halfW, halfW, -halfH, halfH, -25f, 80f)
        } else {
            Matrix.perspectiveM(projMatrix, 0, state.camera.fovDeg, aspect, 0.1f, 100f)
        }
        Matrix.multiplyMM(viewProjMatrix, 0, projMatrix, 0, viewMatrix, 0)
        lastViewProj = viewProjMatrix.copyOf()

        GLES30.glUseProgram(mainProgram)
        val lightDir = computeLightDirection(state.light)
        val lightColor = kelvinToRgb(state.light.colorTemperatureK)
        GLES30.glUniform3f(uLightDirLoc, lightDir.x, lightDir.y, lightDir.z)
        GLES30.glUniform3f(uCamPosLoc, camPos.x, camPos.y, camPos.z)
        GLES30.glUniform1f(uBrightnessLoc, state.light.brightness)
        GLES30.glUniform3f(uLightColorLoc, lightColor.r, lightColor.g, lightColor.b)
        GLES30.glUniform1f(uGiLoc, state.light.giStrength)

        // Draw Ground Grid
        if (state.showGrid && !state.previewMode) {
            val gridColor = if (state.showSky) RgbColor(0.75f, 0.82f, 0.90f) else RgbColor(0.32f, 0.36f, 0.44f)
            drawMeshUnlit(gridMesh, Math3D.identity(), gridColor)
        }

        // Draw Characters
        var activeGizmoPos: Vec3? = null
        var activeGizmoRotMat: FloatArray? = null

        for (ch in state.characters) {
            val built = SkeletonBuilder.buildCharacter(
                ch = ch,
                showSportswear = state.showSportswear,
                selectedEntityId = state.selectedEntityId,
                selectedJoint = state.selectedJoint,
                selectedPart = state.selectedPart
            )
            for (item in built.renderItems) {
                val mesh = if (item.hairStyle != null) {
                    hairCache[item.hairStyle]
                } else {
                    meshCache[item.meshKey]
                }
                drawMeshLit(
                    mesh = mesh,
                    model = item.modelMatrix,
                    color = item.color,
                    highlight = if (!state.previewMode && item.isHighlighted) 1f else 0f
                )
            }
            if (!state.previewMode && state.selectedEntityId == ch.id) {
                when (state.selectedPart) {
                    SelectedPart.SKEL_MODEL -> {
                        activeGizmoPos = ch.position + Vec3(0f, 0.9f * ch.scale, 0f)
                        activeGizmoRotMat = Math3D.rotateEulerDeg(
                            Math3D.translate(Math3D.identity(), activeGizmoPos.x, activeGizmoPos.y, activeGizmoPos.z),
                            ch.rotationEuler.x, ch.rotationEuler.y, ch.rotationEuler.z
                        )
                    }
                    SelectedPart.HEAD, SelectedPart.HAIR -> {
                        val jm = built.jointTransforms[JointId.HEAD]
                        if (jm != null) {
                            activeGizmoPos = built.headCenter
                            activeGizmoRotMat = Math3D.translate(Math3D.identity(), activeGizmoPos.x, activeGizmoPos.y, activeGizmoPos.z)
                        }
                    }
                    SelectedPart.CONTROL, SelectedPart.HAND -> {
                        val jId = state.selectedJoint
                        val jm = if (jId != null) built.jointTransforms[jId] else null
                        if (jm != null) {
                            val p = Math3D.transformPoint(jm, Vec3())
                            activeGizmoPos = p
                            activeGizmoRotMat = Math3D.translate(Math3D.identity(), p.x, p.y, p.z)
                        }
                    }
                    else -> Unit
                }
            }
        }

        // Draw Props
        for (prop in state.props) {
            var m = Math3D.identity()
            m = Math3D.translate(m, prop.position.x, prop.position.y, prop.position.z)
            m = Math3D.rotateEulerDeg(m, prop.rotationEuler.x, prop.rotationEuler.y, prop.rotationEuler.z)
            m = Math3D.scale(m, prop.scale.x, prop.scale.y, prop.scale.z)
            val mesh = meshCache[propMeshKey(prop.type)]
            val isSel = !state.previewMode && state.selectedEntityId == prop.id && state.selectedPart == SelectedPart.MODEL
            drawMeshLit(mesh, m, prop.color, if (isSel) 1f else 0f)
            if (isSel) {
                activeGizmoPos = prop.position
                activeGizmoRotMat = Math3D.rotateEulerDeg(
                    Math3D.translate(Math3D.identity(), prop.position.x, prop.position.y, prop.position.z),
                    prop.rotationEuler.x, prop.rotationEuler.y, prop.rotationEuler.z
                )
            }
        }

        // Draw Directional Light Sphere Gizmo
        if (!state.previewMode) {
            val lightPos = lightGizmoPosition(state.light)
            val isLightSel = state.selectedPart == SelectedPart.LIGHT
            var lm = Math3D.translate(Math3D.identity(), lightPos.x, lightPos.y, lightPos.z)
            lm = Math3D.scale(lm, 0.22f, 0.22f, 0.22f)
            val sunColor = if (isLightSel) {
                RgbColor(
                    (lightColor.r * 0.7f + 0.3f).coerceIn(0f, 1f),
                    (lightColor.g * 0.7f + 0.3f).coerceIn(0f, 1f),
                    (lightColor.b * 0.7f + 0.3f).coerceIn(0f, 1f)
                )
            } else {
                lightColor
            }
            drawMeshUnlit(meshCache[RenderMeshKey.SPHERE], lm, sunColor)
            if (isLightSel) {
                activeGizmoPos = lightPos
                activeGizmoRotMat = Math3D.translate(Math3D.identity(), lightPos.x, lightPos.y, lightPos.z)
            }
        }

        // Draw Active 3D Transform Gizmo on top (depth test disabled for clean studio visibility)
        if (!state.previewMode && activeGizmoPos != null && activeGizmoRotMat != null) {
            GLES30.glDisable(GLES30.GL_DEPTH_TEST)
            val axisColors = arrayOf(
                RgbColor(0.96f, 0.30f, 0.30f), // X Red
                RgbColor(0.30f, 0.92f, 0.45f), // Y Green
                RgbColor(0.30f, 0.62f, 0.98f)  // Z Blue
            )
            val useTranslate = state.manipulatorMode == ManipulatorMode.TRANSLATE &&
                (state.selectedPart == SelectedPart.MODEL || state.selectedPart == SelectedPart.SKEL_MODEL)
            for (axis in 0..2) {
                val c = if (state.activeGizmoAxis == axis) RgbColor(1f, 0.95f, 0.35f) else axisColors[axis]
                val gizMesh = if (useTranslate) arrowMeshes[axis] else ringMeshes[axis]
                drawMeshUnlit(gizMesh, activeGizmoRotMat, c)
            }
            GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        }
    }

    private fun drawMeshLit(mesh: GpuMesh?, model: FloatArray, color: RgbColor, highlight: Float) {
        if (mesh == null) return
        Matrix.multiplyMM(mvpMatrix, 0, viewProjMatrix, 0, model, 0)
        GLES30.glUniformMatrix4fv(uMvpLoc, 1, false, mvpMatrix, 0)
        GLES30.glUniformMatrix4fv(uModelLoc, 1, false, model, 0)
        GLES30.glUniform3f(uColorLoc, color.r, color.g, color.b)
        GLES30.glUniform1f(uUnlitLoc, 0f)
        GLES30.glUniform1f(uHighlightLoc, highlight)
        mesh.draw()
    }

    private fun drawMeshUnlit(mesh: GpuMesh?, model: FloatArray, color: RgbColor) {
        if (mesh == null) return
        Matrix.multiplyMM(mvpMatrix, 0, viewProjMatrix, 0, model, 0)
        GLES30.glUniformMatrix4fv(uMvpLoc, 1, false, mvpMatrix, 0)
        GLES30.glUniformMatrix4fv(uModelLoc, 1, false, model, 0)
        GLES30.glUniform3f(uColorLoc, color.r, color.g, color.b)
        GLES30.glUniform1f(uUnlitLoc, 1f)
        GLES30.glUniform1f(uHighlightLoc, 0f)
        mesh.draw()
    }

    fun pickAt(screenX: Float, screenY: Float, includeGizmo: Boolean = true): PickHit {
        val state = snapshot
        val ray = Math3D.screenToRay(screenX, screenY, viewWidth.toFloat(), viewHeight.toFloat(), lastViewProj)
            ?: return PickHit.Nothing

        // 1. Check active Gizmo first if requested
        if (includeGizmo && !state.previewMode && state.selectedPart != SelectedPart.NOTHING) {
            val gizmoOrigin = resolveSelectedGizmoOrigin(state)
            if (gizmoOrigin != null) {
                val (pos, rotMat) = gizmoOrigin
                val useTranslate = state.manipulatorMode == ManipulatorMode.TRANSLATE &&
                    (state.selectedPart == SelectedPart.MODEL || state.selectedPart == SelectedPart.SKEL_MODEL)
                val axesDirs = arrayOf(
                    Math3D.transformDir(rotMat, Vec3(1f, 0f, 0f)),
                    Math3D.transformDir(rotMat, Vec3(0f, 1f, 0f)),
                    Math3D.transformDir(rotMat, Vec3(0f, 0f, 1f))
                )
                var bestAxis = -1
                var bestDist = Float.MAX_VALUE
                for (axis in 0..2) {
                    if (useTranslate) {
                        val (distToSeg, tRay) = Math3D.distanceRayToSegment(ray, pos, pos + axesDirs[axis] * 0.38f)
                        if (distToSeg < 0.065f && tRay < bestDist) {
                            bestDist = tRay
                            bestAxis = axis
                        }
                    } else {
                        val hit = Math3D.intersectRayRing(ray, pos, axesDirs[axis], 0.26f, 0.055f)
                        if (hit != null && hit < bestDist) {
                            bestDist = hit
                            bestAxis = axis
                        }
                    }
                }
                if (bestAxis != -1) {
                    return PickHit.GizmoAxis(bestAxis)
                }
            }
        }

        // 2. Check Light sphere
        var closestT = Float.MAX_VALUE
        var bestHit: PickHit = PickHit.Nothing

        val lightPos = lightGizmoPosition(state.light)
        Math3D.intersectRaySphere(ray, lightPos, 0.18f)?.let { t ->
            if (t < closestT) {
                closestT = t
                bestHit = PickHit.Light
            }
        }

        // 3. Check Character joints/parts
        for (ch in state.characters) {
            val built = SkeletonBuilder.buildCharacter(
                ch, state.showSportswear, state.selectedEntityId, state.selectedJoint, state.selectedPart
            )
            for (vol in built.pickVolumes) {
                val t = Math3D.intersectRaySphere(ray, vol.worldCenter, vol.pickRadius)
                if (t != null && t < closestT) {
                    closestT = t
                    bestHit = PickHit.CharacterPart(vol.entityId, vol.part, vol.jointId)
                }
            }
        }

        // 4. Check Props
        for (prop in state.props) {
            var m = Math3D.identity()
            m = Math3D.translate(m, prop.position.x, prop.position.y, prop.position.z)
            m = Math3D.rotateEulerDeg(m, prop.rotationEuler.x, prop.rotationEuler.y, prop.rotationEuler.z)
            m = Math3D.scale(m, prop.scale.x, prop.scale.y, prop.scale.z)
            val ext = propHalfExtents(prop.type)
            val t = Math3D.intersectRayOBB(ray, m, ext)
            if (t != null && t < closestT) {
                closestT = t
                bestHit = PickHit.Prop(prop.id)
            }
        }

        return bestHit
    }

    fun getLastViewProj(): FloatArray = lastViewProj
    fun getViewSize(): Pair<Float, Float> = viewWidth.toFloat() to viewHeight.toFloat()

    fun screenToGround(screenX: Float, screenY: Float): Vec3 {
        val ray = Math3D.screenToRay(screenX, screenY, viewWidth.toFloat(), viewHeight.toFloat(), lastViewProj)
        if (ray != null && kotlin.math.abs(ray.dir.y) > 1e-4f) {
            val t = -ray.origin.y / ray.dir.y
            if (t > 0.1f && t < 65f) {
                val hit = ray.origin + ray.dir * t
                return Vec3(
                    hit.x.coerceIn(-4.2f, 4.2f),
                    0f,
                    hit.z.coerceIn(-4.2f, 4.2f)
                )
            }
        }
        val camTarget = snapshot.camera.target
        return Vec3(camTarget.x.coerceIn(-3f, 3f), 0f, camTarget.z.coerceIn(-3f, 3f))
    }

    fun resolveSelectedGizmoOrigin(state: StudioRenderSnapshot = snapshot): Pair<Vec3, FloatArray>? {
        when (state.selectedPart) {
            SelectedPart.LIGHT -> {
                val pos = lightGizmoPosition(state.light)
                return pos to Math3D.translate(Math3D.identity(), pos.x, pos.y, pos.z)
            }
            SelectedPart.MODEL -> {
                val prop = state.props.firstOrNull { it.id == state.selectedEntityId } ?: return null
                val m = Math3D.rotateEulerDeg(
                    Math3D.translate(Math3D.identity(), prop.position.x, prop.position.y, prop.position.z),
                    prop.rotationEuler.x, prop.rotationEuler.y, prop.rotationEuler.z
                )
                return prop.position to m
            }
            SelectedPart.SKEL_MODEL, SelectedPart.CONTROL, SelectedPart.HAND, SelectedPart.HEAD, SelectedPart.HAIR -> {
                val ch = state.characters.firstOrNull { it.id == state.selectedEntityId } ?: return null
                if (state.selectedPart == SelectedPart.SKEL_MODEL) {
                    val p = ch.position + Vec3(0f, 0.9f * ch.scale, 0f)
                    val m = Math3D.rotateEulerDeg(
                        Math3D.translate(Math3D.identity(), p.x, p.y, p.z),
                        ch.rotationEuler.x, ch.rotationEuler.y, ch.rotationEuler.z
                    )
                    return p to m
                }
                val built = SkeletonBuilder.buildCharacter(ch, state.showSportswear, ch.id, state.selectedJoint, state.selectedPart)
                val jId = state.selectedJoint ?: JointId.HEAD
                val jm = built.jointTransforms[jId] ?: return null
                val p = if (jId == JointId.HEAD) built.headCenter else Math3D.transformPoint(jm, Vec3())
                return p to Math3D.translate(Math3D.identity(), p.x, p.y, p.z)
            }
            else -> return null
        }
    }

    companion object {
        fun computeCameraPosition(cam: CameraSettings): Vec3 {
            val az = cam.azimuthDeg * PI.toFloat() / 180f
            val el = cam.elevationDeg * PI.toFloat() / 180f
            val cosEl = cos(el)
            val x = cam.target.x + cam.distance * cosEl * sin(az)
            val y = cam.target.y + cam.distance * sin(el)
            val z = cam.target.z + cam.distance * cosEl * cos(az)
            return Vec3(x, y, z)
        }

        fun computeLightDirection(light: LightSettings): Vec3 {
            val az = light.azimuthDeg * PI.toFloat() / 180f
            val el = light.elevationDeg * PI.toFloat() / 180f
            val cosEl = cos(el)
            return Vec3(cosEl * sin(az), sin(el), cosEl * cos(az)).normalized()
        }

        fun lightGizmoPosition(light: LightSettings): Vec3 {
            val dir = computeLightDirection(light)
            return Vec3(0f, 1.0f, 0f) + dir * 1.9f
        }

        private fun propMeshKey(type: PropType): RenderMeshKey = when (type) {
            PropType.SPHERE -> RenderMeshKey.SPHERE
            PropType.CUBE -> RenderMeshKey.CUBE
            PropType.CONE -> RenderMeshKey.CONE
            PropType.CYLINDER -> RenderMeshKey.CYLINDER
            PropType.HEX -> RenderMeshKey.HEX
            PropType.PYRAMID -> RenderMeshKey.PYRAMID
            PropType.RECTANGLE -> RenderMeshKey.RECTANGLE
            PropType.STAIRS -> RenderMeshKey.STAIRS
            PropType.TORUS -> RenderMeshKey.TORUS
            PropType.WALL -> RenderMeshKey.WALL
            PropType.CHAIR -> RenderMeshKey.CHAIR
            PropType.STOOL -> RenderMeshKey.STOOL
            PropType.TABLE -> RenderMeshKey.TABLE
            PropType.SOFA -> RenderMeshKey.SOFA
            PropType.LAMP -> RenderMeshKey.LAMP
            PropType.PEDESTAL -> RenderMeshKey.PEDESTAL
        }

        private fun propHalfExtents(type: PropType): Vec3 = when (type) {
            PropType.RECTANGLE -> Vec3(0.65f, 0.4f, 0.15f)
            PropType.WALL -> Vec3(0.95f, 0.95f, 0.15f)
            PropType.CHAIR -> Vec3(0.28f, 0.48f, 0.28f)
            PropType.STOOL -> Vec3(0.24f, 0.38f, 0.24f)
            PropType.TABLE -> Vec3(0.55f, 0.40f, 0.36f)
            PropType.SOFA -> Vec3(0.66f, 0.42f, 0.38f)
            PropType.LAMP -> Vec3(0.26f, 0.80f, 0.26f)
            PropType.PEDESTAL -> Vec3(0.32f, 0.48f, 0.32f)
            else -> Vec3(0.55f, 0.55f, 0.55f)
        }

        private fun compileShader(type: Int, code: String): Int {
            val shader = GLES30.glCreateShader(type)
            GLES30.glShaderSource(shader, code)
            GLES30.glCompileShader(shader)
            return shader
        }

        private fun buildProgram(vs: String, fs: String): Int {
            val v = compileShader(GLES30.GL_VERTEX_SHADER, vs)
            val f = compileShader(GLES30.GL_FRAGMENT_SHADER, fs)
            val p = GLES30.glCreateProgram()
            GLES30.glAttachShader(p, v)
            GLES30.glAttachShader(p, f)
            GLES30.glLinkProgram(p)
            return p
        }

        private const val VERTEX_SHADER = """#version 300 es
            layout(location = 0) in vec3 aPos;
            layout(location = 1) in vec3 aNormal;
            uniform mat4 uMVP;
            uniform mat4 uModel;
            out vec3 vNormal;
            out vec3 vWorldPos;
            void main() {
                vec4 wp = uModel * vec4(aPos, 1.0);
                vWorldPos = wp.xyz;
                vNormal = mat3(transpose(inverse(uModel))) * aNormal;
                gl_Position = uMVP * vec4(aPos, 1.0);
            }
        """

        private const val FRAGMENT_SHADER = """#version 300 es
            precision mediump float;
            in vec3 vNormal;
            in vec3 vWorldPos;
            uniform vec3 uColor;
            uniform vec3 uLightDir;
            uniform vec3 uCamPos;
            uniform float uBrightness;
            uniform vec3 uLightColor;
            uniform float uGI;
            uniform float uUnlit;
            uniform float uHighlight;
            out vec4 fragColor;
            void main() {
                if (uUnlit > 0.5) {
                    fragColor = vec4(uColor, 1.0);
                    return;
                }
                vec3 N = normalize(vNormal);
                vec3 L = normalize(uLightDir);
                vec3 V = normalize(uCamPos - vWorldPos);
                vec3 H = normalize(L + V);

                float ndl = max(dot(N, L), 0.0);
                float hemi = 0.5 + 0.5 * N.y;
                vec3 giSky = vec3(0.75, 0.85, 0.98) * mix(vec3(1.0), uLightColor, 0.32);
                vec3 giGround = vec3(0.28, 0.24, 0.22) * mix(vec3(1.0), uLightColor, 0.20);
                vec3 ambient = mix(giGround, giSky, hemi) * (0.20 + 0.65 * uGI);

                vec3 directDiffuse = ndl * (1.15 * uBrightness) * uLightColor;
                vec3 spec = vec3(pow(max(dot(N, H), 0.0), 36.0) * 0.26 * uBrightness) * uLightColor;
                vec3 rim = vec3(pow(1.0 - max(dot(N, V), 0.0), 3.0) * (0.06 + 0.10 * uBrightness)) * mix(vec3(1.0), uLightColor, 0.5);

                vec3 lit = uColor * (ambient + directDiffuse) + spec + rim;
                vec3 finalCol = mix(lit, vec3(0.30, 0.85, 0.92), uHighlight * 0.22);
                fragColor = vec4(clamp(finalCol, 0.0, 1.0), 1.0);
            }
        """

        private const val SKY_VERTEX_SHADER = """#version 300 es
            out vec2 vUv;
            void main() {
                vec2 pos[3] = vec2[](
                    vec2(-1.0, -1.0),
                    vec2(3.0, -1.0),
                    vec2(-1.0, 3.0)
                );
                vUv = pos[gl_VertexID] * 0.5 + 0.5;
                gl_Position = vec4(pos[gl_VertexID], 0.999, 1.0);
            }
        """

        private const val SKY_FRAGMENT_SHADER = """#version 300 es
            precision mediump float;
            in vec2 vUv;
            out vec4 fragColor;
            void main() {
                vec3 horizon = vec3(0.68, 0.78, 0.88);
                vec3 zenith = vec3(0.22, 0.40, 0.64);
                vec3 ground = vec3(0.24, 0.27, 0.33);
                float t = clamp((vUv.y - 0.35) / 0.65, 0.0, 1.0);
                vec3 skyCol = mix(horizon, zenith, pow(t, 0.75));
                vec3 col = vUv.y < 0.35 ? mix(ground, horizon, clamp(vUv.y / 0.35, 0.0, 1.0)) : skyCol;
                fragColor = vec4(col, 1.0);
            }
        """
    }
}
