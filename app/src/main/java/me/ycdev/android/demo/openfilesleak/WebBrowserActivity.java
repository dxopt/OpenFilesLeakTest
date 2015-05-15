package me.ycdev.android.demo.openfilesleak;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import me.ycdev.android.demo.openfilesleak.utils.AppLogger;

public class WebBrowserActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "WebBrowserActivity";

    private Button mLoadBtn;
    private Button mClearBtn;
    private Button mDestroyBtn;
    private WebView mWebView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webbrowser);

        mLoadBtn = (Button) findViewById(R.id.btn_load);
        mLoadBtn.setOnClickListener(this);
        mClearBtn = (Button) findViewById(R.id.btn_clear);
        mClearBtn.setOnClickListener(this);
        mDestroyBtn = (Button) findViewById(R.id.btn_destroy);
        mDestroyBtn.setOnClickListener(this);

        mWebView = (WebView) findViewById(R.id.web_browser);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                AppLogger.d(TAG, "on progress updated: " + newProgress);
            }
        });
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                AppLogger.d(TAG, "onPageFinished, url: " + url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                AppLogger.d(TAG, "onPageStarted, url: " + url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                AppLogger.d(TAG, "onReceivedError, errCode: " + errorCode + ", desc: " + description
                        + ", failUrl: " + failingUrl);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == mLoadBtn) {
            Toast.makeText(this, "load", Toast.LENGTH_SHORT).show();
            AppLogger.d(TAG, "load");
            loadWebPage();
        } else if (v == mClearBtn) {
            Toast.makeText(this, "clear", Toast.LENGTH_SHORT).show();
            AppLogger.d(TAG, "clear");
            clearWebView();
        } else if (v == mDestroyBtn) {
            Toast.makeText(this, "destroy", Toast.LENGTH_SHORT).show();
            AppLogger.d(TAG, "destroy");
            destroyWebView();
        }
    }

    private void loadWebPage() {
        mWebView.loadUrl("http://www.jd.com");
    }

    private void clearWebView() {
        mWebView.loadUrl("about:blank");
    }

    private void destroyWebView() {
        mWebView.destroy();
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
