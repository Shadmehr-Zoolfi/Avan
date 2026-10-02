package com.example.repository

import com.example.data.db.AppDao
import com.example.data.db.PaymentTransactionEntity
import com.example.data.db.PracticeSessionEntity
import com.example.data.db.SongProgressEntity
import com.example.data.db.UserEntity
import com.example.model.AssessmentResult
import com.example.model.FollowUpTest
import com.example.model.HandType
import com.example.model.LearningPathStage
import com.example.model.PaymentTransaction
import com.example.model.PianoBookRecommendation
import com.example.model.PianoNote
import com.example.model.PracticeSessionRecord
import com.example.model.PracticeStep
import com.example.model.Song
import com.example.model.SubscriptionType
import com.example.model.TodayPracticePlan
import com.example.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PianoRepository(private val appDao: AppDao) {

    val userProfileFlow: Flow<UserProfile> = appDao.getUserFlow().map { entity ->
        if (entity != null) {
            UserProfile(
                id = entity.id,
                fullName = entity.fullName,
                email = entity.email,
                isPremium = entity.isPremium,
                currentLevel = entity.currentLevel,
                hasCompletedAssessment = entity.hasCompletedAssessment,
                totalPracticeMinutes = entity.totalPracticeMinutes,
                completedSongsCount = entity.completedSongsCount,
                currentBpm = entity.currentBpm
            )
        } else {
            UserProfile()
        }
    }

    val practiceSessionsFlow: Flow<List<PracticeSessionRecord>> = appDao.getAllSessionsFlow().map { list ->
        list.map {
            PracticeSessionRecord(
                id = it.id,
                songId = it.songId,
                songTitle = it.songTitle,
                timestamp = it.timestamp,
                durationMinutes = it.durationMinutes,
                accuracyPercent = it.accuracyPercent,
                rhythmPercent = it.rhythmPercent,
                tempoPercent = it.tempoPercent,
                mistakesCount = it.mistakesCount,
                problematicSection = it.problematicSection,
                teacherFeedback = it.teacherFeedback,
                recommendedNextStep = it.recommendedNextStep
            )
        }
    }

    suspend fun saveAssessmentResult(result: AssessmentResult) {
        val currentUser = appDao.getUser() ?: UserEntity()
        val updated = currentUser.copy(
            hasCompletedAssessment = true,
            currentLevel = result.overallLevel,
            pitchAccuracy = result.pitchAccuracy,
            rhythmAccuracy = result.rhythmAccuracy,
            tempoStability = result.tempoStability,
            technicalControl = result.technicalControl,
            twoHandCoordination = result.twoHandCoordination,
            currentBpm = result.recommendedStartingBpm,
            assessmentDate = System.currentTimeMillis(),
            assessmentTeacherNote = result.teacherDiagnosis
        )
        appDao.insertOrUpdateUser(updated)
    }

    suspend fun recordPracticeSession(session: PracticeSessionRecord) {
        appDao.insertPracticeSession(
            PracticeSessionEntity(
                songId = session.songId,
                songTitle = session.songTitle,
                timestamp = session.timestamp,
                durationMinutes = session.durationMinutes,
                accuracyPercent = session.accuracyPercent,
                rhythmPercent = session.rhythmPercent,
                tempoPercent = session.tempoPercent,
                mistakesCount = session.mistakesCount,
                problematicSection = session.problematicSection,
                teacherFeedback = session.teacherFeedback,
                recommendedNextStep = session.recommendedNextStep
            )
        )

        // Update user's aggregate practice minutes
        val user = appDao.getUser() ?: UserEntity()
        appDao.insertOrUpdateUser(
            user.copy(
                totalPracticeMinutes = user.totalPracticeMinutes + session.durationMinutes
            )
        )

        // Update song progress
        val existingProgress = appDao.getSongProgress(session.songId)
        val newHighest = maxOf(existingProgress?.highestAccuracy ?: 0, session.accuracyPercent)
        val newTimes = (existingProgress?.timesPracticed ?: 0) + 1
        val isDone = (existingProgress?.isCompleted == true) || session.accuracyPercent >= 85

        appDao.upsertSongProgress(
            SongProgressEntity(
                songId = session.songId,
                songTitle = session.songTitle,
                masteryPercent = maxOf(existingProgress?.masteryPercent ?: 0, session.accuracyPercent),
                highestAccuracy = newHighest,
                timesPracticed = newTimes,
                lastPracticedAt = System.currentTimeMillis(),
                isCompleted = isDone
            )
        )
    }

    suspend fun activatePremiumSubscription(plan: SubscriptionType, txId: String, refId: String) {
        val user = appDao.getUser() ?: UserEntity()
        appDao.insertOrUpdateUser(user.copy(isPremium = true))

        appDao.insertTransaction(
            PaymentTransactionEntity(
                transactionId = txId,
                planType = plan.name,
                amountToman = plan.priceToman,
                timestamp = System.currentTimeMillis(),
                status = "SUCCESS",
                referenceId = refId
            )
        )
    }

    // Static curated piano books matching student level and weaknesses
    fun getRecommendedBooks(level: String): List<PianoBookRecommendation> {
        return listOf(
            PianoBookRecommendation(
                id = "beyer_101",
                title = "Elementary Instruction Book Op. 101",
                persianTitle = "متد مقدماتی پیانو بایر (Op. 101)",
                author = "فردیناند بایر (Ferdinand Beyer)",
                difficulty = "مقدماتی",
                description = "کتاب مرجع یادگیری الفبای پیانو، کلید سل و فا، پوزیسیون ۵ انگشت و هماهنگی گام به گام دست راست و چپ.",
                whyRecommended = "آوان این کتاب را برای ساختاربندی محکم پایه نوازندگی و تقویت خوانش نوت‌های دست چپ پیشنهاد می‌کند.",
                targetSkills = listOf("خواندن کلید فا", "استقلال انگشتان", "ریتم پایه ۴/۴ و ۳/۴")
            ),
            PianoBookRecommendation(
                id = "czerny_599",
                title = "Practical Exercises for Beginners Op. 599",
                persianTitle = "اتودهای کاربردی پیانو چرنی (Op. 599)",
                author = "کارل چرنی (Carl Czerny)",
                difficulty = "متوسط",
                description = "مجموعه ۱۰۰ اتود طلایی برای افزایش چابکی انگشتان، دقت در اجرای آکسان‌ها، استکاتو و لگاتو.",
                whyRecommended = "با توجه به نتایج ارزیابی، برای بهبود پایداری سرعت در پاساژها و تمیزی ضرب‌آهنگ عالی است.",
                targetSkills = listOf("سرعت و چابکی", "اجرای پاساژهای پرپیچ‌وخم", "تقویت دست ضعیف‌تر")
            ),
            PianoBookRecommendation(
                id = "hanon_virtuoso",
                title = "The Virtuoso Pianist in 60 Exercises",
                persianTitle = "پیانیست چیره‌دست (هانون ۶۰ تمرین)",
                author = "شارل لویی هانون (C. L. Hanon)",
                difficulty = "متوسط تا پیشرفته",
                description = "ورزشگاه انگشتان پیانیست؛ برطرف‌کننده ضعف انگشتان ۴ و ۵ و یکنواخت‌کننده قدرت لمس در تمام کلاویه‌ها.",
                whyRecommended = "برای افزایش قدرت عضلانی دست چپ و برطرف کردن تاخیر ناخواسته در ضرب‌ها.",
                targetSkills = listOf("قدرت انگشت ۴ و ۵", "انعطاف مچ دست", "برابری قدرت دو دست")
            ),
            PianoBookRecommendation(
                id = "maroofi_album",
                title = "Persian Piano Masterpieces",
                persianTitle = "ردیف و قطعات پیانو استاد جواد معروفی",
                author = "استاد جواد معروفی",
                difficulty = "متوسط به بالا",
                description = "گنجینه‌ی موسیقی اصیل ایرانی برای پیانو؛ پیوند تکنیک کلاسیک اروپایی با لطافت دستگاه‌های ایرانی چون همایون، شور و اصفهان.",
                whyRecommended = "برای پرورش لحن احساسی، آرپژهای مواج و ادای ریزه‌کاری‌های موسیقی فاخر ایرانی بر روی کلاویه‌ها.",
                targetSkills = listOf("آرپژهای گسترده", "دینامیک عاطفی", "ریتم‌های لنگ ایرانی")
            ),
            PianoBookRecommendation(
                id = "burgmuller_100",
                title = "25 Progressive Etudes Op. 100",
                persianTitle = "۲۵ اتود ملودیک و پیش‌رونده (Op. 100)",
                author = "فریدریش بورگمولر (Friedrich Burgmüller)",
                difficulty = "متوسط",
                description = "اتودهایی که نه شبیه تمرین مکانیکی، بلکه مانند قطعات زیبای کنسرت هستند (شامل چوپان، عربسک و تسلی).",
                whyRecommended = "تقویت بیان موسیقایی و لطافت تاچ بدون حس خستگی تمرینات خشک.",
                targetSkills = listOf("بیان عاطفی", "آکورد و آرپژ ملودیک", "پدال‌گیری تمیز")
            )
        )
    }

    // Dynamic learning path stages
    fun getLearningPath(userLevel: String): List<LearningPathStage> {
        val isAdv = userLevel.contains("پیشرفته")
        val isInter = userLevel.contains("متوسط")

        return listOf(
            LearningPathStage(
                stageNumber = 1,
                title = "مرحله ۱: کنترل ریتم و ضرب‌آهنگ درونی",
                subtitle = "تسلط بر ارزش زمانی نت‌ها، سکوت‌ها و شمارش دقیق با مترونوم",
                focusSkill = "ثبات تمپو و فرود سر ضرب",
                exercisesCount = 8,
                recommendedPiece = "جان مریم (بخش نخست)",
                isUnlocked = true,
                isCurrent = !isInter && !isAdv,
                isCompleted = isInter || isAdv,
                masteryPercent = if (isInter || isAdv) 100 else 65
            ),
            LearningPathStage(
                stageNumber = 2,
                title = "مرحله ۲: هماهنگی و استقلال دو دست",
                subtitle = "جدا کردن ریتم ملودی دست راست از همراهی دست چپ",
                focusSkill = "استقلال انگشت ۴ و ۵ دست چپ",
                exercisesCount = 12,
                recommendedPiece = "گل گلدون من",
                isUnlocked = true,
                isCurrent = isInter && !isAdv,
                isCompleted = isAdv,
                masteryPercent = if (isAdv) 100 else if (isInter) 75 else 20
            ),
            LearningPathStage(
                stageNumber = 3,
                title = "مرحله ۳: کنترل سرعت و پاساژهای پرسرعت",
                subtitle = "اجرای روان چنگ‌ها و دولاچنگ‌ها بدون تنش در مچ دست",
                focusSkill = "ریلکس بودن مچ و انعطاف انگشتان",
                exercisesCount = 15,
                recommendedPiece = "Für Elise (برای الیزه)",
                isUnlocked = isInter || isAdv,
                isCurrent = isAdv,
                isCompleted = false,
                masteryPercent = if (isAdv) 80 else 15
            ),
            LearningPathStage(
                stageNumber = 4,
                title = "مرحله ۴: آکوردهای پیچیده و همراهی هارمونیک",
                subtitle = "آکوردهای ۷ و معکوس‌ها در گام‌های مینور و ماژور",
                focusSkill = "آرپژهای شکسته و همراهی باس آلبِرتی",
                exercisesCount = 10,
                recommendedPiece = "خواب‌های طلایی (ورژن کامل)",
                isUnlocked = isAdv,
                isCurrent = false,
                isCompleted = false,
                masteryPercent = 0
            ),
            LearningPathStage(
                stageNumber = 5,
                title = "مرحله ۵: اجرای قطعات فاخر و بیان ویرتوزو",
                subtitle = "اجرای صحنه‌ای با داینامیک پیانو و فورته، پدال‌گیری حرفه‌ای و تنفس عاطفی",
                focusSkill = "داینامیک و رنگ‌آمیزی صوتی",
                exercisesCount = 20,
                recommendedPiece = "مهتاب (Clair de Lune)",
                isUnlocked = false,
                isCurrent = false,
                isCompleted = false,
                masteryPercent = 0
            )
        )
    }

    // Daily smart practice plan generator tailored to recent performance
    fun getTodayPracticePlan(userLevel: String): TodayPracticePlan {
        return TodayPracticePlan(
            date = "امروز",
            totalMinutes = 40,
            motivationQuote = "«نواختن پیانو فقط فشردن کلیدها نیست؛ هر ضربه باید روایتگر یک تنفس باشد.» — آوان",
            steps = listOf(
                PracticeStep(
                    stepNumber = 1,
                    title = "گرم کردن دستان و انگشتان",
                    durationMinutes = 5,
                    description = "تمرین ۵ انگشت هانون شماره ۱ روی گام دو ماژور با تمپوی ملایم ۶۰ برای خون‌رسانی و آرامش تاندون‌ها.",
                    targetBpm = 60
                ),
                PracticeStep(
                    stepNumber = 2,
                    title = "تمرین تمرکز ریتم و شمارش ضرب",
                    durationMinutes = 7,
                    description = "اجرای میزان‌های ۴ تا ۸ قطعه هدف، با همراهی مترونوم و شمارش با صدای بلند بدون پدال.",
                    targetBpm = 65
                ),
                PracticeStep(
                    stepNumber = 3,
                    title = "تمرین اختصاصی نقطه ضعف (دست چپ)",
                    durationMinutes = 8,
                    description = "آرپژهای دو دست چپ به تنهایی با تاکید بر ضرب اول برای ایجاد استقلال و دقت در پرش‌ها.",
                    targetBpm = 70
                ),
                PracticeStep(
                    stepNumber = 4,
                    title = "تمرین قطعه اصلی (خواب‌های طلایی)",
                    durationMinutes = 15,
                    description = "اجرای بخش تماتیک و ورود به موتیف احساسی با داینامیک متغیر و پیوند دو دست.",
                    targetBpm = 72
                ),
                PracticeStep(
                    stepNumber = 5,
                    title = "آزمون عملکرد پایانی روز با آوان",
                    durationMinutes = 5,
                    description = "یک اجرای زنده و یکپارچه در حضور آوان برای ثبت رکورد دقت، سنجش پیشرفت و دریافت بازخورد جلسه.",
                    targetBpm = 72
                )
            )
        )
    }

    // Curated Persian and Classical Piano Songs Library
    fun getAllSongs(): List<Song> {
        return listOf(
            Song(
                id = "maroofi_golden_dreams",
                title = "Golden Dreams",
                persianTitle = "خواب‌های طلایی",
                composer = "استاد جواد معروفی",
                difficulty = "متوسط",
                defaultBpm = 72,
                category = "موسیقی ایرانی",
                description = "شاهکار بی‌بدیل پیانوی ایرانی در آواز اصفهان با ملودی عاطفی و ماندگار.",
                whyRecommended = "آوان این قطعه را به دلیل تناسب بالا با سطح فعلی شما و نیاز به تقویت آرپژهای روان دست چپ پیشنهاد می‌کند.",
                skillsLearned = listOf("آرپژهای دست چپ", "بیان عاطفی ملودی", "پدال‌گیری دقیق"),
                userCompatibilityPercent = 95,
                notes = generateGoldenDreamsNotes()
            ),
            Song(
                id = "gole_goldoon",
                title = "Gole Goldoon Man",
                persianTitle = "گل گلدون من",
                composer = "فریدون شهبازیان / سیمین غانم",
                difficulty = "مبتدی تا متوسط",
                defaultBpm = 68,
                category = "پاپ",
                description = "از محبوب‌ترین و خاطره‌انگیزترین نغمه‌های موسیقی پاپ اصیل ایران با هارمونی دلنشین.",
                whyRecommended = "عالی برای یادگیری آکوردهای پایه و حفظ تعادل صدای همراهی نسبت به ملودی آواز.",
                skillsLearned = listOf("تعادل صداها", "آکوردهای سه صدایی", "شمارش ۶/۸ آرام"),
                userCompatibilityPercent = 90,
                notes = generateGoleGoldoonNotes()
            ),
            Song(
                id = "jane_maryam",
                title = "Jan-e Maryam",
                persianTitle = "جان مریم (گل مریم)",
                composer = "کامبیز مژدهی / محمد نوری",
                difficulty = "مبتدی",
                defaultBpm = 80,
                category = "موسیقی ایرانی",
                description = "نغمه‌ای لطیف در مایه دشتی و شوشتری که اجرای پیانویی آن روح‌بخش است.",
                whyRecommended = "بهترین گزینه برای تسلط بر ریتم ۳/۴ والسی و هماهنگی گام به گام دو دست.",
                skillsLearned = listOf("ریتم والسی ۳/۴", "هماهنگی سرضرب‌ها", "ملودی دست راست"),
                userCompatibilityPercent = 98,
                notes = generateJaneMaryamNotes()
            ),
            Song(
                id = "fur_elise",
                title = "Für Elise",
                persianTitle = "برای الیزه",
                composer = "لودویگ وان بتهوون",
                difficulty = "متوسط",
                defaultBpm = 85,
                category = "کلاسیک",
                description = "شناخته‌شده‌ترین اثر پیانویی تاریخ موسیقی، در گام لا مینور (A minor).",
                whyRecommended = "پرورش پرش‌های ظریف نیم‌پرده‌ای و انتقال سریع بین کلاویه‌های سفید و سیاه.",
                skillsLearned = listOf("تناوب نیم‌پرده‌ای E-D#", "انتقال اکتاو", "کنترل لمس لگاتو"),
                userCompatibilityPercent = 88,
                notes = generateFurEliseNotes()
            ),
            Song(
                id = "soltane_ghalbha",
                title = "Soltane Ghalbha",
                persianTitle = "سلطان قلب‌ها",
                composer = "انوشیروان روحانی",
                difficulty = "متوسط",
                defaultBpm = 84,
                category = "فیلم و سریال",
                description = "موسیقی متن جاودان سینمای ایران ساخته استاد انوشیروان روحانی با آکوردهای درخشان.",
                whyRecommended = "تقویت قدرت جهش دست‌ها و فرود محکم روی آکوردهای مینور.",
                skillsLearned = listOf("آکوردهای احساسی", "تکنیک تکرار نت", "ریتم والس ملایم"),
                userCompatibilityPercent = 92,
                notes = generateSoltaneGhalbhaNotes()
            ),
            Song(
                id = "clair_de_lune",
                title = "Clair de Lune",
                persianTitle = "مهتاب (سوئیت برگاماسک)",
                composer = "کلود دبوسی",
                difficulty = "پیشرفته",
                defaultBpm = 50,
                category = "کلاسیک",
                description = "قله‌ی امپرسیونیسم در پیانو با آکوردهای اثیری و بافت مه‌آلود و رویایی.",
                whyRecommended = "برای پیانیست‌های ماهر که خواهان تسلط بر پدال، نوانس پیانیسیمو و تعلیق زمانی هستند.",
                skillsLearned = listOf("تاچ نرم پیانیسیمو", "پلی‌ریتم و تعلیق", "هارمونی گسترده"),
                userCompatibilityPercent = 75,
                notes = generateClairDeLuneNotes()
            ),
            Song(
                id = "gymnopedie_1",
                title = "Gymnopédie No. 1",
                persianTitle = "ژیمنوپدی شماره ۱",
                composer = "اریک ساتی (Erik Satie)",
                difficulty = "متوسط",
                defaultBpm = 62,
                category = "آرام",
                description = "قطعه‌ای آرامش‌بخش، درون‌گرایانه و شاهکار مینیمال برای رهایی از تنش‌های روزمره.",
                whyRecommended = "تقویت پرش نرم دست چپ از نت باس به آکورد میانی در هر میزان.",
                skillsLearned = listOf("پرش نرم باس به آکورد", "سکوت‌های معنادار", "تنفس موسیقایی"),
                userCompatibilityPercent = 85,
                notes = generateGymnopedieNotes()
            ),
            Song(
                id = "canon_in_d",
                title = "Canon in D",
                persianTitle = "کانن در ر ماژور",
                composer = "یوهان پاخلبل",
                difficulty = "متوسط رو به پیشرفته",
                defaultBpm = 75,
                category = "کلاسیک",
                description = "ملودی باشکوه با خط باس تکرارشونده و پلی‌فونی شگفت‌انگیز دوره باروک.",
                whyRecommended = "عالی برای یادگیری همراهی آکوردها و تقویت شنیداری خطوط موازی.",
                skillsLearned = listOf("خط باس باروک", "آرپژهای دو دست", "پلی‌فونی چندصدایی"),
                userCompatibilityPercent = 86,
                notes = generateCanonNotes()
            )
        )
    }

    // Follow-up test generator if first assessment had low confidence
    fun getFollowUpTest(testType: String): FollowUpTest {
        return when (testType) {
            "rhythm" -> FollowUpTest(
                id = "test_rhythm_1",
                title = "آزمون تکمیلی: پایداری ریتم و ضرب‌آهنگ",
                description = "این الگوی ریتمیک ۴ میزانی را همراه با مترونوم اجرا کن تا ثبات ضرب تو دقیق‌تر سنجیده شود.",
                bpm = 70,
                focusArea = "ثبات مترونومیک",
                notes = listOf(
                    PianoNote("r1", 60, HandType.RIGHT, 0f, 1f, 1),
                    PianoNote("r2", 60, HandType.RIGHT, 1f, 1f, 1),
                    PianoNote("r3", 64, HandType.RIGHT, 2f, 1f, 1),
                    PianoNote("r4", 67, HandType.RIGHT, 3f, 1f, 1),
                    PianoNote("r5", 65, HandType.RIGHT, 4f, 2f, 2),
                    PianoNote("r6", 64, HandType.RIGHT, 6f, 2f, 2)
                )
            )
            "chord" -> FollowUpTest(
                id = "test_chord_1",
                title = "آزمون تکمیلی: هارمونی و آکوردهای سه صدایی",
                description = "آکوردهای دو ماژور و سل ماژور را با دست چپ و راست به صورت همزمان اجرا کن.",
                bpm = 60,
                focusArea = "هماهنگی آکوردها",
                notes = listOf(
                    PianoNote("c1", 48, HandType.LEFT, 0f, 2f, 1),
                    PianoNote("c2", 60, HandType.RIGHT, 0f, 2f, 1),
                    PianoNote("c3", 64, HandType.RIGHT, 0f, 2f, 1),
                    PianoNote("c4", 67, HandType.RIGHT, 0f, 2f, 1),
                    PianoNote("c5", 43, HandType.LEFT, 2f, 2f, 2),
                    PianoNote("c6", 59, HandType.RIGHT, 2f, 2f, 2),
                    PianoNote("c7", 62, HandType.RIGHT, 2f, 2f, 2),
                    PianoNote("c8", 67, HandType.RIGHT, 2f, 2f, 2)
                )
            )
            else -> FollowUpTest(
                id = "test_scale_speed",
                title = "آزمون تکمیلی: گذرگاه پرسرعت گام",
                description = "گام ۵ نت دو تا سل را با چابکی و دقت انگشت‌گذاری اجرا کن.",
                bpm = 85,
                focusArea = "چابکی انگشتان",
                notes = listOf(
                    PianoNote("s1", 60, HandType.RIGHT, 0f, 0.5f, 1),
                    PianoNote("s2", 62, HandType.RIGHT, 0.5f, 0.5f, 1),
                    PianoNote("s3", 64, HandType.RIGHT, 1.0f, 0.5f, 1),
                    PianoNote("s4", 65, HandType.RIGHT, 1.5f, 0.5f, 1),
                    PianoNote("s5", 67, HandType.RIGHT, 2.0f, 1.0f, 1)
                )
            )
        }
    }

    // Helper generators for actual musical notes of masterpieces
    private fun generateGoldenDreamsNotes(): List<PianoNote> {
        val list = mutableListOf<PianoNote>()
        // Bar 1 - Melody in Right Hand (A minor / Esfahan flavor: E4, A4, B4, C5, B4, A4)
        list.add(PianoNote("gd_1", 57, HandType.LEFT, 0.0f, 2.0f, 1)) // A3 Bass
        list.add(PianoNote("gd_2", 64, HandType.RIGHT, 0.0f, 1.0f, 1)) // E4
        list.add(PianoNote("gd_3", 69, HandType.RIGHT, 1.0f, 1.0f, 1)) // A4
        list.add(PianoNote("gd_4", 71, HandType.RIGHT, 2.0f, 1.0f, 1)) // B4
        list.add(PianoNote("gd_5", 72, HandType.RIGHT, 3.0f, 1.0f, 1)) // C5

        // Bar 2
        list.add(PianoNote("gd_6", 52, HandType.LEFT, 4.0f, 2.0f, 2)) // E3
        list.add(PianoNote("gd_7", 71, HandType.RIGHT, 4.0f, 1.0f, 2)) // B4
        list.add(PianoNote("gd_8", 69, HandType.RIGHT, 5.0f, 1.0f, 2)) // A4
        list.add(PianoNote("gd_9", 68, HandType.RIGHT, 6.0f, 2.0f, 2)) // G#4

        // Bar 3
        list.add(PianoNote("gd_10", 57, HandType.LEFT, 8.0f, 2.0f, 3)) // A3
        list.add(PianoNote("gd_11", 69, HandType.RIGHT, 8.0f, 2.0f, 3)) // A4
        list.add(PianoNote("gd_12", 72, HandType.RIGHT, 10.0f, 1.0f, 3)) // C5
        list.add(PianoNote("gd_13", 76, HandType.RIGHT, 11.0f, 1.0f, 3)) // E5

        // Bar 4
        list.add(PianoNote("gd_14", 53, HandType.LEFT, 12.0f, 2.0f, 4)) // F3
        list.add(PianoNote("gd_15", 77, HandType.RIGHT, 12.0f, 2.0f, 4)) // F5
        list.add(PianoNote("gd_16", 76, HandType.RIGHT, 14.0f, 1.0f, 4)) // E5
        list.add(PianoNote("gd_17", 74, HandType.RIGHT, 15.0f, 1.0f, 4)) // D5

        // Bar 5 - Resolution
        list.add(PianoNote("gd_18", 57, HandType.LEFT, 16.0f, 4.0f, 5)) // A3
        list.add(PianoNote("gd_19", 72, HandType.RIGHT, 16.0f, 2.0f, 5)) // C5
        list.add(PianoNote("gd_20", 69, HandType.RIGHT, 18.0f, 2.0f, 5)) // A4
        return list
    }

    private fun generateJaneMaryamNotes(): List<PianoNote> {
        val list = mutableListOf<PianoNote>()
        // 3/4 Waltz feel: D4 - F4 - G4 - A4 - G4 - F4 - E4 - D4
        list.add(PianoNote("jm_1", 50, HandType.LEFT, 0.0f, 3.0f, 1)) // D3 Bass
        list.add(PianoNote("jm_2", 62, HandType.RIGHT, 0.0f, 1.5f, 1)) // D4
        list.add(PianoNote("jm_3", 65, HandType.RIGHT, 1.5f, 1.5f, 1)) // F4

        list.add(PianoNote("jm_4", 57, HandType.LEFT, 3.0f, 3.0f, 2)) // A3
        list.add(PianoNote("jm_5", 67, HandType.RIGHT, 3.0f, 1.5f, 2)) // G4
        list.add(PianoNote("jm_6", 69, HandType.RIGHT, 4.5f, 1.5f, 2)) // A4

        list.add(PianoNote("jm_7", 50, HandType.LEFT, 6.0f, 3.0f, 3)) // D3
        list.add(PianoNote("jm_8", 67, HandType.RIGHT, 6.0f, 1.0f, 3)) // G4
        list.add(PianoNote("jm_9", 65, HandType.RIGHT, 7.0f, 1.0f, 3)) // F4
        list.add(PianoNote("jm_10", 64, HandType.RIGHT, 8.0f, 1.0f, 3)) // E4

        list.add(PianoNote("jm_11", 50, HandType.LEFT, 9.0f, 3.0f, 4))
        list.add(PianoNote("jm_12", 62, HandType.RIGHT, 9.0f, 3.0f, 4)) // D4
        return list
    }

    private fun generateGoleGoldoonNotes(): List<PianoNote> {
        val list = mutableListOf<PianoNote>()
        // Gole Goldoon: C4 - E4 - G4 - A4 - G4
        list.add(PianoNote("gg_1", 48, HandType.LEFT, 0.0f, 2.0f, 1))
        list.add(PianoNote("gg_2", 60, HandType.RIGHT, 0.0f, 1.0f, 1))
        list.add(PianoNote("gg_3", 64, HandType.RIGHT, 1.0f, 1.0f, 1))
        list.add(PianoNote("gg_4", 67, HandType.RIGHT, 2.0f, 1.5f, 1))
        list.add(PianoNote("gg_5", 69, HandType.RIGHT, 3.5f, 0.5f, 1))

        list.add(PianoNote("gg_6", 45, HandType.LEFT, 4.0f, 2.0f, 2))
        list.add(PianoNote("gg_7", 67, HandType.RIGHT, 4.0f, 2.0f, 2))
        list.add(PianoNote("gg_8", 65, HandType.RIGHT, 6.0f, 1.0f, 2))
        list.add(PianoNote("gg_9", 64, HandType.RIGHT, 7.0f, 1.0f, 2))
        return list
    }

    private fun generateFurEliseNotes(): List<PianoNote> {
        val list = mutableListOf<PianoNote>()
        // E5 - D#5 - E5 - D#5 - E5 - B4 - D5 - C5 - A4
        list.add(PianoNote("fe_1", 76, HandType.RIGHT, 0.0f, 0.5f, 1)) // E5
        list.add(PianoNote("fe_2", 75, HandType.RIGHT, 0.5f, 0.5f, 1)) // D#5
        list.add(PianoNote("fe_3", 76, HandType.RIGHT, 1.0f, 0.5f, 1)) // E5
        list.add(PianoNote("fe_4", 75, HandType.RIGHT, 1.5f, 0.5f, 1)) // D#5
        list.add(PianoNote("fe_5", 76, HandType.RIGHT, 2.0f, 0.5f, 1)) // E5
        list.add(PianoNote("fe_6", 71, HandType.RIGHT, 2.5f, 0.5f, 1)) // B4
        list.add(PianoNote("fe_7", 74, HandType.RIGHT, 3.0f, 0.5f, 1)) // D5
        list.add(PianoNote("fe_8", 72, HandType.RIGHT, 3.5f, 0.5f, 1)) // C5

        list.add(PianoNote("fe_9", 57, HandType.LEFT, 4.0f, 2.0f, 2))  // A3
        list.add(PianoNote("fe_10", 69, HandType.RIGHT, 4.0f, 1.5f, 2)) // A4
        list.add(PianoNote("fe_11", 60, HandType.RIGHT, 5.5f, 0.5f, 2)) // C4
        list.add(PianoNote("fe_12", 64, HandType.RIGHT, 6.0f, 0.5f, 2)) // E4
        list.add(PianoNote("fe_13", 69, HandType.RIGHT, 6.5f, 0.5f, 2)) // A4
        list.add(PianoNote("fe_14", 71, HandType.RIGHT, 7.0f, 1.0f, 2)) // B4
        return list
    }

    private fun generateSoltaneGhalbhaNotes(): List<PianoNote> {
        val list = mutableListOf<PianoNote>()
        list.add(PianoNote("sg_1", 57, HandType.LEFT, 0.0f, 3.0f, 1))
        list.add(PianoNote("sg_2", 69, HandType.RIGHT, 0.0f, 1.0f, 1))
        list.add(PianoNote("sg_3", 72, HandType.RIGHT, 1.0f, 1.0f, 1))
        list.add(PianoNote("sg_4", 76, HandType.RIGHT, 2.0f, 1.0f, 1))
        list.add(PianoNote("sg_5", 53, HandType.LEFT, 3.0f, 3.0f, 2))
        list.add(PianoNote("sg_6", 77, HandType.RIGHT, 3.0f, 2.0f, 2))
        list.add(PianoNote("sg_7", 76, HandType.RIGHT, 5.0f, 1.0f, 2))
        return list
    }

    private fun generateClairDeLuneNotes(): List<PianoNote> {
        val list = mutableListOf<PianoNote>()
        list.add(PianoNote("cdl_1", 65, HandType.RIGHT, 0.0f, 2.0f, 1))
        list.add(PianoNote("cdl_2", 68, HandType.RIGHT, 0.0f, 2.0f, 1))
        list.add(PianoNote("cdl_3", 67, HandType.RIGHT, 2.0f, 1.0f, 1))
        list.add(PianoNote("cdl_4", 65, HandType.RIGHT, 3.0f, 1.0f, 1))
        return list
    }

    private fun generateGymnopedieNotes(): List<PianoNote> {
        val list = mutableListOf<PianoNote>()
        list.add(PianoNote("gym_1", 43, HandType.LEFT, 0.0f, 1.0f, 1)) // G2
        list.add(PianoNote("gym_2", 55, HandType.LEFT, 1.0f, 2.0f, 1)) // G3 Chord
        list.add(PianoNote("gym_3", 59, HandType.LEFT, 1.0f, 2.0f, 1)) // B3
        list.add(PianoNote("gym_4", 71, HandType.RIGHT, 0.0f, 3.0f, 1)) // B4 Melody

        list.add(PianoNote("gym_5", 41, HandType.LEFT, 3.0f, 1.0f, 2)) // F2
        list.add(PianoNote("gym_6", 53, HandType.LEFT, 4.0f, 2.0f, 2)) // F3
        list.add(PianoNote("gym_7", 74, HandType.RIGHT, 3.0f, 3.0f, 2)) // D5
        return list
    }

    private fun generateCanonNotes(): List<PianoNote> {
        val list = mutableListOf<PianoNote>()
        list.add(PianoNote("cn_1", 50, HandType.LEFT, 0.0f, 2.0f, 1)) // D3
        list.add(PianoNote("cn_2", 74, HandType.RIGHT, 0.0f, 2.0f, 1)) // D5
        list.add(PianoNote("cn_3", 45, HandType.LEFT, 2.0f, 2.0f, 1)) // A2
        list.add(PianoNote("cn_4", 73, HandType.RIGHT, 2.0f, 2.0f, 1)) // C#5
        return list
    }
}
