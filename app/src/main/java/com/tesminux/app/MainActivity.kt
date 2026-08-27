package com.tesminux.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.tesminux.app.ui.theme.TesminuxTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


enum class TerminalThemeConfig(
    val title: String,
    val bg: Color,
    val panel: Color,
    val defaultText: Color,
    val accent: Color
) {
    DRACULA("Dracula", Color(0xFF181825), Color(0xFF1E1E2E), Color(0xFFF8F8F2), Color(0xFF50FA7B)),
    MONOKAI("Monokai", Color(0xFF272822), Color(0xFF1E1F1C), Color(0xFFF8F8F2), Color(0xFFA6E22E)),
    MATRIX("Matrix", Color(0xFF050B05), Color(0xFF0A150A), Color(0xFF00FF66), Color(0xFF00FF66)),
    CYBERPUNK("Cyberpunk", Color(0xFF0D0221), Color(0xFF190634), Color(0xFF00F0FF), Color(0xFFFF007F)),
    AMBER("Retro Amber", Color(0xFF120D00), Color(0xFF1C1400), Color(0xFFFFB000), Color(0xFFFFC107))
}

class MainActivity : ComponentActivity() {

    companion object {
        init {
            System.loadLibrary("tesminux_core")
        }
    }

    private external fun tesminuxStart(): Int
    private external fun tesminuxWrite(input: String): Int
    private external fun tesminuxRead(): String
    private external fun tesminuxIsRunning(): Boolean
    private external fun tesminuxClear()

    // Runtime storage permission launcher (Android 6-12)
    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true
        if (!granted) {
            Toast.makeText(
                this,
                "Storage izni verilmedi. Terminal /storage/emulated/0 erişimi kısıtlı olabilir.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestStoragePermission()

        setContent {
            TesminuxTheme {
                TesminuxTerminal()
            }
        }
    }

    /**
     * Requests external storage access for the terminal home directory (/storage/emulated/0).
     * - Android 11+ (API 30+): Opens system settings for MANAGE_EXTERNAL_STORAGE.
     * - Android 6-12: Requests READ/WRITE_EXTERNAL_STORAGE at runtime.
     */
    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+: check MANAGE_EXTERNAL_STORAGE
            if (!Environment.isExternalStorageManager()) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                } catch (e: Exception) {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    startActivity(intent)
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6-12
            val readGranted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
            if (!readGranted) {
                storagePermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
            }
        }
    }

    @Composable
    private fun TesminuxTerminal() {
        val context = LocalContext.current
        val clipboardManager = LocalClipboardManager.current
        val hapticFeedback = LocalHapticFeedback.current

        var command by remember { mutableStateOf("") }
        var rawOutput by remember { mutableStateOf("") }
        var terminalStarted by remember { mutableStateOf(false) }

        var currentTheme by remember { mutableStateOf(TerminalThemeConfig.DRACULA) }
        var fontSizeSp by remember { mutableIntStateOf(13) }
        var themeMenuExpanded by remember { mutableStateOf(false) }

        val history = remember { mutableStateListOf<String>() }
        var historyIndex by remember { mutableIntStateOf(-1) }

        val scrollState = rememberScrollState()
        val coroutineScope = rememberCoroutineScope()
        val keyboardController = LocalSoftwareKeyboardController.current

        var userScrolledUp by remember { mutableStateOf(false) }

        val annotatedOutput = remember(rawOutput, currentTheme) {
            parseAnsiToAnnotatedString(rawOutput, currentTheme.defaultText)
        }

        // Auto-suggestions calculation
        val defaultCommands = remember {
            listOf("ls", "ls -la", "cd", "pwd", "mkdir", "cat", "echo", "rm", "grep", "chmod", "sysinfo", "version", "date", "whoami", "env", "clear", "help")
        }
        val suggestions = remember(command, history) {
            if (command.isBlank()) emptyList()
            else {
                (defaultCommands + history).distinct()
                    .filter { it.lowercase().startsWith(command.trim().lowercase()) && it != command.trim() }
                    .take(6)
            }
        }

        // Auto-scroll lock detection
        LaunchedEffect(scrollState.value, scrollState.maxValue) {
            userScrolledUp = scrollState.maxValue - scrollState.value > 100
        }

        // Auto-scroll to bottom when output updates (if not scrolled up)
        LaunchedEffect(rawOutput) {
            if (!userScrolledUp) {
                scrollState.animateScrollTo(scrollState.maxValue)
            }
        }

        fun sendRawKey(keyInput: String) {
            if (!terminalStarted) return
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            tesminuxWrite(keyInput)
            rawOutput = tesminuxRead()
        }

        fun previousCommand() {
            if (history.isEmpty()) return
            historyIndex = if (historyIndex > 0) historyIndex - 1 else 0
            command = history[historyIndex]
        }

        fun nextCommand() {
            if (history.isEmpty()) return
            if (historyIndex < history.lastIndex) {
                historyIndex++
                command = history[historyIndex]
            } else {
                historyIndex = history.size
                command = ""
            }
        }

        fun executeCommand() {
            if (!terminalStarted || command.isBlank()) return

            if (command.trim() == "clear" || command.trim() == "cls") {
                tesminuxClear()
                rawOutput = ""
                command = ""
                keyboardController?.hide()
                return
            }

            history.add(command)
            historyIndex = history.size

            tesminuxWrite("$command\n")
            command = ""
            keyboardController?.hide()
        }

        LaunchedEffect(Unit) {
            val result = tesminuxStart()
            terminalStarted = result == 0

            if (!terminalStarted) {
                rawOutput = "TESMINUX: Failed to start terminal session.\n"
            }

            while (true) {
                if (terminalStarted) {
                    val newOutput = tesminuxRead()
                    if (newOutput != rawOutput) {
                        rawOutput = newOutput
                    }
                }
                delay(100)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(currentTheme.bg)
                .padding(12.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "TESMINUX v5",
                        color = currentTheme.accent,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box {
                        Surface(
                            onClick = { themeMenuExpanded = true },
                            shape = RoundedCornerShape(4.dp),
                            color = currentTheme.panel,
                            contentColor = currentTheme.accent
                        ) {
                            Text(
                                text = currentTheme.title,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        DropdownMenu(
                            expanded = themeMenuExpanded,
                            onDismissRequest = { themeMenuExpanded = false },
                            modifier = Modifier.background(currentTheme.panel)
                        ) {
                            TerminalThemeConfig.entries.forEach { theme ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            theme.title,
                                            color = if (theme == currentTheme) theme.accent else theme.defaultText,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    },
                                    onClick = {
                                        currentTheme = theme
                                        themeMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Font scaling controls
                    IconButton(
                        onClick = { if (fontSizeSp > 10) fontSizeSp-- },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Text("A-", color = Color.LightGray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }

                    Text(
                        text = "${fontSizeSp}sp",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    IconButton(
                        onClick = { if (fontSizeSp < 24) fontSizeSp++ },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Text("A+", color = Color.LightGray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Copy to Clipboard button
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(rawOutput))
                            Toast.makeText(context, "Terminal output copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Text("Copy", color = Color(0xFF8BE9FD), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    // Clear button
                    IconButton(
                        onClick = {
                            tesminuxClear()
                            rawOutput = ""
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Text("Clear", color = Color(0xFFFF79C6), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Terminal Output Buffer Container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(currentTheme.panel, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    Text(
                        text = annotatedOutput,
                        fontFamily = FontFamily.Monospace,
                        fontSize = fontSizeSp.sp,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Jump to Bottom Badge (Auto-Scroll Resume)
                if (userScrolledUp) {
                    Surface(
                        onClick = {
                            userScrolledUp = false
                            coroutineScope.launch {
                                scrollState.animateScrollTo(scrollState.maxValue)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = currentTheme.accent,
                        contentColor = Color.Black
                    ) {
                        Text(
                            text = "↓ Bottom",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Auto-Suggestions Chips Row
            if (suggestions.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(suggestions) { suggestion ->
                        Surface(
                            onClick = {
                                command = suggestion
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = currentTheme.panel,
                            contentColor = currentTheme.accent
                        ) {
                            Text(
                                text = "💡 $suggestion",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Quick Key Toolbar
            QuickKeyToolbar(
                theme = currentTheme,
                onSendKey = { sendRawKey(it) }
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Command Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = command,
                    onValueChange = { command = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    placeholder = {
                        Text("Enter command...", color = Color.Gray, fontFamily = FontFamily.Monospace)
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { executeCommand() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = currentTheme.defaultText,
                        unfocusedTextColor = currentTheme.defaultText,
                        focusedBorderColor = currentTheme.accent,
                        unfocusedBorderColor = Color(0xFF6272A4)
                    )
                )

                Button(
                    onClick = { previousCommand() },
                    enabled = history.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF44475A))
                ) {
                    Text("↑")
                }

                Button(
                    onClick = { nextCommand() },
                    enabled = history.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF44475A))
                ) {
                    Text("↓")
                }

                Button(
                    onClick = { executeCommand() },
                    enabled = terminalStarted,
                    colors = ButtonDefaults.buttonColors(containerColor = currentTheme.accent, contentColor = Color.Black)
                ) {
                    Text("Run", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    @Composable
    private fun QuickKeyToolbar(
        theme: TerminalThemeConfig,
        onSendKey: (String) -> Unit
    ) {
        val quickKeys = listOf(
            "CTRL+L" to "\u000C",
            "CTRL+C" to "\u0003",
            "CTRL+Z" to "\u001A",
            "CTRL+D" to "\u0004",
            "CTRL+A" to "\u0001",
            "CTRL+E" to "\u0005",
            "TAB" to "\t",
            "ESC" to "\u001B",
            "|" to "|",
            "/" to "/",
            "-" to "-",
            "~" to "~",
            "↑" to "\u001B[A",
            "↓" to "\u001B[B",
            "←" to "\u001B[D",
            "→" to "\u001B[C"
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(theme.panel, RoundedCornerShape(6.dp))
                .padding(vertical = 4.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(quickKeys) { (label, value) ->
                Surface(
                    onClick = { onSendKey(value) },
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF44475A),
                    contentColor = theme.accent
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    private fun parseAnsiToAnnotatedString(input: String, defaultColor: Color): AnnotatedString {
        val builder = AnnotatedString.Builder()
        var currentFgColor: Color? = null
        var isBold = false

        var i = 0
        val len = input.length

        while (i < len) {
            if (input[i] == '\u001B' && i + 1 < len && input[i + 1] == '[') {
                i += 2
                val paramStart = i
                while (i < len && (input[i].isDigit() || input[i] == ';')) {
                    i++
                }
                if (i < len && input[i] == 'm') {
                    val paramsStr = input.substring(paramStart, i)
                    i++
                    if (paramsStr.isEmpty() || paramsStr == "0") {
                        currentFgColor = null
                        isBold = false
                    } else {
                        val parts = paramsStr.split(';')
                        for (part in parts) {
                            val code = part.toIntOrNull() ?: continue
                            when (code) {
                                0 -> {
                                    currentFgColor = null
                                    isBold = false
                                }
                                1 -> isBold = true
                                in 30..37 -> currentFgColor = getAnsiColor(code - 30, false, defaultColor)
                                in 90..97 -> currentFgColor = getAnsiColor(code - 90, true, defaultColor)
                                39 -> currentFgColor = null
                            }
                        }
                    }
                } else {
                    if (i < len) i++
                }
            } else {
                val style = SpanStyle(
                    color = currentFgColor ?: defaultColor,
                    fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
                )
                builder.pushStyle(style)
                builder.append(input[i])
                builder.pop()
                i++
            }
        }
        return builder.toAnnotatedString()
    }

    private fun getAnsiColor(code: Int, bright: Boolean, defaultFallback: Color): Color {
        return when (code + if (bright) 8 else 0) {
            0 -> Color(0xFF21222C)
            1 -> Color(0xFFFF5555)
            2 -> Color(0xFF50FA7B)
            3 -> Color(0xFFF1FA8C)
            4 -> Color(0xFFBD93F9)
            5 -> Color(0xFFFF79C6)
            6 -> Color(0xFF8BE9FD)
            7 -> Color(0xFFBFBFBF)
            8 -> Color(0xFF6272A4)
            9 -> Color(0xFFFF6E6E)
            10 -> Color(0xFF69FF94)
            11 -> Color(0xFFFFFFA5)
            12 -> Color(0xD6ACFF)
            13 -> Color(0xFFFF92D0)
            14 -> Color(0xFFA4FFFF)
            15 -> Color(0xFFFFFFFF)
            else -> defaultFallback
        }
    }
}