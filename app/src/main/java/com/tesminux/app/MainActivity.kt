package com.tesminux.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.tesminux.app.ui.theme.TesminuxTheme
import kotlinx.coroutines.delay

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
    private external fun tesminuxCreateSession(): Int
    private external fun tesminuxSwitchSession(index: Int): Boolean
    private external fun tesminuxCloseSession(): Boolean
    private external fun tesminuxSessionCount(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            TesminuxTheme {
                TesminuxTerminal()
            }
        }
    }

    @Composable
    private fun TesminuxTerminal() {

        var command by remember {
            mutableStateOf("")
        }

        var output by remember {
            mutableStateOf("")
        }

        var terminalStarted by remember {
            mutableStateOf(false)
        }

        var activeTabIndex by remember {
            mutableIntStateOf(0)
        }

        var sessionCount by remember {
            mutableIntStateOf(1)
        }

        val history = remember {
            mutableStateListOf<String>()
        }

        var historyIndex by remember {
            mutableIntStateOf(-1)
        }

        val scrollState = rememberScrollState()

        val keyboardController =
            LocalSoftwareKeyboardController.current

        fun selectTab(index: Int) {
            if (tesminuxSwitchSession(index)) {
                activeTabIndex = index
                output = tesminuxRead()
            }
        }

        fun createNewTab() {
            val newId = tesminuxCreateSession()
            if (newId >= 0) {
                sessionCount = tesminuxSessionCount()
                activeTabIndex = sessionCount - 1
                output = tesminuxRead()
            }
        }

        fun closeCurrentTab() {
            if (sessionCount > 1) {
                if (tesminuxCloseSession()) {
                    sessionCount = tesminuxSessionCount()
                    if (activeTabIndex >= sessionCount) {
                        activeTabIndex = sessionCount - 1
                    }
                    output = tesminuxRead()
                }
            }
        }

        fun previousCommand() {

            if (history.isEmpty()) return

            if (historyIndex > 0) {
                historyIndex--
            } else {
                historyIndex = 0
            }

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

            if (!terminalStarted) return

            if (command.isBlank()) return

            if (command.trim() == "clear") {
                tesminuxClear()
                output = ""
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
                output =
                    "TESMINUX: Failed to start terminal.\n"
            }

            while (true) {

                if (terminalStarted) {

                    val newOutput =
                        tesminuxRead()

                    if (newOutput != output) {
                        output = newOutput
                    }
                }

                delay(100)
            }
        }

        LaunchedEffect(output) {
            scrollState.animateScrollTo(
                scrollState.maxValue
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(12.dp)
        ) {

            Text(
                text = "TESMINUX",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (terminalStarted)
                    "Rust Core ✓   Android Shell ✓"
                else
                    "Starting Rust Core...",
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tab / Sekme Yönetim Barı
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ScrollableTabRow(
                    selectedTabIndex = activeTabIndex,
                    modifier = Modifier.weight(1f),
                    edgePadding = 0.dp,
                    containerColor = Color(0xFF1E1E1E),
                    contentColor = Color.White
                ) {
                    for (i in 0 until sessionCount) {
                        Tab(
                            selected = activeTabIndex == i,
                            onClick = { selectTab(i) },
                            text = {
                                Text(
                                    text = "Tab ${i + 1}",
                                    color = if (activeTabIndex == i) Color.Green else Color.LightGray
                                )
                            }
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { createNewTab() }) {
                        Text("+", color = Color.Green, style = MaterialTheme.typography.titleLarge)
                    }
                    if (sessionCount > 1) {
                        IconButton(onClick = { closeCurrentTab() }) {
                            Text("✕", color = Color.Red, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .background(Color.Black)
                    .padding(8.dp)
            ) {

                Text(
                    text = output,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                OutlinedTextField(
                    value = command,
                    onValueChange = {
                        command = it
                    },

                    modifier = Modifier.weight(1f),

                    singleLine = true,

                    label = {
                        Text("Command")
                    },

                    placeholder = {
                        Text("Enter command...")
                    },

                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),

                    keyboardActions = KeyboardActions(
                        onDone = {
                            executeCommand()
                        }
                    ),

                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Button(
                    onClick = {
                        previousCommand()
                    },
                    enabled = history.isNotEmpty()
                ) {
                    Text("↑")
                }

                Button(
                    onClick = {
                        nextCommand()
                    },
                    enabled = history.isNotEmpty()
                ) {
                    Text("↓")
                }

                Button(
                    onClick = {
                        executeCommand()
                    },
                    enabled = terminalStarted
                ) {
                    Text("Run")
                }
            }
        }
    }
}