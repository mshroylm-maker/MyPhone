
package com.myphone.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Typeface
import android.view.Gravity
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : Activity() {

    private val db by lazy {
        FirebaseDatabase.getInstance().reference.child("pairings")
    }

    private lateinit var status: TextView
    private lateinit var codeInput: EditText
    private lateinit var codeView: TextView

    private val uid get() = FirebaseAuth.getInstance().currentUser?.uid
    private val email get() = FirebaseAuth.getInstance().currentUser?.email

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (uid == null) {
            showMessage("سجّل الدخول أولاً")
            return
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(30, 30, 30, 30)
        }

        val title = TextView(this).apply {
            text = "MyPhone"
            textSize = 30f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "ربط الهاتفين عبر الإنترنت"
            textSize = 18f
            gravity = Gravity.CENTER
        }

        val create = Button(this).apply {
            text = "إنشاء رمز اقتران"
            setOnClickListener { createPair() }
        }

        codeView = TextView(this).apply {
            text = "لم يتم إنشاء رمز"
            textSize = 24f
            gravity = Gravity.CENTER
        }

        codeInput = EditText(this).apply {
            hint = "أدخل رمز الهاتف الأول"
            inputType = 2
        }

        val join = Button(this).apply {
            text = "ربط بهذا الهاتف"
            setOnClickListener { joinPair() }
        }

        status = TextView(this).apply {
            text = "جاهز للربط"
            textSize = 17f
            gravity = Gravity.CENTER
        }

        layout.addView(title)
        layout.addView(subtitle)
        layout.addView(create)
        layout.addView(codeView)
        layout.addView(codeInput)
        layout.addView(join)
        layout.addView(status)

        setContentView(layout)
    }

    private fun createPair() {
        val code = (100000..999999).random().toString()
        val data = mapOf(
            "ownerUid" to uid,
            "ownerEmail" to email,
            "createdAt" to ServerValue.TIMESTAMP
        )

        status.text = "جارٍ إنشاء الرمز..."

        db.child(code).runTransaction(object : Transaction.Handler {
            override fun doTransaction(current: MutableData): Transaction.Result {
                if (current.exists()) return Transaction.abort()
                current.value = data
                return Transaction.success(current)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                snapshot: DataSnapshot?
            ) {
                if (error != null) {
                    status.text = "خطأ: ${error.message}"
                } else if (committed) {
                    codeView.text = "رمز الاقتران: $code"
                    status.text = "أدخل هذا الرمز في الهاتف الثاني"
                    listen(code)
                } else {
                    status.text = "تعذر إنشاء الرمز، حاول مرة أخرى"
                }
            }
        })
    }

    private fun joinPair() {
        val code = codeInput.text.toString().trim()

        if (code.length != 6) {
            status.text = "أدخل رمزاً من 6 أرقام"
            return
        }

        status.text = "جارٍ التحقق من الرمز..."

        db.child(code).runTransaction(object : Transaction.Handler {
            override fun doTransaction(current: MutableData): Transaction.Result {
                if (!current.exists()) return Transaction.abort()

                val owner = current.child("ownerUid").getValue(String::class.java)
                    ?: return Transaction.abort()

                val connected = current.child("connectedUid")
                    .getValue(String::class.java)

                if (owner == uid || (connected != null && connected != uid)) {
                    return Transaction.abort()
                }

                current.child("connectedUid").value = uid
                current.child("connectedEmail").value = email

                return Transaction.success(current)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                snapshot: DataSnapshot?
            ) {
                if (error != null) {
                    status.text = "خطأ: ${error.message}"
                } else if (committed) {
                    codeView.text = "تم إدخال الرمز: $code"
                    status.text = "تم اقتران الهاتفين بنجاح"
                    listen(code)
                } else {
                    status.text = "الرمز غير صحيح أو مستخدم"
                }
            }
        })
    }

    private fun listen(code: String) {
        db.child(code).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.child("connectedUid")
                    .getValue(String::class.java)

                if (connected != null) {
                    status.text = "الهاتفان مقترنان بنجاح"
                } else {
                    status.text = "بانتظار اتصال الهاتف الثاني"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                status.text = "تعذر متابعة الاتصال: ${error.message}"
            }
        })
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
