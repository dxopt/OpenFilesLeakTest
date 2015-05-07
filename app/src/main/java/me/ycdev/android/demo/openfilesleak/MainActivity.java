package me.ycdev.android.demo.openfilesleak;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipFile;

import me.ycdev.android.demo.openfilesleak.utils.AppLogger;


public class MainActivity extends ActionBarActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppLogger.d(TAG, "onCreate");

        findViewById(R.id.btn_run_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runZipFileTester();
            }
        });
    }

    private void runZipFileTester() {
        File invalidZipFile = getFileStreamPath("zip_100");
        // create a invalid zip file
        try {
            FileOutputStream fos = new FileOutputStream(invalidZipFile);
            fos.write("hello".getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // do the test
        for (int i = 0; i < 100; i++) {
            try {
                new ZipFile(invalidZipFile);
                throw new RuntimeException("failure expected");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // force gc to collect the leaked file
        AppLogger.i(TAG, "force GC and sleep 1s");
        System.runFinalization();
        System.gc();
        SystemClock.sleep(1000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_force_gc) {
            AppLogger.i(TAG, "force GC");
            System.gc();
            System.runFinalization();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
