package ru.tg.pawaptz.achievments

import com.vdurmont.emoji.Emoji
import com.vdurmont.emoji.EmojiManager

enum class FunnySmiles(val alias: String) {
    MAN_WITH_RED_HAIR("man_with_red_hair"),
    MAN_REDHEAD("man_redhead"),
    MAN_GINGER("man_ginger"),
    WOMAN_WITH_RED_HAIR("woman_with_red_hair"),
    WOMAN_REDHEAD("woman_redhead"),
    WOMAN_GINGER("woman_ginger"),
    MAN_WITH_CURLY_HAIR("man_with_curly_hair"),
    WOMAN_WITH_CURLY_HAIR("woman_with_curly_hair"),
    MAN_WITH_WHITE_HAIR("man_with_white_hair"),
    MAN_WITH_GRAY_HAIR("man_with_gray_hair"),
    MAN_WITH_GREY_HAIR("man_with_grey_hair"),
    WOMAN_WITH_WHITE_HAIR("woman_with_white_hair"),
    WOMAN_WITH_GRAY_HAIR("woman_with_gray_hair"),
    WOMAN_WITH_GREY_HAIR("woman_with_grey_hair"),
    MAN_WITH_NO_HAIR("man_with_no_hair"),
    BALD_MAN("bald_man"),
    WOMAN_WITH_NO_HAIR("woman_with_no_hair"),
    BALD_WOMAN("bald_woman"),
    SMILE("smile"),
    SMILEY("smiley"),
    GRINNING("grinning"),
    BLUSH("blush"),
    WINK("wink"),
    HEART_EYES("heart_eyes"),
    KISSING_HEART("kissing_heart"),
    KISSING_CLOSED_EYES("kissing_closed_eyes"),
    KISSING("kissing"),
    KISSING_SMILING_EYES("kissing_smiling_eyes"),
    STUCK_OUT_TONGUE_WINKING_EYE("stuck_out_tongue_winking_eye"),
    STUCK_OUT_TONGUE_CLOSED_EYES("stuck_out_tongue_closed_eyes"),
    STUCK_OUT_TONGUE("stuck_out_tongue"),
    JOY("joy"),
    SOB("sob"),
    LAUGHING("laughing"),
    SATISFIED("satisfied"),
    YUM("yum"),
    SUNGLASSES("sunglasses"),
    HUSHED("hushed"),
    MAN_WITH_GUA_PI_MAO("man_with_gua_pi_mao"),
    SMIRK("smirk"),
    PRINCESS("princess"),
    SMILEY_CAT("smiley_cat"),
    SMILE_CAT("smile_cat"),
    JOY_CAT("joy_cat"),
    THUMBS_UP("thumbsup"),
    PLUS_ONE("+1");

    fun emoji(): Emoji {
        return EmojiManager.getForAlias(alias)
    }
}

enum class SadSmiles(val alias: String) {
    MINUS_ONE("-1"),
    NO_GOOD("no_good"),
    THUMBS_DOWN("thumbsdown"),
    POUTING_CAT("pouting_cat"),
    SCREAM_CAT("scream_cat"),
    CRYING_CAT_FACE("crying_cat_face"),
    CONFUSED("confused"),
    WORRIED("worried"),
    FROWNING("frowning"),
    ANGUISHED("anguished"),
    SMILING_IMP("smiling_imp"),
    IMP("imp"),
    OPEN_MOUTH("open_mouth"),
    FEMALE_FACEPALM("female_facepalm"),
    WOMAN_FACEPALM("woman_facepalm"),
    MALE_SHRUG("male_shrug"),
    MAN_SHRUG("man_shrug"),
    FEMALE_SHRUG("female_shrug"),
    WOMAN_SHRUG("woman_shrug"),
    PENSIVE("pensive"),
    UNAMUSED("unamused"),
    DISAPPOINTED("disappointed"),
    PERSEVERE("persevere"),
    CRY("cry"),
    DISAPPOINTED_RELIEVED("disappointed_relieved"),
    SWEAT("sweat"),
    WEARY("weary"),
    TIRED_FACE("tired_face"),
    EARFUL("fearful"),
    SCREAM("scream"),
    ANGRY("angry"),
    RAGE("rage"),
    TRIUMPH("triumph"),
    CONFOUNDED("confounded");

    fun emoji(): Emoji {
        return EmojiManager.getForAlias(alias)
    }
}
