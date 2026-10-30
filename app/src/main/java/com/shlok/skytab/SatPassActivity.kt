package com.shlok.skytab

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SatPassActivity : AppCompatActivity() {

    private lateinit var satelliteList: LinearLayout
    private lateinit var trackerPanel: LinearLayout
    private lateinit var satPassTitle: TextView
    private lateinit var satPassMessage: TextView
    private lateinit var satPassWebView: WebView
    private lateinit var satPassProgress: ProgressBar

    private lateinit var backButton: Button

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sat_pass)

        backButton = findViewById(R.id.backButton)

        backButton.setOnClickListener {
            finish()
        }

        satelliteList = findViewById(R.id.satelliteList)
        trackerPanel = findViewById(R.id.trackerPanel)
        satPassTitle = findViewById(R.id.satPassTitle)
        satPassMessage = findViewById(R.id.satPassMessage)
        satPassWebView = findViewById(R.id.satPassWebView)
        satPassProgress = findViewById(R.id.satPassProgress)

        findViewById<Button>(R.id.backButton).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.chooseAnotherButton)
            .setOnClickListener {
                showSatelliteList()
            }

        satPassWebView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadsImagesAutomatically = true
        }

        satPassWebView.webViewClient = WebViewClient()

        satPassWebView.webChromeClient =
            object : WebChromeClient() {
                override fun onProgressChanged(
                    view: WebView?,
                    newProgress: Int
                ) {
                    satPassProgress.visibility =
                        if (newProgress < 100) View.VISIBLE
                        else View.GONE
                }
            }

        satPassWebView.setLayerType(
            View.LAYER_TYPE_HARDWARE, null
        )

        findViewById<Button>(R.id.issButton).setOnClickListener {
            openTracker(
                "ISS — Space Station",
                "https://azmth.space/embed?sat=25544&rotate=0"
            )
        }

        findViewById<Button>(R.id.hubbleButton).setOnClickListener {
            openTracker(
                "Hubble Space Telescope",
                "https://azmth.space/embed?sat=20580&rotate=0"
            )
        }

        findViewById<Button>(R.id.starlinkButton).setOnClickListener {
            openTracker(
                "Starlink Train",
                "https://azmth.space/embed?trains=1&rotate=0"
            )
        }

        findViewById<Button>(R.id.gpsButton).setOnClickListener {
            openTracker(
                "GPS Satellites",
                "https://azmth.space/embed?filter=gps&rotate=0"
            )
        }

        findViewById<Button>(R.id.stationsButton).setOnClickListener {
            openTracker(
                "Space Stations",
                "https://azmth.space/embed?filter=station&rotate=0"
            )
        }

        showSatelliteList()
    }

    private fun openTracker(title: String, url: String) {
        satPassTitle.text = title
        satPassMessage.text = "Loading tracker…"

        backButton.visibility = View.GONE
        satPassTitle.visibility = View.GONE
        satPassMessage.visibility = View.GONE

        satelliteList.visibility = View.GONE
        trackerPanel.visibility = View.VISIBLE

        satPassWebView.stopLoading()
        satPassWebView.loadUrl(url)
    }

    private fun showSatelliteList() {
        satPassWebView.stopLoading()
        satPassWebView.loadUrl("about:blank")

        trackerPanel.visibility = View.GONE
        satelliteList.visibility = View.VISIBLE

        backButton.visibility = View.VISIBLE
        satPassTitle.visibility = View.VISIBLE
        satPassMessage.visibility = View.VISIBLE

        satPassTitle.text = "SAT-PASS"
        satPassMessage.text = "Choose a satellite to track"
        satPassProgress.visibility = View.GONE
    }

    @Deprecated("Deprecated in Android API")
    override fun onBackPressed() {
        if (trackerPanel.visibility == View.VISIBLE) {
            showSatelliteList()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        if (::satPassWebView.isInitialized) {
            satPassWebView.stopLoading()
            satPassWebView.loadUrl("about:blank")
            satPassWebView.destroy()
        }
        super.onDestroy()
    }
}