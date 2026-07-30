package com.tesminux.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

        val scrollState = rememberScrollState()

        LaunchedEffect(Unit) {
            val result = tesminuxStart()

            terminalStarted = result == 0

            if (!terminalStarted) {
                output = "TESMINUX: Terminal başlatılamadı.\n"
            }

            while (true) {
                if (terminalStarted) {
                    val newOutput = tesminuxRead()

                    if (newOutput != output) {
                        output = newOutput
                    }
                }

                delay(100)
            }
        }

        LaunchedEffect(output) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }

        fun executeCommand() {
            if (command.isBlank() || !terminalStarted) {
                return
            }

            tesminuxWrite("$command\n")
            command = ""
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
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = if (terminalStarted) {
                    "Terminal aktif"
                } else {
                    "Terminal başlatılıyor..."
                },
                color = Color.LightGray,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
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
                        Text("Komut")
                    }
                )

                Button(
                    onClick = {
                        executeCommand()
                    },
                    enabled = terminalStarted
                ) {
                    Text("Çalıştır")
                }
            }
        }
    }
}