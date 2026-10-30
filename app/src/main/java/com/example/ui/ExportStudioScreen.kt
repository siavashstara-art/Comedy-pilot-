package com.example.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.theme.*

@Composable
fun ExportStudioScreen(
    videoPath: String = "",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedPlatform by remember { mutableStateOf("Instagram / Telegram (9:16)") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkCharcoalBg)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎬 استودیو خروجی و اشتراک‌گذاری ComedyPilot",
            style = MaterialTheme.typography.titleLarge,
            color = JoyYellow
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        // انتخاب پلتفرم هدف
        PlatformButton(
            title = "📱 اینستاگرام / تلگرام (استوری و ریلز ۹:۱۶)",
            isSelected = selectedPlatform == "Instagram / Telegram (9:16)"
        ) {
            selectedPlatform = "Instagram / Telegram (9:16)"
        }

        Spacer(modifier = Modifier.height(12.dp))

        PlatformButton(
            title = "📺 آپارات / یوتیوب (ویدیوی افقی/عمودی استاندارد)",
            isSelected = selectedPlatform == "Aparat / YouTube"
        ) {
            selectedPlatform = "Aparat / YouTube"
        }

        Spacer(modifier = Modifier.height(32.dp))

        // دکمهی نهایی شلیک به پلتفرم‌های منتخب
        Button(
            onClick = {
                shareVideoToPlatform(context, videoPath, selectedPlatform)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = JoyYellow, contentColor = Color.Black)
        ) {
            Text(
                text = "🚀 ساخت و اشتراک‌گذاری سریع با واترمارک Made with ComedyPilot 🎭",
                color = Color.Black,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PlatformButton(title: String, isSelected: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) CardSurfaceVariant else CardDarkBg
        )
    ) {
        Text(text = title, color = Color.White)
    }
}

// تابع امن و سبک اندروید برای ارسال ویدیو بدون نیاز به کتابخانه‌های سنگین
fun shareVideoToPlatform(context: Context, videoPath: String, platform: String) {
    val fileUri = if (videoPath.isNotBlank()) videoPath.toUri() else "android.resource://com.example/drawable/ic_launcher_background".toUri()
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "video/*"
        putExtra(Intent.EXTRA_STREAM, fileUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    
    val chooser = Intent.createChooser(intent, "ارسال خروجی ComedyPilot به $platform")
    context.startActivity(chooser)
}
