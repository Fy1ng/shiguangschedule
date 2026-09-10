package com.xingheyuzhuan.shiguangschedule.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.lifecycleScope
import com.xingheyuzhuan.shiguangschedule.R
import kotlinx.coroutines.launch

/** Android AppWidget configuration screen; settings are stored per widget instance. */
class WidgetConfigActivity : ComponentActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // AppWidget hosts expect cancellation unless configuration completes successfully.
        setResult(RESULT_CANCELED)
        appWidgetId = intent?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContentView(R.layout.widget_config_activity)

        val showCourseTimeSwitch = findViewById<SwitchCompat>(R.id.switch_show_course_time)
        showCourseTimeSwitch.isChecked = WidgetPreferences.showCourseTime(this, appWidgetId)

        findViewById<Button>(R.id.button_confirm_widget_config).setOnClickListener {
            WidgetPreferences.setShowCourseTime(this, appWidgetId, showCourseTimeSwitch.isChecked)

            lifecycleScope.launch {
                updateAllWidgets(applicationContext)
                val resultIntent = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }
    }
}
