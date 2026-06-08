package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CvRepository
import com.example.data.GeminiParser
import com.example.data.PreferenceManager
import com.example.model.CvData
import com.example.model.Education
import com.example.model.WorkExperience
import com.example.util.PdfGenerator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

sealed interface Screen {
    object Splash : Screen
    object Home : Screen
    object Paywall : Screen
    object TemplateSelect : Screen
    object Builder : Screen
    object Preview : Screen
    object Editor : Screen
    object Settings : Screen
    data class PayfastCheckout(val planName: String, val amount: Double, val frequencyId: Int) : Screen
}

class CvViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = CvRepository(db.cvDao())
    private val prefManager = PreferenceManager(application)

    // Flow of all local CV profiles saved in database
    val cvList: StateFlow<List<CvData>> = repository.allCvs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Routing / Navigation State
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Subscription & Ads state
    private val _isPremium = MutableStateFlow(prefManager.isPremium)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _subscriptionPlan = MutableStateFlow(prefManager.subscriptionPlan)
    val subscriptionPlan: StateFlow<String> = _subscriptionPlan.asStateFlow()

    private val _credits = MutableStateFlow(prefManager.creationCredits)
    val credits: StateFlow<Int> = _credits.asStateFlow()

    // Currently active CV profile being created or edited
    private val _selectedCv = MutableStateFlow<CvData?>(null)
    val selectedCv: StateFlow<CvData?> = _selectedCv.asStateFlow()

    // Form editing states
    private val _activeTab = MutableStateFlow(0) // 0: Personal, 1: Experience, 2: Education, 3: Skills, 4: Summary/Preview
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    // Rewarded Ad Simulation states
    private val _isAdPlaying = MutableStateFlow(false)
    val isAdPlaying: StateFlow<Boolean> = _isAdPlaying.asStateFlow()

    private val _adCountdown = MutableStateFlow(5)
    val adCountdown: StateFlow<Int> = _adCountdown.asStateFlow()

    // Pending navigation action to resume after successful Ad completion
    private var pendingAdAction: (() -> Unit)? = null

    // Gemini Parsing States
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanError = MutableStateFlow<String?>(null)
    val scanError: StateFlow<String?> = _scanError.asStateFlow()

    init {
        // Pre-populate some awesome demo entries if local database is empty to deliver pristine visuals out-of-the-box
        viewModelScope.launch {
            cvList.collect { list ->
                if (list.isEmpty()) {
                    createDefaultModelData()
                }
            }
        }
    }

    private suspend fun createDefaultModelData() {
        val johnDoe = CvData(
            name = "John Doe",
            title = "Senior Software Engineer",
            email = "johndoe@email.com",
            phone = "+1 (555) 019-2834",
            location = "San Francisco, CA",
            linkedin = "linkedin.com/in/johndoe",
            summary = "Passionate, impact-focused lead engineer with 8+ years designing scalable cloud backends, microservice fabrics, and modular web interfaces. Champion of product quality, unit testing, and elegant design architectures.",
            experience = listOf(
                WorkExperience(
                    company = "Google",
                    title = "Senior Software Engineer",
                    startDate = "Sep 2022",
                    endDate = "Present",
                    description = "Lead engineering design streams for core Google Cloud storage interfaces. Mentored 4 junior developers and optimized microservice throughput by 34%."
                ),
                WorkExperience(
                    company = "TechCorp Solutions",
                    title = "Full Stack Developer",
                    startDate = "Jun 2018",
                    endDate = "Aug 2022",
                    description = "Architected offline-first client dashboards using Kotlin, Compose, and Room. Built reusable UI blocks decreasing design-to-production lifecycle by 25%."
                )
            ),
            education = listOf(
                Education(
                    institution = "Stanford University",
                    degree = "Bachelor of Science in Computer Science",
                    year = "2018"
                )
            ),
            skills = listOf("Kotlin", "Jetpack Compose", "Android SDK", "Room DB", "REST APIs", "Microservices", "System Design"),
            templateId = "modern_prof"
        )

        val janeSmith = CvData(
            name = "Jane Smith",
            title = "Lead Graphic Designer",
            email = "janesmith@creative.io",
            phone = "+1 (555) 012-9988",
            location = "New York, NY",
            linkedin = "linkedin.com/in/janesmith",
            summary = "Award-winning lead creative director with 6+ years driving visual identity projects, digital graphics workflows, and visual-first mobile app animations. Expert in Material Design, layouts, and typography branding.",
            experience = listOf(
                WorkExperience(
                    company = "Aesthetic Studio",
                    title = "Art Director",
                    startDate = "Jan 2021",
                    endDate = "Present",
                    description = "Formulated visual design languages for 20+ global high-tech and food brands. Managed creative layouts, typography books, and icon systems."
                )
            ),
            education = listOf(
                Education(
                    institution = "Pratt Institute",
                    degree = "Master of Fine Arts in Communication Design",
                    year = "纽约, 2020"
                )
            ),
            skills = listOf("UI/UX Design", "Material Design 3", "Brand Identity", "Motion Graphics", "Adobe Suite", "Typography pairing", "Illustration"),
            templateId = "classic_elegance"
        )

        val ahmedKhan = CvData(
            name = "Ahmed Khan",
            title = "Technical Project Manager",
            email = "ahmed.khan@techops.org",
            phone = "+971 4 123 4567",
            location = "Dubai, UAE",
            linkedin = "linkedin.com/in/ahmedkhan",
            summary = "Technical Program Manager with extensive experience directing agile software teams, cross-department operations, and enterprise delivery timelines in multi-vendor environments.",
            experience = listOf(
                WorkExperience(
                    company = "Delta Fintech",
                    title = "Agile Project Coordinator",
                    startDate = "Mar 2023",
                    endDate = "Present",
                    description = "Spearheaded agile scrum cycles for 3 development squads. Reduced software deployment friction through structured DevOps integration schedules."
                )
            ),
            education = listOf(
                Education(
                    institution = "American University of Sharjah",
                    degree = "Bachelor of Engineering in Computer Engineering",
                    year = "2023"
                )
            ),
            skills = listOf("Scrum / Agile", "Technical PM", "DevOps Pipelines", "Risk Analysis", "Resource Allocation", "Communication"),
            templateId = "creative_portfolio"
        )

        repository.insertCv(johnDoe)
        repository.insertCv(janeSmith)
        repository.insertCv(ahmedKhan)
    }

    // Navigation Utilities
    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    // Settings adjustments
    fun setPremiumStatus(status: Boolean, planName: String = "") {
        prefManager.isPremium = status
        _isPremium.value = status
        prefManager.subscriptionPlan = if (status) planName else ""
        _subscriptionPlan.value = if (status) planName else ""
    }

    fun addAdCredits(amount: Int) {
        prefManager.addCredit(amount)
        _credits.value = prefManager.creationCredits
    }

    // Core Actions check ad requirements
    fun checkAdAndProceed(action: () -> Unit) {
        if (_isPremium.value || prefManager.creationCredits > 0) {
            // Deduct credit if not premium
            if (!_isPremium.value) {
                prefManager.useCredit()
                _credits.value = prefManager.creationCredits
            }
            action()
        } else {
            // Must watch ad
            pendingAdAction = action
            navigateTo(Screen.Paywall)
        }
    }

    // Simulate standard AdMob rewarded ad playing
    fun playSimulatedAd() {
        viewModelScope.launch {
            _isAdPlaying.value = true
            _adCountdown.value = 5
            
            while (_adCountdown.value > 0) {
                delay(1000)
                _adCountdown.value = _adCountdown.value - 1
            }
            
            // Ad Watched fully (Reward granted)
            _isAdPlaying.value = false
            
            // Grant 1 credit automatically
            prefManager.addCredit(1)
            _credits.value = prefManager.creationCredits
            
            // Execute the pending action
            val pending = pendingAdAction
            if (pending != null) {
                prefManager.useCredit()
                _credits.value = prefManager.creationCredits
                pending.invoke()
                pendingAdAction = null
            } else {
                // If they watched directly from Paywall management
                navigateTo(Screen.Home)
            }
        }
    }

    // Play either a real AdMob Rewarded Ad or gracefully fallback to high-fidelity simulated progress ad
    fun playRealOrSimulatedAd(activity: android.app.Activity) {
        if (com.example.util.AdMobHelper.isAdLoaded()) {
            com.example.util.AdMobHelper.showRewardedAd(
                activity = activity,
                onRewardEarned = {
                    _isAdPlaying.value = false
                    prefManager.addCredit(1)
                    _credits.value = prefManager.creationCredits
                    
                    val pending = pendingAdAction
                    if (pending != null) {
                        prefManager.useCredit()
                        _credits.value = prefManager.creationCredits
                        pending.invoke()
                        pendingAdAction = null
                    } else {
                        navigateTo(Screen.Home)
                    }
                },
                onAdDismissed = {
                    com.example.util.AdMobHelper.loadRewardedAd(activity)
                }
            )
        } else {
            // Keep user experience amazing with high-fidelity local ad simulator fallback
            playSimulatedAd()
        }
    }

    // CV CRUD Handlers
    fun selectCvForEditing(cv: CvData?) {
        _selectedCv.value = cv ?: CvData()
        _activeTab.value = 0
    }

    fun updateSelectedCv(updated: CvData) {
        _selectedCv.value = updated
    }

    fun saveCurrentCv() {
        val current = _selectedCv.value ?: return
        val finalCv = current.copy(lastModified = System.currentTimeMillis())
        
        viewModelScope.launch {
            if (finalCv.id == 0L) {
                repository.insertCv(finalCv)
            } else {
                repository.updateCv(finalCv)
            }
            // Clear current selection and return to home
            _selectedCv.value = null
            navigateTo(Screen.Home)
        }
    }

    fun deleteCv(cv: CvData) {
        viewModelScope.launch {
            repository.deleteCv(cv)
        }
    }

    fun changeActiveTab(index: Int) {
        _activeTab.value = index
    }

    // PDF generation trigger
    fun generatePdfFile(context: Context, cv: CvData): File? {
        return PdfGenerator.generateCvPdf(context, cv, _isPremium.value)
    }

    // AI Scanner via Gemini API
    fun parseResumeWithAi(rawResumeText: String) {
        if (rawResumeText.isBlank()) return
        
        _isScanning.value = true
        _scanError.value = null

        viewModelScope.launch {
            val parsedCv = GeminiParser.parseResumeText(rawResumeText)
            if (parsedCv != null) {
                _selectedCv.value = parsedCv
                _isScanning.value = false
                _activeTab.value = 0
                navigateTo(Screen.Builder)
            } else {
                _isScanning.value = false
                // Check if API key is missing
                val apiKey = try {
                    com.example.BuildConfig.GEMINI_API_KEY
                } catch (e: Exception) {
                    ""
                }
                
                if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                    _scanError.value = "AI key is missing! Please configure the GEMINI_API_KEY in the Secrets Panel in AI Studio."
                } else {
                    _scanError.value = "Failed to parse text. Please ensure the formatting is legible and try again."
                }
            }
        }
    }
}
