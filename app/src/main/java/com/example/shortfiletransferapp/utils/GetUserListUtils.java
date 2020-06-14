package com.example.shortfiletransferapp.utils;

import com.example.shortfiletransferapp.vo.UserVO;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

// 자신의 토큰 보내서 서버에 있어야 목록 받을 수 있음
// token 전달하여 올바른 사용자 확인
public class GetUserListUtils {
    public static UserVO[] send2Server(String token) {

        Log.d("FCM Log", "GetUserList token: " + token);
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        // create your json here
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("token", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            RequestBody requestBody = RequestBody.create(JSON, jsonObject.toString());

            Request request = new Request.Builder()
//                    .url("https://junior-programmer.com/mobile/read/UserList") // Server URL 은 본인 IP를 입력
                    .url("http://172.30.1.60:8080/mobile/read/UserList")
                    .post(requestBody)
                    .build();

            OkHttpClient client = new OkHttpClient();
            Response response = client.newCall(request).execute();

            try {
                String jsonStr = response.body().string();
                System.out.println("유저목록 JSON 확인" + jsonStr);

                ObjectMapper mapper = new ObjectMapper();

                UserVO[] userVOS;
                userVOS = mapper.readValue(jsonStr, UserVO[].class);
                System.out.println("JSON array to Array objects");
                for (UserVO vo : userVOS) {
                    System.out.println("name: " + vo.getName());
                }
                response.close();

                return userVOS; // user name 리스트 반환

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
        return null;
    }
}
