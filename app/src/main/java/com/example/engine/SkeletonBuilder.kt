package com.example.engine

import com.example.model.CharacterEntity
import com.example.model.HairStyle
import com.example.model.HandFingerState
import com.example.model.JointId
import com.example.model.RgbColor
import com.example.model.SelectedPart
import com.example.model.Vec3

enum class RenderMeshKey {
    SPHERE,
    CUBE,
    CONE,
    CYLINDER,
    HEX,
    PYRAMID,
    RECTANGLE,
    STAIRS,
    TORUS,
    WALL,
    HEAD,
    LIMB_CAPSULE
}

data class RenderItem(
    val meshKey: RenderMeshKey,
    val hairStyle: HairStyle? = null,
    val modelMatrix: FloatArray,
    val color: RgbColor,
    val isHighlighted: Boolean = false
)

data class PickBoneVolume(
    val entityId: String,
    val part: SelectedPart,
    val jointId: JointId?,
    val worldCenter: Vec3,
    val pickRadius: Float,
    val worldMatrix: FloatArray,
    val halfExtents: Vec3? = null
)

data class JointGizmoTransform(
    val entityId: String,
    val jointId: JointId?,
    val worldMatrix: FloatArray, // rotation + translation at gizmo origin
    val worldPos: Vec3
)

data class BuiltCharacterScene(
    val renderItems: List<RenderItem>,
    val pickVolumes: List<PickBoneVolume>,
    val jointTransforms: Map<JointId, FloatArray>,
    val headCenter: Vec3,
    val leftHandCenter: Vec3,
    val rightHandCenter: Vec3
)

object SkeletonBuilder {

    fun buildCharacter(
        ch: CharacterEntity,
        showSportswear: Boolean,
        selectedEntityId: String?,
        selectedJoint: JointId?,
        selectedPart: SelectedPart
    ): BuiltCharacterScene {
        val items = ArrayList<RenderItem>(48)
        val picks = ArrayList<PickBoneVolume>(32)
        val jointMats = HashMap<JointId, FloatArray>(20)

        val p = ch.type.proportions
        val isCharSelected = (selectedEntityId == ch.id)
        val baseColor = ch.bodyColor
        val wearColor = if (showSportswear) {
            RgbColor(0.20f, 0.25f, 0.34f)
        } else {
            baseColor
        }
        val jointBallColor = RgbColor(
            (baseColor.r * 0.88f).coerceIn(0f, 1f),
            (baseColor.g * 0.88f).coerceIn(0f, 1f),
            (baseColor.b * 0.88f).coerceIn(0f, 1f)
        )

        fun rot(id: JointId): Vec3 = ch.jointRotations[id] ?: Vec3()

        fun applyJoint(parent: FloatArray, offset: Vec3, id: JointId): FloatArray {
            val t = Math3D.translate(parent, offset.x, offset.y, offset.z)
            val r = rot(id)
            val rotated = Math3D.rotateEulerDeg(t, r.x, r.y, r.z)
            jointMats[id] = rotated
            return rotated
        }

        fun addSegment(
            jointMat: FloatArray,
            lenY: Float,
            rx: Float,
            rz: Float,
            color: RgbColor,
            highlighted: Boolean
        ) {
            val m = Math3D.translate(jointMat, 0f, lenY * 0.5f, 0f)
            val scaled = Math3D.scale(m, rx * 2f, kotlin.math.abs(lenY), rz * 2f)
            items.add(RenderItem(RenderMeshKey.SPHERE, null, scaled, color, highlighted))
        }

        fun addJointBall(jointMat: FloatArray, radius: Float, highlighted: Boolean) {
            val scaled = Math3D.scale(jointMat, radius * 2f, radius * 2f, radius * 2f)
            items.add(RenderItem(RenderMeshKey.SPHERE, null, scaled, jointBallColor, highlighted))
        }

        fun isJointHighlighted(jointId: JointId, fallbackPart: SelectedPart? = null): Boolean {
            if (!isCharSelected) return false
            if (selectedPart == SelectedPart.SKEL_MODEL) return true
            if (selectedJoint == jointId) return true
            if (fallbackPart != null && selectedPart == fallbackPart) return true
            return false
        }

        // Root matrix
        var rootMat = Math3D.identity()
        rootMat = Math3D.translate(rootMat, ch.position.x, ch.position.y, ch.position.z)
        rootMat = Math3D.rotateEulerDeg(rootMat, ch.rotationEuler.x, ch.rotationEuler.y, ch.rotationEuler.z)
        rootMat = Math3D.scale(rootMat, ch.scale, ch.scale, ch.scale)

        val legLenScale = p.heightScale
        val hipHeight = 0.88f * legLenScale + ch.pelvisOffsetY

        // Pelvis
        val pelvisMat = applyJoint(rootMat, Vec3(0f, hipHeight, 0f), JointId.PELVIS)
        val pelvisHighlight = isJointHighlighted(JointId.PELVIS)
        addSegment(pelvisMat, 0.14f, 0.125f * p.hipWidth, 0.088f * p.chestDepth, wearColor, pelvisHighlight)
        picks.add(
            PickBoneVolume(
                ch.id, SelectedPart.SKEL_MODEL, JointId.PELVIS,
                Math3D.transformPoint(pelvisMat, Vec3(0f, 0.07f, 0f)),
                0.15f * ch.scale, pelvisMat
            )
        )

        // Spine
        val spineMat = applyJoint(pelvisMat, Vec3(0f, 0.14f, 0f), JointId.SPINE)
        addSegment(spineMat, 0.16f * legLenScale, 0.105f * p.shoulderWidth, 0.080f * p.chestDepth, baseColor, isJointHighlighted(JointId.SPINE))
        picks.add(
            PickBoneVolume(
                ch.id, SelectedPart.CONTROL, JointId.SPINE,
                Math3D.transformPoint(spineMat, Vec3(0f, 0.08f, 0f)),
                0.12f * ch.scale, spineMat
            )
        )

        // Chest
        val chestMat = applyJoint(spineMat, Vec3(0f, 0.16f * legLenScale, 0f), JointId.CHEST)
        addSegment(chestMat, 0.22f * legLenScale, 0.135f * p.shoulderWidth, 0.095f * p.chestDepth, wearColor, isJointHighlighted(JointId.CHEST))
        picks.add(
            PickBoneVolume(
                ch.id, SelectedPart.CONTROL, JointId.CHEST,
                Math3D.transformPoint(chestMat, Vec3(0f, 0.11f, 0f)),
                0.14f * ch.scale, chestMat
            )
        )

        // Neck & Head
        val neckMat = applyJoint(chestMat, Vec3(0f, 0.22f * legLenScale, 0f), JointId.NECK)
        addSegment(neckMat, 0.08f, 0.042f * p.limbThickness, 0.042f * p.limbThickness, baseColor, isJointHighlighted(JointId.NECK))

        val headMat = applyJoint(neckMat, Vec3(0f, 0.075f, 0f), JointId.HEAD)
        val headScaled = Math3D.scale(headMat, p.headScale, p.headScale, p.headScale)
        val headHighlight = isJointHighlighted(JointId.HEAD, SelectedPart.HEAD) || isJointHighlighted(JointId.HEAD, SelectedPart.HAIR)
        items.add(RenderItem(RenderMeshKey.HEAD, null, headScaled, baseColor, headHighlight))
        if (ch.hairStyle != HairStyle.NAKED) {
            items.add(RenderItem(RenderMeshKey.HEAD, ch.hairStyle, headScaled, ch.hairColor, headHighlight))
        }
        val headCenter = Math3D.transformPoint(headMat, Vec3(0f, 0.11f * p.headScale, 0f))
        picks.add(
            PickBoneVolume(
                ch.id, SelectedPart.HEAD, JointId.HEAD,
                headCenter, 0.13f * p.headScale * ch.scale, headMat
            )
        )

        // Arms helper
        fun buildArm(isLeft: Boolean): Vec3 {
            val side = if (isLeft) -1f else 1f
            val shoulderId = if (isLeft) JointId.L_SHOULDER else JointId.R_SHOULDER
            val upperId = if (isLeft) JointId.L_UPPER_ARM else JointId.R_UPPER_ARM
            val foreId = if (isLeft) JointId.L_FOREARM else JointId.R_FOREARM
            val handId = if (isLeft) JointId.L_HAND else JointId.R_HAND
            val fingerState = if (isLeft) ch.leftHand else ch.rightHand

            val shoulderOffsetX = side * 0.155f * p.shoulderWidth
            val shoulderMat = applyJoint(chestMat, Vec3(shoulderOffsetX, 0.19f * legLenScale, 0f), shoulderId)
            val upperMat = applyJoint(shoulderMat, Vec3(0f, 0f, 0f), upperId)
            addJointBall(upperMat, 0.045f * p.limbThickness, isJointHighlighted(upperId))
            addSegment(upperMat, -0.25f * legLenScale, 0.042f * p.limbThickness, 0.042f * p.limbThickness, baseColor, isJointHighlighted(upperId))
            picks.add(
                PickBoneVolume(
                    ch.id, SelectedPart.CONTROL, upperId,
                    Math3D.transformPoint(upperMat, Vec3(0f, -0.12f * legLenScale, 0f)),
                    0.075f * ch.scale, upperMat
                )
            )

            val foreMat = applyJoint(upperMat, Vec3(0f, -0.25f * legLenScale, 0f), foreId)
            addJointBall(foreMat, 0.036f * p.limbThickness, isJointHighlighted(foreId))
            addSegment(foreMat, -0.23f * legLenScale, 0.035f * p.limbThickness, 0.035f * p.limbThickness, baseColor, isJointHighlighted(foreId))
            picks.add(
                PickBoneVolume(
                    ch.id, SelectedPart.CONTROL, foreId,
                    Math3D.transformPoint(foreMat, Vec3(0f, -0.11f * legLenScale, 0f)),
                    0.07f * ch.scale, foreMat
                )
            )

            val handMat = applyJoint(foreMat, Vec3(0f, -0.23f * legLenScale, 0f), handId)
            val handHi = isJointHighlighted(handId, SelectedPart.HAND)
            addJointBall(handMat, 0.025f * p.limbThickness, handHi)
            // Palm
            val palmM = Math3D.translate(handMat, 0f, -0.042f, 0f)
            val palmScaled = Math3D.scale(palmM, 0.042f * p.limbThickness, 0.072f, 0.022f)
            items.add(RenderItem(RenderMeshKey.CUBE, null, palmScaled, baseColor, handHi))

            // 5 Articulated Fingers
            addFingers(items, handMat, side, fingerState, p.limbThickness, baseColor, handHi)

            val handCenter = Math3D.transformPoint(handMat, Vec3(0f, -0.05f, 0f))
            picks.add(
                PickBoneVolume(
                    ch.id, SelectedPart.HAND, handId,
                    handCenter, 0.085f * ch.scale, handMat
                )
            )
            return handCenter
        }

        val lHandCenter = buildArm(true)
        val rHandCenter = buildArm(false)

        // Legs helper
        fun buildLeg(isLeft: Boolean) {
            val side = if (isLeft) -1f else 1f
            val thighId = if (isLeft) JointId.L_THIGH else JointId.R_THIGH
            val shinId = if (isLeft) JointId.L_SHIN else JointId.R_SHIN
            val footId = if (isLeft) JointId.L_FOOT else JointId.R_FOOT

            val thighMat = applyJoint(pelvisMat, Vec3(side * 0.088f * p.hipWidth, 0.01f, 0f), thighId)
            addJointBall(thighMat, 0.056f * p.limbThickness, isJointHighlighted(thighId))
            addSegment(thighMat, -0.40f * legLenScale, 0.062f * p.limbThickness, 0.064f * p.limbThickness, baseColor, isJointHighlighted(thighId))
            picks.add(
                PickBoneVolume(
                    ch.id, SelectedPart.CONTROL, thighId,
                    Math3D.transformPoint(thighMat, Vec3(0f, -0.20f * legLenScale, 0f)),
                    0.09f * ch.scale, thighMat
                )
            )

            val shinMat = applyJoint(thighMat, Vec3(0f, -0.40f * legLenScale, 0f), shinId)
            addJointBall(shinMat, 0.044f * p.limbThickness, isJointHighlighted(shinId))
            addSegment(shinMat, -0.40f * legLenScale, 0.046f * p.limbThickness, 0.048f * p.limbThickness, baseColor, isJointHighlighted(shinId))
            picks.add(
                PickBoneVolume(
                    ch.id, SelectedPart.CONTROL, shinId,
                    Math3D.transformPoint(shinMat, Vec3(0f, -0.20f * legLenScale, 0f)),
                    0.085f * ch.scale, shinMat
                )
            )

            val footMat = applyJoint(shinMat, Vec3(0f, -0.40f * legLenScale, 0f), footId)
            val footHi = isJointHighlighted(footId)
            addJointBall(footMat, 0.032f * p.limbThickness, footHi)
            val footM = Math3D.translate(footMat, 0f, -0.04f, 0.045f)
            val footScaled = Math3D.scale(footM, 0.068f * p.limbThickness, 0.05f, 0.16f)
            items.add(RenderItem(RenderMeshKey.CUBE, null, footScaled, baseColor, footHi))
            picks.add(
                PickBoneVolume(
                    ch.id, SelectedPart.CONTROL, footId,
                    Math3D.transformPoint(footMat, Vec3(0f, -0.03f, 0.04f)),
                    0.08f * ch.scale, footMat
                )
            )
        }

        buildLeg(true)
        buildLeg(false)

        return BuiltCharacterScene(
            renderItems = items,
            pickVolumes = picks,
            jointTransforms = jointMats,
            headCenter = headCenter,
            leftHandCenter = lHandCenter,
            rightHandCenter = rHandCenter
        )
    }

    private fun addFingers(
        items: MutableList<RenderItem>,
        handMat: FloatArray,
        side: Float, // -1 left, +1 right
        fingers: HandFingerState,
        thickness: Float,
        color: RgbColor,
        highlighted: Boolean
    ) {
        val curls = floatArrayOf(
            fingers.thumbCurl,
            fingers.indexCurl,
            fingers.middleCurl,
            fingers.ringCurl,
            fingers.pinkyCurl
        )
        val lengths = floatArrayOf(0.036f, 0.048f, 0.054f, 0.048f, 0.038f)
        val xOffsets = floatArrayOf(-side * 0.024f, -side * 0.015f, -side * 0.005f, side * 0.006f, side * 0.016f)
        val yOffsets = floatArrayOf(-0.042f, -0.076f, -0.078f, -0.076f, -0.072f)
        val spreadAngles = floatArrayOf(side * (22f + fingers.spread * 16f), side * fingers.spread * 12f, 0f, -side * fingers.spread * 10f, -side * fingers.spread * 18f)

        for (i in 0..4) {
            val curlDeg = curls[i] * 85f
            var fm = Math3D.translate(handMat, xOffsets[i], yOffsets[i], 0.004f)
            fm = Math3D.rotateEulerDeg(fm, curlDeg, 0f, spreadAngles[i])
            val segLen = lengths[i] * 0.55f
            val s1 = Math3D.translate(fm, 0f, -segLen * 0.5f, 0f)
            items.add(
                RenderItem(
                    RenderMeshKey.SPHERE, null,
                    Math3D.scale(s1, 0.011f * thickness, segLen, 0.011f * thickness),
                    color, highlighted
                )
            )
            // Distal phalanx
            var fm2 = Math3D.translate(fm, 0f, -segLen, 0f)
            fm2 = Math3D.rotateEulerDeg(fm2, curlDeg * 0.9f, 0f, 0f)
            val segLen2 = lengths[i] * 0.45f
            val s2 = Math3D.translate(fm2, 0f, -segLen2 * 0.5f, 0f)
            items.add(
                RenderItem(
                    RenderMeshKey.SPHERE, null,
                    Math3D.scale(s2, 0.009f * thickness, segLen2, 0.009f * thickness),
                    color, highlighted
                )
            )
        }
    }
}
