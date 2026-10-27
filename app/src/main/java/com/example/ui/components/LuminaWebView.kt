package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.WebTab
import com.example.ui.viewmodel.WebViewAction

private const val DESKTOP_USER_AGENT =
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LuminaWebView(
    tab: WebTab,
    modifier: Modifier = Modifier,
    textZoom: Int = 100,
    actionTrigger: WebViewAction? = null,
    onConsumeAction: () -> Unit = {},
    onUpdateTab: (title: String?, url: String?, canGoBack: Boolean?, canGoForward: Boolean?, isLoading: Boolean?, progress: Int?) -> Unit,
    onExtractPageText: (String) -> Unit,
    onDownloadRequested: (url: String, fileName: String, mimeType: String, length: Long) -> Unit = { _, _, _, _ -> },
    onFindResult: (activeMatchOrdinal: Int, numberOfMatches: Int) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current

    val webView = remember(tab.id) {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = !tab.isIncognito
                databaseEnabled = !tab.isIncognito
                useWideViewPort = true
                loadWithOverviewMode = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                cacheMode = if (tab.isIncognito) WebSettings.LOAD_NO_CACHE else WebSettings.LOAD_DEFAULT
            }

            setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
                val guessName = URLUtil.guessFileName(url, contentDisposition, mimetype)
                onDownloadRequested(url, guessName, mimetype ?: "", contentLength)
            }

            setFindListener { activeMatchOrdinal, numberOfMatches, isDoneCounting ->
                onFindResult(activeMatchOrdinal, numberOfMatches)
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    val isLoading = newProgress < 100
                    onUpdateTab(null, null, view?.canGoBack(), view?.canGoForward(), isLoading, newProgress)

                    if (newProgress == 100) {
                        view?.evaluateJavascript(
                            "(function() { return (document.body ? document.body.innerText : ''); })();"
                        ) { rawText ->
                            val cleanText = unescapeJsString(rawText)
                            onExtractPageText(cleanText)
                        }
                    }
                }

                override fun onReceivedTitle(view: WebView?, title: String?) {
                    if (!title.isNullOrBlank()) {
                        onUpdateTab(title, view?.url, view?.canGoBack(), view?.canGoForward(), null, null)
                    }
                }
            }

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    onUpdateTab(null, url, view?.canGoBack(), view?.canGoForward(), true, 10)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    onUpdateTab(view?.title, url, view?.canGoBack(), view?.canGoForward(), false, 100)
                    view?.evaluateJavascript(
                        "(function() { return (document.body ? document.body.innerText : ''); })();"
                    ) { rawText ->
                        val cleanText = unescapeJsString(rawText)
                        onExtractPageText(cleanText)
                    }
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    if (request?.isForMainFrame == true) {
                        onUpdateTab(null, null, view?.canGoBack(), view?.canGoForward(), false, 0)
                    }
                }
            }
        }
    }

    // Apply text zoom
    LaunchedEffect(textZoom) {
        webView.settings.textZoom = textZoom
    }

    // Apply Desktop User Agent if toggled
    LaunchedEffect(tab.isDesktopMode) {
        if (tab.isDesktopMode) {
            webView.settings.userAgentString = DESKTOP_USER_AGENT
            webView.settings.useWideViewPort = true
            webView.settings.loadWithOverviewMode = true
        } else {
            webView.settings.userAgentString = null
        }
    }

    // Handle WebViewActions
    LaunchedEffect(actionTrigger) {
        when (actionTrigger) {
            is WebViewAction.GoBack -> {
                if (webView.canGoBack()) webView.goBack()
                onConsumeAction()
            }
            is WebViewAction.GoForward -> {
                if (webView.canGoForward()) webView.goForward()
                onConsumeAction()
            }
            is WebViewAction.Reload -> {
                webView.reload()
                onConsumeAction()
            }
            is WebViewAction.LoadUrl -> {
                webView.loadUrl(actionTrigger.url)
                onConsumeAction()
            }
            is WebViewAction.FindQuery -> {
                if (actionTrigger.query.isNotBlank()) {
                    webView.findAllAsync(actionTrigger.query)
                } else {
                    webView.clearMatches()
                }
                onConsumeAction()
            }
            is WebViewAction.FindNext -> {
                webView.findNext(actionTrigger.forward)
                onConsumeAction()
            }
            is WebViewAction.ClearFindMatches -> {
                webView.clearMatches()
                onConsumeAction()
            }
            is WebViewAction.ClearAllWebCache -> {
                webView.clearCache(true)
                webView.clearHistory()
                CookieManager.getInstance().removeAllCookies(null)
                onConsumeAction()
            }
            null -> {}
        }
    }

    // Initial load if tab URL is set and not loaded yet
    LaunchedEffect(tab.url) {
        if (tab.url != "lumina://newtab" && webView.url != tab.url) {
            webView.loadUrl(tab.url)
        }
    }

    // Back handling for web history
    BackHandler(enabled = tab.canGoBack) {
        if (webView.canGoBack()) {
            webView.goBack()
        }
    }

    DisposableEffect(tab.id) {
        onDispose {
            if (tab.isIncognito) {
                webView.clearCache(true)
                webView.clearHistory()
            }
            webView.stopLoading()
        }
    }

    AndroidView(
        factory = { webView },
        modifier = modifier.fillMaxSize()
    )
}

private fun unescapeJsString(raw: String?): String {
    if (raw == null || raw == "null") return ""
    var str = raw.trim()
    if (str.startsWith("\"") && str.endsWith("\"") && str.length >= 2) {
        str = str.substring(1, str.length - 1)
    }
    return str
        .replace("\\n", "\n")
        .replace("\\r", "")
        .replace("\\t", " ")
        .replace("\\\"", "\"")
        .replace("\\\\", "\\")
        .replace(Regex("\\s+"), " ")
        .trim()
}
