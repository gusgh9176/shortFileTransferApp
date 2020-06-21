package com.example.shortfiletransferapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.shortfiletransferapp.adapter.ListViewAdapter;
import com.example.shortfiletransferapp.permission.TedPermission;
import com.example.shortfiletransferapp.utils.network.FileUploadUtils;
import com.example.shortfiletransferapp.utils.GetFileNameUtils;
import com.example.shortfiletransferapp.utils.network.GetUserListUtils;
import com.example.shortfiletransferapp.utils.network.SendingMytokenAndOpponentName;
import com.example.shortfiletransferapp.vo.ListViewUserVO;
import com.example.shortfiletransferapp.vo.UserVO;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences prefs;

    ListView listview;
    ListViewAdapter adapter;

    File tempSelectFile;

    private String token; // my token onStart에서 값 불러와서 넣어짐
    private String opponentName; // 전송 상대 이름, 유저목록에서 클릭 시 값 저장 됨

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("Start: onCreate ", "Start: onCreate ");

        prefs = getSharedPreferences("pref", MODE_PRIVATE);
        // FireBase 테스트
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("FCM Log", "getInstanceId failed", task.getException());
                            return;
                        }
                        String token = task.getResult().getToken(); // 해당 앱의 FCM token
                        Log.d("FCM Log", "FCM Token: " + token);
                    }
                });
        //이렇게 ALL 추가 하면 이 디바이스는 ALL을 구독한다는 얘기가 된다.
        FirebaseMessaging.getInstance().subscribeToTopic("ALL");
        // FireBase 테스트 끝

        // 권한 요구
        TedPermission.getPermission(getApplicationContext(), getResources());

        // 액션바 설정하기 //
        // 액션바 타이틀 변경하기
        getSupportActionBar().setTitle("유저리스트");
        // 액션바 배경색 변경
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xFF339999));
        // 액션바 설정 끝 //

        Log.d("End: onCreate ", "End: onCreate ");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("Start: onStart ", "Start: onStart ");

        refreshUsers();

        Log.d("End: onStart ", "End: onStart ");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != 1 || resultCode != RESULT_OK) {
            return;
        }
        final Uri dataUri = data.getData();

        // 전송 완료 될때까지 떠있을 alertDialog 생성
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("대기중").setMessage("파일 업로드를 기다리고 있습니다.");
        builder.setCancelable(false);
        final AlertDialog alertDialog = builder.create();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int bytesRead;
                    byte[] buffer = new byte[10240];

                    InputStream in = getContentResolver().openInputStream(dataUri);
                    // 확장자 찾기
                    String[] sts = GetFileNameUtils.getFileName(dataUri, getContentResolver()).split("\\.");
                    String extension = sts[sts.length - 1]; // 확장자
                    // 확장자 찾기 끝

                    // 폴더 없을 시 폴더 생성
                    File dir = new File(getFilesDir() + "/TempFile");
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }

                    // 선택한 파일 임시 저장
                    String date = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss").format(new Date());
                    tempSelectFile = new File(getFilesDir() + "/TempFile", "temp_" + date + "." + extension);
                    OutputStream out = new FileOutputStream(tempSelectFile);

                    // 데이터 쓰기
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                    // 업로드 테스트
                    FileUploadUtils.send2Server(tempSelectFile);
                    in.close();
                    out.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                // 전송 작업 다 완료되면 해당 팝업창 닫힘
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        alertDialog.dismiss();
                        completeAlertDialog();
                    }
                });
            }
        }).start();
        // 전송 완료 될때까지 해당 alertDialog 화면에 떠있음
        alertDialog.show();
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(checkFirstRun()){
            Toast.makeText(this, "목록 갱신이 안된다면 잠시 후 새로고침 버튼을 눌러보세요.", Toast.LENGTH_SHORT).show();
        }
    }


    // 액션버튼 메뉴 액션바에 집어 넣기
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    // 액션버튼을 클릭했을때의 동작
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_refresh: // actionbar의 refresh 키 눌렀을 때 유저 목록 새로고침
                refreshUsers();
                Toast.makeText(this, "새로 고침", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addItemAdapter(UserVO[] userVOS) {
        // Adapter 생성
        adapter = new ListViewAdapter();
        // 리스트뷰 참조 및 Adapter달기
        listview = (ListView) findViewById(R.id.listView_user);
        listview.setAdapter(adapter);

        for (UserVO vo : userVOS) {
            String index = "1";
            String name = vo.getName();
            String explain = "나와의 거리 차이 10m";
            adapter.addItem(index, name, explain);
        }

        adapter.notifyDataSetChanged(); // adapter 새로 고침
    }

    // FCM 실행시 저장해둔 token 값 가져옴
    // 서버에 요청하여 유저 목록 갱신
    // 갱신된 유저 목록에서 클릭 시 작업 실행
    private void refreshUsers() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 저장해놨던 token 값 가져오기
                SharedPreferences tempPref = getSharedPreferences("pref", MODE_PRIVATE);
                token = tempPref.getString("token", "");
                // 얻은 토큰으로 유저 목록 받아오기
                final UserVO[] userVOS = GetUserListUtils.send2Server(token);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (userVOS == null) {
                            Log.d("state: userVOS", "is null");
                            return;
                        }
                        addItemAdapter(userVOS); // 유저 목록 갱신
                        // 유저 클릭시 파일 선택 창 열음
                        // 전송 관련 내용 https://derveljunit.tistory.com/302?category=523828
                        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView parent, View v, int position, long id) {
                                opponentName = ((ListViewUserVO) adapter.getItem(position)).getnameStr(); // 상대 이름 변수에 저장 됨
                                Intent intent = new Intent();
                                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                                intent.setType("*/*");
                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                startActivityForResult(intent, 1);
                            }
                        });
                    }
                });
            }
        }).start();
    }

    // 기다리는 작업 완료시 띄워줄 alertDialog
    public void completeAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("끝났습니다").setMessage("파일 업로드가 완료되었습니다. \n확인 버튼을 눌러 상대에게 요청을 보냅니다.");
        builder.setCancelable(false);
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                // 서버로 요청 보내는 부분 코드 작성하기
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SendingMytokenAndOpponentName.send2Server(token, opponentName); // 내 token과 상대 이름으로 전송 요청 서버로 보내기
                    }
                }).start();

            }
        });

        AlertDialog alertDialog = builder.create();

        alertDialog.show();
    }

    // 앱 첫 실행 체크
    // 첫 실행 시 true 반환, 아닐 시 false 반환
    public boolean checkFirstRun() {
        boolean isFirstRun = prefs.getBoolean("isFirstRun", true); // 해당 값 불러옴, 비워져있으면 true 넣음
        prefs.edit().putBoolean("isFirstRun", false).apply(); // 해당 값 false 넣어줌
        return isFirstRun;
    }
}

