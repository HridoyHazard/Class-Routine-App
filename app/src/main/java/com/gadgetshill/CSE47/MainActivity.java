package com.gadgetshill.CSE47;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {

    private WebView mWebView;
    private RelativeLayout noInternetLayout;
    private Button btnNo;
    private ProgressDialog dialog;
    private static final String mURL = "https://hridoyhazard.github.io/cse47c/";
    private static final String OsVersion = Build.VERSION.RELEASE + "; ";
    private static final String DeviceModel = Build.MODEL+ " ";
    private static final String BuildVersion = Build.ID;
    private static final String UA = "Mozilla/5.0 " + "(" + "Linux; " + "Android " + OsVersion + DeviceModel + "Build/" +BuildVersion + ") " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/90.0.4430.72 Mobile Safari/537.36";

    @SuppressLint({"SetJavaScriptEnabled", "ObsoleteSdkInt"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dialog = new ProgressDialog(MainActivity.this);
        dialog.setTitle(null);
        dialog.setMessage("Loading..");
        dialog.setCancelable(true);
        dialog.setIndeterminate(true);
        dialog.show();

        mWebView = (WebView) findViewById(R.id.WV);
        noInternetLayout = (RelativeLayout) findViewById(R.id.noInternetLayout);
        btnNo = (Button) findViewById(R.id.btnNo);
        mWebView.setWebViewClient(new WebViewClient());

        mWebView.getSettings().setUserAgentString(UA);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.setBackgroundColor(0);
        mWebView.getSettings().setBuiltInZoomControls(false);
        mWebView.getSettings().setAppCachePath(getApplicationContext().getCacheDir().getPath());
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        mWebView.getSettings().setAllowFileAccess(false);
        mWebView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        checkinternet();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // chromium, enable hardware acceleration
            mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
        else {
            // older android version, disable hardware acceleration
            mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        mWebView.setWebViewClient(new WebViewClient()
        {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                dialog.dismiss();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                if (url.startsWith("http")){
                    return false;//open web links as usual
                }
                //try to find browse activity to handle uri
                Uri parsedUri = Uri.parse(url);
                PackageManager packageManager = getApplicationContext().getPackageManager();
                Intent browseIntent = new Intent(Intent.ACTION_VIEW).setData(parsedUri);
                if (browseIntent.resolveActivity(packageManager) != null) {
                    startActivity(browseIntent);
                    return true;
                }
                //if not activity found, try to parse intent://
                if (url.startsWith("intent:")) {
                    try {
                        Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        if (intent.resolveActivity(getApplicationContext().getPackageManager()) != null) {
                            startActivity(intent);
                            return true;
                        }
                        //try to find fallback url
                        String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                        if (fallbackUrl != null) {
                            mWebView.loadUrl(fallbackUrl);
                            return true;
                        }
                        //invite to install
                        Intent marketIntent = new Intent(Intent.ACTION_VIEW).setData(
                                Uri.parse("market://details?id=" + intent.getPackage()));
                        if (marketIntent.resolveActivity(packageManager) != null) {
                            startActivity(marketIntent);
                            return true;
                        }
                    } catch (URISyntaxException e) {
                        //not an intent uri
                    }
                }
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;//do nothing in other cases
            }

        });

        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkinternet();
            }
        });
    }

    public void checkinternet(){

        ConnectivityManager connectivityManager = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileNetwork = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);


        if(wifi.isConnected()){
            mWebView.loadUrl(mURL);
            mWebView.setVisibility(View.VISIBLE);
            noInternetLayout.setVisibility(View.GONE);


        }
        else if (mobileNetwork.isConnected()){
            mWebView.loadUrl(mURL);
            mWebView.setVisibility(View.VISIBLE);
            noInternetLayout.setVisibility(View.GONE);
        }
        else{
            dialog.dismiss();
            mWebView.setVisibility(View.GONE);
            noInternetLayout.setVisibility(View.VISIBLE);

        }


    }

    @Override
    public void onBackPressed() {
        if (dialog != null || dialog.isShowing())
            dialog.dismiss();
        if (mWebView.isFocused() && mWebView.canGoBack()) {
            mWebView.goBack();
        }

        AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
        builder1.setMessage("Are you sure you want to exit?");
        builder1.setCancelable(false);
        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        moveTaskToBack(true);
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(1);
                    }
                });

        builder1.setNegativeButton(
                "No!",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog Exit_Dialog = builder1.create();
        Exit_Dialog.show();
    }

}