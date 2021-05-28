package com.example.webload;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.webload.androidFileSystem.CountUpdate;
import com.example.webload.androidFileSystem.CreatePath;
import com.example.webload.androidFileSystem.RequestIdUpdate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class NewActivity extends AppCompatActivity {

    private String html_;
    String url;
    CreatePath createPath;
    CountUpdate countUpdate;
    RequestIdUpdate requestIdUpdate;
    TextView urlCountId;
    CookieManager cookieManager;
    Random random;

    String urlRepeat;
    int urlRepeatCount;

    Button nextButton ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);




        random = new Random();
        createPath = new CreatePath();
        countUpdate = new CountUpdate();
        requestIdUpdate = new RequestIdUpdate();
        WebView webView = findViewById(R.id.myWebView);
        urlCountId = findViewById(R.id.urlCountId);

        nextButton = findViewById(R.id.next);


        String referrer = readSleep("/config/","referrer.txt");
        System.out.println("New Referer Set : "+referrer);
        Map<String, String> extraHeaders = new HashMap<String, String>();
        extraHeaders.put("Referer", referrer);
        Log.i("Current Referrer : ",referrer);

        String countToDisplay = MainActivity.count + "/" + MainActivity.length;
        urlCountId.setText(countToDisplay);

        if (!isConnected(getApplicationContext())) {

            Log.i("Network Status :", "You are offline");
            countUpdate.updateCount(--MainActivity.count);
            createFlightModeFolder("flight1.txt");
            goToMainWithWait(10);
        }

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        String userAgentRead = readSleep("/", "useragent.txt");
        System.out.println("Read Successfully : " + userAgentRead);
//        System.out.println("Read UserAgent Before Change "+"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/600.2.5 (KHTML, like Gecko) Version/8.0.2 Safari/600.2.5");
        webView.getSettings().setUserAgentString(userAgentRead);
        Log.i("Current User Agent : ",userAgentRead);
        webView.addJavascriptInterface(new NewActivity.MyJavaScriptInterface(), "HTMLOUT");
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
                System.out.println("Error received" + error);
                Log.e("ErrorReceives", String.valueOf(error));


            }

            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(NewActivity.this, description, Toast.LENGTH_SHORT).show();
                System.out.println("Error Description :" + description);
                if(description.contains("net::ERR_NAME_NOT_RESOLVED") || description.contains("net::ERR_CONNECTION_TIMED_OUT"))
                    goToMainWithWait(3);
            }

            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                request.getRequestHeaders(); // returns a map of request headers
                System.out.println("RequestHeader For App:"+ request.getRequestHeaders());
                Log.i("RequestHeader For App: ", request.getRequestHeaders().toString());
                return super.shouldInterceptRequest(view, request);
            }
        });

        Intent intent = getIntent();
        url = intent.getStringExtra("url");
        urlRepeatCount = intent.getIntExtra("urlRepeatCount", 0);
        System.out.println("REPEAT COUNT" + urlRepeatCount);
        System.out.println(url);
        String status = intent.getStringExtra("status");


        clearCookies(getApplicationContext());
        webView.loadUrl(url,extraHeaders);
//        webView.loadUrl(url);



        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent1 = new Intent(NewActivity.this, MainActivity.class);
                startActivity(intent1);
                finish();

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

            System.out.println(html_);

            String emptyBody = "<body></body>";
            if (html_.contains("Parse the tracking code from cookies") || html_.contains(emptyBody)||html_.contains("var subdomainIndex = location.host.indexOf(\".linkedin\");")) {
                Toast.makeText(NewActivity.this, "Sorry , you are caught", Toast.LENGTH_LONG).show();

                countUpdate.updateCount(--MainActivity.count);

                String readSleep = readSleep("/MyAlbums/sleep","sleep.txt");
                String[] repeatCount = readSleep.split("_");
                int repeatedUrl = Integer.parseInt(repeatCount[0]);
                if(repeatedUrl == MainActivity.count){
                    int repeat = Integer.parseInt(repeatCount[1])+1 ;
                    System.out.println(repeat);
                    String sleepUpdate = repeatedUrl + "_"+repeat;
                    System.out.println("Sleep State " + sleepUpdate);
                    updateSleep(sleepUpdate ,"/MyAlbums/sleep","sleep.txt");
                }
                else
                {
                    String sleepUpdate = MainActivity.count + "_"+0;
                    updateSleep(sleepUpdate ,"/MyAlbums/sleep","sleep.txt");
                }





                createFlightModeFolder("flight.txt");
                goToMainWithWait(8);


            } else {


                String htmlFile = DeviceId.deviceId(getApplicationContext())+"_"+requestIdUpdate.getRequestId();
                String fileName[] = url.split("linkedin.com/in/");
                File path = createPath.createFilePath("/MyAlbums/Html/"+htmlFile);
                File file = new File(path, fileName[1].replace("/", "") + ".html");
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                FileOutputStream fOut = null;
                try {
                    fOut = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                try {
                    myOutWriter.append(html);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    myOutWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    fOut.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    fOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                int i = random.nextInt(3) + 2;
                goToMainWithWait(i);


            }


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

    public void goToMainWithWait(int i) {

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

    public void selfStart(final int urlRepeatCount) {

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = getIntent();
                finish();
                intent.putExtra("urlRepeatCount", urlRepeatCount);
                startActivity(intent);

            }
        }, 5 * 1000);
    }


    public void updateSleep(String urlCount , String directory , String fileName ){
        File file;
        File path = createPath.createFilePath(directory);
        if (!path.exists()) {
            boolean status = path.mkdirs();
            Log.i("URLCount Path Created :", String.valueOf(status));
        }
        file = new File(path, fileName);

        if (!file.exists()) {
            try {
                boolean status = file.createNewFile();
                Log.i("urlCount File Created :", String.valueOf(status));
            } catch (IOException e) {
                Log.e("Create URL File :", String.valueOf(e));
            }
        }
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file);
            try (OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut)) {
                myOutWriter.write(urlCount);
            }

            fOut.flush();
            fOut.close();
        } catch (FileNotFoundException e) {
            Log.e("File Not Found :", e.toString());
        } catch (IOException e) {
            Log.e("IOException :", e.toString());
        }

    }
    public String readSleep(String dir , String fileName) {

        File file;
        File path = createPath.createFilePath(dir);
        file = new File(path, fileName);
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            Log.e("File Not Found", String.valueOf(e));
        }
        StringBuilder count = null;
        try (InputStreamReader myInputReader = new InputStreamReader(fin)) {
            int i;
            count = new StringBuilder();
            while ((i = myInputReader.read()) != -1) {
                count.append((char) i);
            }
        } catch (IOException e) {
            Log.e("IO Exception :", e.toString());
        }
        try {
            fin.close();
        } catch (IOException e) {
            Log.e("IOException :", e.toString());
        }

        return count.toString();
    }

}
