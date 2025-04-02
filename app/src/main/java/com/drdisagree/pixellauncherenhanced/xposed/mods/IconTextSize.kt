package com.drdisagree.pixellauncherenhanced.xposed.mods

import android.content.Context
import com.drdisagree.pixellauncherenhanced.data.common.Constants.LAUNCHER_ICON_SIZE
import com.drdisagree.pixellauncherenhanced.data.common.Constants.LAUNCHER_TEXT_SIZE
import com.drdisagree.pixellauncherenhanced.xposed.ModPack
import com.drdisagree.pixellauncherenhanced.xposed.mods.toolkit.XposedHook.Companion.findClass
import com.drdisagree.pixellauncherenhanced.xposed.mods.toolkit.callMethod
import com.drdisagree.pixellauncherenhanced.xposed.mods.toolkit.getField
import com.drdisagree.pixellauncherenhanced.xposed.mods.toolkit.hookConstructor
import com.drdisagree.pixellauncherenhanced.xposed.mods.toolkit.setField
import com.drdisagree.pixellauncherenhanced.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class IconTextSize(context: Context) : ModPack(context) {

    private var invariantDeviceProfileInstance: Any? = null
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
            ) -> invariantDeviceProfileInstance.callMethod("onConfigChanged", mContext)
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        val invariantDeviceProfileClass = findClass("com.android.launcher3.InvariantDeviceProfile")

        invariantDeviceProfileClass
            .hookConstructor()
            .runAfter { param ->
                invariantDeviceProfileInstance = param.thisObject
            }

        val deviceProfileClass = findClass("com.android.launcher3.DeviceProfile")

        deviceProfileClass
            .hookConstructor()
            .runAfter { param ->
                param.thisObject.apply {
                    var iconSizePx = getField("iconSizePx") as Int
                    var folderIconSizePx = getField("folderIconSizePx") as Int
                    var folderChildIconSizePx = getField("folderChildIconSizePx") as Int
                    var allAppsIconSizePx = getField("allAppsIconSizePx") as Int
                    var iconTextSizePx = getField("iconTextSizePx") as Int
                    var folderLabelTextSizePx = getField("folderLabelTextSizePx") as Int
                    var folderChildTextSizePx = getField("folderChildTextSizePx") as Int
                    var allAppsIconTextSizePx = getField("allAppsIconTextSizePx") as Float
                    var folderCellWidthPx = getField("folderCellWidthPx") as Int
                    var folderCellHeightPx = getField("folderCellHeightPx") as Int

                    iconSizePx = (iconSizePx * iconSizeModifier).toInt()
                    folderIconSizePx = (folderIconSizePx * iconSizeModifier).toInt()
                    folderChildIconSizePx = (folderChildIconSizePx * iconSizeModifier).toInt()
                    allAppsIconSizePx = (allAppsIconSizePx * iconSizeModifier).toInt()
                    iconTextSizePx = (iconTextSizePx * textSizeModifier).toInt()
                    folderLabelTextSizePx = (folderLabelTextSizePx * textSizeModifier).toInt()
                    folderChildTextSizePx = (folderChildTextSizePx * textSizeModifier).toInt()
                    allAppsIconTextSizePx = allAppsIconTextSizePx * textSizeModifier
                    folderCellWidthPx = (folderCellWidthPx * iconSizeModifier).toInt()
                    folderCellHeightPx = (folderCellHeightPx * iconSizeModifier).toInt()

                    setField("iconSizePx", iconSizePx)
                    setField("folderIconSizePx", folderIconSizePx)
                    setField("folderChildIconSizePx", folderChildIconSizePx)
                    setField("allAppsIconSizePx", allAppsIconSizePx)
                    setField("iconTextSizePx", iconTextSizePx)
                    setField("folderLabelTextSizePx", folderLabelTextSizePx)
                    setField("folderChildTextSizePx", folderChildTextSizePx)
                    setField("allAppsIconTextSizePx", allAppsIconTextSizePx)
                    setField("folderCellWidthPx", folderCellWidthPx)
                    setField("folderCellHeightPx", folderCellHeightPx)
                }
            }
    }
}