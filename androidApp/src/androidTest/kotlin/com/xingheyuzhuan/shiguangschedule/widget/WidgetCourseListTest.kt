package com.xingheyuzhuan.shiguangschedule.widget

import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.ListView
import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.xingheyuzhuan.shiguangschedule.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WidgetCourseListTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @Test
    fun hidingAndRestoringTimesReclaimsLocationSpaceInBothThemes() {
        instrumentation.runOnMainSync {
            for (night in listOf(Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_YES)) {
                val base = instrumentation.targetContext
                val config = Configuration(base.resources.configuration).apply {
                    uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or night
                }
                val context = base.createConfigurationContext(config)
                val course = WidgetCourseProto(
                    name = "模拟电子技术 I", position = "砺志楼 0106", teacher = "教师",
                    start_time = "08:00", end_time = "09:40"
                )
                for (vertical in listOf(false, true)) {
                    val shown = WidgetSnapshot()
                    val row = renderCourseListItem(context, course, shown, vertical)
                        .apply(context, FrameLayout(context))
                    measure(row, 200, null)
                    val position = row.findViewById<TextView>(R.id.tv_course_position)
                    val originalWidth = position.width
                    val originalLeft = position.left
                    val content = position.parent as View
                    val originalContentLeft = content.left
                    val originalContentWidth = content.width
                    val timeId = if (vertical) R.id.container_time_vertical else R.id.tv_course_time
                    renderCourseListItem(context, course.copy(teacher = ""), shown.copy(hide_course_time = true), vertical)
                        .reapply(context, row)
                    measure(row, 200, null)
                    assertEquals(View.GONE, row.findViewById<View>(timeId).visibility)
                    assertEquals(View.GONE, row.findViewById<View>(R.id.tv_course_teacher).visibility)
                    // The common row expands its weighted location; the vertical row expands its content column.
                    if (vertical) {
                        assertTrue(content.left < originalContentLeft)
                        assertTrue(content.width > originalContentWidth)
                    } else {
                        assertTrue(position.width > originalWidth)
                    }
                    renderCourseListItem(context, course, shown, vertical).reapply(context, row)
                    measure(row, 200, null)
                    assertEquals(View.VISIBLE, row.findViewById<View>(timeId).visibility)
                    assertEquals(View.VISIBLE, row.findViewById<View>(R.id.tv_course_teacher).visibility)
                    assertEquals(originalWidth, position.width)
                    assertEquals(originalLeft, position.left)
                }
            }
        }
    }

    @Test
    fun doubleDayColumnsHaveBoundedScrollableContentBelowHeaders() {
        instrumentation.runOnMainSync {
            val context = instrumentation.targetContext
            val root = LayoutInflater.from(context).inflate(R.layout.widget_double_days_native, FrameLayout(context), false)
            val today = root.findViewById<ListView>(R.id.container_today)
            val tomorrow = root.findViewById<ListView>(R.id.container_tomorrow)
            for (list in listOf(today, tomorrow)) {
                list.adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, (1..20).map { "Course $it" })
            }
            measure(root, 400, 220)
            assertTrue(today.height > 0 && today.height < root.height)
            assertTrue(tomorrow.height > 0 && tomorrow.height < root.height)
            assertTrue(today.canScrollVertically(1))
            assertTrue(tomorrow.canScrollVertically(1))
            today.setSelection(10)
            measure(root, 400, 220)
            assertTrue(today.firstVisiblePosition > 0)
            assertEquals(0, tomorrow.firstVisiblePosition)
        }
    }

    private fun measure(view: View, widthDp: Int, heightDp: Int?) {
        val density = view.resources.displayMetrics.density
        view.measure(
            View.MeasureSpec.makeMeasureSpec((widthDp * density).toInt(), View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(
                heightDp?.let { (it * density).toInt() } ?: 0,
                if (heightDp == null) View.MeasureSpec.UNSPECIFIED else View.MeasureSpec.EXACTLY
            )
        )
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
    }
}
