
package com.myphone.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Typeface
import android.text.InputType
import android.view.Gravity
import android.widget.*
import com.google.firebase.auth.FirebaseAuth

class MainActivity : Activity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var status: TextView
    private lateinit var email: EditText
    private lateinit var password: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(35, 35, 35, 35)
        }

        val title = TextView(this).apply {
            text = "MyPhone"
            textSize = 32f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "تسجيل الدخول وربط الهاتفين"
            textSize = 18f
            gravity = Gravity.CENTER
        }

        email = EditText(this).apply {
            hint = "البريد الإلكتروني"
            inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }

        password = EditText(this).apply {
            hint = "كلمة المرور"
            inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        val login = Button(this).apply {
            text = "تسجيل الدخول"
            setOnClickListener { loginUser() }
        }

        status = TextView(this).apply {
            text = "أدخل بيانات حسابك"
            textSize = 16f
            gravity = Gravity.CENTER
        }

        layout.addView(title)
        layout.addView(subtitle)
        layout.addView(email)
        layout.addView(password)
        layout.addView(login)
        layout.addView(status)

        setContentView(layout)

        if (auth.currentUser != null) {
            showLoggedIn()
        }
    }

    private fun loginUser() {
        val mail = email.text.toString().trim()
        val pass = password.text.toString()

        if (mail.isEmpty() || pass.isEmpty()) {
            status.text = "أدخل البريد وكلمة المرور"
            return
        }

        status.text = "جارٍ تسجيل الدخول..."

        auth.signInWithEmailAndPassword(mail, pass)
            .addOnSuccessListener {
                showLoggedIn()
            }
            .addOnFailureListener {
                status.text = "فشل الدخول: ${it.localizedMessage}"
            }
    }

    private fun showLoggedIn() {
        val user = auth.currentUser ?: return

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(30, 30, 30, 30)
        }

        val title = TextView(this).apply {
            text = "تم تسجيل الدخول بنجاح"
            textSize = 23f
            gravity = Gravity.CENTER
        }

        val account = TextView(this).apply {
            text = "الحساب: ${user.email}"
            textSize = 16f
            gravity = Gravity.CENTER
        }

        val next = TextView(this).apply {
            text = "سيتم إعداد ربط الهاتفين في الخطوة التالية"
            textSize = 17f
            gravity = Gravity.CENTER
        }

        val logout = Button(this).apply {
            text = "تسجيل الخروج"
            setOnClickListener {
                auth.signOut()
                recreate()
            }
        }

        layout.addView(title)
        layout.addView(account)
        layout.addView(next)
        layout.addView(logout)

        setContentView(layout)
    }
}
