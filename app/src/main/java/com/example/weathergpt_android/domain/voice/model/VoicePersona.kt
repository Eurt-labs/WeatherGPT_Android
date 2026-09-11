package com.example.weathergpt_android.domain.voice.model

import java.util.Locale

/**
 * Model representing an AI voice persona with localized speech samples,
 * gender, articulation styling, and optimal recommendation tags.
 */
data class VoicePersona(
    val id: String,
    val name: String,
    val languageCode: String,
    val languageName: String,
    val gender: String, // "Female" or "Male"
    val styleDescription: String,
    val isOptimal: Boolean = false,
    val samplePhrase: String,
    val pitch: Float = 1.0f,
    val speechRate: Float = 0.95f
) {
    val locale: Locale
        get() = when (languageCode.lowercase()) {
            "hi" -> Locale.forLanguageTag("hi-IN")
            "mr" -> Locale.forLanguageTag("mr-IN")
            "bn" -> Locale.forLanguageTag("bn-IN")
            "ta" -> Locale.forLanguageTag("ta-IN")
            "te" -> Locale.forLanguageTag("te-IN")
            "gu" -> Locale.forLanguageTag("gu-IN")
            else -> Locale.forLanguageTag("en-IN")
        }
}

/**
 * Catalog of high-definition voice personas tailored for Indian languages & English.
 * Guarantees optimal recommended voices for every supported regional tongue.
 */
object VoicePersonaCatalog {

    val allPersonas: List<VoicePersona> = listOf(
        // ─── ENGLISH (en) ────────────────────────────────────────────────────
        VoicePersona(
            id = "en_aoede",
            name = "Aoede",
            languageCode = "en",
            languageName = "English",
            gender = "Female",
            styleDescription = "Warm, melodic, and reassuring Indian articulation",
            isOptimal = true,
            samplePhrase = "Good morning! Clear skies and gentle breezes are expected today with peak temperatures near 32 degrees.",
            pitch = 1.04f,
            speechRate = 0.96f
        ),
        VoicePersona(
            id = "en_charon",
            name = "Charon",
            languageCode = "en",
            languageName = "English",
            gender = "Male",
            styleDescription = "Deep, confident, and authoritative broadcast delivery",
            isOptimal = false,
            samplePhrase = "Advisory update: Barometric pressure is holding steady at 1012 hectopascals across the northern corridor.",
            pitch = 0.88f,
            speechRate = 0.94f
        ),
        VoicePersona(
            id = "en_kore",
            name = "Kore",
            languageCode = "en",
            languageName = "English",
            gender = "Female",
            styleDescription = "Gentle, calm, and soothing clarity",
            isOptimal = false,
            samplePhrase = "Humidity levels are comfortable at 55 percent, making it ideal for outdoor agricultural planning.",
            pitch = 1.08f,
            speechRate = 0.93f
        ),
        VoicePersona(
            id = "en_fenrir",
            name = "Fenrir",
            languageCode = "en",
            languageName = "English",
            gender = "Male",
            styleDescription = "Energetic, dynamic, and focused",
            isOptimal = false,
            samplePhrase = "Wind gusts could reach 22 kilometers per hour by evening. Keep an eye on topsoil evaporation.",
            pitch = 0.94f,
            speechRate = 1.00f
        ),
        VoicePersona(
            id = "en_puck",
            name = "Puck",
            languageCode = "en",
            languageName = "English",
            gender = "Male",
            styleDescription = "Youthful, friendly, and conversational",
            isOptimal = false,
            samplePhrase = "Looks like great weather ahead! No rain in the 24-hour forecast, so you are good to go.",
            pitch = 1.02f,
            speechRate = 1.02f
        ),

        // ─── HINDI (hi) ──────────────────────────────────────────────────────
        VoicePersona(
            id = "hi_ananya",
            name = "Ananya",
            languageCode = "hi",
            languageName = "हिन्दी",
            gender = "Female",
            styleDescription = "प्राकृतिक, स्पष्ट और मधुर हिंदी उच्चारण",
            isOptimal = true,
            samplePhrase = "नमस्ते! आज आपके क्षेत्र में हल्की धूप और सुखद हवा चलने का अनुमान है। फसल सिंचाई के लिए मौसम अनुकूल है।",
            pitch = 1.05f,
            speechRate = 0.94f
        ),
        VoicePersona(
            id = "hi_aarav",
            name = "Aarav",
            languageCode = "hi",
            languageName = "हिन्दी",
            gender = "Male",
            styleDescription = "गंभीर, आत्मविश्वासी और विश्वसनीय वाणी",
            isOptimal = false,
            samplePhrase = "मौसम बुलेटिन: शाम तक उत्तर-पश्चिम से ठंडी हवाएं आ सकती हैं। वायु गुणवत्ता सूचकांक सामान्य स्तर पर रहेगा।",
            pitch = 0.90f,
            speechRate = 0.95f
        ),
        VoicePersona(
            id = "hi_sunita",
            name = "Sunita",
            languageCode = "hi",
            languageName = "हिन्दी",
            gender = "Female",
            styleDescription = "पारंपरिक, सौम्य एवं संवेदनशील आवाज़",
            isOptimal = false,
            samplePhrase = "खेतों में पर्याप्त नमी बनी हुई है। अगले 48 घंटों में भारी बारिश की कोई चेतावनी नहीं है।",
            pitch = 1.02f,
            speechRate = 0.92f
        ),

        // ─── MARATHI (mr) ────────────────────────────────────────────────────
        VoicePersona(
            id = "mr_sai",
            name = "Sai",
            languageCode = "mr",
            languageName = "मराठी",
            gender = "Female",
            styleDescription = "अस्खलित, गोड आणि अस्सल महाराष्ट्रीयन लहेजा",
            isOptimal = true,
            samplePhrase = "नमस्कार! आज दिवसभरात हवामान कोरडे आणि सूर्यप्रकाशित राहील. पिकांसाठी वातावरण उत्तम आहे.",
            pitch = 1.06f,
            speechRate = 0.95f
        ),
        VoicePersona(
            id = "mr_omkar",
            name = "Omkar",
            languageCode = "mr",
            languageName = "मराठी",
            gender = "Male",
            styleDescription = "स्पष्ट, दमदार आणि धीरगंभीर संवाद",
            isOptimal = false,
            samplePhrase = "हवामान इशारा: हवेतील बाष्पाचे प्रमाण 60 टक्के असून रात्री गारवा वाढण्याची शक्यता आहे.",
            pitch = 0.91f,
            speechRate = 0.96f
        ),

        // ─── BENGALI (bn) ────────────────────────────────────────────────────
        VoicePersona(
            id = "bn_shreya",
            name = "Shreya",
            languageCode = "bn",
            languageName = "বাংলা",
            gender = "Female",
            styleDescription = "মিষ্টি, সাবলীল এবং আন্তরিক বাংলা কণ্ঠ",
            isOptimal = true,
            samplePhrase = "নমস্কার! আজকে আপনার অঞ্চলে মনোরম আবহাওয়া থাকবে। বৃষ্টির কোনো পূর্বাভাস নেই।",
            pitch = 1.05f,
            speechRate = 0.94f
        ),
        VoicePersona(
            id = "bn_ishan",
            name = "Ishan",
            languageCode = "bn",
            languageName = "বাংলা",
            gender = "Male",
            styleDescription = "পরিমার্জিত, বলিষ্ঠ ও প্রামাণিক বাচনভঙ্গি",
            isOptimal = false,
            samplePhrase = "আবহাওয়া আপডেট: নদীর জলস্তর এবং মাটির আর্দ্রতা বর্তমানে সম্পূর্ণ স্বাভাবিক রয়েছে।",
            pitch = 0.90f,
            speechRate = 0.95f
        ),

        // ─── TAMIL (ta) ──────────────────────────────────────────────────────
        VoicePersona(
            id = "ta_madhavi",
            name = "Madhavi",
            languageCode = "ta",
            languageName = "தமிழ்",
            gender = "Female",
            styleDescription = "தெளிவான, மென்மையான மற்றும் இனிமையான தமிழ் உச்சரிப்பு",
            isOptimal = true,
            samplePhrase = "வணக்கம்! இன்று உங்கள் பகுதியில் இதமான வானிலையும் மிதமான காற்றும் வீசக்கூடும்.",
            pitch = 1.04f,
            speechRate = 0.94f
        ),
        VoicePersona(
            id = "ta_karthik",
            name = "Karthik",
            languageCode = "ta",
            languageName = "தமிழ்",
            gender = "Male",
            styleDescription = "துடிப்பான, நம்பிக்கையான மற்றும் தெளிவான பேச்சு",
            isOptimal = false,
            samplePhrase = "வானிலை தகவல்: அடுத்த 24 மணி நேரத்தில் மழைக்கான வாய்ப்பு குறைவு. விவசாய பணிகளை தொடரலாம்.",
            pitch = 0.92f,
            speechRate = 0.96f
        ),

        // ─── TELUGU (te) ─────────────────────────────────────────────────────
        VoicePersona(
            id = "te_pallavi",
            name = "Pallavi",
            languageCode = "te",
            languageName = "తెలుగు",
            gender = "Female",
            styleDescription = "స్పష్టమైన, శ్రావ్యమైన మరియు సహజమైన తెలుగు పలుకు",
            isOptimal = true,
            samplePhrase = "నమస్కారం! ఈరోజు మీ ప్రాంతంలో వాతావరణం పొడిగా మరియు ఆహ్లాదకరంగా ఉంటుంది.",
            pitch = 1.05f,
            speechRate = 0.94f
        ),
        VoicePersona(
            id = "te_vamsi",
            name = "Vamsi",
            languageCode = "te",
            languageName = "తెలుగు",
            gender = "Male",
            styleDescription = "గంభీరమైన మరియు విశ్వసనీయమైన గొంతు",
            isOptimal = false,
            samplePhrase = "వాతావరణ నివేదిక: నేల తేమ అనుకూలంగా ఉంది, ప్రస్తుతానికి ఎటువంటి తుఫాను హెచ్చరికలు లేవు.",
            pitch = 0.91f,
            speechRate = 0.95f
        ),

        // ─── GUJARATI (gu) ───────────────────────────────────────────────────
        VoicePersona(
            id = "gu_dhriti",
            name = "Dhriti",
            languageCode = "gu",
            languageName = "ગુજરાતી",
            gender = "Female",
            styleDescription = "સ્પષ્ટ, મધુર અને પ્રભાવશાળી ગુજરાતી લહેકો",
            isOptimal = true,
            samplePhrase = "નમસ્તે! આજે તમારા વિસ્તારમાં વાતાવરણ ખુશનુમા રહેશે અને વરસાદની સંભાવના નહિવત છે.",
            pitch = 1.05f,
            speechRate = 0.95f
        ),
        VoicePersona(
            id = "gu_bhavin",
            name = "Bhavin",
            languageCode = "gu",
            languageName = "ગુજરાતી",
            gender = "Male",
            styleDescription = "મક્કમ, શાંત અને વિશ્વાસપાત્ર વાણી",
            isOptimal = false,
            samplePhrase = "હવામાન વિશેષ: પવનની ગતિ સામાન્ય રહેશે અને ખેતી કાર્યો માટે આજનો દિવસ ઉત્તમ છે.",
            pitch = 0.90f,
            speechRate = 0.96f
        )
    )

    fun getVoicesForLanguage(langCode: String): List<VoicePersona> {
        val matches = allPersonas.filter { it.languageCode.equals(langCode, ignoreCase = true) }
        return matches.ifEmpty { allPersonas.filter { it.languageCode == "en" } }
    }

    fun getOptimalVoiceForLanguage(langCode: String): VoicePersona {
        val voices = getVoicesForLanguage(langCode)
        return voices.firstOrNull { it.isOptimal } ?: voices.firstOrNull() ?: allPersonas.first()
    }

    fun getVoiceById(id: String): VoicePersona? {
        return allPersonas.firstOrNull { it.id.equals(id, ignoreCase = true) }
    }
}
