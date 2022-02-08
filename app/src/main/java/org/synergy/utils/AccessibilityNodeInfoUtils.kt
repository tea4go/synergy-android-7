package org.synergy.utils

import android.os.Bundle
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.*
import org.synergy.utils.AccessibilityNodeInfoUtils.MoveDirection.PREVIOUS


object AccessibilityNodeInfoUtils {
    /**
     * Get the [AccessibilityNodeInfoCompat]'s text if it is not default text (e.g. "Search..."
     * in a search box).
     *
     *
     * Note: (AccessibilityNodeInfoCompat#getTextSelectionStart == -1) indicates that there is no
     * current selection or cursor position because the text is empty (excluding default text).
     *
     * @param node The node for which to get text
     * @return The node's text, or an empty string if the given node's text is default text
     */
    private fun getNonDefaultTextForNode(node: AccessibilityNodeInfoCompat): CharSequence =
        if (node.textSelectionStart == -1) "" else node.text

    /**
     * Set the text and the cursor position of the given [AccessibilityNodeInfoCompat].
     *
     * @param node The [AccessibilityNodeInfoCompat] for which to set text
     * @param text The [String] to set
     * @return `true` if setting the text and the cursor position is successful
     */
    fun insertText(
        node: AccessibilityNodeInfoCompat,
        text: String,
    ): Boolean {
        node.refresh()
        val previousSelectionStart = node.textSelectionStart.coerceAtLeast(0)
        val previousSelectionEnd = node.textSelectionEnd.coerceAtLeast(0)
        val previousTextContent = getNonDefaultTextForNode(node)
        val builder = StringBuilder(previousTextContent)
        builder.replace(previousSelectionStart, previousSelectionEnd, text)
        val newSelectionStart = previousSelectionStart + text.length
        val newSelectionEnd = previousSelectionStart + text.length

        val args = Bundle().apply {
            putCharSequence(ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, builder.toString())
        }

        if (!node.performAction(ACTION_SET_TEXT, args)) {
            return false
        }

        // Restore the cursor position. If arguments has no ACTION_ARGUMENT_SELECTION_START_INT, this
        // moves the cursor to the beginning of the text.
        node.refresh()
        return selectText(
            node,
            newSelectionStart,
            newSelectionEnd
        )
    }

    /**
     * Select the text in the given {@link AccessibilityNodeInfoCompat} between the given starting and
     * ending indices.
     *
     * @param node The {@link AccessibilityNodeInfoCompat} to select text in
     * @param startIndex The beginning index of the selection
     * @param endIndex The ending index of the selection
     * @return {@code true} if the selection is successful
     */
    private fun selectText(
        node: AccessibilityNodeInfoCompat,
        startIndex: Int,
        endIndex: Int
    ): Boolean {
        val args = Bundle().apply {
            putInt(ACTION_ARGUMENT_SELECTION_START_INT, startIndex)
            putInt(ACTION_ARGUMENT_SELECTION_END_INT, endIndex)
        }
        val selected: Boolean = node.performAction(ACTION_SET_SELECTION, args)
        // Return true if we're setting the cursor to the end of the text. This is a workaround for
        //  where setting selection returns false when setting the index at the end of the
        // text. The correct action is still performed, but the method call sometimes returns false.
        val text = getNonDefaultTextForNode(node)
        val textEnd = text.length
        return startIndex == textEnd && endIndex == textEnd || selected
    }

    /**
     * Deletes text in the given [AccessibilityNodeInfoCompat].
     *
     * @param node The [AccessibilityNodeInfoCompat] containing the text to delete
     * @return `true` if the deletion is successful
     */
    fun deleteText(
        node: AccessibilityNodeInfoCompat,
        isForwardDelete: Boolean = false,
    ): Boolean {
        node.refresh()
        val text = node.text ?: ""

        // Find the bounds of the section of text to delete.
        var deleteSectionStart = node.textSelectionStart
        var deleteSectionEnd = node.textSelectionEnd
        if (deleteSectionStart == deleteSectionEnd) {
            // if there is no selection
            // if isForwardDelete == false => delete the char before cursor (if cursor is not at start of text)
            // else => delete the char after the cursor (if cursor is not at the end of the text)
            if (!isForwardDelete && deleteSectionStart > 0) {
                deleteSectionStart -= 1
            } else if (isForwardDelete && deleteSectionEnd < text.length) {
                deleteSectionEnd += 1
            }
        }
        // make sure start <= end
        val deleteSectionLowerIndex = deleteSectionStart.coerceAtMost(deleteSectionEnd)
        val deleteSectionUpperIndex = deleteSectionStart.coerceAtLeast(deleteSectionEnd)

        // Set text to be the entire existing text minus the section to delete.
        val oldText = text.toString()
        val firstPart = oldText.substring(0, deleteSectionLowerIndex)
        val secondPart = oldText.substring(deleteSectionUpperIndex)
        val newText = "$firstPart$secondPart"
        val args = Bundle().apply {
            putCharSequence(
                ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                newText,
            )
        }
        if (!node.performAction(ACTION_SET_TEXT, args)) {
            return false
        }
        node.refresh()
        // Place the cursor back where it was before the deletion.
        val endOfText = text.length
        val newCursorPosition = deleteSectionLowerIndex.coerceAtMost(endOfText)
        return selectText(node, newCursorPosition, newCursorPosition)
    }

    enum class MoveDirection {
        PREVIOUS,
        NEXT,
    }

    fun moveCursor(
        node: AccessibilityNodeInfoCompat,
        granularity: Int,
        direction: MoveDirection,
        extendSelection: Boolean = false,
    ): Boolean {
        val args = Bundle().apply {
            putInt(ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT, granularity)
            putBoolean(ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN, extendSelection)
        }
        val action = if (direction == PREVIOUS) {
            ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY
        } else {
            ACTION_NEXT_AT_MOVEMENT_GRANULARITY
        }
        return node.performAction(action, args)
    }

    fun copyText(node: AccessibilityNodeInfoCompat): Boolean {
        val previousSelectionStart = node.textSelectionStart.coerceAtLeast(0)
        val previousSelectionEnd = node.textSelectionEnd.coerceAtLeast(0)
        val performed = node.performAction(ACTION_COPY)
        node.refresh()
        selectText(node, previousSelectionStart, previousSelectionEnd)
        return performed
    }

    fun cutText(node: AccessibilityNodeInfoCompat) = node.performAction(ACTION_CUT)

    fun pasteText(node: AccessibilityNodeInfoCompat) = node.performAction(ACTION_PASTE)
}