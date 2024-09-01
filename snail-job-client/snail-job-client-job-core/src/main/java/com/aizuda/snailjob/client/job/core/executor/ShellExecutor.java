package com.aizuda.snailjob.client.job.core.executor;


public class ShellExecutor extends AbstractScriptExecutor {

    @Override
    protected String getScriptName(Long taskBatchId) {
        return String.format("shell_%d.sh", taskBatchId);
    }

    @Override
    protected String getRunCommand() {
        return SH_SHELL;
    }
}
