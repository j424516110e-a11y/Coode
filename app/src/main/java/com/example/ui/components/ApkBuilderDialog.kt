package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProjectEntity
import com.example.engine.BuildStepProgress
import com.example.ui.theme.IdeAccentApk
import com.example.ui.theme.IdeAccentRun
import com.example.ui.theme.IdePrimarySky
import java.io.File

@Composable
fun ApkBuilderDialog(
    project: ProjectEntity,
    isBuilding: Boolean,
    buildProgress: BuildStepProgress?,
    onStartBuild: (
        appName: String,
        packageName: String,
        versionName: String,
        appIconPreset: String,
        appIconUri: String?
    ) -> Unit,
    onInstallApk: (File) -> Unit,
    onDismiss: () -> Unit
) {
    var appName by remember(project) { mutableStateOf(project.apkAppName) }
    var packageName by remember(project) { mutableStateOf(project.apkPackageName) }
    var versionName by remember(project) { mutableStateOf(project.apkVersionName) }

    var selectedIconPreset by remember(project) { mutableStateOf(project.customIconRes) }
    var selectedIconGradientIndex by remember { mutableIntStateOf(0) }
    var customIconUriString by remember { mutableStateOf<String?>(null) }

    // System Image Picker launcher for custom app icon photo upload
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            customIconUriString = it.toString()
            selectedIconPreset = "custom_image"
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isBuilding) onDismiss() },
        confirmButton = {},
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp)
            ) {
                // Header Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Android,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "محرك تصدير APK حقيقي 100%",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "تصدير تطبيق أندرويد أصلي بدون حدود",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        enabled = !isBuilding,
                        modifier = Modifier.testTag("close_apk_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!isBuilding && buildProgress?.isFinished != true) {
                    // Config Inputs
                    Text(
                        text = "1. إعدادات حزمة التطبيق (App Configuration)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = IdePrimarySky
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = appName,
                        onValueChange = { appName = it },
                        label = { Text("اسم التطبيق الأصلي") },
                        leadingIcon = { Icon(Icons.Default.Label, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("apk_name_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = packageName,
                        onValueChange = { packageName = it },
                        label = { Text("معرف الحزمة (Package ID)") },
                        leadingIcon = { Icon(Icons.Default.Code, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("apk_package_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = versionName,
                        onValueChange = { versionName = it },
                        label = { Text("رقم الإصدار (Version)") },
                        leadingIcon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("apk_version_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // App Icon & Logo Customizer Section
                    Text(
                        text = "2. صورة وأيقونة التطبيق (App Icon & Logo)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = IdePrimarySky
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (customIconUriString != null) "تم اختيار صورة مخصصة 🖼️" else "اختر أيقونة وشعار التطبيق",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "ستظهر هذه الصورة على شاشة الهاتف الرئيسية عند تثبيت التطبيق",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }

                                // Live App Icon Preview Squircle Badge
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(Color(0xFF2563EB), Color(0xFF38BDF8))
                                            )
                                        )
                                        .border(1.5.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (selectedIconPreset) {
                                            "rocket" -> Icons.Default.RocketLaunch
                                            "code" -> Icons.Default.Code
                                            "ai" -> Icons.Default.AutoAwesome
                                            "game" -> Icons.Default.SportsEsports
                                            "star" -> Icons.Default.Star
                                            "shopping" -> Icons.Default.ShoppingCart
                                            else -> Icons.Default.Android
                                        },
                                        contentDescription = "معاينة الشعار",
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Gallery Photo Upload Trigger Button
                            OutlinedButton(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("upload_app_icon_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (customIconUriString != null) "تغيير الصورة المحددة من الهاتف" else "تحميل صورة خاصة من معرض الصور 🖼️",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Preset Icon Quick Grid Selection
                            Text(
                                text = "أو اختر شارة جاهزة للتطبيق:",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                listOf(
                                    "android" to Icons.Default.Android,
                                    "rocket" to Icons.Default.RocketLaunch,
                                    "code" to Icons.Default.Code,
                                    "ai" to Icons.Default.AutoAwesome,
                                    "game" to Icons.Default.SportsEsports,
                                    "star" to Icons.Default.Star
                                ).forEach { (key, iconVec) ->
                                    val isSelected = selectedIconPreset == key && customIconUriString == null
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                                            )
                                            .border(
                                                1.dp,
                                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                selectedIconPreset = key
                                                customIconUriString = null
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = iconVec,
                                            contentDescription = key,
                                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Build Trigger Button
                    Button(
                        onClick = {
                            onStartBuild(
                                appName, packageName, versionName,
                                selectedIconPreset, customIconUriString
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF1D4ED8), Color(0xFF38BDF8))
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .testTag("start_apk_compile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.BuildCircle,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "بدء بناء وتصدير ملف APK الآن",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                    }
                } else {
                    // Building Progress Logs Pane
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isBuilding) {
                            CircularProgressIndicator(
                                color = IdePrimarySky,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = buildProgress?.stepName ?: "جاري تجميع حزمة التطبيق...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "الخطوة ${buildProgress?.stepIndex ?: 1} من ${buildProgress?.totalSteps ?: 6}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = IdeAccentRun,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "تم تصدير التطبيق بنجاح! 🚀",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = IdeAccentRun
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Terminal Log Output Box
                        Surface(
                            color = Color(0xFF090D12),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                        ) {
                            Box(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = buildProgress?.logMessage ?: "",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = Color(0xFF38BDF8)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (buildProgress?.isFinished == true && buildProgress.generatedApkFile != null) {
                            Button(
                                onClick = { onInstallApk(buildProgress.generatedApkFile) },
                                colors = ButtonDefaults.buttonColors(containerColor = IdeAccentRun),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("install_apk_now_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GetApp,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "تثبيت / تحميل ملف APK المباشر",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        dismissButton = null,
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}
