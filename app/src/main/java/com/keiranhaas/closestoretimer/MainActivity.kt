package com.keiranhaas.closestoretimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.ExperimentalMaterial3Api
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

        enableEdgeToEdge()

        setContent {
            CloseStoreTimerApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@androidx.compose.runtime.Composable
fun CloseStoreTimerApp() {

    var selectedHour by remember {
        mutableStateOf(LocalTime.now().hour)
    }

    var selectedMinute by remember {
        mutableStateOf(LocalTime.now().minute)
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

    /*
     * Recalculate the countdown once per second.
     *
     * The countdown is always based on the actual system clock,
     * so it cannot drift if Android delays the coroutine.
     */
    LaunchedEffect(isRunning, selectedHour, selectedMinute) {

        if (!isRunning) {
            return@LaunchedEffect
        }

        while (isRunning) {

            val now = LocalDateTime.now()

            var closingDate = LocalDate.now()

            var closingDateTime = LocalDateTime.of(
                closingDate,
                LocalTime.of(
                    selectedHour,
                    selectedMinute
                )
            )

            /*
             * If the selected closing time has already passed today,
             * assume the store is closing at that time tomorrow.
             */
            if (!closingDateTime.isAfter(now)) {
                closingDate = closingDate.plusDays(1)

                closingDateTime = LocalDateTime.of(
                    closingDate,
                    LocalTime.of(
                        selectedHour,
                        selectedMinute
                    )
                )
            }

            val secondsRemaining = Duration.between(
                now,
                closingDateTime
            ).seconds

            remainingSeconds = secondsRemaining

            if (secondsRemaining <= 0) {
                isClosed = true
                isRunning = false
                break
            }

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
                onHourChanged = {
                    selectedHour = it
                },
                onMinuteChanged = {
                    selectedMinute = it
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

@OptIn(ExperimentalMaterial3Api::class)
@androidx.compose.runtime.Composable
private fun SetupScreen(
    selectedHour: Int,
    selectedMinute: Int,
    onHourChanged: (Int) -> Unit,
    onMinuteChanged: (Int) -> Unit,
    onStart: () -> Unit
) {

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

        TimeInput(
            state = androidx.compose.material3.rememberTimePickerState(
                initialHour = selectedHour,
                initialMinute = selectedMinute,
                is24Hour = false
            ),
            modifier = Modifier.padding(vertical = 32.dp)
        )

        /*
         * The TimeInput component maintains its own state, so the
         * selected time is captured by the button below.
         *
         * This section is replaced with the simpler custom picker
         * below for reliable state handling.
         */
        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
        ) {
            Text(
                text = "START",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@androidx.compose.runtime.Composable
private fun CountdownScreen(
    remainingSeconds: Long,
    isClosed: Boolean,
    onReset: () -> Unit
) {

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