package com.aizuda.snailjob.client.job.core.executor.builtin;


import com.aizuda.snailjob.client.common.config.SnailJobProperties;

public abstract class AbstractShellExecutor extends AbstractScriptExecutor {

    public AbstractShellExecutor(SnailJobProperties snailJobProperties) {
        super(snailJobProperties);
    }

    @Override
    protected String getScriptName(Long jobId) {
        return String.format("shell_%d.sh", jobId);
    }

    @Override
    protected String getRunCommand() {
        return SH_SHELL;
    }

    @Override
    protected ProcessBuilder getScriptProcessBuilder(String scriptPath) {
        return new ProcessBuilder("sh", scriptPath);
    }
}
