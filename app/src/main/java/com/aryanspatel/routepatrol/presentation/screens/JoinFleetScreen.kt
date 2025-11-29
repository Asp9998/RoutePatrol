package com.aryanspatel.routepatrol.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aryanspatel.routepatrol.domain.model.UserRole
import com.aryanspatel.routepatrol.presentation.SnackBarMessage
import com.aryanspatel.routepatrol.presentation.TextField
import com.aryanspatel.routepatrol.presentation.isKeyboardOpen
import com.aryanspatel.routepatrol.presentation.models.JoinFleetEvent
import com.aryanspatel.routepatrol.presentation.viewmodels.JoinFleetViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinFleetScreen(
    onBack: () -> Unit,
    onFleetJoined: (fleetCode: String, role: UserRole) -> Unit,
    viewModel: JoinFleetViewModel = hiltViewModel()
) {

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardOpen = isKeyboardOpen()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is JoinFleetEvent.FleetJoined -> {
                    onFleetJoined(event.fleet.code, state.userRole)
                }
                is JoinFleetEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold (
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Join Fleet",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
            )
        },
        snackbarHost = {
            SnackBarMessage(
                snackbarHostState = snackbarHostState)
        },
        modifier = Modifier
            .fillMaxSize(),
        containerColor = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .imePadding()
                .padding(it)
                .padding(horizontal = 24.dp)
                .fillMaxSize()
        ) {

            CredentialSection(
                userName = state.userName,
                fleetCode = state.fleetCode,
                onUserNameChange = { viewModel.onUserNameeChanged(it) },
                onFleetCodeChange = { viewModel.onFleetCodeChanged(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            RoleSection(
                selectedRole = state.userRole,
                listOfRole = listOf(UserRole.VIEWER, UserRole.DRIVER),
                onRoleSelected = { viewModel.onRoleChanged(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.onJoinClicked() },
                enabled = !state.isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4F46E5),
                    disabledContainerColor = Color(0xFF4F46E5).copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = if (state.isSubmitting) "Joining Fleet..." else "Join Fleet",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }


            // Info Box
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 24.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFEFF6FF)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Don't have a code?",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827),
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Ask your fleet manager to share the fleet code with you.",
                            color = Color(0xFF374151),
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CredentialSection(
    userName: String,
    fleetCode: String,
    onUserNameChange: (String) -> Unit,
    onFleetCodeChange: (String) -> Unit
) {

    Text(
        text = "Credentials",
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Medium,
        color = Color.Gray,
        modifier = Modifier.padding(bottom = 6.dp)
    )

    // Form Fields
    Column(
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        TextField(
            label = "Enter Your Name",
            value = userName,
            onValueChange = { onUserNameChange(it) }
        )

        OutlinedTextField(
            value = fleetCode ,
            onValueChange = { if (it.length <= 6) onFleetCodeChange(it.uppercase()) },
            label = { Text("Fleet Code") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            textStyle = LocalTextStyle.current.copy(
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                letterSpacing = 8.sp,
                fontWeight = FontWeight.Medium
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF4F46E5),
                unfocusedBorderColor = Color(0xFFD1D5DB)
            )
        )
    }
}