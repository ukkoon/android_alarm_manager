package dev.fluttercommunity.plus.androidalarmmanager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import org.json.JSONObject;
import org.json.JSONException;
import android.util.Log;

public class AlarmFlagManager {

  private static final String FLUTTER_SHARED_PREFERENCE_KEY = "FlutterSharedPreferences";
  private static final String ALARM_FLAG_KEY = "flutter.ringingAlarm";

  static public void set(Context context, Intent intent) {
    int callbackId = intent.getIntExtra("id", -1);
    String paramsJsonStr = intent.getStringExtra("params");

    try {
      JSONObject obj = new JSONObject();
      obj.put("id", callbackId);
      obj.put("params", paramsJsonStr);

      SharedPreferences prefs = context.getSharedPreferences(FLUTTER_SHARED_PREFERENCE_KEY, 0);
      prefs.edit().putString(ALARM_FLAG_KEY, obj.toString()).apply();

    } catch (JSONException e) {
      // some exception handler code.
      // Log.d("AlarmFlagManager",e.toString());
    }    
  }

}