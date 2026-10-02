package com.example.ai

import android.util.Log
import com.example.BuildConfig
import com.example.model.AssessmentResult
import com.example.model.PracticeSessionRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AvanTeacherAiService {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun analyzeFirstTimePerformance(
        notesCount: Int,
        uniquePitchesCount: Int,
        tempoBpmEstimate: Int,
        tempoVariationPercent: Int,
        rhythmSteadinessPercent: Int,
        pitchStabilityPercent: Int,
        detectedNoteNames: List<String>,
        hasFastPassages: Boolean
    ): AssessmentResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        val prompt = """
            تو «آوان»، یک استاد و معلم خصوصی پیانوی بسیار حرفه‌ای، صبور و دقیق ایرانی هستی.
            یک هنرجوی جدید سخت‌ترین قطعه‌ای که بلد است را روی پیانو اجرا کرده و سنسورهای محلی صوتی اپلیکیشن اطلاعات عملکرد او را استخراج کرده‌اند:
            - تعداد کل نت‌های نواخته شده: $notesCount
            - تنوع و گستره نت‌ها (تعداد نت‌های منحصربه‌فرد): $uniquePitchesCount
            - تمپوی تقریبی اجرا: $tempoBpmEstimate BPM
            - نوسان و ناپایداری تمپو: $tempoVariationPercent%
            - پایداری و ثبات ریتم: $rhythmSteadinessPercent%
            - دقت و تمیزی فرکانسی نت‌ها: $pitchStabilityPercent%
            - آیا پاساژهای سریع داشت: ${if (hasFastPassages) "بله" else "خیر"}
            - نمونه نت‌های ثبت‌شده: ${detectedNoteNames.take(20).joinToString(", ")}

            وظیفه تو:
            یک ارزیابی سطح دقیق و حرفه‌ای انجام بده بدون اینکه بپرسی مبتدی است یا نه.
            سطح واقعی او را تخمین بزن (مثلاً "مقدماتی پیشرفته", "متوسط", "متوسط رو به پیشرفته", "پیشرفته").
            نقاط قوت و موارد نیازمند تقویت را مشخص کن.
            پاسخ را دقیقاً در قالب این شیء JSON بدون هیچ متن اضافی بده:
            {
              "overallLevel": "متوسط رو به پیشرفته",
              "pitchAccuracy": 85,
              "rhythmAccuracy": 78,
              "tempoStability": 72,
              "technicalControl": 80,
              "twoHandCoordination": 75,
              "strengths": ["شناخت خوب فواصل نت‌ها در دست راست", "آغاز تمیز ملودی"],
              "areasToImprove": ["ثبات ضرب‌آهنگ در پاساژهای پرسرعت", "هماهنگی دست چپ با آکوردها"],
              "teacherDiagnosis": "تحلیل تخصصی تو به فارسی روان، صمیمی و در عین حال استادانه",
              "recommendedStartingBpm": 65,
              "requiresFollowUp": false
            }
        """.trimIndent()

        if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val responseJson = callGeminiWithThinking(
                    model = "gemini-3.1-pro-preview",
                    prompt = prompt,
                    apiKey = apiKey
                )
                if (responseJson != null) {
                    val parsed = parseAssessmentJson(responseJson)
                    if (parsed != null) return@withContext parsed
                }
            } catch (e: Exception) {
                Log.e("AvanAI", "Gemini error: ${e.message}")
            }
        }

        // Heuristic fallback for zero-network or missing API key
        generateHeuristicAssessment(
            notesCount,
            uniquePitchesCount,
            tempoBpmEstimate,
            tempoVariationPercent,
            rhythmSteadinessPercent,
            pitchStabilityPercent,
            hasFastPassages
        )
    }

    suspend fun getTeacherChatResponse(
        userQuestion: String,
        userLevel: String,
        currentBpm: Int,
        recentSessions: List<PracticeSessionRecord>
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        val sessionContext = if (recentSessions.isNotEmpty()) {
            val last = recentSessions.first()
            "آخرین قطعه تمرین شده: ${last.songTitle}، دقت نت: ${last.accuracyPercent}٪، دقت ریتم: ${last.rhythmPercent}٪، بخش مشکل‌دار: ${last.problematicSection}"
        } else {
            "هنرجو هنوز تمرین کامل ثبت نکرده است."
        }

        val prompt = """
            تو «آوان»، معلم خصوصی پیانوی پیگیر و مهربان هستی.
            سطح فعلی هنرجو: $userLevel
            تمپوی تمرینی: $currentBpm BPM
            سابقه تمرین اخیر هنرجو: $sessionContext

            سوال یا پیام هنرجو:
            "$userQuestion"

            پاسخ تو به عنوان معلم پیانو:
            - کاملاً فارسی، محترمانه، راهگشا و متمرکز صرفاً روی نوازندگی پیانو (دست چپ، دست راست، پدال، انگشت‌گذاری، ریتم، تنفس موسیقایی).
            - حداکثر ۳ الی ۴ پاراگراف کوتاه با تمرین عملی مشخص.
        """.trimIndent()

        if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val result = callGemini(
                    model = "gemini-3.5-flash",
                    prompt = prompt,
                    apiKey = apiKey
                )
                if (!result.isNullOrBlank()) return@withContext result
            } catch (e: Exception) {
                Log.e("AvanAI", "Chat Gemini call failed: ${e.message}")
            }
        }

        // Intelligent local response fallback
        generateOfflineChatAnswer(userQuestion, userLevel, recentSessions)
    }

    suspend fun analyzePracticeMistakes(
        songTitle: String,
        accuracyPercent: Int,
        rhythmPercent: Int,
        tempoPercent: Int,
        mistakesCount: Int,
        lateNotesCount: Int,
        earlyNotesCount: Int,
        currentBpm: Int
    ): Pair<String, String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        val prompt = """
            به عنوان معلم پیانو «آوان»، اجرای قطعه «$songTitle» را تحلیل کن:
            - دقت نت: $accuracyPercent%
            - دقت ریتم: $rhythmPercent%
            - پایداری تمپو: $tempoPercent%
            - تعداد خطاهای نت: $mistakesCount
            - تعداد نت‌های دیرتر از ضرب: $lateNotesCount
            - تعداد نت‌های زودتر از ضرب: $earlyNotesCount
            - تمپوی تمرین: $currentBpm BPM

            پاسخ در فرمت JSON:
            {
              "feedback": "تحلیل کوتاه و دقیق معلم درباره علت خطاها و دستاورد این جلسه",
              "nextStep": "دستور تمرین بعدی (مثلا کاهش تمپو به ۶۰، تمرین دست راست تنها)"
            }
        """.trimIndent()

        if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val res = callGemini(model = "gemini-3.5-flash", prompt = prompt, apiKey = apiKey)
                if (res != null) {
                    val cleanJson = cleanJsonString(res)
                    val jsonObj = JSONObject(cleanJson)
                    val fb = jsonObj.optString("feedback", "")
                    val ns = jsonObj.optString("nextStep", "")
                    if (fb.isNotBlank()) return@withContext Pair(fb, ns)
                }
            } catch (e: Exception) {
                Log.e("AvanAI", "Mistake analysis error: ${e.message}")
            }
        }

        // Fallback rule-based teacher diagnosis
        val feedback = when {
            accuracyPercent >= 90 && rhythmPercent >= 85 ->
                "تسلط فوق‌العاده‌ای روی فواصل نت‌های $songTitle داشتی. زمان‌بندی ضرب‌ها بسیار تمیز بود و حس قطعه به خوبی منتقل شد."
            rhythmPercent < 75 ->
                "ریتم این قسمت کمی شتاب‌زده شد؛ نت‌ها را زودتر از فرود ضرب نواختی. مترونوم درونی نیاز به استراحت و تنفس دارد."
            else ->
                "دقت انگشت‌گذاری در دست راست مناسب بود اما در پاساژهای میانی، کشش برخی نت‌ها کمتر از ارزش زمانی واقعی‌شان ادا شد."
        }

        val nextStep = when {
            accuracyPercent >= 88 && rhythmPercent >= 85 ->
                "آماده‌ای تمپو را ۵ واحد افزایش داده و روی $currentBpm BPM تثبیت کنی."
            currentBpm > 65 && rhythmPercent < 80 ->
                "تمپو را به ${currentBpm - 10} BPM کاهش بده و ابتدا دست راست را به صورت لوپ ۲ میزانی تمرین کن."
            else ->
                "روی میزان‌های ۶ تا ۱۰ تمرکز کن و قبل از نواختن، ضرب‌ها را با صدای بلند بشمار."
        }

        Pair(feedback, nextStep)
    }

    private fun callGeminiWithThinking(model: String, prompt: String, apiKey: String): String? {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val requestJson = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                })
            }
            put("contents", contentsArray)

            // High thinking configuration for gemini-3.1-pro-preview
            val genConfig = JSONObject().apply {
                val thinkingConfig = JSONObject().apply {
                    put("thinkingLevel", "HIGH")
                }
                put("thinkingConfig", thinkingConfig)
                put("temperature", 0.7)
            }
            put("generationConfig", genConfig)
        }

        val request = Request.Builder()
            .url(url)
            .post(requestJson.toString().toRequestBody(jsonMediaType))
            .build()

        val response = okHttpClient.newCall(request).execute()
        val body = response.body?.string()
        if (!response.isSuccessful || body == null) {
            return null
        }

        val root = JSONObject(body)
        val candidates = root.optJSONArray("candidates") ?: return null
        if (candidates.length() == 0) return null
        val firstCandidate = candidates.getJSONObject(0)
        val content = firstCandidate.optJSONObject("content") ?: return null
        val parts = content.optJSONArray("parts") ?: return null
        for (i in 0 until parts.length()) {
            val part = parts.getJSONObject(i)
            if (part.has("text")) {
                return part.getString("text")
            }
        }
        return null
    }

    private fun callGemini(model: String, prompt: String, apiKey: String): String? {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val requestJson = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                })
            }
            put("contents", contentsArray)
        }

        val request = Request.Builder()
            .url(url)
            .post(requestJson.toString().toRequestBody(jsonMediaType))
            .build()

        val response = okHttpClient.newCall(request).execute()
        val body = response.body?.string() ?: return null
        if (!response.isSuccessful) return null

        val root = JSONObject(body)
        val candidates = root.optJSONArray("candidates") ?: return null
        if (candidates.length() == 0) return null
        val parts = candidates.getJSONObject(0).optJSONObject("content")?.optJSONArray("parts") ?: return null
        for (i in 0 until parts.length()) {
            val part = parts.getJSONObject(i)
            if (part.has("text")) {
                return part.getString("text")
            }
        }
        return null
    }

    private fun cleanJsonString(raw: String): String {
        var str = raw.trim()
        if (str.startsWith("```json")) {
            str = str.removePrefix("```json").trim()
        } else if (str.startsWith("```")) {
            str = str.removePrefix("```").trim()
        }
        if (str.endsWith("```")) {
            str = str.removeSuffix("```").trim()
        }
        return str
    }

    private fun parseAssessmentJson(rawText: String): AssessmentResult? {
        return try {
            val clean = cleanJsonString(rawText)
            val json = JSONObject(clean)

            val strengthsArr = json.optJSONArray("strengths")
            val strengths = mutableListOf<String>()
            if (strengthsArr != null) {
                for (i in 0 until strengthsArr.length()) {
                    strengths.add(strengthsArr.getString(i))
                }
            }

            val improveArr = json.optJSONArray("areasToImprove")
            val areasToImprove = mutableListOf<String>()
            if (improveArr != null) {
                for (i in 0 until improveArr.length()) {
                    areasToImprove.add(improveArr.getString(i))
                }
            }

            AssessmentResult(
                overallLevel = json.optString("overallLevel", "متوسط"),
                pitchAccuracy = json.optInt("pitchAccuracy", 82),
                rhythmAccuracy = json.optInt("rhythmAccuracy", 78),
                tempoStability = json.optInt("tempoStability", 74),
                technicalControl = json.optInt("technicalControl", 80),
                twoHandCoordination = json.optInt("twoHandCoordination", 76),
                strengths = if (strengths.isNotEmpty()) strengths else listOf("کنترل روان بر کلاویه‌های میانی", "شنوایی موسیقایی خوب"),
                areasToImprove = if (areasToImprove.isNotEmpty()) areasToImprove else listOf("تقویت ضرب‌آهنگ دست چپ", "پایداری تمپو در پرش‌ها"),
                teacherDiagnosis = json.optString(
                    "teacherDiagnosis",
                    "نوازندگی شما نشان‌دهنده درک حسی خوب از ملودی است. با تمرکز بر ثبات مترونومیک، به سرعت وارد قطعات پیشرفته‌تر خواهید شد."
                ),
                recommendedStartingBpm = json.optInt("recommendedStartingBpm", 70),
                requiresFollowUp = json.optBoolean("requiresFollowUp", false)
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun generateHeuristicAssessment(
        notesCount: Int,
        uniquePitchesCount: Int,
        tempoBpmEstimate: Int,
        tempoVariationPercent: Int,
        rhythmSteadinessPercent: Int,
        pitchStabilityPercent: Int,
        hasFastPassages: Boolean
    ): AssessmentResult {
        // High-level piano pedagogue scoring heuristics based on acoustic signals
        val pitchAcc = (pitchStabilityPercent * 0.85f + (uniquePitchesCount * 2.5f).coerceAtMost(15f)).toInt().coerceIn(60, 96)
        val rhythmAcc = rhythmSteadinessPercent.coerceIn(58, 94)
        val tempoStab = (100 - tempoVariationPercent).coerceIn(55, 92)
        val techControl = if (hasFastPassages) (pitchAcc * 0.5f + rhythmAcc * 0.5f).toInt() else (pitchAcc * 0.6f + 30).toInt().coerceIn(65, 91)
        val twoHands = ((rhythmAcc + techControl) / 2).coerceIn(60, 90)

        val avgScore = (pitchAcc + rhythmAcc + tempoStab + techControl + twoHands) / 5

        val level = when {
            avgScore >= 88 -> "پیشرفته"
            avgScore >= 80 -> "متوسط رو به پیشرفته"
            avgScore >= 72 -> "متوسط"
            avgScore >= 64 -> "مقدماتی پیشرفته"
            else -> "مقدماتی (پایه‌ای)"
        }

        val strengths = mutableListOf<String>()
        val areasToImprove = mutableListOf<String>()

        if (pitchAcc >= 80) strengths.add("دقت بالا در تشخیص و تفکیک فواصل نغمه‌ها")
        if (tempoStab >= 75) strengths.add("حفظ ضرب‌آهنگ یکنواخت در موتیف‌های اصلی")
        if (uniquePitchesCount >= 10) strengths.add("تسلط بر گستره پویای کلاویه‌ها و اکتاوهای گوناگون")
        if (strengths.isEmpty()) strengths.add("علاقه‌مندی و لحن زیبای لمس کلاویه‌ها")

        if (rhythmAcc < 82) areasToImprove.add("تقویت دقت کشش نت‌های نقطه‌دار و سکوت‌ها")
        if (twoHands < 80) areasToImprove.add("استقلال دست چپ در همراهی آکوردها")
        if (tempoStab < 78) areasToImprove.add("جلوگیری از شتاب‌زدگی ناخواسته در گذرگاه‌های تکنیکی")
        if (areasToImprove.isEmpty()) areasToImprove.add("پالایش دینامیک نوازندگی و کنترل ظریف تاچ کلاویه")

        val diagnosis = "بر اساس آنالیز دقیق نوسان فرکانسی، پایداری تمپو و تداوم خط ملودی، نوازندگی شما در رده «$level» برآورد می‌شود. پایه شنوایی و انگشت‌گذاری شما بسیار مستعد است و برنامه‌ی تمرینی امروز را متناسب با تقویت این موارد تنظیم کرده‌ام."

        return AssessmentResult(
            overallLevel = level,
            pitchAccuracy = pitchAcc,
            rhythmAccuracy = rhythmAcc,
            tempoStability = tempoStab,
            technicalControl = techControl,
            twoHandCoordination = twoHands,
            strengths = strengths,
            areasToImprove = areasToImprove,
            teacherDiagnosis = diagnosis,
            recommendedStartingBpm = (tempoBpmEstimate * 0.85).toInt().coerceIn(55, 90),
            requiresFollowUp = false
        )
    }

    private fun generateOfflineChatAnswer(
        question: String,
        level: String,
        recentSessions: List<PracticeSessionRecord>
    ): String {
        return when {
            question.contains("دست چپ") || question.contains("عقب") -> {
                "عقب ماندن دست چپ یکی از چالش‌های رایج در سطح $level است. دلیل اصلی آن ضعف حافظه عضلانی انگشت‌های ۴ و ۵ دست چپ نسبت به دست راست است.\n\nتمرین پیشنهادی آوان:\n۱. ابتدا دست چپ را به تنهایی با تمپوی بسیار آرام (۵۵ BPM) با مترونوم اجرا کن.\n۲. روی ضرب اول هر میزان اندکی تاکید وزنی (اکسان) بگذار.\n۳. سپس دست راست را بسیار آرام اضافه کن."
            }
            question.contains("ریتم") || question.contains("مترونوم") -> {
                "برای تثبیت ریتم، بهترین راهکار در این مقطع «شمارش با صدای بلند» همراه با ضرب پا است. هنگام اجرای نت‌های سیاه بگو «یک، دو، سه، چهار» و در چنگ‌ها «یک و دو و». تمرینات فصل ریتم در بخش «مسیر یادگیری» آوان دقیقاً برای حل این موضوع طراحی شده است."
            }
            question.contains("چه قطعه") || question.contains("پیشنهاد") -> {
                "با توجه به سطح $level شما، قطعه «خواب‌های طلایی» جواد معروفی برای روان شدن آرپژهای دست چپ، و قطعه «Gymnopédie No. 1» اریک ساتی برای کنترل لمس و آرامش مچ دست فوق‌العاده هستند. هر دو را در کتابخانه قطعات آماده کرده‌ام."
            }
            question.contains("تمرکز") || question.contains("هفته") -> {
                "این هفته تمرکزت را روی سه اصل بگذار:\n۱. آرام‌سازی مچ دست و رها کردن تنش شانه‌ها.\n۲. ۲۰ دقیقه تمرین روزانه در دو نوبت ۱۰ دقیقه‌ای.\n۳. اتمام کامل گام دو ماژور و تمرینات استقلال دو دست قبل از ورود به قطعه اصلی."
            }
            else -> {
                "سلام هنرجوی گرامی آوان! سوالت بسیار به جاست. نوازندگی پیانو ترکیبی از گوش تربیت‌شده، کنترل تنفس و آرامش دستان است. در سطح فعلی ($level)، بزرگترین کلید موفقیت، تمرین با تمپوی کنترل‌شده و متمرکز است. هر زمان آماده بودی، با زدن دکمه «شروع تمرین امروز» جلسه جدیدمان را آغاز کنیم!"
            }
        }
    }
}
