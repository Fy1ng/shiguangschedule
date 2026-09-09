package com.xingheyuzhuan.shiguangschedule.widget.list_vertical

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.xingheyuzhuan.shiguangschedule.MainActivity
import com.xingheyuzhuan.shiguangschedule.R
import com.xingheyuzhuan.shiguangschedule.widget.bindCourseList
import com.xingheyuzhuan.shiguangschedule.widget.WidgetSnapshot
import com.xingheyuzhuan.shiguangschedule.widget.WidgetCourseProto
import java.time.LocalDate
import java.time.LocalTime

object ListVerticalNativeRenderer {

    fun render(context: Context, snapshot: WidgetSnapshot, appWidgetId: Int): RemoteViews {
        val rv = RemoteViews(context.packageName, R.layout.widget_list_vertical_native)

        resetWidgetState(rv)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        rv.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

        val now = LocalTime.now()
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)
        val allCourses = snapshot.courses
        val currentWeek = if (snapshot.current_week <= 0) null else snapshot.current_week

        if (currentWeek == null) {
            showFullStatus(
                rv,
                context.getString(R.string.title_vacation),
                context.getString(R.string.widget_vacation_expecting)
            )
            return rv
        }

        val todayStr = today.toString()
        val tomorrowStr = tomorrow.toString()

        val todayRemaining = allCourses.filter {
            (it.date == todayStr || it.date.isBlank()) && !it.is_skipped && try { LocalTime.parse(it.end_time) > now } catch (_: Exception) { true }
        }.sortedBy { it.start_time }

        val tomorrowCourses = allCourses.filter {
            it.date == tomorrowStr && !it.is_skipped
        }.sortedBy { it.start_time }

        val weekDaysArray = context.resources.getStringArray(R.array.week_days_full_names)
        val dayOfWeekStr = weekDaysArray[today.dayOfWeek.value - 1]

        when {
            todayRemaining.isNotEmpty() -> {
                val weekText = context.getString(R.string.title_current_week, currentWeek.toString())
                rv.setTextViewText(R.id.tv_header_title, "$weekText  $dayOfWeekStr")
                rv.setTextViewText(R.id.tv_header_count_summary, context.getString(R.string.widget_remaining_courses_format_today, todayRemaining.size))
                renderCourseContent(context, rv, todayRemaining, snapshot, appWidgetId)
            }
            tomorrowCourses.isNotEmpty() -> {
                rv.setTextViewText(R.id.tv_header_title, context.getString(R.string.widget_tomorrow_course_preview))
                rv.setTextViewText(R.id.tv_header_count_summary, context.getString(R.string.widget_remaining_courses_format_tomorrow, tomorrowCourses.size))
                renderCourseContent(context, rv, tomorrowCourses, snapshot, appWidgetId)
            }
            else -> {
                val hasCoursesToday = allCourses.any { it.date == todayStr || it.date.isBlank() }
                val tip = if (!hasCoursesToday) context.getString(R.string.text_no_courses_today) else context.getString(R.string.widget_today_courses_finished)
                val weekText = context.getString(R.string.title_current_week, currentWeek.toString())
                rv.setTextViewText(R.id.tv_header_title, "$weekText  $dayOfWeekStr")
                showInnerStatus(rv, tip)
                rv.setTextViewText(R.id.tv_header_count_summary, "")
            }
        }
        return rv
    }

    private fun resetWidgetState(rv: RemoteViews) {
        rv.setViewVisibility(R.id.container_full_status, View.GONE)
        rv.setViewVisibility(R.id.inner_content_card, View.VISIBLE)
        rv.setViewVisibility(R.id.container_courses, View.GONE)
        rv.setViewVisibility(R.id.container_status, View.GONE)
    }

    private fun renderCourseContent(context: Context, rv: RemoteViews, courses: List<WidgetCourseProto>, snapshot: WidgetSnapshot, appWidgetId: Int) {
        rv.setViewVisibility(R.id.container_courses, View.VISIBLE)

        bindCourseList(context, rv, appWidgetId, R.id.container_courses, courses, snapshot, verticalTime = true)
    }

    private fun showFullStatus(rv: RemoteViews, title: String, msg: String) {
        rv.setViewVisibility(R.id.inner_content_card, View.GONE)
        rv.setViewVisibility(R.id.container_full_status, View.VISIBLE)
        rv.setTextViewText(R.id.tv_full_status_title, title)
        rv.setTextViewText(R.id.tv_full_status_msg, msg)
    }

    private fun showInnerStatus(rv: RemoteViews, title: String) {
        rv.setViewVisibility(R.id.container_courses, View.GONE)
        rv.setViewVisibility(R.id.container_status, View.VISIBLE)
        rv.setTextViewText(R.id.tv_status_title, title)
    }
}