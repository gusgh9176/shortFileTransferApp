package com.example.shortfiletransferapp.permission;

import android.Manifest;
import android.content.Context;
import android.content.res.Resources;

import com.example.shortfiletransferapp.R;
import com.gun0912.tedpermission.PermissionListener;

import java.util.ArrayList;

public class TedPermission {
    // 첫 실행시 사용자에게 권한 요청 부분
    public static void getPermission(Context context, Resources resources) {
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
        com.gun0912.tedpermission.TedPermission.with(context)
                .setPermissionListener(permissionListener)
                .setRationaleMessage(resources.getString(R.string.permission_2))
                .setDeniedMessage(resources.getString(R.string.permission_1))
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET)
                .check();
    }
}
