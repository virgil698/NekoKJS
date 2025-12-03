package org.virgil.nekokjs.dimension;

import java.util.HashMap;
import java.util.Map;

/**
 * 维度配置
 */
public class DimensionConfig {
    
    private String dimensionId;
    private DimensionTypeConfig typeConfig;
    private String generatorType;
    private Map<String, Object> generatorSettings;
    
    public DimensionConfig(String dimensionId) {
        this.dimensionId = dimensionId;
        this.typeConfig = new DimensionTypeConfig();
        this.generatorType = "noise";
        this.generatorSettings = new HashMap<>();
    }
    
    public String getDimensionId() {
        return dimensionId;
    }
    
    public void setDimensionId(String dimensionId) {
        this.dimensionId = dimensionId;
    }
    
    public DimensionTypeConfig getTypeConfig() {
        return typeConfig;
    }
    
    public void setTypeConfig(DimensionTypeConfig typeConfig) {
        this.typeConfig = typeConfig;
    }
    
    public String getGeneratorType() {
        return generatorType;
    }
    
    public void setGeneratorType(String generatorType) {
        this.generatorType = generatorType;
    }
    
    public Map<String, Object> getGeneratorSettings() {
        return generatorSettings;
    }
    
    public void setGeneratorSettings(Map<String, Object> generatorSettings) {
        this.generatorSettings = generatorSettings;
    }
    
    /**
     * 验证配置是否有效
     */
    public boolean validate() {
        if (dimensionId == null || dimensionId.isEmpty()) {
            return false;
        }
        if (!dimensionId.contains(":")) {
            return false;
        }
        if (typeConfig == null || !typeConfig.validate()) {
            return false;
        }
        if (generatorType == null || generatorType.isEmpty()) {
            return false;
        }
        return true;
    }
}
