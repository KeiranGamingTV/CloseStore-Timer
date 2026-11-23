package com.keiranhaas.closestoretimer

import android.app.TimePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation =
            android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        setContent {
            CloseStoreTimerApp()
        }
    }
}

@Composable
fun CloseStoreTimerApp() {

    var selectedHour by remember {
        mutableStateOf(20)
    }

    var selectedMinute by remember {
        mutableStateOf(0)
    }

    var isRunning by remember {
        mutableStateOf(false)
    }

    var isClosed by remember {
        mutableStateOf(false)
    }

    var remainingSeconds by remember {
        mutableLongStateOf(0L)
    }

    LaunchedEffect(
        isRunning,
        selectedHour,
        selectedMinute
    ) {

        if (!isRunning) {
            return@LaunchedEffect
        }

        while (isRunning) {

            val now = LocalDateTime.now()

            var closingDateTime = LocalDateTime.of(
                LocalDate.now(),
                LocalTime.of(
                    selectedHour,
                    selectedMinute
                )
            )

            /*
             * If today's closing time has already passed,
             * use tomorrow's closing time.
             */
            if (!closingDateTime.isAfter(now)) {

                closingDateTime = closingDateTime.plusDays(1)
            }

            val seconds = Duration.between(
                now,
                closingDateTime
            ).seconds

            if (seconds <= 0) {

                remainingSeconds = 0
                isClosed = true
                isRunning = false

                break
            }

            remainingSeconds = seconds

            delay(1_000L)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {

        if (!isRunning && !isClosed) {

            SetupScreen(
                selectedHour = selectedHour,
                selectedMinute = selectedMinute,
                onTimeSelected = { hour, minute ->
                    selectedHour = hour
                    selectedMinute = minute
                },
                onStart = {
                    isClosed = false
                    isRunning = true
                }
            )

        } else {

            CountdownScreen(
                remainingSeconds = remainingSeconds,
                isClosed = isClosed,
                onReset = {
                    isRunning = false
                    isClosed = false
                }
            )
        }
    }
}

@Composable
private fun SetupScreen(
    selectedHour: Int,
    selectedMinute: Int,
    onTimeSelected: (Int, Int) -> Unit,
    onStart: () -> Unit
) {

    val context = androidx.compose.ui.platform.LocalContext.current

    val formattedTime = formatTime(
        selectedHour,
        selectedMinute
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "STORE CLOSING TIME",
            color = Color.Black,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Button(
            onClick = {

                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        onTimeSelected(hour, minute)
                    },
                    selectedHour,
                    selectedMinute,
                    false
                ).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 32.dp,
                    start = 32.dp,
                    end = 32.dp
                )
        ) {

            Text(
                text = formattedTime,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 32.dp,
                    start = 32.dp,
                    end = 32.dp
                )
        ) {

            Text(
                text = "START",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CountdownScreen(
    remainingSeconds: Long,
    isClosed: Boolean,
    onReset: () -> Unit
) {

    /*
     * Round upward so that any partial minute still counts as
     * a minute remaining.
     *
     * Example:
     * 599 seconds -> 10 minutes
     * 540 seconds -> 9 minutes
     * 1 second    -> 1 minute
     * 0 seconds   -> 0 minutes
     */
    val minutesRemaining = if (remainingSeconds > 0) {
        (remainingSeconds + 59) / 60
    } else {
        0
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            if (isClosed) {

                Text(
                    text = "STORE CLOSED",
                    color = Color.Black,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

            } else {

                Text(
                    text = "CLOSING IN",
                    color = Color.Black,
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "$minutesRemaining MINUTES",
                    color = Color.Black,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.padding(top = 48.dp)
            ) {

                Text(
                    text = "RESET",
                    fontSize = 22.sp
                )
            }
        }
    }
}

private fun formatTime(
    hour: Int,
    minute: Int
): String {

    val suffix = if (hour >= 12) {
        "PM"
    } else {
        "AM"
    }

    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }

    return String.format(
        "%d:%02d %s",
        displayHour,
        minute,
        suffix
    )
}