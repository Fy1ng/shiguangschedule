package com.xingheyuzhuan.shiguangschedule.data.model

import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesOf
import com.xingheyuzhuan.shiguangschedule.widget.WidgetSnapshot
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WidgetTimePreferenceTest {
    @Test
    fun existingPreferencesAndSnapshotsKeepTimesVisible() {
        assertTrue(AppSettingsModel.fromPreferences(emptyPreferences(), "table").showWidgetCourseTime)
        assertFalse(WidgetSnapshot.ADAPTER.decode(byteArrayOf()).hide_course_time)
    }

    @Test
    fun savedPreferenceSurvivesReloadAndSnapshotSerialization() {
        for (show in listOf(false, true)) {
            val prefs = preferencesOf(AppSettingsModel.KEY_SHOW_WIDGET_COURSE_TIME to show)
            val settings = AppSettingsModel.fromPreferences(prefs, "table")
            val snapshot = WidgetSnapshot(hide_course_time = !settings.showWidgetCourseTime)
            val restored = WidgetSnapshot.ADAPTER.decode(WidgetSnapshot.ADAPTER.encode(snapshot))
            assertTrue(restored.hide_course_time == !show)
        }
    }
}
