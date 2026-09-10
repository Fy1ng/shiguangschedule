package com.xingheyuzhuan.shiguangschedule.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.core.widget.RemoteViewsCompat
import com.xingheyuzhuan.shiguangschedule.MainActivity
import com.xingheyuzhuan.shiguangschedule.R
import java.util.UUID

/** Bind each widget instance and column separately, including the pre-Android 12L adapter cache. */
internal fun bindCourseList(
    context: Context,
    views: RemoteViews,
    appWidgetId: Int,
    listId: Int,
    courses: List<WidgetCourseProto>,
    snapshot: WidgetSnapshot,
    verticalTime: Boolean = false
) {
    val showCourseTime = WidgetPreferences.showCourseTime(context, appWidgetId)
    val items = RemoteViewsCompat.RemoteCollectionItems.Builder()
        .setHasStableIds(true)
        .setViewTypeCount(1)
    courses.forEach { course ->
        val id = UUID.nameUUIDFromBytes(
            "${course.id}|${course.date}|${course.start_time}".toByteArray(Charsets.UTF_8)
        ).leastSignificantBits
        items.addItem(
            id,
            renderCourseListItem(context, course, snapshot, verticalTime, showCourseTime)
        )
    }
    RemoteViewsCompat.setRemoteAdapter(context, views, appWidgetId, listId, items.build())

    // Collection rows use a template and fill-in intents. Keep the target explicit.
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val mutability = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
    views.setPendingIntentTemplate(
        listId,
        PendingIntent.getActivity(
            context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT or mutability
        )
    )
}

internal fun renderCourseListItem(
    context: Context,
    course: WidgetCourseProto,
    snapshot: WidgetSnapshot,
    verticalTime: Boolean,
    showCourseTime: Boolean
): RemoteViews {
    val layout = if (verticalTime) R.layout.widget_item_course_list_node else R.layout.widget_item_course_common
    return RemoteViews(context.packageName, layout).apply {
        setTextViewText(R.id.tv_course_name, course.name)
        setTextViewText(R.id.tv_course_position, course.position)
        val timeVisibility = if (showCourseTime) View.VISIBLE else View.GONE
        if (verticalTime) {
            setTextViewText(R.id.tv_course_start_time, course.start_time.take(5))
            setTextViewText(R.id.tv_course_end_time, course.end_time.take(5))
            setViewVisibility(R.id.container_time_vertical, timeVisibility)
        } else {
            setTextViewText(R.id.tv_course_time, "${course.start_time.take(5)}-${course.end_time.take(5)}")
            setViewVisibility(R.id.tv_course_time, timeVisibility)
        }
        setTextViewText(R.id.tv_course_teacher, course.teacher)
        setViewVisibility(R.id.tv_course_teacher, if (course.teacher.isBlank()) View.GONE else View.VISIBLE)
        snapshot.style?.course_color_maps?.getOrNull(course.color_int)?.let { colors ->
            setInt(R.id.course_indicator, "setColorFilter", colors.light_color.toInt())
            setInt(R.id.course_indicator_dark, "setColorFilter", colors.dark_color.toInt())
        }
        setOnClickFillInIntent(R.id.widget_course_item, Intent())
    }
}
