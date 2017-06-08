package com.kairui.spider.core.utils;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.HttpResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.aliyuncs.sms.model.v20160927.SingleSendSmsRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 发送短信工具类
 * <p>
 * 短信验证码：使用同一个签名，对同一个手机号码发送短信验证码，支持1条/分钟，累计7条/小时；
 * 短信通知：使用同一个签名和同一个短信模板ID，对同一个手机号码发送短信通知，支持50条/日；
 * 推广短信：使用同一个签名和同一个短信模板ID，对同一个手机号码发送短信通知，支持50条/日；
 */
public class SmsUtils {

    private static Log log = LogFactory.getLog(SmsUtils.class);

    private static List<UserParam> userList = new ArrayList<>();

    static {
        //每分钟执行一次检查
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                removeVerifyCode();
            }
        }, 60000, 60000);
    }

    /**
     * 给手机发送验证码
     *
     * @param tel  手机号
     * @param code 验证码
     */
    public static Boolean sendVerifyCode(String tel, String code) {
        Boolean flag = true;
        try {
            IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", "LTAI7NXjopBcCTMO", "i4KrNE1fiGK3DU9qC2to6wz8AL16lX");
            DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", "Sms", "sms.aliyuncs.com");
            IAcsClient client = new DefaultAcsClient(profile);
            SingleSendSmsRequest request = new SingleSendSmsRequest();
            request.setSignName("恺睿科技");
            request.setTemplateCode("SMS_34695111");
            request.setParamString("{\"code\":\"" + code + "\"}");
            request.setRecNum(tel);
            HttpResponse response = client.doAction(request);
            if (!response.isSuccess()) {
                log.error("号码【" + tel + "】验证码发送失败，错误代码：" + response.getStatus());
                flag = false;
            }
        } catch (ClientException e) {
            e.printStackTrace();
            log.error("号码【" + tel + "】验证码发送失败，接口调用出错!");
            flag = false;
        }
        if (flag) {
            removeVerifyCode(tel);
            UserParam param = new UserParam();
            param.setTel(tel);
            param.setCode(code);
            param.setCtime(new Date());
            userList.add(param);
        }
        return flag;
    }

    /**
     * 获取验证码
     *
     * @param tel 手机号
     * @return 验证码，如果为null说明没有生成验证码
     */
    public static String getVerifyCode(String tel) {
        if (tel == null || tel.isEmpty()) return null;
        String code = null;
        for (UserParam param : userList) {
            if (param.getTel().equals(tel)) {
                code = param.getCode();
                break;
            }
        }
        return code;
    }

    /**
     * 删除验证码，释放资源
     * 有效期3分钟
     */
    private static void removeVerifyCode() {
        if (userList == null || userList.size() <= 0) return;
        List<UserParam> delList = userList.stream().filter(temp -> getSecsBetween(temp.getCtime(), new Date()) > 180).collect(Collectors.toList());
        if (delList.size() > 0) userList.removeAll(delList);
    }

    /**
     * 删除手机号对应的验证码
     */
    private static void removeVerifyCode(String tel) {
        if (userList == null || userList.size() <= 0) return;
        for (UserParam temp : userList) {
            if (temp.getTel().equals(tel)) {
                userList.remove(temp);
                break;
            }
        }
    }

    /**
     * 计算两个日期之间相差的秒数
     *
     * @param smdate 较小的时间
     * @param bdate  较大的时间
     * @return 相差秒数
     */
    private static int getSecsBetween(Date smdate, Date bdate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            smdate = sdf.parse(sdf.format(smdate));
            bdate = sdf.parse(sdf.format(bdate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(smdate);
        long time1 = cal.getTimeInMillis();
        cal.setTime(bdate);
        long time2 = cal.getTimeInMillis();
        long between_days = (time2 - time1) / 1000;

        return Math.abs(Integer.parseInt(String.valueOf(between_days)));
    }

    private static class UserParam {

        private String tel;

        private String code;

        private Date ctime;

        String getTel() {
            return tel;
        }

        void setTel(String tel) {
            this.tel = tel;
        }

        String getCode() {
            return code;
        }

        void setCode(String code) {
            this.code = code;
        }

        Date getCtime() {
            return ctime;
        }

        void setCtime(Date ctime) {
            this.ctime = ctime;
        }
    }
}
