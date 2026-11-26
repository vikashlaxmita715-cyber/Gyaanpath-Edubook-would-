package com.gyaanpath.playstorebrowser

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback

class MainActivity : ComponentActivity() {
    private lateinit var webView: WebView
    private lateinit var addressBar: EditText
    private lateinit var backButton: Button
    private lateinit var forwardButton: Button
    private lateinit var progressBar: ProgressBar

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.rgb(248, 249, 250)
        window.navigationBarColor = Color.rgb(248, 249, 250)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
            View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
        }

        val header = TextView(this).apply {
            text = "Play Store Browser"
            textSize = 20f
            setTextColor(Color.rgb(32, 33, 36))
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(8))
        }
        root.addView(header, LinearLayout.LayoutParams(-1, dp(52)))

        val searchRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(4), dp(12), dp(8))
        }
        addressBar = EditText(this).apply {
            hint = "Search Google Play or enter a URL"
            textSize = 15f
            singleLine = true
            imeOptions = EditorInfo.IME_ACTION_GO
            setPadding(dp(12), 0, dp(12), 0)
            background = roundedBackground(Color.rgb(245, 247, 248), dp(12))
            setTextColor(Color.rgb(32, 33, 36))
            setHintTextColor(Color.rgb(95, 99, 104))
            setSelectAllOnFocus(false)
        }
        searchRow.addView(addressBar, LinearLayout.LayoutParams(0, dp(48), 1f))
        val goButton = Button(this).apply {
            text = "Go"
            isAllCaps = false
            setTextColor(Color.WHITE)
            background = roundedBackground(Color.rgb(26, 115, 86), dp(10))
            setOnClickListener { navigateFromAddressBar() }
        }
        searchRow.addView(goButton, LinearLayout.LayoutParams(dp(64), dp(44)).apply {
            marginStart = dp(8)
        })
        root.addView(searchRow)

        progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            progress = 0
            visibility = View.GONE
            progressDrawable.setTint(Color.rgb(26, 115, 86))
        }
        root.addView(progressBar, LinearLayout.LayoutParams(-1, dp(3)))

        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadsImagesAutomatically = true
            settings.javaScriptCanOpenWindowsAutomatically = false
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    return routeUrl(request.url)
                }

                @Suppress("DEPRECATION")
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean = routeUrl(Uri.parse(url))

                override fun onPageStarted(view: WebView, url: String, favicon: android.graphics.Bitmap?) {
                    addressBar.setText(url)
                    backButton.isEnabled = view.canGoBack()
                    forwardButton.isEnabled = view.canGoForward()
                }

                override fun onPageFinished(view: WebView, url: String) {
                    addressBar.setText(url)
                    backButton.isEnabled = view.canGoBack()
                    forwardButton.isEnabled = view.canGoForward()
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView, newProgress: Int) {
                    progressBar.progress = newProgress
                    progressBar.visibility = if (newProgress in 1..99) View.VISIBLE else View.GONE
                }
            }
        }
        root.addView(webView, LinearLayout.LayoutParams(-1, 0, 1f))

        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(4), dp(12), dp(8))
        }
        backButton = controlButton("Back") { if (webView.canGoBack()) webView.goBack() }
        forwardButton = controlButton("Forward") { if (webView.canGoForward()) webView.goForward() }
        val homeButton = controlButton("Home") { webView.loadUrl(HOME_URL) }
        val reloadButton = controlButton("Reload") { webView.reload() }
        controls.addView(backButton, controlLayout())
        controls.addView(forwardButton, controlLayout())
        controls.addView(homeButton, controlLayout())
        controls.addView(reloadButton, controlLayout())
        root.addView(controls)

        setContentView(root)
        addressBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE) {
                navigateFromAddressBar()
                true
            } else false
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack() else finish()
            }
        })
        if (savedInstanceState == null) webView.loadUrl(HOME_URL) else webView.restoreState(savedInstanceState)
    }

    private fun navigateFromAddressBar() {
        val input = addressBar.text.toString().trim()
        if (input.isEmpty()) return
        val url = if (input.contains(' ') || !input.contains('.')) {
            "https://play.google.com/store/search?q=${Uri.encode(input)}&c=apps"
        } else {
            val candidate = if (input.startsWith("http://") || input.startsWith("https://")) input else "https://$input"
            val uri = Uri.parse(candidate)
            if (uri.scheme != "https" || !isPlayStoreHost(uri.host)) {
                Toast.makeText(this, "Only Google Play Store pages open in this app", Toast.LENGTH_SHORT).show()
                return
            }
            candidate
        }
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(addressBar.windowToken, 0)
        webView.loadUrl(url)
    }

    private fun routeUrl(uri: Uri): Boolean {
        if (uri.scheme == "https" && isPlayStoreHost(uri.host)) return false
        if (uri.scheme == "http" || uri.scheme == "https") {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(this, "No browser available to open this link", Toast.LENGTH_SHORT).show()
            }
            return true
        }
        return true
    }

    private fun isPlayStoreHost(host: String?): Boolean =
        host == "play.google.com" || host?.endsWith(".play.google.com") == true

    private fun controlButton(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        isAllCaps = false
        textSize = 12f
        setOnClickListener { action() }
    }

    private fun controlLayout() = LinearLayout.LayoutParams(0, dp(48), 1f)

    private fun roundedBackground(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = radius.toFloat()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    override fun onSaveInstanceState(outState: Bundle) {
        webView.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }

    companion object {
        private const val HOME_URL = "https://play.google.com/store"
    }
}
