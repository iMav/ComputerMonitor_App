package de.theonlymarv.computermonitor.Remote.WebServer;

import android.app.SearchManager;
import android.os.AsyncTask;
import android.util.Log;

import com.owlike.genson.GenericType;
import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;
import com.owlike.genson.JsonBindingException;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import de.theonlymarv.computermonitor.Interfaces.OnNetworkAccess;

/**
 * Created by Marvin on 23.08.2016.
 */
public class ServerConnection extends AsyncTask<Request, Void, Object> {
    private static final String TAG = SearchManager.class.getSimpleName();
    OnNetworkAccess networkAccess;

    public ServerConnection(OnNetworkAccess networkAccess) {
        this.networkAccess = networkAccess;
    }

    @Override
    protected Object doInBackground(Request... params) {
        if (params == null || params.length > 1) {
            networkAccess.OnError(new IllegalArgumentException("Wrong number of arguments"));
            return null;
        }
        Request request = params[0];
        try {
            URL url = new URL(request.getRequestUrl());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            InputStream is = connection.getInputStream();
            Genson genson = new GensonBuilder().failOnMissingProperty(true).create();

            switch (request.getAction()) {
                case LOAD_DEVICE:
                    try {
                        return genson.deserialize(is, new GenericType<List<Device>>() {
                        });
                    } catch (JsonBindingException jbe) {
                        Log.i(TAG, "JsonBindingException: not a List of Device");
                    }
                case LOAD_USAGE:
                    try {
                        return genson.deserialize(is, new GenericType<List<Usage>>() {
                        });
                    } catch (JsonBindingException jbe) {
                        Log.i(TAG, "JsonBindingException: not a List of Device");
                    }
                case LOGIN:
                case REGISTER:
                case ADD_DEVICE:
                case ADD_USAGE:
                    try {
                        return genson.deserialize(is, de.theonlymarv.computermonitor.Remote.WebServer.Status.class);
                    } catch (JsonBindingException jbe) {
                        Log.e(TAG, "JsonBindingException: not a object of Status", jbe);
                    }
            }
        } catch (Exception e) {
            networkAccess.OnError(e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Object object) {
        if (object != null) {
            networkAccess.OnSuccessful(object);
        }
    }
}
