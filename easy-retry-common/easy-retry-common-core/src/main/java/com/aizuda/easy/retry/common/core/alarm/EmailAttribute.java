package com.aizuda.easy.retry.common.core.alarm;

import cn.hutool.extra.mail.MailAccount;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: opensnail
 * @date : 2022-05-04 16:13
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class EmailAttribute extends MailAccount {

    private static final String EMAIL_PATTERN = "^[A-Za-z0-9-._]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private String tos;

    public List<String> getTos() {
        String[] split = tos.split(";");
        Pattern p = Pattern.compile(EMAIL_PATTERN);

        List<String> emailList = new ArrayList<>();
        for (String s : split) {
            Matcher m = p.matcher(s);
            if (m.matches()) {
                emailList.add(s);
                continue;
            }

           EasyRetryLog.LOCAL.warn("邮箱地址: [{}] 地址有误", s);
        }

        return emailList;
    }
}
