
package com.shlok.skytab

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var clockText: TextView
    private lateinit var appTitle: TextView

    private val handler = Handler(Looper.getMainLooper())

    // Change this only if your PC's local IP address changes.
    private val pcIp = "192.168.1.2"

    private val preferences by lazy {
        getSharedPreferences("SkyTabConnection", MODE_PRIVATE)
    }

    private val pcToken: String?
        get() = preferences.getString("device_token", null)

    private val clockUpdater = object : Runnable {
        override fun run() {
            clockText.text =
                SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        clockText = findViewById(R.id.clockText)
        appTitle = findViewById(R.id.appTitle)

        val animatedIds = listOf(
            R.id.skyMapButton,
            R.id.satPassButton,
            R.id.shutDownButton,
            R.id.sleepButton,
            R.id.testConnectionButton,
            R.id.pcStatusText
        )

        clockText.alpha = 0f

        animatedIds.forEach { id ->
            findViewById<View>(id).apply {
                alpha = 0f
                translationY = 24f
            }
        }

        appTitle.post {
            val parent = appTitle.parent as View

            val targetX = appTitle.x
            val targetY = appTitle.y

            val centerX = (parent.width - appTitle.width) / 2f
            val centerY = (parent.height - appTitle.height) / 2f

            appTitle.alpha = 0f
            appTitle.x = centerX
            appTitle.y = centerY

            appTitle.animate()
                .alpha(1f)
                .x(targetX)
                .y(targetY)
                .setDuration(1000)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction {
                    animateHomeScreen()
                }
                .start()

            clockText.alpha = 0f
            clockText.scaleX = 0.9f
            clockText.scaleY = 0.9f
        }

        handler.post(clockUpdater)

        findViewById<Button>(R.id.skyMapButton).setOnClickListener {
            startActivity(
                android.content.Intent(this, MainActivity2::class.java)
            )
        }

        findViewById<Button>(R.id.satPassButton).setOnClickListener {
            startActivity(
                android.content.Intent(this, SatPassActivity::class.java)
            )
        }

        findViewById<Button>(R.id.shutDownButton).setOnClickListener {
            confirmPowerAction(
                "shutdown",
                "Shut Down PC?",
                "The PC will shut down after a 30-second countdown."
            )
        }

        findViewById<Button>(R.id.sleepButton).setOnClickListener {
            confirmPowerAction(
                "sleep",
                "Put PC to Sleep?",
                "Your Windows PC will enter sleep mode."
            )
        }

        findViewById<Button>(R.id.testConnectionButton).setOnClickListener {
            checkConnection()
        }

        updateConnectionLabel()
    }

    private fun updateConnectionLabel() {
        val status = findViewById<TextView>(R.id.pcStatusText)

        status.text = if (pcToken == null) {
            "PC status: Not paired"
        } else {
            "PC status: Paired"
        }
    }

    private fun checkConnection() {
        sendRequest("/status", "GET", null) { success, message ->
            if (success) {
                if (pcToken == null) {
                    showPairingDialog()
                } else {
                    findViewById<TextView>(R.id.pcStatusText).text =
                        "PC status: Connected"
                    Toast.makeText(
                        this,
                        "PC connected!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                findViewById<TextView>(R.id.pcStatusText).text =
                    "PC status: Offline"

                Toast.makeText(
                    this,
                    "Connection failed: $message",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showPairingDialog() {
        val codeInput = EditText(this).apply {
            hint = "Enter 6-digit pairing code"
            inputType = InputType.TYPE_CLASS_NUMBER
            setSingleLine(true)
        }

        AlertDialog.Builder(this)
            .setTitle("Pair with SkyTab Companion")
            .setMessage("Enter the pairing code shown in the PC companion window.")
            .setView(codeInput)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Pair", null)
            .create()
            .also { dialog ->
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener {
                            val code = codeInput.text.toString().trim()

                            if (!code.matches(Regex("\\d{6}"))) {
                                codeInput.error = "Enter a valid 6-digit code"
                                return@setOnClickListener
                            }

                            dialog.dismiss()
                            pairWithPc(code)
                        }
                }

                dialog.show()
            }
    }

    private fun pairWithPc(code: String) {
        val body = JSONObject().put("code", code).toString()

        sendRequest(
            path = "/pair",
            method = "POST",
            body = body
        ) { success, response ->
            if (success) {
                try {
                    val token = JSONObject(response).getString("token")

                    preferences.edit()
                        .putString("device_token", token)
                        .apply()

                    findViewById<TextView>(R.id.pcStatusText).text =
                        "PC status: Connected"

                    Toast.makeText(
                        this,
                        "SkyTab paired successfully!",
                        Toast.LENGTH_LONG
                    ).show()

                } catch (e: Exception) {
                    Toast.makeText(
                        this,
                        "Pairing response was invalid",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(
                    this,
                    "Pairing failed: $response",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun confirmPowerAction(
        action: String,
        title: String,
        message: String
    ) {
        if (pcToken == null) {
            Toast.makeText(
                this,
                "Please pair SkyTab with your PC first.",
                Toast.LENGTH_LONG
            ).show()

            showPairingDialog()
            return
        }

        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Continue") { _, _ ->
                sendRequest(
                    path = "/power/$action",
                    method = "POST",
                    body = null,
                    token = pcToken
                ) { success, result ->
                    Toast.makeText(
                        this,
                        if (success) "$action request sent"
                        else "Failed: $result",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .show()
    }

    private fun sendRequest(
        path: String,
        method: String,
        body: String?,
        token: String? = null,
        callback: (Boolean, String) -> Unit
    ) {
        Thread {
            var connection: HttpURLConnection? = null

            try {
                connection = URL("http://$pcIp:8765$path")
                    .openConnection() as HttpURLConnection

                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.requestMethod = method

                if (body != null) {
                    connection.doOutput = true
                    connection.setRequestProperty(
                        "Content-Type",
                        "application/json"
                    )
                }

                if (token != null) {
                    connection.setRequestProperty(
                        "X-SkyTab-Token",
                        token
                    )
                }

                if (body != null) {
                    connection.outputStream.use {
                        it.write(body.toByteArray(Charsets.UTF_8))
                    }
                }

                val code = connection.responseCode
                val success = code in 200..299

                val stream = if (success) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

                val response = stream?.bufferedReader()?.use {
                    it.readText()
                } ?: "HTTP $code"

                runOnUiThread {
                    callback(success, response)
                }

            } catch (e: Exception) {
                runOnUiThread {
                    callback(false, e.message ?: "Unknown error")
                }
            } finally {
                connection?.disconnect()
            }
        }.start()
    }

    private fun animateHomeScreen() {
        val testButton =
            findViewById<Button>(R.id.testConnectionButton)

        val pcStatus =
            findViewById<TextView>(R.id.pcStatusText)

        clockText.alpha = 0f
        clockText.scaleX = 0.9f
        clockText.scaleY = 0.9f

        val buttons = listOf(
            findViewById<Button>(R.id.skyMapButton),
            findViewById<Button>(R.id.satPassButton),
            findViewById<Button>(R.id.shutDownButton),
            findViewById<Button>(R.id.sleepButton)
        )

        buttons.forEach {
            it.alpha = 0f
            it.translationY = 24f
        }

        pcStatus.alpha = 0f
        pcStatus.translationY = 16f

        testButton.alpha = 0f
        testButton.translationY = 24f

        clockText.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setStartDelay(1000)
            .setDuration(550)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        buttons.forEachIndexed { index, button ->
            button.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(1550L + index * 140L)
                .setDuration(450)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }

        pcStatus.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(2200)
            .setDuration(400)
            .start()

        testButton.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(2350)
            .setDuration(450)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    override fun onDestroy() {
        handler.removeCallbacks(clockUpdater)
        super.onDestroy()
    }
}
