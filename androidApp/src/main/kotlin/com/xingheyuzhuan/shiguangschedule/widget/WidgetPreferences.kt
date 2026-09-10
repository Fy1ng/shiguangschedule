package com.xingheyuzhuan.shiguangschedule.widget

import android.content.Context

/** Android-only preferences scoped to individual home-screen widget instances. */
internal object WidgetPreferences {
    private const val PREFS_NAME = "widget_preferences"
    private const val KEY_SHOW_COURSE_TIME_PREFIX = "show_course_time_"

    private fun showCourseTimeKey(appWidgetId: Int) = "$KEY_SHOW_COURSE_TIME_PREFIX$appWidgetId"

    fun showCourseTime(context: Context, appWidgetId: Int): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(showCourseTimeKey(appWidgetId), true)

    fun setShowCourseTime(context: Context, appWidgetId: Int, show: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(showCourseTimeKey(appWidgetId), show)
            .apply()
    }

    fun delete(context: Context, appWidgetId: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(showCourseTimeKey(appWidgetId))
            .apply()
    }
}
