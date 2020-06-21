package com.example.shortfiletransferapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.shortfiletransferapp.utils.network.FileDownloadUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DownloadActivity extends AppCompatActivity {

    String token;
    String senderName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        // 액션바 설정하기 //
        // 액션바 타이틀 변경하기
        getSupportActionBar().setTitle("다운로드 요청 화면");
        // 액션바 배경색 변경
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xFF339999));
        // 액션바 설정 끝 //

        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        token = pref.getString("token", ""); // 내 token 저장
        senderName = getIntent().getStringExtra("senderName");
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        RequestSendingAlertDialog();
    }

    private void RequestSendingAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("전송 요청").setMessage("선택하세요.");
        builder.setCancelable(false);

        builder.setPositiveButton("수락", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Toast.makeText(getApplicationContext(), "다운로드가 실행됩니다. 완료 후 메인 페이지로 돌아갑니다.", Toast.LENGTH_SHORT).show();
                ProgressBar progressBar = findViewById(R.id.progress1);
                progressBar.setVisibility(View.VISIBLE);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // http://egloos.zum.com/pavecho/v/7204359
                        // 선택한 파일 저장
                        // 송신자 Name
                        String date = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss").format(new Date());
                        String savePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + date + ".";
                        FileDownloadUtils.send2Server(senderName, token, savePath); // 다운로드 테스트
                        moveMainActivity();
                    }
                }).start();
            }
        });

        builder.setNegativeButton("거절", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Toast.makeText(getApplicationContext(), "거절하였습니다. 잠시 후 메인 페이지로 돌아갑니다.", Toast.LENGTH_SHORT).show();
                moveMainActivity();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void moveMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
