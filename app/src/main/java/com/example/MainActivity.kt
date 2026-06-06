package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Request Notification Permission and Setup Channel
        com.example.ui.NotificationHelper.createNotificationChannel(applicationContext)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this, arrayOf(permission), 101)
            }
        }

        setContent {
            MyApplicationTheme(darkTheme = viewModel.isDarkMode) {
                // Apply RTL and LTR Dynamics in Compose layout directly
                CompositionLocalProvider(LocalLayoutDirection provides Localization.layoutDirection) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigationContainer(viewModel)
                    }
                }
            }
        }
    }
}

// --- App Root Navigation States Switcher ---

@Composable
fun AppNavigationContainer(viewModel: MainViewModel) {
    val coroutineScope = rememberCoroutineScope()

    AnimatedContent(
        targetState = viewModel.currentScreen,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        label = "screen_transition"
    ) { screenState ->
        when (screenState) {
            is Screen.Splash -> {
                SplashScreen(onTimeout = {
                    coroutineScope.launch {
                        delay(2000) // Splash time hold
                        if (viewModel.currentUserEmail.isNotBlank()) {
                            viewModel.currentScreen = Screen.MainApp
                        } else {
                            viewModel.currentScreen = Screen.Onboarding
                        }
                    }
                })
            }
            is Screen.Onboarding -> {
                OnboardingScreen(
                    onOnboardingComplete = {
                        viewModel.currentScreen = Screen.Login
                    }
                )
            }
            is Screen.Login -> {
                LoginScreen(viewModel)
            }
            is Screen.Register -> {
                RegisterScreen(viewModel)
            }
            is Screen.ForgotPassword -> {
                ForgotPasswordScreen(viewModel)
            }
            is Screen.FamilySetup -> {
                FamilySetupScreen(viewModel)
            }
            is Screen.MainApp -> {
                MainAppLayout(viewModel)
            }
        }
    }
}

// --- SPLASH SCREEN ---

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) {
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(ForestGreen, Color(0xFF1B4D1F))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = GoldAmber,
                modifier = Modifier.size(110.dp),
                tonalElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Logo",
                        tint = DarkBackground,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            Text(
                text = "دارنا",
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
//                fontFamily = FontFamily.Serif,
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "DARNA",
                color = GoldAmber,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 4.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "المنصة الذكية لإدارة العائلة وتنسيق البيت الموحد",
                color = Color.White.copy(alpha = 0.82f),
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            CircularProgressIndicator(
                color = GoldAmber,
                modifier = Modifier.size(32.dp),
                strokeWidth = 3.dp
            )
        }
    }
}

// --- ONBOARDING SCREENS ---

@Composable
fun OnboardingScreen(onOnboardingComplete: () -> Unit) {
    var currentPage by remember { mutableStateOf(0) }
    
    val titles = listOf(
        Localization.get("onboarding_1_title"),
        Localization.get("onboarding_2_title"),
        Localization.get("onboarding_3_title")
    )
    
    val descs = listOf(
        Localization.get("onboarding_1_desc"),
        Localization.get("onboarding_2_desc"),
        Localization.get("onboarding_3_desc")
    )
    
    val icons = listOf(
        Icons.Default.ShoppingCart,
        Icons.Default.Kitchen,
        Icons.Default.Fastfood
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onOnboardingComplete) {
                    Text(
                        text = Localization.get("skip"),
                        color = ForestGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(160.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icons[currentPage],
                        contentDescription = "Onboarding Icon",
                        tint = ForestGreen,
                        modifier = Modifier.size(80.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = titles[currentPage],
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = descs[currentPage],
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Indicators
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                for (i in 0..2) {
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(if (i == currentPage) 16.dp else 10.dp)
                            .clip(CircleShape)
                            .background(if (i == currentPage) ForestGreen else SoftGray)
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1.2f))
            
            Button(
                onClick = {
                    if (currentPage < 2) {
                        currentPage++
                    } else {
                        onOnboardingComplete()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("onboarding_next_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (currentPage < 2) Localization.get("next") else Localization.get("get_started"),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// --- AUTHENTICATION FLOW: LOGIN ---

@Composable
fun LoginScreen(viewModel: MainViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var showErrorAlert by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                tint = ForestGreen,
                contentDescription = null,
                modifier = Modifier.size(72.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = Localization.get("login_title"),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Text(
                text = "دارنا / " + Localization.get("app_slogan"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(36.dp))
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(Localization.get("email_label")) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("email_input"),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = ForestGreen) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(Localization.get("password_label")) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("password_input"),
                leadingIcon = { Icon(Icons.Default.Password, contentDescription = null, tint = ForestGreen) },
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = ForestGreen
                        )
                    }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            
            // Forgot Password Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { viewModel.currentScreen = Screen.ForgotPassword }) {
                    Text(
                        text = Localization.get("forgot_password"),
                        color = ForestGreen,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (showErrorAlert) {
                Text(
                    text = Localization.get("invalid_credentials"),
                    color = ErrorRed,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            
            Button(
                onClick = {
                    if (email.isNotBlank() && password.length >= 6) {
                        viewModel.login(email, "") { success ->
                            if (!success) {
                                showErrorAlert = true
                            }
                        }
                    } else {
                        showErrorAlert = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("login_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = Localization.get("login_button"),
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            TextButton(onClick = { viewModel.currentScreen = Screen.Register }) {
                Text(
                    text = Localization.get("no_account"),
                    color = ForestGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

// --- AUTHENTICATION FLOW: REGISTER ---

@Composable
fun RegisterScreen(viewModel: MainViewModel) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var alertShow by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.PersonAdd,
                tint = ForestGreen,
                contentDescription = null,
                modifier = Modifier.size(72.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = Localization.get("register_title"),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(28.dp))
            
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(Localization.get("name_label")) },
                modifier = Modifier.fillMaxWidth().testTag("reg_name_input"),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = ForestGreen) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(Localization.get("email_label")) },
                modifier = Modifier.fillMaxWidth().testTag("reg_email_input"),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = ForestGreen) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text(Localization.get("phone_label")) },
                modifier = Modifier.fillMaxWidth().testTag("reg_phone_input"),
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = ForestGreen) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(Localization.get("password_label")) },
                modifier = Modifier.fillMaxWidth().testTag("reg_password_input"),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = ForestGreen) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (alertShow) {
                Text(
                    text = "يرجى تعبئة كافة الحقول بشكل صحيح.",
                    color = ErrorRed,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            
            Button(
                onClick = {
                    if (name.isNotBlank() && email.isNotBlank() && phone.isNotBlank()) {
                        viewModel.register(name, email, phone) { }
                    } else {
                        alertShow = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("register_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = Localization.get("register_button"),
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            TextButton(onClick = { viewModel.currentScreen = Screen.Login }) {
                Text(
                    text = Localization.get("has_account"),
                    color = ForestGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

// --- AUTHENTICATION FLOW: FORGOT PASSWORD ---

@Composable
fun ForgotPasswordScreen(viewModel: MainViewModel) {
    var email by remember { mutableStateOf("") }
    var sentNotice by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.LockReset,
                tint = ForestGreen,
                contentDescription = null,
                modifier = Modifier.size(72.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = Localization.get("forgot_password"),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(Localization.get("email_label")) },
                modifier = Modifier.fillMaxWidth().testTag("reset_email_input"),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = ForestGreen) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (sentNotice) {
                Text(
                    text = Localization.get("reset_sent_msg"),
                    color = ForestGreen,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            Button(
                onClick = {
                    if (email.isNotBlank()) {
                        sentNotice = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("send_reset_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = Localization.get("reset_password_btn"),
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            TextButton(onClick = { viewModel.currentScreen = Screen.Login }) {
                Text(
                    text = "الرجوع لتسجيل الدخول / Back to Login",
                    color = ForestGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// --- SETUP FAMILY OR JOIN SCREEN ---

@Composable
fun FamilySetupScreen(viewModel: MainViewModel) {
    var familyName by remember { mutableStateOf("") }
    var inviteCode by remember { mutableStateOf("") }
    var joinFailed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.GroupAdd,
                tint = ForestGreen,
                contentDescription = null,
                modifier = Modifier.size(72.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = Localization.get("family_setup_title"),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // SECTION A: CREATE NEW FAMILY
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = Localization.get("family_create_title"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = familyName,
                        onValueChange = { familyName = it },
                        label = { Text(Localization.get("family_name_label")) },
                        modifier = Modifier.fillMaxWidth().testTag("family_name_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    Button(
                        onClick = {
                            if (familyName.isNotBlank()) {
                                viewModel.createFamily(familyName)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(Localization.get("create_family_btn"), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // SECTION B: JOIN FAMILY
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = Localization.get("family_join_title"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = inviteCode,
                        onValueChange = { inviteCode = it },
                        label = { Text(Localization.get("enter_invite_code")) },
                        modifier = Modifier.fillMaxWidth().testTag("family_code_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    
                    if (joinFailed) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = Localization.get("invalid_invite"),
                            color = ErrorRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    Button(
                        onClick = {
                            viewModel.joinFamily(inviteCode) { success ->
                                if (!success) {
                                    joinFailed = true
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(Localization.get("join_family_btn"), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            TextButton(
                onClick = { 
                    // Support offline bypass straight to dashboard!
                    viewModel.currentScreen = Screen.MainApp 
                }
            ) {
                Text(
                    text = "تجاوز ودخول مؤقت كزائر / Skip Offline Bypass",
                    color = ForestGreen.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// --- MAIN APPLICATION: SHELL INTEGRATING TABS ---

@Composable
fun MainAppLayout(viewModel: MainViewModel) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    // Notification Alerts Pre-checker / Expiry alert toaster
    val recentNotifications by viewModel.notifications.collectAsState()
    
    Scaffold(
        bottomBar = {
            if (!isTablet) {
                AppBottomBar(
                    activeTab = viewModel.currentTab,
                    onTabSelected = { viewModel.currentTab = it },
                    notificationsCount = recentNotifications.count { !it.isRead }
                )
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isTablet) {
                AppNavigationRail(
                    activeTab = viewModel.currentTab,
                    onTabSelected = { viewModel.currentTab = it },
                    notificationsCount = recentNotifications.count { !it.isRead }
                )
                VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Offline internet alert banner
                AnimatedVisibility(visible = !viewModel.isOnline) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WifiOff,
                                contentDescription = "Offline Mode",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "أنت تتصفح بدون إنترنت حالياً. سنقوم بحفظ التغييرات ومزامنتها تلقائياً فور عودة الاتصال! 🌐",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    when (viewModel.currentTab) {
                        Tab.DASHBOARD -> DashboardScreen(viewModel)
                        Tab.SHOPPING -> ShoppingListScreen(viewModel)
                        Tab.INVENTORY -> InventoryScreen(viewModel)
                        Tab.MEALS -> MealPlannerScreen(viewModel)
                        Tab.TASKS -> TasksScreen(viewModel)
                        Tab.CHAT -> ChatScreen(viewModel)
                        Tab.EXPENSES -> ExpensesScreen(viewModel)
                        Tab.NOTIFICATIONS -> NotificationsScreen(viewModel)
                        Tab.PROFILE -> ProfileScreen(viewModel)
                        Tab.SETTINGS -> SettingsScreen(viewModel)
                        Tab.ABOUT -> AboutScreen()
                    }
                }
            }
        }
    }
}

// --- ADAPTIVE NAVIGATION ELEMENTS: BOTTOM BAR & RAIL ---

@Composable
fun AppBottomBar(activeTab: Tab, onTabSelected: (Tab) -> Unit, notificationsCount: Int) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        windowInsets = WindowInsets.navigationBars
    ) {
        val menuItems = listOf(
            Triple(Tab.DASHBOARD, Icons.Default.Dashboard, Localization.get("dashboard_tab")),
            Triple(Tab.SHOPPING, Icons.Default.ShoppingCart, Localization.get("shopping_tab")),
            Triple(Tab.INVENTORY, Icons.Default.Kitchen, Localization.get("inventory_tab")),
            Triple(Tab.MEALS, Icons.Default.Fastfood, Localization.get("meals_tab")),
            Triple(Tab.TASKS, Icons.Default.Assignment, Localization.get("tasks_tab")),
            Triple(Tab.CHAT, Icons.Default.Chat, Localization.get("chat_tab")),
            Triple(Tab.EXPENSES, Icons.Default.AccountBalanceWallet, Localization.get("expenses_tab")),
            Triple(Tab.SETTINGS, Icons.Default.Settings, Localization.get("settings_tab"))
        )

        menuItems.forEach { (tab, icon, label) ->
            NavigationBarItem(
                selected = activeTab == tab,
                onClick = { onTabSelected(tab) },
                icon = {
                    Box {
                        Icon(imageVector = icon, contentDescription = label)
                        if (tab == Tab.DASHBOARD && notificationsCount > 0) {
                            Badge(
                                containerColor = ErrorRed,
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Text(notificationsCount.toString(), color = Color.White, fontSize = 9.sp)
                            }
                        }
                    }
                },
                label = { Text(label, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = ForestGreen,
                    indicatorColor = ForestGreen,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
fun AppNavigationRail(activeTab: Tab, onTabSelected: (Tab) -> Unit, notificationsCount: Int) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surface,
        header = {
            Text(
                "دارنا",
                fontWeight = FontWeight.Bold,
                color = ForestGreen,
                fontSize = 20.sp,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }
    ) {
        val menuItems = listOf(
            Triple(Tab.DASHBOARD, Icons.Default.Dashboard, Localization.get("dashboard_tab")),
            Triple(Tab.SHOPPING, Icons.Default.ShoppingCart, Localization.get("shopping_tab")),
            Triple(Tab.INVENTORY, Icons.Default.Kitchen, Localization.get("inventory_tab")),
            Triple(Tab.MEALS, Icons.Default.Fastfood, Localization.get("meals_tab")),
            Triple(Tab.TASKS, Icons.Default.Assignment, Localization.get("tasks_tab")),
            Triple(Tab.CHAT, Icons.Default.Chat, Localization.get("chat_tab")),
            Triple(Tab.EXPENSES, Icons.Default.AccountBalanceWallet, Localization.get("expenses_tab")),
            Triple(Tab.SETTINGS, Icons.Default.Settings, Localization.get("settings_tab"))
        )

        menuItems.forEach { (tab, icon, label) ->
            NavigationRailItem(
                selected = activeTab == tab,
                onClick = { onTabSelected(tab) },
                icon = {
                    Box {
                        Icon(imageVector = icon, contentDescription = label)
                        if (tab == Tab.DASHBOARD && notificationsCount > 0) {
                            Badge(containerColor = ErrorRed, modifier = Modifier.align(Alignment.TopEnd)) {
                                Text(notificationsCount.toString())
                            }
                        }
                    }
                },
                label = { Text(label) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = Color.White,
                    indicatorColor = ForestGreen
                )
            )
        }
    }
}

// --- SUB-SCREEN A: HOME DASHBOARD ---

@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val shopping by viewModel.shoppingList.collectAsState()
    val inventory by viewModel.inventoryList.collectAsState()
    val tasks by viewModel.taskList.collectAsState()
    val expenses by viewModel.expenseList.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    val pendingShopCount = shopping.count { !it.isPurchased }
    val nearExpiryCount = inventory.count { 
        // Simulated near expiry
        it.name.contains("أسنان") || it.name.contains("ليمون")
    }
    val lowStockCount = inventory.count { it.quantity <= it.limitThreshold }
    val assignedPendingTasks = tasks.count { !it.isCompleted && it.assignedMemberName.contains(viewModel.currentUserName) }

    val monthExpenseSum = expenses.sumOf { it.amount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcomer Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ForestGreen),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            // Artistic green canvas elements
                            drawCircle(
                                color = SageGreen.copy(alpha = 0.25f),
                                radius = 240.dp.toPx(),
                                center = Offset(size.width * 0.9f, size.height * 0.1f)
                            )
                        }
                        .padding(24.dp)
                ) {
                    Column(modifier = Modifier.align(Alignment.CenterStart)) {
                        Text(
                            text = Localization.get("welcome") + " ${viewModel.currentUserName} 👋",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "${Localization.get("role")}: " + if (viewModel.currentUserRole == "Admin") Localization.get("admin") else Localization.get("member"),
                            color = GoldAmber,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = viewModel.currentFamilyName,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Quick Alarm Bell Inbox (If has warnings)
        if (notifications.any { !it.isRead }) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.currentTab = Tab.NOTIFICATIONS },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.12f)),
                    border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "تنبيه! هناك سلع توشك صلاحيتها على الانتهاء بالمخزون. اضغط للاطلاع.",
                            color = ErrorRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Core summary cards Row
        item {
            Text(
                text = Localization.get("family_summary"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // SHOPPING CARD
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.currentTab = Tab.SHOPPING },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Shop", tint = ForestGreen)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(Localization.get("shopping_summary"), fontSize = 12.sp, color = Color.Gray)
                        Text("$pendingShopCount", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                    }
                }

                // INVENTORY ALERT CARD
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.currentTab = Tab.INVENTORY },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Icon(Icons.Default.Kitchen, contentDescription = "Inv", tint = GoldAmber)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(Localization.get("low_stock"), fontSize = 12.sp, color = Color.Gray)
                        Text("$lowStockCount", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = GoldAmber)
                    }
                }
            }
        }

        // Second Row: Tasks & Expirations info
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // MY TASKS
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.currentTab = Tab.TASKS },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Icon(Icons.Default.AssignmentInd, contentDescription = "Tasks", tint = ForestGreen)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(Localization.get("upcoming_tasks"), fontSize = 12.sp, color = Color.Gray)
                        Text("$assignedPendingTasks", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                    }
                }

                // EXPIRING SOON PANTRY ALERT
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.currentTab = Tab.NOTIFICATIONS },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Icon(Icons.Default.HourglassBottom, contentDescription = "Expired", tint = ErrorRed)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(Localization.get("expiring_soon"), fontSize = 12.sp, color = Color.Gray)
                        Text("$nearExpiryCount", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = ErrorRed)
                    }
                }
            }
        }

        // Budget summary limit progress bar
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.currentTab = Tab.EXPENSES },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = Localization.get("monthly_expenses"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    val ratio = (monthExpenseSum / viewModel.budgetLimit).coerceIn(0.0, 1.0).toFloat()
                    val colorFill = when {
                        ratio >= 1.0f -> ErrorRed
                        ratio >= 0.8f -> GoldAmber
                        else -> ForestGreen
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "$monthExpenseSum ${viewModel.getCurrencySymbol()}", fontWeight = FontWeight.Bold, color = colorFill)
                        Text(text = "${viewModel.budgetLimit} ${viewModel.getCurrencySymbol()}", fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { ratio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = colorFill,
                        trackColor = SoftGray
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (ratio >= 1.0f) "تجاوزت الميزانية المحددة للمنزل!" else if (ratio >= 0.8f) "اقتربت من بلوغ ميزانية المصروفات!" else "النفقات ضمن نطاق الميزانية الآمن والممتاز ✔️",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorFill
                    )
                }
            }
        }

        // App Cloud Control Dashboard Panel
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.4f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Header of the Panel
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                tint = ForestGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "لوحة تحكم المزامنة العائلية المشتركة",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = ForestGreen
                            )
                        }
                        
                        // Small Active Indicator
                        Surface(
                            shape = CircleShape,
                            color = ForestGreen.copy(alpha = 0.15f),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(ForestGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("نشط", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                    // Sync details & Force trigger
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("الرمز العائلي: ${viewModel.currentFamilyId}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GoldAmber)
                            Text(
                                text = "حالة الاتصال: متصل سحابياً", 
                                fontSize = 11.sp, 
                                color = Color.Gray
                            )
                            val formattedTime = if (viewModel.lastSyncTimestamp > 0) {
                                val elapsedSec = (System.currentTimeMillis() - viewModel.lastSyncTimestamp) / 1000
                                if (elapsedSec < 10) "الآن" else "$elapsedSec ثانية مضت"
                            } else {
                                "مستمر"
                            }
                            Text(
                                text = "آخر تحديث تلقائي: $formattedTime", 
                                fontSize = 11.sp, 
                                color = Color.Gray
                            )
                        }

                        Button(
                            onClick = { viewModel.syncWithCloud() },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تحديث سريع", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    // Sync Indicators per Module Grid
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text("مؤشرات مزامنة البيانات المشتركة بين الأجهزة:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = ForestGreen)
                        
                        val modules = listOf(
                            "المشتريات والطلبات" to "مزامنة لحظية نشطة ✔️",
                            "مخزون السلع والأطعمة" to "مزامنة لحظية نشطة ✔️",
                            "المهام والأشغال المنزلية" to "مزامنة تفاعلية ✔️",
                            "دردشة وملاحظات العائلة" to "مزامنة تلقائية ✔️",
                            "الأدوار والرتب المشتركة" to "تعديل لحظي نشط ✔️"
                        )

                        modules.forEach { (module, status) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(ForestGreen))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(module, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Text(status, fontSize = 11.sp, color = ForestGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Shortcuts list section
        item {
            Text(
                text = Localization.get("quick_access"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val tools = listOf(
                    Triple(Tab.MEALS, Icons.Default.SmartToy, "اقتراح وجبة بالذكاء"),
                    Triple(Tab.CHAT, Icons.Default.StickyNote2, "الملاحظات العائلية"),
                    Triple(Tab.PROFILE, Icons.Default.ManageAccounts, "بطاقة العائلة"),
                    Triple(Tab.ABOUT, Icons.Default.HelpOutline, "معلومات حول")
                )

                items(tools) { (tab, icon, title) ->
                    AssistChip(
                        onClick = { viewModel.currentTab = tab },
                        label = { Text(title, fontWeight = FontWeight.Bold) },
                        leadingIcon = { Icon(icon, contentDescription = null, tint = ForestGreen) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }
    }
}

// --- SUB-SCREEN B: SHOPPING LIST ---

@Composable
fun ShoppingListScreen(viewModel: MainViewModel) {
    val items by viewModel.shoppingList.collectAsState()
    var searchWord by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }

    // Direct edit form values
    var itemName by remember { mutableStateOf("") }
    var itemQuant by remember { mutableStateOf(1.0) }
    var itemUnit by remember { mutableStateOf("حبة") }
    var itemCategory by remember { mutableStateOf("cat_food") }

    val categories = listOf(
        "" to "الكل / All",
        "cat_food" to Localization.get("cat_food"),
        "cat_cleaning" to Localization.get("cat_cleaning"),
        "cat_personal" to Localization.get("cat_personal"),
        "cat_other" to Localization.get("cat_other")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {
        Text(
            text = Localization.get("shopping_tab"),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = ForestGreen
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Direct Item Creation Panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = Localization.get("add_item"),
                    fontWeight = FontWeight.Bold,
                    color = ForestGreen,
                    fontSize = 14.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text(Localization.get("item_name_label")) },
                    modifier = Modifier.fillMaxWidth().testTag("shop_item_name_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Category Selection
                    Box(modifier = Modifier.weight(1.2f)) {
                        var expandedCat by remember { mutableStateOf(false) }
                        OutlinedButton(
                            onClick = { expandedCat = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = categories.find { it.first == itemCategory }?.second ?: "فئة",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        DropdownMenu(
                            expanded = expandedCat,
                            onDismissRequest = { expandedCat = false }
                        ) {
                            categories.filter { it.first.isNotEmpty() }.forEach { (key, title) ->
                                DropdownMenuItem(
                                    text = { Text(title) },
                                    onClick = {
                                        itemCategory = key
                                        expandedCat = false
                                    }
                                )
                            }
                        }
                    }

                    // Quantity Counter
                    Row(
                        modifier = Modifier
                            .weight(1.3f)
                            .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = { if (itemQuant > 1) itemQuant-- }) {
                            Icon(Icons.Default.Remove, contentDescription = null, tint = ForestGreen)
                        }
                        Text("$itemQuant", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        IconButton(onClick = { itemQuant++ }) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = ForestGreen)
                        }
                    }

                    // Unit selector
                    Box(modifier = Modifier.weight(1f)) {
                        var expandedUnit by remember { mutableStateOf(false) }
                        val unitsList = listOf("حبة", "كجم", "لتر", "علبة", "كيس")
                        OutlinedButton(
                            onClick = { expandedUnit = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(itemUnit)
                        }
                        DropdownMenu(expanded = expandedUnit, onDismissRequest = { expandedUnit = false }) {
                            unitsList.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit) },
                                    onClick = {
                                        itemUnit = unit
                                        expandedUnit = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        if (itemName.isNotBlank()) {
                            viewModel.addShoppingItem(itemName, itemCategory, itemQuant, itemUnit)
                            // reset
                            itemName = ""
                            itemQuant = 1.0
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("add_shop_item_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(Localization.get("add_item"), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { (key, name) ->
                FilterChip(
                    selected = selectedCategory == key,
                    onClick = { selectedCategory = key },
                    label = { Text(name, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ForestGreen,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Dynamic items list
        val filteredList = items.filter { item ->
            val matchesCategory = selectedCategory.isEmpty() || item.category == selectedCategory
            matchesCategory
        }

        if (filteredList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "القائمة فارغة، أضف مواد للشراء أولاً",
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredList) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.isPurchased) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item.isPurchased,
                                onCheckedChange = { viewModel.toggleShoppingItemPurchased(item) },
                                colors = CheckboxDefaults.colors(checkedColor = ForestGreen)
                            )
                            
                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.isPurchased) Color.Gray else MaterialTheme.colorScheme.onSurface,
                                    style = if (item.isPurchased) MaterialTheme.typography.bodyMedium.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough) else MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "${Localization.get("quantity")}: ${item.quantity} ${item.unit}",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "• ${categories.find { it.first == item.category }?.second ?: ""}",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                                Text(
                                    text = "${Localization.get("added_by")}: ${item.createdBy}",
                                    fontSize = 10.sp,
                                    color = Color.Gray.copy(alpha = 0.8f)
                                )
                            }

                            IconButton(onClick = { viewModel.deleteShoppingItem(item) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SUB-SCREEN C: HOME INVENTORY ---

@Composable
fun InventoryScreen(viewModel: MainViewModel) {
    val items by viewModel.inventoryList.collectAsState()
    var searchWord by remember { mutableStateOf("") }
    
    // Add product form values
    var showAddDialog by remember { mutableStateOf(false) }
    var prodName by remember { mutableStateOf("") }
    var prodCategory by remember { mutableStateOf("cat_food") }
    var prodQuant by remember { mutableStateOf(1.0) }
    var prodUnit by remember { mutableStateOf("كجم") }
    var prodExpiry by remember { mutableStateOf("2026-06-30") }

    val categories = listOf(
        "cat_food" to Localization.get("cat_food"),
        "cat_cleaning" to Localization.get("cat_cleaning"),
        "cat_personal" to Localization.get("cat_personal"),
        "cat_other" to Localization.get("cat_other")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = Localization.get("inventory_tab"),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = ForestGreen
            )

            // Dynamic scan toggle or button
            FilledIconButton(
                onClick = { viewModel.isScannerActive = true },
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = ForestGreen)
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Scanner Active HUD Overlay
        if (viewModel.isScannerActive) {
            BarcodeScannerMockHUD(
                onScanSuccess = { barcode ->
                    viewModel.triggerSimulateBarcodeScan(barcode)
                },
                onCancel = { viewModel.isScannerActive = false }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(Localization.get("add_product"), color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        OutlinedTextField(
            value = searchWord,
            onValueChange = { searchWord = it },
            placeholder = { Text("ابحث عن سبل وأغذية بالمخزون...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ForestGreen) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        val filteredInventory = items.filter { it.name.contains(searchWord, ignoreCase = true) }

        if (filteredInventory.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("المستودع فارغ حالياً.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                items(filteredInventory) { item ->
                    val nearExpiry = item.name.contains("أسنان") || item.name.contains("ليمون")
                    val isExpired = false // Simulated
                    
                    val cardBorder = when {
                        isExpired -> BorderStroke(1.5.dp, ErrorRed)
                        nearExpiry -> BorderStroke(1.5.dp, GoldAmber)
                        else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("inventory_item_${item.id}"),
                        shape = RoundedCornerShape(12.dp),
                        border = cardBorder,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Expiration Visual Dot/Icon
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            color = if (nearExpiry) GoldAmber.copy(alpha = 0.15f) else ForestGreen.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(8.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (nearExpiry) Icons.Default.Timer else Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (nearExpiry) GoldAmber else ForestGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = item.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${Localization.get("quantity") ?: "الكمية"}: ${item.quantity} ${item.unit}",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "|",
                                            fontSize = 12.sp,
                                            color = Color.LightGray
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "انتهاء: ${item.expiryDate}",
                                            fontSize = 11.sp,
                                            color = if (nearExpiry) GoldAmber else Color.Gray,
                                            fontWeight = if (nearExpiry) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                    if (item.barcode.isNotEmpty()) {
                                        Text(
                                            text = "رمز الباركود: ${item.barcode}",
                                            fontSize = 9.sp,
                                            color = Color.Gray.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }

                            // Delete Action button
                            IconButton(onClick = { viewModel.deleteInventoryItem(item) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = ErrorRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Add Product Dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text(Localization.get("add_product"), fontWeight = FontWeight.Bold, color = ForestGreen) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = prodName,
                            onValueChange = { prodName = it },
                            label = { Text("اسم السلعة") },
                            modifier = Modifier.fillMaxWidth().testTag("inv_name_input"),
                            singleLine = true
                        )

                        // Quantity input
                        OutlinedTextField(
                            value = "$prodQuant",
                            onValueChange = { prodQuant = it.toDoubleOrNull() ?: 1.0 },
                            label = { Text(Localization.get("quantity")) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        // Unit dropdown placeholder
                        OutlinedTextField(
                            value = prodUnit,
                            onValueChange = { prodUnit = it },
                            label = { Text(Localization.get("unit")) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Expiry date input
                        OutlinedTextField(
                            value = prodExpiry,
                            onValueChange = { prodExpiry = it },
                            label = { Text("تاريخ الانتهاء YYYY-MM-DD") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        onClick = {
                            if (prodName.isNotBlank()) {
                                viewModel.addInventoryItem(prodName, prodCategory, prodQuant, prodUnit, prodExpiry)
                                showAddDialog = false
                                // reset
                                prodName = ""
                            }
                        }
                    ) {
                        Text("حفظ السلعة", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text(Localization.get("cancel"), color = ForestGreen)
                    }
                }
            )
        }
    }
}

// --- BARCODE SCANNER MOCK M3 HUD SCREEN ---

@Composable
fun BarcodeScannerMockHUD(onScanSuccess: (String) -> Unit, onCancel: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkBackground),
        border = BorderStroke(2.dp, ForestGreen)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "عدسة مسح الباركود الذكية / Barcode Camera",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                IconButton(onClick = onCancel) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Hologram bounding box mockup
            Box(
                modifier = Modifier
                    .size(width = 240.dp, height = 110.dp)
                    .border(2.dp, GoldAmber, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "وجه الباركود داخل الصندوق التفاعلي\nAlign the Barcode here",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "حدد عينة باركود سريعة للتعرف عليها فوراً:",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onScanSuccess("6281013002") },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Text("عصير المراعي", color = Color.White, fontSize = 10.sp)
                }

                Button(
                    onClick = { onScanSuccess("4005900135") },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Text("كريم نيفيا", color = Color.White, fontSize = 10.sp)
                }

                Button(
                    onClick = { onScanSuccess("3251241123") },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Text("مناديل مطبخ", color = Color.White, fontSize = 10.sp)
                }
            }
        }
    }
}

// --- Parsed AI Meal Model ---
data class ParsedMeal(
    val title: String,
    val details: String,
    val ingredients: String = "",
    val instructions: String = ""
)

fun parseAiMealResult(rawText: String): List<ParsedMeal> {
    if (rawText.isBlank()) return emptyList()
    val list = mutableListOf<ParsedMeal>()
    
    // Split on typical markdown headings, numbers, or specific words
    val potentialBlocks = rawText.split(Regex("(?=###|##|\\b\\d\\.\\s|\\*\\*\\d\\.|-\\s*الوجبة|الوجبة الأولى|الوجبة الثانية|الوجبة الثالثة|الوجبة:)"))
    
    for (block in potentialBlocks) {
        val trimmed = block.trim()
        if (trimmed.isEmpty()) continue
        
        val lines = trimmed.split("\n")
        val rawTitle = lines.firstOrNull() ?: ""
        var cleanTitle = rawTitle
            .replace("#", "")
            .replace("*", "")
            .trim()
            
        // Clean leading numbers like "1. " or "الوجبة الأولى: "
        cleanTitle = cleanTitle.replace(Regex("^\\d+\\.\\s*"), "")
        if (cleanTitle.startsWith(":")) {
            cleanTitle = cleanTitle.drop(1).trim()
        }
        
        if (cleanTitle.length < 3) continue
        
        val details = lines.drop(1).joinToString("\n").trim()
        if (details.length < 10) continue
        
        // Extract ingredients/instructions from sublines
        val ingredsList = mutableListOf<String>()
        val instrsList = mutableListOf<String>()
        var mode = 0 // 0 = general, 1 = ingredients, 2 = instructions
        
        for (line in lines.drop(1)) {
            val l = line.trim()
            if (l.contains("المقادير") || l.contains("المكونات") || l.contains("مقادير") || l.contains("ingredients") || l.contains("Ingredients")) {
                mode = 1
                continue
            }
            if (l.contains("التحضير") || l.contains("الاعداد") || l.contains("الإعداد") || l.contains("طريقة") || l.contains("instructions") || l.contains("Instructions")) {
                mode = 2
                continue
            }
            if (mode == 1 && l.isNotBlank()) {
                val cleanItem = l.replace(Regex("^[-*]\\s*"), "").trim()
                if (cleanItem.isNotBlank() && cleanItem.length > 1) {
                    ingredsList.add(cleanItem)
                }
            } else if (mode == 2 && l.isNotBlank()) {
                val cleanStep = l.replace(Regex("^[-*\\d.]\\s*"), "").trim()
                if (cleanStep.isNotBlank() && cleanStep.length > 1) {
                    instrsList.add(cleanStep)
                }
            }
        }
        
        list.add(ParsedMeal(
            title = cleanTitle,
            details = details,
            ingredients = ingredsList.joinToString("، ").ifBlank { "مكونات جزائرية تقليدية طازجة" },
            instructions = instrsList.joinToString("\n").ifBlank { "اتبع طريقة التحضير الموصوفة بالدليل." }
        ))
    }
    
    // Fallback if list empty
    if (list.isEmpty()) {
        val nonHashLines = rawText.split("\n").filter { it.isNotBlank() && !it.startsWith("#") }
        val fallbackTitle = nonHashLines.firstOrNull()?.replace("*", "")?.trim() ?: "وجبة مقترحة بذكاء دارنا الذكي ✨"
        list.add(ParsedMeal(
            title = if (fallbackTitle.length < 50) fallbackTitle else "وجبة جزائرية مقترحة ✨",
            details = rawText,
            ingredients = "مكونات جزائرية متوفرة في المطبخ",
            instructions = "مدرجة تحت التفاصيل."
        ))
    }
    
    return list
}

// --- SUB-SCREEN D: MEAL PLANNING & AI SUGGESTIONS ---

@Composable
fun MealPlannerScreen(viewModel: MainViewModel) {
    val meals by viewModel.mealPlanner.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    var showAddMealForm by remember { mutableStateOf(false) }
    var mealNameInput by remember { mutableStateOf("") }
    var mealTypeInput by remember { mutableStateOf("Breakfast") }
    var mealIngredsInput by remember { mutableStateOf("") }
    var mealInstructionsInput by remember { mutableStateOf("") }
    var expandedMealId by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = Localization.get("meals_tab"),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = ForestGreen
        )

        // SECTION A: AI SUGGESTIONS SECTION (POWERED BY GEMINI)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = ForestGreen.copy(alpha = 0.08f)),
            border = BorderStroke(1.5.dp, ForestGreen)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SmartToy, contentDescription = null, tint = ForestGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Localization.get("ai_suggestions"),
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen,
                        fontSize = 16.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = "سنقوم بمسح مخزون طعامك واقتراح وجبات شهية لمنع هدر المكونات القريبة للانتهاء.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { viewModel.requestAIMealSuggestions() },
                    modifier = Modifier.fillMaxWidth().testTag("ai_meal_generate_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                ) {
                    if (viewModel.isGeneratingMeals) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(Localization.get("generating_ai"), color = Color.White)
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(Localization.get("generate_ai_meals"), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                if (viewModel.aiMealResult.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    Text(
                        text = Localization.get("ai_results_desc") ?: "الوجبات المقترحة بذكاء الاصطناعي:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = ForestGreen
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    val parsedMeals = remember(viewModel.aiMealResult) { parseAiMealResult(viewModel.aiMealResult) }
                    var expandedMealIndex by remember { mutableStateOf<Int?>(null) }

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        parsedMeals.forEachIndexed { index, meal ->
                            val isExpanded = expandedMealIndex == index
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expandedMealIndex = if (isExpanded) null else index },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.3f)),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isExpanded) ForestGreen.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.RestaurantMenu,
                                                contentDescription = null,
                                                tint = ForestGreen,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = meal.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = ForestGreen
                                            )
                                        }
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = "Details",
                                            tint = ForestGreen
                                        )
                                    }

                                    if (isExpanded) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        HorizontalDivider(color = ForestGreen.copy(alpha = 0.15f))
                                        Spacer(modifier = Modifier.height(10.dp))
                                        
                                        Text(
                                            text = meal.details,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            lineHeight = 18.sp
                                        )

                                        Spacer(modifier = Modifier.height(14.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                colors = ButtonDefaults.buttonColors(containerColor = GoldAmber),
                                                onClick = {
                                                    viewModel.applyAISuggestionToPlanner(
                                                        name = meal.title,
                                                        ingredients = meal.ingredients,
                                                        instructions = meal.instructions
                                                    )
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                            ) {
                                                Icon(Icons.Default.PlaylistAddCheck, contentDescription = null, tint = DarkBackground, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("اعتماد 🍽️", color = DarkBackground, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            Button(
                                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                                                onClick = {
                                                    viewModel.shareMealToChat(
                                                        mealName = meal.title,
                                                        ingredients = meal.ingredients,
                                                        instructions = meal.details
                                                    )
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                            ) {
                                                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("مشاركة 💬", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // SECTION B: ALGERIAN CUISINE QUICK SUGGESTIONS
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Restaurant, contentDescription = null, tint = ForestGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "اقتراحات سريعة للمطبخ الجزائري الدافئ 🇩🇿",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ForestGreen
                    )
                }

                Text(
                    text = "اضغط على أي وجبة لتخطيطها وإضافتها مباشرة لجدول العائلة:",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val algerianMeals = listOf(
                        Triple("كسكسي جزائري عاصمي 🇩🇿", "Lunch", "سميد كسكس، مرق أبيض باللفت والحمص، دجاج وقرفة"),
                        Triple("شربة فريك حمراء 🍲", "Dinner", "فريك قمح، طماطم مصبرة، كزبرة ونعناع، دجاج أو لحم"),
                        Triple("طاجين الزيتون اللذيذ 🫒", "Lunch", "زيتون أخضر، جزر، لحم دجاج بالبصل وصلصة الليمون"),
                        Triple("رشتة جزائرية بالدجاج 🍜", "Lunch", "خيوط الرشتة الطازجة، لفت أبيض، مرق دجاج وقرفة وحمص"),
                        Triple("شكشوكة حارة بالبيض 🍳", "Dinner", "فلفل حلو وحار، طماطم، ثوم، بيض بلدي وزيت زيتون أصيل"),
                        Triple("بغرير شهي دافئ 🍯", "Breakfast", "سميد رقيق، خميرة خبز، عسل حر وزبدة ذائبة")
                    )

                    algerianMeals.forEach { (name, type, ingreds) ->
                        Card(
                            modifier = Modifier
                                .width(160.dp)
                                .clickable {
                                    viewModel.planMeal(
                                        "2026-06-02",
                                        type,
                                        name,
                                        ingreds,
                                        "مطهي بالطريقة الجزائرية التقليدية العائلية."
                                    )
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = ForestGreen.copy(alpha = 0.05f)),
                            border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.15f))
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = when (type) {
                                        "Breakfast" -> "فطور الصباح 🌅"
                                        "Lunch" -> "الغذاء 🍲"
                                        else -> "العشاء 🌙"
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SageGreen
                                )
                                Text(
                                    text = ingreds,
                                    fontSize = 9.sp,
                                    color = Color.DarkGray,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // SECTION B: STANDARD DAILY PLANNER LIST
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "جدول وجبات العائلة الفعلي",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = { showAddMealForm = !showAddMealForm }) {
                Icon(
                    imageVector = if (showAddMealForm) Icons.Default.Close else Icons.Default.AddCircle,
                    contentDescription = null,
                    tint = ForestGreen,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Add Planned meal toggle form
        if (showAddMealForm) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("إضافة وجبة للمجدول", fontWeight = FontWeight.Bold, color = ForestGreen)

                    OutlinedTextField(
                        value = mealNameInput,
                        onValueChange = { mealNameInput = it },
                        label = { Text("اسم الوجبة") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Meal type selector Breakfast, Lunch, Dinner
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Breakfast", "Lunch", "Dinner").forEach { type ->
                            FilterChip(
                                selected = mealTypeInput == type,
                                onClick = { mealTypeInput = type },
                                label = { Text(type) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = mealIngredsInput,
                        onValueChange = { mealIngredsInput = it },
                        label = { Text("المقادير / المقترحات") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (mealNameInput.isNotBlank()) {
                                viewModel.planMeal("2026-06-02", mealTypeInput, mealNameInput, mealIngredsInput, "خطوات تحضير يدوية")
                                showAddMealForm = false
                                mealNameInput = ""
                                mealIngredsInput = ""
                            }
                        }
                    ) {
                        Text("حفظ بمخطط الوجبات", color = Color.White)
                    }
                }
            }
        }

        // Meals rendering
        val types = listOf(
            "Breakfast" to Localization.get("breakfast"),
            "Lunch" to Localization.get("lunch"),
            "Dinner" to Localization.get("dinner")
        )

        types.forEach { (typeKey, typeLabel) ->
            Text(
                text = typeLabel,
                fontWeight = FontWeight.Bold,
                color = ForestGreen,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            val currentTypeMeals = meals.filter { it.mealType == typeKey }

            if (currentTypeMeals.isEmpty()) {
                Text(
                    text = "لا توجد وجبات مخططة لليوم.",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 12.dp)
                )
            } else {
                currentTypeMeals.forEach { item ->
                    val isExpanded = expandedMealId == item.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedMealId = if (isExpanded) null else item.id },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isExpanded) ForestGreen.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, if (isExpanded) ForestGreen.copy(alpha = 0.5f) else Color.Transparent),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RestaurantMenu,
                                    contentDescription = null,
                                    tint = if (isExpanded) ForestGreen else SageGreen
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (isExpanded) ForestGreen else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isExpanded) "انقر لإخفاء المكونات والطريقة" else "انقر للاطلاع على المقادير وتفاصيل التحضير 🍽️",
                                        fontSize = 10.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { viewModel.deletePlannedMeal(item) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = ErrorRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = ForestGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            AnimatedVisibility(
                                visible = isExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(top = 10.dp)
                                        .fillMaxWidth()
                                ) {
                                    HorizontalDivider(
                                        color = ForestGreen.copy(alpha = 0.15f),
                                        thickness = 1.dp,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )

                                    if (item.ingredients.isNotEmpty()) {
                                        Text(
                                            text = "المقادير والمكونات اللازمة للطبخ 🍅:",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = ForestGreen
                                        )
                                        
                                        // Build highly stylized list of ingredients
                                        val ingredientList = item.ingredients.split(Regex("[,،و]")).map { it.trim() }.filter { it.isNotEmpty() }
                                        ingredientList.forEach { ingred ->
                                            Row(
                                                modifier = Modifier.padding(vertical = 2.dp, horizontal = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .background(GoldAmber, CircleShape)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(text = ingred, fontSize = 12.sp)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Balanced actions row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    ingredientList.forEach { ingred ->
                                                        viewModel.addShoppingItem(
                                                            name = ingred,
                                                            category = "cat_food",
                                                            quantity = 1.0,
                                                            unit = "حبة"
                                                        )
                                                    }
                                                    // Trigger notification
                                                    viewModel.syncWithCloud()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = GoldAmber),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                                modifier = Modifier.height(34.dp).weight(1.1f)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AddShoppingCart,
                                                    contentDescription = null,
                                                    tint = DarkBackground,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "إضافة المقادير للتسوق 🛒",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = DarkBackground
                                                )
                                            }

                                            Button(
                                                onClick = {
                                                    viewModel.shareMealToChat(item.name, item.ingredients, item.instructions)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                                modifier = Modifier.height(34.dp).weight(0.9f)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Share,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "مشاركة مع العائلة 💬",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(12.dp))
                                    }

                                    Text(
                                        text = "خطوات التحضير والإعداد 👩‍🍳:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = ForestGreen
                                    )
                                    Text(
                                        text = if (item.instructions.isNotBlank()) item.instructions else "خطوات تحضير يدوية وتنسيق عائلي مشترك لتحضير أشهى المأكولات الجزائرية والعالمية.",
                                        fontSize = 11.sp,
                                        color = Color.DarkGray,
                                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SUB-SCREEN E: FAMILY CHORES & ASSIGNED TASKS ---

@Composable
fun TasksScreen(viewModel: MainViewModel) {
    val tasks by viewModel.taskList.collectAsState()
    val userList by viewModel.userList.collectAsState()
    
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskTitle by remember { mutableStateOf("") }
    var taskDesc by remember { mutableStateOf("") }
    var taskAssignee by remember { mutableStateOf("الكل / All") }
    var taskDueDate by remember { mutableStateOf("2026-06-06") }
    var taskRecur by remember { mutableStateOf("None") }
    
    var selectedFilter by remember { mutableStateOf("Pending") } // "All", "Mine", "Pending", "Completed"

    val completedTasksCount = tasks.count { it.isCompleted }
    val totalTasks = tasks.size
    val completionPercentage = if (totalTasks > 0) (completedTasksCount * 100) / totalTasks else 0

    // Filter tasks accordingly
    val filteredTasks = when (selectedFilter) {
        "Mine" -> tasks.filter { 
            it.assignedMemberName.trim().equals(viewModel.currentUserName.trim(), ignoreCase = true) || 
            it.assignedMemberName == "الكل / All" ||
            (viewModel.currentUserEmail.isNotBlank() && it.assignedMemberName.trim().equals(viewModel.currentUserEmail.trim(), ignoreCase = true))
        }
        "Pending" -> tasks.filter { !it.isCompleted }
        "Completed" -> tasks.filter { it.isCompleted }
        else -> tasks
    }

    // Populate active dynamic members to assign chores
    val familyMembers = userList.filter { it.familyId == viewModel.currentFamilyId }
    val memberNames = (listOf("الكل / All", viewModel.currentUserName) + familyMembers.map { it.name }).distinct().filter { it.isNotBlank() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = Localization.get("tasks_tab"),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = ForestGreen
            )
            Icon(Icons.Default.Task, contentDescription = null, tint = ForestGreen)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // FAMILY ACHIEVEMENTS PROGRESS CARD WITH A REFINED TEAMWORK LOOK
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = GoldAmber.copy(alpha = 0.08f)),
            border = BorderStroke(1.dp, GoldAmber.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = GoldAmber,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = DarkBackground)
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = Localization.get("family_achievements") + " 🎖️",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7D5F00),
                            fontSize = 13.sp
                        )
                        Text(
                            text = "تم إنجاز $completedTasksCount مهمة من أصل $totalTasks من مهمات البيت المنسقة.",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (completionPercentage >= 100 && totalTasks > 0) "عمل جبار! تم إتمام كل المهام 🎉" else "معدل تعاون عائلة دارنا: $completionPercentage%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )
                    }
                    LinearProgressIndicator(
                        progress = { if (totalTasks > 0) completedTasksCount.toFloat() / totalTasks else 0.0f },
                        color = ForestGreen,
                        trackColor = ForestGreen.copy(alpha = 0.15f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(Color.Transparent, RoundedCornerShape(3.dp))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = { 
                // Auto-set default assignee to current user
                taskAssignee = viewModel.currentUserName
                showAddTaskDialog = true 
            },
            modifier = Modifier.fillMaxWidth().testTag("add_task_trigger"),
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.PlaylistAdd, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(Localization.get("add_task"), color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // FILTER CHIPS ROW
        Text(
            text = "تصفية وتنظيم المهام حسب الحالة والتعاون:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf(
                "All" to "الكل 📋",
                "Mine" to "مهامي الصيفية 👤",
                "Pending" to "قيد التنفيذ ⏳",
                "Completed" to "المكتملة بنجاح ✅"
            )
            filters.forEach { (key, label) ->
                FilterChip(
                    selected = selectedFilter == key,
                    onClick = { selectedFilter = key },
                    label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ForestGreen.copy(alpha = 0.12f),
                        selectedLabelColor = ForestGreen
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f), 
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.TaskAlt, contentDescription = null, tint = SageGreen, modifier = Modifier.size(36.dp))
                    Text("لا توجد مهام مطابقة للتصفية الحالية.", color = Color.Gray, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredTasks) { task ->
                    val isNearDue = true // Stylized indicator
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, if (task.isCompleted) Color.Transparent else ForestGreen.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = task.isCompleted,
                                onCheckedChange = { viewModel.toggleTaskCompletion(task) },
                                colors = CheckboxDefaults.colors(checkedColor = ForestGreen)
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
 
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (task.isCompleted) Color.Gray else MaterialTheme.colorScheme.onSurface,
                                    style = if (task.isCompleted) MaterialTheme.typography.bodyMedium.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough) else MaterialTheme.typography.bodyMedium
                                )
                                if (task.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = task.description,
                                        fontSize = 11.sp,
                                        color = if (task.isCompleted) Color.LightGray else Color.Gray
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(ForestGreen.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "👤 المكلف: ${task.assignedMemberName}",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ForestGreen
                                        )
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .background(ErrorRed.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "📅 تاريخ: ${task.dueDate}",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ErrorRed
                                        )
                                    }
 
                                    if (task.recurrence != "None") {
                                        Box(
                                            modifier = Modifier
                                                .background(GoldAmber.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "🔄 تكرار: " + when (task.recurrence) {
                                                    "Daily" -> "يومي"
                                                    "Weekly" -> "أسبوعي"
                                                    else -> task.recurrence
                                                },
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = GoldAmber
                                            )
                                        }
                                    }
                                }
                            }

                            IconButton(onClick = { viewModel.deleteTask(task) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        // Add Task Dialog Form
        if (showAddTaskDialog) {
            AlertDialog(
                onDismissRequest = { showAddTaskDialog = false },
                title = { Text(Localization.get("add_task"), fontWeight = FontWeight.Bold, color = ForestGreen) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        OutlinedTextField(
                            value = taskTitle,
                            onValueChange = { taskTitle = it },
                            label = { Text("الموضوع / عنوان المهمة") },
                            modifier = Modifier.fillMaxWidth().testTag("task_title_input"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = taskDesc,
                            onValueChange = { taskDesc = it },
                            label = { Text("التفاصيل والوصف") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Clean Choice Chips for assigning task instead of text input
                        Text("إسناد المهمة لعضو العائلة 👇:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            memberNames.forEach { name ->
                                val isSelected = taskAssignee == name
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { taskAssignee = name },
                                    label = { Text(name, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ForestGreen.copy(alpha = 0.15f),
                                        selectedLabelColor = ForestGreen
                                    )
                                )
                            }
                        }

                        OutlinedTextField(
                            value = taskDueDate,
                            onValueChange = { taskDueDate = it },
                            label = { Text("تاريخ الإنجاز YYYY-MM-DD") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Recurrence option selector with Arabic display tags
                        Text("تكرار تنفيذ المهمة تدويرياً:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val recurrenceOptions = listOf(
                                "None" to "لا يتكرر 📌",
                                "Daily" to "يومي 🔁",
                                "Weekly" to "أسبوعي 🔄"
                            )
                            recurrenceOptions.forEach { (key, label) ->
                                val isSelected = taskRecur == key
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { taskRecur = key },
                                    label = { Text(label, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GoldAmber.copy(alpha = 0.15f),
                                        selectedLabelColor = Color(0xFF7D5F00)
                                    )
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        onClick = {
                            if (taskTitle.isNotBlank()) {
                                viewModel.addTask(taskTitle, taskDesc, taskAssignee, taskDueDate, taskRecur)
                                showAddTaskDialog = false
                                // reset
                                taskTitle = ""
                                taskDesc = ""
                            }
                        }
                    ) {
                        Text("حفظ المهمة", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddTaskDialog = false }) {
                        Text(Localization.get("cancel"), color = ForestGreen)
                    }
                }
            )
        }
    }
}

// --- SUB-SCREEN F: FAMILY CHAT & SHARED NOTEBOOKS ---

@Composable
fun ChatScreen(viewModel: MainViewModel) {
    var subTabState by remember { mutableStateOf(0) } // 0 = Chat Messages, 1 = Family Notes

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = subTabState,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = ForestGreen,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[subTabState]),
                    color = ForestGreen
                )
            }
        ) {
            Tab(selected = subTabState == 0, onClick = { subTabState = 0 }) {
                Text(
                    Localization.get("chat_tab"),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(14.dp)
                )
            }
            Tab(selected = subTabState == 1, onClick = { subTabState = 1 }) {
                Text(
                    Localization.get("notes"),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(14.dp)
        ) {
            if (subTabState == 0) {
                FamilyChatView(viewModel)
            } else {
                FamilyNotebookView(viewModel)
            }
        }
    }
}

@Composable
fun FamilyChatView(viewModel: MainViewModel) {
    val messages by viewModel.chatMessages.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Automatically scroll to bottom when new messages arrive or when view loads
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(messages) { msg ->
                val isMe = msg.senderName.trim().equals(viewModel.currentUserName.trim(), ignoreCase = true)
                val isSystem = msg.senderRole == "System" || msg.senderName == "دارنا الذكي"

                if (isSystem) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = ForestGreen.copy(alpha = 0.08f)
                            ),
                            border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.15f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = ForestGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = msg.text,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp,
                                    color = ForestGreen
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        if (!isMe) {
                            Surface(
                                shape = CircleShape,
                                color = GoldAmber.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, GoldAmber),
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = msg.senderName.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = ForestGreen
                                    )
                                }
                            }
                        }

                        Card(
                            modifier = Modifier.widthIn(max = 260.dp),
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isMe) 16.dp else 4.dp,
                                bottomEnd = if (isMe) 4.dp else 16.dp
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isMe) ForestGreen else SoftGray
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                if (!isMe) {
                                    Text(
                                        text = msg.senderName,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ForestGreen
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                }

                                Text(
                                    text = msg.text,
                                    color = if (isMe) Color.White else Color.Black,
                                    fontSize = 13.5.sp,
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                val timeFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                                val formattedTime = timeFormat.format(java.util.Date(msg.timestamp))
                                Text(
                                    text = formattedTime,
                                    fontSize = 8.5.sp,
                                    color = if (isMe) Color.White.copy(alpha = 0.7f) else Color.Gray,
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .imePadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text(Localization.get("chat_placeholder")) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input"),
                singleLine = true,
                shape = RoundedCornerShape(24.dp)
            )

            FloatingActionButton(
                onClick = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendChatMessage(messageText)
                        messageText = ""
                    }
                },
                containerColor = ForestGreen,
                shape = CircleShape,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}

@Composable
fun FamilyNotebookView(viewModel: MainViewModel) {
    val notes by viewModel.sharedNotes.collectAsState()
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = { showAddNoteDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.NoteAdd, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(Localization.get("add_note"), color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (notes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("المفكرة العائلية فارغة.", color = Color.Gray)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(notes) { note ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = GoldAmber.copy(alpha = 0.08f)),
                        border = BorderStroke(1.dp, GoldAmber.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(note.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DarkBackground)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(note.content, fontSize = 12.sp, maxLines = 4, overflow = TextOverflow.Ellipsis)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "بقلم: ${note.createdBy}",
                                    fontSize = 8.sp,
                                    color = Color.Gray
                                )
                                IconButton(onClick = { viewModel.deleteSharedNote(note) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Note Dialog
        if (showAddNoteDialog) {
            AlertDialog(
                onDismissRequest = { showAddNoteDialog = false },
                title = { Text(Localization.get("add_note"), fontWeight = FontWeight.Bold, color = ForestGreen) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = noteTitle,
                            onValueChange = { noteTitle = it },
                            label = { Text(Localization.get("note_title")) },
                            modifier = Modifier.fillMaxWidth().testTag("note_title_input"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = noteContent,
                            onValueChange = { noteContent = it },
                            label = { Text(Localization.get("note_body")) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        onClick = {
                            if (noteTitle.isNotBlank()) {
                                viewModel.addSharedNote(noteTitle, noteContent)
                                showAddNoteDialog = false
                                noteTitle = ""
                                noteContent = ""
                            }
                        }
                    ) {
                        Text("حفظ الملاحظة", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddNoteDialog = false }) {
                        Text(Localization.get("cancel"), color = ForestGreen)
                    }
                }
            )
        }
    }
}

// --- SUB-SCREEN G: FINANCES & GRAPH REPORTS ---

@Composable
fun ExpensesScreen(viewModel: MainViewModel) {
    val expenses by viewModel.expenseList.collectAsState()
    
    var showExpenseDialog by remember { mutableStateOf(false) }
    var sumAmount by remember { mutableStateOf("") }
    var expCategory by remember { mutableStateOf("cat_food") }
    var expDesc by remember { mutableStateOf("") }

    val totalSpent = expenses.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = Localization.get("expenses_tab"),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = ForestGreen
        )

        Button(
            onClick = { showExpenseDialog = true },
            modifier = Modifier.fillMaxWidth().testTag("add_expense_trigger"),
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.AddCard, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(Localization.get("add_expense"), color = Color.White, fontWeight = FontWeight.Bold)
        }

        // BUDGET PROGRESS COMPONENT
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = Localization.get("remaining_budget"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "المتبقي: ${viewModel.budgetLimit - totalSpent} ${viewModel.getCurrencySymbol()}",
                        fontWeight = FontWeight.Bold,
                        color = if (viewModel.budgetLimit - totalSpent >= 0) ForestGreen else ErrorRed
                    )
                    Text("الحد: ${viewModel.budgetLimit} ${viewModel.getCurrencySymbol()}")
                }
            }
        }

        // CUSTOM CANVAS BAR CHART (SPENDING BREAKDOWN BY CATEGORY)
        Text(
            text = Localization.get("spending_trends"),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )

        // Calculate Category distribution
        val foodSum = expenses.filter { it.category == "cat_food" }.sumOf { it.amount }
        val cleaningSum = expenses.filter { it.category == "cat_cleaning" }.sumOf { it.amount }
        val personalSum = expenses.filter { it.category == "cat_personal" }.sumOf { it.amount }
        val otherSum = expenses.filter { it.category == "cat_other" }.sumOf { it.amount }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Drawing categories bar lines
                val maxVal = maxOf(foodSum, cleaningSum, personalSum, otherSum, 10.0).toFloat()
                val values = listOf(foodSum, cleaningSum, personalSum, otherSum)
                val colors = listOf(ForestGreen, SageGreen, GoldAmber, ErrorRed)
                
                val barWidth = 40.dp.toPx()
                val gap = (canvasWidth - (barWidth * 4)) / 5

                for (i in 0..3) {
                    val barHeight = (values[i].toFloat() / maxVal) * (canvasHeight - 30.dp.toPx())
                    val x = gap + i * (barWidth + gap)
                    val y = canvasHeight - barHeight

                    drawRect(
                        color = colors[i],
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight)
                    )
                }

                // Baseline
                drawLine(
                    color = Color.Gray,
                    start = Offset(0f, canvasHeight),
                    end = Offset(canvasWidth, canvasHeight),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }

        // Category Legend Indicator Markers
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            val categorMapping = listOf(
                Triple(ForestGreen, "الغذاء والمشتريات الغذائية", foodSum),
                Triple(SageGreen, "منظفات البيت ومستلزمات الصرف", cleaningSum),
                Triple(GoldAmber, "أغراض شخصية وصحية عائلية", personalSum),
                Triple(ErrorRed, "أخرى ومصطلحات متفرقة", otherSum)
            )

            categorMapping.forEach { (color, label, sum) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("$label: $sum ${viewModel.getCurrencySymbol()}", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }

        // SECTION C: BUDGET ADVISOR WITH GEMINI AI
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = GoldAmber.copy(alpha = 0.08f)),
            border = BorderStroke(1.5.dp, GoldAmber)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ForestGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Localization.get("ai_budget_tips"),
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen,
                        fontSize = 15.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = { viewModel.requestAIBudgetInsights() },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    modifier = Modifier.fillMaxWidth().testTag("ai_budget_insights_btn")
                ) {
                    if (viewModel.isGeneratingBudget) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Text("طلب استشارة وتحليل الميزانية بالذكاء", color = Color.White)
                    }
                }

                if (viewModel.aiBudgetResult.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = viewModel.aiBudgetResult,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                            .fillMaxWidth()
                    )
                }
            }
        }

        // EXPENSE TRANSACTIONS LOG RECORD LIST
        Text(
            text = "شريط العمليات النقدية المسجلة",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )

        expenses.forEach { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AttachMoney, contentDescription = null, tint = ForestGreen)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.description, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("تاريخ: ${item.date}", fontSize = 10.sp, color = Color.Gray)
                    }
                    Text("-${item.amount} ${viewModel.getCurrencySymbol()}", fontWeight = FontWeight.Bold, color = ErrorRed)
                    IconButton(onClick = { viewModel.deleteExpense(item) }) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Add Expense Dialog
        if (showExpenseDialog) {
            AlertDialog(
                onDismissRequest = { showExpenseDialog = false },
                title = { Text(Localization.get("add_expense"), fontWeight = FontWeight.Bold, color = ForestGreen) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = sumAmount,
                            onValueChange = { sumAmount = it },
                            label = { Text("مبلغ الصرفية (${viewModel.getCurrencySymbol()})") },
                            modifier = Modifier.fillMaxWidth().testTag("expense_amount_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = expDesc,
                            onValueChange = { expDesc = it },
                            label = { Text("الوصف / بند التوضيح") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        // Category choices
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("cat_food", "cat_cleaning", "cat_personal", "cat_other").forEach { cat ->
                                FilterChip(
                                    selected = expCategory == cat,
                                    onClick = { expCategory = cat },
                                    label = { Text(cat.substringAfter("_")) }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        onClick = {
                            val amt = sumAmount.toDoubleOrNull()
                            if (amt != null && expDesc.isNotBlank()) {
                                viewModel.addExpense(amt, expCategory, expDesc, "2026-06-02")
                                showExpenseDialog = false
                                sumAmount = ""
                                expDesc = ""
                            }
                        }
                    ) {
                        Text("قيد المصروف", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExpenseDialog = false }) {
                        Text(Localization.get("cancel"), color = ForestGreen)
                    }
                }
            )
        }
    }
}

// --- SUB-SCREEN H: SYSTEM ALARMS / NOTIFICATIONS FEED ---

@Composable
fun NotificationsScreen(viewModel: MainViewModel) {
    val items by viewModel.notifications.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = Localization.get("notifications_tab"),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = ForestGreen
            )

            TextButton(onClick = { viewModel.clearAllNotifications() }) {
                Text(Localization.get("clear_all"), color = ErrorRed, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لا توجد إشعارات أو تنبيهات نشطة حالياً.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items) { alarm ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = if (alarm.type == "expiry_warning") Icons.Default.Timer else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (alarm.type == "expiry_warning") ErrorRed else GoldAmber,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(alarm.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(alarm.message, fontSize = 12.sp, color = Color.DarkGray)
                            }
                            IconButton(onClick = { viewModel.removeNotification(alarm) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SUB-SCREEN I: PROFILE CARD ---

@Composable
fun ProfileScreen(viewModel: MainViewModel) {
    val userList by viewModel.userList.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = Localization.get("profile_tab"),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = ForestGreen,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            shape = CircleShape,
            color = ForestGreen,
            modifier = Modifier.size(120.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = viewModel.currentUserName.take(1),
                    color = Color.White,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Text(
            text = viewModel.currentUserName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "عضو موقر في المجلس العائلي: " + viewModel.currentFamilyName,
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = Localization.get("email_label"), fontWeight = FontWeight.Bold)
                    Text(text = viewModel.currentUserEmail)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = Localization.get("phone_label"), fontWeight = FontWeight.Bold)
                    Text(text = viewModel.currentUserPhone)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "رتبة العضوية", fontWeight = FontWeight.Bold)
                    Text(text = viewModel.currentUserRole)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "رمز دعوة العائلة الحالي", fontWeight = FontWeight.Bold, color = ForestGreen)
                    Text(text = viewModel.currentFamilyId, fontWeight = FontWeight.Bold, color = GoldAmber)
                }
            }
        }

        // Active Family Members list
        Text(
            text = "أعضاء عائلتك المتصلين بالمخزن المشترك 🇩🇿",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start)
        )

        var showRoleDialogForUser by remember { mutableStateOf<User?>(null) }

        if (showRoleDialogForUser != null) {
            val targetUser = showRoleDialogForUser!!
            var selectedRole by remember(targetUser) { mutableStateOf(targetUser.role) }
            var selectedRelation by remember(targetUser) { mutableStateOf(targetUser.relation) }

            AlertDialog(
                onDismissRequest = { showRoleDialogForUser = null },
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SupervisedUserCircle, contentDescription = null, tint = ForestGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تعديل هوية العضو: ${targetUser.name}", fontWeight = FontWeight.Bold, color = ForestGreen, fontSize = 16.sp)
                    }
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        // SECTION 1: ADMIN ROLE
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                "الصلاحية الإدارية في التطبيق:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = ForestGreen
                            )
                            val roles = listOf(
                                "Admin" to "مدير العائلة 👑 (تحكم كامل)",
                                "Member" to "عضو العائلة 🏠 (مشاركة كاملة)",
                                "Guest" to "زائر 👥 (عرض ومعاينة فقط)"
                            )
                            roles.forEach { (roleKey, roleLabel) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedRole = roleKey }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedRole == roleKey,
                                        onClick = { selectedRole = roleKey }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(roleLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // SECTION 2: RELATIONSHIP / MEMBER TYPE
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "صلة القرابة / نوع العضو بالمنزل:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = ForestGreen
                            )
                            Text(
                                "تحديد نوع العضو يساعد في تنظيم المهام العائلية وتحضير الوجبات اليومية بذكاء.",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                            
                            val row1 = listOf("زوج" to "زوج 👨", "زوجة" to "زوجة 👩", "ابن" to "ابن 👦")
                            val row2 = listOf("بنت" to "بنت 👧", "أم" to "أم 👩‍🦱", "أب" to "أب 👨‍💼")
                            val row3 = listOf("أخ" to "أخ 👱‍♂️", "أخت" to "أخت 👩‍🦰", "جد" to "جد 👴")
                            val row4 = listOf("جدة" to "جدة 👵", "غير محدد" to "غير محدد 👥")

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(row1, row2, row3, row4).forEach { rowList ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        rowList.forEach { (relKey, relLabel) ->
                                            val isSelected = selectedRelation == relKey
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .background(if (isSelected) GoldAmber.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                                    .border(1.dp, if (isSelected) GoldAmber else Color.Transparent, RoundedCornerShape(8.dp))
                                                    .clickable { selectedRelation = relKey }
                                                    .padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(relLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color(0xFF7D5F00) else Color.DarkGray)
                                            }
                                        }
                                        // Fill extra spot if row is shorter than standard 3 items
                                        if (rowList.size < 3) {
                                            Spacer(modifier = Modifier.weight(3f - rowList.size))
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updateUserRole(targetUser, selectedRole)
                            viewModel.updateUserRelation(targetUser, selectedRelation)
                            showRoleDialogForUser = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("حفظ التغييرات", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRoleDialogForUser = null }) {
                        Text("إلغاء", color = Color.Gray, fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        }

        userList.filter { it.familyId == viewModel.currentFamilyId }.forEach { relative ->
            val relationEmoji = when (relative.relation) {
                "زوج" -> "👨"
                "زوجة" -> "👩"
                "ابن" -> "👦"
                "بنت" -> "👧"
                "أم" -> "👩‍🦱"
                "أب" -> "👨‍💼"
                "أخ" -> "👱‍♂️"
                "أخت" -> "👩‍🦰"
                "جد" -> "👴"
                "جدة" -> "👵"
                else -> "👥"
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showRoleDialogForUser = relative },
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.12f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = ForestGreen.copy(alpha = 0.08f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(relationEmoji, fontSize = 22.sp)
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(relative.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                if (relative.email.trim().equals(viewModel.currentUserEmail.trim(), ignoreCase = true)) {
                                    Box(
                                        modifier = Modifier
                                            .background(ForestGreen.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("أنت", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                                    }
                                }
                            }
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Relation/Type tag inside the home
                                Box(
                                    modifier = Modifier
                                        .background(GoldAmber.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${relationEmoji} ${relative.relation}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF7D5F00)
                                    )
                                }
                                
                                // Administrative role tag
                                Box(
                                    modifier = Modifier
                                        .background(ForestGreen.copy(alpha = 0.06f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = when(relative.role) {
                                            "Admin" -> "مدير العائلة 👑"
                                            "Member" -> "عضو العائلة 🏠"
                                            else -> "زائر 👥"
                                        },
                                        fontSize = 10.sp,
                                        color = ForestGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                    Icon(Icons.Default.Edit, contentDescription = "تعديل الهوية", tint = SageGreen, modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Join Existing Home / Family Card ---
        var joinInviteCode by remember { mutableStateOf("") }
        var profileJoinFailed by remember { mutableStateOf(false) }
        var profileJoinSuccess by remember { mutableStateOf(false) }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.GroupAdd, contentDescription = null, tint = ForestGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "الانضمام إلى منزل / عائلة أخرى",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = ForestGreen
                    )
                }

                Text(
                    text = "أدخل رمز دعوة المنزل أو العائلة التي ترغب في مشاركتها إدارة الأعباء ومخزن الطعام، وسيتم نقلك ومزامنتك معهم فوراً.",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                OutlinedTextField(
                    value = joinInviteCode,
                    onValueChange = { 
                        joinInviteCode = it
                        profileJoinFailed = false
                        profileJoinSuccess = false
                    },
                    label = { Text("رمز دعوة المنزل الجديد") },
                    placeholder = { Text("مثال: FAMILY123") },
                    modifier = Modifier.fillMaxWidth().testTag("profile_join_code_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                if (profileJoinFailed) {
                    Text(
                        text = "عذراً، هذا الرمز غير صحيح أو غير مسجل في النظام الحالي.",
                        color = ErrorRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (profileJoinSuccess) {
                    Text(
                        text = "تم الانتقال والانضمام للمنزل الجديد بنجاح! يتم الآن ربط البيانات...",
                        color = ForestGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        if (joinInviteCode.isNotBlank()) {
                            viewModel.joinFamily(joinInviteCode) { success ->
                                if (success) {
                                    profileJoinSuccess = true
                                    joinInviteCode = ""
                                } else {
                                    profileJoinFailed = true
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Login, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("انضمام عاجل للمنزل", color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.logout() },
            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(Localization.get("logout"), color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

// --- SUB-SCREEN J: SETTINGS SWITCHERS ---

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = Localization.get("settings_tab"),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = ForestGreen
        )

        Spacer(modifier = Modifier.height(6.dp))

        // --- Firebase Cloud Connection Card ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Cloud, contentDescription = null, tint = ForestGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ربط قاعدة بيانات Firebase المباشرة",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = ForestGreen
                    )
                }

                Text(
                    text = "مستودع البيانات السحابي النشط:\nاتصال آمن ومشفر بالكامل 🔒",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreen,
                    modifier = Modifier
                        .background(SoftGray, RoundedCornerShape(6.dp))
                        .padding(8.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("حالة المزامنة السحابية:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    
                    val (statusLabel, statusColor) = when (viewModel.syncStatus) {
                        "idle" -> "مستقر محلياً" to Color.Gray
                        "syncing" -> "جاري الرفع والدمج..." to ForestGreen
                        "done" -> "تمت المزامنة بنجاح! ✔️" to SageGreen
                        "failed" -> "فشل المزامنة ❌" to ErrorRed
                        else -> "غير معروف" to Color.Gray
                    }
                    Text(
                        text = statusLabel,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        fontSize = 13.sp
                    )
                }

                if (viewModel.lastSyncTimestamp > 0L) {
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                    val dateString = sdf.format(java.util.Date(viewModel.lastSyncTimestamp))
                    Text("آخر مزامنة ناجحة: $dateString", fontSize = 11.sp, color = Color.Gray)
                }

                Button(
                    onClick = { viewModel.syncWithCloud() },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("دمج ومزامنة البيانات السحابية", color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- Country Settings Card (Algeria as Default) ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Public, contentDescription = null, tint = ForestGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "إعدادات بلد الإقامة والخدمات الإقليمية",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = ForestGreen
                    )
                }

                Text(
                    text = "قم بتعديل إعدادات بلد الإقامة لتفعيل العملة المحلية للمصروفات، وشبكات الجوال وتلقي إشعارات الطوارئ المناسبة.",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                // Select Country
                Text("اختر البلد / Select Country:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                val countries = listOf("الجزائر", "السعودية", "مصر", "الإمارات")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    countries.forEach { country ->
                        val isSelected = viewModel.currentCountry == country
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ForestGreen else SoftGray)
                                .clickable { 
                                    viewModel.currentCountry = country 
                                    if (country == "الجزائر") {
                                        viewModel.currentCity = "الجزائر العاصمة"
                                    } else if (country == "السعودية") {
                                        viewModel.currentCity = "الرياض"
                                    } else if (country == "مصر") {
                                        viewModel.currentCity = "القاهرة"
                                    } else {
                                        viewModel.currentCity = "دبي"
                                    }
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = country,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color.Black
                            )
                        }
                    }
                }

                // If Algeria is selected, show local services and currency info directly
                if (viewModel.currentCountry == "الجزائر") {
                    Spacer(modifier = Modifier.height(4.dp))

                    // Emergency Contacts of Algeria desk box
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = ForestGreen.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "🇩🇿 بوابة الحماية والخدمات للجزائر:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen
                            )
                            Text("• العملة المفعلة: الدينار الجزائري (${viewModel.getCurrencySymbol()})", fontSize = 11.sp)
                            Text("• الحماية المدنية الجزائرية: الاتصال بالرقم المجاني 14", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text("• الشرطة الجزائرية الوطنية: الاتصال بالرقم المجاني 1548", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text("• المساعدة الطبية الاستعجالية SAMU: الاتصال على الرقم 115", fontSize = 11.sp)
                            Text("• طوارئ شركة سونلغاز (الكهرباء والغاز): اتصل بالرقم الوطني 3303", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Arabic English Switching
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = Localization.get("language_toggle"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = ForestGreen
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "اختر لغة واجهة التطبيق المعتمدة / Choose your localization theme",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { Localization.isArabic = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (Localization.isArabic) ForestGreen else SoftGray
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("العربية", color = if (Localization.isArabic) Color.White else Color.Black)
                    }

                    Button(
                        onClick = { Localization.isArabic = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!Localization.isArabic) ForestGreen else SoftGray
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("English", color = if (!Localization.isArabic) Color.White else Color.Black)
                    }
                }
            }
        }

        // Dark/Light Theme Switching
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = Localization.get("dark_mode"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = ForestGreen
                    )
                    Text("تبديل مظهر تطبيق دارنا تلقائياً", fontSize = 11.sp, color = Color.Gray)
                }

                Switch(
                    checked = viewModel.isDarkMode,
                    onCheckedChange = { viewModel.isDarkMode = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = ForestGreen)
                )
            }
        }

        // Admin Only Section (Invite Code lookup, modify monthly wallet goals)
        if (viewModel.currentUserRole == "Admin") {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("إعدادات مدير العائلة (المالك فقط)", fontWeight = FontWeight.Bold, color = ErrorRed)
                    Text("كود دعوة عائلتك المفتوح: ${viewModel.currentFamilyId}", fontWeight = FontWeight.SemiBold)
                    
                    Spacer(modifier = Modifier.height(6.dp))

                    Text("تثبيت أو تغيير حد المصروفات الشهري الموحد للأسرة:")
                    Slider(
                        value = viewModel.budgetLimit.toFloat(),
                        onValueChange = { viewModel.budgetLimit = it.toDouble() },
                        valueRange = 500f..10000f,
                        steps = 19,
                        colors = SliderDefaults.colors(thumbColor = ForestGreen, activeTrackColor = ForestGreen)
                    )
                    Text("الحد المالي المستهدف: ${viewModel.budgetLimit.toInt()} ${viewModel.getCurrencySymbol()}", fontWeight = FontWeight.Bold, color = ForestGreen)
                }
            }
        }

        // Direct shortcut link to About Darna
        OutlinedButton(
            onClick = { viewModel.currentTab = Tab.ABOUT },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.HelpCenter, contentDescription = null, tint = ForestGreen)
            Spacer(modifier = Modifier.width(8.dp))
            Text("المزيد حول دارنا / About Darna Help", color = ForestGreen)
        }
    }
}

// --- SUB-SCREEN K: ABOUT SYSTEM PANEL ---

@Composable
fun AboutScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = Localization.get("about_tab"),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = ForestGreen
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Splash info
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("تطبيق دارنا الذكي", fontWeight = FontWeight.Bold, color = ForestGreen, fontSize = 18.sp)
                Text("إصدار المنتج: v1.0.4 (بنية اختبارية سحابية)", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "دارنا هي منصة عائلية تعاونية رائدة تهدف إلى تنظيم الأعباء المنزلية، وجرد مستودعات الغذاء بصورة تفاعلية ممتازة للوقاية من تلف الطعام وترشيد الميزانيات النقدية بالذكاء الاصطناعي.",
                    fontSize = 13.sp
                )
            }
        }

        Text("قنوات الدعم الفني والخصوصية", fontWeight = FontWeight.Bold)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("الدعم عبر البريد: contact@darna-smart.com", fontSize = 12.sp)
                Text("سياسة حماية بيانات البيت: جميع البيانات مأمنة ومشفرة محلياً 🔒", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "صنع بكل حب لعائلتك العربية الكريمة ❤️",
            fontSize = 11.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
