package org.synergy.common.key

import android.text.method.MetaKeyKeyListener
import android.view.KeyCharacterMap
import android.view.KeyEvent

class BarrierKeyEvent : KeyEvent {
    constructor(
        id: Int,
        downTime: Long,
        eventTime: Long,
        action: Int,
        mask: Int,
        scanCode: Int
    ) : super(
        downTime,
        eventTime,
        action,
        getKeyCodeFromId(id),
        0,
        getMetaStateFromMask(mask),
        KeyCharacterMap.VIRTUAL_KEYBOARD,
        scanCode,
    )

    constructor(keyEvent: KeyEvent) : super(keyEvent)

    val hasValidKeyCode by lazy { keyCodeIsValid(keyCode) }

    val isCharacter by lazy { unicodeChar != 0 }

    val isModifier by lazy { isModifierKey(keyCode) }

    private val hasNoModifiers: Boolean by lazy {
        normalizeMetaState(metaState) and META_MODIFIER_MASK == 0
    }

    val hasModifiers: Boolean by lazy { !hasNoModifiers }

    /**
     * Returns the pressed state of the SUPER meta key.
     *
     * @return true if the SUPER key is pressed, false otherwise
     */
    fun isSuperPressed(): Boolean {
        return metaState and META_SUPER_ON != 0
    }

    override fun toString() = StringBuilder().run {
        append("KeyEvent { action=").append(actionToString(action))
        append(", keyCode=").append(keyCodeToString(keyCode))
        append(", scanCode=").append(scanCode)
        append(", metaState=").append(metaStateToString(metaState))
        append(", flags=0x").append(Integer.toHexString(flags))
        append(", repeatCount=").append(repeatCount)
        append(", eventTime=").append(eventTime)
        append(", downTime=").append(downTime)
        append(", deviceId=").append(deviceId)
        append(", source=0x").append(Integer.toHexString(source))
        append(" }")
        toString()
    }

    companion object {
        private fun getKeyCodeFromId(id: Int) = KEY_ID_TO_KEYCODE_MAP[id] ?: KEYCODE_UNKNOWN

        private fun getMetaStateFromMask(mask: Int) = getActiveMasks(mask).fold(0) { acc, m ->
            acc or (MODIFIER_MASKS[m] ?: 0)
        }

        private const val META_MODIFIER_MASK =
            (META_SHIFT_ON or META_SHIFT_LEFT_ON or META_SHIFT_RIGHT_ON
                    or META_ALT_ON or META_ALT_LEFT_ON or META_ALT_RIGHT_ON
                    or META_CTRL_ON or META_CTRL_LEFT_ON or META_CTRL_RIGHT_ON
                    or META_META_ON or META_META_LEFT_ON or META_META_RIGHT_ON
                    or META_SYM_ON or META_FUNCTION_ON or META_SUPER_ON)

        private const val META_LOCK_MASK =
            META_CAPS_LOCK_ON or META_NUM_LOCK_ON or META_SCROLL_LOCK_ON

        private const val META_ALL_MASK = META_MODIFIER_MASK or META_LOCK_MASK

        private val META_SYMBOLIC_NAMES = arrayOf(
            "META_SHIFT_ON",
            "META_ALT_ON",
            "META_SYM_ON",
            "META_FUNCTION_ON",
            "META_ALT_LEFT_ON",
            "META_ALT_RIGHT_ON",
            "META_SHIFT_LEFT_ON",
            "META_SHIFT_RIGHT_ON",
            "META_CAP_LOCKED",
            "META_ALT_LOCKED",
            "META_SYM_LOCKED",
            "0x00000800",
            "META_CTRL_ON",
            "META_CTRL_LEFT_ON",
            "META_CTRL_RIGHT_ON",
            "0x00008000",
            "META_META_ON",
            "META_META_LEFT_ON",
            "META_META_RIGHT_ON",
            "0x00080000",
            "META_CAPS_LOCK_ON",
            "META_NUM_LOCK_ON",
            "META_SCROLL_LOCK_ON",
            "META_SUPER_ON",
            "0x01000000",
            "0x02000000",
            "0x04000000",
            "0x08000000",
            "0x10000000",
            "0x20000000",
            "0x40000000",
            "0x80000000"
        )

        private val maxKeyCode = KEYCODE_SUPER_RIGHT

        fun keyCodeIsValid(keyCode: Int) = keyCode in KEYCODE_UNKNOWN..maxKeyCode

        /**
         * Normalizes the specified meta state.
         *
         *
         * The meta state is normalized such that if either the left or right modifier meta state
         * bits are set then the result will also include the universal bit for that modifier.
         *
         *
         * If the specified meta state contains [.META_ALT_LEFT_ON] then
         * the result will also contain [.META_ALT_ON] in addition to [.META_ALT_LEFT_ON]
         * and the other bits that were specified in the input.  The same is process is
         * performed for shift, control and meta.
         *
         *
         * If the specified meta state contains synthetic meta states defined by
         * [MetaKeyKeyListener], then those states are translated here and the original
         * synthetic meta states are removed from the result.
         * [MetaKeyKeyListener.META_CAP_LOCKED] is translated to [.META_CAPS_LOCK_ON].
         * [MetaKeyKeyListener.META_ALT_LOCKED] is translated to [.META_ALT_ON].
         * [MetaKeyKeyListener.META_SYM_LOCKED] is translated to [.META_SYM_ON].
         *
         *
         * Undefined meta state bits are removed.
         *
         *
         * @param metaState The meta state.
         * @return The normalized meta state.
         */
        fun normalizeMetaState(metaState: Int): Int {
            var state = metaState
            if (state and (META_SHIFT_LEFT_ON or META_SHIFT_RIGHT_ON) != 0) {
                state = state or META_SHIFT_ON
            }
            if (state and (META_ALT_LEFT_ON or META_ALT_RIGHT_ON) != 0) {
                state = state or META_ALT_ON
            }
            if (state and (META_CTRL_LEFT_ON or META_CTRL_RIGHT_ON) != 0) {
                state = state or META_CTRL_ON
            }
            if (state and (META_META_LEFT_ON or META_META_RIGHT_ON) != 0) {
                state = state or META_META_ON
            }
            if (state and MetaKeyKeyListener.META_CAP_LOCKED != 0) {
                state = state or META_CAPS_LOCK_ON
            }
            if (state and MetaKeyKeyListener.META_ALT_LOCKED != 0) {
                state = state or META_ALT_ON
            }
            if (state and MetaKeyKeyListener.META_SYM_LOCKED != 0) {
                state = state or META_SYM_ON
            }
            if (state and META_SUPER_ON != 0) {
                state = state or META_SUPER_ON
            }
            return state and META_ALL_MASK
        }

        /**
         * Returns a string that represents the symbolic name of the specified action
         * such as "ACTION_DOWN", or an equivalent numeric constant such as "35" if unknown.
         *
         * @param action The action.
         * @return The symbolic name of the specified action.
         */
        fun actionToString(action: Int) = when (action) {
            ACTION_DOWN -> "ACTION_DOWN"
            ACTION_UP -> "ACTION_UP"
            ACTION_MULTIPLE -> "ACTION_MULTIPLE"
            else -> action.toString()
        }

        /**
         * Returns a string that represents the symbolic name of the specified combined meta
         * key modifier state flags such as "0", "META_SHIFT_ON",
         * "META_ALT_ON|META_SHIFT_ON" or an equivalent numeric constant such as "0x10000000"
         * if unknown.
         *
         * @param metaState The meta state.
         * @return The symbolic name of the specified combined meta state flags.
         */
        fun metaStateToString(metaState: Int): String {
            var state = metaState
            if (state == 0) {
                return "0"
            }
            var result: StringBuilder? = null
            var i = 0
            while (state != 0) {
                val isSet = state and 1 != 0
                state = state ushr 1 // unsigned shift!
                if (isSet) {
                    val name: String = META_SYMBOLIC_NAMES[i]
                    if (result == null) {
                        if (state == 0) {
                            return name
                        }
                        result = java.lang.StringBuilder(name)
                    } else {
                        result.append('|')
                        result.append(name)
                    }
                }
                i += 1
            }
            return result.toString()
        }

        /**
         * Returns true if this key code is a modifier key.
         *
         *
         * For the purposes of this function, [.KEYCODE_CAPS_LOCK],
         * [.KEYCODE_SCROLL_LOCK], and [.KEYCODE_NUM_LOCK] are
         * not considered modifier keys.  Consequently, this function return false
         * for those keys.
         *
         *
         * @return True if the key code is one of
         * [.KEYCODE_SHIFT_LEFT] [.KEYCODE_SHIFT_RIGHT],
         * [.KEYCODE_ALT_LEFT], [.KEYCODE_ALT_RIGHT],
         * [.KEYCODE_CTRL_LEFT], [.KEYCODE_CTRL_RIGHT],
         * [.KEYCODE_META_LEFT], or [.KEYCODE_META_RIGHT],
         * [.KEYCODE_SYM], [.KEYCODE_NUM], [.KEYCODE_FUNCTION].
         */
        fun isModifierKey(keyCode: Int): Boolean {
            return when (keyCode) {
                KEYCODE_SHIFT_LEFT,
                KEYCODE_SHIFT_RIGHT,
                KEYCODE_ALT_LEFT,
                KEYCODE_ALT_RIGHT,
                KEYCODE_CTRL_LEFT,
                KEYCODE_CTRL_RIGHT,
                KEYCODE_META_LEFT,
                KEYCODE_META_RIGHT,
                KEYCODE_SYM,
                KEYCODE_NUM,
                KEYCODE_FUNCTION,
                KEYCODE_SUPER_LEFT,
                KEYCODE_SUPER_RIGHT -> true
                else -> false
            }
        }
    }
}
