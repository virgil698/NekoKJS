package org.virgil.nekokjs.api.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;

import java.util.*;
import java.util.logging.Logger;

/**
 * 特征生成 API
 * 提供矿石、树木、植被等特征的生成功能
 */
public class FeatureAPI {
    
    private static final Logger LOGGER = Logger.getLogger("NekoKJS-Feature");
    private static final Map<String, OreConfig> oreConfigs = new HashMap<>();
    private static final Map<String, TreeConfig> treeConfigs = new HashMap<>();
    private static final Random random = new Random();
    
    // ===== 矿石生成 =====
    
    /**
     * 注册矿石生成配置
     * 
     * @param oreId 矿石 ID
     * @param config 配置 Map（Rhino 会自动将 JS 对象转换为 Map）
     * @return 是否注册成功
     */
    public static boolean registerOre(String oreId, java.util.Map<String, Object> config) {
        try {
            OreConfig oreConfig = new OreConfig(oreId);
            
            if (config.containsKey("block")) {
                oreConfig.block = config.get("block").toString();
            }
            
            if (config.containsKey("replaceBlock")) {
                oreConfig.replaceBlock = config.get("replaceBlock").toString();
            }
            
            if (config.containsKey("size")) {
                Object size = config.get("size");
                if (size instanceof Number) {
                    oreConfig.size = ((Number) size).intValue();
                }
            }
            
            if (config.containsKey("count")) {
                Object count = config.get("count");
                if (count instanceof Number) {
                    oreConfig.count = ((Number) count).intValue();
                }
            }
            
            if (config.containsKey("minY")) {
                Object minY = config.get("minY");
                if (minY instanceof Number) {
                    oreConfig.minY = ((Number) minY).intValue();
                }
            }
            
            if (config.containsKey("maxY")) {
                Object maxY = config.get("maxY");
                if (maxY instanceof Number) {
                    oreConfig.maxY = ((Number) maxY).intValue();
                }
            }
            
            oreConfigs.put(oreId, oreConfig);
            LOGGER.info("Registered ore config: " + oreId);
            return true;
        } catch (Exception e) {
            LOGGER.severe("Failed to register ore: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 在区块中生成矿石
     * 
     * @param world 世界
     * @param chunkX 区块 X
     * @param chunkZ 区块 Z
     * @param oreId 矿石 ID
     * @return 生成的矿石数量
     */
    public static int generateOre(Object world, int chunkX, int chunkZ, String oreId) {
        try {
            if (!(world instanceof World)) {
                LOGGER.warning("Invalid world object");
                return 0;
            }
            
            OreConfig config = oreConfigs.get(oreId);
            if (config == null) {
                LOGGER.warning("Ore config not found: " + oreId);
                return 0;
            }
            
            World bukkitWorld = (World) world;
            int generated = 0;
            
            // 在区块中生成多个矿脉
            for (int i = 0; i < config.count; i++) {
                int x = chunkX * 16 + random.nextInt(16);
                int z = chunkZ * 16 + random.nextInt(16);
                int y = config.minY + random.nextInt(config.maxY - config.minY);
                
                generated += placeOreVein(bukkitWorld, x, y, z, config);
            }
            
            return generated;
        } catch (Exception e) {
            LOGGER.severe("Failed to generate ore: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * 放置单个矿脉
     */
    private static int placeOreVein(World world, int x, int y, int z, OreConfig config) {
        int placed = 0;
        Material oreMaterial = Material.getMaterial(config.block.toUpperCase().replace("MINECRAFT:", ""));
        Material replaceMaterial = Material.getMaterial(config.replaceBlock.toUpperCase().replace("MINECRAFT:", ""));
        
        if (oreMaterial == null || replaceMaterial == null) {
            return 0;
        }
        
        // 生成球形矿脉
        for (int dx = -config.size; dx <= config.size; dx++) {
            for (int dy = -config.size; dy <= config.size; dy++) {
                for (int dz = -config.size; dz <= config.size; dz++) {
                    if (dx * dx + dy * dy + dz * dz <= config.size * config.size) {
                        if (random.nextFloat() < 0.5f) { // 50% 概率放置
                            Location loc = new Location(world, x + dx, y + dy, z + dz);
                            if (world.getBlockAt(loc).getType() == replaceMaterial) {
                                world.getBlockAt(loc).setType(oreMaterial);
                                placed++;
                            }
                        }
                    }
                }
            }
        }
        
        return placed;
    }
    
    // ===== 树木生成 =====
    
    /**
     * 注册树木生成配置
     * 
     * @param treeId 树木 ID
     * @param config 配置 Map（Rhino 会自动将 JS 对象转换为 Map）
     * @return 是否注册成功
     */
    public static boolean registerTree(String treeId, java.util.Map<String, Object> config) {
        try {
            TreeConfig treeConfig = new TreeConfig(treeId);
            
            if (config.containsKey("trunk")) {
                treeConfig.trunk = config.get("trunk").toString();
            }
            
            if (config.containsKey("leaves")) {
                treeConfig.leaves = config.get("leaves").toString();
            }
            
            if (config.containsKey("height")) {
                Object height = config.get("height");
                if (height instanceof Number) {
                    treeConfig.height = ((Number) height).intValue();
                }
            }
            
            if (config.containsKey("canopyRadius")) {
                Object radius = config.get("canopyRadius");
                if (radius instanceof Number) {
                    treeConfig.canopyRadius = ((Number) radius).intValue();
                }
            }
            
            treeConfigs.put(treeId, treeConfig);
            LOGGER.info("Registered tree config: " + treeId);
            return true;
        } catch (Exception e) {
            LOGGER.severe("Failed to register tree: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 生成树木
     * 
     * @param world 世界
     * @param x X 坐标
     * @param y Y 坐标
     * @param z Z 坐标
     * @param treeId 树木 ID
     * @return 是否生成成功
     */
    public static boolean generateTree(Object world, int x, int y, int z, String treeId) {
        try {
            if (!(world instanceof World)) {
                LOGGER.warning("Invalid world object");
                return false;
            }
            
            TreeConfig config = treeConfigs.get(treeId);
            if (config == null) {
                LOGGER.warning("Tree config not found: " + treeId);
                return false;
            }
            
            World bukkitWorld = (World) world;
            Material trunkMaterial = Material.getMaterial(config.trunk.toUpperCase().replace("MINECRAFT:", ""));
            Material leavesMaterial = Material.getMaterial(config.leaves.toUpperCase().replace("MINECRAFT:", ""));
            
            if (trunkMaterial == null || leavesMaterial == null) {
                return false;
            }
            
            // 生成树干
            for (int i = 0; i < config.height; i++) {
                Location loc = new Location(bukkitWorld, x, y + i, z);
                bukkitWorld.getBlockAt(loc).setType(trunkMaterial);
            }
            
            // 生成树冠
            int canopyY = y + config.height - 1;
            for (int dx = -config.canopyRadius; dx <= config.canopyRadius; dx++) {
                for (int dy = -2; dy <= 2; dy++) {
                    for (int dz = -config.canopyRadius; dz <= config.canopyRadius; dz++) {
                        if (dx * dx + dz * dz <= config.canopyRadius * config.canopyRadius) {
                            Location loc = new Location(bukkitWorld, x + dx, canopyY + dy, z + dz);
                            if (bukkitWorld.getBlockAt(loc).getType() == Material.AIR) {
                                bukkitWorld.getBlockAt(loc).setType(leavesMaterial);
                            }
                        }
                    }
                }
            }
            
            return true;
        } catch (Exception e) {
            LOGGER.severe("Failed to generate tree: " + e.getMessage());
            return false;
        }
    }
    
    // ===== 植被生成 =====
    
    /**
     * 生成草和花
     * 
     * @param world 世界
     * @param x X 坐标
     * @param y Y 坐标
     * @param z Z 坐标
     * @param radius 半径
     * @param density 密度 (0.0-1.0)
     * @return 生成的植被数量
     */
    public static int generateVegetation(Object world, int x, int y, int z, int radius, double density) {
        try {
            if (!(world instanceof World)) {
                LOGGER.warning("Invalid world object");
                return 0;
            }
            
            World bukkitWorld = (World) world;
            int generated = 0;
            
            Material[] plants = {
                Material.SHORT_GRASS,
                Material.TALL_GRASS,
                Material.DANDELION,
                Material.POPPY,
                Material.BLUE_ORCHID
            };
            
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dz * dz <= radius * radius && random.nextDouble() < density) {
                        Location loc = new Location(bukkitWorld, x + dx, y, z + dz);
                        Location below = new Location(bukkitWorld, x + dx, y - 1, z + dz);
                        
                        // 只在草方块上生成
                        if (bukkitWorld.getBlockAt(below).getType() == Material.GRASS_BLOCK &&
                            bukkitWorld.getBlockAt(loc).getType() == Material.AIR) {
                            Material plant = plants[random.nextInt(plants.length)];
                            bukkitWorld.getBlockAt(loc).setType(plant);
                            generated++;
                        }
                    }
                }
            }
            
            return generated;
        } catch (Exception e) {
            LOGGER.severe("Failed to generate vegetation: " + e.getMessage());
            return 0;
        }
    }
    
    // ===== 湖泊生成 =====
    
    /**
     * 生成湖泊
     * 
     * @param world 世界
     * @param x X 坐标
     * @param y Y 坐标
     * @param z Z 坐标
     * @param radius 半径
     * @param liquid 液体类型 ("water" 或 "lava")
     * @return 是否生成成功
     */
    public static boolean generateLake(Object world, int x, int y, int z, int radius, String liquid) {
        try {
            if (!(world instanceof World)) {
                LOGGER.warning("Invalid world object");
                return false;
            }
            
            World bukkitWorld = (World) world;
            Material liquidMaterial = liquid.equalsIgnoreCase("lava") ? Material.LAVA : Material.WATER;
            Material borderMaterial = liquid.equalsIgnoreCase("lava") ? Material.STONE : Material.DIRT;
            
            // 生成湖泊主体
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius/2; dy <= 0; dy++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        double distance = Math.sqrt(dx * dx + dy * dy * 2 + dz * dz);
                        if (distance <= radius) {
                            Location loc = new Location(bukkitWorld, x + dx, y + dy, z + dz);
                            
                            // 内部填充液体
                            if (distance < radius - 1) {
                                bukkitWorld.getBlockAt(loc).setType(liquidMaterial);
                            } else {
                                // 边缘使用边界材料
                                bukkitWorld.getBlockAt(loc).setType(borderMaterial);
                            }
                        }
                    }
                }
            }
            
            LOGGER.info("Generated " + liquid + " lake at " + x + ", " + y + ", " + z);
            return true;
        } catch (Exception e) {
            LOGGER.severe("Failed to generate lake: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // ===== 自定义特征 =====
    
    /**
     * 注册自定义特征
     * 
     * @param featureId 特征 ID
     * @param config 配置
     * @return 是否注册成功
     */
    public static boolean registerCustomFeature(String featureId, java.util.Map<String, Object> config) {
        try {
            String type = config.getOrDefault("type", "SCATTER").toString().toUpperCase();
            
            CustomFeature feature = new CustomFeature(featureId, type);
            
            if (config.containsKey("block")) {
                feature.block = config.get("block").toString();
            }
            
            if (config.containsKey("chance")) {
                Object chance = config.get("chance");
                if (chance instanceof Number) {
                    feature.chance = ((Number) chance).doubleValue();
                }
            }
            
            if (config.containsKey("minHeight")) {
                Object minHeight = config.get("minHeight");
                if (minHeight instanceof Number) {
                    feature.minHeight = ((Number) minHeight).intValue();
                }
            }
            
            if (config.containsKey("maxHeight")) {
                Object maxHeight = config.get("maxHeight");
                if (maxHeight instanceof Number) {
                    feature.maxHeight = ((Number) maxHeight).intValue();
                }
            }
            
            customFeatures.put(featureId, feature);
            LOGGER.info("Registered custom feature: " + featureId);
            return true;
        } catch (Exception e) {
            LOGGER.severe("Failed to register custom feature: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private static final Map<String, CustomFeature> customFeatures = new HashMap<>();
    
    /**
     * 生成自定义特征
     * 
     * @param world 世界
     * @param chunkX 区块 X
     * @param chunkZ 区块 Z
     * @param featureId 特征 ID
     * @return 生成的特征数量
     */
    public static int generateCustomFeature(Object world, int chunkX, int chunkZ, String featureId) {
        try {
            CustomFeature feature = customFeatures.get(featureId);
            if (feature == null) {
                LOGGER.warning("Custom feature not found: " + featureId);
                return 0;
            }
            
            if (!(world instanceof World)) {
                return 0;
            }
            
            World bukkitWorld = (World) world;
            int generated = 0;
            
            for (int i = 0; i < 16; i++) {
                if (random.nextDouble() < feature.chance) {
                    int x = chunkX * 16 + random.nextInt(16);
                    int z = chunkZ * 16 + random.nextInt(16);
                    int y = feature.minHeight + random.nextInt(feature.maxHeight - feature.minHeight);
                    
                    Location loc = new Location(bukkitWorld, x, y, z);
                    Material material = Material.getMaterial(
                        feature.block.toUpperCase().replace("MINECRAFT:", "")
                    );
                    
                    if (material != null) {
                        bukkitWorld.getBlockAt(loc).setType(material);
                        generated++;
                    }
                }
            }
            
            return generated;
        } catch (Exception e) {
            LOGGER.severe("Failed to generate custom feature: " + e.getMessage());
            return 0;
        }
    }
    
    // ===== 配置类 =====
    
    /**
     * 矿石配置
     */
    public static class OreConfig {
        public final String id;
        public String block = "minecraft:iron_ore";
        public String replaceBlock = "minecraft:stone";
        public int size = 9;
        public int count = 20;
        public int minY = -64;
        public int maxY = 320;
        
        public OreConfig(String id) {
            this.id = id;
        }
    }
    
    /**
     * 树木配置
     */
    public static class TreeConfig {
        public final String id;
        public String trunk = "minecraft:oak_log";
        public String leaves = "minecraft:oak_leaves";
        public int height = 5;
        public int canopyRadius = 2;
        
        public TreeConfig(String id) {
            this.id = id;
        }
    }
    
    /**
     * 生成巨石（Boulder）
     * 
     * @param world 世界
     * @param x X 坐标
     * @param y Y 坐标
     * @param z Z 坐标
     * @param radius 半径
     * @param block 方块类型
     * @return 是否生成成功
     */
    public static boolean generateBoulder(Object world, int x, int y, int z, int radius, String block) {
        try {
            if (!(world instanceof World)) {
                return false;
            }
            
            World bukkitWorld = (World) world;
            Material material = Material.getMaterial(block.toUpperCase().replace("MINECRAFT:", ""));
            
            if (material == null) {
                return false;
            }
            
            // 生成不规则球形巨石
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                        // 添加随机性使其不规则
                        double randomFactor = 0.8 + random.nextDouble() * 0.4;
                        if (distance <= radius * randomFactor) {
                            Location loc = new Location(bukkitWorld, x + dx, y + dy, z + dz);
                            bukkitWorld.getBlockAt(loc).setType(material);
                        }
                    }
                }
            }
            
            return true;
        } catch (Exception e) {
            LOGGER.severe("Failed to generate boulder: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 生成矿脉（Vein）- 更自然的矿石分布
     * 
     * @param world 世界
     * @param x X 坐标
     * @param y Y 坐标
     * @param z Z 坐标
     * @param oreId 矿石配置 ID
     * @param veinSize 矿脉大小
     * @return 生成的矿石数量
     */
    public static int generateVein(Object world, int x, int y, int z, String oreId, int veinSize) {
        try {
            OreConfig config = oreConfigs.get(oreId);
            if (config == null) {
                return 0;
            }
            
            if (!(world instanceof World)) {
                return 0;
            }
            
            World bukkitWorld = (World) world;
            int generated = 0;
            
            // 使用随机游走算法生成更自然的矿脉
            int currentX = x;
            int currentY = y;
            int currentZ = z;
            
            for (int i = 0; i < veinSize; i++) {
                Location loc = new Location(bukkitWorld, currentX, currentY, currentZ);
                Material current = bukkitWorld.getBlockAt(loc).getType();
                Material replace = Material.getMaterial(config.replaceBlock.toUpperCase().replace("MINECRAFT:", ""));
                
                if (current == replace) {
                    Material ore = Material.getMaterial(config.block.toUpperCase().replace("MINECRAFT:", ""));
                    if (ore != null) {
                        bukkitWorld.getBlockAt(loc).setType(ore);
                        generated++;
                    }
                }
                
                // 随机游走到相邻方块
                currentX += random.nextInt(3) - 1;
                currentY += random.nextInt(3) - 1;
                currentZ += random.nextInt(3) - 1;
            }
            
            return generated;
        } catch (Exception e) {
            LOGGER.severe("Failed to generate vein: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * 生成化石（Fossil）
     * 
     * @param world 世界
     * @param x X 坐标
     * @param y Y 坐标
     * @param z Z 坐标
     * @return 是否生成成功
     */
    public static boolean generateFossil(Object world, int x, int y, int z) {
        try {
            if (!(world instanceof World)) {
                return false;
            }
            
            World bukkitWorld = (World) world;
            
            // 生成简单的化石结构（骨块）
            Material bone = Material.BONE_BLOCK;
            Material coal = Material.COAL_ORE;
            
            // 随机形状的化石
            int size = 3 + random.nextInt(3);
            for (int i = 0; i < size; i++) {
                int dx = random.nextInt(5) - 2;
                int dy = random.nextInt(3) - 1;
                int dz = random.nextInt(5) - 2;
                
                Location loc = new Location(bukkitWorld, x + dx, y + dy, z + dz);
                bukkitWorld.getBlockAt(loc).setType(random.nextDouble() < 0.8 ? bone : coal);
            }
            
            return true;
        } catch (Exception e) {
            LOGGER.severe("Failed to generate fossil: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 自定义特征配置
     */
    public static class CustomFeature {
        public final String id;
        public final String type;
        public String block = "minecraft:stone";
        public double chance = 0.1;
        public int minHeight = 0;
        public int maxHeight = 256;
        
        public CustomFeature(String id, String type) {
            this.id = id;
            this.type = type;
        }
    }
}
