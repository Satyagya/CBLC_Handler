package com.example.webload;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.webload.androidFileSystem.CreatePath;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.example.webload.NewActivity.isConnected;

public class TestNew extends AppCompatActivity {

    private String html_;
    String url;
    CreatePath createPath;
    Random random;
    TextView urlCountId;
    Button nextButton ;

    WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_new);

        if (!isConnected(getApplicationContext())) {

            Log.i("Network Status :", "You are offline");
            --TestMain.count;
            goToMainWithWait(5);
        }

        Map<String, String> extraHeaders = new HashMap<String, String>();
        extraHeaders.put("Referer", "https://duckduckgo.com/");


        Intent intent = getIntent();
        url = intent.getStringExtra("url");
        System.out.println("New Urls : "+url);
        createPath = new CreatePath();
        random = new Random();

        urlCountId = findViewById(R.id.urlCountId);
        nextButton = findViewById(R.id.next);
        WebView webView = findViewById(R.id.myWebView);

        String countToDisplay = TestMain.count + "/" + TestMain.arr.length;
        urlCountId.setText(countToDisplay);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.addJavascriptInterface(new TestNew.MyJavaScriptInterface(), "HTMLOUT");
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

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                super.onReceivedSslError(view, handler, error);
                handler.proceed();
            }

            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(TestNew.this, description, Toast.LENGTH_SHORT).show();
                System.out.println("Error Description :" + description);
                if(description.contains("net::ERR_NAME_NOT_RESOLVED") || description.contains("net::ERR_CONNECTION_TIMED_OUT"))
                    goToMainWithWait(3);
            }

            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                request.getRequestHeaders(); // returns a map of request headers
                System.out.println("RequestHeader For App:"+ request.getRequestHeaders());
                return super.shouldInterceptRequest(view, request);
            }
        });


        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent1 = new Intent(TestNew.this, TestMain.class);
                startActivity(intent1);
                finish();

            }
        });

//        webView.getSettings().setUserAgentString("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36");
//        webView.loadUrl(url,extraHeaders);

        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        webView.loadUrl(url);

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
                Toast.makeText(TestNew.this, "Sorry , you are caught", Toast.LENGTH_LONG).show();
                --TestMain.count;
                goToMainWithWait(8);


            } else {

                String fileName[] = url.split("linkedin.com/in/");
                File path = createPath.createFilePath("/TestMyAlbums/Html/ProfileHtml");
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
    public void goToMainWithWait(int i) {

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent1 = new Intent(TestNew.this, TestMain.class);
                startActivity(intent1);
                finish();

            }
        }, i * 1000);


    }
}
