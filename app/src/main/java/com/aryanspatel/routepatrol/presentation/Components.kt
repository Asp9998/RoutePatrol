package com.aryanspatel.routepatrol.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun isKeyboardOpen(): Boolean {
    val ime = WindowInsets.ime
    return ime.getBottom(LocalDensity.current) > 0
}

@Composable
fun TextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
){
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it) },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF2563EB),
            unfocusedBorderColor = Color(0xFFD1D5DB)
        )
    )
}

@Composable
fun SnackBarMessage(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState) {

    SnackbarHost(
        hostState = snackbarHostState,
        modifier = modifier
            .zIndex(5f)
            .padding(16.dp)
            .navigationBarsPadding() // avoid system bars
    ) { data ->
        Snackbar(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = Color.Black,
            snackbarData = data,
            shape = RoundedCornerShape(12.dp),
        )
    }
}

@Composable
fun ActionCardIcon(
    icon: ImageVector
){
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(
                listOf(Color(0xFF2563EB), Color(0xFF4F46E5)))),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
    }
}