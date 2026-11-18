package com.example.model

data class BodyPosePreset(
    val index: Int,
    val assetPath: String,
    val pelvisOffsetY: Float,
    val jointRotations: Map<JointId, Vec3>
)

data class HandPosePreset(
    val index: Int,
    val assetPath: String,
    val fingerState: HandFingerState
)

object PosePresets {

    val bodyPoses: List<BodyPosePreset> = List(25) { idx ->
        val num = idx.toString().padStart(2, '0')
        val path = "imgs/body_poses/image_$num.png"
        val (pelvisY, joints) = when (idx) {
            0 -> 0f to mapOf( // Natural standing A-pose
                JointId.L_UPPER_ARM to Vec3(0f, 0f, -14f),
                JointId.R_UPPER_ARM to Vec3(0f, 0f, 14f),
                JointId.L_FOREARM to Vec3(-8f, 0f, -4f),
                JointId.R_FOREARM to Vec3(-8f, 0f, 4f)
            )
            1 -> 0f to mapOf( // Contrapposto hip-shift pose
                JointId.PELVIS to Vec3(0f, -10f, 4f),
                JointId.SPINE to Vec3(2f, 6f, -5f),
                JointId.HEAD to Vec3(4f, 8f, -3f),
                JointId.L_UPPER_ARM to Vec3(6f, 0f, -18f),
                JointId.R_UPPER_ARM to Vec3(-12f, 0f, 26f),
                JointId.R_FOREARM to Vec3(-52f, 0f, 15f),
                JointId.L_THIGH to Vec3(-6f, 0f, -4f),
                JointId.R_THIGH to Vec3(10f, 8f, 6f),
                JointId.R_SHIN to Vec3(14f, 0f, 0f)
            )
            2 -> -0.04f to mapOf( // Confident walk stride
                JointId.PELVIS to Vec3(4f, 12f, 0f),
                JointId.SPINE to Vec3(4f, -10f, 0f),
                JointId.HEAD to Vec3(-4f, -4f, 0f),
                JointId.L_UPPER_ARM to Vec3(-26f, 0f, -12f),
                JointId.L_FOREARM to Vec3(-28f, 0f, 0f),
                JointId.R_UPPER_ARM to Vec3(28f, 0f, 12f),
                JointId.R_FOREARM to Vec3(-18f, 0f, 0f),
                JointId.L_THIGH to Vec3(-26f, 0f, -2f),
                JointId.L_SHIN to Vec3(16f, 0f, 0f),
                JointId.R_THIGH to Vec3(24f, 0f, 2f),
                JointId.R_SHIN to Vec3(32f, 0f, 0f)
            )
            3 -> -0.08f to mapOf( // Dynamic sprint / run
                JointId.PELVIS to Vec3(16f, 18f, 0f),
                JointId.SPINE to Vec3(14f, -18f, 0f),
                JointId.HEAD to Vec3(-14f, 0f, 0f),
                JointId.L_UPPER_ARM to Vec3(-55f, 0f, -14f),
                JointId.L_FOREARM to Vec3(-88f, 0f, 0f),
                JointId.R_UPPER_ARM to Vec3(48f, 0f, 14f),
                JointId.R_FOREARM to Vec3(-72f, 0f, 0f),
                JointId.L_THIGH to Vec3(-52f, 0f, -2f),
                JointId.L_SHIN to Vec3(62f, 0f, 0f),
                JointId.R_THIGH to Vec3(42f, 0f, 2f),
                JointId.R_SHIN to Vec3(74f, 0f, 0f)
            )
            4 -> -0.36f to mapOf( // Seated relaxed
                JointId.PELVIS to Vec3(-10f, 0f, 0f),
                JointId.SPINE to Vec3(12f, 0f, 0f),
                JointId.HEAD to Vec3(-6f, 8f, 0f),
                JointId.L_UPPER_ARM to Vec3(12f, 0f, -22f),
                JointId.L_FOREARM to Vec3(-48f, 18f, 0f),
                JointId.R_UPPER_ARM to Vec3(12f, 0f, 22f),
                JointId.R_FOREARM to Vec3(-48f, -18f, 0f),
                JointId.L_THIGH to Vec3(-86f, 0f, -8f),
                JointId.L_SHIN to Vec3(88f, 0f, 0f),
                JointId.R_THIGH to Vec3(-86f, 0f, 8f),
                JointId.R_SHIN to Vec3(88f, 0f, 0f)
            )
            5 -> -0.34f to mapOf( // Cross-legged sit / lean
                JointId.PELVIS to Vec3(-8f, -14f, 0f),
                JointId.SPINE to Vec3(14f, 10f, 0f),
                JointId.HEAD to Vec3(-8f, 6f, 4f),
                JointId.L_UPPER_ARM to Vec3(18f, 0f, -28f),
                JointId.L_FOREARM to Vec3(-24f, 0f, 0f),
                JointId.R_UPPER_ARM to Vec3(-22f, 0f, 22f),
                JointId.R_FOREARM to Vec3(-72f, -24f, 0f),
                JointId.L_THIGH to Vec3(-88f, 0f, -12f),
                JointId.L_SHIN to Vec3(92f, 0f, 0f),
                JointId.R_THIGH to Vec3(-95f, -18f, 14f),
                JointId.R_SHIN to Vec3(72f, 0f, 0f)
            )
            6 -> -0.28f to mapOf( // Action crouch
                JointId.PELVIS to Vec3(24f, 18f, 0f),
                JointId.SPINE to Vec3(22f, -16f, 0f),
                JointId.HEAD to Vec3(-28f, 12f, 0f),
                JointId.L_UPPER_ARM to Vec3(-40f, 0f, -32f),
                JointId.L_FOREARM to Vec3(-65f, 0f, 0f),
                JointId.R_UPPER_ARM to Vec3(34f, 0f, 44f),
                JointId.R_FOREARM to Vec3(-30f, 0f, 0f),
                JointId.L_THIGH to Vec3(-72f, 0f, -18f),
                JointId.L_SHIN to Vec3(98f, 0f, 0f),
                JointId.R_THIGH to Vec3(-34f, 0f, 18f),
                JointId.R_SHIN to Vec3(68f, 0f, 0f)
            )
            7 -> 0.34f to mapOf( // Mid-air leap
                JointId.PELVIS to Vec3(-12f, 12f, 0f),
                JointId.SPINE to Vec3(-14f, -8f, 0f),
                JointId.HEAD to Vec3(10f, 0f, 0f),
                JointId.L_UPPER_ARM to Vec3(-130f, 0f, -24f),
                JointId.L_FOREARM to Vec3(-22f, 0f, 0f),
                JointId.R_UPPER_ARM to Vec3(28f, 0f, 38f),
                JointId.R_FOREARM to Vec3(-38f, 0f, 0f),
                JointId.L_THIGH to Vec3(-68f, 0f, -8f),
                JointId.L_SHIN to Vec3(94f, 0f, 0f),
                JointId.R_THIGH to Vec3(28f, 0f, 8f),
                JointId.R_SHIN to Vec3(45f, 0f, 0f)
            )
            8 -> 0f to mapOf( // Wave hello
                JointId.PELVIS to Vec3(0f, 8f, -3f),
                JointId.SPINE to Vec3(-4f, -6f, 4f),
                JointId.HEAD to Vec3(-4f, -6f, -8f),
                JointId.L_UPPER_ARM to Vec3(6f, 0f, -16f),
                JointId.L_FOREARM to Vec3(-18f, 0f, 0f),
                JointId.R_UPPER_ARM to Vec3(-115f, 0f, 46f),
                JointId.R_FOREARM to Vec3(-52f, 0f, -12f),
                JointId.L_THIGH to Vec3(-4f, 0f, -6f),
                JointId.R_THIGH to Vec3(4f, 0f, 6f)
            )
            9 -> -0.05f to mapOf( // Martial arts guard
                JointId.PELVIS to Vec3(6f, 38f, 0f),
                JointId.SPINE to Vec3(8f, -26f, 0f),
                JointId.HEAD to Vec3(-6f, -12f, 0f),
                JointId.L_UPPER_ARM to Vec3(-68f, 24f, -22f),
                JointId.L_FOREARM to Vec3(-95f, 0f, 0f),
                JointId.R_UPPER_ARM to Vec3(-52f, -18f, 24f),
                JointId.R_FOREARM to Vec3(-102f, 0f, 0f),
                JointId.L_THIGH to Vec3(-36f, -12f, -14f),
                JointId.L_SHIN to Vec3(48f, 0f, 0f),
                JointId.R_THIGH to Vec3(28f, 12f, 14f),
                JointId.R_SHIN to Vec3(36f, 0f, 0f)
            )
            10 -> -0.02f to mapOf( // High kick
                JointId.PELVIS to Vec3(-18f, 26f, -12f),
                JointId.SPINE to Vec3(10f, -18f, 14f),
                JointId.HEAD to Vec3(4f, -10f, 0f),
                JointId.L_UPPER_ARM to Vec3(-45f, 0f, -35f),
                JointId.L_FOREARM to Vec3(-72f, 0f, 0f),
                JointId.R_UPPER_ARM to Vec3(22f, 0f, 42f),
                JointId.R_FOREARM to Vec3(-64f, 0f, 0f),
                JointId.L_THIGH to Vec3(8f, 0f, -6f),
                JointId.L_SHIN to Vec3(10f, 0f, 0f),
                JointId.R_THIGH to Vec3(-104f, 0f, 22f),
                JointId.R_SHIN to Vec3(12f, 0f, 0f)
            )
            11 -> -0.22f to mapOf( // One-knee kneel
                JointId.PELVIS to Vec3(8f, 14f, 0f),
                JointId.SPINE to Vec3(10f, -10f, 0f),
                JointId.HEAD to Vec3(-12f, 4f, 0f),
                JointId.L_UPPER_ARM to Vec3(-22f, 0f, -18f),
                JointId.L_FOREARM to Vec3(-42f, 0f, 0f),
                JointId.R_UPPER_ARM to Vec3(26f, 0f, 28f),
                JointId.R_FOREARM to Vec3(-24f, 0f, 0f),
                JointId.L_THIGH to Vec3(-78f, 0f, -10f),
                JointId.L_SHIN to Vec3(84f, 0f, 0f),
                JointId.R_THIGH to Vec3(14f, 0f, 10f),
                JointId.R_SHIN to Vec3(92f, 0f, 0f)
            )
            12 -> 0f to mapOf( // Hands on hips heroic
                JointId.PELVIS to Vec3(-4f, 0f, 0f),
                JointId.SPINE to Vec3(-6f, 0f, 0f),
                JointId.HEAD to Vec3(-6f, 0f, 0f),
                JointId.L_UPPER_ARM to Vec3(14f, 0f, -44f),
                JointId.L_FOREARM to Vec3(-24f, 0f, 74f),
                JointId.R_UPPER_ARM to Vec3(14f, 0f, 44f),
                JointId.R_FOREARM to Vec3(-24f, 0f, -74f),
                JointId.L_THIGH to Vec3(0f, 0f, -10f),
                JointId.R_THIGH to Vec3(0f, 0f, 10f)
            )
            13 -> 0f to mapOf( // Arms crossed
                JointId.PELVIS to Vec3(0f, -8f, 0f),
                JointId.SPINE to Vec3(-4f, 8f, 0f),
                JointId.HEAD to Vec3(4f, 4f, 0f),
                JointId.L_UPPER_ARM to Vec3(-42f, 28f, -18f),
                JointId.L_FOREARM to Vec3(-86f, 36f, 0f),
                JointId.R_UPPER_ARM to Vec3(-46f, -28f, 18f),
                JointId.R_FOREARM to Vec3(-88f, -36f, 0f),
                JointId.L_THIGH to Vec3(-4f, 0f, -6f),
                JointId.R_THIGH to Vec3(6f, 0f, 6f)
            )
            14 -> 0f to mapOf( // Sword / staff two-hand swing
                JointId.PELVIS to Vec3(6f, -28f, 0f),
                JointId.SPINE to Vec3(12f, -22f, 0f),
                JointId.HEAD to Vec3(-8f, 36f, 0f),
                JointId.L_UPPER_ARM to Vec3(-68f, 24f, -12f),
                JointId.L_FOREARM to Vec3(-46f, 12f, 0f),
                JointId.R_UPPER_ARM to Vec3(-74f, -22f, 16f),
                JointId.R_FOREARM to Vec3(-42f, -10f, 0f),
                JointId.L_THIGH to Vec3(-24f, 0f, -14f),
                JointId.L_SHIN to Vec3(26f, 0f, 0f),
                JointId.R_THIGH to Vec3(22f, 0f, 14f),
                JointId.R_SHIN to Vec3(18f, 0f, 0f)
            )
            15 -> 0f to mapOf( // Dramatic pointing forward
                JointId.PELVIS to Vec3(0f, 16f, 0f),
                JointId.SPINE to Vec3(6f, -12f, 0f),
                JointId.HEAD to Vec3(-4f, -4f, 0f),
                JointId.L_UPPER_ARM to Vec3(12f, 0f, -16f),
                JointId.L_FOREARM to Vec3(-26f, 0f, 0f),
                JointId.R_UPPER_ARM to Vec3(-88f, -8f, 8f),
                JointId.R_FOREARM to Vec3(-8f, 0f, 0f),
                JointId.L_THIGH to Vec3(-12f, 0f, -8f),
                JointId.R_THIGH to Vec3(14f, 0f, 8f),
                JointId.R_SHIN to Vec3(14f, 0f, 0f)
            )
            16 -> -0.03f to mapOf( // Idol / stage pose
                JointId.PELVIS to Vec3(0f, -12f, 6f),
                JointId.SPINE to Vec3(-6f, 10f, -8f),
                JointId.HEAD to Vec3(6f, 6f, -10f),
                JointId.L_UPPER_ARM to Vec3(-32f, 0f, -42f),
                JointId.L_FOREARM to Vec3(-74f, 0f, 0f),
                JointId.R_UPPER_ARM to Vec3(-118f, 0f, 28f),
                JointId.R_FOREARM to Vec3(-26f, 0f, 0f),
                JointId.L_THIGH to Vec3(-8f, 8f, -6f),
                JointId.L_SHIN to Vec3(18f, 0f, 0f),
                JointId.R_THIGH to Vec3(6f, 0f, 6f)
            )
            17 -> -0.06f to mapOf( // Archer draw
                JointId.PELVIS to Vec3(0f, 44f, 0f),
                JointId.SPINE to Vec3(0f, 18f, 0f),
                JointId.HEAD to Vec3(0f, -56f, 0f),
                JointId.L_UPPER_ARM to Vec3(-88f, 0f, -88f),
                JointId.L_FOREARM to Vec3(-6f, 0f, 0f),
                JointId.R_UPPER_ARM to Vec3(-82f, 0f, 68f),
                JointId.R_FOREARM to Vec3(-110f, 0f, 0f),
                JointId.L_THIGH to Vec3(-12f, 0f, -14f),
                JointId.L_SHIN to Vec3(18f, 0f, 0f),
                JointId.R_THIGH to Vec3(10f, 0f, 14f)
            )
            18 -> 0f to mapOf( // Thinking / chin rest
                JointId.PELVIS to Vec3(0f, 8f, -3f),
                JointId.SPINE to Vec3(6f, -6f, 4f),
                JointId.HEAD to Vec3(12f, -6f, 8f),
                JointId.L_UPPER_ARM to Vec3(-34f, 24f, -16f),
                JointId.L_FOREARM to Vec3(-72f, 24f, 0f),
                JointId.R_UPPER_ARM to Vec3(-56f, -16f, 14f),
                JointId.R_FOREARM to Vec3(-116f, -18f, 0f),
                JointId.L_THIGH to Vec3(4f, 0f, -5f),
                JointId.R_THIGH to Vec3(-6f, 0f, 5f)
            )
            19 -> -0.14f to mapOf( // Superhero landing prep
                JointId.PELVIS to Vec3(26f, 22f, 0f),
                JointId.SPINE to Vec3(28f, -16f, 0f),
                JointId.HEAD to Vec3(-24f, -8f, 0f),
                JointId.L_UPPER_ARM to Vec3(36f, 0f, -44f),
                JointId.L_FOREARM to Vec3(-36f, 0f, 0f),
                JointId.R_UPPER_ARM to Vec3(-68f, 0f, 18f),
                JointId.R_FOREARM to Vec3(-20f, 0f, 0f),
                JointId.L_THIGH to Vec3(-58f, 0f, -16f),
                JointId.L_SHIN to Vec3(84f, 0f, 0f),
                JointId.R_THIGH to Vec3(22f, 0f, 16f),
                JointId.R_SHIN to Vec3(58f, 0f, 0f)
            )
            20 -> 0.28f to mapOf( // Floating / levitating
                JointId.PELVIS to Vec3(-14f, 0f, 0f),
                JointId.SPINE to Vec3(-10f, 0f, 0f),
                JointId.HEAD to Vec3(14f, 0f, 0f),
                JointId.L_UPPER_ARM to Vec3(-28f, 0f, -52f),
                JointId.L_FOREARM to Vec3(-22f, 0f, 0f),
                JointId.R_UPPER_ARM to Vec3(-28f, 0f, 52f),
                JointId.R_FOREARM to Vec3(-22f, 0f, 0f),
                JointId.L_THIGH to Vec3(-22f, 0f, -6f),
                JointId.L_SHIN to Vec3(46f, 0f, 0f),
                JointId.R_THIGH to Vec3(14f, 0f, 6f),
                JointId.R_SHIN to Vec3(28f, 0f, 0f)
            )
            21 -> 0f to mapOf( // Back turn glance
                JointId.PELVIS to Vec3(0f, 140f, 0f),
                JointId.SPINE to Vec3(-4f, 18f, 0f),
                JointId.HEAD to Vec3(4f, 44f, -6f),
                JointId.L_UPPER_ARM to Vec3(10f, 0f, -18f),
                JointId.R_UPPER_ARM to Vec3(-18f, 0f, 22f),
                JointId.R_FOREARM to Vec3(-42f, 0f, 0f),
                JointId.L_THIGH to Vec3(-6f, 0f, -6f),
                JointId.R_THIGH to Vec3(8f, 0f, 6f)
            )
            22 -> 0f to mapOf( // Bowing / polite lean
                JointId.PELVIS to Vec3(24f, 0f, 0f),
                JointId.SPINE to Vec3(26f, 0f, 0f),
                JointId.HEAD to Vec3(12f, 0f, 0f),
                JointId.L_UPPER_ARM to Vec3(18f, 0f, -10f),
                JointId.R_UPPER_ARM to Vec3(18f, 0f, 10f),
                JointId.L_THIGH to Vec3(-16f, 0f, -4f),
                JointId.R_THIGH to Vec3(-16f, 0f, 4f)
            )
            23 -> -0.04f to mapOf( // Side lunge
                JointId.PELVIS to Vec3(8f, -16f, -6f),
                JointId.SPINE to Vec3(8f, 14f, 6f),
                JointId.HEAD to Vec3(-4f, 8f, 0f),
                JointId.L_UPPER_ARM to Vec3(-48f, 0f, -44f),
                JointId.L_FOREARM to Vec3(-28f, 0f, 0f),
                JointId.R_UPPER_ARM to Vec3(24f, 0f, 34f),
                JointId.R_FOREARM to Vec3(-56f, 0f, 0f),
                JointId.L_THIGH to Vec3(-34f, 0f, -26f),
                JointId.L_SHIN to Vec3(52f, 0f, 0f),
                JointId.R_THIGH to Vec3(14f, 0f, 22f)
            )
            else -> 0f to mapOf( // T-Pose reference
                JointId.L_UPPER_ARM to Vec3(0f, 0f, -82f),
                JointId.R_UPPER_ARM to Vec3(0f, 0f, 82f)
            )
        }
        BodyPosePreset(idx, path, pelvisY, joints)
    }

    val handPoses: List<HandPosePreset> = List(30) { idx ->
        val frame = (idx * 5).toString().padStart(4, '0')
        val path = "imgs/hand_poses/man_hand.$frame.jpg"
        val state = when (idx) {
            0 -> HandFingerState(0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.5f) // Open wide
            1 -> HandFingerState(0.15f, 0.12f, 0.14f, 0.18f, 0.22f, 0.15f) // Relaxed
            2 -> HandFingerState(0.85f, 0.95f, 0.95f, 0.95f, 0.95f, 0.0f) // Closed fist
            3 -> HandFingerState(0.75f, 0.05f, 0.92f, 0.94f, 0.95f, 0.1f) // Point index
            4 -> HandFingerState(0.70f, 0.05f, 0.05f, 0.92f, 0.94f, 0.45f) // Peace V
            5 -> HandFingerState(0.65f, 0.05f, 0.05f, 0.05f, 0.92f, 0.35f) // Three fingers
            6 -> HandFingerState(0.65f, 0.05f, 0.05f, 0.05f, 0.05f, 0.3f) // Four fingers
            7 -> HandFingerState(0.05f, 0.92f, 0.94f, 0.94f, 0.95f, 0.2f) // Thumbs up
            8 -> HandFingerState(0.55f, 0.58f, 0.12f, 0.15f, 0.18f, 0.25f) // OK pinch
            9 -> HandFingerState(0.10f, 0.05f, 0.90f, 0.92f, 0.05f, 0.4f) // Rock / horns
            10 -> HandFingerState(0.05f, 0.05f, 0.92f, 0.94f, 0.94f, 0.3f) // Finger gun
            11 -> HandFingerState(0.42f, 0.48f, 0.48f, 0.50f, 0.52f, 0.25f) // Cupped hold
            12 -> HandFingerState(0.55f, 0.65f, 0.68f, 0.70f, 0.72f, 0.15f) // Claw
            13 -> HandFingerState(0.25f, 0.22f, 0.24f, 0.26f, 0.28f, 0.02f) // Flat blade
            14 -> HandFingerState(0.30f, 0.38f, 0.40f, 0.42f, 0.44f, 0.2f) // Light grip
            15 -> HandFingerState(0.60f, 0.75f, 0.78f, 0.80f, 0.82f, 0.05f) // Tight grip
            16 -> HandFingerState(0.12f, 0.10f, 0.12f, 0.45f, 0.65f, 0.22f) // Graceful reach
            17 -> HandFingerState(0.20f, 0.52f, 0.55f, 0.58f, 0.60f, 0.10f) // Pen hold
            18 -> HandFingerState(0.05f, 0.90f, 0.92f, 0.94f, 0.05f, 0.55f) // Shaka / call me
            19 -> HandFingerState(0.45f, 0.18f, 0.35f, 0.55f, 0.70f, 0.28f) // Natural cascade
            else -> {
                val t = (idx - 19) / 10f
                HandFingerState(
                    thumbCurl = 0.2f + 0.5f * t,
                    indexCurl = 0.15f + 0.65f * t,
                    middleCurl = 0.2f + 0.65f * t,
                    ringCurl = 0.25f + 0.65f * t,
                    pinkyCurl = 0.3f + 0.6f * t,
                    spread = 0.35f * (1f - t)
                )
            }
        }
        HandPosePreset(idx, path, state)
    }

    val materialSwatches: List<RgbColor> = listOf(
        RgbColor(0.95f, 0.26f, 0.21f),
        RgbColor(0.91f, 0.12f, 0.39f),
        RgbColor(0.61f, 0.15f, 0.69f),
        RgbColor(0.40f, 0.23f, 0.72f),
        RgbColor(0.25f, 0.32f, 0.71f),
        RgbColor(0.13f, 0.59f, 0.95f),
        RgbColor(0.01f, 0.66f, 0.96f),
        RgbColor(0.00f, 0.74f, 0.83f),
        RgbColor(0.00f, 0.59f, 0.53f),
        RgbColor(0.30f, 0.69f, 0.31f),
        RgbColor(0.55f, 0.76f, 0.29f),
        RgbColor(0.80f, 0.86f, 0.22f),
        RgbColor(1.00f, 0.92f, 0.23f),
        RgbColor(1.00f, 0.76f, 0.03f),
        RgbColor(1.00f, 0.60f, 0.00f),
        RgbColor(1.00f, 0.34f, 0.13f),
        RgbColor(0.47f, 0.33f, 0.28f),
        RgbColor(0.38f, 0.49f, 0.55f)
    )

    // Exact 15 skin tones from MaterialMenu.js
    val skinToneSwatches: List<RgbColor> = listOf(
        hex("#ffe9dc"),
        hex("#fce9db"),
        hex("#e0a899"),
        hex("#dfa290"),
        hex("#c99789"),
        hex("#fbead4"),
        hex("#f9d6c3"),
        hex("#dbaf92"),
        hex("#a97754"),
        hex("#765238"),
        hex("#ffdbac"),
        hex("#f1c27d"),
        hex("#e0ac69"),
        hex("#c68642"),
        hex("#8d5524")
    )

    private fun hex(s: String): RgbColor {
        val clean = s.removePrefix("#")
        val v = clean.toLong(16).toInt()
        return RgbColor(
            r = ((v shr 16) and 0xFF) / 255f,
            g = ((v shr 8) and 0xFF) / 255f,
            b = (v and 0xFF) / 255f
        )
    }
}
