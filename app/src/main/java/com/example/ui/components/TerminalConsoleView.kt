package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IdeAccentRun
import com.example.ui.theme.IdeDarkBorder
import com.example.ui.theme.IdeErrorRed

@Composable
fun TerminalConsoleView(
    consoleOutput: String,
    isError: Boolean,
    executionTimeMs: Long,
    onSendInput: (String) -> Unit,
    onClearConsole: () -> Unit
) {
    var inputCommand by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    LaunchedEffect(consoleOutput) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .border(0.5.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "الموجه",
                        tint = if (isError) IdeErrorRed else IdeAccentRun,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "نافذة التشغيل والموجه (Terminal & Console)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (executionTimeMs > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "(${executionTimeMs}ms)",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }

                IconButton(
                    onClick = onClearConsole,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("clear_console_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "مسح المخرجات",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Output Display
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF090D12))
                    .verticalScroll(scrollState)
                    .padding(10.dp)
            ) {
                Text(
                    text = consoleOutput.ifEmpty { "جاهز لتشغيل الكود..." },
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = if (isError) IdeErrorRed else Color(0xFF38BDF8)
                )
            }

            // Interactive Input Line
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ">>> ",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = IdeAccentRun
                )

                BasicTextField(
                    value = inputCommand,
                    onValueChange = { inputCommand = it },
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .testTag("terminal_input_field")
                )

                IconButton(
                    onClick = {
                        if (inputCommand.isNotBlank()) {
                            onSendInput(inputCommand)
                            inputCommand = ""
                        }
                    },
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("terminal_send_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "إرسال المدخل",
                        tint = IdeAccentRun,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
