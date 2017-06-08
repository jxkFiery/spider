package com.kairui.spider.controller.internet;

import com.kairui.spider.domain.commons.PageParam;
import com.kairui.spider.domain.commons.Result;
import com.kairui.spider.domain.internet.Domains;
import com.kairui.spider.service.internet.DomainsService;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/domains")
public class DomainsController {

    @Resource
    private DomainsService service;

    @PostMapping
    public Result create(@RequestBody Domains model) {
        Result result = new Result();
        try {
            result.setData(service.create(model));
            result.setMsg("添加成功");
        } catch (Exception ex) {
            result.setError(ex.getMessage() != null ? ex.getMessage() : ex.toString());
            ex.printStackTrace();
        }
        return result;
    }

    @PutMapping("/{id}")
    public Result save(@PathVariable String id, @RequestBody Domains Domains) {
        Result result = new Result();
        try {
            Domains.setId(id);
            result.setData(service.save(Domains));
            result.setMsg("保存成功");
        } catch (Exception ex) {
            result.setError(ex.getMessage() != null ? ex.getMessage() : ex.toString());
            ex.printStackTrace();
        }
        return result;
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable String id) {
        Result result = new Result();
        try {
            service.delete(id);
            result.setMsg("删除成功");
        } catch (Exception ex) {
            result.setError(ex.getMessage() != null ? ex.getMessage() : ex.toString());
            ex.printStackTrace();
        }
        return result;
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable String id) {
        Result result = new Result();
        try {
            result.setData(service.findOne(id));
            result.setMsg("加载成功");
        } catch (Exception ex) {
            result.setError(ex.getMessage() != null ? ex.getMessage() : ex.toString());
            ex.printStackTrace();
        }
        return result;
    }

    @GetMapping
    public Result findAll(PageParam param, Domains model) {
        Result result = new Result();
        try {
            PageRequest pageRequest = param.getPageRequest();
            result.setData(service.findAll(pageRequest, model));
            result.setMsg("加载成功");
        } catch (Exception ex) {
            result.setError(ex.getMessage() != null ? ex.getMessage() : ex.toString());
            ex.printStackTrace();
        }
        return result;
    }
}
