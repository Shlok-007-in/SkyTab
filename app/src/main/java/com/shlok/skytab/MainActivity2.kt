
package com.shlok.skytab

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity2 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        val backButton = findViewById<Button>(R.id.backButton)
        val liveButton = findViewById<Button>(R.id.liveMapButton)
        val localButton = findViewById<Button>(R.id.localMapButton)
        val webView = findViewById<WebView>(R.id.skyWebView)
        val localView =
            findViewById<LocalSkyView>(R.id.localMapPlaceholder)
        val title = findViewById<View>(R.id.skyMapTitle)

        backButton.setOnClickListener {
            finish()
        }

        liveButton.setOnClickListener {
            localView.visibility = View.GONE
            webView.visibility = View.VISIBLE
            backButton.visibility = View.VISIBLE
            title.visibility = View.GONE

            liveButton.visibility = View.GONE
            localButton.visibility = View.GONE

            webView.settings.javaScriptEnabled = true
            webView.settings.domStorageEnabled = true
            webView.settings.allowFileAccess = false
            webView.settings.allowContentAccess = false
            webView.webViewClient = WebViewClient()

            if (webView.url == null) {
                webView.loadUrl("https://stellarium-web.org/")
            }
        }

        localButton.setOnClickListener {
            webView.stopLoading()
            webView.visibility = View.GONE
            localView.visibility = View.VISIBLE

            liveButton.visibility = View.GONE
            localButton.visibility = View.GONE
            title.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        findViewById<WebView>(R.id.skyWebView)?.stopLoading()
        super.onDestroy()
    }
}
