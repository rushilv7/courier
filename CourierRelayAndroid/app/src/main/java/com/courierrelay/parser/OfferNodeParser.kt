package com.courierrelay.parser

import android.view.accessibility.AccessibilityNodeInfo

data class ParsedOffer(
    val payout: String?,
    val duration: String?,
    val distance: String?,
    val merchantOrPickup: String?,
    val dropoff: String?,
    val confidence: Float,
    val rawLines: List<String>
) {
    fun toRelayMessage(): String {
        val lines = buildList {
            add("Offer detected")
            payout?.let { add("Payout: $it") }
            duration?.let { add("Duration: $it") }
            distance?.let { add("Distance: $it") }
            merchantOrPickup?.let { add("Pickup: $it") }
            dropoff?.let { add("Dropoff: $it") }
            add("Confidence: ${(confidence * 100).toInt()}%")
        }
        return lines.joinToString("\n")
    }
}

class OfferNodeParser {
    fun parse(root: AccessibilityNodeInfo?): ParsedOffer? {
        if (root == null) return null
        val lines = mutableListOf<String>()
        collectText(root, lines)

        if (lines.isEmpty()) return null

        val joined = lines.joinToString(" ")
        val payout = Regex("\\$\\s?\\d+(?:\\.\\d{1,2})?").find(joined)?.value?.replace(" ", "")
        val duration = Regex("\\b\\d+\\s*min\\b", RegexOption.IGNORE_CASE).find(joined)?.value
        val distance = Regex("\\b\\d+(?:\\.\\d+)?\\s*(?:km|mi)\\b", RegexOption.IGNORE_CASE).find(joined)?.value

        val pickup = lines.firstOrNull { it.length > 10 && it.contains("(") && it.contains(")") }
            ?: lines.firstOrNull { it.length > 10 && !it.contains("Accept", ignoreCase = true) }
        val dropoff = lines.firstOrNull { it.contains("Dr") || it.contains("St") || it.contains("Ave") || it.contains("Ct") }

        var score = 0f
        if (payout != null) score += 0.25f
        if (duration != null) score += 0.2f
        if (distance != null) score += 0.2f
        if (pickup != null) score += 0.2f
        if (dropoff != null) score += 0.15f

        return ParsedOffer(
            payout = payout,
            duration = duration,
            distance = distance,
            merchantOrPickup = pickup,
            dropoff = dropoff,
            confidence = score.coerceIn(0f, 1f),
            rawLines = lines.distinct().take(20)
        )
    }

    fun findAcceptNode(root: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (root == null) return null
        if (root.text?.toString()?.equals("Accept", ignoreCase = true) == true) return root
        if (root.contentDescription?.toString()?.equals("Accept", ignoreCase = true) == true) return root

        for (i in 0 until root.childCount) {
            val child = root.getChild(i) ?: continue
            val found = findAcceptNode(child)
            if (found != null) return found
        }
        return null
    }

    private fun collectText(node: AccessibilityNodeInfo, out: MutableList<String>) {
        node.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }?.let(out::add)
        node.contentDescription?.toString()?.trim()?.takeIf { it.isNotEmpty() }?.let(out::add)
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { collectText(it, out) }
        }
    }
}
