package com.nerufuyo.hyperisland.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ThemeSwitcherWidgetProviderTest {

    private val ids = listOf("a", "b", "c")

    @Test
    fun noThemes_returnsNull() {
        assertNull(ThemeSwitcherWidgetProvider.nextThemeId(emptyList(), "a"))
    }

    @Test
    fun noActiveTheme_startsAtFirst() {
        assertEquals("a", ThemeSwitcherWidgetProvider.nextThemeId(ids, null))
    }

    @Test
    fun advancesToNextInOrder() {
        assertEquals("b", ThemeSwitcherWidgetProvider.nextThemeId(ids, "a"))
        assertEquals("c", ThemeSwitcherWidgetProvider.nextThemeId(ids, "b"))
    }

    @Test
    fun wrapsAroundAtEnd() {
        assertEquals("a", ThemeSwitcherWidgetProvider.nextThemeId(ids, "c"))
    }

    @Test
    fun activeThemeNoLongerInstalled_startsAtFirst() {
        assertEquals("a", ThemeSwitcherWidgetProvider.nextThemeId(ids, "deleted_theme_id"))
    }
}
