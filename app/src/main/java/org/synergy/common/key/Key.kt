package org.synergy.common.key

import android.view.KeyEvent.*

// Barrier server sends ascii codes as id for keys from `space` to `~`
// in such a case we can ignore mask and directly call id.toChar() to get the character
private const val ASCII_CHAR_START = 32 // Space
private const val ASCII_CHAR_END = 126 // ~

data class Key(
    val id: Int,
    val mask: Int,
    val scanCode: Int,
) {
    val isCharacter by lazy { id in ASCII_CHAR_START..ASCII_CHAR_END }

    /**
     * Key is unknown if it is not a character key and it's id is not mapped to any key code,
     * or if it's id is mapped to [KEYCODE_UNKNOWN]
     */
    val isUnknown by lazy {
        val keyCode = NON_CHAR_KEY_ID_MAP[id]
        !isCharacter && (keyCode == null || keyCode == KEYCODE_UNKNOWN)
    }

    /**
     * Key is a non-char key if it is not a character key and it's id is mapped to a valid key code
     */
    val isNonChar by lazy {
        val keyCode = NON_CHAR_KEY_ID_MAP[id]
        !isCharacter && keyCode != null && keyCode != KEYCODE_UNKNOWN
    }

    val isGlobalAction by lazy { NON_CHAR_KEY_ID_MAP[id] in GLOBAL_ACTION_KEYCODES }

    val keyCode by lazy { NON_CHAR_KEY_ID_MAP[id] }
}

// From https://github.com/debauchee/barrier/blob/master/src/lib/barrier/key_types.h
private val NON_CHAR_KEY_ID_MAP = mapOf(
    0x0000 to KEYCODE_UNKNOWN,                         // kKeyNone
    0xEF08 to KEYCODE_DEL,                             // kKeyBackSpace, back space, back char
    0xEF09 to KEYCODE_TAB,                             // kKeyTab
    0xEF0A to KEYCODE_ENTER,                           // kKeyLinefeed, Linefeed, LF
    0xEF0D to KEYCODE_ENTER,                           // kKeyReturn, Return, enter
    0xEF1B to KEYCODE_ESCAPE,                          // kKeyEscape
    0xEFFF to KEYCODE_FORWARD_DEL,                     // kKeyDelete, Delete, rubout
    // static const KeyID        kKeyClear        = 0xEF0B;
    // static const KeyID        kKeyPause        = 0xEF13;    /* Pause, hold */
    // static const KeyID        kKeyScrollLock    = 0xEF14;
    // static const KeyID        kKeySysReq        = 0xEF15;
    // static const KeyID        kKeyMuhenkan    = 0xEF22;    /* Cancel Conversion */
    // static const KeyID        kKeyHenkan        = 0xEF23;    /* Start/Stop Conversion */
    // static const KeyID        kKeyKana        = 0xEF26;    /* Kana */
    // static const KeyID        kKeyHiraganaKatakana = 0xEF27;    /* Hiragana/Katakana toggle */
    // static const KeyID        kKeyZenkaku        = 0xEF2A;    /* Zenkaku/Hankaku */
    // static const KeyID        kKeyKanzi        = 0xEF2A;    /* Kanzi */
    // static const KeyID        kKeyEisuToggle    = 0xEF30;    /* Alphanumeric toggle */
    // static const KeyID        kKeyHangul        = 0xEF31;    /* Hangul */
    // static const KeyID        kKeyHanja        = 0xEF34;    /* Hanja */
)

val GLOBAL_ACTION_KEYCODES = listOf(
    KEYCODE_ESCAPE,
)