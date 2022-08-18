package com.misoo.partnersalesapp;

/**
 * @Author : Benard Mulinge
 * https://youtube.com/BenitoInTech
 * misoo limited
 */

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.MimeTypeMap;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    WebView webView;
    ProgressBar progressBar;
    SwipeRefreshLayout swipeRefreshLayout;

//    String url = "http://misoo.co.ke:3110";

    String  url = "";


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.web);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        progressBar = findViewById(R.id.progress);
        swipeRefreshLayout = findViewById(R.id.swipe);

        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
        PreferenceManager.setDefaultValues(this,  R.xml.pref_notification, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_data_sync, false);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        url = pref.getString("user_favorite_social", "");

        //improve webView performance
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        webView.getSettings().setCacheMode(webSettings.LOAD_CACHE_ELSE_NETWORK);
        webView.getSettings().setAppCacheEnabled(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
//        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportZoom(false);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSettings.setUseWideViewPort(true);
        webSettings.setSavePassword(true);
        webSettings.setSaveFormData(true);
        webSettings.setEnableSmoothTransition(true);

        // Clear all the Application Cache, Web SQL Database and the HTML5 Web Storage
        WebStorage.getInstance().deleteAllData();

        // Clear all the cookies
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();

        webView.clearCache(true);
        webView.clearFormData();
        webView.clearHistory();
        webView.clearSslPreferences();

        

        webView.setWebViewClient(new myWebViewClient());
        webView.loadUrl(url);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        webView.loadUrl(url);
                    }
                },  3000);
            }
        });

        swipeRefreshLayout.setColorSchemeColors(
                getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_orange_dark),
                getResources().getColor(android.R.color.holo_green_dark),
                getResources().getColor(android.R.color.holo_red_dark)
        );

        //  ==================== START HERE: THIS CODE BLOCK IS TO ENABLE FILE DOWNLOAD FROM THE WEB. ======//

        webView.setDownloadListener(new DownloadListener() {
            String fileName = MimeTypeMap.getFileExtensionFromUrl(url);
            @Override
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {

                DownloadManager.Request request = new DownloadManager.Request(
                        Uri.parse(url));

                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); //Notify client once download is completed!
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);
                Toast.makeText(getApplicationContext(), "Downloading File", //To notify the Client that the file is being downloaded
                        Toast.LENGTH_LONG).show();

            }
        });
        //  ==================== END HERE: THIS CODE BLOCK IS TO ENABLE FILE DOWNLOAD FROM THE WEB. YOU CAN COMMENT IT OUT IF YOUR APPLICATION DOES NOT REQUIRE FILE DOWNLOAD. IT WAS ADDED ON REQUEST ======//

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserPref();

    }

    private void updateUserPref(){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        url = pref.getString("user_favorite_social", "");

    }

    public class myWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

//        @Override
//        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
//            Toast.makeText(getApplicationContext(), "Error while connecting..., swipe to refresh", Toast.LENGTH_LONG).show();
//            webView.loadUrl("file:///android_asset/connection_error.html");
////            Toast.makeText(getApplicationContext(), "There was an error connecting to the server. make sure you" +
////                    "are on a stable internet connection", Toast.LENGTH_LONG).show();
//        }

        @SuppressWarnings("deprecation")
        @Override
        public void onReceivedError(WebView view, int errorCode, String description,        String failingUrl) {
            handleError(errorCode,view);
        }

        @TargetApi(android.os.Build.VERSION_CODES.M)
        @Override
        public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
            // Redirect to deprecated method, so you can use it in all SDK versions
            onReceivedError(view, rerr.getErrorCode(),rerr.getDescription().toString(),req.getUrl().toString());

        }

        public void handleError(int errorCode, WebView view) {

            String message = null;
            if (errorCode == WebViewClient.ERROR_AUTHENTICATION) {
                message = "User authentication failed on server";
            } else if (errorCode == WebViewClient.ERROR_TIMEOUT) {
                message = "The server is taking too much time to communicate. Try again later.";
                webView.loadUrl("file:///android_asset/connection_error.html");
            } else if (errorCode == WebViewClient.ERROR_TOO_MANY_REQUESTS) {
                message = "Too many requests during this load";
            } else if (errorCode == WebViewClient.ERROR_UNKNOWN) {
                message = "Generic error";
            } else if (errorCode == WebViewClient.ERROR_BAD_URL) {
                message = "Check entered URL..";
            } else if (errorCode == WebViewClient.ERROR_CONNECT) {
                message = "Failed to connect to the server, swipe to refresh";
                webView.loadUrl("file:///android_asset/server_error.html");
            } else if (errorCode == WebViewClient.ERROR_FAILED_SSL_HANDSHAKE) {
                message = "Failed to perform SSL handshake";
            } else if (errorCode == WebViewClient.ERROR_HOST_LOOKUP) {
//                message = "Server or proxy hostname lookup failed, swipe to refresh";
                message = "Check your network connection!, swipe to refresh";
                webView.loadUrl("file:///android_asset/connection_error.html");
            } else if (errorCode == WebViewClient.ERROR_PROXY_AUTHENTICATION) {
                message = "User authentication failed on proxy";
            } else if (errorCode == WebViewClient.ERROR_REDIRECT_LOOP) {
                message = "Too many redirects";
            } else if (errorCode == WebViewClient.ERROR_UNSUPPORTED_AUTH_SCHEME) {
                message = "Unsupported authentication scheme (not basic or digest)";
            } else if (errorCode == WebViewClient.ERROR_UNSUPPORTED_SCHEME) {
                message = "unsupported scheme";
            } else if (errorCode == WebViewClient.ERROR_FILE) {
                message = "Generic file error";
            } else if (errorCode == WebViewClient.ERROR_FILE_NOT_FOUND) {
                message = "File not found";
            } else if (errorCode == WebViewClient.ERROR_IO) {
                message = "The server failed to communicate. Try again later.";
            }
            if (message != null) {
                Toast.makeText(getApplicationContext(), "" + message, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            super.onReceivedSslError(view, handler, error);
            handler.cancel();
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


}