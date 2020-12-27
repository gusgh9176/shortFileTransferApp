package com.example.shortfiletransferapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.example.shortfiletransferapp.utils.DateManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;


public class MyFireBaseMessagingService extends FirebaseMessagingService {

    // 처음 앱 설치시에만 해당 함수 실행 됨
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("FCM Log", "Refreshed token: " + token);

        // https://yamea-guide.tistory.com/227
        // 생성한 토큰을 서버로 날려서 저장하기 위해서 만든거
        sendRegistrationToServer(token);
    }


    // 메시지를 받았을 때 실행되는 함수
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        // 알람 Title, 알람 Body
        try {
            sendNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("body"), remoteMessage.getData().get("clickAction"), remoteMessage.getData().get("senderName"));
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    // 메시지를 받았을 때 처리하는 함수 (알람 보여줌)
    private void sendNotification(String messageTitle, String messageBody, String click_action, String senderName) {

        if (messageTitle != null && messageBody != null) {
            Log.d("FCM Log", "알림 메시지: " + messageBody);

            Intent intent = new Intent(this, MainActivity.class); // 초기화
            if(click_action.equals("DownloadActivity")){
                intent = new Intent(this, DownloadActivity.class);
                intent.putExtra("senderName", senderName); // 해당 Activity로 senderName 전달
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            }
            else {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            }

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

            String channelId = "Channel ID";
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, channelId)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(messageTitle)
                            .setContentText(messageBody)
                            .setAutoCancel(true)
                            .setSound(defaultSoundUri)
                            .setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    String channelName = "Channel Name";
                    NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
                    notificationManager.createNotificationChannel(channel);
                }
                notificationManager.notify(0, notificationBuilder.build());
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    // 서버로 토큰 값 전송 (서버에서 해당 기기 토큰 값 저장하는 용도)
    private void sendRegistrationToServer(String token) {
        Random random = new Random();
        String name = Integer.toString(random.nextInt(10000000)); // 이름

        //만들어진 token, name, 시간 저장
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("token", token);
        editor.putString("name", name);
        editor.putString("modifyTime", DateManager.getDateStr());
        editor.apply();
        // 저장 끝

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        JSONObject jsonInput = new JSONObject();
        try {
            jsonInput.put("name", name); // User name (변동 가능)
            jsonInput.put("token", token); // User token (설치시 고정)
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // OKHTTP를 이용해 웹서버로 토큰값을 날려준다.
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(JSON, jsonInput.toString()); // JSON 담은 RequestBody 생성

        //request
        Request request = new Request.Builder()
                .url("http://172.30.1.60:8080/mobile/insert/FCMToken") // 토큰 저장하려고 보내는 URL
//                .url("https://junior-programmer.com/mobile/insert/FCMToken")
                .post(body)
                .build();

        try {
            client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}