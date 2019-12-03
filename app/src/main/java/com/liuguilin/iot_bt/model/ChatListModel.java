package com.liuguilin.iot_bt.model;


public class ChatListModel  {

    //类型
    private int type;

    //左边文本
    private String leftText;
    //右边文本
    private String rightText;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getLeftText() {
        return leftText;
    }

    public void setLeftText(String leftText) {
        this.leftText = leftText;
    }

    public String getRightText() {
        return rightText;
    }

    public void setRightText(String rightText) {
        this.rightText = rightText;
    }
}
