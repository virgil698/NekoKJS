package org.virgil.nekokjs.api.dimension;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.virgil.nekokjs.dimension.DimensionConfig;
import org.virgil.nekokjs.dimension.DimensionManager;
import org.virgil.nekokjs.NekoKJSPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 维度 API
 * 提供自定义维度的创建、管理和传送功能
 */
public class DimensionAPI {
    
    private static final Logger LOGGER = Logger.getLogger("NekoKJS-Dimension");
    private static DimensionManager dimensionManager;
    private static final Map<String, World> loadedWorlds = new HashMap<>();
    private static boolean isFolia = false;
    
    /**
     * 初始化 API
     */
    public static void initialize(DimensionManager manager) {
        dimensionManager = manager;
        // 检测是否为 Folia
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            isFolia = true;
            LOGGER.info("Detected Folia server - using regionized world management");
        } catch (ClassNotFoundException e) {
            isFolia = false;
            LOGGER.info("Detected Paper/Leaves server - using standard world management");
        }
    }
    
    /**
     * 检查是否运行在 Folia 上
     */
    public static boolean isFolia() {
        return isFolia;
    }
    
    // ===== 维度注册 =====
    
    /**
     * 注册自定义维度
     * 注意：维度需要在服务器重启后才能完全生效
     * 
     * @param dimensionId 维度 ID（格式：namespace:name）
     * @param config 维度配置对象
     * @return 是否注册成功
     */
    public static boolean registerDimension(String dimensionId, Object config) {
        if (dimensionManager == null) {
            LOGGER.warning("DimensionManager not initialized");
            return false;
        }
        
        try {
            // 将 JavaScript 对象转换为 DimensionConfig
            DimensionConfig dimensionConfig = convertToDimensionConfig(dimensionId, config);
            
            // 注册维度
            boolean success = dimensionManager.registerDimension(dimensionConfig);
            
            if (success) {
                LOGGER.info("Registered dimension: " + dimensionId + " (requires restart)");
            }
            
            return success;
        } catch (Exception e) {
            LOGGER.severe("Failed to register dimension: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // ===== 世界创建 =====
    
    /**
     * 创建或加载世界
     * 使用 Bukkit API 在运行时创建世界
     * 
     * @param worldName 世界名称
     * @param environment 环境类型（NORMAL, NETHER, THE_END）
     * @return 创建的世界，失败返回 null
     */
    public static World createWorld(String worldName, String environment) {
        try {
            // 检查世界是否已存在
            World existingWorld = Bukkit.getWorld(worldName);
            if (existingWorld != null) {
                LOGGER.info("World already exists: " + worldName);
                loadedWorlds.put(worldName, existingWorld);
                return existingWorld;
            }
            
            // 创建世界
            WorldCreator creator = new WorldCreator(worldName);
            
            // 设置环境
            switch (environment.toUpperCase()) {
                case "NETHER":
                case "THE_NETHER":
                    creator.environment(World.Environment.NETHER);
                    break;
                case "END":
                case "THE_END":
                    creator.environment(World.Environment.THE_END);
                    break;
                case "NORMAL":
                case "OVERWORLD":
                default:
                    creator.environment(World.Environment.NORMAL);
                    break;
            }
            
            // 创建世界
            World world = creator.createWorld();
            
            if (world != null) {
                loadedWorlds.put(worldName, world);
                LOGGER.info("Created world: " + worldName);
            }
            
            return world;
        } catch (Exception e) {
            LOGGER.severe("Failed to create world: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 创建平坦世界
     * 
     * @param worldName 世界名称
     * @return 创建的世界
     */
    public static World createFlatWorld(String worldName) {
        try {
            WorldCreator creator = new WorldCreator(worldName);
            creator.environment(World.Environment.NORMAL);
            creator.type(org.bukkit.WorldType.FLAT);
            
            World world = creator.createWorld();
            if (world != null) {
                loadedWorlds.put(worldName, world);
                LOGGER.info("Created flat world: " + worldName);
            }
            
            return world;
        } catch (Exception e) {
            LOGGER.severe("Failed to create flat world: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 创建虚空世界
     * 
     * @param worldName 世界名称
     * @return 创建的世界
     */
    public static World createVoidWorld(String worldName) {
        try {
            WorldCreator creator = new WorldCreator(worldName);
            creator.environment(World.Environment.NORMAL);
            creator.generator(new VoidChunkGenerator());
            
            World world = creator.createWorld();
            if (world != null) {
                loadedWorlds.put(worldName, world);
                LOGGER.info("Created void world: " + worldName);
            }
            
            return world;
        } catch (Exception e) {
            LOGGER.severe("Failed to create void world: " + e.getMessage());
            return null;
        }
    }
    
    // ===== 世界管理 =====
    
    /**
     * 获取世界
     * 
     * @param worldName 世界名称
     * @return 世界对象，不存在返回 null
     */
    public static World getWorld(String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            loadedWorlds.put(worldName, world);
        }
        return world;
    }
    
    /**
     * 获取所有已加载的世界
     * 
     * @return 世界名称数组
     */
    public static String[] getLoadedWorlds() {
        return Bukkit.getWorlds().stream()
            .map(World::getName)
            .toArray(String[]::new);
    }
    
    /**
     * 卸载世界
     * 
     * @param worldName 世界名称
     * @return 是否卸载成功
     */
    public static boolean unloadWorld(String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            LOGGER.warning("World not found: " + worldName);
            return false;
        }
        
        boolean success = Bukkit.unloadWorld(world, true);
        if (success) {
            loadedWorlds.remove(worldName);
            LOGGER.info("Unloaded world: " + worldName);
        }
        
        return success;
    }
    
    // ===== 传送功能 =====
    
    /**
     * 传送玩家到指定世界
     * 
     * @param player 玩家对象
     * @param worldName 世界名称
     * @param x X 坐标
     * @param y Y 坐标
     * @param z Z 坐标
     * @return 是否传送成功（Folia 下总是返回 true，实际结果通过异步回调）
     */
    public static boolean teleport(Object player, String worldName, double x, double y, double z) {
        if (!(player instanceof Player)) {
            LOGGER.warning("Invalid player object");
            return false;
        }
        
        Player p = (Player) player;
        World world = getWorld(worldName);
        
        if (world == null) {
            LOGGER.warning("World not found: " + worldName);
            return false;
        }
        
        Location location = new Location(world, x, y, z);
        
        // Folia 使用异步传送，Paper/Leaves 使用同步传送
        if (isFolia) {
            p.teleportAsync(location).thenAccept(result -> {
                if (result) {
                    LOGGER.info("Player " + p.getName() + " teleported to " + worldName);
                } else {
                    LOGGER.warning("Failed to teleport player " + p.getName() + " to " + worldName);
                }
            });
            return true; // 异步操作，立即返回
        } else {
            return p.teleport(location);
        }
    }
    
    /**
     * 传送玩家到世界出生点
     * 
     * @param player 玩家对象
     * @param worldName 世界名称
     * @return 是否传送成功（Folia 下总是返回 true，实际结果通过异步回调）
     */
    public static boolean teleportToSpawn(Object player, String worldName) {
        if (!(player instanceof Player)) {
            LOGGER.warning("Invalid player object");
            return false;
        }
        
        Player p = (Player) player;
        World world = getWorld(worldName);
        
        if (world == null) {
            LOGGER.warning("World not found: " + worldName);
            return false;
        }
        
        Location spawn = world.getSpawnLocation();
        
        // Folia 使用异步传送，Paper/Leaves 使用同步传送
        if (isFolia) {
            p.teleportAsync(spawn).thenAccept(result -> {
                if (result) {
                    LOGGER.info("Player " + p.getName() + " teleported to spawn of " + worldName);
                } else {
                    LOGGER.warning("Failed to teleport player " + p.getName() + " to spawn");
                }
            });
            return true;
        } else {
            return p.teleport(spawn);
        }
    }
    
    // ===== 辅助方法 =====
    
    /**
     * 将 JavaScript 配置对象转换为 DimensionConfig
     */
    private static DimensionConfig convertToDimensionConfig(String dimensionId, Object config) {
        DimensionConfig dimensionConfig = new DimensionConfig(dimensionId);
        
        // TODO: 解析 JavaScript 对象并设置配置
        // 这需要根据实际的 JavaScript 对象结构来实现
        
        return dimensionConfig;
    }
    
    /**
     * 简单的虚空区块生成器
     */
    private static class VoidChunkGenerator extends ChunkGenerator {
        @Override
        public boolean shouldGenerateNoise() {
            return false;
        }
        
        @Override
        public boolean shouldGenerateSurface() {
            return false;
        }
        
        @Override
        public boolean shouldGenerateCaves() {
            return false;
        }
        
        @Override
        public boolean shouldGenerateDecorations() {
            return false;
        }
        
        @Override
        public boolean shouldGenerateMobs() {
            return false;
        }
        
        @Override
        public boolean shouldGenerateStructures() {
            return false;
        }
    }
}
