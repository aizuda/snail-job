package com.aizuda.easy.retry.client.common.report;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author: xiaowoniu
 * @date : 2024-03-21
 * @since : 3.2.0
 */
public final class LogReportFactory {

   private static final List<LogReport> reports = Lists.newArrayList();

   static void add(LogReport logReport) {
       reports.add(logReport);
   }

    public static LogReport get() {

        for (final LogReport report : reports) {
            if (report.supports()) {
                return report;
            }
        }

        return null;
    }

}
