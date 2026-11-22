package com.sahayak.app

data class GuideStep(
    val stepNumber: Int,
    val screenLabel: String,
    val instructionEn: String,
    val instructionHi: String,
    val targetTextHint: String,
    val isSecureSensitive: Boolean = false
)

data class GuideWorkflow(
    val id: String,
    val title: String,
    val appPackage: String,
    val appName: String,
    val steps: List<GuideStep>
)

data class EvalConfig(
    var confidenceThreshold: Int = 80,
    var hesitationTimeoutSec: Int = 12,
    var autoFallbackOnFlagSecure: Boolean = true,
    var simplifyLanguage: Boolean = true
)

object DemoGuides {
    val workflows = listOf(
        GuideWorkflow(
            id = "whatsapp_story",
            title = "Add & Delete a Story (Status) in WhatsApp",
            appPackage = "com.whatsapp",
            appName = "WhatsApp",
            steps = listOf(
                GuideStep(1, "Chats Home", "Tap the 'Updates' tab at the bottom left.", "नीचे बाईं ओर 'Updates' टैब दबाएं।", "Updates"),
                GuideStep(2, "Updates Tab", "Tap 'My status' with the + icon to add a photo or video.", "फोटो जोड़ने के लिए '+' वाले 'My status' पर दबाएं।", "My status"),
                GuideStep(3, "Status Camera", "Tap the green Send button at the bottom right to post.", "पोस्ट करने के लिए नीचे दाईं ओर हरा Send बटन दबाएं।", "Send"),
                GuideStep(4, "Delete Story", "Open 'My status', tap the 3-dots menu, and tap 'Delete'.", "'My status' खोलें, 3-डॉट मेनू दबाएं और 'Delete' चुनें।", "Delete")
            )
        ),
        GuideWorkflow(
            id = "instagram_song",
            title = "Add a Song to a Post in Instagram",
            appPackage = "com.instagram.android",
            appName = "Instagram",
            steps = listOf(
                GuideStep(1, "Home Feed", "Tap the '+' Create button at the bottom center.", "नीचे बीच में '+' Create बटन दबाएं।", "Create"),
                GuideStep(2, "Gallery", "Select your photo and tap 'Next' at the top right.", "अपनी फोटो चुनें और ऊपर दाईं ओर 'Next' दबाएं।", "Next"),
                GuideStep(3, "Edit & Audio", "Tap 'Add music' (♪ icon) and search for a song.", "'Add music' (♪) दबाएं और अपना पसंदीदा गाना चुनें।", "Add music"),
                GuideStep(4, "Caption & Share", "Tap the blue 'Share' button at the bottom to post.", "पोस्ट करने के लिए नीचे नीला 'Share' बटन दबाएं।", "Share")
            )
        ),
        GuideWorkflow(
            id = "phonepe_qr",
            title = "Find & Share Your QR Code in PhonePe",
            appPackage = "com.phonepe.app",
            appName = "PhonePe",
            steps = listOf(
                GuideStep(1, "PhonePe Home", "Tap your Profile / QR icon at the top left corner.", "ऊपर बाईं ओर अपनी प्रोफ़ाइल / QR आइकन दबाएं।", "Profile"),
                GuideStep(2, "Profile & Settings", "Tap 'QR Codes' to open your personal UPI QR.", "अपना UPI QR देखने के लिए 'QR Codes' दबाएं।", "QR Codes", isSecureSensitive = true),
                GuideStep(3, "My QR Screen", "Tap 'Share QR' below your code to send it on WhatsApp.", "WhatsApp पर भेजने के लिए नीचे 'Share QR' दबाएं।", "Share QR", isSecureSensitive = true)
            )
        )
    )

    fun findById(id: String?): GuideWorkflow {
        return workflows.firstOrNull { it.id == id } ?: workflows.first()
    }
}
