package org.virgil.nekokjs.dimension;

import java.util.OptionalLong;

/**
 * 维度类型配置
 */
public class DimensionTypeConfig {
    
    private OptionalLong fixedTime;
    private boolean hasSkyLight;
    private boolean hasCeiling;
    private boolean ultraWarm;
    private boolean natural;
    private double coordinateScale;
    private boolean bedWorks;
    private boolean respawnAnchorWorks;
    private int minY;
    private int height;
    private int logicalHeight;
    private float ambientLight;
    private String infiniburn;
    private String effectsLocation;
    
    public DimensionTypeConfig() {
        // 默认值（类似主世界）
        this.fixedTime = OptionalLong.empty();
        this.hasSkyLight = true;
        this.hasCeiling = false;
        this.ultraWarm = false;
        this.natural = true;
        this.coordinateScale = 1.0;
        this.bedWorks = true;
        this.respawnAnchorWorks = false;
        this.minY = -64;
        this.height = 384;
        this.logicalHeight = 384;
        this.ambientLight = 0.0f;
        this.infiniburn = "#minecraft:infiniburn_overworld";
        this.effectsLocation = "minecraft:overworld";
    }
    
    // Getters and Setters
    
    public OptionalLong getFixedTime() {
        return fixedTime;
    }
    
    public void setFixedTime(Long time) {
        this.fixedTime = time == null ? OptionalLong.empty() : OptionalLong.of(time);
    }
    
    public boolean hasSkyLight() {
        return hasSkyLight;
    }
    
    public void setHasSkyLight(boolean hasSkyLight) {
        this.hasSkyLight = hasSkyLight;
    }
    
    public boolean hasCeiling() {
        return hasCeiling;
    }
    
    public void setHasCeiling(boolean hasCeiling) {
        this.hasCeiling = hasCeiling;
    }
    
    public boolean isUltraWarm() {
        return ultraWarm;
    }
    
    public void setUltraWarm(boolean ultraWarm) {
        this.ultraWarm = ultraWarm;
    }
    
    public boolean isNatural() {
        return natural;
    }
    
    public void setNatural(boolean natural) {
        this.natural = natural;
    }
    
    public double getCoordinateScale() {
        return coordinateScale;
    }
    
    public void setCoordinateScale(double coordinateScale) {
        this.coordinateScale = coordinateScale;
    }
    
    public boolean bedWorks() {
        return bedWorks;
    }
    
    public void setBedWorks(boolean bedWorks) {
        this.bedWorks = bedWorks;
    }
    
    public boolean respawnAnchorWorks() {
        return respawnAnchorWorks;
    }
    
    public void setRespawnAnchorWorks(boolean respawnAnchorWorks) {
        this.respawnAnchorWorks = respawnAnchorWorks;
    }
    
    public int getMinY() {
        return minY;
    }
    
    public void setMinY(int minY) {
        this.minY = minY;
    }
    
    public int getHeight() {
        return height;
    }
    
    public void setHeight(int height) {
        this.height = height;
    }
    
    public int getLogicalHeight() {
        return logicalHeight;
    }
    
    public void setLogicalHeight(int logicalHeight) {
        this.logicalHeight = logicalHeight;
    }
    
    public float getAmbientLight() {
        return ambientLight;
    }
    
    public void setAmbientLight(float ambientLight) {
        this.ambientLight = ambientLight;
    }
    
    public String getInfiniburn() {
        return infiniburn;
    }
    
    public void setInfiniburn(String infiniburn) {
        this.infiniburn = infiniburn;
    }
    
    public String getEffectsLocation() {
        return effectsLocation;
    }
    
    public void setEffectsLocation(String effectsLocation) {
        this.effectsLocation = effectsLocation;
    }
    
    /**
     * 验证配置是否有效
     */
    public boolean validate() {
        if (height < 16) {
            return false;
        }
        if (height % 16 != 0) {
            return false;
        }
        if (minY % 16 != 0) {
            return false;
        }
        if (minY + height > 2032) { // MAX_Y + 1
            return false;
        }
        if (logicalHeight > height) {
            return false;
        }
        return true;
    }
}
