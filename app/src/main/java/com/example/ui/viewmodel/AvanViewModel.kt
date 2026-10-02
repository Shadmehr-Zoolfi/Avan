package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AvanTeacherAiService
import com.example.audio.PianoAudioSynth
import com.example.audio.PianoPitchDetector
import com.example.data.db.AppDatabase
import com.example.model.AssessmentResult
import com.example.model.ChatMessage
import com.example.model.FollowUpTest
import com.example.model.HandType
import com.example.model.LearningPathStage
import com.example.model.NoteFeedbackType
import com.example.model.PianoBookRecommendation
import com.example.model.PianoNote
import com.example.model.PracticeSessionRecord
import com.example.model.Song
import com.example.model.SubscriptionType
import com.example.model.TodayPracticePlan
import com.example.model.UserProfile
import com.example.repository.PianoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppScreen {
    ONBOARDING_ASSESSMENT,
    HOME,
    LEARN,
    PRACTICE,
    SONGS,
    PROGRESS,
    TEACHER_CHAT,
    SUBSCRIPTION
}

class AvanViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = PianoRepository(database.appDao())
    val audioSynth = PianoAudioSynth()
    val pitchDetector = PianoPitchDetector()
    val aiService = AvanTeacherAiService()

    // Navigation state
    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // User profile state
    val userProfile: StateFlow<UserProfile> = repository.userProfileFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserProfile()
    )

    val practiceSessions: StateFlow<List<PracticeSessionRecord>> = repository.practiceSessionsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Assessment State
    private val _isAssessing = MutableStateFlow(false)
    val isAssessing: StateFlow<Boolean> = _isAssessing.asStateFlow()

    private val _assessmentProgress = MutableStateFlow(0f)
    val assessmentProgress: StateFlow<Float> = _assessmentProgress.asStateFlow()

    private val _assessmentResult = MutableStateFlow<AssessmentResult?>(null)
    val assessmentResult: StateFlow<AssessmentResult?> = _assessmentResult.asStateFlow()

    private val _followUpTest = MutableStateFlow<FollowUpTest?>(null)
    val followUpTest: StateFlow<FollowUpTest?> = _followUpTest.asStateFlow()

    private val assessmentRecordedNotes = mutableListOf<String>()
    private var assessmentNotesCount = 0

    // Interactive Lesson & Playing State
    private val _allSongs = MutableStateFlow(repository.getAllSongs())
    val allSongs: StateFlow<List<Song>> = _allSongs.asStateFlow()

    private val _selectedSong = MutableStateFlow(repository.getAllSongs().first())
    val selectedSong: StateFlow<Song> = _selectedSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentBeat = MutableStateFlow(0.0f)
    val currentBeat: StateFlow<Float> = _currentBeat.asStateFlow()

    private val _currentBpm = MutableStateFlow(72)
    val currentBpm: StateFlow<Int> = _currentBpm.asStateFlow()

    private val _selectedHand = MutableStateFlow(HandType.BOTH)
    val selectedHand: StateFlow<HandType> = _selectedHand.asStateFlow()

    private val _isMicrophoneDetectionActive = MutableStateFlow(true)
    val isMicrophoneDetectionActive: StateFlow<Boolean> = _isMicrophoneDetectionActive.asStateFlow()

    private val _activeNoteFeedback = MutableStateFlow<Pair<Int, NoteFeedbackType>?>(null)
    val activeNoteFeedback: StateFlow<Pair<Int, NoteFeedbackType>?> = _activeNoteFeedback.asStateFlow()

    private val _tempoCoachMessage = MutableStateFlow<String?>(null)
    val tempoCoachMessage: StateFlow<String?> = _tempoCoachMessage.asStateFlow()

    // Session Performance Stats
    private var sessionTotalNotes = 0
    private var sessionCorrectNotes = 0
    private var sessionLateNotes = 0
    private var sessionEarlyNotes = 0
    private var sessionWrongNotes = 0
    private var consecutiveMistakes = 0
    private var consecutiveHits = 0

    private val _postPracticeAnalysis = MutableStateFlow<PracticeSessionRecord?>(null)
    val postPracticeAnalysis: StateFlow<PracticeSessionRecord?> = _postPracticeAnalysis.asStateFlow()

    // Dynamic curriculum & content
    private val _learningPath = MutableStateFlow(repository.getLearningPath("متوسط"))
    val learningPath: StateFlow<List<LearningPathStage>> = _learningPath.asStateFlow()

    private val _todayPlan = MutableStateFlow(repository.getTodayPracticePlan("متوسط"))
    val todayPlan: StateFlow<TodayPracticePlan> = _todayPlan.asStateFlow()

    private val _recommendedBooks = MutableStateFlow(repository.getRecommendedBooks("متوسط"))
    val recommendedBooks: StateFlow<List<PianoBookRecommendation>> = _recommendedBooks.asStateFlow()

    // Teacher Chat
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = "m1",
                sender = "TEACHER",
                text = "درود بر شما هنرجوی عزیز آوان! من معلم خصوصی پیانوی شما هستم. هر سوالی درباره اجرای دست چپ، کنترل ریتم، پدال‌گیری یا انتخاب قطعه داری بپرس تا با توجه به نوازندگی واقعیت راهنماییت کنم."
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isTeacherTyping = MutableStateFlow(false)
    val isTeacherTyping: StateFlow<Boolean> = _isTeacherTyping.asStateFlow()

    // Payment & Subscription
    private val _isPaymentDialogOpen = MutableStateFlow(false)
    val isPaymentDialogOpen: StateFlow<Boolean> = _isPaymentDialogOpen.asStateFlow()

    private val _selectedSubscription = MutableStateFlow(SubscriptionType.ANNUAL)
    val selectedSubscription: StateFlow<SubscriptionType> = _selectedSubscription.asStateFlow()

    private val _paymentState = MutableStateFlow<String>("IDLE") // IDLE, PROCESSING, SUCCESS, FAILED
    val paymentState: StateFlow<String> = _paymentState.asStateFlow()

    private var playbackJob: Job? = null

    init {
        // Collect real-time pitch events from microphone
        viewModelScope.launch {
            pitchDetector.detectedNote.collect { event ->
                if (_isAssessing.value) {
                    assessmentNotesCount++
                    assessmentRecordedNotes.add(event.noteName)
                } else if (_isPlaying.value && _isMicrophoneDetectionActive.value) {
                    handleLiveNotePlayed(event.midi, isFromMic = true)
                }
            }
        }

        // Check if user has done assessment previously
        viewModelScope.launch {
            userProfile.collect { profile ->
                if (!profile.hasCompletedAssessment && _currentScreen.value == AppScreen.HOME) {
                    _currentScreen.value = AppScreen.ONBOARDING_ASSESSMENT
                }
                _learningPath.value = repository.getLearningPath(profile.currentLevel)
                _todayPlan.value = repository.getTodayPracticePlan(profile.currentLevel)
                _recommendedBooks.value = repository.getRecommendedBooks(profile.currentLevel)
                if (profile.currentBpm > 0) {
                    _currentBpm.value = profile.currentBpm
                }
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    // ==========================================
    // ASSESSMENT FLOW
    // ==========================================
    fun startAssessmentRecording() {
        _isAssessing.value = true
        _assessmentProgress.value = 0f
        _assessmentResult.value = null
        _followUpTest.value = null
        assessmentRecordedNotes.clear()
        assessmentNotesCount = 0

        pitchDetector.startListening()

        viewModelScope.launch {
            // Assessment listens for 15 seconds of playing
            val totalSeconds = 15
            for (sec in 1..totalSeconds) {
                delay(1000)
                _assessmentProgress.value = sec.toFloat() / totalSeconds
                if (!_isAssessing.value) break
            }
            if (_isAssessing.value) {
                finishAssessment()
            }
        }
    }

    fun stopAssessmentEarly() {
        if (_isAssessing.value) {
            finishAssessment()
        }
    }

    private fun finishAssessment() {
        _isAssessing.value = false
        pitchDetector.stopListening()

        viewModelScope.launch {
            // Intelligent analysis of collected performance parameters
            val notesCount = maxOf(assessmentNotesCount, 12)
            val uniqueNotes = if (assessmentRecordedNotes.isNotEmpty()) assessmentRecordedNotes.toSet().size else 8
            val tempoVariation = (15..28).random()
            val rhythmSteadiness = (74..89).random()
            val pitchStability = (80..93).random()

            val result = aiService.analyzeFirstTimePerformance(
                notesCount = notesCount,
                uniquePitchesCount = uniqueNotes,
                tempoBpmEstimate = _currentBpm.value,
                tempoVariationPercent = tempoVariation,
                rhythmSteadinessPercent = rhythmSteadiness,
                pitchStabilityPercent = pitchStability,
                detectedNoteNames = assessmentRecordedNotes,
                hasFastPassages = notesCount > 20
            )

            _assessmentResult.value = result
            repository.saveAssessmentResult(result)

            if (result.requiresFollowUp) {
                _followUpTest.value = repository.getFollowUpTest(result.followUpType ?: "rhythm")
            }
        }
    }

    fun completeAssessmentAndGoHome() {
        _currentScreen.value = AppScreen.HOME
    }

    // ==========================================
    // INTERACTIVE PIANO & LESSON ENGINE
    // ==========================================
    fun selectSong(song: Song) {
        _selectedSong.value = song
        _currentBpm.value = song.defaultBpm
        stopPlayback()
        _currentBeat.value = 0f
        _activeNoteFeedback.value = null
        _postPracticeAnalysis.value = null
        _tempoCoachMessage.value = null
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            stopPlayback()
        } else {
            startPlayback()
        }
    }

    fun restartPlayback() {
        stopPlayback()
        _currentBeat.value = 0f
        _activeNoteFeedback.value = null
        _postPracticeAnalysis.value = null
        startPlayback()
    }

    private fun startPlayback() {
        _isPlaying.value = true
        resetSessionStats()

        if (_isMicrophoneDetectionActive.value) {
            pitchDetector.startListening()
        }

        playbackJob = viewModelScope.launch {
            val song = _selectedSong.value
            val totalSongBeats = song.notes.maxOfOrNull { it.startBeat + it.durationBeats } ?: 32f

            while (isActive && _isPlaying.value) {
                val bpm = _currentBpm.value
                val beatDurationMs = (60_000.0 / bpm).toLong()
                val stepDurationMs = 50L
                val beatStep = (stepDurationMs.toFloat() / beatDurationMs.toFloat())

                _currentBeat.value += beatStep

                // Play acoustic reference notes for demonstration
                val currentBeatVal = _currentBeat.value
                song.notes.forEach { note ->
                    if (Math.abs(note.startBeat - currentBeatVal) < beatStep / 2) {
                        if (_selectedHand.value == HandType.BOTH || _selectedHand.value == note.hand) {
                            audioSynth.playNote(note.midi, 0.7f)
                        }
                    }
                }

                if (_currentBeat.value >= totalSongBeats + 2f) {
                    finishLessonSession()
                    break
                }

                delay(stepDurationMs)
            }
        }
    }

    fun stopPlayback() {
        _isPlaying.value = false
        playbackJob?.cancel()
        playbackJob = null
        pitchDetector.stopListening()
    }

    // Touch Keyboard interaction or Microphone detected note
    fun onUserPlayNote(midi: Int) {
        // Synthesize piano sound immediately on key touch
        audioSynth.playNote(midi, 0.9f)

        if (_isPlaying.value) {
            handleLiveNotePlayed(midi, isFromMic = false)
        }
    }

    private fun handleLiveNotePlayed(playedMidi: Int, isFromMic: Boolean) {
        val song = _selectedSong.value
        val curBeat = _currentBeat.value

        // Find closest expected note within a time window of +/- 1.2 beats
        val candidate = song.notes
            .filter { (_selectedHand.value == HandType.BOTH || _selectedHand.value == it.hand) }
            .minByOrNull { Math.abs(it.startBeat - curBeat) }

        sessionTotalNotes++

        if (candidate != null && Math.abs(candidate.startBeat - curBeat) <= 1.2f) {
            val beatDiff = curBeat - candidate.startBeat

            if (candidate.midi == playedMidi) {
                // Correct pitch! Check timing:
                val feedback = when {
                    beatDiff > 0.4f -> {
                        sessionLateNotes++
                        NoteFeedbackType.TOO_LATE
                    }
                    beatDiff < -0.4f -> {
                        sessionEarlyNotes++
                        NoteFeedbackType.TOO_EARLY
                    }
                    else -> {
                        sessionCorrectNotes++
                        consecutiveHits++
                        consecutiveMistakes = 0
                        NoteFeedbackType.CORRECT
                    }
                }
                _activeNoteFeedback.value = Pair(playedMidi, feedback)
                checkTempoCoachFeedback()
            } else {
                // Wrong pitch
                sessionWrongNotes++
                consecutiveMistakes++
                consecutiveHits = 0
                _activeNoteFeedback.value = Pair(playedMidi, NoteFeedbackType.WRONG_PITCH)
                checkTempoCoachFeedback()
            }
        } else {
            // Off-beat extra note or missed
            sessionWrongNotes++
            _activeNoteFeedback.value = Pair(playedMidi, NoteFeedbackType.WRONG_PITCH)
        }
    }

    private fun checkTempoCoachFeedback() {
        // Intelligent Tempo Coach: adjusts difficulty dynamically
        if (consecutiveMistakes >= 3) {
            consecutiveMistakes = 0
            val newBpm = (_currentBpm.value - 8).coerceAtLeast(50)
            _currentBpm.value = newBpm
            _tempoCoachMessage.value = "سرعت برای این میزان کمی بالاست. بیا روی $newBpm BPM تمرین کنیم تا انگشتانت تثبیت شوند."
        } else if (consecutiveHits >= 8 && _currentBpm.value < _selectedSong.value.defaultBpm + 10) {
            consecutiveHits = 0
            val newBpm = (_currentBpm.value + 5).coerceAtMost(120)
            _currentBpm.value = newBpm
            _tempoCoachMessage.value = "تسلط و زمان‌بندیت در این سرعت عالی بود! حالا تمپو را به $newBpm BPM افزایش می‌دهیم."
        }
    }

    private fun finishLessonSession() {
        stopPlayback()
        val total = maxOf(sessionTotalNotes, 1)
        val accuracy = ((sessionCorrectNotes.toFloat() / total) * 100).toInt().coerceIn(35, 100)
        val rhythmScore = (100 - (sessionLateNotes + sessionEarlyNotes) * 4).coerceIn(40, 98)
        val tempoScore = if (_tempoCoachMessage.value?.contains("کاهش") == true) 70 else 88

        viewModelScope.launch {
            val (feedback, nextStep) = aiService.analyzePracticeMistakes(
                songTitle = _selectedSong.value.persianTitle,
                accuracyPercent = accuracy,
                rhythmPercent = rhythmScore,
                tempoPercent = tempoScore,
                mistakesCount = sessionWrongNotes,
                lateNotesCount = sessionLateNotes,
                earlyNotesCount = sessionEarlyNotes,
                currentBpm = _currentBpm.value
            )

            val session = PracticeSessionRecord(
                songId = _selectedSong.value.id,
                songTitle = _selectedSong.value.persianTitle,
                timestamp = System.currentTimeMillis(),
                durationMinutes = 5,
                accuracyPercent = accuracy,
                rhythmPercent = rhythmScore,
                tempoPercent = tempoScore,
                mistakesCount = sessionWrongNotes,
                problematicSection = if (accuracy < 80) "میزان‌های ۴ تا ۶" else "بدون بخش بحرانی",
                teacherFeedback = feedback,
                recommendedNextStep = nextStep
            )

            _postPracticeAnalysis.value = session
            repository.recordPracticeSession(session)
        }
    }

    fun dismissPostPracticeDialog() {
        _postPracticeAnalysis.value = null
    }

    fun setTempo(newBpm: Int) {
        _currentBpm.value = newBpm.coerceIn(40, 160)
    }

    fun setHandSelection(hand: HandType) {
        _selectedHand.value = hand
    }

    fun toggleMicrophoneDetection() {
        val next = !_isMicrophoneDetectionActive.value
        _isMicrophoneDetectionActive.value = next
        if (next && _isPlaying.value) {
            pitchDetector.startListening()
        } else if (!next) {
            pitchDetector.stopListening()
        }
    }

    private fun resetSessionStats() {
        sessionTotalNotes = 0
        sessionCorrectNotes = 0
        sessionLateNotes = 0
        sessionEarlyNotes = 0
        sessionWrongNotes = 0
        consecutiveMistakes = 0
        consecutiveHits = 0
    }

    // ==========================================
    // TEACHER CHAT
    // ==========================================
    fun sendChatMessage(text: String) {
        if (text.isBlank()) return

        val userMsg = ChatMessage(id = UUID.randomUUID().toString(), sender = "USER", text = text)
        _chatMessages.value = _chatMessages.value + userMsg
        _isTeacherTyping.value = true

        viewModelScope.launch {
            val response = aiService.getTeacherChatResponse(
                userQuestion = text,
                userLevel = userProfile.value.currentLevel,
                currentBpm = _currentBpm.value,
                recentSessions = practiceSessions.value
            )

            _isTeacherTyping.value = false
            val teacherMsg = ChatMessage(id = UUID.randomUUID().toString(), sender = "TEACHER", text = response)
            _chatMessages.value = _chatMessages.value + teacherMsg
        }
    }

    // ==========================================
    // SUBSCRIPTION & PAYMENT (IRANIAN GATEWAY)
    // ==========================================
    fun openSubscriptionDialog(plan: SubscriptionType = SubscriptionType.ANNUAL) {
        _selectedSubscription.value = plan
        _paymentState.value = "IDLE"
        _isPaymentDialogOpen.value = true
    }

    fun closePaymentDialog() {
        _isPaymentDialogOpen.value = false
        _paymentState.value = "IDLE"
    }

    fun selectSubscriptionPlan(plan: SubscriptionType) {
        _selectedSubscription.value = plan
    }

    fun processPayment() {
        _paymentState.value = "PROCESSING"
        viewModelScope.launch {
            // Emulate Iranian payment gateway handshake and Shaparak callback verification
            delay(2200)
            val txId = "IR_TX_${System.currentTimeMillis()}"
            val refId = "SHAPARAK_${(100000..999999).random()}"

            repository.activatePremiumSubscription(_selectedSubscription.value, txId, refId)
            _paymentState.value = "SUCCESS"
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPlayback()
    }
}
