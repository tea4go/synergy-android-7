package org.synergy.common.key

import android.accessibilityservice.AccessibilityService.*
import android.view.KeyEvent.*
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.*

/**
 * 1-key global action mappings
 * eg: Esc
 */
val ONE_KEY_GLOBAL_ACTION_MAP = mapOf(
    KEYCODE_ESCAPE to GLOBAL_ACTION_BACK,
)

/**
 * 1-key text action mappings
 * eg: Home, End
 */
val ONE_KEY_TEXT_NODE_ACTION_MAP = mapOf(
    KEYCODE_DEL to ACTION_SET_TEXT,
    KEYCODE_FORWARD_DEL to ACTION_SET_TEXT,
    KEYCODE_MOVE_HOME to ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY,
    KEYCODE_MOVE_END to ACTION_NEXT_AT_MOVEMENT_GRANULARITY,
    KEYCODE_DPAD_LEFT to ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY,
    KEYCODE_DPAD_RIGHT to ACTION_NEXT_AT_MOVEMENT_GRANULARITY,
)

val MODIFIER_KEY_GLOBAL_ACTION_MAP = mapOf(
    KEYCODE_SUPER_LEFT to GLOBAL_ACTION_ACCESSIBILITY_ALL_APPS,
    KEYCODE_SUPER_RIGHT to GLOBAL_ACTION_ACCESSIBILITY_ALL_APPS,
)

/**
 * 2-key combinations to global Action mappings
 * eg: Win+D, Alt+Tab
 */
val TWO_KEY_COMBO_GLOBAL_ACTION_MAP = mapOf(
    KEYCODE_A to mapOf(
        META_SUPER_ON to GLOBAL_ACTION_NOTIFICATIONS, // Win+A => Notifications
    ),
    KEYCODE_D to mapOf(
        META_SUPER_ON to GLOBAL_ACTION_HOME, // Win+D => Home
    ),
    KEYCODE_TAB to mapOf(
        META_ALT_ON to GLOBAL_ACTION_RECENTS, // Alt+Tab => Recents
    ),
)

/**
 * 2-key combinations to text Action mappings
 * eg: Ctrl+C, Ctrl+V
 */
val TWO_KEY_COMBO_TEXT_NODE_ACTION_MAP = mapOf(
    KEYCODE_C to mapOf(
        META_CTRL_ON to ACTION_COPY, // Ctrl+C => Copy
    ),
    KEYCODE_X to mapOf(
        META_CTRL_ON to ACTION_CUT, // Ctrl+X => Cut
    ),
    KEYCODE_V to mapOf(
        META_CTRL_ON to ACTION_PASTE, // Ctrl+V => Paste
    ),
    KEYCODE_DPAD_LEFT to mapOf(
        META_SHIFT_ON to ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY,
    ),
    KEYCODE_DPAD_RIGHT to mapOf(
        META_SHIFT_ON to ACTION_NEXT_AT_MOVEMENT_GRANULARITY,
    ),
    KEYCODE_MOVE_HOME to mapOf(
        META_SHIFT_ON to ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY,
    ),
    KEYCODE_MOVE_END to mapOf(
        META_SHIFT_ON to ACTION_NEXT_AT_MOVEMENT_GRANULARITY,
    ),
)