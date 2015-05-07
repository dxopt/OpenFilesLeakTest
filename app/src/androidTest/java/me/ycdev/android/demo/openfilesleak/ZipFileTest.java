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

public class ZipFileTest extends AndroidTestCase {
    private static final String TAG = "ZipFileTest";

    public void testValidZipFile() {
        String validZipFile = TestUtils.pickOneSystemAppPackageName(getContext());
        AppLogger.d(TAG, "test zip file: " + validZipFile);
        assertTrue("failed to pick one system package", validZipFile != null);

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
        String invalidZipFile = getContext().getFileStreamPath("zip_test").getAbsolutePath();
        // create a invalid zip file
        try {
            FileOutputStream fos = new FileOutputStream(invalidZipFile);
            fos.write("hello".getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            fail("failed to create invalid zip file");
        }

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
            assertFalse("failed to collect the leaked file",
                    OpenFilesUtils.isOpened(ofList, invalidZipFile));
        } else {
            // The bug is already fixed in Android 5.0+
            assertFalse("failed to close the file",
                    OpenFilesUtils.isOpened(ofList, invalidZipFile));
        }
    }
}
