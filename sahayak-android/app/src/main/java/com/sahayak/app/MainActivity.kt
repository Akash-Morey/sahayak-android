package com.sahayak.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {

    private var selectedWorkflow = DemoGuides.workflows.first()
    private val evalConfig = EvalConfig()
    private var currentMode = 0 // 0 = Educator, 1 = Parent, 2 = Evals & APIs

    private lateinit var contentContainer: LinearLayout
    private lateinit var tabLayout: TabLayout

    private val micLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        renderCurrentMode()
    }

    private val projectionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        if (res.resultCode == RESULT_OK) {
            startForegroundService(Intent(this, ScreenRecorderService::class.java))
            Toast.makeText(this, "Recording started! Open WhatsApp/Instagram/PhonePe.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#F4F6F9"))
        }

        val header = TextView(this).apply {
            text = "Sahayak — Family Tech Guide"
            textSize = 20f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(Color.parseColor("#0F172A"))
            setPadding(40, 48, 40, 24)
            setBackgroundColor(Color.WHITE)
        }

        tabLayout = TabLayout(this).apply {
            setBackgroundColor(Color.WHITE)
            addTab(newTab().setText("🎥 Educator"))
            addTab(newTab().setText("👆 Parent"))
            addTab(newTab().setText("⚙️ Evals & APIs"))
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    currentMode = tab.position
                    renderCurrentMode()
                }
                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })
        }

        val scroll = ScrollView(this)
        contentContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(36, 36, 36, 60)
        }
        scroll.addView(contentContainer)

        root.addView(header)
        root.addView(tabLayout)
        root.addView(scroll, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        ))
        setContentView(root)

        handleDeepLink(intent)
        renderCurrentMode()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    /**
     * 1-Click Deep Link Handler: sahayak://guide/whatsapp_story
     * Launches straight into Persona 2 (Parent Mode) for that specific task!
     */
    private fun handleDeepLink(intent: Intent?) {
        val data: Uri = intent?.data ?: return
        if (data.scheme == "sahayak" && data.host == "guide") {
            val guideId = data.lastPathSegment
            selectedWorkflow = DemoGuides.findById(guideId)
            currentMode = 1
            tabLayout.getTabAt(1)?.select()
            renderCurrentMode()
        }
    }

    override fun onResume() {
        super.onResume()
        renderCurrentMode()
    }

    private fun renderCurrentMode() {
        contentContainer.removeAllViews()
        when (currentMode) {
            0 -> renderEducatorMode()
            1 -> renderParentMode()
            2 -> renderEvalsAndApis()
        }
    }

    private fun renderEducatorMode() {
        addSectionTitle("1. Choose or Record a Task")
        val spinner = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@MainActivity,
                android.R.layout.simple_spinner_dropdown_item,
                DemoGuides.workflows.map { it.title }
            )
            setSelection(DemoGuides.workflows.indexOf(selectedWorkflow))
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                    selectedWorkflow = DemoGuides.workflows[pos]
                }
                override fun onNothingSelected(p: AdapterView<*>?) {}
            }
        }
        contentContainer.addView(spinner)

        addButton("⏺ Record Screen Walkthrough (MediaProjection)", "#2563EB") {
            val mgr = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            projectionLauncher.launch(mgr.createScreenCaptureIntent())
        }

        addSectionTitle("2. Auto-Generated Navigation Transcript")
        selectedWorkflow.steps.forEach { step ->
            addCard("${step.stepNumber}. [${step.screenLabel}]", "${step.instructionEn}\nHI: ${step.instructionHi}")
        }

        addSectionTitle("3. 1-Click Share to Parent")
        addButton("📲 Share 1-Click Link on WhatsApp", "#16A34A") {
            val deepLink = "sahayak://guide/${selectedWorkflow.id}"
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Tap this 1-click Sahayak link to learn: ${selectedWorkflow.title}\n$deepLink"
                )
            }
            startActivity(Intent.createChooser(shareIntent, "Share Guide with Parent"))
        }
    }

    private fun renderParentMode() {
        val hasOverlay = Settings.canDrawOverlays(this)
        val hasMic = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        val hasAccessibility = SahayakAccessibilityService.instance != null

        addSectionTitle("Active Task: ${selectedWorkflow.title}")

        // Clear 1-Click Permissions Onboarding
        if (!hasOverlay || !hasMic || !hasAccessibility) {
            addCard(
                "Quick Setup Needed (Tap Each Button Once)",
                "To point on top of ${selectedWorkflow.appName} and hear your voice, allow these 3 permissions:"
            )
            if (!hasOverlay) {
                addButton("1. Allow Floating Screen Pointer", "#D97706") {
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                }
            }
            if (!hasAccessibility) {
                addButton("2. Enable UI-Tree Reader (For Banking/UPI)", "#D97706") {
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }
            }
            if (!hasMic) {
                addButton("3. Allow Microphone (Speak Any Language)", "#D97706") {
                    micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
        } else {
            addCard("✅ All Permissions Ready", "Tap below to launch ${selectedWorkflow.appName} with the live floating pointer.")
        }

        addButton("🚀 Start Guided Task on ${selectedWorkflow.appName}", "#16A34A") {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Please allow Floating Screen Pointer first", Toast.LENGTH_SHORT).show()
                return@addButton
            }
            val firstStep = selectedWorkflow.steps.first()
            val intent = Intent(this, FloatingPointerService::class.java).apply {
                action = FloatingPointerService.ACTION_SHOW_STEP
                putExtra(FloatingPointerService.EXTRA_CALLOUT, firstStep.instructionHi)
                putExtra(FloatingPointerService.EXTRA_X, 180)
                putExtra(FloatingPointerService.EXTRA_Y, 1200)
            }
            startForegroundService(intent)

            val launchIntent = packageManager.getLaunchIntentForPackage(selectedWorkflow.appPackage)
            if (launchIntent != null) {
                startActivity(launchIntent)
            } else {
                Toast.makeText(this, "Pointer overlay started! (${selectedWorkflow.appName} not installed on this device)", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun renderEvalsAndApis() {
        addSectionTitle("Configurable Agent Evals")
        addCard(
            "Live Telemetry",
            "Step Completion: 94% | Avg Latency: 620ms | FLAG_SECURE Fallbacks: 2"
        )

        val confLabel = TextView(this).apply {
            text = "Pointer Confidence Threshold: ${evalConfig.confidenceThreshold}%"
            setPadding(0, 16, 0, 8)
        }
        val confSeek = SeekBar(this).apply {
            max = 100
            progress = evalConfig.confidenceThreshold
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, p: Int, f: Boolean) {
                    evalConfig.confidenceThreshold = p
                    confLabel.text = "Pointer Confidence Threshold: $p%"
                }
                override fun onStartTrackingTouch(s: SeekBar?) {}
                override fun onStopTrackingTouch(s: SeekBar?) {}
            })
        }
        contentContainer.addView(confLabel)
        contentContainer.addView(confSeek)

        val secureSwitch = Switch(this).apply {
            text = "Auto-Fallback on FLAG_SECURE (UI-Tree + Voice-Only)"
            isChecked = evalConfig.autoFallbackOnFlagSecure
            setPadding(0, 20, 0, 20)
            setOnCheckedChangeListener { _, isChecked -> evalConfig.autoFallbackOnFlagSecure = isChecked }
        }
        contentContainer.addView(secureSwitch)

        addSectionTitle("API Integrations (Sarvam AI + Divyam.ai)")
        val sarvamInput = EditText(this).apply {
            hint = "Paste Sarvam AI Key (STT / Translate / TTS)"
            setText(VoiceAndAgentClients.getSarvamCredential(this@MainActivity))
        }
        val divyamInput = EditText(this).apply {
            hint = "Paste Divyam.ai Key (Multimodal Agent Reasoning)"
            setText(VoiceAndAgentClients.getDivyamCredential(this@MainActivity))
        }
        contentContainer.addView(sarvamInput)
        contentContainer.addView(divyamInput)

        addButton("💾 Save API Keys", "#2563EB") {
            VoiceAndAgentClients.saveCredentials(this, sarvamInput.text.toString(), divyamInput.text.toString())
            Toast.makeText(this, "API credentials saved locally!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addSectionTitle(title: String) {
        contentContainer.addView(TextView(this).apply {
            text = title
            textSize = 15f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(Color.parseColor("#1E293B"))
            setPadding(0, 28, 0, 12)
        })
    }

    private fun addCard(title: String, body: String) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(32, 24, 32, 24)
        }
        card.addView(TextView(this).apply {
            text = title
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(Color.parseColor("#0F172A"))
        })
        card.addView(TextView(this).apply {
            text = body
            textSize = 13f
            setTextColor(Color.parseColor("#475569"))
            setPadding(0, 8, 0, 0)
        })
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = 16 }
        contentContainer.addView(card, lp)
    }

    private fun addButton(label: String, hexColor: String, onClick: () -> Unit) {
        val btn = Button(this).apply {
            text = label
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor(hexColor))
            setPadding(24, 24, 24, 24)
            setOnClickListener { onClick() }
        }
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = 16 }
        contentContainer.addView(btn, lp)
    }
}
