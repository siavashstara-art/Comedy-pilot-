package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.audio.SoundEffectsEngine
import com.example.audio.TextToSpeechEngine
import com.example.model.VoiceEffectItem
import com.example.ui.theme.*

@Composable
fun VoiceChangerTab(
    soundEngine: SoundEffectsEngine,
    ttsEngine: TextToSpeechEngine,
    onSaveCustomSound: (String) -> Unit
) {
    val context = LocalContext.current
    val isRecording by soundEngine.isRecording.collectAsState()
    val recordedAudioBuffer by soundEngine.recordedAudioBuffer.collectAsState()
    val waveformLevels by soundEngine.audioWaveformLevels.collectAsState()

    // Runtime permission launcher for microphone
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            soundEngine.startRecordingMicrophone()
            Toast.makeText(context, "🎤 در حال ضبط صدا... لطفاً صحبت کنید!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "⚠️ برای ضبط و تغییر صدا، دسترسی میکروفون لازم است.", Toast.LENGTH_LONG).show()
        }
    }

    val effects = listOf(
        VoiceEffectItem("chipmunk", "سنجاب خنده‌دار", "Chipmunk", "🐿️", "صدای زیر و نازک و تند", 1.8f, 1.3f, accentColor = JoyYellow),
        VoiceEffectItem("monster", "غول و هیولا", "Monster", "👹", "صدای بم و ترسناک و سنگین", 0.6f, 0.8f, accentColor = JoyOrange),
        VoiceEffectItem("robot", "ربات فضایی", "Robot", "🤖", "افکت رباتیک فلزی", 1.0f, 1.0f, isRobotEffect = true, accentColor = JoyGreen),
        VoiceEffectItem("helium", "بادکنک هلیوم", "Helium", "🎈", "جیغ‌مانند و بادکنکی", 2.2f, 1.1f, accentColor = JoyPink),
        VoiceEffectItem("megaphone", "مگافون / بلندگو", "Megaphone", "📢", "صدای خیابانی و اکو", 1.2f, 1.0f, accentColor = JoyCyan),
        VoiceEffectItem("echo", "انعکاس غار", "Echo", "🌊", "افکت پژواک و اکو", 1.0f, 0.9f, accentColor = JoyPurple),
        VoiceEffectItem("slow", "اسلو موشن", "Slow Mo", "🐢", "کُند و آهسته خنده‌دار", 0.7f, 0.6f, accentColor = JoyYellow),
        VoiceEffectItem("fast", "دور تند", "Fast", "🚀", "شتاب‌زده و عجولانه", 1.5f, 1.6f, accentColor = JoyPink)
    )

    var selectedEffect by remember { mutableStateOf(effects[0]) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = "🎙️ ماژول تغییر صدای پیشرفته",
            color = JoyGreen,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )
        Text(
            text = "صدای خود را ضبط کنید یا روی افکت‌ها بزنید تا صدای نمونه را بشنوید!",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            textAlign = TextAlign.Start
        )

        // Recording Visualizer Panel
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp),
            colors = CardDefaults.cardColors(containerColor = CardDarkBg),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(
                width = if (isRecording) 2.dp else 1.dp,
                color = if (isRecording) JoyPink else JoyGreen.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isRecording) "🔴 در حال ضبط صدا... بگو: سلام چطوری!" else if (recordedAudioBuffer != null) "✅ صدای شما با موفقیت ضبط شد!" else "برای ضبط صدای خود، دکمه میکروفون را لمس کنید",
                    color = if (isRecording) JoyPink else Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                // Animated Waveform Bar Visualizer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val displayLevels = if (waveformLevels.isNotEmpty()) waveformLevels else listOf(0.2f, 0.4f, 0.8f, 0.3f, 0.6f, 0.9f, 0.5f, 0.2f, 0.7f, 0.3f)
                    displayLevels.forEach { lvl ->
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .fillMaxHeight(fraction = if (isRecording) lvl else 0.3f)
                                .clip(CircleShape)
                                .background(if (isRecording) JoyPink else JoyGreen)
                        )
                    }
                }

                // Record Mic Button Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if (isRecording) {
                                soundEngine.stopRecordingMicrophone()
                                Toast.makeText(context, "✅ ضبط متوقف شد! حالا افکت‌ها را انتخاب کنید.", Toast.LENGTH_SHORT).show()
                            } else {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasPermission) {
                                    soundEngine.startRecordingMicrophone()
                                    Toast.makeText(context, "🎤 در حال ضبط صدا... صحبت کنید!", Toast.LENGTH_SHORT).show()
                                } else {
                                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRecording) JoyPink else JoyGreen,
                            contentColor = Color.Black
                        ),
                        shape = CircleShape,
                        modifier = Modifier.size(54.dp)
                    ) {
                        Icon(
                            if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = "ضبط صدا",
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Play converted audio button
                    if (recordedAudioBuffer != null && !isRecording) {
                        Button(
                            onClick = {
                                soundEngine.playRecordedAudioWithEffect(
                                    selectedEffect.pitchMultiplier,
                                    selectedEffect.speedMultiplier,
                                    selectedEffect.isRobotEffect
                                )
                                Toast.makeText(context, "▶️ پخش صدای شما با افکت ${selectedEffect.nameFa}", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = JoyYellow, contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = "پخش")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("پخش صدای شما: ${selectedEffect.nameFa}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Effects Grid Selector Header
        Text(
            text = "انتخاب افکت صوتی خنده‌دار (لمس کنید تا بشنوید):",
            color = JoyYellow,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .weight(1f)
                .padding(top = 8.dp)
        ) {
            items(effects) { fx ->
                val isSel = fx.id == selectedEffect.id
                Card(
                    onClick = {
                        selectedEffect = fx
                        if (recordedAudioBuffer != null) {
                            // Play user's actual voice with this effect!
                            soundEngine.playRecordedAudioWithEffect(
                                fx.pitchMultiplier,
                                fx.speedMultiplier,
                                fx.isRobotEffect
                            )
                            Toast.makeText(context, "🎤 صدای شما با افکت ${fx.nameFa}!", Toast.LENGTH_SHORT).show()
                        } else {
                            // No recording yet: Speak funny sample dialogue demonstrating the character effect!
                            when (fx.id) {
                                "chipmunk" -> ttsEngine.speak("سلام! من سنجاب شیطون کمدی پایلوتم!", pitch = 1.8f, speechRate = 1.3f)
                                "monster" -> ttsEngine.speak("من غول بزرگ و خنده‌دارم!", pitch = 0.6f, speechRate = 0.85f)
                                "robot" -> ttsEngine.speak("سلام کاربر عزیز، من ربات هوشمندم!", pitch = 1.0f, speechRate = 1.0f)
                                "helium" -> ttsEngine.speak("صدام شبیه بادکنک هلیومی شده!", pitch = 2.1f, speechRate = 1.15f)
                                "megaphone" -> ttsEngine.speak("توجه فرمایید! برنامه کمدی پایلوت در حال اجراست!", pitch = 1.2f, speechRate = 1.0f)
                                "echo" -> ttsEngine.speak("پژواک صدا در غار... سلام... سلام...", pitch = 0.95f, speechRate = 0.9f)
                                "slow" -> ttsEngine.speak("چــــطـــــوری ایــــــرانـــــی؟", pitch = 0.7f, speechRate = 0.6f)
                                "fast" -> ttsEngine.speak("سریع بدو که قطار خنده رفت!", pitch = 1.4f, speechRate = 1.5f)
                                else -> ttsEngine.speak("افکت صدای ${fx.nameFa} فعال شد!", pitch = fx.pitchMultiplier, speechRate = fx.speedMultiplier)
                            }
                            soundEngine.playSoundEffect(if (fx.id == "monster") "dramatic_horn" else if (fx.id == "chipmunk") "boing" else "cheetori")
                            Toast.makeText(context, "🗣️ نمونه ${fx.nameFa} | برای تغییر صدای خودتان، دکمه میکروفون بالا را بزنید!", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSel) fx.accentColor.copy(alpha = 0.25f) else CardDarkBg
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSel) 2.dp else 1.dp,
                        color = if (isSel) fx.accentColor else Color.Gray.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(fx.emoji, fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = fx.nameFa,
                                color = if (isSel) fx.accentColor else Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = fx.descriptionFa,
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // Save to My Soundboard Button
        if (recordedAudioBuffer != null) {
            Button(
                onClick = {
                    onSaveCustomSound("صدای ${selectedEffect.nameFa}")
                    Toast.makeText(context, "✅ صدای تغییر یافته به تخته صدای شما اضافه شد!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = JoyPink, contentColor = Color.White),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Bookmark, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ذخیره در موزه صدا / دابسمش", fontWeight = FontWeight.Bold)
            }
        }
    }
}
