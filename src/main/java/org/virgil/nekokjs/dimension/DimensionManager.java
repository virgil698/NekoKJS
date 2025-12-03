package org.virgil.nekokjs.dimension;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 维度管理器
 * 负责维度配置的存储、加载和管理
 */
public class DimensionManager {
    
    private static final Logger LOGGER = Logger.getLogger("NekoKJS-Dimension");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    private final Plugin plugin;
    private final File dimensionsFolder;
    private final Map<String, DimensionConfig> dimensions;
    private final List<DimensionConfig> pendingDimensions;
    
    public DimensionManager(Plugin plugin) {
        this.plugin = plugin;
        this.dimensionsFolder = new File(plugin.getDataFolder(), "dimensions");
        this.dimensions = new HashMap<>();
        this.pendingDimensions = new ArrayList<>();
        
        // 创建维度配置文件夹
        if (!dimensionsFolder.exists()) {
            dimensionsFolder.mkdirs();
        }
    }
    
    /**
     * 加载所有维度配置
     */
    public void loadDimensions() {
        dimensions.clear();
        
        File[] files = dimensionsFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null || files.length == 0) {
            LOGGER.info("No custom dimensions found.");
            return;
        }
        
        for (File file : files) {
            try (FileReader reader = new FileReader(file)) {
                DimensionConfig config = GSON.fromJson(reader, DimensionConfig.class);
                
                if (config.validate()) {
                    dimensions.put(config.getDimensionId(), config);
                    pendingDimensions.add(config);
                    LOGGER.info("Loaded dimension config: " + config.getDimensionId());
                } else {
                    LOGGER.warning("Invalid dimension config in file: " + file.getName());
                }
            } catch (IOException e) {
                LOGGER.severe("Failed to load dimension config from " + file.getName() + ": " + e.getMessage());
            }
        }
        
        LOGGER.info("Loaded " + dimensions.size() + " custom dimension(s).");
    }
    
    /**
     * 保存维度配置
     */
    public boolean saveDimension(DimensionConfig config) {
        if (!config.validate()) {
            LOGGER.warning("Cannot save invalid dimension config: " + config.getDimensionId());
            return false;
        }
        
        String fileName = config.getDimensionId().replace(":", "_") + ".json";
        File file = new File(dimensionsFolder, fileName);
        
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(config, writer);
            dimensions.put(config.getDimensionId(), config);
            LOGGER.info("Saved dimension config: " + config.getDimensionId());
            return true;
        } catch (IOException e) {
            LOGGER.severe("Failed to save dimension config: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 注册维度（运行时）
     */
    public boolean registerDimension(DimensionConfig config) {
        if (!config.validate()) {
            LOGGER.warning("Cannot register invalid dimension: " + config.getDimensionId());
            return false;
        }
        
        if (dimensions.containsKey(config.getDimensionId())) {
            LOGGER.warning("Dimension already registered: " + config.getDimensionId());
            return false;
        }
        
        // 保存配置
        if (!saveDimension(config)) {
            return false;
        }
        
        // 添加到待处理列表（需要重启服务器才能生效）
        pendingDimensions.add(config);
        LOGGER.info("Dimension registered (requires restart): " + config.getDimensionId());
        
        return true;
    }
    
    /**
     * 获取维度配置
     */
    public DimensionConfig getDimension(String dimensionId) {
        return dimensions.get(dimensionId);
    }
    
    /**
     * 获取所有维度配置
     */
    public Map<String, DimensionConfig> getAllDimensions() {
        return new HashMap<>(dimensions);
    }
    
    /**
     * 获取待加载的维度列表
     * 用于 Mixin 在服务器启动时注入
     */
    public List<DimensionConfig> getPendingDimensions() {
        return new ArrayList<>(pendingDimensions);
    }
    
    /**
     * 清空待加载列表
     */
    public void clearPendingDimensions() {
        pendingDimensions.clear();
    }
    
    /**
     * 删除维度配置
     */
    public boolean deleteDimension(String dimensionId) {
        DimensionConfig config = dimensions.remove(dimensionId);
        if (config == null) {
            LOGGER.warning("Dimension not found: " + dimensionId);
            return false;
        }
        
        String fileName = dimensionId.replace(":", "_") + ".json";
        File file = new File(dimensionsFolder, fileName);
        
        if (file.exists() && file.delete()) {
            LOGGER.info("Deleted dimension config: " + dimensionId);
            return true;
        } else {
            LOGGER.warning("Failed to delete dimension config file: " + fileName);
            return false;
        }
    }
}
