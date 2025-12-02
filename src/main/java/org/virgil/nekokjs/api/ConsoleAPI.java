package org.virgil.nekokjs.api;

import java.util.logging.Logger;

/**
 * Console API
 * 提供脚本日志输出功能
 */
public class ConsoleAPI {
    private final Logger logger;

    public ConsoleAPI(Logger logger) {
        this.logger = logger;
    }

    public void log(Object message) {
        logger.info("[Script] " + String.valueOf(message));
    }

    public void info(Object message) {
        logger.info("[Script] " + String.valueOf(message));
    }

    public void warn(Object message) {
        logger.warning("[Script] " + String.valueOf(message));
    }

    public void error(Object message) {
        logger.severe("[Script] " + String.valueOf(message));
    }

    public void debug(Object message) {
        logger.fine("[Script] " + String.valueOf(message));
    }
}
