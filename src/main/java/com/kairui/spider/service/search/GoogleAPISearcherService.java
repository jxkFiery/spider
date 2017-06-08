

package com.kairui.spider.service.search;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.kairui.spider.domain.internet.Domains;
import com.kairui.spider.domain.search.Webpage;
import com.kairui.spider.service.internet.DomainsService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GoogleAPISearcherService {
    private static final Logger LOG = LoggerFactory.getLogger(GoogleAPISearcherService.class);

    @Resource
    private DomainsService domainsService;

    Integer pageSize = 10;

    public List<Webpage> search(String keyWord, Integer page) throws Exception {
        String urls;
        if (page == 1) {
            urls = "https://www.googleapis.com/customsearch/v1?key=AIzaSyC_pisvwigYJUyMj8bH6C65LxL81wqiEz0&cx=015323544115260942742:9yyn1-_8ism&q=" + keyWord + "&alt=json";
        } else {
            urls = "https://www.googleapis.com/customsearch/v1?key=AIzaSyC_pisvwigYJUyMj8bH6C65LxL81wqiEz0&cx=015323544115260942742:9yyn1-_8ism&q=" + keyWord + "&alt=json&start=" + (page - 1) * pageSize;
        }
        List<Webpage> list = new ArrayList<>();
        URL url = new URL(urls);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        BufferedReader br = new BufferedReader(new InputStreamReader(
                (conn.getInputStream())));

        String output;
        StringBuffer content = new StringBuffer();
        while ((output = br.readLine()) != null) {
            content.append(output);
        }
        JSONObject json = JSONObject.parseObject(content.toString());
        JSONArray array = (JSONArray) json.get("items");
        for (Object object : array) {
            Webpage webpage = new Webpage();
            JSONObject object1 = (JSONObject) object;
            String title = object1.get("title").toString();
            webpage.setTitle(title);
            String link = object1.get("link").toString();
            Document documents = Jsoup.connect(link).get();
            List<String> email = getEmailList(documents);
            List<String> emailLists = new ArrayList<>();
            emailLists.addAll(email);
            webpage.setEmail(emailLists);
            webpage.setUrl(link);
            list.add(webpage);
            //保存到数据库
            Domains domains = new Domains();
            domains.setKeyWord(keyWord);
            domains.setUrl(link);
            if (link.contains("https")) link = link.replaceAll("https://", "");
            if (link.contains("http")) link = link.replaceAll("http://", "");
            if (link.contains("www")) link = link.replaceAll("www.", "");
            if (link.endsWith("/")) link = link.substring(0, link.length() - 1);
            domains.setDomain(link);
            domains.setEmails(webpage.getEmail());
            domains.setTitle(webpage.getTitle());
            domains.setSource(1);
            domainsService.create(domains);
        }
        conn.disconnect();
        return list;
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