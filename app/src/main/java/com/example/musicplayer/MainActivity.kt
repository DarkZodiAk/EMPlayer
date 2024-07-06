package com.example.musicplayer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.musicplayer.presentation.NavRoot
import com.example.musicplayer.presentation.components.PermissionDialog
import com.example.musicplayer.ui.theme.MusicPlayerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private var originRequestOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MusicPlayerTheme {
                val viewModel: MainViewModel = hiltViewModel()
                val navController = rememberNavController()

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    restoreOrientation()
                    viewModel.onAction(MainAction.SubmitReadPermissionInfo(
                        isGranted = isGranted,
                        shouldShowRationale = shouldShowReadPermissionRationale()
                    ))
                }

                val settingsLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) {
                    viewModel.onAction(MainAction.SubmitReadPermissionInfo(
                        isGranted = hasReadPermission(),
                        shouldShowRationale = shouldShowReadPermissionRationale()
                    ))
                }

                LaunchedEffect(key1 = true) {
                    lockOrientation()
                    permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    viewModel.event.collect { event ->
                        when(event) {
                            MainEvent.RequestReadPermission -> {
                                lockOrientation()
                                permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            }
                        }
                    }
                }

                if(viewModel.state.showReadSettingsDialog) {
                    PermissionDialog(
                        title = "Permission permanently declined",
                        text = "You need to grant read permission in Settings to make player work",
                        buttonText = "OK",
                        onClick = {
                            settingsLauncher.launch(
                                Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", packageName, null)
                                )
                            )
                        }
                    )

                }

                NavRoot(
                    isLoaded = viewModel.state.isLoaded,
                    navController = navController
                )
            }
        }
    }

    private fun lockOrientation() {
        originRequestOrientation = requestedOrientation
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
    }

    private fun restoreOrientation() {
        requestedOrientation = originRequestOrientation
    }
}

fun ComponentActivity.shouldShowReadPermissionRationale(): Boolean {
    return shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
}

fun Context.hasReadPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED
}