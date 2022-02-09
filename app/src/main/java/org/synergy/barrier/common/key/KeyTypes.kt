package org.synergy.barrier.common.key

import android.view.KeyEvent.*

// Key ids are taken from https://github.com/debauchee/barrier/blob/master/src/lib/barrier/key_types.h

// Taken from KeyEvent Source:
// Android currently does not reserve code ranges for vendor-
// specific key codes.  If you have new key codes to have, you
// MUST contribute a patch to the open source project to define
// those new codes.  This is intended to maintain a consistent
// set of key code definitions across all Android devices.

// Since android KeyEvent does not have dedicated key codes for some keys, we define key codes
// for them using KeyEvent.getMaxKeyCode().
// If you add any key code, update the BarrierKeyEvent.Companion.maxKeyCode
/**
 * Windows Left Key (in Windows OS)
 */
val KEYCODE_SUPER_LEFT = getMaxKeyCode() + 1

/**
 * Windows Right Key (in Windows OS)
 */
val KEYCODE_SUPER_RIGHT = KEYCODE_SUPER_LEFT + 1

// Barrier server sends ascii codes as id for keys from `space` to `~`

private const val ASCII_0 = 48
private const val ASCII_9 = ASCII_0 + 9

private const val ASCII_A = 65
private const val ASCII_Z = ASCII_A + 25

private const val ASCII_a = 97
private const val ASCII_z = ASCII_a + 25

private val ASCII_CODE_TO_KEYCODE_MAP = (ASCII_0..ASCII_9).associateWith {
    getKeyCodeFromAscii(it)
} + (ASCII_A..ASCII_Z).associateWith {
    getKeyCodeFromAscii(it)
} + (ASCII_a..ASCII_z).associateWith {
    getKeyCodeFromAscii(it)
} + mapOf(
    // map of ASCII codes whose KeyCodes are out of order or require a modifier
    32 to KEYCODE_SPACE,
    33 to KEYCODE_1,                        // !
    34 to KEYCODE_APOSTROPHE,               // "
    35 to KEYCODE_POUND,                    // #
    36 to KEYCODE_4,                        // $
    37 to KEYCODE_5,                        // %
    38 to KEYCODE_7,                        // &
    39 to KEYCODE_APOSTROPHE,               // '
    40 to KEYCODE_9,                        // (
    41 to KEYCODE_0,                        // )
    42 to KEYCODE_STAR,                     // *
    43 to KEYCODE_PLUS,                     // +
    44 to KEYCODE_COMMA,                    // ,
    45 to KEYCODE_MINUS,                    // -
    46 to KEYCODE_PERIOD,                   // .
    47 to KEYCODE_SLASH,                    // /
    58 to KEYCODE_SEMICOLON,                // :
    59 to KEYCODE_SEMICOLON,                // ;
    60 to KEYCODE_COMMA,                    // <
    61 to KEYCODE_EQUALS,                   // =
    62 to KEYCODE_PERIOD,                   // >
    63 to KEYCODE_SLASH,                    // ?
    64 to KEYCODE_AT,                       // @
    91 to KEYCODE_LEFT_BRACKET,             // [
    92 to KEYCODE_BACKSLASH,                // \
    93 to KEYCODE_RIGHT_BRACKET,            // ]
    94 to KEYCODE_6,                        // ^
    95 to KEYCODE_MINUS,                    // _
    96 to KEYCODE_GRAVE,                    // `
    123 to KEYCODE_LEFT_BRACKET,            // {
    124 to KEYCODE_BACKSLASH,               // |
    125 to KEYCODE_RIGHT_BRACKET,           // }
    126 to KEYCODE_GRAVE,                   // ~
)

private fun getKeyCodeFromAscii(ascii: Int): Int {
    var asciiStart = -1
    var keyCodeStart = -1
    when (ascii) {
        in ASCII_0..ASCII_9 -> {
            asciiStart = ASCII_0
            keyCodeStart = KEYCODE_0
        }
        in ASCII_A..ASCII_Z -> {
            asciiStart = ASCII_A
            keyCodeStart = KEYCODE_A
        }
        in ASCII_a..ASCII_z -> {
            asciiStart = ASCII_a
            keyCodeStart = KEYCODE_A
        }
    }
    val offset = ascii - asciiStart
    return keyCodeStart + offset
}

val KEY_ID_TO_KEYCODE_MAP = ASCII_CODE_TO_KEYCODE_MAP + mapOf(
    // keypad
    0xEF80 to KEYCODE_SPACE,                           // kKeyKP_Space, space
    0xEF89 to KEYCODE_TAB,                             // kKeyKP_Tab
    0xEF8D to KEYCODE_NUMPAD_ENTER,                    // kKeyKP_Enter, enter
    0xEF91 to KEYCODE_F1,                              // kKeyKP_F1, PF1, KP_A
    0xEF92 to KEYCODE_F2,                              // kKeyKP_F2
    0xEF93 to KEYCODE_F3,                              // kKeyKP_F3
    0xEF94 to KEYCODE_F4,                              // kKeyKP_F4
    0xEF95 to KEYCODE_MOVE_HOME,                       // kKeyKP_Home
    0xEF96 to KEYCODE_DPAD_LEFT,                       // kKeyKP_Left
    0xEF97 to KEYCODE_DPAD_UP,                         // kKeyKP_Up
    0xEF98 to KEYCODE_DPAD_RIGHT,                      // kKeyKP_Right
    0xEF99 to KEYCODE_DPAD_DOWN,                       // kKeyKP_Down
    0xEF9A to KEYCODE_PAGE_UP,                         // kKeyKP_PageUp
    0xEF9B to KEYCODE_PAGE_DOWN,                       // kKeyKP_PageDown
    0xEF9C to KEYCODE_MOVE_END,                        // kKeyKP_End
    // static const KeyID		kKeyKP_Begin	= 0xEF9D;
    0xEF9E to KEYCODE_INSERT,                          // kKeyKP_Insert
    0xEF9F to KEYCODE_FORWARD_DEL,                     // kKeyKP_Delete
    0xEFBD to KEYCODE_NUMPAD_EQUALS,                   // kKeyKP_Equal, equals
    0xEFAA to KEYCODE_NUMPAD_MULTIPLY,                 // kKeyKP_Multiply
    0xEFAB to KEYCODE_NUMPAD_ADD,                      // kKeyKP_Add
    0xEFAC to KEYCODE_NUMPAD_COMMA,                    // kKeyKP_Separator, separator, often comma
    0xEFAD to KEYCODE_NUMPAD_SUBTRACT,                 // kKeyKP_Subtract
    0xEFAE to KEYCODE_NUMPAD_DOT,                      // kKeyKP_Decimal
    0xEFAF to KEYCODE_NUMPAD_DIVIDE,                   // kKeyKP_Divide
    0xEFB0 to KEYCODE_0,                               // kKeyKP_0
    0xEFB1 to KEYCODE_1,                               // kKeyKP_1
    0xEFB2 to KEYCODE_2,                               // kKeyKP_2
    0xEFB3 to KEYCODE_3,                               // kKeyKP_3
    0xEFB4 to KEYCODE_4,                               // kKeyKP_4
    0xEFB5 to KEYCODE_5,                               // kKeyKP_5
    0xEFB6 to KEYCODE_6,                               // kKeyKP_6
    0xEFB7 to KEYCODE_7,                               // kKeyKP_7
    0xEFB8 to KEYCODE_8,                               // kKeyKP_8
    0xEFB9 to KEYCODE_9,                               // kKeyKP_9

    // non-char keys
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

    // cursor control
    0xEF50 to KEYCODE_MOVE_HOME,
    0xEF51 to KEYCODE_DPAD_LEFT,                         // kKeyLeft, Move left, left arrow
    0xEF52 to KEYCODE_DPAD_UP,                           // kKeyUp, Move up, up arrow
    0xEF53 to KEYCODE_DPAD_RIGHT,                        // kKeyRight, Move right, right arrow
    0xEF54 to KEYCODE_DPAD_DOWN,                         // kKeyDown, Move down, down arrow
    0xEF55 to KEYCODE_PAGE_UP,                           // kKeyPageUp
    0xEF56 to KEYCODE_PAGE_DOWN,                         // kKeyPageDown
    0xEF57 to KEYCODE_MOVE_END,                          // kKeyEnd, EOL
    // static const KeyID		kKeyBegin		= 0xEF58;	/* BOL */

    // modifiers
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

// Modifier key masks
const val KEY_MODIFIER_SHIFT = 0x0001
const val KEY_MODIFIER_CONTROL = 0x0002
const val KEY_MODIFIER_ALT = 0x0004
const val KEY_MODIFIER_META = 0x0008
const val KEY_MODIFIER_SUPER = 0x0010
const val KEY_MODIFIER_ALT_GR = 0x0020

// const val KEY_MODIFIER_LEVEL5LOCK = 0x0040
const val KEY_MODIFIER_CAPSLOCK = 0x1000
const val KEY_MODIFIER_NUMLOCK = 0x2000
const val KEY_MODIFIER_SCROLLLOCK = 0x4000

const val META_SUPER_ON = 0x00800000

val MODIFIER_MASKS = mapOf(
    KEY_MODIFIER_SHIFT to META_SHIFT_ON,
    KEY_MODIFIER_CONTROL to META_CTRL_ON,
    KEY_MODIFIER_ALT to META_ALT_ON,
    KEY_MODIFIER_META to META_META_ON,
    KEY_MODIFIER_SUPER to META_SUPER_ON,
    KEY_MODIFIER_ALT_GR to META_ALT_ON,
    // KEY_MODIFIER_LEVEL5LOCK,
    KEY_MODIFIER_CAPSLOCK to META_CAPS_LOCK_ON,
    KEY_MODIFIER_NUMLOCK to META_NUM_LOCK_ON,
    KEY_MODIFIER_SCROLLLOCK to META_SCROLL_LOCK_ON,
)

fun getActiveMasks(mask: Int) = MODIFIER_MASKS.keys.filter { it and mask == it }