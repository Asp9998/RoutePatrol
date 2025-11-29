package com.aryanspatel.routepatrol.presentation.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aryanspatel.routepatrol.domain.model.UserRole
import com.aryanspatel.routepatrol.presentation.SnackBarMessage
import com.aryanspatel.routepatrol.presentation.TextField
import com.aryanspatel.routepatrol.presentation.models.CreateFleetEvent
import com.aryanspatel.routepatrol.presentation.viewmodels.CreateFleetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFleetScreen(
    onBack: () -> Unit,
    onFleetCreated: (fleetCode: String, role: UserRole) -> Unit,
    viewModel: CreateFleetViewModel = hiltViewModel()
) {

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CreateFleetEvent.FleetCreated -> {
                    // Save session
                    viewModel.onSaveSession(context)

                    // navigate
                    onFleetCreated(event.fleet.code, state.userRole)
                }
                is CreateFleetEvent.ShowError -> {
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
                        text = "Create Your Fleet",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
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
                .padding(it)
                .padding(horizontal = 24.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {

            CredentialSection(
                userName = state.userName,
                fleetName = state.fleetName,
                onUserNameChange = { viewModel.onUserNameChanged(it) },
                onFleetNameChange = { viewModel.onFleetNameChanged(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            RoleSection(
                selectedRole = state.userRole,
                listOfRole = listOf(UserRole.VIEWER, UserRole.DRIVER),
                onRoleSelected = { viewModel.onRoleChanged(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))


            FleetCodeSection(
                fleetCode = state.fleetCode
            )

            Spacer(modifier = Modifier.height(16.dp))


            BottomActionButton(
                isFleetCodeEmpty = state.fleetCode.isEmpty(),
                isSubmitting = state.isSubmitting,
                onButtonClick = {
                    if(state.fleetCode.isEmpty()){
                        viewModel.onGenerateFleetCode()
                    } else {
                        viewModel.onCreateFleetClicked()
                    }
                }
            )

            BottomInfoSection()
        }
    }
}

@Composable
private fun BottomInfoSection() {
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
                    text = "Note:",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827),
                    fontSize = 14.sp
                )
                Text(
                    text = "After Generating Fleet code, you'll receive a unique fleet code to share with your team members.",
                    color = Color(0xFF374151),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun BottomActionButton(
    isFleetCodeEmpty: Boolean,
    isSubmitting: Boolean,
    onButtonClick: () -> Unit
) {
    Button(
        onClick = onButtonClick,
        enabled = !isSubmitting,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(top = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2563EB),
            disabledContainerColor = Color(0xFF2563EB).copy(alpha = 0.5f)
        )
    ) {
        Text(
            text =
                if(isFleetCodeEmpty) "Generate Fleet Code"
                else if(isSubmitting)"Creating Fleet..."
                else "Create Fleet",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

@Composable
private fun FleetCodeSection(
    fleetCode: String,
) {

    val borderColor = if(fleetCode.isEmpty())  Color(0xFFD1D5DB) else Color(0xFF2563EB)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Fleet Code",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Box(modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, borderColor, shape = RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ){
            Text(
                text = fleetCode,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(vertical = 10.dp),
                letterSpacing = 8.sp
            )
        }
    }
}

@Composable
fun RoleSection(
    selectedRole: UserRole,
    listOfRole: List<UserRole>,
    onRoleSelected: (UserRole) -> Unit
){
    val description =
        if(selectedRole == UserRole.VIEWER) "Monitor fleet and receive notifications"
        else "Share location and receive assignments"

    Column {
        Text(
            text = "Your Role",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOfRole.forEach {
                RoleOption(
                    role = it.name,
                    description = description,
                    selected = selectedRole == it,
                    onSelect = { onRoleSelected(it) },
                )
            }
        }
    }

}

@Composable
private fun CredentialSection(
    userName: String,
    fleetName: String,
    onUserNameChange: (String) -> Unit,
    onFleetNameChange: (String) -> Unit
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

        TextField(
            label = "Enter Fleet Your Name",
            value = fleetName,
            onValueChange = { onFleetNameChange(it) }
        )
    }
}
