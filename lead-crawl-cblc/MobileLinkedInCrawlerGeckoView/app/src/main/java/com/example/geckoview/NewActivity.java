package com.example.geckoview;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.geckoview.androidFileSystem.CreatePath;
import com.example.geckoview.androidFileSystem.FileStructure;
import com.example.geckoview.androidFileSystem.JsonUpdate;

import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.geckoview.ContentBlocking;
import org.mozilla.geckoview.GeckoResult;
import org.mozilla.geckoview.GeckoRuntime;

import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoView;
import org.mozilla.geckoview.StorageController;
import org.mozilla.geckoview.WebExtension;
import org.mozilla.geckoview.WebRequestError;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


public class NewActivity extends AppCompatActivity {

    public static final String PROFILE_NOT_FOUND = "Profile Not Found";
    public static final String EMPTY_PAGE = "<body></body>";
    public static final String LINKEDIN_COM_IN = "linkedin.com/in/";
    public static final String DOWNLOAD_PATH = "/MyAlbums/Html/";
    private static final String EXTENSION_LOCATION = "resource://android/assets/messaging/";
    private static final String EXTENSION_ID = "messaging@example.com";
    public static final String LINKEDIN_PROFILE_COMPANY_URL_EXTRACT_CODE = "var subdomainIndex = location.host.indexOf(\".linkedin\");";
    public static final String CONTENT_SIGN_UP = "content=\"Sign Up";
    public static final String SECURITY_VERIFICATION = "<title>Security Verification";
    String modifiedURL;
    private GeckoRuntime sRuntime;
    FileStructure fileStructure;
    String url;
    CreatePath createPath;
    TextView urlCountId;
    Random random;
    int urlCount;

    Button nextButton;
    int shouldOverRide = 1;
    String packageName;
    String userAgent;

    String[] pkg = new String[]{"com.android.browser", "com.android.chrome", "com.UCMobile.inti", "com.opera.mini.native", "com.brave.browser",
            "mobi.mgeek.TunnyBrowser", "com.duckduckgo.mobile.android", "com.ecosia.android", "org.mozilla.firefox", "com.kiwibrowser.browser", "com.google.android.gm", "com.yahoo.mobile.client.android.mail", "com.hubspot.android", "com.zelo.customer", "ch.protonmail.android", "com.microsoft.office.outlook", "ru.yandex.mail"
    };

    String[] ua = new String[]{
            "Mozilla/5.0 (Windows NT 5.1; rv:7.0.1) Gecko/20100101 Firefox/7.0.1", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:54.0) Gecko/20100101 Firefox/54.0", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:18.0) Gecko/20100101 Firefox/18.0", "Mozilla/5.0 (Windows NT 5.1; rv:36.0) Gecko/20100101 Firefox/36.0", "Mozilla/5.0 (Windows NT 5.1; rv:33.0) Gecko/20100101 Firefox/33.0", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:66.0) Gecko/20100101 Firefox/66.0", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:50.0) Gecko/20100101 Firefox/50.0", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:52.0) Gecko/20100101 Firefox/52.0", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:67.0) Gecko/20100101 Firefox/67.0", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:50.0) Gecko/20100101 Firefox/50.0", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:61.0) Gecko/20100101 Firefox/61.0", "Mozilla/5.0 (X11; Ubuntu; Linux i686; rv:24.0) Gecko/20100101 Firefox/24.0", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:43.0) Gecko/20100101 Firefox/43.0", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7.12) Gecko/20050915 Firefox/1.0.7",
            "Mozilla/5.0 (Windows NT 6.3; Win64; x64; rv:73.0) Gecko/20100101 Firefox/73.0", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:54.0) Gecko/20100101 Firefox/54.0", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:52.0) Gecko/20100101 Firefox/52.0", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:65.0) Gecko/20100101 Firefox/65.0", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:57.0) Gecko/20100101 Firefox/57.0", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:57.0) Gecko/20100101 Firefox/57.0", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:61.0) Gecko/20100101 Firefox/61.0",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:60.0) Gecko/20100101 Firefox/60.0", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:59.0) Gecko/20100101 Firefox/59.0", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:47.0) Gecko/20100101 Firefox/47.0", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:62.0) Gecko/20100101 Firefox/62.0", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:58.0) Gecko/20100101 Firefox/58.0", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:57.0) Gecko/20100101 Firefox/57.0", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:79.0) Gecko/20100101 Firefox/79.0", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:73.0) Gecko/20100101 Firefox/73.0"
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);

        int i = new Random().nextInt(pkg.length);
        Log.i("package Name", pkg[i]);
        packageName = pkg[i];

        int j = new Random().nextInt(ua.length);
        userAgent = ua[j];

        shouldOverRide = 0;

        random = new Random();
        fileStructure = JsonUpdate.getJson();
        createPath = new CreatePath();
        GeckoView geckoView = findViewById(R.id.geckoview);
        GeckoSession session = new GeckoSession();
        urlCountId = findViewById(R.id.urlCountId);
        nextButton = findViewById(R.id.next);

        Intent intent = getIntent();
        url = intent.getStringExtra("url");
        urlCount = intent.getIntExtra("urlCount", 0);

        /**
         * just read referer from locals but not sending
         */

        String referrer = readContent("/config/", "referrer.txt");
        Map<String, String> extraHeaders = new HashMap<>();
        extraHeaders.put("Referer", referrer);
        Log.i("Current Referrer : ", referrer);

        String countToDisplay = urlCount + "/" + fileStructure.getProfile().size();
        urlCountId.setText(countToDisplay);

        if (!isConnected(getApplicationContext())) {

            Log.i("Network Status :", "You are offline");
            createFlightModeFolder("flight1.txt");
            goToMainWithWait(10);
        }


        sRuntime = GeckoRuntime.getDefault(this);
        sRuntime.getSettings().getContentBlocking().setSafeBrowsing(ContentBlocking.SafeBrowsing.DEFAULT);
        sRuntime.getSettings().getContentBlocking().setEnhancedTrackingProtectionLevel(ContentBlocking.EtpLevel.STRICT);

        WebExtension.MessageDelegate messageDelegate = new WebExtension.MessageDelegate() {
            @Nullable
            @Override
            public GeckoResult<Object> onMessage(final @NonNull String nativeApp,
                                                 final @NonNull Object message,
                                                 final @NonNull WebExtension.MessageSender sender) {
                if (message instanceof JSONObject) {
                    JSONObject json = (JSONObject) message;

                    String s = "";

                    Log.i("Got Message :", json.toString());
                    try {
                        s = String.valueOf(json.get("manifest"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Log.i("Download Data", s);
                    writeHTML(s);
                }
                return null;
            }
        };

        sRuntime.getWebExtensionController()
                .ensureBuiltIn(EXTENSION_LOCATION, EXTENSION_ID).accept(
                // Set delegate that will receive messages coming from this extension.
                extension -> session.getWebExtensionController()
                        .setMessageDelegate(extension, messageDelegate, "browser"),
                // Something bad happened, let's log an error
                e -> Log.e("MessageDelegate", "Error registering extension", e)
        );

        session.setNavigationDelegate(new GeckoSession.NavigationDelegate() {
            @Nullable
            @Override
            public GeckoResult<String> onLoadError(@NonNull GeckoSession geckoSession, @Nullable String s, @NonNull WebRequestError webRequestError) {
                if (webRequestError.code == 35) {

                    Log.e("LoadErrorCode: ", String.valueOf(webRequestError.code));
                    Log.e("LoadErrorCategory: ", String.valueOf(webRequestError.category));
                    Log.e("LoadErrorcertificate:", String.valueOf(webRequestError.certificate));

                    int i = random.nextInt(3) + 2;
                    goToMainWithWait(i);
                }
                return null;
            }

        });
        StorageController storageController = new StorageController();
        storageController.clearData(StorageController.ClearFlags.COOKIES);
        storageController.clearData(StorageController.ClearFlags.AUTH_SESSIONS);
        session.getSettings().setUseTrackingProtection(true);
        session.getSettings().setAllowJavascript(true);

        // reading user agent from locals
        String userAgentRead = readContent("/", "useragent.txt");

        // setting userAgent from above array but can be set as useragent from locals
        Log.i("Current User Agent : ", userAgentRead);
        session.getSettings().setUserAgentOverride(userAgent);
        session.open(sRuntime);
        geckoView.setSession(session);
        modifiedURL = finalURL(url);
        session.loadUri(modifiedURL);
        Log.i("URL TO Load", modifiedURL);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                fileStructure.setUrlCount(++urlCount);
                JsonUpdate.setJson(fileStructure);
                goToMainWithWait(1);

            }
        });


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

        shouldOverRide = 1;
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

    @Override
    public String getPackageName() {
        if (shouldOverRide == 1) {
            Log.i("package Name", "By Intent");
            return super.getPackageName();
        } else {

            return packageName;
        }

    }

    private void writeHTML(String html) {

        Log.i("Page Content : ", html);

        if (html.contains("Parse the tracking code from cookies") || html.contains(EMPTY_PAGE) || html.contains(LINKEDIN_PROFILE_COMPANY_URL_EXTRACT_CODE) || html.contains(CONTENT_SIGN_UP) || html.contains(SECURITY_VERIFICATION)) {
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

            if (!html.contains(PROFILE_NOT_FOUND) && !html.contains(EMPTY_PAGE)) {
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

    /**
     * Function to check url pattern and modify in browsable formate
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
