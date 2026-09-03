package com.example.weathergpt_android.core.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * ThreeUI <LiquidMetalButton /> Variant: Sign up Pill (pill).
 * Authored liquid-metal Sign up pill with complete spectral dispersion field,
 * bloom pass, pointer well displacement, and faceted press ripple.
 * Rendered using raw WebGL 2 + DOM inside hardware-accelerated transparent surface.
 * Source SHA-256: 76624e881a3a
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LiquidMetalButton(
    text: String = "Sign up",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 80.dp
) {
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth().height(height),
            factory = { context: Context ->
                WebView(context).apply {
                    setLayerType(View.LAYER_TYPE_HARDWARE, null)
                    setBackgroundColor(Color.TRANSPARENT)
                    isVerticalScrollBarEnabled = false
                    isHorizontalScrollBarEnabled = false
                    overScrollMode = View.OVER_SCROLL_NEVER

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        allowFileAccess = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                    }

                    addJavascriptInterface(object {
                        @JavascriptInterface
                        fun onButtonClick() {
                            mainHandler.post {
                                onClick()
                            }
                        }
                    }, "AndroidBridge")

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            view?.evaluateJavascript("if (window.setButtonLabel) { window.setButtonLabel('$text'); }", null)
                        }
                    }

                    webChromeClient = WebChromeClient()

                    loadUrl("file:///android_asset/liquid_metal_button.html")
                }
            },
            update = { webView ->
                webView.evaluateJavascript("if (window.setButtonLabel) { window.setButtonLabel('$text'); }", null)
            }
        )
    }
}
