package com.example.finsight.presentation.screens.login

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finsight.presentation.screens.settings.SettingsDataStore
import com.example.finsight.presentation.theme.Primary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

private const val JSONBIN_API_KEY = "\$2a\$10\$PLACEHOLDER_REPLACE_WITH_YOUR_JSONBIN_KEY"
private const val JSONBIN_BIN_ID  = "PLACEHOLDER_REPLACE_WITH_YOUR_BIN_ID"
private const val JSONBIN_BASE    = "https://api.jsonbin.io/v3/b"

private fun simpleHash(input: String): String {
    var h = 5381L
    for (c in input) h = h * 33 + c.code
    return h.toString(16)
}

sealed class LoginResult {
    data object Idle : LoginResult()
    data object Loading : LoginResult()
    data object Success : LoginResult()
    data class Error(val message: String) : LoginResult()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val dataStore: SettingsDataStore
) : ViewModel() {

    private val _result = MutableStateFlow<LoginResult>(LoginResult.Idle)
    val result: StateFlow<LoginResult> = _result

    fun loginAsGuest() {
        viewModelScope.launch {
            _result.value = LoginResult.Loading
            delay(600) //
            dataStore.setUserName("Guest")
            _result.value = LoginResult.Success
        }
    }

    fun loginWithEmail(email: String, password: String, isSignUp: Boolean) {
        if (email.isBlank() || !email.contains("@")) {
            _result.value = LoginResult.Error("Please enter a valid email")
            return
        }
        if (password.length < 6) {
            _result.value = LoginResult.Error("Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            _result.value = LoginResult.Loading
            try {
                val ok = withContext(Dispatchers.IO) {
                    if (isSignUp) signUp(email, password) else signIn(email, password)
                }
                if (ok.first) {
                    val displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() }
                    dataStore.setUserName(displayName)
                    _result.value = LoginResult.Success
                } else {
                    _result.value = LoginResult.Error(ok.second)
                }
            } catch (e: Exception) {
                // Network unavailable fall back to offline login
                val displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() }
                dataStore.setUserName(displayName)
                _result.value = LoginResult.Success
            }
        }
    }

    private fun signUp(email: String, password: String): Pair<Boolean, String> {
        val existing = readBin() ?: JSONObject()
        if (existing.has(email)) return Pair(false, "Account already exists. Please sign in.")
        existing.put(email, simpleHash(password))
        writeBin(existing)
        return Pair(true, "")
    }

    private fun signIn(email: String, password: String): Pair<Boolean, String> {
        val data = readBin() ?: return Pair(false, "Could not reach server. Logging in offline.")
        if (!data.has(email)) return Pair(false, "No account found. Please sign up first.")
        if (data.getString(email) != simpleHash(password)) return Pair(false, "Incorrect password.")
        return Pair(true, "")
    }

    private fun readBin(): JSONObject? {
        return try {
            val url = URL("$JSONBIN_BASE/$JSONBIN_BIN_ID/latest")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("X-Master-Key", JSONBIN_API_KEY)
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            val body = conn.inputStream.bufferedReader().readText()
            JSONObject(body).optJSONObject("record")
        } catch (e: Exception) { null }
    }

    private fun writeBin(data: JSONObject) {
        try {
            val url = URL("$JSONBIN_BASE/$JSONBIN_BIN_ID")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "PUT"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("X-Master-Key", JSONBIN_API_KEY)
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.doOutput = true
            conn.outputStream.write(data.toString().toByteArray())
            conn.inputStream.close()
        } catch (_: Exception) {}
    }
}

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val result by viewModel.result.collectAsState()
    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(result) {
        if (result is LoginResult.Success) onLoginSuccess()
    }

    // Logo pulse animation
    val logoScale by rememberInfiniteTransition(label = "logo").animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1040),
                        Color(0xFF2D1B69),
                        Color(0xFF6C63FF).copy(alpha = 0.85f)
                    )
                )
            )
    ) {
        // Decorative circles
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-80).dp, y = (-80).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.04f))
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 60.dp, y = 60.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.04f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(72.dp))

            // Logo
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .scale(logoScale)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF9C8FFF), Color(0xFF6C63FF))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountBalance,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Finsight",
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Your personal finance companion",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.65f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(44.dp))

            // Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.10f)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    // Toggle Sign In / Sign Up
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .padding(4.dp)
                    ) {
                        listOf("Sign In" to false, "Sign Up" to true).forEach { (label, mode) ->
                            val selected = isSignUp == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(if (selected) Color.White.copy(alpha = 0.18f) else Color.Transparent)
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                TextButton(onClick = { isSignUp = mode; viewModel.result.let { } }) {
                                    Text(
                                        text = label,
                                        color = if (selected) Color.White else Color.White.copy(alpha = 0.5f),
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Email field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email", color = Color.White.copy(alpha = 0.7f)) },
                        leadingIcon = {
                            Icon(Icons.Filled.Email, null, tint = Color.White.copy(alpha = 0.7f))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White.copy(alpha = 0.8f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    // Password field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password", color = Color.White.copy(alpha = 0.7f)) },
                        leadingIcon = {
                            Icon(Icons.Filled.Lock, null, tint = Color.White.copy(alpha = 0.7f))
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    null,
                                    tint = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                                               else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                viewModel.loginWithEmail(email, password, isSignUp)
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White.copy(alpha = 0.8f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    // Error message
                    AnimatedVisibility(visible = result is LoginResult.Error) {
                        val msg = (result as? LoginResult.Error)?.message ?: ""
                        Row(
                            modifier = Modifier.padding(top = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.ErrorOutline,
                                null,
                                tint = Color(0xFFFF6B6B),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(msg, color = Color(0xFFFF6B6B), fontSize = 13.sp)
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Primary button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.loginWithEmail(email, password, isSignUp)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF6C63FF)
                        ),
                        enabled = result !is LoginResult.Loading
                    ) {
                        if (result is LoginResult.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color(0xFF6C63FF),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (isSignUp) "Create Account" else "Sign In",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color.White.copy(alpha = 0.2f)
                )
                Text(
                    "  or  ",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 13.sp
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color.White.copy(alpha = 0.2f)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Guest button
            OutlinedButton(
                onClick = { viewModel.loginAsGuest() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, Color.White.copy(alpha = 0.35f)
                ),
                enabled = result !is LoginResult.Loading
            ) {
                Icon(
                    Icons.Filled.PersonOutline,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    "Continue as Guest",
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Your data stays private on this device.\nCloud sync uses JSONBin.io for account storage.",
                color = Color.White.copy(alpha = 0.35f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )

            Spacer(Modifier.height(40.dp))
        }
    }
}
