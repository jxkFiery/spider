package com.kairui.spider.domain.commons;

public enum Role {
    PATIENT("patient", "患者"), DOCTOR("doctor", "医生"), ASSISTANT("assistant", "专员"), ADMIN("admin", "管理员"), SA("sa", "超级管理员");

    private String value;
    private String text;

    Role(String value, String text) {
        this.value = value;
        this.text = text;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
