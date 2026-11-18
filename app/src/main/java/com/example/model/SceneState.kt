package com.example.model

data class Vec3(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f
) {
    operator fun plus(other: Vec3) = Vec3(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vec3) = Vec3(x - other.x, y - other.y, z - other.z)
    operator fun times(s: Float) = Vec3(x * s, y * s, z * s)

    fun length(): Float = kotlin.math.sqrt(x * x + y * y + z * z)
    fun normalized(): Vec3 {
        val len = length()
        return if (len > 1e-6f) Vec3(x / len, y / len, z / len) else Vec3(0f, 1f, 0f)
    }

    fun dot(other: Vec3): Float = x * other.x + y * other.y + z * other.z
    fun cross(other: Vec3): Vec3 = Vec3(
        y * other.z - z * other.y,
        z * other.x - x * other.z,
        x * other.y - y * other.x
    )
}

data class RgbColor(
    val r: Float = 0.88f,
    val g: Float = 0.85f,
    val b: Float = 0.82f
)

enum class JointId(val label: String) {
    PELVIS("Pelvis"),
    SPINE("Spine"),
    CHEST("Chest"),
    NECK("Neck"),
    HEAD("Head"),
    L_SHOULDER("L Shoulder"),
    L_UPPER_ARM("L Upper Arm"),
    L_FOREARM("L Forearm"),
    L_HAND("L Hand"),
    R_SHOULDER("R Shoulder"),
    R_UPPER_ARM("R Upper Arm"),
    R_FOREARM("R Forearm"),
    R_HAND("R Hand"),
    L_THIGH("L Thigh"),
    L_SHIN("L Shin"),
    L_FOOT("L Foot"),
    R_THIGH("R Thigh"),
    R_SHIN("R Shin"),
    R_FOOT("R Foot")
}

data class HandFingerState(
    val thumbCurl: Float = 0.15f,
    val indexCurl: Float = 0.12f,
    val middleCurl: Float = 0.14f,
    val ringCurl: Float = 0.18f,
    val pinkyCurl: Float = 0.22f,
    val spread: Float = 0.15f
)

data class CharacterEntity(
    val id: String,
    val type: CharacterType = CharacterType.ANIME_KYO_FEMALE,
    val position: Vec3 = Vec3(0f, 0f, 0f),
    val rotationEuler: Vec3 = Vec3(0f, 0f, 0f),
    val scale: Float = 1.0f,
    val bodyColor: RgbColor = RgbColor(0.95f, 0.86f, 0.82f),
    val hairStyle: HairStyle = HairStyle.MIKU,
    val hairColor: RgbColor = RgbColor(0.28f, 0.72f, 0.78f),
    val locked: Boolean = false,
    val pelvisOffsetY: Float = 0f,
    val jointRotations: Map<JointId, Vec3> = emptyMap(),
    val leftHand: HandFingerState = HandFingerState(),
    val rightHand: HandFingerState = HandFingerState()
)

data class PropEntity(
    val id: String,
    val type: PropType = PropType.CUBE,
    val position: Vec3 = Vec3(0f, 0.5f, 0f),
    val rotationEuler: Vec3 = Vec3(0f, 0f, 0f),
    val scale: Vec3 = Vec3(1f, 1f, 1f),
    val color: RgbColor = RgbColor(0.78f, 0.82f, 0.88f),
    val locked: Boolean = false
)

data class LightSettings(
    val azimuthDeg: Float = 42f,
    val elevationDeg: Float = 52f,
    val brightness: Float = 0.85f,
    val giStrength: Float = 0.45f
)

data class CameraSettings(
    val target: Vec3 = Vec3(0f, 0.95f, 0f),
    val distance: Float = 3.8f,
    val azimuthDeg: Float = 22f,
    val elevationDeg: Float = 14f,
    val fovDeg: Float = 42f
)

data class SceneSnapshot(
    val characters: List<CharacterEntity>,
    val props: List<PropEntity>,
    val light: LightSettings,
    val selectedEntityId: String?,
    val selectedJoint: JointId?,
    val selectedPart: SelectedPart
)
