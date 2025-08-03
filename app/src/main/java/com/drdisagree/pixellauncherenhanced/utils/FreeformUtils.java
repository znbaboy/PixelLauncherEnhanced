package com.drdisagree.pixellauncherenhanced.utils;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import de.robv.android.xposed.XposedHelpers;

public class FreeformUtils {
  private static String TAG = "Free Launcher";
  public static final int AOSP = 0;
  public static final int SUNSHINE = 1;
  public static final int LMAO = 2;
  public static final int YAMF = 3;

  public static void startFreeformByIntent(Context mContext, Object task, int mode) {

    Intent intent = new Intent(getFreeformIntent(mode)).setPackage(getFreeformPackage(mode));
    XposedHelpers.callMethod(
        intent,
        "putExtra",
        "packageName",
        XposedHelpers.callMethod(XposedHelpers.getObjectField(task, "key"), "getPackageName"));
    XposedHelpers.callMethod(
        intent,
        "putExtra",
        "activityName",
        XposedHelpers.callMethod(
            XposedHelpers.callMethod(task, "getTopComponent"), "getClassName"));
    XposedHelpers.callMethod(
        intent,
        "putExtra",
        "userId",
        XposedHelpers.getObjectField(XposedHelpers.getObjectField(task, "key"), "userId"));
    XposedHelpers.callMethod(
        intent,
        "putExtra",
        "taskId",
        XposedHelpers.getObjectField(XposedHelpers.getObjectField(task, "key"), "id"));

    XposedHelpers.callMethod(mContext, "sendBroadcast", intent);
  }

  public static void startFreeformFromRecents(Object task, Object Iamw) {
    XposedHelpers.callMethod(
        Iamw,
        "startActivityFromRecents",
        XposedHelpers.getObjectField(task, "key"),
        getFreeformOpt());
  }

  public static ActivityOptions getFreeformOpt() {
    ActivityOptions opt = ActivityOptions.makeBasic();
    XposedHelpers.callMethod(opt, "setLaunchWindowingMode", 5);
    XposedHelpers.callMethod(opt, "setTaskAlwaysOnTop", true);
    XposedHelpers.callMethod(opt, "setTaskOverlay", true, true);
    XposedHelpers.callMethod(opt, "setApplyMultipleTaskFlagForShortcut", true);
    XposedHelpers.callMethod(opt, "setApplyActivityFlagsForBubbles", true);
    XposedHelpers.callMethod(opt, "setLaunchedFromBubble", true);
    /* final View decorView = container.getWindow().getDecorView();
    final WindowInsets insets = decorView.getRootWindowInsets();
    r.offsetTo(insets.getSystemWindowInsetLeft() + 50, insets.getSystemWindowInsetTop() + 50);*/
    //opt.setLaunchBounds(r);

    return opt;
  }

  public static String getFreeformIntent(int mode) {
    String r;
    switch (mode) {
      case LMAO:
        r = "com.libremobileos.freeform.START_FREEFORM";
        break;
      default:
        r = "com.sunshine.freeform.start_freeform";
    }
    return r;
  }

  public static String getFreeformPackage(int mode) {
    String r;
    switch (mode) {
      case LMAO:
        r = "com.libremobileos.freeform";
        break;
      default:
        r = "com.sunshine.freeform";
    }
    return r;
  }
}
