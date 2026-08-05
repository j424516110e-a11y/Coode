package com.example.ui.components

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DesktopMac
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.IdeAccentRun
import com.example.ui.theme.IdePrimarySky

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PreviewPaneView(
    htmlBundleContent: String,
    onRefresh: () -> Unit,
    onConsoleLog: (String) -> Unit
) {
    var isMobileFrame by remember { mutableStateOf(false) }
    var key by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Compact Top Toolbar for Live Preview
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(IdeAccentRun)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "معاينة مباشرة",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Mobile vs Desktop view toggle
                    IconButton(
                        onClick = { isMobileFrame = !isMobileFrame },
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("preview_device_toggle")
                    ) {
                        Icon(
                            imageVector = if (isMobileFrame) Icons.Default.PhoneAndroid else Icons.Default.DesktopMac,
                            contentDescription = "تبديل الشاشة",
                            tint = IdePrimarySky,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Refresh Button
                    IconButton(
                        onClick = {
                            key++
                            onRefresh()
                        },
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("preview_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "تحديث المعاينة",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }

        // Preview Render Canvas
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(if (isMobileFrame) 8.dp else 0.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .then(
                        if (isMobileFrame) Modifier
                            .widthIn(max = 320.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(12.dp)
                            )
                        else Modifier.fillMaxWidth()
                    )
                    .background(Color.White)
            ) {
                key(key) {
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.allowFileAccess = true
                                settings.allowContentAccess = true

                                addJavascriptInterface(object {
                                    @JavascriptInterface
                                    fun postMessage(json: String) {
                                        onConsoleLog(json)
                                    }
                                }, "AndroidConsole")

                                webViewClient = WebViewClient()
                                loadDataWithBaseURL(
                                    "https://appassets.androidpreview.local/",
                                    htmlBundleContent,
                                    "text/html",
                                    "UTF-8",
                                    null
                                )
                            }
                        },
                        update = { webView ->
                            webView.loadDataWithBaseURL(
                                "https://appassets.androidpreview.local/",
                                htmlBundleContent,
                                "text/html",
                                "UTF-8",
                                null
                            )
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
