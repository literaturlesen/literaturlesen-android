package de.dlamarbach.literaturlesen;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.ClipboardManager;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private WebView surfer;
    private Button bildBtn;
    public String TAG = "literaturlesen";
    private int requestCode = 0;
    private int notcounter = 0;
    public static String previousUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        MainActivity.previousUrl = "https://app.literaturlesen.com";

        if (getIntent() != null) {

            String intentData = getIntent().getDataString();
            if (intentData != null) {
                if (intentData.contains("app.literaturlesen.com")) {
                    MainActivity.previousUrl = intentData;
                }
            }

        }


        //Zurück-Button für externe Quellen
        this.bildBtn = (Button) findViewById(R.id.zurueckbutton);


        //Intiting Webview
        surfer = (WebView) findViewById(R.id.webview);
        surfer.setInitialScale(1);
        WebSettings settings = surfer.getSettings();
        settings.setJavaScriptEnabled(true);

        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setDomStorageEnabled(true);
        surfer.addJavascriptInterface(new WebAppInterface(this), "AndroidInterface");
        surfer.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        surfer.setScrollbarFadingEnabled(false);
        surfer.clearCache(false);

        surfer.setWebViewClient(
                new WebViewClient() {

                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url)
                    {

                        if (url.contains("literaturlesen.com")) {
                            MainActivity.previousUrl = url;
                        }
                        if (url.startsWith("tel:") || url.startsWith("https://api.whatsapp") || url.startsWith("https://twitter.com")) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(url));
                            try {
                                startActivity(intent);
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(MainActivity.this, "Sie besitzen keine App zum öffnen.", Toast.LENGTH_SHORT).show();
                            }
                            view.goBack();
                            return true;
                        } else if (url.contains("share://")) {
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_SEND);
                            String text = url.replace("share://", "");
                            intent.putExtra(Intent.EXTRA_TEXT, text);
                            intent.setType("text/plain");
                            startActivity(intent);
                            view.goBack();
                            return true;
                        } else if (url.contains("copy://")) {
                            String text = url.replace("copy://", "");
                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("Kopiere", text);
                            clipboard.setPrimaryClip(clip);
                            view.goBack();
                            return true;
                        } else if (url.startsWith("mailto:")) {
                            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                            emailIntent.setData(Uri.parse(url));
                            emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            startActivity(emailIntent);
                            view.goBack();
                            return true;
                        } else if (url.endsWith(".pdf")) {
                            Toast.makeText(MainActivity.this, "Öffne PDF..." + url, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.parse(url), "application/pdf");
                            try {
                                //view.getContext().startActivity(intent);
                                startActivity(intent);
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(MainActivity.this, "Sie besitzen keine App zum öffnen von PDFs", Toast.LENGTH_SHORT).show();
                            }
                            view.goBack();
                            return false;
                        }
                        /* Handhabung externer Links */
                        else if (!url.contains("literaturlesen.com")) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            startActivity(intent);
                            view.goBack();
                            return false;
                        } else {
                            // Returning false causes WebView to load the URL while preventing it from adding URL redirects to the WebView history.
                            return false;
                        }
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        //Setzt in der Web-App den Schalter für den App-Aufruf auf true
                        surfer.evaluateJavascript("AppDevice.isapp=true;", null);
                    }
                });

        this.reload();


    }


    /**
     * Löscht den Cache des Webviews per Javascript-Kommando
     */
    public void clearCache()
    {
        this.surfer.clearCache(true);
    }

    /**
     * Senden von Kommandos an die WebApp
     *
     * @param cmd   Commando
     * @param param Parameter
     */
    public void sendToApp(String cmd, String param) {
        Log.d("app", "Commando" + cmd + " Parameter" + param);
        surfer.evaluateJavascript("AppInterface.receive('" + cmd + "','" + param + "');", null);
    }

    public void webviewLoadURL(String url) {
        Log.d("app", "now loading " + url);
        surfer.clearHistory();
        surfer.loadUrl(url);
    }

    public void reload() {
        Toast.makeText(getApplicationContext(), "Lade App", Toast.LENGTH_SHORT).show();
        if (!DetectConnection.checkInternetConnection(this)) {
            Toast.makeText(getApplicationContext(), "Keine Internetverbindung", Toast.LENGTH_SHORT).show();

            surfer.post(new Runnable() {
                @Override
                public void run() {
                    webviewLoadURL("file:///android_asset/404.html");
                }
            });
        } else {

            surfer.post(new Runnable() {
                @Override
                public void run() {
                    webviewLoadURL(previousUrl);
                    sendToApp("isapp", "true");
                }
            });
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && this.surfer.canGoBack()) {
            this.surfer.goBack();
            return true;
        } else {
            finish();
            // finish the activity
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //this.notification(2,getString(R.string.notification_name),getString(R.string.notification_description), this.getTomorrowTime());
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


}
