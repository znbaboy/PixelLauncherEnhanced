package com.drdisagree.pixellauncherenhanced.xposed.mods

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
import com.drdisagree.pixellauncherenhanced.data.common.Constants.HIDE_GESTURE_PILL
import com.drdisagree.pixellauncherenhanced.xposed.ModPack
import com.drdisagree.pixellauncherenhanced.xposed.mods.toolkit.XposedHook.Companion.findClass
import com.drdisagree.pixellauncherenhanced.xposed.mods.toolkit.callMethod
import com.drdisagree.pixellauncherenhanced.xposed.mods.toolkit.hookConstructor
import com.drdisagree.pixellauncherenhanced.xposed.mods.toolkit.hookMethod
import com.drdisagree.pixellauncherenhanced.xposed.mods.toolkit.setField
import com.drdisagree.pixellauncherenhanced.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class TaskbarHandle(context: Context) : ModPack(context) {

    private var mHidePill = false
    private var stashedHandleViewObj: Any? = null
    private var mIsRegionDark: Boolean? = null

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            mHidePill = getBoolean(HIDE_GESTURE_PILL, false)
        }

        when (key.firstOrNull()) {
            HIDE_GESTURE_PILL -> updateHandleColor(true)
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        val stashedHandleViewClass = findClass("com.android.launcher3.taskbar.StashedHandleView")

        stashedHandleViewClass
            .hookConstructor()
            .runAfter { param ->
                stashedHandleViewObj = param.thisObject
                updateHandleColor()
            }

        stashedHandleViewClass
            .hookMethod("updateHandleColor")
            .runBefore { param ->
                mIsRegionDark = param.args[0] as? Boolean
            }
    }

    @SuppressLint("DiscouragedApi")
    private fun updateHandleColor(apply: Boolean = false) {
        val mStashedHandleLightColor = if (!mHidePill) ContextCompat.getColor(
            mContext,
            mContext.resources.getIdentifier(
                "taskbar_stashed_handle_light_color",
                "color",
                mContext.packageName
            )
        ) else Color.TRANSPARENT
        val mStashedHandleDarkColor = if (!mHidePill) ContextCompat.getColor(
            mContext,
            mContext.resources.getIdentifier(
                "taskbar_stashed_handle_dark_color",
                "color",
                mContext.packageName
            )
        ) else Color.TRANSPARENT

        stashedHandleViewObj?.apply {
            setField("mStashedHandleLightColor", mStashedHandleLightColor)
            setField("mStashedHandleDarkColor", mStashedHandleDarkColor)

            if (apply) {
                if (mIsRegionDark != null) {
                    callMethod("updateHandleColor", mIsRegionDark != true, true)
                    callMethod("updateHandleColor", mIsRegionDark == true, true)
                } else {
                    callMethod("updateHandleColor", false, true)
                    callMethod("updateHandleColor", true, true)
                }
            }
        }
    }
}