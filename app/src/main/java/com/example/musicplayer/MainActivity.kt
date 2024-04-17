package com.example.musicplayer

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.musicplayer.presentation.NavRoot
import com.example.musicplayer.presentation.components.PermissionDialog
import com.example.musicplayer.ui.theme.MusicPlayerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MusicPlayerTheme {
                val mainViewModel: MainViewModel = hiltViewModel()
                val navController = rememberNavController()

                val storagePermissionResultLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = {
                        mainViewModel.checkReadPermission()
                    }
                )
                
                LaunchedEffect(true) {
                    mainViewModel.uiPermissionChannel.collect { event ->
                        when(event) {
                            is UiMainEvent.AskForPermission -> {
                                storagePermissionResultLauncher.launch(event.permission)
                            }
                        }
                    }
                }

                if(!shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
                    && !mainViewModel.hasReadPermission) {
                    PermissionDialog(
                        onClick = {
                            openAppSettings()
                        }
                    )
                }

                NavRoot(
                    isLoaded = mainViewModel.isLoaded,
                    navController = navController
                )
            }
        }
    }
}

fun Activity.openAppSettings() {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).also(::startActivity)
}