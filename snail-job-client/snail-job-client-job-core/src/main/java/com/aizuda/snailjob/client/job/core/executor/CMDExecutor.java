package com.aizuda.snailjob.client.job.core.executor;

import java.nio.charset.Charset;


public class CMDExecutor extends AbstractScriptExecutor {

    @Override
    protected String getScriptName(Long taskBatchId) {
        return String.format("cmd_%d.bat", taskBatchId);
    }

    @Override
    protected String getRunCommand() {
        return "cmd.exe";
    }

    @Override
    protected Charset getCharset() {
        return Charset.defaultCharset();
    }
}
