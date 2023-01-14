package com.x.retry.common.core.alarm;

import cn.hutool.extra.mail.MailAccount;
import com.x.retry.common.core.log.LogUtils;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: www.byteblogs.com
 * @date : 2022-05-04 16:13
 */
@Data
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

            LogUtils.warn("邮箱地址: [{}] 地址有误", s);

        }

        return emailList;
    }
}
