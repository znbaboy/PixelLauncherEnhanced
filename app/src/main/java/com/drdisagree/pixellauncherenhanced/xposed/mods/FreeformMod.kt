package com.drdisagree.pixellauncherenhanced.xposed.mods

import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.core.graphics.ColorUtils
import com.drdisagree.pixellauncherenhanced.data.common.Constants.FREEFORM_GESTURE_PROGRESS
import com.drdisagree.pixellauncherenhanced.data.common.Constants.FREEFORM_GESTURE
import com.drdisagree.pixellauncherenhanced.data.common.Constants.FREEFORM_MODE
import com.drdisagree.pixellauncherenhanced.xposed.ModPack
import com.drdisagree.pixellauncherenhanced.xposed.mods.toolkit.XposedHook.Companion.findClass
import com.drdisagree.pixellauncherenhanced.xposed.mods.toolkit.XposedHook.Companion.newInstance
import com.drdisagree.pixellauncherenhanced.xposed.mods.toolkit.callMethod
import com.drdisagree.pixellauncherenhanced.xposed.mods.toolkit.getField
import com.drdisagree.pixellauncherenhanced.xposed.mods.toolkit.getStaticField
import com.drdisagree.pixellauncherenhanced.xposed.mods.toolkit.hookMethod
import com.drdisagree.pixellauncherenhanced.xposed.mods.toolkit.setFieldSilently
import com.drdisagree.pixellauncherenhanced.xposed.utils.XPrefs.Xprefs
import com.drdisagree.pixellauncherenhanced.utils.FreeformUtils.startFreeformByIntent
import com.drdisagree.pixellauncherenhanced.utils.FreeformUtils.startFreeformFromRecents
import com.drdisagree.pixellauncherenhanced.utils.FreeformUtils.AOSP
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class FreeformMod(context: Context) : ModPack(context) {

    private var freeformMode: Int = 0
    private var offProgress: Float = 3f
    private var isEnabled: Boolean = false

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            offProgress = getSliderFloat(FREEFORM_GESTURE_PROGRESS, 3f)
            isEnabled = getBoolean(FREEFORM_GESTURE, false)
            freeformMode = Integer.valueOf(getListString(FREEFORM_MODE, "2"))
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        val absSwipeClass = findClass("com.android.quickstep.AbsSwipeUpHandler")
        
        absSwipeClass
            .hookMethod("updateSysUiFlags")
            .runAfter { param ->
                if (!isEnabled) return@runAfter
                
                val mProgress = param.thisObject
                    .getField("mCurrentShift").getField("value") as Float
                    
                if (mProgress > offProgress) {
                    param.thisObject.callMethod("performHapticFeedback")
                }
            
            }

        absSwipeClass
            .hookMethod("initStateCallbacks")
            .runAfter{ param ->
                if (!isEnabled) return@runAfter
                
                val STATE_END_TARGET_SET = findClass(
                              "com.android.quickstep.GestureState")
                          .getStaticField("STATE_END_TARGET_SET") as Int
                val STATE_RECENTS_ANIMATION_STARTED = findClass(
                              "com.android.quickstep.GestureState")
                          .getStaticField("STATE_RECENTS_ANIMATION_STARTED") as Int
                val HOME_TARGET = findClass(
                              "com.android.quickstep.GestureState.GestureEndTarget")
                          ?.getEnumConstants()[0]
                          
                          
                param.thisObject.getField("mGestureState")
                    .callMethod("runOnceAtState",
                    STATE_END_TARGET_SET,
                    Runnable {
                        val mProgress = param.thisObject
                            .getField("mCurrentShift").getField("value") as Float
                            
                        if (mProgress > offProgress) {
                            var mTask = param.thisObject
                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
                                mTask = (param.thisObject
                                    .getField("mRecentsView").callMethod("getRunningTaskView").callMethod("getTaskContainers")).callMethod("get",0).callMethod("getTask")
                            } else {
                                mTask = param.thisObject
                                    .getField("mRecentsView").callMethod("getRunningTaskView").callMethod("getTask")
                            }
                            if (freeformMode == AOSP) {
                                startFreeformFromRecents(mTask, newInstance("com.android.systemui.shared.system.ActivityManagerWrapper"))
                            } else {
                                startFreeformByIntent(mContext, mTask, freeformMode)
                            }
                            param.thisObject.getField("mGestureState").callMethod("setEndTarget",HOME_TARGET)
                            
                        }
                    })
            }
        
    }
}