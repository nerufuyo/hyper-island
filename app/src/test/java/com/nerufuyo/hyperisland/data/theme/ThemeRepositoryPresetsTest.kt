package com.nerufuyo.hyperisland.data.theme

import com.nerufuyo.hyperisland.models.theme.HyperTheme
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-JVM check for the bundled theme presets (ThemeRepository.BUILT_IN_PRESETS).
 * Does not touch Context/filesystem - just validates the data itself, since that's
 * the part actually at risk of a typo (duplicate id, bad enum, serialization mismatch).
 */
class ThemeRepositoryPresetsTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun builtInPresets_areNotEmpty() {
        assertTrue(ThemeRepository.BUILT_IN_PRESETS.isNotEmpty())
    }

    @Test
    fun builtInPresets_haveUniqueIds() {
        val ids = ThemeRepository.BUILT_IN_PRESETS.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun builtInPresets_roundTripThroughJson() {
        ThemeRepository.BUILT_IN_PRESETS.forEach { theme ->
            val encoded = json.encodeToString(theme)
            val decoded = json.decodeFromString<HyperTheme>(encoded)
            assertEquals(theme, decoded)
        }
    }
}
