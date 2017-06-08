package com.kairui.spider.controller.sendMail;

import com.kairui.spider.domain.commons.Result;
import com.kairui.spider.service.sendMail.EmailService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/send/mail")
public class SendMailController {

    @Resource
    private EmailService service;

    @PostMapping
    public Result send() {
        Result result = new Result();
        try {
            service.sendEmail();
            result.setMsg("邮件发送成功");
        } catch (Exception ex) {
            result.setError(ex.getMessage() != null ? ex.getMessage() : ex.toString());
            ex.printStackTrace();
        }
        return result;
    }
}
