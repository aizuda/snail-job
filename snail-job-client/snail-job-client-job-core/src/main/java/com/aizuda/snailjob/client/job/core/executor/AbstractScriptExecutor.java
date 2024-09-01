package com.aizuda.snailjob.client.job.core.executor;

import com.aizuda.snailjob.client.common.config.SnailJobProperties;
import com.aizuda.snailjob.common.core.util.SnailJobFileUtil;
import com.aizuda.snailjob.common.core.util.SnailJobSystemUtil;
import com.aizuda.snailjob.client.model.ExecuteResult;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.exception.SnailJobInnerExecutorException;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.google.common.collect.Sets;
import io.micrometer.common.util.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractScriptExecutor {

    private static final Set<String> DOWNLOAD_PROTOCOL = Sets.newHashSet("http", "https", "ftp");
    // 将所有协议组合成一个正则表达式，使用 "|" 分隔表示或操作
    private static final String PROTOCOLS_PATTERN = String.join("|", DOWNLOAD_PROTOCOL);
    // 编译正则表达式，创建 Pattern 对象，作为常量使用
    private static final Pattern PROTOCOL_PATTERN = Pattern.compile("^(" + PROTOCOLS_PATTERN + ")");

    protected static final String SH_SHELL = "/bin/sh";

    protected static final String CMD_SHELL = "cmd.exe";

    private static final String READ_PATH = "READPATH:";

    private static final String WORKER_DIR = SnailFileUtils.workspace() + "/script_processor/";

    protected ExecuteResult process(Long taskBatchId, String scriptParams) {
        logInfo("ScriptProcessor start to process, params: {}", scriptParams);
        if (scriptParams == null) {
            logWarn("ScriptParams is null, please check jobParam configuration.");
            return ExecuteResult.failure("ScriptParams is null.");
        }
        String scriptPath = prepareScriptFile(taskBatchId, scriptParams);
        logInfo("Generate executable file successfully, path: {}", scriptPath);

        if (SnailJobSystemUtil.isOsWindows() && SH_SHELL.equals(getRunCommand())) {
            logWarn("Current OS is {} where shell scripts cannot run.", SnailJobSystemUtil.getOsName());
            return ExecuteResult.failure("Shell scripts cannot run on Windows.");
        }

        if (!SnailJobSystemUtil.isOsWindows()) {
            setScriptPermissions(scriptPath);
        }

        return executeScript(scriptPath);
    }

    private String prepareScriptFile(Long taskBatchId, String processorInfo) {
        String scriptPath = WORKER_DIR + getScriptName(taskBatchId);
        File script = new File(scriptPath);
        scriptPath = script.getAbsolutePath();
        if (script.exists()) {
            return scriptPath;
        }
        // 创建脚本目录
        ensureScriptDirectory(script);

        // 是否是本地目录
        if (processorInfo.startsWith(READ_PATH)) {
            return handleLocalScript(script, scriptPath, processorInfo);
        }

        // 是否为下载
        // 如果是下载链接，则从网络获取
        Matcher matcher = PROTOCOL_PATTERN.matcher(processorInfo);
        if (matcher.find()) {
            try {
                SnailJobFileUtil.downloadFile(processorInfo, script, 5000, 300000);
            } catch (IOException e) {
                throw new SnailJobInnerExecutorException("[snail-job] Script download failed", e);
            }
            return scriptPath;
        }

        // 写入脚本
        try {
            writeScriptContent(script, processorInfo);
        } catch (IOException e) {
            throw new SnailJobInnerExecutorException("[snail-job] Failed to write script", e);
        }
        return scriptPath;
    }

    private String handleLocalScript(File script, String scriptPath, String processorInfo) {
        // 去掉 "READPATH:" 前缀
        String newProcessorInfo = processorInfo.substring(READ_PATH.length()).trim();
        File routhFile = new File(newProcessorInfo);

        // 判断文件是否存在
        if (routhFile.exists()) {
            // 读取文件内容并写入到 script 中
            try (BufferedReader br = new BufferedReader(new FileReader(routhFile));
                 BufferedWriter bw = new BufferedWriter(new FileWriter(script))) {
                String line;
                while ((line = br.readLine()) != null) {
                    bw.write(line);
                    bw.newLine();
                }
                bw.flush();
            } catch (IOException e) {
                throw new SnailJobInnerExecutorException("[snail-job] Local script write exception", e);
            }
            return scriptPath;
        } else {
            throw new SnailJobInnerExecutorException("File not found: {" + newProcessorInfo + "}");
        }
    }

    private void ensureScriptDirectory(File script) {
        try {
            File parentDir = script.getParentFile();
            if (!parentDir.exists()) {
                logInfo("Script directory does not exist, creating: {}", parentDir.getAbsolutePath());
                SnailJobFileUtil.mkdirs(parentDir);
            }
        } catch (SnailJobInnerExecutorException e) {
            throw new SnailJobInnerExecutorException("[snail-job] ensure script directory error", e);
        }
    }

    private void writeScriptContent(File script, String processorInfo) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(script.toPath(), getCharset())) {
            writer.write(processorInfo);
            logInfo("Script content written successfully to: {}", script.getAbsolutePath());
        }
    }

    private void setScriptPermissions(String scriptPath) {
        ProcessBuilder chmodPb = new ProcessBuilder("/bin/chmod", "755", scriptPath);
        try {
            chmodPb.start().waitFor();
        } catch (InterruptedException | IOException e) {
            throw new SnailJobInnerExecutorException("[snail-job] Failed to set script permissions", e);
        }
        logInfo("chmod 755 authorization complete, ready to start execution~");
    }

    private ExecuteResult executeScript(String scriptPath) {
        ProcessBuilder pb = getProcessBuilder(scriptPath);

        Process process = null;
        ExecuteResult executeResult;
        try {
            process = pb.start();
            executeResult = captureOutput(process);
        } catch (IOException | InterruptedException e) {
            throw new SnailJobInnerExecutorException("[snail-job] Script execution failed", e);
        } finally {
            if (process.isAlive()) {
                // 脚本执行失败 终止;
                process.destroy();
                try {
                    boolean exited = process.waitFor(5, TimeUnit.SECONDS); // 等待5秒
                    if (!exited) {
                        // 如果进程没有在5秒内终止，则强制终止
                        process.destroyForcibly();
                        process.waitFor(); // 等待进程终止
                    }
                    logWarn("Script execution failed, starting to terminate script operation");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        return executeResult;

    }

    private ProcessBuilder getProcessBuilder(String scriptPath) {
        return getRunCommand().equals(CMD_SHELL) ?
                new ProcessBuilder(getRunCommand(), "/c", scriptPath) :
                new ProcessBuilder(getRunCommand(), scriptPath);
    }

    private ExecuteResult captureOutput(Process process) throws InterruptedException {
        StringBuilder inputBuilder = new StringBuilder();
        StringBuilder errorBuilder = new StringBuilder();
        boolean success = process.waitFor() == 0;

        captureStream(process.getInputStream(), inputBuilder);
        captureStream(process.getErrorStream(), errorBuilder);

        String result = formatResult(inputBuilder, errorBuilder);
        logInfo(result);

        return success ? ExecuteResult.success("Script executed successfully.") : ExecuteResult.failure("Script execution failed.");
    }

    private void captureStream(InputStream is, StringBuilder sb) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, getCharset()))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
        } catch (Exception e) {
            logWarn("Failed to capture stream.", e);
        } finally {
            closeQuietly(is);
        }
    }

    private String formatResult(StringBuilder inputBuilder, StringBuilder errorBuilder) {
        return String.format("[INPUT]: %s;[ERROR]: %s", inputBuilder, errorBuilder);
    }

    private void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            logWarn("Failed to close stream.", e);
        }
    }

    protected abstract String getScriptName(Long instanceId);

    protected abstract String getRunCommand();

    protected Charset getCharset() {
        return StandardCharsets.UTF_8;
    }

    // Logging methods
    private void logInfo(String msg, Object... params) {
        SnailJobLog.REMOTE.info("[snail-job] " + msg, params);
    }

    private void logWarn(String msg, Object... params) {
        SnailJobLog.REMOTE.warn("[snail-job] " + msg, params);
    }

    public class SnailFileUtils {

        /**
         * 获取工作目录
         *
         * @return 允许用户通过启动配置文件自定义存储目录，默认为 user.home
         */
        public static String workspace() {
            SnailJobProperties snailJobProperties = SnailSpringContext.getBean(SnailJobProperties.class);
            String workspaceByDKey = snailJobProperties.getWorkspace();
            if (StringUtils.isNotEmpty(workspaceByDKey)) {
                SnailJobLog.LOCAL.info("[FileUtils] [workspace] use custom workspace: {}", workspaceByDKey);
                return workspaceByDKey;
            }
            final String userHome = System.getProperty("user.home").concat("/snailJob/worker");
            SnailJobLog.LOCAL.info("[FileUtils] [workspace] use user.home as workspace: {}", userHome);
            return userHome;
        }
    }
}
