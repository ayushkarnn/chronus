package ayush.chronos.presentation.auth

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import ayush.chronos.R

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    var errorText by remember { mutableStateOf<String?>(null) }
    val colorScheme = MaterialTheme.colorScheme
    val isLight = colorScheme.isLight()

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("23298068250-mb1mets0lve3a24jsacj2695umfkmo0i.apps.googleusercontent.com")
        .requestEmail()
        .build()
    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data: Intent? = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                viewModel.signInWithCredential(idToken)
            } catch (e: ApiException) {
                errorText = e.localizedMessage
            }
        }

    LaunchedEffect(state) {
        if (state is LoginState.Success) {
            onLoginSuccess()
        } else if (state is LoginState.Error) {
            errorText = (state as LoginState.Error).message
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(72.dp))
            Box(
                Modifier
                    .size(92.dp)
                    .shadow(12.dp, CircleShape)
                    .background(
                        when {
                            isLight -> colorScheme.primaryContainer.copy(alpha = 0.9f)
                            else -> colorScheme.surfaceVariant.copy(alpha = 0.9f)
                        }, CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(54.dp),
                    tint = colorScheme.primary
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Welcome to Chronos",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = colorScheme.onBackground,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Your intelligent reminder companion.\nSign in to get started.",
                style = MaterialTheme.typography.bodyLarge,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(44.dp))
            AnimatedVisibility(visible = errorText != null) {
                errorText?.let {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = colorScheme.errorContainer,
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .padding(bottom = 12.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = it,
                            color = colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }
            Button(
                onClick = { launcher.launch(googleSignInClient.signInIntent) },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(
                        width = 1.dp,
                        color = if (isLight) Color(0xFFDADCE0) else Color.Transparent,
                        shape = CircleShape
                    )
                    .shadow(4.dp, CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_google_logo),
                    contentDescription = "Google Logo",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Sign in with Google",
                    color = Color.Black,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            if (state is LoginState.Loading) {
                Spacer(modifier = Modifier.height(20.dp))
                CircularProgressIndicator(color = colorScheme.primary)
            }
            Spacer(Modifier.weight(1f))
        }
    }
}

private fun ColorScheme.isLight() = this.background.luminance() > 0.5f
