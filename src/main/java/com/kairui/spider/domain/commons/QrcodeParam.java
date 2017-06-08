package com.kairui.spider.domain.commons;

/**
 * 二维码实体类
 */
public class QrcodeParam {

    private String uploadPath;//【上传完整路径】

    private String content;//【二维码内容】

    private String name;//【文件名】

    private Integer width;//【大小】

    public String getUploadPath() {
        return uploadPath;
    }

    public void setUploadPath(String uploadPath) {
        this.uploadPath = uploadPath;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }
}
