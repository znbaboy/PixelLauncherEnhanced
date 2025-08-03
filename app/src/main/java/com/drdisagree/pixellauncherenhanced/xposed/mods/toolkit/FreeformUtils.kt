package com.drdisagree.pixellauncherenhanced.xposed.mods.toolkit

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent

object FreeformUtils {

    private const val TAG = "Free Launcher"

    enum class Variant(val id: Int) {
        AOSP(0),
        SUNSHINE(1),
        LMAO(2),
        YAMF(3);

        companion object {
            fun fromId(id: Int): Variant = entries.find { it.id == id } ?: AOSP
        }
    }

    fun startFreeformByIntent(mContext: Context, task: Any?, mode: Variant) {
        Intent(getFreeformIntent(mode)).setPackage(getFreeformPackage(mode)).apply {
            callMethod(
                "putExtra",
                "packageName",
                task.getFieldSilently("key").callMethod("getPackageName")
            )
            callMethod(
                "putExtra",
                "activityName",
                task.callMethod("getTopComponent").callMethod("getClassName")
            )
            callMethod(
                "putExtra",
                "userId",
                task.getFieldSilently("key").getFieldSilently("userId")
            )
            callMethod(
                "putExtra",
                "taskId",
                task.getFieldSilently("key").getFieldSilently("id")
            )
            mContext.callMethod("sendBroadcast", this)
        }
    }

    fun startFreeformFromRecents(task: Any?, Iamw: Any?) {
        Iamw.callMethod(
            "startActivityFromRecents",
            task.getFieldSilently("key"),
            freeformOpt
        )
    }

    val freeformOpt: ActivityOptions?
        get() {
            val opt = ActivityOptions.makeBasic().apply {
                callMethod("setLaunchWindowingMode", 5)
                callMethod("setTaskAlwaysOnTop", true)
                callMethod("setTaskOverlay", true, true)
                callMethod("setApplyMultipleTaskFlagForShortcut", true)
                callMethod("setApplyActivityFlagsForBubbles", true)
                callMethod("setLaunchedFromBubble", true)
            }

            /* final View decorView = container.getWindow().getDecorView();
            final WindowInsets insets = decorView.getRootWindowInsets();
            r.offsetTo(insets.getSystemWindowInsetLeft() + 50, insets.getSystemWindowInsetTop() + 50);*/
            //opt.setLaunchBounds(r);

            return opt
        }

    fun getFreeformIntent(mode: Variant): String {
        return when (mode) {
            Variant.LMAO -> "com.libremobileos.freeform.START_FREEFORM"
            else -> "com.sunshine.freeform.start_freeform"
        }
    }

    fun getFreeformPackage(mode: Variant): String {
        return when (mode) {
            Variant.LMAO -> "com.libremobileos.freeform"
            else -> "com.sunshine.freeform"
        }
    }
}
