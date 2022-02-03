package org.synergy.common.key

import android.view.KeyEvent.*

// Taken from KeyEvent Source:
// Android currently does not reserve code ranges for vendor-
// specific key codes.  If you have new key codes to have, you
// MUST contribute a patch to the open source project to define
// those new codes.  This is intended to maintain a consistent
// set of key code definitions across all Android devices.

// Since android KeyEvent does not have dedicated key codes for some keys, we define key codes
// for them using KeyEvent.getMaxKeyCode().
/**
 * Windows Left Key (in Windows OS)
 */
val KEYCODE_SUPER_LEFT = getMaxKeyCode() + 1

/**
 * Windows Right Key (in Windows OS)
 */
val KEYCODE_SUPER_RIGHT = KEYCODE_SUPER_LEFT + 1


// From https://github.com/debauchee/barrier/blob/master/src/lib/barrier/key_types.h
val NON_CHAR_KEY_ID_MAP = mapOf(
    // 0x0000 to KEYCODE_UNKNOWN,                      // kKeyNone
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

val MODIFIER_KEY_ID_MAP = mapOf(
    0xEFE1 to KEYCODE_SHIFT_LEFT,                      // kKeyShift_L, Left shift
    0xEFE2 to KEYCODE_SHIFT_RIGHT,                     // kKeyShift_R, Right shift
    0xEFE3 to KEYCODE_CTRL_LEFT,                       // kKeyControl_L, Left control
    0xEFE4 to KEYCODE_CTRL_RIGHT,                      // kKeyControl_R, Right control
    0xEFE5 to KEYCODE_CAPS_LOCK,                       // kKeyCapsLock, Caps lock
    0xEFE7 to KEYCODE_META_LEFT,                       // kKeyMeta_L, Left meta
    0xEFE8 to KEYCODE_META_RIGHT,                      // kKeyMeta_R, Right meta
    0xEFE9 to KEYCODE_ALT_LEFT,                        // kKeyAlt_L, Left alt
    0xEFEA to KEYCODE_ALT_RIGHT,                       // kKeyAlt_R, Right alt
    0xEFEB to KEYCODE_SUPER_LEFT,                      // kKeySuper_L, Left super
    0xEFEC to KEYCODE_SUPER_RIGHT,                     // kKeySuper_R, Right super
    // static const KeyID		kKeyShiftLock	= 0xEFE6;	/* Shift lock */
    // static const KeyID		kKeyHyper_L		= 0xEFED;	/* Left hyper */
    // static const KeyID		kKeyHyper_R		= 0xEFEE;	/* Right hyper */
)

val GLOBAL_ACTION_KEYCODES = listOf(
    KEYCODE_ESCAPE,
    KEYCODE_SUPER_LEFT,
    KEYCODE_SUPER_RIGHT,
)

// Modifier key masks
const val KEY_MODIFIER_SHIFT = 0x0001
const val KEY_MODIFIER_CONTROL = 0x0002
const val KEY_MODIFIER_ALT = 0x0004
const val KEY_MODIFIER_META = 0x0008
const val KEY_MODIFIER_SUPER = 0x0010
const val KEY_MODIFIER_ALT_GR = 0x0020
const val KEY_MODIFIER_LEVEL5LOCK = 0x0040
const val KEY_MODIFIER_CAPSLOCK = 0x1000
const val KEY_MODIFIER_NUMLOCK = 0x2000
const val KEY_MODIFIER_SCROLLLOCK = 0x4000

val MODIFIER_MASKS = listOf(
    KEY_MODIFIER_SHIFT,
    KEY_MODIFIER_CONTROL,
    KEY_MODIFIER_ALT,
    KEY_MODIFIER_META,
    KEY_MODIFIER_SUPER,
    KEY_MODIFIER_ALT_GR,
    KEY_MODIFIER_LEVEL5LOCK,
    KEY_MODIFIER_CAPSLOCK,
    KEY_MODIFIER_NUMLOCK,
    KEY_MODIFIER_SCROLLLOCK,
)

fun getActiveMasks(mask: Int) = MODIFIER_MASKS.filter { it and mask == it }