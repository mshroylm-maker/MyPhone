package com.myphone.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private val blue = Color.rgb(35, 91, 190)
    private val dark = Color.rgb(28, 35, 48)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = dark
        window.navigationBarColor = dark

        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            setPadding(28, 42, 28, 28)
            setBackgroundColor(Color.rgb(246, 248, 252))
            layoutDirection = LinearLayout.LAYOUT_DIRECTION_RTL
        }

        val title = TextView(this).apply {
            text = "MyPhone"
            textSize = 30f
            setTextColor(dark)
            gravity = Gravity.CENTER
        }
        page.addView(title, matchWidth(70))

        val subtitle = TextView(this).apply {
            text = "نظامك الخاص لإدارة هاتفك عن بُعد"
            textSize = 16f
            setTextColor(Color.DKGRAY)
            gravity = Gravity.CENTER
        }
        page.addView(subtitle, matchWidth(55))

        val status = TextView(this).apply {
            text = "حالة المشروع: النسخة التأسيسية"
            textSize = 17f
            setTextColor(dark)
            gravity = Gravity.CENTER
            setPadding(12, 24, 12, 24)
        }
        page.addView(status, matchWidth(80))

        addAction(page, "ربط جهاز جديد", "سيتم تفعيل الاقتران الآمن في المرحلة التالية.")
        addAction(page, "الملفات والصور", "سيتم إضافة الوصول المصرّح به للملفات لاحقاً.")
        addAction(page, "مشاركة الشاشة", "تتطلب موافقة Android عند بدء مشاركة الشاشة.")
        addAction(page, "المكالمات والرسائل", "ستضاف الوظائف المتاحة وفق صلاحيات Android.")

        val note = TextView(this).apply {
            text = "تنبيه: هذه نسخة تأسيسية للواجهة فقط. لا تنفّذ اتصالاً عن بُعد ولا تقرأ الملفات أو الرسائل بعد."
            textSize = 13f
            setTextColor(Color.rgb(130, 70, 30))
            gravity = Gravity.CENTER
            setPadding(12, 24, 12, 12)
        }
        page.addView(note, matchWidth(110))

        setContentView(page)
    }

    private fun addAction(parent: LinearLayout, label: String, message: String) {
        val button = Button(this).apply {
            text = label
            textSize = 16f
            isAllCaps = false
            setTextColor(Color.WHITE)
            setBackgroundColor(blue)
            setOnClickListener {
                android.widget.Toast.makeText(this@MainActivity, message, android.widget.Toast.LENGTH_LONG).show()
            }
        }
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 8, 0, 8)
        parent.addView(button, params)
    }

    private fun matchWidth(height: Int): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
}
