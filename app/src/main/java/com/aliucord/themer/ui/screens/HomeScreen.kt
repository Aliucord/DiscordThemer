package com.aliucord.themer.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.aliucord.themer.*
import com.aliucord.themer.R
import com.aliucord.themer.ui.components.ThemerAppBar
import com.aliucord.themer.utils.ThemeManager
import com.aliucord.themer.utils.Utils
import com.google.accompanist.permissions.*

var hsThemes: MutableState<ArrayList<ThemeManager.Theme>>? = null

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(navController: NavController) {
    val storagePermissionState = rememberPermissionState(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    var isMenuExpanded by remember { mutableStateOf(false) }

    SideEffect {
        if (!storagePermissionState.hasPermission) storagePermissionState.launchPermissionRequest()
    }

    Scaffold(
        topBar = {
            ThemerAppBar(
                navController = navController,
                title = R.string.app_name,
                actions = {
                    val context = LocalContext.current
                    IconButton(onClick = {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://discord.gg/${BuildConfig.SUPPORT_SERVER}")
                            )
                        )
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_discord),
                            contentDescription = null,
                            tint = MaterialTheme.colors.onPrimary
                        )
                    }
                    if (storagePermissionState.hasPermission) {
                        IconButton(onClick = { isMenuExpanded = !isMenuExpanded }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.show_menu),
                                tint = MaterialTheme.colors.onPrimary
                            )
                        }
                        DropdownMenu(expanded = isMenuExpanded, onDismissRequest = { isMenuExpanded = false }) {
                            val launcher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.StartActivityForResult(),
                                onResult = { res -> Utils.handleImportRes(context, ".json", res) }
                            )
                            DropdownMenuItem(onClick = { launcher.launch(Utils.getImportIntent("json")) }) {
                                Text(stringResource(R.string.import_theme))
                            }
                        }
                    }
                }
            )
        }
    ) {
        PermissionRequired(
            permissionState = storagePermissionState,
            permissionNotGrantedContent = { GrantPermission(permissionState = storagePermissionState) },
            permissionNotAvailableContent = { GrantPermission(permissionState = storagePermissionState) }
        ) {
            ThemeManager.init()
            hsThemes = remember { mutableStateOf(ThemeManager.themes) }

            var selected by remember { mutableStateOf(hsThemes!!.value.find { it.enabled }?.name) }
            val selectTheme = { theme: ThemeManager.Theme? ->
                selected = if (theme == null) {
                    for (t in hsThemes!!.value) t.disable()
                    null
                } else {
                    theme.enable()
                    theme.name
                }
            }

            Column(Modifier.padding(horizontal = 12.dp, vertical = 5.dp)) {
                Text(
                    stringResource(if (xposedEnabled) R.string.tested_on else R.string.xposed_not_detected),
                    textAlign = TextAlign.Center,
                )
                Divider(Modifier.padding(vertical = 8.dp))
                Text(
                    "Selected theme",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colors.primary
                )
                LazyColumn {
                    items(count = hsThemes!!.value.size + 1, itemContent = {
                        val theme = if (it == 0) null else hsThemes!!.value[it - 1]
                        val s = selected == theme?.name
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = s,
                                    onClick = { selectTheme(theme) }
                                )
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                if (theme == null) "None" else "${theme.name} v${theme.version} by ${theme.author}",
                                style = MaterialTheme.typography.subtitle1
                            )
                            Row {
                                if (theme != null) IconButton(
                                    onClick = { navController.navigate("editor/${it - 1}") },
                                    modifier = Modifier
                                        .wrapContentSize(Alignment.Center)
                                        .padding(start = 0.dp, top = 2.dp, end = 8.dp, bottom = 2.dp)
                                        .requiredSize(20.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
                                }
                                RadioButton(selected = s, onClick = null)
                            }
                        }
                    })
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GrantPermission(permissionState: PermissionState) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.permission_required), style = MaterialTheme.typography.h6, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Row {
            val context = LocalContext.current

            Button(onClick = { permissionState.launchPermissionRequest() }) {
                Text(stringResource(R.string.permission_grant))
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray, contentColor = Color.Black)
            ) {
                Text(stringResource(R.string.open_settings))
            }
        }
    }
}
