package com.example.shortfiletransferapp;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.shortfiletransferapp.adapter.ListViewAdapter;
import com.example.shortfiletransferapp.utils.FileUploadUtils;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    ListView listview;
    ListViewAdapter adapter;

    File tempSelectFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 권한 요구
        tedPermission();

        // 액션바 설정하기 //
        // 액션바 타이틀 변경하기
        getSupportActionBar().setTitle("유저리스트");
        // 액션바 배경색 변경
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xFF339999));
        // 액션바 설정 끝 //

        // 푸쉬알람 설정하기 //
        //푸시 알림을 보내기위해 시스템에 권한을 요청하여 생성
        final NotificationManager notificationManager =
                (NotificationManager) MainActivity.this.getSystemService(MainActivity.this.NOTIFICATION_SERVICE);
        //푸시 알림 터치시 실행할 작업 설정(여기선 MainActivity로 이동하도록 설정)
        final Intent intent = new Intent(MainActivity.this.getApplicationContext(), MainActivity.class);
        //Notification 객체 생성
        final Notification.Builder builder = new Notification.Builder(getApplicationContext());
        //푸시 알림을 터치하여 실행할 작업에 대한 Flag 설정 (현재 액티비티를 최상단으로 올린다 | 최상단 액티비티를 제외하고 모든 액티비티를 제거한다)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // 푸쉬알람 설정 끝 //

//        PushAlarm(notificationManager, intent, builder); // 푸쉬 알람 보냄

        addItemAdapter(); // 유저 목록 갱신

        // addItemAdapter() 메소드 내부에서 생성한 listview에 클릭 이벤트 핸들러 정의.
        // 유저 클릭시 파일 선택 창 열음
        // 전송 관련 내용 https://derveljunit.tistory.com/302?category=523828
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {

                Intent intent = new Intent();
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setType("*/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 1);

            }
        });
    }

    // 첫 실행시 사용자에게 권한 요청 부분
    private void tedPermission() {
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                // 권한 요청 성공
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                // 권한 요청 실패
            }
        };
        // 쓰기, 카메라, 인터넷 권한 요구
        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage(getResources().getString(R.string.permission_2))
                .setDeniedMessage(getResources().getString(R.string.permission_1))
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET)
                .check();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != 1 || resultCode != RESULT_OK) {
            return;
        }
        Uri dataUri = data.getData();

        try {
            byte[] buffer = null;

            InputStream in = getContentResolver().openInputStream(dataUri);

            String[] sts = getFileName(dataUri).split("\\.");
            String extension = sts[sts.length-1]; // 확장자

//            WaitingAlertDialog(); // 전송 요청 보내고 Response 기다림

            // 폴더 없을 시 생성
            File dir = new File(getFilesDir() + "/TempFile");
            if(!dir.exists()){
                dir.mkdirs();
            }

            // 선택한 파일 임시 저장
            String date = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss").format(new Date());
            tempSelectFile = new File(getFilesDir()+"/TempFile", "temp_" + date + "." + extension);
            final OutputStream out = new FileOutputStream(tempSelectFile);

            buffer = new byte[10240];
            int bytesRead;
            while((bytesRead = in.read(buffer)) != -1){
                out.write(buffer, 0, bytesRead);
            }
            in.close();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    FileUploadUtils.send2Server(tempSelectFile);
                    try {
                        out.close();
                    }catch (IOException ioe){
                        ioe.printStackTrace();
                    }
                }
            }).start();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (NullPointerException npe){
            npe.printStackTrace();
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
            case R.id.action_refresh: // actionbar의 refresh 키 눌렀을 때 동작
                addItemAdapter();  // 메소드 통해 adapter와 listview를 재 생성 시켜줌
                Toast.makeText(this, "새로 고침", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addItemAdapter() {
        // Adapter 생성
        adapter = new ListViewAdapter();
        // 리스트뷰 참조 및 Adapter달기
        listview = (ListView) findViewById(R.id.listView_user);
        listview.setAdapter(adapter);

        String index = "1";
        String name = "Moon";
        String explain = "나와의 거리 차이 10m";
        String explain2 = "나와의 거리 차이 19m";
        adapter.addItem(index, name, explain);
        adapter.addItem(index + 2, name + 2, explain2);

        adapter.notifyDataSetChanged(); // adapter 새로 고침
    }

    public void WaitingAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("대기중").setMessage("수신자의 수락을 기다리고 있습니다.");

        AlertDialog alertDialog = builder.create();

        alertDialog.show();
    }

    public void SendingAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("파일 전송중...").setMessage("파일을 수신자에게 전송하고 있습니다. \n 남은 용량: xx, 남은 시간: xx");

        AlertDialog alertDialog = builder.create();

        alertDialog.show();
    }

    public void RequestSendingAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("전송 요청").setMessage("선택하세요.");

        builder.setPositiveButton("수락", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Toast.makeText(getApplicationContext(), "수락하였습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("거절", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Toast.makeText(getApplicationContext(), "거절하였습니다.", Toast.LENGTH_SHORT).show();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void PushAlarm(NotificationManager notificationManager, Intent intent, Notification.Builder builder) {
        //앞서 생성한 작업 내용을 Notification 객체에 담기 위한 PendingIntent 객체 생성
        PendingIntent pendnoti = PendingIntent.getActivity(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //푸시 알림에 대한 각종 설정
        builder.setSmallIcon(R.drawable.ic_launcher_background).setTicker("Ticker").setWhen(System.currentTimeMillis())
                .setNumber(1)
                .setContentTitle("전송 알림")
                .setContentText("유저 xx가 xx 파일을 전송하려 합니다.")
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .setContentIntent(pendnoti)
                .setAutoCancel(true)
                .setOngoing(true);

        //NotificationManager를 이용하여 푸시 알림 보내기
        notificationManager.notify(1, builder.build());
    }

    // Uri 통해서 파일 이름 얻는 메소드
    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

}

