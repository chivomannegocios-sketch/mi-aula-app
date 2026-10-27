package com.chivits.miaula;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Locale;

/**
 * Mi Aula como app: muestra la página (assets/index.html) en un WebView.
 * El progreso se guarda en el propio celular (localStorage del WebView).
 * La voz de los idiomas usa el TextToSpeech del teléfono mediante window.AndroidVoz.
 */
public class MainActivity extends Activity {

    private WebView web;
    private TextToSpeech tts;
    private volatile boolean ttsListo = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean noche = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        if (!noche && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }

        web = new WebView(this);
        setContentView(web);

        WebSettings ws = web.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setDatabaseEnabled(true);
        ws.setAllowFileAccess(true);
        ws.setTextZoom(100);
        ws.setMediaPlaybackRequiresUserGesture(false);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri u = request.getUrl();
                String s = u.getScheme();
                if ("http".equals(s) || "https".equals(s)) {
                    try { startActivity(new Intent(Intent.ACTION_VIEW, u)); } catch (Exception ignored) {}
                    return true;
                }
                return false;
            }
        });

        tts = new TextToSpeech(this, status -> ttsListo = (status == TextToSpeech.SUCCESS));
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override public void onStart(String id) {}
            @Override public void onDone(String id) { avisarFin(); }
            @Override public void onError(String id) { avisarFin(); }
            @Override public void onStop(String id, boolean interrupted) { avisarFin(); }
        });

        web.addJavascriptInterface(new Voz(), "AndroidVoz");

        if (savedInstanceState != null) web.restoreState(savedInstanceState);
        else web.loadUrl("file:///android_asset/index.html");
    }

    private void avisarFin() {
        runOnUiThread(() -> { if (web != null) web.evaluateJavascript("window.vozFin && vozFin()", null); });
    }

    /** Lo que la página puede llamar desde JavaScript. */
    private class Voz {
        @JavascriptInterface
        public String hablar(String texto, String idioma, float velocidad) {
            if (tts == null || !ttsListo) return "iniciando";
            Locale loc = Locale.forLanguageTag(idioma);
            int disp = tts.isLanguageAvailable(loc);
            if (disp < TextToSpeech.LANG_AVAILABLE) return "sinidioma";
            tts.setLanguage(loc);
            tts.setSpeechRate(velocidad);
            tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "miaula");
            return "ok";
        }

        @JavascriptInterface
        public void parar() { if (tts != null) tts.stop(); }
    }

    @Override
    public void onBackPressed() {
        web.evaluateJavascript("(typeof atrasApp === 'function') ? atrasApp() : false", valor -> {
            if (!"true".equals(valor)) super.onBackPressed();
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        web.saveState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (tts != null) tts.stop();
    }

    @Override
    protected void onDestroy() {
        if (tts != null) { tts.stop(); tts.shutdown(); }
        if (web != null) web.destroy();
        super.onDestroy();
    }
}
