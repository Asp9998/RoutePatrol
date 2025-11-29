package com.aryanspatel.routepatrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.aryanspatel.routepatrol.presentation.nav.NavGraph
import com.aryanspatel.routepatrol.presentation.screens.FleetTrackTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FleetTrackTheme {
                App()
            }
        }
    }
}

@Composable
fun App(){
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ){
        NavGraph()
    }
}