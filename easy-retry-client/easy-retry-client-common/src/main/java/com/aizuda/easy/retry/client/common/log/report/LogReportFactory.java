package com.aizuda.easy.retry.client.common.log.report;

import com.aizuda.easy.retry.client.common.LogReport;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author: xiaowoniu
 * @date : 2024-03-21
 * @since : 3.2.0
 */
public final class LogReportFactory {

   private static final List<LogReport> REPORTS = Lists.newArrayList();
    private LogReportFactory() {
    }

    static void add(LogReport logReport) {
        REPORTS.add(logReport);
   }

    public static LogReport get() {

        for (final LogReport report : REPORTS) {
            if (report.supports()) {
                return report;
            }
        }

        return null;
    }

}
