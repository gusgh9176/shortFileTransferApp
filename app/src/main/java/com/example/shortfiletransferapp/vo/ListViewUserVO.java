package com.example.shortfiletransferapp.vo;

public class ListViewUserVO {
    private String index; // user의 인덱스
    private String nameStr ; // name
    private String explainStr ; // 설명

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
