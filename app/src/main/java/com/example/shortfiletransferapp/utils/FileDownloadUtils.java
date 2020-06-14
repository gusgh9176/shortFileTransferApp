package com.example.shortfiletransferapp.utils;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

// okhttp로 안됨 다른 통신으로 리스폰스 받아서 구현해야함
// 이걸로 파일 경로(확장자포함) 구하고 다른 통신으로 파일 다운로드 해야함
// httpURLConnection
public class FileDownloadUtils {
    // token 전달하여 파일 경로 얻음
    public static void send2Server(String token, String dir) {
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
                    .url("https://junior-programmer.com/download") // Server URL 은 본인 IP를 입력
//                    .url("http://172.30.1.60:8080/download")
                    .post(requestBody)
                    .build();

            OkHttpClient client = new OkHttpClient();
            Response response = client.newCall(request).execute();
            String[] temp = response.headers().get("Content-Disposition").split("\\.");
            String extension = temp[temp.length-1].substring(0, temp[temp.length-1].length()-2); // 확장자 jpg, docx

            //
            int bytesRead = 0;
            byte[] buffer = new byte[10240];
            InputStream inputStream = response.body().byteStream();
            File newFile = new File(dir + extension);
            OutputStream outputStream = new FileOutputStream(newFile);
            while((bytesRead =inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            response.close();
            inputStream.close();
            outputStream.close();
            //

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
