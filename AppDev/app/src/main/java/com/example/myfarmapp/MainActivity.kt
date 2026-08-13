package com.example.myfarmapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var tts: TextToSpeech

    // State สำหรับควบคุม UI
    private var isTargetOn by mutableStateOf(false)
    private var resultText by mutableStateOf("กดปุ่มด้านล่างแล้วพูดคำสั่ง...")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // เริ่มต้นระบบ TTS และ Permission
        tts = TextToSpeech(this, this)
        checkAudioPermission()
        setupSpeechRecognizer()

        setContent {
            // เรียก Theme ตามที่มีในโปรเจกต์
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                VoiceControlScreen(
                    isTargetOn = isTargetOn,
                    resultText = resultText,
                    onListenClick = { startListening() }
                )
            }
        }
    }

    private fun setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                resultText = "กำลังฟังอยู่... พูดได้เลย"
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val spokenText = matches[0].lowercase(Locale.getDefault())
                    resultText = "คุณพูดว่า: \"$spokenText\""
                    processVoiceCommand(spokenText)
                }
            }

            override fun onError(error: Int) {
                resultText = "ฟังไม่ทัน หรือเกิดข้อผิดพลาด ($error)"
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "th-TH")
        }
        speechRecognizer.startListening(intent)
    }

    private fun processVoiceCommand(command: String) {
        when {
            command.contains("เปิด") || command.contains("on") -> {
                isTargetOn = true
                speakOut("เปิดสวิตช์เรียบร้อยแล้วค่ะ")
            }
            command.contains("ปิด") || command.contains("off") -> {
                isTargetOn = false
                speakOut("ปิดสวิตช์เรียบร้อยแล้วค่ะ")
            }
            else -> {
                speakOut("ไม่เข้าใจคำสั่ง กรุณาพูดว่าเปิดหรือปิดค่ะ")
            }
        }
    }

    private fun speakOut(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale("th", "TH"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "เครื่องนี้ยังไม่รองรับภาษาไทยสำหรับ TTS", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                100
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
        tts.stop()
        tts.shutdown()
    }
}

// Layout หน้าจอหลักที่เขียนด้วย Jetpack Compose
@Composable
fun VoiceControlScreen(
    isTargetOn: Boolean,
    resultText: String,
    onListenClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ปุ่มเป้าหมาย: ตั้ง enabled = false เพื่อห้ามกดตรงๆ
        Button(
            onClick = { /* กดไม่ได้อยู่แล้ว */ },
            enabled = false,
            modifier = Modifier
                .width(220.dp)
                .height(60.dp)
        ) {
            Text(
                text = if (isTargetOn) "สถานะ: เปิดใช้งาน" else "สถานะ: ปิดอยู่",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ข้อความแสดงผลคำที่ฟังได้
        Text(
            text = resultText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ปุ่มกดเพื่อเริ่มรับเสียง
        Button(
            onClick = onListenClick,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = "🎙️ กดแล้วพูดคำสั่ง",
                fontSize = 16.sp
            )
        }
    }
}

