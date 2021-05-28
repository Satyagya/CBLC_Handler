package com.example.webload;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.webload.androidFileSystem.CreatePath;
import com.example.webload.androidFileSystem.FileStructure;
import com.example.webload.androidFileSystem.JsonUpdate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class NewActivity extends AppCompatActivity {

    public static final String JAVASCRIPT_TO_DOWNLOAD_HTML = "javascript:HTMLOUT.processHTML(document.documentElement.innerHTML);";
    public static final String NET_ERR_NAME_NOT_RESOLVED = "net::ERR_NAME_NOT_RESOLVED";
    public static final String NET_ERR_CONNECTION_TIMED_OUT = "net::ERR_CONNECTION_TIMED_OUT";
    public static final String LINKEDIN_COM_IN = "linkedin.com/in/";
    public static final String DOWNLOAD_PATH = "/MyAlbums/Html/";

    String url;
    CreatePath createPath;
    TextView urlCountId;
    CookieManager cookieManager;
    Random random;
    FileStructure fileStructure;

    int urlCount;
    String modifiedURL;


    Button nextButton;
    int SHOULD_OVER_RIDE = 1;
    String PACKAGE_NAME;


    String[] pkg = new String[]{
            "com.android.browser",
            "com.android.chrome",
            "com.UCMobile.inti",
            "com.opera.mini.native",
            "com.brave.browser",
            "mobi.mgeek.TunnyBrowser",
            "com.duckduckgo.mobile.android",
            "com.ecosia.android",
            "org.mozilla.firefox",
            "com.kiwibrowser.browser",
            "com.google.android.gm",
            "com.yahoo.mobile.client.android.mail",
            "com.hubspot.android",
            "com.zelo.customer",
            "ch.protonmail.android",
            "com.microsoft.office.outlook",
            "ru.yandex.mail"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);

        int i = new Random().nextInt(pkg.length);
        Log.i("package Name", pkg[i]);
        PACKAGE_NAME = pkg[i];
        SHOULD_OVER_RIDE = 0;

        random = new Random();
        fileStructure = JsonUpdate.getJson();
        createPath = new CreatePath();
        WebView webView = findViewById(R.id.myWebView);
        urlCountId = findViewById(R.id.urlCountId);
        nextButton = findViewById(R.id.next);
        clearCookies(getApplicationContext());
        Intent intent = getIntent();
        url = intent.getStringExtra("url");
        urlCount = intent.getIntExtra("urlCount", 0);
        final String referrer = readContent("/config/", "referrer.txt");
        final Map<String, String> extraHeaders = new HashMap<>();
        extraHeaders.put("Referer", referrer);
        Log.i("Current Referrer : ", referrer);
        String countToDisplay = urlCount + "/" + fileStructure.getProfile().size();
        urlCountId.setText(countToDisplay);


        if (!isConnected(getApplicationContext())) {

            Log.i("Network Status :", "You are offline");
            createFlightModeFolder("flight1.txt");
            goToMainWithWait(10);
        }

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        String userAgentRead = readContent("/", "useragent.txt");
        webView.getSettings().setUserAgentString(userAgentRead);
        Log.i("Current User Agent : ", userAgentRead);
        webView.addJavascriptInterface(new NewActivity.MyJavaScriptInterface(), "HTMLOUT");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.i("Host", Uri.parse(url).getHost());
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.i("On Page Start :", "Page Load Started");

            }

            public void onPageFinished(WebView view, String url) {
                view.loadUrl(JAVASCRIPT_TO_DOWNLOAD_HTML);
                Log.i("Page Lod Status :", "Finished");
                view.clearCache(true);

            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                Log.e("ErrorReceives", String.valueOf(error));


            }

            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(NewActivity.this, description, Toast.LENGTH_SHORT).show();
                Log.i("Error Description :", description);
                if (description.contains(NET_ERR_NAME_NOT_RESOLVED) || description.contains(NET_ERR_CONNECTION_TIMED_OUT)) {
                    fileStructure.setUrlCount(++urlCount);
                    JsonUpdate.setJson(fileStructure);
                }
                goToMainWithWait(3);
            }

            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                request.getRequestHeaders().put("DNT", "1");
                Log.i("RequestHeader For App: ", request.getRequestHeaders().toString());
                return super.shouldInterceptRequest(view, request);
            }
        });


        clearCookies(getApplicationContext());
        modifiedURL = finalURL(url);
        Log.i("URL TO Load :", modifiedURL);
        webView.loadUrl(modifiedURL, extraHeaders);


        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                fileStructure.setUrlCount(++urlCount);
                JsonUpdate.setJson(fileStructure);
                goToMainWithWait(1);

            }
        });


    }

    private class MyJavaScriptInterface {


        public static final String TRACKING_CODE_FROM_COOKIE = "Parse the tracking code from cookie";
        public static final String BODY_BODY = "<body></body>";
        public static final String VAR_SUBDOMAIN_INDEX_LOCATION_HOST_INDEX_OF_LINKEDIN = "var subdomainIndex = location.host.indexOf(\".linkedin\");";

        @SuppressWarnings("unused")
        @JavascriptInterface
        public void processHTML(final String html) {
            // process the html as needed by the app

            Log.i("Html Content :", html);

            String emptyBody = BODY_BODY;
            if (html.contains(TRACKING_CODE_FROM_COOKIE + "s") || html.contains(emptyBody) || html.contains(VAR_SUBDOMAIN_INDEX_LOCATION_HOST_INDEX_OF_LINKEDIN)) {
                Toast.makeText(NewActivity.this, "Sorry , you are caught", Toast.LENGTH_LONG).show();

                String[] repeatCount = fileStructure.getSleepCount().split("_");
                int repeatedUrl = Integer.parseInt(repeatCount[0]);
                int currentRepeatedUrl = Integer.parseInt(repeatCount[1]);
                int totalRepeatedUrl = Integer.parseInt(repeatCount[2]);
                currentRepeatedUrl++;
                totalRepeatedUrl++;
                String sleepUpdate = repeatedUrl + "_" + currentRepeatedUrl + "_" + totalRepeatedUrl;
                fileStructure.setSleepCount(sleepUpdate);
                JsonUpdate.setJson(fileStructure);
                createFlightModeFolder("flight.txt");
                goToMainWithWait(10);


            } else {


                String htmlFile = DeviceId.deviceId(getApplicationContext()) + "_" + fileStructure.getRequestId();
                String[] fileName = modifiedURL.split(LINKEDIN_COM_IN);
                File path = createPath.createFilePath(DOWNLOAD_PATH + htmlFile);
                File file = new File(path, fileName[1].replace("/", "") + ".html");
                FileOutputStream fout;
                OutputStreamWriter myOutWriter;
                if (!file.exists()) {
                    try {
                        boolean newFile = file.createNewFile();
                        Log.i("Download File Created :", String.valueOf(newFile));
                        fout = new FileOutputStream(file);
                        myOutWriter = new OutputStreamWriter(fout);
                        myOutWriter.append(html);
                        myOutWriter.close();
                        fout.close();
                        fout.flush();

                    } catch (Exception e) {
                        Log.e("Download Error : ", Arrays.toString(e.getStackTrace()));
                    }
                }


                ++urlCount;
                String sleepUpdate = urlCount + "_0_0";
                fileStructure.setSleepCount(sleepUpdate);
                fileStructure.setUrlCount(urlCount);
                JsonUpdate.setJson(fileStructure);
                int i = random.nextInt(3) + 2;
                goToMainWithWait(i);


            }


        }
    }

    public void createFlightModeFolder(String fileName) {

        File file;
        File path = new CreatePath().createFilePath("/MyAlbums/FlightMode/");
        file = new File(path, fileName);

        if (!file.exists()) {


            try {
                boolean newFile = file.createNewFile();
                Log.i("flight.txt Created :", String.valueOf(newFile));
            } catch (IOException e) {
                Log.i("File Not Created : ", e.toString());
            }
        }
    }

    public void goToMainWithWait(int i) {

        SHOULD_OVER_RIDE = 1;
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent1 = new Intent(NewActivity.this, MainActivity.class);
                startActivity(intent1);
                finish();

            }
        }, i * 1000);

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

    public String readContent(String dir, String fileName) {

        File file;
        File path = createPath.createFilePath(dir);
        file = new File(path, fileName);
        StringBuilder count = null;
        FileInputStream fin;
        try {
            fin = new FileInputStream(file);
            InputStreamReader myInputReader = new InputStreamReader(fin);
            int i;
            count = new StringBuilder();
            while ((i = myInputReader.read()) != -1) {
                count.append((char) i);
            }
            myInputReader.close();
            fin.close();
        } catch (Exception e) {
            Log.e("File Not Found", String.valueOf(e));
        }
        return count.toString();
    }

    /**
     * Used to override package name for external calls
     */
    @Override
    public String getPackageName() {
        if (SHOULD_OVER_RIDE == 1) {
            Log.i("package Name", "By Intent");
            return super.getPackageName();
        } else {

            return PACKAGE_NAME;
        }

    }

    /**
     * Function to check url pattern and modify in browsable format
     */

    String finalURL(String unmodifiedURL) {
        String finalUrl = "";
        if (unmodifiedURL.contains("/url?")) {
            int from = unmodifiedURL.indexOf("https");
            int end = unmodifiedURL.indexOf('&', from);
            finalUrl = unmodifiedURL.substring(from, end);
        } else if (unmodifiedURL.contains("search.lycos.com")) {
            String[] localArray = unmodifiedURL.split("http://");
            finalUrl = "https://" + localArray[localArray.length - 1];
        } else if (unmodifiedURL.contains("http://")) {
            finalUrl = unmodifiedURL.replace("http:", "https:");
        } else {

            String[] localArray = unmodifiedURL.split("/");
            if (localArray[0].contains("linkedin.com")) {
                finalUrl = "https://" + unmodifiedURL;
            } else if (localArray[0].equals("https:")) {
                finalUrl = unmodifiedURL;
            }
        }

        if (finalUrl.equals("")) {
            Log.e("Illegal Url Formate :", "This url Pattern does not exist : " + unmodifiedURL);
            finalUrl = unmodifiedURL;
        }

        return finalUrl;
    }
}
