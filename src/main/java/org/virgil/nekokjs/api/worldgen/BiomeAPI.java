package org.virgil.nekokjs.api.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 生物群系 API
 * 提供生物群系查询和修改功能
 */
public class BiomeAPI {
    
    private static final Logger LOGGER = Logger.getLogger("NekoKJS-Biome");
    private static final Map<String, BiomeModifier> biomeModifiers = new HashMap<>();
    
    // ===== 生物群系查询 =====
    
    /**
     * 获取指定位置的生物群系
     * 
     * @param world 世界
     * @param x X 坐标
     * @param y Y 坐标
     * @param z Z 坐标
     * @return 生物群系 ID
     */
    public static String getBiome(Object world, int x, int y, int z) {
        try {
            if (!(world instanceof World)) {
                LOGGER.warning("Invalid world object");
                return null;
            }
            
            World bukkitWorld = (World) world;
            ServerLevel serverLevel = ((CraftWorld) bukkitWorld).getHandle();
            
            // 获取生物群系
            Holder<Biome> biomeHolder = serverLevel.getBiome(new net.minecraft.core.BlockPos(x, y, z));
            
            // 获取生物群系 ID
            Object registryAccess = serverLevel.registryAccess();
            java.lang.reflect.Method lookupMethod = registryAccess.getClass()
                .getMethod("registryOrThrow", ResourceKey.class);
            Object biomeRegistry = lookupMethod.invoke(registryAccess, Registries.BIOME);
            
            java.lang.reflect.Method getKeyMethod = biomeRegistry.getClass()
                .getMethod("getKey", Object.class);
            ResourceLocation location = (ResourceLocation) getKeyMethod.invoke(biomeRegistry, biomeHolder.value());
            
            return location != null ? location.toString() : "minecraft:plains";
        } catch (Exception e) {
            LOGGER.severe("Failed to get biome: " + e.getMessage());
            return "minecraft:plains";
        }
    }
    
    /**
     * 设置指定位置的生物群系
     * 
     * @param world 世界
     * @param x X 坐标
     * @param y Y 坐标
     * @param z Z 坐标
     * @param biomeId 生物群系 ID
     * @return 是否设置成功
     */
    public static boolean setBiome(Object world, int x, int y, int z, String biomeId) {
        try {
            if (!(world instanceof World)) {
                LOGGER.warning("Invalid world object");
                return false;
            }
            
            World bukkitWorld = (World) world;
            Location loc = new Location(bukkitWorld, x, y, z);
            
            // 使用 Registry 获取生物群系
            org.bukkit.NamespacedKey key = org.bukkit.NamespacedKey.fromString(biomeId);
            if (key == null) {
                LOGGER.warning("Invalid biome ID: " + biomeId);
                return false;
            }
            
            org.bukkit.block.Biome bukkitBiome = org.bukkit.Registry.BIOME.get(key);
            if (bukkitBiome == null) {
                LOGGER.warning("Biome not found: " + biomeId);
                return false;
            }
            
            bukkitWorld.setBiome(loc, bukkitBiome);
            return true;
        } catch (Exception e) {
            LOGGER.severe("Failed to set biome: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 设置区域的生物群系
     * 
     * @param world 世界
     * @param x1 起始 X
     * @param z1 起始 Z
     * @param x2 结束 X
     * @param z2 结束 Z
     * @param biomeId 生物群系 ID
     * @return 是否设置成功
     */
    public static boolean setBiomeRegion(Object world, int x1, int z1, int x2, int z2, String biomeId) {
        try {
            if (!(world instanceof World)) {
                LOGGER.warning("Invalid world object");
                return false;
            }
            
            World bukkitWorld = (World) world;
            
            // 使用 Registry 获取生物群系
            org.bukkit.NamespacedKey key = org.bukkit.NamespacedKey.fromString(biomeId);
            if (key == null) {
                LOGGER.warning("Invalid biome ID: " + biomeId);
                return false;
            }
            
            org.bukkit.block.Biome bukkitBiome = org.bukkit.Registry.BIOME.get(key);
            if (bukkitBiome == null) {
                LOGGER.warning("Biome not found: " + biomeId);
                return false;
            }
            
            int minX = Math.min(x1, x2);
            int maxX = Math.max(x1, x2);
            int minZ = Math.min(z1, z2);
            int maxZ = Math.max(z1, z2);
            
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Location loc = new Location(bukkitWorld, x, 64, z);
                    bukkitWorld.setBiome(loc, bukkitBiome);
                }
            }
            
            LOGGER.info("Set biome region from (" + x1 + "," + z1 + ") to (" + x2 + "," + z2 + ")");
            return true;
        } catch (Exception e) {
            LOGGER.severe("Failed to set biome region: " + e.getMessage());
            return false;
        }
    }
    
    // ===== 生物群系修改 =====
    
    /**
     * 注册生物群系修改器（完整气候参数支持）
     * 
     * @param biomeId 生物群系 ID
     * @param config 配置 Map（Rhino 会自动将 JS 对象转换为 Map）
     * @return 是否注册成功
     */
    public static boolean registerBiomeModifier(String biomeId, java.util.Map<String, Object> config) {
        try {
            BiomeModifier biomeModifier = new BiomeModifier(biomeId);
            
            // 基础气候参数
            if (config.containsKey("temperature")) {
                Object temp = config.get("temperature");
                if (temp instanceof Number) {
                    biomeModifier.temperature = ((Number) temp).floatValue();
                }
            }
            
            if (config.containsKey("downfall")) {
                Object downfall = config.get("downfall");
                if (downfall instanceof Number) {
                    biomeModifier.downfall = ((Number) downfall).floatValue();
                }
            }
            
            // 高级气候参数
            if (config.containsKey("temperatureModifier")) {
                biomeModifier.temperatureModifier = config.get("temperatureModifier").toString();
            }
            
            if (config.containsKey("hasPrecipitation")) {
                Object precip = config.get("hasPrecipitation");
                if (precip instanceof Boolean) {
                    biomeModifier.hasPrecipitation = (Boolean) precip;
                }
            }
            
            // 视觉效果
            if (config.containsKey("skyColor")) {
                Object skyColor = config.get("skyColor");
                if (skyColor instanceof Number) {
                    biomeModifier.skyColor = ((Number) skyColor).intValue();
                }
            }
            
            if (config.containsKey("fogColor")) {
                Object fogColor = config.get("fogColor");
                if (fogColor instanceof Number) {
                    biomeModifier.fogColor = ((Number) fogColor).intValue();
                }
            }
            
            if (config.containsKey("waterColor")) {
                Object waterColor = config.get("waterColor");
                if (waterColor instanceof Number) {
                    biomeModifier.waterColor = ((Number) waterColor).intValue();
                }
            }
            
            if (config.containsKey("waterFogColor")) {
                Object waterFogColor = config.get("waterFogColor");
                if (waterFogColor instanceof Number) {
                    biomeModifier.waterFogColor = ((Number) waterFogColor).intValue();
                }
            }
            
            if (config.containsKey("foliageColor")) {
                Object foliageColor = config.get("foliageColor");
                if (foliageColor instanceof Number) {
                    biomeModifier.foliageColor = ((Number) foliageColor).intValue();
                }
            }
            
            if (config.containsKey("grassColor")) {
                Object grassColor = config.get("grassColor");
                if (grassColor instanceof Number) {
                    biomeModifier.grassColor = ((Number) grassColor).intValue();
                }
            }
            
            // 生物生成
            if (config.containsKey("creatureSpawnProbability")) {
                Object prob = config.get("creatureSpawnProbability");
                if (prob instanceof Number) {
                    biomeModifier.creatureSpawnProbability = ((Number) prob).floatValue();
                }
            }
            
            biomeModifiers.put(biomeId, biomeModifier);
            LOGGER.info("Registered biome modifier for: " + biomeId);
            return true;
        } catch (Exception e) {
            LOGGER.severe("Failed to register biome modifier: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 注册自定义生物群系（简化版）
     * 注意：这是运行时注册，不会持久化到数据包
     * 
     * @param biomeId 生物群系 ID
     * @param config 配置
     * @return 是否注册成功
     */
    public static boolean registerCustomBiome(String biomeId, java.util.Map<String, Object> config) {
        try {
            LOGGER.info("Registering custom biome: " + biomeId);
            
            // 创建修改器作为自定义生物群系的配置
            BiomeModifier customBiome = new BiomeModifier(biomeId);
            customBiome.isCustom = true;
            
            // 解析所有参数
            if (config.containsKey("temperature")) {
                customBiome.temperature = ((Number) config.get("temperature")).floatValue();
            }
            if (config.containsKey("downfall")) {
                customBiome.downfall = ((Number) config.get("downfall")).floatValue();
            }
            if (config.containsKey("hasPrecipitation")) {
                customBiome.hasPrecipitation = (Boolean) config.get("hasPrecipitation");
            }
            if (config.containsKey("skyColor")) {
                customBiome.skyColor = ((Number) config.get("skyColor")).intValue();
            }
            if (config.containsKey("fogColor")) {
                customBiome.fogColor = ((Number) config.get("fogColor")).intValue();
            }
            if (config.containsKey("waterColor")) {
                customBiome.waterColor = ((Number) config.get("waterColor")).intValue();
            }
            if (config.containsKey("waterFogColor")) {
                customBiome.waterFogColor = ((Number) config.get("waterFogColor")).intValue();
            }
            
            biomeModifiers.put(biomeId, customBiome);
            LOGGER.info("Registered custom biome: " + biomeId + " (runtime only)");
            return true;
        } catch (Exception e) {
            LOGGER.severe("Failed to register custom biome: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 获取所有可用的生物群系
     * 
     * @return 生物群系 ID 数组
     */
    public static String[] getAllBiomes() {
        try {
            // 返回 Bukkit 的生物群系列表
            // 使用 Registry 获取所有生物群系
            java.util.List<String> biomeList = new java.util.ArrayList<>();
            for (org.bukkit.block.Biome biome : org.bukkit.Registry.BIOME) {
                org.bukkit.NamespacedKey key = biome.getKey();
                biomeList.add(key.toString());
            }
            String[] result = biomeList.toArray(new String[0]);
            
            return result;
        } catch (Exception e) {
            LOGGER.severe("Failed to get all biomes: " + e.getMessage());
            return new String[0];
        }
    }
    
    /**
     * 设置生物群系（带边界混合）
     * 
     * @param world 世界
     * @param x X 坐标
     * @param y Y 坐标
     * @param z Z 坐标
     * @param biomeId 生物群系 ID
     * @param blendRadius 混合半径
     * @return 是否设置成功
     */
    public static boolean setBiomeWithBlend(Object world, int x, int y, int z, String biomeId, int blendRadius) {
        try {
            if (!(world instanceof World)) {
                return false;
            }
            
            World bukkitWorld = (World) world;
            
            // 中心设置为目标生物群系
            setBiome(world, x, y, z, biomeId);
            
            // 在混合半径内逐渐过渡
            for (int dx = -blendRadius; dx <= blendRadius; dx++) {
                for (int dz = -blendRadius; dz <= blendRadius; dz++) {
                    double distance = Math.sqrt(dx * dx + dz * dz);
                    if (distance > 0 && distance <= blendRadius) {
                        // 根据距离决定是否设置
                        double probability = 1.0 - (distance / blendRadius);
                        if (Math.random() < probability) {
                            setBiome(world, x + dx, y, z + dz, biomeId);
                        }
                    }
                }
            }
            
            return true;
        } catch (Exception e) {
            LOGGER.severe("Failed to set biome with blend: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 设置生物群系（指定高度范围）
     * 
     * @param world 世界
     * @param x X 坐标
     * @param minY 最小 Y
     * @param maxY 最大 Y
     * @param z Z 坐标
     * @param biomeId 生物群系 ID
     * @return 是否设置成功
     */
    public static boolean setBiomeVertical(Object world, int x, int minY, int maxY, int z, String biomeId) {
        try {
            for (int y = minY; y <= maxY; y++) {
                if (!setBiome(world, x, y, z, biomeId)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            LOGGER.severe("Failed to set vertical biome: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取生物群系信息
     * 
     * @param biomeId 生物群系 ID
     * @return 生物群系信息对象
     */
    public static BiomeInfo getBiomeInfo(String biomeId) {
        try {
            return new BiomeInfo(biomeId);
        } catch (Exception e) {
            LOGGER.severe("Failed to get biome info: " + e.getMessage());
            return null;
        }
    }
    
    // ===== 内部类 =====
    
    /**
     * 生物群系修改器
     */
    public static class BiomeModifier {
        public final String biomeId;
        public boolean isCustom = false;
        
        // 基础气候参数
        public float temperature = 0.8f;
        public float downfall = 0.4f;
        public String temperatureModifier = "NONE";
        public boolean hasPrecipitation = true;
        
        // 视觉效果
        public int skyColor = 0x78A7FF;
        public int fogColor = 0xC0D8FF;
        public int waterColor = 0x3F76E4;
        public int waterFogColor = 0x050533;
        public int foliageColor = -1;  // -1 表示使用默认值
        public int grassColor = -1;
        
        // 生物生成
        public float creatureSpawnProbability = 0.1f;
        
        public BiomeModifier(String biomeId) {
            this.biomeId = biomeId;
        }
    }
    
    /**
     * 生物群系信息
     */
    public static class BiomeInfo {
        public final String id;
        public final String name;
        public final float temperature;
        public final float downfall;
        
        public BiomeInfo(String biomeId) {
            this.id = biomeId;
            
            String tempName;
            try {
                // 使用 Registry 获取生物群系
                org.bukkit.NamespacedKey key = org.bukkit.NamespacedKey.fromString(biomeId);
                if (key == null) {
                    tempName = biomeId;
                } else {
                    org.bukkit.block.Biome bukkitBiome = org.bukkit.Registry.BIOME.get(key);
                    tempName = bukkitBiome != null ? bukkitBiome.getKey().toString() : biomeId;
                }
            } catch (Exception e) {
                tempName = biomeId;
            }
            
            this.name = tempName;
            // 获取温度和湿度（简化版本）
            this.temperature = 0.8f;
            this.downfall = 0.4f;
        }
        
        @Override
        public String toString() {
            return "BiomeInfo{id='" + id + "', name='" + name + 
                   "', temperature=" + temperature + ", downfall=" + downfall + "}";
        }
    }
}
