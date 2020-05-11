package com.example.shortfiletransferapp.vo;

import android.graphics.drawable.Drawable;

public class ListViewNotepadVO {
//    private Drawable iconDrawable ; // 미리보기 아이콘
    private String index; // 메모의 인덱스
    private String nameStr ; // 이름
    private String explainStr ; // 설명

//    public Drawable getIconDrawable() {
//        return iconDrawable;
//    }

//    public void setIconDrawable(Drawable iconDrawable) {
//        this.iconDrawable = iconDrawable;
//    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getnameStr() {
        return nameStr;
    }

    public void setNameStr(String nameStr) {
        this.nameStr = nameStr;
    }

    public String getExplainStr() {
        return explainStr;
    }

    public void setExplainStr(String explainStr) {
        this.explainStr = explainStr;
    }
}
