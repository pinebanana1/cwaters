package com.ora.calmwaters.ui;

import static com.ora.calmwaters.ui.ProtocolActivity.dailyLog;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.annotation.RequiresApi;

import calmwaters.R;
import androidx.appcompat.app.AppCompatActivity;

public class DiaryActivity extends AppCompatActivity {

    private WebView webView = null;
    private WebChromeClient.CustomViewCallback mWebviewCallback = null;
    private FrameLayout mContainer = null;
    private ViewGroup mFullscreenLayout = null;
    private boolean mIsFullscreen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_webview);

        dailyLog.append("Dear diary create");

// Hide both the navigation bar and the status bar.
// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
// a general rule, you should design your app to hide the status bar whenever you
// hide the navigation bar.

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);

        webView = (WebView) findViewById(R.id.webview);
        mFullscreenLayout = findViewById(R.id.fullscreenLayout);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        //webSettings.setBuiltInZoomControls(true);
        //webSettings.setDisplayZoomControls(true);
        //webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.setSupportZoom(true);
        //webSettings.setUserAgentString("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.55 Safari/537.36");

        WebViewClientImpl webViewClient = new WebViewClientImpl(this);
        webView.setWebViewClient(webViewClient);
        //webView.setPadding(0,0,0,0);

        //webView.setInitialScale(99);
        //webView.loadDataWithBaseURL(null, "<style>img{display: inline;height: auto;max-width: 100%;}</style>" /*+ post.getContent()*/, "text/html", "UTF-8", null);

//        Display display = getWindowManager().getDefaultDisplay();
//        int width = display.getWidth();
//
//        String data = "<html><head><title>Example</title><meta name=\"viewport\"\"content=\"width="+width+", initial-scale=0.65 \" /></head>";
//        data = data + "<body><center><img width=\""+width+"\" src=\""+url+"\" /></center></body></html>";
//        webView.loadData(data, "text/html", null);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);

                // Your custom code.
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                //super.onShowCustomView(view, callback);

                if (view instanceof FrameLayout) {
                    // A video wants to be shown
                    FrameLayout frameLayout = (FrameLayout) view;
                    View focusedChild = frameLayout.getFocusedChild();

                    mWebviewCallback = callback;
                    mContainer = frameLayout;

                    webView.setVisibility(View.INVISIBLE);
                    mFullscreenLayout.addView(mContainer, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    mFullscreenLayout.setVisibility(View.VISIBLE);
//
//                    // Hide the non-video view, add the video view, and show it
//                    activityNonVideoView.setVisibility(View.INVISIBLE);
//                    activityVideoView.addView(videoViewContainer, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//                    activityVideoView.setVisibility(View.VISIBLE);

                    mIsFullscreen = true;
                }
            }

            @Override
            public void onHideCustomView() {
                //super.onHideCustomView();
                if (mIsFullscreen) {
                    // Hide the video view, remove it, and show the non-video view
                    mFullscreenLayout.setVisibility(View.INVISIBLE);
                    mFullscreenLayout.removeView(mContainer);
                    webView.setVisibility(View.VISIBLE);

                    // Call back (only in API level <19, because in API level 19+ with chromium webview it crashes)
                    if (mWebviewCallback != null) {
                        mWebviewCallback.onCustomViewHidden();
                    }

                    // Reset variables
                    mIsFullscreen = false;
                    mContainer = null;
                    mWebviewCallback = null;
                }
            }


            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
            }

            @Override
            public void onRequestFocus(WebView view) {
                super.onRequestFocus(view);
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                return super.onJsConfirm(view, url, message, result);
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                return super.onJsPrompt(view, url, message, defaultValue, result);
            }

            @Override
            public void onCloseWindow(WebView window) {
                super.onCloseWindow(window);
            }

            @Override
            public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
                return super.onJsBeforeUnload(view, url, message, result);
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.i("WebView", consoleMessage.toString());
                return super.onConsoleMessage(consoleMessage);
            }

            @Override
            public void getVisitedHistory(ValueCallback<String[]> callback) {
                super.getVisitedHistory(callback);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
            }
        });

        String url = getIntent().getDataString();
        boolean first = true;
        for (String key : getIntent().getExtras().keySet()) {
            if (first) {
                url += '?';
                first = false;
            }
            else
                url+= '&';
            url += key + '='+getIntent().getStringExtra(key);
        }

        webView.loadUrl(url);
        //webView.loadUrl("https://run.pavlovia.org/OraClinical/fitted-screen-vanishing-optotypes");
        //webView.loadUrl("https://run.pavlovia.org/OraClinical/readingpassages/html/");
        //webView.loadUrl("https://www.findingfive.com/experiments/preview/61232f9ae13248aecada338a/?groupId=default");
        //webView.loadData("<html><body>Hello, world!</body></html>", "text/html", "UTF-8");
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && this.webView.canGoBack()) {
            this.webView.goBack();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public class WebViewClientImpl extends WebViewClient {

        private Activity activity = null;
        boolean isFinished;

        public WebViewClientImpl(Activity activity) {
            this.activity = activity;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, String url) {
            return false;
//            if(url.indexOf("https://run.pavlovia.org/OraClinical/readingpassages/html") > -1 ||
//                    url.indexOf("https://www.findingfive.com/experiments/preview/61232f9ae13248aecada338a/?groupId=default") > -1 ||
//                    url.indexOf("https://www.findingfive.com/signin") > -1 ||
//                    url.indexOf("https://run.pavlovia.org/OraClinical/fitted-screen-vanishing-optotypes") > -1
//            ) return false;
//
//            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//            activity.startActivity(intent);
//            return true;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return super.shouldOverrideUrlLoading(view, request);
        }

        @Override
        public void onScaleChanged(WebView view, float oldScale, float newScale) {
            super.onScaleChanged(view, oldScale, newScale);

            if (oldScale == 2.75 && isFinished)
                finish();

//            if (newScale > 2) {
//                view.setInitialScale(50);
                //view.zoomBy(.5f);
                //boolean z= view.zoomOut();
                //Log.i("Webview", Boolean.toString(z))   ;
           // }
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            isFinished = true;

            WindowManager manager = (WindowManager) view.getContext().getSystemService(Context.WINDOW_SERVICE);

            DisplayMetrics metrics = new DisplayMetrics();
            manager.getDefaultDisplay().getMetrics(metrics);

            metrics.widthPixels /= metrics.density;

            //view.loadUrl("javascript:var scale = " + metrics.widthPixels + " / document.body.scrollWidth; document.body.style.zoom = scale;");
            view.loadUrl("javascript:document.body.style.zoom = 1");
            //view.loadUrl("javascript:document.body.style.height = document.getElementById(\"page-main\").clientHeight + 400 + 'px';");
        }

        @Override
        public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
            return super.onRenderProcessGone(view, detail);
        }

        @Override
        public void onPageCommitVisible(WebView view, String url) {
            super.onPageCommitVisible(view, url);
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            Log.e("Webview", (String) error.getDescription());
            super.onReceivedError(view, request, error);
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            Log.e("Webview", errorResponse.getReasonPhrase());
            super.onReceivedHttpError(view, request, errorResponse);
        }

        @Override
        public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
            super.onUnhandledKeyEvent(view, event);
        }

        @Override
        public void onFormResubmission(WebView view, Message dontResend, Message resend) {
            super.onFormResubmission(view, dontResend, resend);
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);

            //view.evaluateJavascript("document.querySelector('meta[name=\"viewport\"]').setAttribute('content', 'width=100, height=100, initial-scale=.35, maximum-scale=.35, minimum-scale=.35, user-scalable=no');", null);
            //webView.loadDataWithBaseURL(null, "<style>img{display: inline;height: auto;max-width: 100%;}</style>" /*+ post.getContent()*/, "text/html", "UTF-8", null);


        }
    }

    @Override
    protected void onPause() {
        dailyLog.append("Diary onPause");

        ProtocolActivity.isAlarmed = false;
        super.onPause();
    }

    @Override
    protected void onStop() {
        dailyLog.append("Diary onStop");

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        dailyLog.append("Diary onDestroy");

        super.onDestroy();
    }
}