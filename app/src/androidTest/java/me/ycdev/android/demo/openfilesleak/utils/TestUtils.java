package me.ycdev.android.demo.openfilesleak.utils;

import android.content.Context;
import android.content.pm.PackageInfo;

import java.util.List;

public class TestUtils {
    public static String pickOneSystemAppPackageName(Context cxt) {
        List<PackageInfo> pkgList = cxt.getPackageManager().getInstalledPackages(0);
        for (PackageInfo pkgInfo : pkgList) {
            if (pkgInfo.applicationInfo.sourceDir.startsWith("/system/app/")) {
                return pkgInfo.applicationInfo.sourceDir;
            }
        }
        return null;
    }
}
