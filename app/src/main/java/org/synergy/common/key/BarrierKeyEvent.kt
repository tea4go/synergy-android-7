package org.synergy.common.key

// Barrier server sends ascii codes as id for keys from `space` to `~`
// in such a case we can ignore mask and directly call id.toChar() to get the character
private const val ASCII_CHAR_START = 32 // Space
private const val ASCII_CHAR_END = 126 // ~

data class BarrierKeyEvent(
    val id: Int,
    val mask: Int,
    val scanCode: Int,
) {
    val isCharacter by lazy { id in ASCII_CHAR_START..ASCII_CHAR_END }

    /**
     * Key is a non-char key if its id is mapped in [NON_CHAR_KEY_ID_MAP]
     */
    val isNonChar by lazy { NON_CHAR_KEY_ID_MAP.containsKey(id) }

    /**
     * Key is a non-char key if its id is mapped in [MODIFIER_KEY_ID_MAP]
     */
    val isModifier by lazy { MODIFIER_KEY_ID_MAP.containsKey(id) }

    /**
     * Key is a global action key if its id is mapped to a keycode in [GLOBAL_ACTION_KEYCODES]
     */
    val isGlobalAction by lazy { NON_CHAR_KEY_ID_MAP[id] in GLOBAL_ACTION_KEYCODES }

    /**
     * Key is unknown if it is neither a character key, nor a modifier nor a non-char key
     */
    val isUnknown by lazy { !isCharacter && !isNonChar && !isModifier }

    val keyCode by lazy {
        when {
            isNonChar -> NON_CHAR_KEY_ID_MAP[id]
            isModifier -> MODIFIER_KEY_ID_MAP[id]
            else -> null
        }
    }
}
