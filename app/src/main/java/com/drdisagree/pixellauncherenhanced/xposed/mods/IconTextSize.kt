package com.drdisagree.pixellauncherenhanced.xposed.mods

import android.content.Context
import android.os.Build
import com.drdisagree.pixellauncherenhanced.data.common.Constants.LAUNCHER_ICON_SIZE
import com.drdisagree.pixellauncherenhanced.data.common.Constants.LAUNCHER_TEXT_SIZE
import com.drdisagree.pixellauncherenhanced.xposed.ModPack
import com.drdisagree.pixellauncherenhanced.xposed.mods.LauncherUtils.Companion.reloadLauncher
import com.drdisagree.pixellauncherenhanced.xposed.mods.toolkit.XposedHook.Companion.findClass
import com.drdisagree.pixellauncherenhanced.xposed.mods.toolkit.getField
import com.drdisagree.pixellauncherenhanced.xposed.mods.toolkit.getFieldSilently
import com.drdisagree.pixellauncherenhanced.xposed.mods.toolkit.hookConstructor
import com.drdisagree.pixellauncherenhanced.xposed.mods.toolkit.setField
import com.drdisagree.pixellauncherenhanced.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class IconTextSize(context: Context) : ModPack(context) {

    private var iconSizeModifier = 1f
    private var textSizeModifier = 1f

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            iconSizeModifier = getSliderInt(LAUNCHER_ICON_SIZE, 100) / 100f
            textSizeModifier = getSliderInt(LAUNCHER_TEXT_SIZE, 100) / 100f
        }

        when (key.firstOrNull()) {
            in setOf(
                LAUNCHER_ICON_SIZE,
                LAUNCHER_TEXT_SIZE
            ) -> reloadLauncher(mContext)
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        val deviceProfileClass = findClass("com.android.launcher3.DeviceProfile")

        deviceProfileClass
            .hookConstructor()
            .runAfter { param ->
                param.thisObject.apply {
                    var iconSizePx = getField("iconSizePx") as Int
                    var folderIconSizePx = getField("folderIconSizePx") as Int
                    var folderChildIconSizePx = getField("folderChildIconSizePx") as Int
                    var allAppsIconSizePx = getFieldSilently("allAppsIconSizePx") as? Int
                    var iconTextSizePx = getField("iconTextSizePx") as Int
                    var folderLabelTextSizePx = getField("folderLabelTextSizePx") as Int
                    var folderChildTextSizePx = getField("folderChildTextSizePx") as Int
                    var allAppsIconTextSizePx = getFieldSilently("allAppsIconTextSizePx") as? Float
                    var folderCellWidthPx = getField("folderCellWidthPx") as Int
                    var folderCellHeightPx = getField("folderCellHeightPx") as Int

                    iconSizePx = (iconSizePx * iconSizeModifier).toInt()
                    folderIconSizePx = (folderIconSizePx * iconSizeModifier).toInt()
                    folderChildIconSizePx = (folderChildIconSizePx * iconSizeModifier).toInt()
                    if (allAppsIconSizePx != null) {
                        allAppsIconSizePx = (allAppsIconSizePx * iconSizeModifier).toInt()
                    }
                    iconTextSizePx = (iconTextSizePx * textSizeModifier).toInt()
                    folderLabelTextSizePx = (folderLabelTextSizePx * textSizeModifier).toInt()
                    folderChildTextSizePx = (folderChildTextSizePx * textSizeModifier).toInt()
                    if (allAppsIconTextSizePx != null) {
                        allAppsIconTextSizePx = allAppsIconTextSizePx * textSizeModifier
                    }
                    folderCellWidthPx = (folderCellWidthPx * iconSizeModifier).toInt()
                    folderCellHeightPx = (folderCellHeightPx * iconSizeModifier).toInt()

                    setField("iconSizePx", iconSizePx)
                    setField("folderIconSizePx", folderIconSizePx)
                    setField("folderChildIconSizePx", folderChildIconSizePx)
                    if (allAppsIconSizePx != null) {
                        setField("allAppsIconSizePx", allAppsIconSizePx)
                    }
                    setField("iconTextSizePx", iconTextSizePx)
                    setField("folderLabelTextSizePx", folderLabelTextSizePx)
                    setField("folderChildTextSizePx", folderChildTextSizePx)
                    if (allAppsIconTextSizePx != null) {
                        setField("allAppsIconTextSizePx", allAppsIconTextSizePx)
                    }
                    setField("folderCellWidthPx", folderCellWidthPx)
                    setField("folderCellHeightPx", folderCellHeightPx)
                }
            }

        val allAppsProfileClass = findClass(
            "com.android.launcher3.deviceprofile.AllAppsProfile",
            suppressError = Build.VERSION.SDK_INT <= Build.VERSION_CODES.VANILLA_ICE_CREAM
        )

        allAppsProfileClass
            .hookConstructor()
            .runAfter { param ->
                param.thisObject.apply {
                    var iconSizePx = getField("iconSizePx") as Int
                    var iconTextSizePx = getField("iconTextSizePx") as Float

                    iconSizePx = (iconSizePx * iconSizeModifier).toInt()
                    iconTextSizePx = iconTextSizePx * textSizeModifier

                    setField("iconSizePx", iconSizePx)
                    setField("iconTextSizePx", iconTextSizePx)
                }
            }
    }
}