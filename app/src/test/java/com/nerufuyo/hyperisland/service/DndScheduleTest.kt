package com.nerufuyo.hyperisland.service

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DndScheduleTest {

    @Test
    fun sameDayWindow_insideRange_isWithin() {
        // 09:00 - 17:00, checking 12:00
        assertTrue(NotificationReaderService.isTimeWithinWindow(12 * 60, 9 * 60, 17 * 60))
    }

    @Test
    fun sameDayWindow_outsideRange_isNotWithin() {
        assertFalse(NotificationReaderService.isTimeWithinWindow(8 * 60, 9 * 60, 17 * 60))
        assertFalse(NotificationReaderService.isTimeWithinWindow(17 * 60, 9 * 60, 17 * 60)) // end is exclusive
    }

    @Test
    fun overnightWindow_beforeMidnight_isWithin() {
        // 22:00 - 07:00, checking 23:30
        assertTrue(NotificationReaderService.isTimeWithinWindow(23 * 60 + 30, 22 * 60, 7 * 60))
    }

    @Test
    fun overnightWindow_afterMidnight_isWithin() {
        // 22:00 - 07:00, checking 05:00
        assertTrue(NotificationReaderService.isTimeWithinWindow(5 * 60, 22 * 60, 7 * 60))
    }

    @Test
    fun overnightWindow_duringDay_isNotWithin() {
        // 22:00 - 07:00, checking 12:00
        assertFalse(NotificationReaderService.isTimeWithinWindow(12 * 60, 22 * 60, 7 * 60))
    }
}
