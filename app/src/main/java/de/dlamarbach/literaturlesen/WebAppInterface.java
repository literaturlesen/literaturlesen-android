package de.dlamarbach.literaturlesen;

import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

/**
 * Das Interface zur WebApp
 */

    public class WebAppInterface {
        private MainActivity main;

        /** Instantiate the interface and set the context */
        WebAppInterface(MainActivity c)
        {
            this.main = c;
        }

        /**
         * Funktion zum Austausch von Kommandos
         * @param commandID Kommando-ID
         * @param data Datenobject
         */
        @JavascriptInterface
        public void command(String commandID, String data )
        {
            switch ( commandID )
            {
                case "toast": this.toast(data); break;
                case "clearcache": this.clearCache(); break;
                case "reload": this.reload(); break;
                default:
            }
        }

        /**
         * Toastet eine Nachricht
         * @param message Nachricht
         */
        @JavascriptInterface
        public void toast( String message )
        {
            Toast.makeText( this.main, message, Toast.LENGTH_SHORT).show();
        }


        /**
         * Cleared den Cache der App
         */
        @JavascriptInterface
        public void clearCache()
        {
            this.main.clearCache();

        }

        @JavascriptInterface
        public void reload()
        {
            this.main.reload();
        }


}
