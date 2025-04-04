package com.example.musicplayer

import android.Manifest
import android.annotation.SuppressLint
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.musicplayer.notification.PlayerService
import com.example.musicplayer.presentation.NavRoot
import com.example.musicplayer.presentation.Route
import com.example.musicplayer.presentation.components.PermissionDialog
import com.example.musicplayer.ui.theme.MusicPlayerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.take

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private var originRequestOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

    private var navController: NavHostController? = null

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MusicPlayerTheme {
                val viewModel: MainViewModel = hiltViewModel()
                navController = rememberNavController()

                LaunchedEffect(Unit) {
                    if(intent.action == PlayerService.ACTION_OPEN_PLAYER)
                    navController!!.run {
                        //Fire only when graph was established
                        currentBackStack
                            .dropWhile { it.isEmpty() }
                            .take(1)
                            .collect {
                                navigate(Route.PlayerScreen)
                                //Don't forget to reset Activity's intent action, cuz on every
                                //configuration change everything launches again, including all side-effects.
                                //That's why it's recommended to keep compose side-effects as low as possible
                                intent.action = null
                            }
                    }
                }

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

                LaunchedEffect(Unit) {
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
                    navController = navController!!
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if(intent.action == PlayerService.ACTION_OPEN_PLAYER) {
            //Navigate to PlayerScreen
            navController?.navigate(Route.PlayerScreen) {
                launchSingleTop = true
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
    return shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
}

fun Context.hasReadPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.READ_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED
}