package com.example.ui

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.CardDarkBg
import com.example.ui.theme.DarkCharcoalBg
import com.example.ui.theme.JoyYellow

class WebAppInterface(private val context: Context) {
    @JavascriptInterface
    fun downloadVideo(url: String, fileName: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setTitle("$fileName.mp4")
                setDescription("دانلود کلیپ نوستالژی از کلود مدیا هاب توانا سیتی")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "$fileName.mp4")
            }
            val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)
            Toast.makeText(context, "دانلود کلیپ «$fileName» آغاز شد 📥 در پوشه Downloads ذخیره می‌شود", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(browserIntent)
            Toast.makeText(context, "در حال باز کردن در مرورگر برای دانلود مستقیم...", Toast.LENGTH_SHORT).show()
        }
    }

    @JavascriptInterface
    fun shareVideo(title: String, url: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "$title\nدانلود ویدیو:\n$url\nاکوسیستم توانا سیتی")
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "اشتراک کلیپ در روبیکا، ایتا یا اینستاگرام"))
    }
}

@Composable
fun CloudMediaHubView(
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCharcoalBg)
    ) {
        // Top Action Bar
        Surface(
            color = CardDarkBg,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "بازگشت", tint = JoyYellow)
                        }
                    }
                    Text(
                        text = "کلود مدیا هاب (ویدیوهای نوستالژی) 🎞️",
                        color = JoyYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Row {
                    IconButton(onClick = { webViewInstance?.reload() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "تازه سازی", tint = Color.LightGray)
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.mediaPlaybackRequiresUserGesture = false
                        settings.allowFileAccess = true
                        settings.cacheMode = WebSettings.LOAD_DEFAULT
                        
                        webChromeClient = WebChromeClient()
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                            }
                        }

                        addJavascriptInterface(WebAppInterface(ctx), "AndroidBridge")
                        loadUrl("file:///android_asset/cloud_media_hub.html")
                        webViewInstance = this
                    }
                }
            )

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = JoyYellow
                )
            }
        }
    }
}
