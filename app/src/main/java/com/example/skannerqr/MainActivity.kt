package com.example.skannerqr

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.skannerqr.ui.theme.SkannerqrTheme
import androidx.compose.ui.unit.dp


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SkannerqrTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.background),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .fillMaxHeight()
                            .padding(16.dp), // Add padding to the column
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FilledTonalButton(
                            onClick = test(1),
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(50.dp) // Increase button height
                        ) {
                            Text(text = "Button 1")
                        }
                        Spacer(modifier = Modifier.height(70.dp)) // Add spacing between buttons
                        FilledTonalButton(
                            onClick = quit(),
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(50.dp) // Increase button height
                        ) {
                            Text(text = "Wyjdź z aplikacji")
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun test(number: Int): () -> Unit {
    return {
        println("Button $number clicked!")
    }
}

@Composable
fun quit(): () -> Unit {
    val context = LocalContext.current
    return {
        if (context is ComponentActivity) {
            context.finish()
        }
    }
}
