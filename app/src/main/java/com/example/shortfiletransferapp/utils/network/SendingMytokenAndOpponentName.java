package com.example.shortfiletransferapp.utils.network;

import android.util.Log;

import com.example.shortfiletransferapp.vo.UserVO;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SendingMytokenAndOpponentName {

    public static boolean send2Server(String token, String opponentName) {
        int responseCode = -1;
        boolean result = false;
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        // create your json here
        JSONObject jsonObject = new JSONObject();

        Log.d("FCM Log", "SendingMytokenAndOpponentName");

        try {
            jsonObject.put("token", token);
            jsonObject.put("name", opponentName);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            RequestBody requestBody = RequestBody.create(JSON, jsonObject.toString());

            Request request = new Request.Builder()
//                    .url("https://junior-programmer.com/mobile/send/FCMToken") // Server URL 은 본인 IP를 입력
                    .url("http://172.30.1.60:8080/mobile/send/FCMToken")
                    .post(requestBody)
                    .build();

            OkHttpClient client = new OkHttpClient();
            Response response = client.newCall(request).execute();
            responseCode = response.code();
            if(responseCode != 200){
                return false;
            }
            try {
                String jsonStr = response.body().string(); // 전송 요청 결과 반환 상대 허락X, 서버로 제대로 전송됐는지만
                Log.d("Send Result: ", jsonStr);

                ObjectMapper mapper = new ObjectMapper();

                String resStr;
                resStr = mapper.readValue(jsonStr, String.class);
                response.close();
                if(resStr.equals("") || resStr.isEmpty()){
                    result = false;
                }
                else{
                    result = true;
                }

            } catch (SocketTimeoutException ste) {
                ste.printStackTrace();
            } catch (JsonGenerationException jge) {
                jge.printStackTrace();
            } catch (JsonMappingException jme) {
                jme.printStackTrace();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
