package com.aizuda.snail.job.common.log.dialect.logtube;

import com.aizuda.snail.job.common.log.dialect.Log;
import com.aizuda.snail.job.common.log.factory.LogFactory;
import io.github.logtube.Logtube;

/**
 * <a href="https://github.com/logtube/logtube-java">LogTube</a> log. 封装<br>
 *
 * @author wodeyangzipingpingwuqi
 */
public class LogTubeLogFactory extends LogFactory {

	public LogTubeLogFactory() {
		super("LogTube");
		checkLogExist(Logtube.class);
	}

	@Override
	public Log createLog(String name) {
		return new LogTubeLog(name);
	}

	@Override
	public Log createLog(Class<?> clazz) {
		return new LogTubeLog(clazz);
	}

}
