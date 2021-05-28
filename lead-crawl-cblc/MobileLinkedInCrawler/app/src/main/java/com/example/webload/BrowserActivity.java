package com.example.webload;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.webload.androidFileSystem.CountUpdate;
import com.example.webload.androidFileSystem.CreatePath;
import com.example.webload.androidFileSystem.RequestIdUpdate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;


import static com.example.webload.constants.BrowserConstants.EMPTY_HTML_BODY;
import static com.example.webload.constants.BrowserConstants.LINKEDIN_PROFILE_COMPANY_URL_EXTRACT_CODE;
import static com.example.webload.constants.BrowserConstants.LINKEDIN_TRACK_CODE;

public class BrowserActivity extends AppCompatActivity {

    static String urlRepeat = "";
    static int urlRepeatCount = 0;

    private Context context;
    private Context mContext;
    private CookieManager cookieManager;
    private String status;
    private String url;
    private TextView urlCountId;
    private WebView webView;
    private String html_;

    CountUpdate countUpdate;
    RequestIdUpdate requestIdUpdate;
    CreatePath createPath ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        countUpdate = new CountUpdate();
        requestIdUpdate = new RequestIdUpdate();
        createPath = new CreatePath();

        Button next = findViewById(R.id.next);
        webView = findViewById(R.id.myWebView);
        urlCountId = findViewById(R.id.urlCountId);
        Button flightMode = findViewById(R.id.flightModeToggle);

        //setting the count of url crawled
        String countToDisplay = MainActivity.count + "/" + MainActivity.length;
        urlCountId.setText(countToDisplay);

        context = getApplicationContext();
        mContext = getBaseContext();

        //fetching the url from main Activity
        Intent intent = getIntent();
        url = intent.getStringExtra("url");
        status = intent.getStringExtra("status");

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.addJavascriptInterface(new BrowserActivity.MyJavaScriptInterface(), "HTMLOUT");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url,
                                      Bitmap favicon) {

            }

            public void onPageFinished(WebView view, String url) {
                view.loadUrl("javascript:HTMLOUT.processHTML(document.documentElement.innerHTML);");
                System.out.println("===========>onPageFinished");

            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
//                Toast.makeText(BrowserActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                System.out.println("Error received" + error);
                Log.e("ErrorReceives", String.valueOf(error));


            }

            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(BrowserActivity.this, description, Toast.LENGTH_SHORT).show();
                System.out.println("Error Description :" + description);
            }


        });
//Make sure no cookies created
        CookieManager.getInstance().setAcceptCookie(false);
// Make sure no caching is done
        WebStorage.getInstance().deleteAllData();
        webView.clearHistory();
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.getSettings().setAppCacheEnabled(false);
        webView.clearCache(true);
        clearCookies(context);

        if (!isConnected(getApplicationContext())) {

            Log.i("Network Status :", "You are offline");
            countUpdate.updateCount(--MainActivity.count);
            createFlightModeFolder("flight1.txt");
            goToMainWithWait(10);
        }


        webView.getSettings().setUserAgentString("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.87 Safari/537.36");
        webView.loadUrl(url);


//
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(BrowserActivity.this, MainActivity.class);
                startActivity(intent1);

            }
        });

        flightMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                clearCookies(context);
                toggleFlightMode();

            }
        });

    }


    private class MyJavaScriptInterface {
        @SuppressWarnings("unused")
        @JavascriptInterface
        public void processHTML(final String html) {
            // process the html as needed by the app
            System.out.println("<<<<============>>>>>processHtml");
            html_ = html;

            System.out.println(html);
            if (html_.contains("Parse the tracking code from cookies") || html_.equals("<head></head><body></body>")) {
                Toast.makeText(BrowserActivity.this, "Sorry , you are caught", Toast.LENGTH_LONG).show();

//logic for crawling an url maximum 5 times and toggle flight mode


            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 27);
            }
            //If runtime permission ia already granted by the user.
            else {
                MyAsyncTask task = new MyAsyncTask();
                task.execute();
            }

        }
    }

    /**
     * AsyncTAsk to do work in background
     */
    public class MyAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                writeToFile(html_, mContext);
            } catch (IOException e) {
                Log.e("HTML Write", e.toString());
            }
            return null;
        }
    }

    /**
     * @param html
     * @param context
     * @throws IOException write to external storage ===>DCIM in phone
     *                     edit the location in your phone-
     */
    public void writeToFile(String html, Context context) throws IOException {

        if (!html.contains(LINKEDIN_TRACK_CODE) & !html.equals(EMPTY_HTML_BODY)) {
            File file;
            int requestId = requestIdUpdate.getRequestId();
            File path;
            if (status.equals("ProfileCrawl")) {
                String directory = "/MyAlbums/Html/ProfileHtml" + "_" + requestId + "/";
                path =
                        Environment.getExternalStoragePublicDirectory
                                (
                                        //Environment.DIRECTORY_PICTURES
                                        Environment.DIRECTORY_DCIM + directory
                                );

            } else {
                String directory = "/MyAlbums/Html/CompanyHtml" + "_" + requestId + "/";
                path =
                        Environment.getExternalStoragePublicDirectory
                                (
                                        //Environment.DIRECTORY_PICTURES
                                        Environment.DIRECTORY_DCIM + directory
                                );
            }
            if (!path.exists()) {
                // Make it, if it doesn't exit
                boolean mkdirs = path.mkdirs();
                Log.i("Company Path Created :", String.valueOf(mkdirs));
            }
            //filename for the new file


            /**
             * *code to save company name and href
             */
            if (status.equals("ProfileCrawl")) {
                getCompany(html);
                String fileName[] = url.split("linkedin.com/in/");
                file = new File(path, fileName[1].replace("/", "") + ".html");
            } else
                file = new File(path, url.substring(url.indexOf("company/") + 8, url.indexOf("?")) + ".html");


            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(html);

            myOutWriter.close();

            fOut.flush();
            fOut.close();
            goToMain();

        } else {
            System.out.println("no downloaded data");
        }
    }

    /**
     * @param requestCode
     * @param permissions
     * @param grantResults when permission is granted by the user.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case 27:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    MyAsyncTask task = new MyAsyncTask();
                    task.execute();

                } else {
                    Toast.makeText(mContext, "Permission is not granted", Toast.LENGTH_SHORT).show();
                }
        }
    }


    public void clearCookies(Context context) {

        CookieSyncManager.createInstance(context);
        cookieManager = CookieManager.getInstance();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.removeAllCookies(null);
            cookieManager.removeSessionCookies(null);
        } else {
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
        }


    }

    public void toggleFlightMode() {


        // Workin code to toggle flight mode
//        try {
//            Process su = Runtime.getRuntime().exec("su");
//            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
//
//            outputStream.writeBytes("settings put global airplane_mode_on 1 &&am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true\n");
//            outputStream.flush();
//            outputStream.writeBytes("settings put global airplane_mode_on 0 &&am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false\n");
//            outputStream.flush();
//
//
//            outputStream.writeBytes("exit\n");
//            outputStream.flush();
//            su.waitFor();
//        } catch (IOException | InterruptedException e) {
//            Log.e("Flight Mode Error",e.toString());
//        }


        createFlightModeFolder("flight.txt");


    }


    public void goToMainWithWait(int i) {

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent1 = new Intent(BrowserActivity.this, MainActivity.class);
                startActivity(intent1);

            }
        }, i * 1000);

    }

    public void goToMain() {
        Intent intent = new Intent(BrowserActivity.this, MainActivity.class);
        startActivity(intent);
    }


    public void getCompany(String profileHtml) {

        int index1 = profileHtml.indexOf(LINKEDIN_PROFILE_COMPANY_URL_EXTRACT_CODE);
        String company1 = "";
        String demo1 = LINKEDIN_PROFILE_COMPANY_URL_EXTRACT_CODE;
        char c1;
        for (int m = 0; (c1 = profileHtml.charAt(index1 + demo1.length() + m)) != '\"'; m++) {
            company1 = company1 + c1;
        }


        if (index1 != -1) {
            System.out.println(company1 + "\n");
            updateCompany(company1 + "\n");
        }


    }


    public void updateCompany(String txt) {
        File file;
        final File path =
                Environment.getExternalStoragePublicDirectory
                        (
                                Environment.DIRECTORY_DCIM + "/MyAlbums/Company"
                        );

        if (!path.exists()) {
            path.mkdirs();
        }

        file = new File(path, "company.txt");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file, true);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(txt);

            myOutWriter.close();

            fOut.flush();
            fOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private File createFile(String fileName) {
        File path =
                Environment.getExternalStoragePublicDirectory
                        (
                                //Environment.DIRECTORY_PICTURES
                                Environment.DIRECTORY_DCIM + fileName
                        );
        if (!path.exists()) {
            path.mkdirs();
        }
        return path;
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null != cm) {
            NetworkInfo info = cm.getActiveNetworkInfo();
            return (info != null && info.isConnected());
        }
        return false;
    }

    public void createFlightModeFolder(String fileName) {

        File file;
        File path = createFile("/MyAlbums/FlightMode/");
        file = new File(path, fileName);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Log.i("File Not Created : ", e.toString());
            }
        }
    }
}


