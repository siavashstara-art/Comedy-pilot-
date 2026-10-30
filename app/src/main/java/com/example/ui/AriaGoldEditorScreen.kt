package com.example.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun AriaGoldEditorScreen(
    projectType: String = "Comedy Reel",
    onBackClick: () -> Unit = {}
) {
    var textOverlay by remember { mutableStateOf("متن تبلیغاتی ComedyPilot") }
    var durationSeconds by remember { mutableFloatStateOf(15f) }
    var selectedMediaUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedMediaUri = uri
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCharcoalBg)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBackClick) {
                Text("بازگشت", color = Color(0xFFE0E0E0))
            }
            
            Text(
                text = "ComedyPilot Studio",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = JoyYellow
                )
            )

            Button(
                onClick = { /* رندر نهایی خروجی */ },
                colors = ButtonDefaults.buttonColors(containerColor = JoyYellow, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("خروجی", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Preview Canvas 9:16
        Box(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(9f / 16f)
                .clip(RoundedCornerShape(16.dp))
                .background(CardDarkBg)
                .border(2.dp, JoyYellow.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (selectedMediaUri != null) {
                Text(
                    text = "رسانه انتخاب شد ✅",
                    color = JoyYellow,
                    fontSize = 14.sp
                )
            } else {
                Text(
                    text = "برای افزودن تصویر/ویدیو کلیک کنید",
                    color = Color(0xFFE0E0E0).copy(alpha = 0.5f),
                    fontSize = 13.sp
                )
            }

            // Watermark Branding & Canvas Text
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = textOverlay,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ساخته‌شده با ComedyPilot 🎭",
                    color = JoyYellow,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Gallery Picker Button
        OutlinedButton(
            onClick = { galleryLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = JoyYellow),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("انتخاب عکس پس‌زمینه از گالری")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Settings Panel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardDarkBg)
                .padding(14.dp)
        ) {
            Text(
                text = "زمان تیزر: ${durationSeconds.toInt()} ثانیه",
                color = Color(0xFFE0E0E0),
                fontSize = 13.sp
            )

            Slider(
                value = durationSeconds,
                onValueChange = { durationSeconds = it },
                valueRange = 5f..60f,
                colors = SliderDefaults.colors(
                    thumbColor = JoyYellow,
                    activeTrackColor = JoyYellow,
                    inactiveTrackColor = Color(0xFF333336)
                )
            )

            OutlinedTextField(
                value = textOverlay,
                onValueChange = { textOverlay = it },
                label = { Text("متن تبلیغاتی", color = JoyYellow) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = JoyYellow,
                    unfocusedBorderColor = Color(0xFF333336),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
