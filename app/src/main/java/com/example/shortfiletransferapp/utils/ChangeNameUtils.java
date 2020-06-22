package com.example.shortfiletransferapp.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ChangeNameUtils {

    // 원래 name에 SHA-256 해시 적용한 값에 뒤에서 20자리를 새로운 이름으로 사용
    public static String hashName(String name){
        String SHA = sha256(name);
        String somePartSHA = null;
        int length = 0;
        if(SHA != null){
            length = SHA.length();
            somePartSHA = SHA.substring(length-20, length);
        }
        return somePartSHA;
    }

    // SHA-256 적용
    private static String sha256(String name){
        String SHA = "";
        try{
            MessageDigest sh = MessageDigest.getInstance("SHA-256");
            sh.update(name.getBytes());
            byte byteData[] = sh.digest();
            StringBuffer sb = new StringBuffer();
            for(int i = 0 ; i < byteData.length ; i++){
                sb.append(Integer.toString((byteData[i]&0xff) + 0x100, 16).substring(1));
            }
            SHA = sb.toString();
        }catch(NoSuchAlgorithmException e){
            e.printStackTrace();
            SHA = null;
        }
        return SHA;
    }
}
