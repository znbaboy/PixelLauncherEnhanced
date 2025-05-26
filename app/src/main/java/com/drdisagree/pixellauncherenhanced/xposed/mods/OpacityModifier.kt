package com.drdisagree.pixellauncherenhanced.xposed.mods

import android.content.Context
import android.graphics.Color
import androidx.core.graphics.ColorUtils
import com.drdisagree.pixellauncherenhanced.data.common.Constants.APP_DRAWER_BACKGROUND_OPACITY
import com.drdisagree.pixellauncherenhanced.data.common.Constants.DISABLE_RECENTS_LIVE_TILE
import com.drdisagree.pixellauncherenhanced.data.common.Constants.RECENTS_BACKGROUND_OPACITY
import com.drdisagree.pixellauncherenhanced.xposed.ModPack
import com.drdisagree.pixellauncherenhanced.xposed.mods.toolkit.XposedHook.Companion.findClass
import com.drdisagree.pixellauncherenhanced.xposed.mods.toolkit.callMethod
import com.drdisagree.pixellauncherenhanced.xposed.mods.toolkit.callMethodSilently
import com.drdisagree.pixellauncherenhanced.xposed.mods.toolkit.getField
import com.drdisagree.pixellauncherenhanced.xposed.mods.toolkit.getFieldSilently
import com.drdisagree.pixellauncherenhanced.xposed.mods.toolkit.hookMethod
import com.drdisagree.pixellauncherenhanced.xposed.mods.toolkit.setFieldSilently
import com.drdisagree.pixellauncherenhanced.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class OpacityModifier(context: Context) : ModPack(context) {

    private var appDrawerBackgroundOpacity: Int = 100
    private var recentsBackgroundOpacity: Int = 100
    private var disableRecentsLiveTile: Boolean = false

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            appDrawerBackgroundOpacity =
                getSliderInt(APP_DRAWER_BACKGROUND_OPACITY, 100) * 255 / 100
            recentsBackgroundOpacity = getSliderInt(RECENTS_BACKGROUND_OPACITY, 100) * 255 / 100
            disableRecentsLiveTile = getBoolean(DISABLE_RECENTS_LIVE_TILE, false)
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        val allAppsStateClass = findClass("com.android.launcher3.uioverrides.states.AllAppsState")
        val overviewStateClass = findClass("com.android.launcher3.uioverrides.states.OverviewState")
        val quickSwitchStateClass =
            findClass("com.android.launcher3.uioverrides.states.QuickSwitchState")
        val recentsStateClass = findClass("com.android.quickstep.fallback.RecentsState")
        val hintStateClass = findClass(
            "com.android.launcher3.states.HintState",
            "com.android.launcher3.uioverrides.states.HintState"
        )
        val activityAllAppsContainerViewClass =
            findClass("com.android.launcher3.allapps.ActivityAllAppsContainerView")

        activityAllAppsContainerViewClass
            .hookMethod("updateHeaderScroll")
            .runAfter { param ->
                if (appDrawerBackgroundOpacity != 255) {
                    param.thisObject.setFieldSilently("mHeaderColor", Color.TRANSPARENT)
                    param.thisObject.callMethodSilently("invalidateHeader")
                }
            }

        allAppsStateClass
            .hookMethod("getWorkspaceScrimColor")
            .runAfter { param ->
                val isTablet = param.args[0]
                    .callMethodSilently("getDeviceProfile")
                    .getFieldSilently("isTablet") as? Boolean == true

                if (!isTablet) {
                    param.result = ColorUtils.setAlphaComponent(
                        param.result as Int,
                        appDrawerBackgroundOpacity
                    )
                }
            }

        overviewStateClass
            .hookMethod("getWorkspaceScrimColor")
            .runAfter { param ->
                param.result = ColorUtils.setAlphaComponent(
                    param.result as Int,
                    recentsBackgroundOpacity
                )
            }

        quickSwitchStateClass
            .hookMethod("getWorkspaceScrimColor")
            .runAfter { param ->
                val launcher = param.args[0]
                val deviceProfile = launcher.callMethod("getDeviceProfile")
                val isTaskbarPresentInApps =
                    deviceProfile.callMethodSilently("isTaskbarPresentInApps") as? Boolean
                        ?: deviceProfile.getField("isTaskbarPresentInApps") as Boolean
                val currentResult = param.result as Int

                if (currentResult != Color.TRANSPARENT && !isTaskbarPresentInApps) {
                    param.result = ColorUtils.setAlphaComponent(
                        param.result as Int,
                        recentsBackgroundOpacity
                    )
                }
            }

        recentsStateClass
            .hookMethod("getScrimColor")
            .runAfter { param ->
                param.result = ColorUtils.setAlphaComponent(
                    param.result as Int,
                    recentsBackgroundOpacity
                )
            }

        hintStateClass
            .hookMethod("getWorkspaceScrimColor")
            .runAfter { param ->
                param.result = ColorUtils.setAlphaComponent(
                    param.result as Int,
                    recentsBackgroundOpacity
                )
            }

        val recentsViewClass = findClass("com.android.quickstep.views.RecentsView")

        recentsViewClass
            .hookMethod("onGestureAnimationEnd")
            .runAfter { param ->
                if (!disableRecentsLiveTile) return@runAfter

                param.thisObject.callMethod(
                    "switchToScreenshot",
                    Runnable {
                        param.thisObject.callMethod(
                            "finishRecentsAnimation",
                            true /* toRecents */,
                            false /* shouldPip */,
                            null
                        )
                    }
                )
            }
    }
}