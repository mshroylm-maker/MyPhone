
package com.myphone.app

import android.app.Activity
import android.os.Bundle
import android.widget.*
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

class MainActivity : Activity() {

    private val port = 8989
    private var serverSocket: ServerSocket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 40, 30, 30)
        }

        val title = TextView(this).apply {
            text = "MyPhone - ربط الهاتفين"
            textSize = 24f
        }

        val ipInput = EditText(this).apply {
            hint = "اكتب عنوان IP للهاتف الآخر"
            inputType =  textInputType()
        }

        val status = TextView(this).apply {
            text = "الحالة: غير متصل"
            textSize = 18f
        }

        val connect = Button(this).apply {
            text = "اتصال بالهاتف الآخر"
            setOnClickListener {
                val ip = ipInput.text.toString().trim()
                if (ip.isEmpty()) {
                    status.text = "اكتب عنوان IP أولاً"
                } else {
                    thread {
                        try {
                            Socket(ip, port).use {
                                runOnUiThread {
                                    status.text = "تم الاتصال بنجاح"
                                }
                            }
                        } catch (e: Exception) {
                            runOnUiThread {
                                status.text = "فشل الاتصال: ${e.message}"
                            }
                        }
                    }
                }
            }
        }

        val start = Button(this).apply {
            text = "تشغيل استقبال الاتصال"
            setOnClickListener {
                thread {
                    try {
                        serverSocket = ServerSocket(port)
                        runOnUiThread {
                            status.text = "الهاتف جاهز لاستقبال الاتصال"
                        }
                        while (!serverSocket!!.isClosed) {
                            serverSocket!!.accept().use {
                                runOnUiThread {
                                    status.text = "تم اتصال الهاتف الآخر"
                                }
                            }
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            status.text = "توقف الاستقبال: ${e.message}"
                        }
                    }
                }
            }
        }

        layout.addView(title)
        layout.addView(ipInput)
        layout.addView(start)
        layout.addView(connect)
        layout.addView(status)

        setContentView(layout)
    }

    private fun textInputType(): Int =
        android.text.InputType.TYPE_CLASS_TEXT

    override fun onDestroy() {
        serverSocket?.close()
        super.onDestroy()
    }
}
