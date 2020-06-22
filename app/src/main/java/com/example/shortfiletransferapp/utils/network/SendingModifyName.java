package com.example.shortfiletransferapp.utils.network;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.SocketTimeoutException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

// 이름 변경 요청 보냄
public class SendingModifyName {
    public static int send2Server(String token, String modifyName) {
        int responseCode = -1;
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        // create your json here
        JSONObject jsonObject = new JSONObject();

        Log.d("FCM Log", "SendingModifyName");

        try {
            jsonObject.put("token", token);
            jsonObject.put("name", modifyName);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            RequestBody requestBody = RequestBody.create(JSON, jsonObject.toString());
            Request request = new Request.Builder()
//                    .url("https://junior-programmer.com/mobile/update/FCMToken") // Server URL 은 본인 IP를 입력
                    .url("http://172.30.1.60:8080/mobile/update/FCMToken")
                    .post(requestBody)
                    .build();

            OkHttpClient client = new OkHttpClient();
            Response response = client.newCall(request).execute();
            responseCode = response.code();
            if(responseCode != 200){
                return responseCode;
            }
            try {
                String id = response.body().string(); // id 반환, -1일 경우 잘못된 token이라 해당 요청 실패
                Log.d("Send Result: ", id);
                response.close();

            } catch (SocketTimeoutException ste) {
                ste.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseCode;
    }
}
