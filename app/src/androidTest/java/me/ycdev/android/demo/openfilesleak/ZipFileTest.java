package me.ycdev.android.demo.openfilesleak;

import android.os.Build;
import android.os.SystemClock;
import android.test.AndroidTestCase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipFile;

import me.ycdev.android.demo.openfilesleak.utils.AppLogger;
import me.ycdev.android.demo.openfilesleak.utils.OpenFilesUtils;
import me.ycdev.android.demo.openfilesleak.utils.TestUtils;
import me.ycdev.android.lib.common.utils.IoUtils;
import me.ycdev.android.lib.common.utils.StorageUtils;

public class ZipFileTest extends AndroidTestCase {
    private static final String TAG = "ZipFileTest";

    public void testValidZipFile() {
        String validZipFile = TestUtils.pickOneSystemAppPackageName(getContext());
        assertTrue("failed to pick one system package", validZipFile != null);

        doTestValidZipFile(validZipFile);
    }

    public void testValidZipFile2() {
        String srcZipFile = TestUtils.pickOneSystemAppPackageName(getContext());
        assertTrue("failed to pick one system package", srcZipFile != null);

        assertTrue("no external storage", StorageUtils.isExternalStorageAvailable());
        String sdcardRoot = StorageUtils.getExternalStoragePath();
        String destZipFile = new File(sdcardRoot, "valid_zip_test2.zip").getAbsolutePath();
        try {
            IoUtils.copyFile(srcZipFile, destZipFile);
        } catch (IOException e) {
            e.printStackTrace();
            fail("cannot create zip file on external storage");
        }

        doTestValidZipFile(destZipFile);
    }

    private void doTestValidZipFile(String validZipFile) {
        AppLogger.d(TAG, "test zip file: " + validZipFile);
        try {
            ZipFile zipFile = new ZipFile(validZipFile);
            File[] ofList = OpenFilesUtils.getOpenedFiles();
            assertTrue("failed to get open files", ofList != null);
            assertTrue("failed to open the zip file",
                    OpenFilesUtils.isOpened(ofList, validZipFile));

            zipFile.close();
            ofList = OpenFilesUtils.getOpenedFiles();
            assertTrue("failed to get open files", ofList != null);
            assertFalse("failed to close the zip file",
                    OpenFilesUtils.isOpened(ofList, validZipFile));
        } catch (IOException e) {
            e.printStackTrace();
            fail("failed to parse the zip file: " + validZipFile);
        }
    }

    public void testInvalidZipFile() {
        String invalidZipFile = getContext().getFileStreamPath("invalid_zip_test.zip").getAbsolutePath();
        // create a invalid zip file
        try {
            FileOutputStream fos = new FileOutputStream(invalidZipFile);
            fos.write("hello world, we need a long sentence".getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            fail("failed to create invalid zip file");
        }

        doTestInvalidZipFile(invalidZipFile);
    }

    public void testInvalidZipFile2() {
        assertTrue("no external storage", StorageUtils.isExternalStorageAvailable());
        String sdcardRoot = StorageUtils.getExternalStoragePath();
        String invalidZipFile = new File(sdcardRoot, "invalid_zip_test2.zip").getAbsolutePath();
        // create a invalid apk file
        try {
            FileOutputStream fos = new FileOutputStream(invalidZipFile);
            fos.write("hello world, we need a long sentence".getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            fail("failed to create invalid zip file");
        }

        doTestInvalidZipFile(invalidZipFile);
    }

    private void doTestInvalidZipFile(String invalidZipFile) {
        File[] ofList = OpenFilesUtils.getOpenedFiles();
        assertTrue("failed to get open files", ofList != null);
        assertFalse("failed to close the file",
                OpenFilesUtils.isOpened(ofList, invalidZipFile));

        // do the test
        try {
            new ZipFile(invalidZipFile);
            fail("no exception thrown when parsing an invalid zip file");
        } catch (IOException e) {
            // expected
            AppLogger.w(TAG, "expected exception: " + e);
        }
        ofList = OpenFilesUtils.getOpenedFiles();
        assertTrue("failed to get open files", ofList != null);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // There is a leak bug in Android 4.x
            assertTrue("failed to leak the file",
                    OpenFilesUtils.isOpened(ofList, invalidZipFile));

            // force gc to collect the leaked file
            AppLogger.i(TAG, "force GC and sleep 1s");
            System.runFinalization();
            System.gc();
            SystemClock.sleep(1000);

            ofList = OpenFilesUtils.getOpenedFiles();
            assertTrue("failed to get open files", ofList != null);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH &&
                    Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
                // Android 4.0 ~ 4.1: the opened file will be leaked
                assertTrue("the leak fixed",
                        OpenFilesUtils.isOpened(ofList, invalidZipFile));
            } else {
                // Android 4.2 ~ 4.4: no leak after GC
                assertFalse("failed to collect the leaked file",
                        OpenFilesUtils.isOpened(ofList, invalidZipFile));
            }
        } else {
            // Android 5.0 ~: no leak
            assertFalse("failed to close the file",
                    OpenFilesUtils.isOpened(ofList, invalidZipFile));
        }
    }
}
