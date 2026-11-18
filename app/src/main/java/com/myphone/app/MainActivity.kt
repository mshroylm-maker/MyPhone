package com.myphone.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Typeface
import android.view.Gravity
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : Activity() {

    private val auth by lazy { FirebaseAuth.getInstance() }

    private val db by lazy {
        FirebaseDatabase.getInstance().reference.child("pairings")
    }

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var codeInput: EditText
    private lateinit var status: TextView
    private lateinit var codeView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (auth.currentUser != null) {
            showHome()
        } else {
            showLogin()
        }
    }

    private fun showLogin() {
        val layout = makeLayout()

        layout.addView(makeTitle("MyPhone"))

        emailInput = EditText(this).apply {
            hint = "البريد الإلكتروني"
            inputType = 33
        }

        passwordInput = EditText(this).apply {
            hint = "كلمة المرور"
            inputType = 129
        }

        layout.addView(emailInput)
        layout.addView(passwordInput)

        addButton(layout, "تسجيل الدخول") {
            login()
        }

        addButton(layout, "إنشاء حساب جديد") {
            register()
        }

        status = makeStatus()
        layout.addView(status)

        setContentView(layout)
    }

    private fun login() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()

        if (email.isBlank() || password.isBlank()) {
            status.text = "أدخل البريد وكلمة المرور"
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                showHome()
            }
            .addOnFailureListener {
                status.text = "فشل الدخول: ${it.message}"
            }
    }

    private fun register() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()

        if (email.isBlank() || password.length < 6) {
            status.text = "أدخل بريداً صحيحاً وكلمة مرور من 6 أحرف على الأقل"
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                showHome()
            }
            .addOnFailureListener {
                status.text = "فشل إنشاء الحساب: ${it.message}"
            }
    }

    private fun showHome() {
        val layout = makeLayout()

        layout.addView(makeTitle("MyPhone"))

        val subtitle = TextView(this).apply {
            text = "ربط الهاتفين"
            textSize = 18f
            gravity = Gravity.CENTER
        }

        layout.addView(subtitle)

        addButton(layout, "إنشاء رمز اقتران") {
            createPair()
        }

        codeView = TextView(this).apply {
            text = "لا يوجد رمز"
            textSize = 24f
            gravity = Gravity.CENTER
        }

        layout.addView(codeView)

        codeInput = EditText(this).apply {
            hint = "أدخل رمز الهاتف الأول"
            inputType = 2
        }

        layout.addView(codeInput)

        addButton(layout, "ربط الهاتف الثاني") {
            joinPair()
        }

        addButton(layout, "تسجيل الخروج") {
            auth.signOut()
            showLogin()
        }

        status = makeStatus()
        layout.addView(status)

        setContentView(layout)
    }

    private fun createPair() {
        val uid = auth.currentUser?.uid ?: return
        val code = (100000..999999).random().toString()

        status.text = "جارٍ إنشاء الرمز..."

        val data = mapOf(
            "ownerUid" to uid,
            "createdAt" to ServerValue.TIMESTAMP,
            "connectedUid" to ""
        )

        db.child(code).setValue(data)
            .addOnSuccessListener {
                codeView.text = code
                status.text = "أرسل الرمز للهاتف الثاني"
                listen(code)
            }
            .addOnFailureListener {
                status.text = "تعذر إنشاء الرمز: ${it.message}"
            }
    }

    private fun joinPair() {
        val code = codeInput.text.toString().trim()
        val uid = auth.currentUser?.uid ?: return

        if (code.length != 6) {
            status.text = "أدخل رمزاً من 6 أرقام"
            return
        }

        status.text = "جارٍ التحقق..."

        val ref = db.child(code)

        ref.runTransaction(object : Transaction.Handler {

            override fun doTransaction(
                current: MutableData
            ): Transaction.Result {

                if (!current.exists()) {
                    return Transaction.abort()
                }

                val owner = current.child("ownerUid")
                    .getValue(String::class.java)

                val connected = current.child("connectedUid")
                    .getValue(String::class.java)

                if (owner == uid ||
                    (!connected.isNullOrEmpty() && connected != uid)
                ) {
                    return Transaction.abort()
                }

                current.child("connectedUid").value = uid

                return Transaction.success(current)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                snapshot: DataSnapshot?
            ) {
                if (error != null) {
                    status.text = error.message
                } else if (committed) {
                    status.text = "تم الاقتران"
                    listen(code)
                } else {
                    status.text = "الرمز غير صحيح أو مستخدم"
                }
            }
        })
    }

    private fun listen(code: String) {
        db.child(code).addValueEventListener(
            object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val connected = snapshot.child("connectedUid")
                        .getValue(String::class.java)

                    status.text =
                        if (!connected.isNullOrEmpty()) {
                            "الهاتفان مقترنان"
                        } else {
                            "بانتظار الهاتف الثاني"
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    status.text = error.message
                }
            }
        )
    }

    private fun makeLayout(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 28, 28, 28)
        }
    }

    private fun makeTitle(value: String): TextView {
        return TextView(this).apply {
            text = value
            textSize = 30f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
        }
    }

    private fun makeStatus(): TextView {
        return TextView(this).apply {
            text = "جاهز"
            textSize = 16f
            gravity = Gravity.CENTER
        }
    }

    private fun addButton(
        layout: LinearLayout,
        label: String,
        action: () -> Unit
    ) {
        layout.addView(Button(this).apply {
            text = label
            setOnClickListener { action() }
        })
    }
}