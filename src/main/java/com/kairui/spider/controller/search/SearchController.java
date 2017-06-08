package com.kairui.spider.controller.search;

import com.kairui.spider.domain.commons.Result;
import com.kairui.spider.domain.search.Query;
import com.kairui.spider.service.internet.DomainsService;
import com.kairui.spider.service.search.BaiduSearcherService;
import com.kairui.spider.service.search.GoogleAPISearcherService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Resource
    private BaiduSearcherService baiduSearcherService;

    @Resource
    private GoogleAPISearcherService googleAPISearcherService;

    @Resource
    private DomainsService domainsService;


    //调用百度搜索
    @PostMapping("/baidu")
    public Result getBaidu(@RequestBody Query model) {
        Result result = new Result();
        try {
            result.setData(baiduSearcherService.search(model.getKeyWord(), model.getPage()));
            result.setMsg("获取成功");
        } catch (Exception ex) {
            result.setError(ex.getMessage() != null ? ex.getMessage() : ex.toString());
            ex.printStackTrace();
        }
        return result;
    }

    //调用谷歌搜索
    @PostMapping("/google")
    public Result getGoogle(@RequestBody Query model) {
        Result result = new Result();
        try {
            result.setData(googleAPISearcherService.search(model.getKeyWord(), model.getPage()));
            result.setMsg("添加成功");
        } catch (Exception ex) {
            result.setError(ex.getMessage() != null ? ex.getMessage() : ex.toString());
            ex.printStackTrace();
        }
        return result;
    }
}
