package me.ycdev.android.demo.openfilesleak.utils;

import java.io.File;
import java.io.IOException;

public class OpenFilesUtils {
    private static final String TAG = "OpenFilesUtils";

    public static File[] getOpenedFiles() {
        int pid = android.os.Process.myPid();
        File fdDir = new File("/proc/" + pid + "/fd");
        File[] openedFiles = fdDir.listFiles();
        if (openedFiles == null) {
            AppLogger.w(TAG, "failed to get opened files");
            return null;
        }
        AppLogger.d(TAG, "opened files: " + openedFiles.length);
        return openedFiles;
    }

    public static boolean isOpened(File[] openedFiles, String filePath) {
        for (File of : openedFiles) {
            try {
                String ofPath = of.getCanonicalPath();
                if (filePath.equals(ofPath)) {
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static void dumpOpenedFiles(File[] openedFiles) {
        for (File of : openedFiles) {
            try {
                AppLogger.d(TAG, "of: " + of.getCanonicalPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
