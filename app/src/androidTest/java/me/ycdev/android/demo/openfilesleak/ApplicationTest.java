package me.ycdev.android.demo.openfilesleak;

import android.app.Application;
import android.test.ApplicationTestCase;

import me.ycdev.android.demo.openfilesleak.utils.AppLogger;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    private static final String TAG = "ApplicationTest";

    public ApplicationTest() {
        super(Application.class);
        AppLogger.d(TAG, "test begin");
    }
}