package com.aizuda.easy.retry.client.core.init;

import com.aizuda.easy.retry.client.core.Lifecycle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 系统关闭监听器
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-19 19:00
 * @since 1.0.0
 */
@Component
@Slf4j
public class EasyRetryEndListener implements ApplicationListener<ContextClosedEvent> {

   static class Solution {

       public static void main(String[] args) {
           System.out.println(fullJustify(new String[]{"This", "is", "an", "example", "of", "text", "justification."}, 16));
       }
        public static List<String> fullJustify(String[] words, int maxWidth) {
            List<String> result = new ArrayList<>();

            int n = words.length;
            int start = 0; // 当前行的起始单词索引

            while (start < n) {
                int end = start; // 当前行的结束单词索引
                int lineLength = 0; // 当前行的字符总长度

                // 找到当前行可以容纳的最多单词
                while (end < n && lineLength + words[end].length() + (end - start) <= maxWidth) {
                    lineLength += words[end].length();
                    end++;
                }

                int numWords = end - start; // 当前行的单词数量
                int numSpaces = maxWidth - lineLength; // 当前行需要插入的空格总数

                StringBuilder sb = new StringBuilder();

                // 处理特殊情况：只有一个单词或是最后一行
                if (numWords == 1 || end == n) {
                    for (int i = start; i < end; i++) {
                        sb.append(words[i]);
                        if (i < end - 1) {
                            sb.append(" ");
                        }
                    }
                    int remainingSpaces = maxWidth - sb.length();
                    while (remainingSpaces > 0) {
                        sb.append(" ");
                        remainingSpaces--;
                    }
                } else {
                    int spacesPerWord = numSpaces / (numWords - 1); // 单词间的平均空格数
                    int extraSpaces = numSpaces % (numWords - 1); // 需要额外添加的空格数

                    for (int i = start; i < end; i++) {
                        sb.append(words[i]);

                        if (i < end - 1) {
                            int spacesCount = spacesPerWord;
                            if (extraSpaces > 0) {
                                spacesCount++;
                                extraSpaces--;
                            }
                            for (int j = 0; j < spacesCount; j++) {
                                sb.append(" ");
                            }
                        }
                    }
                }

                result.add(sb.toString());
                start = end;
            }

            return result;
        }
    }

    @Autowired
    private List<Lifecycle> lifecycleList;

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        log.info("Easy-Retry client about to shutdown");
        lifecycleList.forEach(Lifecycle::close);
        log.info("Easy-Retry client closed successfully");
    }
}
