package com.example.shortfiletransferapp.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateManager {

    private static SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // String 타입으로 현재 시간 반환
    public static String getDateStr(){
        Date time = new Date();
        String timeStr = transFormat.format(time);
        return timeStr;
    }

    // String 타입 Date 타입으로 변환해서 반환
    private static Date string2Date(String dateStr){
        Date date = null;
        try {
            date = transFormat.parse(dateStr);
        }catch (ParseException pe){
            pe.printStackTrace();
        }
        return date;
    }

    // 시간 차이 계산
    public static long compareDate(String dateStr, String modifyDateStr){
        Date date = string2Date(dateStr);
        Date modifyDate = string2Date(modifyDateStr);
        long diff = 0;
        long sec, min;

        diff = modifyDate.getTime() - date.getTime();
        sec = diff / 1000; // 초
        min = sec / 60; // 분

        return min;
    }
}
