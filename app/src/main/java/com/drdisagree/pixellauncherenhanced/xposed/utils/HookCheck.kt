package com.drdisagree.pixellauncherenhanced.xposed.utils

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import com.drdisagree.pixellauncherenhanced.data.common.Constants.ACTION_HOOK_CHECK_REQUEST
import com.drdisagree.pixellauncherenhanced.data.common.Constants.ACTION_HOOK_CHECK_RESULT
import com.drdisagree.pixellauncherenhanced.data.common.Constants.LAUNCHER3_PACKAGE
import com.drdisagree.pixellauncherenhanced.data.common.Constants.PIXEL_LAUNCHER_PACKAGE
import com.drdisagree.pixellauncherenhanced.xposed.ModPack
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class HookCheck(context: Context) : ModPack(context) {

    private var intentFilter = IntentFilter()
    private var broadcastRegistered = false

    override fun updatePrefs(vararg key: String) {}

    private fun returnBroadcastResult() {
        Thread {
            mContext.sendBroadcast(
                Intent()
                    .setAction(ACTION_HOOK_CHECK_RESULT)
                    .addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            )
        }.start()
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        val isCorrectPackage = loadPackageParam.packageName in setOf(
            PIXEL_LAUNCHER_PACKAGE,
            LAUNCHER3_PACKAGE
        )

        if (!broadcastRegistered && isCorrectPackage) {
            broadcastRegistered = true

            intentFilter.addAction(ACTION_HOOK_CHECK_REQUEST)

            val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (intent.action == ACTION_HOOK_CHECK_REQUEST && isCorrectPackage) {
                        returnBroadcastResult()
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mContext.registerReceiver(
                    broadcastReceiver,
                    intentFilter,
                    Context.RECEIVER_EXPORTED
                )
            } else {
                mContext.registerReceiver(broadcastReceiver, intentFilter)
            }
        }
    }
}