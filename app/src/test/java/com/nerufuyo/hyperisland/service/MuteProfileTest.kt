package com.nerufuyo.hyperisland.service

import com.nerufuyo.hyperisland.data.db.MuteProfile
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class MuteProfileTest {

    private fun manual(enabled: Boolean = true, priorityApps: String = "") = MuteProfile(
        id = "1", name = "Manual", enabled = enabled, triggerType = MuteProfile.TRIGGER_MANUAL,
        priorityApps = priorityApps
    )

    private fun schedule(start: Int, end: Int, enabled: Boolean = true) = MuteProfile(
        id = "2", name = "Schedule", enabled = enabled, triggerType = MuteProfile.TRIGGER_SCHEDULE,
        scheduleStartMinutes = start, scheduleEndMinutes = end
    )

    private fun appBased(apps: String, enabled: Boolean = true) = MuteProfile(
        id = "3", name = "Apps", enabled = enabled, triggerType = MuteProfile.TRIGGER_APP_FOREGROUND,
        triggerApps = apps
    )

    // --- activeProfiles() ---

    @Test
    fun noProfiles_noneActive() {
        assertTrue(NotificationReaderService.activeProfiles(emptyList(), 12 * 60, null).isEmpty())
    }

    @Test
    fun disabledManualProfile_notActive() {
        assertTrue(NotificationReaderService.activeProfiles(listOf(manual(enabled = false)), 12 * 60, null).isEmpty())
    }

    @Test
    fun enabledManualProfile_alwaysActive() {
        assertEquals(1, NotificationReaderService.activeProfiles(listOf(manual()), 3 * 60, null).size)
    }

    @Test
    fun scheduleProfile_respectsWindow() {
        val profiles = listOf(schedule(22 * 60, 7 * 60))
        assertEquals(1, NotificationReaderService.activeProfiles(profiles, 23 * 60, null).size) // 23:00, overnight window
        assertTrue(NotificationReaderService.activeProfiles(profiles, 12 * 60, null).isEmpty()) // noon, outside
    }

    @Test
    fun appForegroundProfile_matchesOnlyListedApps() {
        val profiles = listOf(appBased("com.game.a,com.game.b"))
        assertEquals(1, NotificationReaderService.activeProfiles(profiles, 12 * 60, "com.game.a").size)
        assertTrue(NotificationReaderService.activeProfiles(profiles, 12 * 60, "com.other.app").isEmpty())
        assertTrue(NotificationReaderService.activeProfiles(profiles, 12 * 60, null).isEmpty())
    }

    // --- isMutedByProfiles() (priority apps / "Allowed Notifications") ---

    @Test
    fun noActiveProfiles_neverMutes() {
        assertFalse(NotificationReaderService.isMutedByProfiles(emptyList(), "com.any.app"))
    }

    @Test
    fun activeProfile_mutesAppsNotOnPriorityList() {
        val active = listOf(manual(priorityApps = "com.phone.dialer"))
        assertTrue(NotificationReaderService.isMutedByProfiles(active, "com.some.chat.app"))
    }

    @Test
    fun activeProfile_letsPriorityAppThrough() {
        val active = listOf(manual(priorityApps = "com.phone.dialer,com.messaging.app"))
        assertFalse(NotificationReaderService.isMutedByProfiles(active, "com.phone.dialer"))
    }

    @Test
    fun multipleActiveProfiles_needAllToAllow() {
        val active = listOf(
            manual(priorityApps = "com.phone.dialer"),
            appBased("com.game.a").copy(priorityApps = "") // second active profile allows nothing
        )
        // one profile allows it, the other doesn't -> still muted
        assertTrue(NotificationReaderService.isMutedByProfiles(active, "com.phone.dialer"))
    }

    @Test
    fun multipleActiveProfiles_allAllow_letsThrough() {
        val active = listOf(
            manual(priorityApps = "com.phone.dialer"),
            appBased("com.game.a").copy(priorityApps = "com.phone.dialer")
        )
        assertFalse(NotificationReaderService.isMutedByProfiles(active, "com.phone.dialer"))
    }
}
