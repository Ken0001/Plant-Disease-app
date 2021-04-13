package com.ken.plant;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class PesticideInfo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pesticide_info);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        Intent intent = this.getIntent();
        String prediction = intent.getStringExtra("prediction");


        WebView webView = (WebView) findViewById(R.id.web_view);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient()); //不調用系統瀏覽器

        WebSettings webSettings=webView.getSettings();
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);

        switch(prediction){
            case "黑點病" :
                webView.loadUrl("https://otserv2.tactri.gov.tw/ppm/PLC0101.aspx?CropNo=00191B079&SRH=%e6%96%87%e6%97%a6&"); //黑點
                break;
            case "缺鎂" :
                webView.loadUrl("https://web.tari.gov.tw/techcd/%E6%9E%9C%E6%A8%B9/%E5%B8%B8%E7%B6%A0%E6%9E%9C%E6%A8%B9/%E6%9F%91%E6%A1%94/%E7%97%85%E5%AE%B3/%E6%A1%94%E6%9F%91-%E7%87%9F%E9%A4%8A%E7%B4%A0%E7%BC%BA%E4%B9%8F%E3%80%81%E9%81%8E%E5%A4%9A.htm");
                break;
            case "潛葉蛾" :
                webView.loadUrl("https://otserv2.tactri.gov.tw/ppm/PLC0101.aspx?CropNo=00191C394&SRH=%e6%96%87%e6%97%a6&");
                break;
            case "油胞病" :
                webView.loadUrl("https://web.tari.gov.tw/techcd/%E6%9E%9C%E6%A8%B9/%E5%B8%B8%E7%B6%A0%E6%9E%9C%E6%A8%B9/%E6%9F%91%E6%A1%94/%E7%97%85%E5%AE%B3/%E6%A1%94%E6%9F%91-%E6%B2%B9%E6%96%91%E7%97%85.htm");
                break;
            default :
                webView.loadUrl("https://otserv2.tactri.gov.tw/ppm/PLC02.aspx");
        }
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) { //網頁的返回上一頁
//        WebView webview = (WebView) findViewById(R.id.web_view);
//        if (keyCode == KeyEvent.KEYCODE_BACK && webview.canGoBack()) {
//            webview.goBack();
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            PesticideInfo.this.finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}