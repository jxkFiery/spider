package com.kairui.spider.service.internet;

import com.kairui.spider.domain.internet.Domains;
import com.kairui.spider.repository.internet.DomainsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class DomainsService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Resource
    private DomainsRepository repository;

    public Domains create(Domains model) {
        List<Domains> list = repository.findByDomain(model.getDomain());
        if (list != null && list.size() > 0) {
            log.info("域名重复：{}", model.getDomain());
        } else {
            model.setId(UUID.randomUUID().toString().replaceAll("-", ""));
            model.setCtime(new Date());
            repository.save(model);
            log.info("创建新域名：{}", model.getDomain());
        }
        return model;
    }

    public Domains save(Domains model) {
        Domains temp = repository.findOne(model.getId());
        Assert.notNull(temp, "接口不存在");

        temp.setDomain(model.getDomain());
        temp.setUrl(model.getUrl());
        temp.setEmails(model.getEmails());
        temp.setTitle(model.getTitle());
        temp.setSource(model.getSource());
        repository.save(temp);

        log.info("接口 {} 信息更改", temp.getId());

        return temp;
    }

    public void delete(String id) {
        Domains model = repository.findOne(id);
        Assert.notNull(model, "域名不存在");
        repository.delete(id);
        log.info("删除域名：{}", model.getDomain());
    }

    public Domains findOne(String id) {
        Domains temp = repository.findOne(id);
        Assert.notNull(temp, "域名不存在");

        return temp;
    }

    public Page<Domains> findAll(PageRequest page, Domains model) {
        if (model.getDomain() != null) return repository.findByDomain(page, model.getDomain());
        else return repository.findByIdNotNull(page);
    }


}
