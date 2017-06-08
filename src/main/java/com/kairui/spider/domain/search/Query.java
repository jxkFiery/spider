package com.kairui.spider.domain.search;

/**
 *搜索条件
 */
public class Query {

    private String keyWord;

    private Integer page;

    public String getKeyWord() {
        return keyWord;
    }

    public void setKeyWord(String keyWord) {
        this.keyWord = keyWord;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }
}
