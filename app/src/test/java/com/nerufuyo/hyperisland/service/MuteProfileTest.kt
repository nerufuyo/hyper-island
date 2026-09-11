package com.nerufuyo.hyperisland.service

import com.nerufuyo.hyperisland.data.db.MuteProfile
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MuteProfileTest {

    private fun manual(enabled: Boolean = true) = MuteProfile(
        id = "1", name = "Manual", enabled = enabled, triggerType = MuteProfile.TRIGGER_MANUAL
    )

    private fun schedule(start: Int, end: Int, enabled: Boolean = true) = MuteProfile(
        id = "2", name = "Schedule", enabled = enabled, triggerType = MuteProfile.TRIGGER_SCHEDULE,
        scheduleStartMinutes = start, scheduleEndMinutes = end
    )

    private fun appBased(apps: String, enabled: Boolean = true) = MuteProfile(
        id = "3", name = "Apps", enabled = enabled, triggerType = MuteProfile.TRIGGER_APP_FOREGROUND,
        triggerApps = apps
    )

    @Test
    fun noProfiles_neverActive() {
        assertFalse(NotificationReaderService.isAnyProfileActive(emptyList(), 12 * 60, null))
    }

    @Test
    fun disabledManualProfile_neverActive() {
        assertFalse(NotificationReaderService.isAnyProfileActive(listOf(manual(enabled = false)), 12 * 60, null))
    }

    @Test
    fun enabledManualProfile_alwaysActive() {
        assertTrue(NotificationReaderService.isAnyProfileActive(listOf(manual()), 3 * 60, null))
    }

    @Test
    fun scheduleProfile_respectsWindow() {
        val profiles = listOf(schedule(22 * 60, 7 * 60))
        assertTrue(NotificationReaderService.isAnyProfileActive(profiles, 23 * 60, null)) // 23:00, overnight window
        assertFalse(NotificationReaderService.isAnyProfileActive(profiles, 12 * 60, null)) // noon, outside
    }

    @Test
    fun appForegroundProfile_matchesOnlyListedApps() {
        val profiles = listOf(appBased("com.game.a,com.game.b"))
        assertTrue(NotificationReaderService.isAnyProfileActive(profiles, 12 * 60, "com.game.a"))
        assertFalse(NotificationReaderService.isAnyProfileActive(profiles, 12 * 60, "com.other.app"))
        assertFalse(NotificationReaderService.isAnyProfileActive(profiles, 12 * 60, null))
    }

    @Test
    fun anyEnabledProfileMatching_isEnoughAmongMultiple() {
        val profiles = listOf(
            schedule(22 * 60, 7 * 60), // not matching at noon
            appBased("com.game.a") // matching
        )
        assertTrue(NotificationReaderService.isAnyProfileActive(profiles, 12 * 60, "com.game.a"))
    }
}
