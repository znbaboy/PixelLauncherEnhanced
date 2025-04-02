package com.drdisagree.pixellauncherenhanced.xposed.mods

import android.content.Context
import android.widget.Toast
import com.drdisagree.pixellauncherenhanced.R
import com.drdisagree.pixellauncherenhanced.data.common.Constants.LOCK_LAYOUT
import com.drdisagree.pixellauncherenhanced.xposed.HookRes.Companion.modRes
import com.drdisagree.pixellauncherenhanced.xposed.ModPack
import com.drdisagree.pixellauncherenhanced.xposed.mods.toolkit.XposedHook.Companion.findClass
import com.drdisagree.pixellauncherenhanced.xposed.mods.toolkit.callMethod
import com.drdisagree.pixellauncherenhanced.xposed.mods.toolkit.hookMethod
import com.drdisagree.pixellauncherenhanced.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class LockLayout(context: Context) : ModPack(context) {

    private var lockLayout = false

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            lockLayout = getBoolean(LOCK_LAYOUT, false)
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        val dragControllerClass = findClass("com.android.launcher3.dragndrop.DragController")
        val launcherAppWidgetHostViewClass =
            findClass("com.android.launcher3.widget.LauncherAppWidgetHostView")
        val systemShortcutClass = findClass("com.android.launcher3.popup.SystemShortcut")
        val popupContainerWithArrowClass =
            findClass("com.android.launcher3.popup.PopupContainerWithArrow")
        val optionsPopupViewClass = findClass("com.android.launcher3.views.OptionsPopupView")

        dragControllerClass
            .hookMethod("onControllerInterceptTouchEvent")
            .runBefore { param ->
                if (!lockLayout) return@runBefore

                param.thisObject.callMethod("cancelDrag")
                param.result = false
            }

        launcherAppWidgetHostViewClass
            .hookMethod("onLongClick")
            .runBefore { param ->
                if (!lockLayout) return@runBefore

                param.result = true
            }

        systemShortcutClass
            .hookMethod("onClick")
            .runBefore { param ->
                if (!lockLayout) return@runBefore

                param.result = null
            }

        popupContainerWithArrowClass
            .hookMethod("onLongClick")
            .runBefore { param ->
                if (!lockLayout) return@runBefore

                param.result = false
            }

        optionsPopupViewClass
            .hookMethod("openWidgets")
            .runBefore { param ->
                if (!lockLayout) return@runBefore

                Toast.makeText(
                    mContext,
                    modRes.getString(R.string.layout_is_locked),
                    Toast.LENGTH_SHORT
                ).show()

                param.result = null
            }
    }
}