package com.aizuda.snailjob.client.job.core.executor;


public class ShellExecutor extends AbstractScriptExecutor {

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
