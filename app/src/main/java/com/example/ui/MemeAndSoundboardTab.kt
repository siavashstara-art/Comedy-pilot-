package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.audio.SoundEffectsEngine
import com.example.audio.TextToSpeechEngine
import com.example.ui.theme.*

@Composable
fun MemeAndSoundboardTab(
    soundEngine: SoundEffectsEngine,
    ttsEngine: TextToSpeechEngine,
    customSoundList: List<String> = emptyList()
) {
    val context = LocalContext.current
    var subTabState by remember { mutableStateOf(0) } // 0: Soundboard, 1: Meme Studio, 2: Gallery

    var selectedCategory by remember { mutableStateOf("سینمای اصیل و قیصر 🎬") }
    val categories = listOf(
        "سینمای اصیل و قیصر 🎬",
        "لاله‌زار و کلاسیک 🎩", 
        "نوستالژی تلویزیون 📺", 
        "کارتون‌ها و خاطرات کودکی 🧸",
        "جوکر و طنز مدرن 🃏", 
        "طنز اینترنتی و وایرال 🚀", 
        "بین‌الملل و تاکشو 🌍"
    )

    var searchQuery by remember { mutableStateOf("") }
    var activePlayingTitle by remember { mutableStateOf<String?>(null) }

    val currentItems = when (selectedCategory) {
        "سینمای اصیل و قیصر 🎬" -> listOf(
            "قیصر! داش رو کشتن! 🔪" to JoyYellow,
            "بهمن مفید (ما بودیم...) 🧔‍♂️" to JoyOrange,
            "حبیب آقا (چاقو از پشت!) 🗡️" to JoyPink,
            "سوته‌دلان (مجید تو چرا؟) 🤦‍♂️" to JoyPurple,
            "«ما خودمون مؤلفِ این سبک‌هاییم!» 😎" to JoyYellow,
            "«دنیا دو روزه، بخور!» 🍲" to JoyGreen
        )
        "لاله‌زار و کلاسیک 🎩" -> listOf(
            "مرحوم میری (رقص و متلک) 🕺" to JoyYellow,
            "مرحوم ظهوری (چشم و ابرو) 👀" to JoyOrange,
            "محمدعلی فردین (شیرین‌کاری) 🎭" to JoyPink,
            "آرام‌سپهر و طنز قدیمی 🎬" to JoyPurple
        )
        "نوستالژی تلویزیون 📺" -> listOf(
            "دشواری نداره! 🧔‍♂️" to JoyYellow,
            "بخور، همش گوشته، همش سوسیسه! 🌭" to JoyOrange,
            "ننه من گشنمه 🍲" to JoyPink,
            "مربا بده بابا 🍓" to JoyGreen,
            "گردِ نخود برره 🥴" to JoyPurple
        )
        "کارتون‌ها و خاطرات کودکی 🧸" -> listOf(
            "پت و مت (هورا! هورا!) 🎉" to JoyYellow,
            "خرابکاری لوله‌کشی (پت و مت) 🚿" to JoyOrange,
            "بریدن صندلی زیر پا (پت و مت) 🪚" to JoyPink,
            "خانم کوچولو و صداهای بانمک 🍼" to JoyPurple,
            "ریتم شاد کارتون‌های قدیمی (هی را را) 🎶" to JoyGreen
        )
        "جوکر و طنز مدرن 🃏" -> listOf(
            "پلیس‌راه باید بیای! 🚔" to JoyYellow,
            "گاوِ فینال جوکر 🐮" to JoyGreen,
            "زلزله‌ی نقطه‌چین 📳" to JoyOrange,
            "کل‌کل با ایرج طهماسب 🎤" to JoyPink
        )
        "طنز اینترنتی و وایرال 🚀" -> listOf(
            "دوربین مخفی‌های خلاقانه و سالم 📷" to JoyYellow,
            "محمد بوقی و بوق‌های خیابانی 📯" to JoyOrange,
            "پرَنک ربات ترسناک در خیابان 🤖" to JoyPink,
            "صدای تصادف، کرش و جیغِ ناگهانی 💥" to JoyPurple,
            "شوک‌های ناگهانی و بامزه (خانوادگی) 🤪" to JoyGreen,
            "ری‌اکشن تیک‌تاکی معروف 🔥" to JoyYellow
        )
        else -> listOf(
            "توشیرو میفونه و هاراکیری (آفتاب سرخ) ⚔️" to JoyYellow,
            "کل‌کل میفونه و چارلز برانسون 🤠" to JoyOrange,
            "کلینت ایستوود (نگاه و سیگار وسترن) 🚬" to JoyPink,
            "موزیک سوت تعجب وسترن (خوب بد زشت) 🎶" to JoyPurple,
            "رقص عجیب تاکشو 🕺" to JoyGreen,
            "خنده‌های اسبی مجری 🐴" to JoyYellow
        )
    }

    val filteredItems = currentItems.filter { (title, _) ->
        searchQuery.isBlank() || title.contains(searchQuery)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Sub-Tabs Selector
        ScrollableTabRow(
            selectedTabIndex = subTabState,
            containerColor = CardDarkBg,
            contentColor = JoyYellow,
            edgePadding = 8.dp,
            divider = {}
        ) {
            Tab(
                selected = subTabState == 0,
                onClick = { subTabState = 0 },
                text = { Text("🎬 موزه ملی صداها و دیالوگ‌ها", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = subTabState == 1,
                onClick = { subTabState = 1 },
                text = { Text("🎨 میم‌ساز (Meme Studio)", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = subTabState == 2,
                onClick = { subTabState = 2 },
                text = { Text("🖼️ گالری میم‌ها (Gallery)", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = subTabState == 3,
                onClick = { subTabState = 3 },
                text = { Text("🎞️ کلود مدیا هاب (ویدیوها)", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }

        when (subTabState) {
            0 -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    Text(
                        text = "🎬 موزهی ملی صداها و دیالوگ‌های طنز ماندگار",
                        color = JoyPink,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Search Input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("جستجوی صدای نوستالژی یا سینمایی...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = JoyYellow) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JoyYellow,
                            unfocusedBorderColor = Color.Gray,
                            focusedContainerColor = CardDarkBg,
                            unfocusedContainerColor = CardDarkBg
                        )
                    )

                    // Categories Horizontal Scroll Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category },
                                label = { Text(category, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = JoyYellow,
                                    selectedLabelColor = Color.Black,
                                    containerColor = CardDarkBg,
                                    labelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Active Character Dialogue & Image Showcase Banner
                    activePlayingTitle?.let { playingTitle ->
                        val heroImg = getImageForTitle(playingTitle)
                        val dialogueText = getSpeechTextForTitle(playingTitle)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = CardDarkBg),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(2.dp, JoyYellow)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (heroImg != null) {
                                    Image(
                                        painter = painterResource(id = heroImg),
                                        contentDescription = playingTitle,
                                        modifier = Modifier
                                            .size(70.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(1.dp, JoyYellow, RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = JoyPink, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "در حال پخش: $playingTitle",
                                            color = JoyYellow,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = dialogueText,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }

                    // Sound Cards Grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredItems) { (title, color) ->
                            val cardImage = getImageForTitle(title)
                            SoundCard(
                                title = title,
                                accentColor = color,
                                isPlaying = activePlayingTitle == title,
                                imageResId = cardImage,
                                onClick = {
                                    activePlayingTitle = title
                                    val dialogueText = getSpeechTextForTitle(title)
                                    val pitch = getPitchForTitle(title)
                                    val speechRate = getSpeechRateForTitle(title)

                                    // 1. Play matching acoustic sound effect immediately
                                    val effectId = mapTitleToEffectId(title)
                                    soundEngine.playSoundEffect(effectId)

                                    // 2. Speak dialogue if TTS available
                                    ttsEngine.speak(dialogueText, pitch = pitch, speechRate = speechRate)

                                    // 3. Show instant feedback toast
                                    Toast.makeText(context, "🗣️ $dialogueText", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
            1 -> {
                MemeMakerView(
                    onMemeSaved = { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                )
            }
            2 -> {
                // Meme Gallery View
                LazyVerticalGrid(
                    columns = GridCells.Fixed(1),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CardDarkBg),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Image(
                                    painter = painterResource(id = R.drawable.surprised_cat_meme_1793285772146),
                                    contentDescription = "Meme 1",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("وقتی بعد ۳ ماه رفتی بانک و میگن کارتت منقضی شده! 🐱", color = JoyYellow, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("❤️ ۱,۴۲۰ لایک | 💬 ۳۴۰ نظر", color = Color.Gray, fontSize = 11.sp)
                                    IconButton(onClick = { Toast.makeText(context, "اشتراک‌گذاری میم 🚀", Toast.LENGTH_SHORT).show() }) {
                                        Icon(Icons.Default.Share, contentDescription = null, tint = JoyPink)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CardDarkBg),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Image(
                                    painter = painterResource(id = R.drawable.persian_tea_meme_1793285783738),
                                    contentDescription = "Meme 2",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("وقتی یکی میگه من چای نمی‌خورم! ☕", color = JoyPink, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("❤️ ۲,۸۹۰ لایک | 💬 ۶۵۰ نظر", color = Color.Gray, fontSize = 11.sp)
                                    IconButton(onClick = { Toast.makeText(context, "اشتراک‌گذاری میم 🚀", Toast.LENGTH_SHORT).show() }) {
                                        Icon(Icons.Default.Share, contentDescription = null, tint = JoyPink)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            3 -> {
                CloudMediaHubView()
            }
        }
    }
}

@Composable
fun SoundCard(
    title: String,
    accentColor: Color,
    isPlaying: Boolean = false,
    imageResId: Int? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (imageResId != null) 140.dp else 90.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) accentColor.copy(alpha = 0.35f) else CardDarkBg
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isPlaying) 2.dp else 1.dp,
            color = if (isPlaying) accentColor else Color.Gray.copy(alpha = 0.35f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (imageResId != null) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Dark bottom gradient for legible text
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0x99000000), Color(0xF5101014))
                            )
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = if (imageResId != null) Arrangement.Bottom else Arrangement.Center
            ) {
                Text(
                    text = title,
                    color = if (isPlaying) accentColor else Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
                if (isPlaying) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.GraphicEq,
                            contentDescription = "در حال پخش",
                            tint = accentColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("در حال پخش", color = accentColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (imageResId != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("🔊 لمس برای پخش", color = JoyYellow, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

private fun getSpeechTextForTitle(title: String): String {
    return when {
        title.contains("قیصر") -> "قیصر! داداش فرمون رو کشتن!"
        title.contains("بهمن مفید") -> "ما بودیم، آره و اینا خیلی بودیم!"
        title.contains("حبیب آقا") -> "حبیب آقا! نامردا از پشت با خنجر زدن!"
        title.contains("سوته‌دلان") -> "مجید تو چرا؟! همه رفتن، ما موندیم و این تنهایی!"
        title.contains("مؤلف") -> "ما خودمون مؤلف این سبک‌هاییم، شوخی نکن!"
        title.contains("دنیا دو روزه") -> "دنیا دو روزه، بخور غصه نخور برادر!"
        title.contains("میری") -> "قربون اون قد و بالات! متلک ننداز به ما!"
        title.contains("ظهوری") -> "چشم و ابرو نیا شیطون، کار دستمون میدی!"
        title.contains("فردین") -> "صفای وجودت داداش، مرامتو عشقه!"
        title.contains("آرام‌سپهر") -> "این همه خنده و شادی برای شما مردم گل!"
        title.contains("دشواری") -> "دشواری نداره! اینجا هیچ دشواری نداریم ما!"
        title.contains("سوسیس") -> "بخور! همش گوشته، همش سوسیسه!"
        title.contains("ننه من گشنمه") -> "ننه من گشنمه! ناهار چی داریم تو این خونه؟"
        title.contains("مربا بده بابا") -> "مربا بده بابا! صبحونه بدون مربا که صفا نداره!"
        title.contains("گردِ نخود") -> "گرد نخود برره رو ریختن، کلاً گیج و خندان شدیم!"
        title.contains("پت و مت (هورا") -> "هورا! هورا! با پت و مت درستش کردیم!"
        title.contains("لوله‌کشی") -> "لوله ترکید، کل خونه رفت زیر آب!"
        title.contains("صندلی") -> "صندلی رو از زیر پا بریدیم، محکم افتادیم زمین!"
        title.contains("خانم کوچولو") -> "سلام به همه بچه‌های گل و خنده‌رو!"
        title.contains("ریتم شاد کارتون") -> "هی را را! شادی و خاطرات شیرین کودکی!"
        title.contains("پلیس‌راه") -> "پلیس راه باید بیای! مدارک خودرو رو نشون بده!"
        title.contains("گاوِ فینال") -> "گاو فینال جوکر اومد وسط استودیو!"
        title.contains("زلزله") -> "زلزله‌ی نقطه‌چین اومد، فرار کنید!"
        title.contains("طهماسب") -> "آقای طهماسب، ما خیلی دوستت داریم ولی مهمونیه دیگه!"
        title.contains("دوربین مخفی") -> "دوربین مخفیه! به دوربین دست تکون بده، داری پخش میشی!"
        title.contains("محمد بوقی") -> "بوق بوق! ایران ایران قهرمان!"
        title.contains("ربات") -> "ربات هوشمند وارد خیابان شد، همه ترسیدند!"
        title.contains("تصادف") -> "وای تصادف شد! صدای جیغ و آژیر خطر!"
        title.contains("شوک‌های ناگهانی") -> "شوخی کردم! هول نشو بابا، دوربین مخفی بود!"
        title.contains("تیک‌تاک") -> "عجب ری‌اکشنی! توی فضای مجازی وایرال شد رفت!"
        title.contains("میفونه") -> "سامورایی با شمشیر وارد میدان نبرد شد!"
        title.contains("برانسون") -> "غرب وحشی با سامورایی طرف شده، تسلیم شو!"
        title.contains("ایستوود") -> "تو این دنیا دو جور آدم هست، اونایی که تفنگ پر دارن..."
        title.contains("سوت") -> "سوت خوب، بد، زشت در صحرای بی‌انتها!"
        title.contains("تاکشو") -> "حرکات موزون کمدی در استودیو تاکشو!"
        title.contains("خنده‌های اسبی") -> "هاهاهاها! خنده‌های قهقه‌دار و پرانرژی مجری!"
        else -> title.replace(Regex("[^a-zA-Z0-9آ-ی‌ ]"), "").trim()
    }
}

private fun getPitchForTitle(title: String): Float {
    return when {
        title.contains("قیصر") || title.contains("بهمن مفید") || title.contains("ایستوود") || title.contains("میفونه") -> 0.75f
        title.contains("ننه من گشنمه") || title.contains("مربا بده بابا") || title.contains("خانم کوچولو") -> 1.4f
        title.contains("پت و مت") || title.contains("میری") -> 1.35f
        else -> 1.0f
    }
}

private fun getSpeechRateForTitle(title: String): Float {
    return when {
        title.contains("پت و مت") || title.contains("میری") -> 1.15f
        title.contains("قیصر") || title.contains("ایستوود") -> 0.85f
        else -> 1.0f
    }
}

private fun getImageForTitle(title: String): Int? {
    return when {
        title.contains("بهمن مفید") -> R.drawable.img_bahman_mofid_1793375044552
        title.contains("قیصر") -> R.drawable.img_gheysar_cinematic_1793375060610
        title.contains("پت و مت") -> R.drawable.img_pat_mat_nostalgia_1793375072772
        else -> null
    }
}

private fun mapTitleToEffectId(title: String): String {
    return when {
        title.contains("بهمن مفید") -> "bahman_mofid"
        title.contains("قیصر") -> "gheysar"
        title.contains("پت و مت") -> "pat_mat"
        title.contains("دشواری") -> "deshvari"
        title.contains("سوسیس") -> "soosis"
        title.contains("ننه من گشنمه") -> "naneh_goshname"
        title.contains("مربا بده بابا") -> "moraba"
        title.contains("حبیب آقا") || title.contains("زلزله") -> "dramatic_horn"
        title.contains("طهماسب") || title.contains("خنده‌های اسبی") || title.contains("گاو") -> "laugh_track"
        title.contains("صندلی") || title.contains("ظهوری") -> "boing"
        title.contains("پلیس‌راه") -> "wrong_buzzer"
        title.contains("گردِ نخود") || title.contains("لوله‌کشی") -> "fart"
        title.contains("سوته‌دلان") || title.contains("ایستوود") || title.contains("سوت") -> "sad_violin"
        title.contains("تصادف") -> "rimshot"
        title.contains("محمد بوقی") || title.contains("دوربین مخفی") || title.contains("تاکشو") -> "cheer"
        title.contains("مؤلف") || title.contains("فردین") || title.contains("خانم کوچولو") -> "win_chime"
        else -> "cheetori"
    }
}
