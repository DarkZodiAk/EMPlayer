package com.example.musicplayer

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
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

    private var orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

    private var navController: NavHostController? = null

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restoreOrientation()
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
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { _ ->
                    restoreOrientation()
                    viewModel.onAction(MainAction.SubmitPermissionsInfo(
                        hasRead = hasReadPermission(),
                        hasPost = hasPostPermission(),
                        shouldShowReadRationale = shouldShowReadPermissionRationale(),
                        shouldShowPostRationale = shouldShowPostPermissionRationale()
                    ))
                }

                val settingsLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) {
                    viewModel.onAction(MainAction.SubmitPermissionsInfo(
                        hasRead = hasReadPermission(),
                        hasPost = hasPostPermission(),
                        shouldShowReadRationale = shouldShowReadPermissionRationale(),
                        shouldShowPostRationale = shouldShowPostPermissionRationale()
                    ))
                }

                LaunchedEffect(Unit) {
                    lockOrientation()
                    permissionLauncher.launch(getPermissionsToAsk())
                    viewModel.event.collect { event ->
                        when(event) {
                            MainEvent.RequestPermissions -> {
                                lockOrientation()
                                permissionLauncher.launch(getPermissionsToAsk())
                            }
                        }
                    }
                }

                if(viewModel.state.showSettingsDialog) {
                    PermissionDialog(
                        title = "Permission permanently declined",
                        text = "You need to grant read and post permission in Settings to make player work",
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
        orientation = requestedOrientation
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
    }

    private fun restoreOrientation() {
        requestedOrientation = orientation
    }
}


fun getPermissionsToAsk(): Array<String> {
    return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.POST_NOTIFICATIONS
        )
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}

fun ComponentActivity.shouldShowPostPermissionRationale(): Boolean {
    return if(sdkTiramisu) {
        shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
    } else false
}

fun ComponentActivity.shouldShowReadPermissionRationale(): Boolean {
    return shouldShowRequestPermissionRationale(getReadPermission())
}

fun Context.hasReadPermission(): Boolean {
    return checkSelfPermission(getReadPermission()) == PackageManager.PERMISSION_GRANTED
}

fun Context.hasPostPermission(): Boolean {
    return if(sdkTiramisu) {
        checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else true
}

fun getReadPermission(): String {
    return if(sdkTiramisu) Manifest.permission.READ_MEDIA_AUDIO
    else Manifest.permission.READ_EXTERNAL_STORAGE
}

val sdkTiramisu: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
