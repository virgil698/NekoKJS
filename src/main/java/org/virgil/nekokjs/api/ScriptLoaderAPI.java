package org.virgil.nekokjs.api;

import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.ContextFactory;
import dev.latvian.mods.rhino.Scriptable;
import org.virgil.nekokjs.script.ScriptPack;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * 脚本加载器 API
 * 用于在主脚本中加载其他模块脚本
 */
public class ScriptLoaderAPI {
    private final Logger logger;
    private final ContextFactory contextFactory;
    private final Scriptable scope;
    private final ScriptPack pack;
    private final Set<String> loadedScripts;
    
    public ScriptLoaderAPI(Logger logger, ContextFactory contextFactory, Scriptable scope, ScriptPack pack) {
        this.logger = logger;
        this.contextFactory = contextFactory;
        this.scope = scope;
        this.pack = pack;
        this.loadedScripts = new HashSet<>();
    }
    
    /**
     * 加载脚本文件
     * 
     * @param path 相对于包根目录的脚本路径，例如：
     *             - "data/utils/helper.js"
     *             - "data/events/player.js"
     * @return 脚本执行结果
     */
    public Object load(String path) {
        // 规范化路径
        path = normalizePath(path);
        
        // 检查是否已加载
        if (loadedScripts.contains(path)) {
            logger.info("脚本已加载，跳过: " + path);
            return null;
        }
        
        // 构建完整路径
        File scriptFile = new File(pack.getPackDir(), path);
        
        // 验证文件
        if (!scriptFile.exists()) {
            logger.warning("脚本文件不存在: " + path);
            return null;
        }
        
        if (!scriptFile.isFile()) {
            logger.warning("路径不是文件: " + path);
            return null;
        }
        
        // 安全检查：确保文件在包目录内
        try {
            String canonicalPackPath = pack.getPackDir().getCanonicalPath();
            String canonicalScriptPath = scriptFile.getCanonicalPath();
            
            if (!canonicalScriptPath.startsWith(canonicalPackPath)) {
                logger.warning("安全错误: 脚本路径超出包目录范围: " + path);
                return null;
            }
        } catch (IOException e) {
            logger.warning("路径验证失败: " + e.getMessage());
            return null;
        }
        
        // 加载并执行脚本
        try {
            String scriptContent = Files.readString(scriptFile.toPath());
            Context ctx = contextFactory.enter();
            
            // 标记为已加载
            loadedScripts.add(path);
            
            logger.info("加载模块脚本: " + pack.getNamespace() + ":" + path);
            
            // 在同一个 scope 中执行，共享变量和函数
            return ctx.evaluateString(scope, scriptContent, path, 1, null);
        } catch (IOException e) {
            logger.severe("读取脚本文件失败 [" + path + "]: " + e.getMessage());
            return null;
        } catch (Exception e) {
            logger.severe("执行脚本失败 [" + path + "]: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 加载脚本文件（别名）
     */
    public Object require(String path) {
        return load(path);
    }
    
    /**
     * 检查脚本是否已加载
     */
    public boolean isLoaded(String path) {
        path = normalizePath(path);
        return loadedScripts.contains(path);
    }
    
    /**
     * 获取已加载的脚本列表
     */
    public String[] getLoadedScripts() {
        return loadedScripts.toArray(new String[0]);
    }
    
    /**
     * 规范化路径
     */
    private String normalizePath(String path) {
        // 移除开头的 /
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        
        // 移除开头的 ./
        if (path.startsWith("./")) {
            path = path.substring(2);
        }
        
        // 自动添加 .js 后缀
        if (!path.endsWith(".js")) {
            path = path + ".js";
        }
        
        // 替换反斜杠为正斜杠
        path = path.replace("\\", "/");
        
        return path;
    }
}
