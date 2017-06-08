/**
 * APDPlat - Application Product Development Platform
 * Copyright (c) 2013, 杨尚川, yang-shangchuan@qq.com
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.kairui.spider.service.search;

import com.kairui.spider.domain.internet.Domains;
import com.kairui.spider.domain.search.SearchResult;
import com.kairui.spider.domain.search.Webpage;
import com.kairui.spider.service.internet.DomainsService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BaiduSearcherService extends AbstractBaiduSearcher {
    private static final Logger LOG = LoggerFactory.getLogger(BaiduSearcherService.class);

    @Resource
    private DomainsService domainsService;

    @Override
    public SearchResult search(String keyword) {
        return search(keyword, 1);
    }

    @Override
    public SearchResult search(String keyWord, int page) {
        int pageSize = 10;
        //百度搜索结果每页大小为10，pn参数代表的不是页数，而是返回结果的开始数
        //如获取第一页则pn=0，第二页则pn=10，第三页则pn=20，以此类推，抽象出模式：(page-1)*pageSize
        String url = "http://www.baidu.com/s?pn=" + (page - 1) * pageSize + "&wd=" + keyWord + "&ie=utf-8";

        SearchResult searchResult = new SearchResult();
        searchResult.setPage(page);
        List<Webpage> webpages = new ArrayList<>();
        try {
            Document document = Jsoup.connect(url).get();

            //获取搜索结果数目
            int total = getBaiduSearchResultCount(document);
            searchResult.setTotal(total);
            int len = 10;
            if (total < 1) {
                return null;
            }
            //如果搜索到的结果不足一页
            if (total < 10) {
                len = total;
            }
            for (int i = 0; i < len; i++) {
                String titleCssQuery = "html body div div div div#content_left div#" + (i + 1 + (page - 1) * pageSize) + ".result.c-container h3.t a";
                String summaryCssQuery = "html body div div div div#content_left div#" + (i + 1 + (page - 1) * pageSize) + ".result.c-container div.c-abstract";
                Element titleElement = document.select(titleCssQuery).first();
                String href = "";
                String titleText = "";
                String email = "";
                if (titleElement != null) {
                    titleText = titleElement.text();
                    href = titleElement.attr("href");
                } else {
                    //处理百度百科
                    titleCssQuery = "html body div#out div#in div#wrapper div#container div#content_left div#1.result-op h3.t a";
                    summaryCssQuery = "html body div#out div#in div#wrapper div#container div#content_left div#1.result-op div p";
                    titleElement = document.select(titleCssQuery).first();
                    if (titleElement != null) {
                        titleText = titleElement.text();
                        href = titleElement.attr("href");
                    }
                }
                Element summaryElement = document.select(summaryCssQuery).first();
                //处理百度知道
                if (summaryElement == null) {
                    summaryCssQuery = summaryCssQuery.replace("div.c-abstract", "font");
                    summaryElement = document.select(summaryCssQuery).first();
                }
                String summaryText = "";
                if (summaryElement != null) {
                    summaryText = summaryElement.text();
                }
                if (titleText != null && !"".equals(titleText.trim()) && summaryText != null && !"".equals(summaryText.trim())) {
                    Webpage webpage = new Webpage();
                    webpage.setTitle(titleText);
                    URL url1 = new URL(href);
                    HttpURLConnection connection = (HttpURLConnection) url1.openConnection();
                    connection.setInstanceFollowRedirects(false); // 设置是否自动重定向
                    String urls = connection.getHeaderField("Location");
                    Document documents = Jsoup.connect(urls).get();
                    List<String> list = getEmailList(documents);
                    List<String> aa = new ArrayList<>();
                    aa.addAll(list);
                    webpage.setEmail(aa);
                    webpage.setUrl(urls);
                    webpages.add(webpage);
                    //保存到数据库
                    Domains domains = new Domains();
                    domains.setKeyWord(keyWord);
                    domains.setUrl(urls);
                    if (urls.contains("https")) urls = urls.replaceAll("https://", "");
                    if (urls.contains("http")) urls = urls.replaceAll("http://", "");
                    if (urls.contains("www")) urls = urls.replaceAll("www.", "");
                    if (urls.endsWith("/")) urls = urls.substring(0, urls.length() - 1);
                    domains.setDomain(urls);
                    domains.setEmails(webpage.getEmail());
                    domains.setTitle(webpage.getTitle());
                    domains.setSource(2);
                    domainsService.create(domains);
                } else {
                    LOG.error("获取搜索结果列表项出错:" + titleText + " - " + summaryText);
                }
            }


        } catch (IOException ex) {
            LOG.error("搜索出错", ex);
        }
        searchResult.setWebpages(webpages);
        ;
        return searchResult;
    }

    /**
     * 获取百度搜索结果数
     * 获取如下文本并解析数字：
     * 百度为您找到相关结果约13,200个
     *
     * @param document 文档
     * @return 结果数
     */
    private int getBaiduSearchResultCount(Document document) {
        String cssQuery = "html body div div div div.nums";
        Element totalElement = document.select(cssQuery).first();
        String totalText = totalElement.text();

        String regEx = "[^0-9]";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(totalText);
        totalText = matcher.replaceAll("");
        int total = Integer.parseInt(totalText);
        return total;
    }

    public List<String> getEmailList(Document document) {
        List<String> ls = new ArrayList<String>();
        String cssQuery = "html";
        Elements elements = document.select(cssQuery);
        String totalText = elements.toString();
        Pattern pattern = Pattern.compile("[a-zA-Z0-9_]+@[a-zA-Z0-9]+(\\.[a-zA-Z0-9]+){1,3}");
        Matcher matcher = pattern.matcher(totalText);
        while (matcher.find())
            ls.add(matcher.group());
        return ls;
    }
}