package de.dlamarbach.literaturlesen;


import android.content.Context;
import android.net.ConnectivityManager;

/**
 * Überprüft, ob ein Netzwerkstatus besteht und die App online ist
 */

public class DetectConnection {
    public static boolean checkInternetConnection(Context context) {

        ConnectivityManager con_manager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return (con_manager.getActiveNetworkInfo() != null
                && con_manager.getActiveNetworkInfo().isAvailable()
                && con_manager.getActiveNetworkInfo().isConnected());
    }
}
