package com.example.shortfiletransferapp.utils.network;

import android.util.Log;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FileUploadUtils {
    public static void send2Server(File file, String token) {
        int tokenLength = token.length();
        int responseCode = -1;
        String somePartToken = token.substring(tokenLength-10, tokenLength); // token 의 뒤에서 10번째 자리 전달해줌

        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("files", file.getName(), RequestBody.create(MultipartBody.FORM, file))
                .build();
        Request request = new Request.Builder()
//                .url("https://junior-programmer.com/upload") // Server URL 은 본인 IP를 입력
                .url("http://172.30.1.60:8080/upload")
                .header("User-Agent", somePartToken)
                .post(requestBody).build();

        OkHttpClient client = new OkHttpClient();

        try {
            Response response = client.newCall(request).execute();
            responseCode = response.code();
        }catch (IOException ioe){
            ioe.printStackTrace();
        }
        Log.d("Upload ResponseCode: ", Integer.toString(responseCode));
        
    }
}

