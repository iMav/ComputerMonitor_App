package de.theonlymarv.computermonitor.Remote.WebServer;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Marvin on 23.08.2016.
 */
public class Request {
    private static String baseUrl = "https://theonlymarv.de/cm/api/";

    private Action action;
    private String requestUrl;

    public Request(Action action, @NonNull String requestUrl) {
        this.action = action;
        this.requestUrl = requestUrl;
    }

    public Action getAction() {
        return action;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public static String getLoginUrl(String username, String password){
        return baseUrl + String.format("login.php?username=%s&password=%s", username, password);
    }

    public static String getRegisterUrl(String username, String password){
        return baseUrl + String.format("register.php?username=%s&password=%s", username, password);
    }

    public static String getLoadDevicesUrl(String token){
        return baseUrl + String.format("devicelist.php?token=%s", token);
    }

    public static String getAddDeviceUrl(String token, String name){
        return baseUrl + String.format("deviceadd.php?token=%s&name=%s", token, name);
    }

    @SuppressLint("SimpleDateFormat")
    public static String getAddUsageUrl(String token, int deviceId, float download, float upload, @NonNull Date date){
        return baseUrl + String.format("usagedataadd.php?token=%s&deviceid=%s&download=%s&upload=%s&date=%s", token, String.valueOf(deviceId), String.valueOf(download), String.valueOf(upload), new SimpleDateFormat("yyyy-MM-dd").format(date)).replace(",", ".");
    }

    public static String getLoadUsageUrl(String token, int deviceId){
        return baseUrl + String.format("usagedatalist.php?token=%s&deviceid=%s", token, String.valueOf(deviceId));
    }

    public static String getDeleteUserUrl(String token, String password) {
        return baseUrl + String.format("userdelete.php?token=%s&password=%s", token, password);
    }

    public static String getDeleteDeviceUrl(String token, int deviceId) {
        return baseUrl + String.format("devicedelete.php?token=%s&deviceid=%s", token, String.valueOf(deviceId));
    }

    public enum Action {
        LOGIN,
        REGISTER,
        LOAD_DEVICE,
        ADD_DEVICE,
        ADD_USAGE,
        LOAD_USAGE,
        DELETE_USER,
        DELETE_DEVICE
    }
}
