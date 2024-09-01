package com.aizuda.snailjob.client.job.core.executor;

import java.nio.charset.Charset;


public class PowerShellExecutor extends AbstractScriptExecutor {

    @Override
    protected String getScriptName(Long taskBatchId) {
        return String.format("powershell_%d.ps1", taskBatchId);
    }

    @Override
    protected String getRunCommand() {
        return "powershell.exe";
    }

    @Override
    protected Charset getCharset() {
        return Charset.defaultCharset();
    }
}
