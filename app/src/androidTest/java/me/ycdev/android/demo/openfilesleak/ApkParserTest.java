package me.ycdev.android.demo.openfilesleak;

import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.SystemClock;
import android.test.AndroidTestCase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import me.ycdev.android.demo.openfilesleak.utils.AppLogger;
import me.ycdev.android.demo.openfilesleak.utils.OpenFilesUtils;
import me.ycdev.android.demo.openfilesleak.utils.TestUtils;
import me.ycdev.android.lib.common.utils.IoUtils;
import me.ycdev.android.lib.common.utils.StorageUtils;

public class ApkParserTest extends AndroidTestCase {
    private static final String TAG = "ApkParserTest";

    public void testValidApkFile() {
        String validApkFile = TestUtils.pickOneSystemAppPackageName(getContext());
        assertTrue("failed to pick one system package", validApkFile != null);

        doTestValidApkFile(validApkFile);
    }

    public void testValidApkFile2() {
        String srcApkFile = TestUtils.pickOneSystemAppPackageName(getContext());
        assertTrue("failed to pick one system package", srcApkFile != null);

        assertTrue("no external storage", StorageUtils.isExternalStorageAvailable());
        String sdcardRoot = StorageUtils.getExternalStoragePath();
        String destApkFile = new File(sdcardRoot, "valid_apk_test2.apk").getAbsolutePath();
        try {
            IoUtils.copyFile(srcApkFile, destApkFile);
        } catch (IOException e) {
            e.printStackTrace();
            fail("cannot create apk file on external storage");
        }

        doTestValidApkFile(destApkFile);
    }

    private void doTestValidApkFile(String validApkFile) {
        AppLogger.d(TAG, "test apk file: " + validApkFile);
        PackageInfo pkgInfo = getContext().getPackageManager().getPackageArchiveInfo(validApkFile, 0);
        assertTrue("failed to parse the apk file", pkgInfo != null);

        File[] ofList = OpenFilesUtils.getOpenedFiles();
        assertTrue("failed to get open files", ofList != null);
        assertFalse("failed to close the apk file",
                OpenFilesUtils.isOpened(ofList, validApkFile));
    }

    public void testInvalidApkFile() {
        String invalidApkFile = getContext().getFileStreamPath("invalid_apk_test.apk").getAbsolutePath();
        // create a invalid apk file
        try {
            FileOutputStream fos = new FileOutputStream(invalidApkFile);
            fos.write("hello world, we need a long sentence".getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            fail("failed to create invalid apk file");
        }

        doTestInvalidApkFile(invalidApkFile);
    }

    public void testInvalidApkFile2() {
        assertTrue("no external storage", StorageUtils.isExternalStorageAvailable());
        String sdcardRoot = StorageUtils.getExternalStoragePath();
        String invalidApkFile = new File(sdcardRoot, "invalid_apk_test2.apk").getAbsolutePath();
        // create a invalid apk file
        try {
            FileOutputStream fos = new FileOutputStream(invalidApkFile);
            fos.write("hello world, we need a long sentence".getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            fail("failed to create invalid apk file");
        }

        doTestInvalidApkFile(invalidApkFile);
    }

    private void doTestInvalidApkFile(String invalidApkFile) {
        File[] ofList = OpenFilesUtils.getOpenedFiles();
        assertTrue("failed to get open files", ofList != null);
        assertFalse("failed to close the file",
                OpenFilesUtils.isOpened(ofList, invalidApkFile));

        // do the test
        PackageInfo pkgInfo = getContext().getPackageManager().getPackageArchiveInfo(invalidApkFile, 0);
        assertTrue("success to parse the INVALID apk file!", pkgInfo == null);

        ofList = OpenFilesUtils.getOpenedFiles();
        assertTrue("failed to get open files", ofList != null);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            assertFalse("failed to close the file",
                    OpenFilesUtils.isOpened(ofList, invalidApkFile));
        } else {
            // force gc to collect the leaked file
            AppLogger.i(TAG, "force GC and sleep 1s");
            System.runFinalization();
            System.gc();
            SystemClock.sleep(1000);

            assertTrue("the leak fixed!",
                    OpenFilesUtils.isOpened(ofList, invalidApkFile));
        }
    }
}
