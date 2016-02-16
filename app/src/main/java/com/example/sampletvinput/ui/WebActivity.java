package com.example.sampletvinput.ui;

import android.os.Bundle;
import android.app.Activity;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.sampletvinput.R;

public class WebActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        WebView webView = (WebView)findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(getIntent().getDataString());

    }

}
