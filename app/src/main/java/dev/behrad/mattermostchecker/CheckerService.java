package dev.behrad.mattermostchecker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CheckerService extends Service {

    Thread workerThread;
    PowerManager.WakeLock wakeLock;

    public CheckerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String userToken = intent.getExtras().get("token").toString();
        String baseUrl = intent.getExtras().get("base_url").toString();

        workerThread = new Thread(() -> {
            while (true) {
                try {
                    Log.i("MattermostChecker", "CHECKING");
                    boolean t = isThereAnyMention(baseUrl + "/api/v4/users/me/teams/unread", userToken);
                    if(t) {
                        ring();
                    }
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    break;
                } catch (IOException | JSONException e) {
//                    throw new RuntimeException(e);
                }
            }
        });
        workerThread.start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        workerThread.interrupt();
        wakeLock.release();
        Toast.makeText(this, "Stopped", Toast.LENGTH_SHORT).show();
    }

    private void ring() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isThereAnyMention(String url, String token) throws IOException, JSONException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .header("Authorization", "Bearer " + token)
                .url(url)
                .build();
        String response = client.newCall(request).execute().body().string();
        Log.d("response", response);
        // Iteration over all teams
        JSONArray jsonArray = new JSONArray(response);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject team = jsonArray.getJSONObject(i);
            if(team.getInt("mention_count") >= 1 || team.getInt("mention_count_root") >= 1){
                return true;
            }
        }
        return false;
    }
}