package com.example.model

import com.example.i18n.StudioStrings

enum class SelectedPart(val id: Int) {
    NOTHING(0),
    MODEL(1),
    CONTROL(2),
    LIGHT(3),
    FINGER(4),
    SKEL_MODEL(5),
    HAND(6),
    HEAD(7),
    HAIR(8)
}

enum class ManipulatorMode {
    TRANSLATE,
    ROTATE
}

enum class CameraPreset {
    LEFT,
    RIGHT,
    BOTTOM,
    TOP,
    FRONT,
    BACK
}

enum class SideMenu {
    NONE,
    ADD_MENU,
    POSE_PRESET_MENU,
    HAND_MENU,
    SWAP_MODEL_MENU,
    TRANSFORM_MENU,
    PERSPECTIVE_MENU,
    BRIGHTNESS_MENU,
    MATERIAL_MENU,
    HAIR_MENU
}

data class BodyProportions(
    val heightScale: Float,
    val shoulderWidth: Float,
    val hipWidth: Float,
    val headScale: Float,
    val limbThickness: Float,
    val chestDepth: Float
)

enum class CharacterType(
    val key: String,
    val assetPath: String,
    val proportions: BodyProportions
) {
    ANIME_KYO_FEMALE(
        key = "anime_kyo_female",
        assetPath = "imgs/character_models/anime_kyo_female.jpg",
        proportions = BodyProportions(0.95f, 0.86f, 0.92f, 1.14f, 0.84f, 0.88f)
    ),
    ANIME_KYO_MALE(
        key = "anime_kyo_male",
        assetPath = "imgs/character_models/anime_kyo_male.jpg",
        proportions = BodyProportions(1.02f, 1.04f, 0.92f, 1.10f, 0.94f, 0.96f)
    ),
    FEMALE_NEW(
        key = "female_new",
        assetPath = "imgs/character_models/female_new.jpg",
        proportions = BodyProportions(0.98f, 0.90f, 0.98f, 0.96f, 0.88f, 0.92f)
    ),
    MAN(
        key = "man",
        assetPath = "imgs/character_models/man.jpg",
        proportions = BodyProportions(1.08f, 1.16f, 0.96f, 0.96f, 1.06f, 1.04f)
    ),
    FEMALE_1_7(
        key = "female_1_7",
        assetPath = "imgs/character_models/1_7_female.jpg",
        proportions = BodyProportions(1.04f, 0.88f, 0.96f, 0.90f, 0.86f, 0.90f)
    ),
    ANIME_GIRL(
        key = "anime_girl",
        assetPath = "imgs/character_models/anime_girl.jpg",
        proportions = BodyProportions(0.88f, 0.82f, 0.86f, 1.24f, 0.80f, 0.84f)
    );

    fun label(s: StudioStrings): String = when (this) {
        ANIME_KYO_FEMALE -> s.animeGirl16
        ANIME_KYO_MALE -> s.animeBoy16
        FEMALE_NEW -> s.woman
        MAN -> s.man
        FEMALE_1_7 -> s.woman17
        ANIME_GIRL -> s.girl15
    }
}

enum class PropType(
    val key: String,
    val assetPath: String
) {
    SPHERE("sphere", "imgs/geometries/sphere_prop.png"),
    CUBE("cube_prop", "imgs/geometries/cube_prop.png"),
    CONE("cone_prop", "imgs/geometries/cone_prop.png"),
    CYLINDER("cylinder_prop", "imgs/geometries/cylinder_prop.png"),
    HEX("hex_prop", "imgs/geometries/hex_prop.png"),
    PYRAMID("pyramid_prop", "imgs/geometries/pyramid_prop.png"),
    RECTANGLE("rectangle_prop", "imgs/geometries/rectangle_prop.png"),
    STAIRS("stairs_prop", "imgs/geometries/stairs_prop.jpg"),
    TORUS("torus_prop", "imgs/geometries/torus_prop.jpg"),
    WALL("wall_prop", "imgs/geometries/wall_prop.jpg");

    fun label(s: StudioStrings): String = when (this) {
        SPHERE -> s.sphere
        CUBE -> s.cube
        CONE -> s.cone
        CYLINDER -> s.cylinder
        HEX -> s.hex
        PYRAMID -> s.pyramid
        RECTANGLE -> s.rectangle
        STAIRS -> s.stairs
        TORUS -> s.torus
        WALL -> s.wall
    }
}

enum class HairStyle(
    val key: String,
    val assetPath: String,
    val displayTag: String
) {
    NAKED("naked", "imgs/hair/Naked.PNG", "None"),
    ALICE("Rig_Alice_v2", "imgs/hair/alice.png", "Alice"),
    ARCHER("Rig_archer_v2", "imgs/hair/Archer.png", "Archer"),
    DONGTIAOXI("Rig_Dongtiaoxi_v2", "imgs/hair/dongtiaoxi.png", "Nozomi"),
    DUBIANYAO("Rig_Dubianyao_v2", "imgs/hair/Dubianyao.PNG", "You"),
    GZYM("gzym_v2", "imgs/hair/gzym.png", "Twintail"),
    IKAROSU("Rig_ikarosu_v2", "imgs/hair/ikarosu.png", "Ikaros"),
    MIKU("Rig_Miku_v2", "imgs/hair/miku.png", "Miku"),
    MIO("Rig_Mio_v2", "imgs/hair/Mio.PNG", "Mio"),
    NARUTO("naruto_v2", "imgs/hair/naruto.png", "Spiky"),
    NICO("Rig_Nico_v2", "imgs/hair/Nico.png", "Nico"),
    REM("Rig_Rem_v2", "imgs/hair/Rem.PNG", "Rem"),
    SJWL("sjwl_v2", "imgs/hair/sjwl.PNG", "Long"),
    TATARA("tatara_v2", "imgs/hair/tatara.PNG", "Bob"),
    UMI("Rig_Umi_v2", "imgs/hair/umi.png", "Umi"),
    YSFX("ysfx_v2", "imgs/hair/ysfx.PNG", "Side"),
    YSL("ysl_v2", "imgs/hair/ysl.png", "Short")
}
