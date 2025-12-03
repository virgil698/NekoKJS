package org.virgil.nekokjs.api.integration;

import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.bukkit.api.CraftEngineFurniture;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

/**
 * CraftEngine 集成 API
 * 允许在世界生成中使用 CraftEngine 的方块和地物
 */
public class CraftEngineAPI {
    
    private static final Logger LOGGER = Logger.getLogger("NekoKJS-CraftEngine");
    private static boolean craftEngineAvailable = false;
    private static Object craftEnginePlugin = null;
    
    /**
     * 初始化 CraftEngine 集成
     */
    public static void initialize() {
        try {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("CraftEngine");
            if (plugin != null && plugin.isEnabled()) {
                craftEnginePlugin = plugin;
                craftEngineAvailable = true;
                LOGGER.info("CraftEngine integration enabled!");
            } else {
                LOGGER.info("CraftEngine not found, integration disabled");
            }
        } catch (Exception e) {
            LOGGER.warning("Failed to initialize CraftEngine integration: " + e.getMessage());
            craftEngineAvailable = false;
        }
    }
    
    /**
     * 检查 CraftEngine 是否可用
     * 
     * @return 是否可用
     */
    public static boolean isAvailable() {
        return craftEngineAvailable;
    }
    
    /**
     * 放置 CraftEngine 方块
     * 
     * @param world 世界
     * @param x X 坐标
     * @param y Y 坐标
     * @param z Z 坐标
     * @param blockId CraftEngine 方块 ID (格式: ce:namespace:id，例如 ce:mypack:custom_ore)
     * @return 是否放置成功
     */
    public static boolean placeBlock(Object world, int x, int y, int z, String blockId) {
        if (!craftEngineAvailable) {
            LOGGER.warning("CraftEngine is not available");
            return false;
        }
        
        try {
            if (!(world instanceof World)) {
                return false;
            }
            
            World bukkitWorld = (World) world;
            Location loc = new Location(bukkitWorld, x, y, z);
            
            // 解析为 Key（移除 ce: 前缀）
            Key key = parseCraftEngineKey(blockId);
            if (key == null) {
                LOGGER.warning("Invalid CraftEngine block ID format: " + blockId + " (expected: ce:namespace:id)");
                return false;
            }
            
            // 使用 CraftEngine API 放置方块
            boolean success = CraftEngineBlocks.place(loc, key, true);
            
            if (success) {
                LOGGER.fine("Placed CraftEngine block: " + blockId + " at " + x + ", " + y + ", " + z);
            }
            
            return success;
        } catch (Exception e) {
            LOGGER.severe("Failed to place CraftEngine block: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 放置 CraftEngine 家具
     * 
     * @param world 世界
     * @param x X 坐标
     * @param y Y 坐标
     * @param z Z 坐标
     * @param furnitureId CraftEngine 家具 ID (格式: ce:namespace:id，例如 ce:mypack:decorative_plant)
     * @return 是否放置成功
     */
    public static boolean placeFurniture(Object world, int x, int y, int z, String furnitureId) {
        if (!craftEngineAvailable) {
            LOGGER.warning("CraftEngine is not available");
            return false;
        }
        
        try {
            if (!(world instanceof World)) {
                return false;
            }
            
            World bukkitWorld = (World) world;
            Location loc = new Location(bukkitWorld, x, y, z);
            
            // 解析为 Key（移除 ce: 前缀）
            Key key = parseCraftEngineKey(furnitureId);
            if (key == null) {
                LOGGER.warning("Invalid CraftEngine furniture ID format: " + furnitureId + " (expected: ce:namespace:id)");
                return false;
            }
            
            // 使用 CraftEngine API 放置家具
            boolean success = CraftEngineFurniture.place(loc, key) != null;
            
            if (success) {
                LOGGER.fine("Placed CraftEngine furniture: " + furnitureId + " at " + x + ", " + y + ", " + z);
            }
            
            return success;
        } catch (Exception e) {
            LOGGER.severe("Failed to place CraftEngine furniture: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 检查位置是否有 CraftEngine 方块
     * 
     * @param world 世界
     * @param x X 坐标
     * @param y Y 坐标
     * @param z Z 坐标
     * @return 是否是 CraftEngine 方块
     */
    public static boolean isCustomBlock(Object world, int x, int y, int z) {
        if (!craftEngineAvailable) {
            return false;
        }
        
        try {
            if (!(world instanceof World)) {
                return false;
            }
            
            World bukkitWorld = (World) world;
            org.bukkit.block.Block block = bukkitWorld.getBlockAt(x, y, z);
            
            return CraftEngineBlocks.isCustomBlock(block);
        } catch (Exception e) {
            LOGGER.severe("Failed to check CraftEngine block: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 移除 CraftEngine 方块
     * 
     * @param world 世界
     * @param x X 坐标
     * @param y Y 坐标
     * @param z Z 坐标
     * @return 是否移除成功
     */
    public static boolean removeBlock(Object world, int x, int y, int z) {
        if (!craftEngineAvailable) {
            return false;
        }
        
        try {
            if (!(world instanceof World)) {
                return false;
            }
            
            World bukkitWorld = (World) world;
            org.bukkit.block.Block block = bukkitWorld.getBlockAt(x, y, z);
            
            return CraftEngineBlocks.remove(block);
        } catch (Exception e) {
            LOGGER.severe("Failed to remove CraftEngine block: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 解析字符串为 CraftEngine Key
     * 支持格式：
     * - ce:namespace:id (例如 ce:mypack:custom_ore)
     * 
     * ce: 前缀用于在 NekoKJS 中区分不同插件的物品
     * （例如 ce: 表示 CraftEngine，ia: 表示 ItemsAdder，oraxen: 表示 Oraxen）
     * 
     * @param id 输入的 ID
     * @return Key 对象，如果格式无效则返回 null
     */
    private static Key parseCraftEngineKey(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        
        try {
            // 检查并移除 ce: 前缀
            if (!id.startsWith("ce:")) {
                LOGGER.warning("CraftEngine ID must start with 'ce:' prefix: " + id);
                return null;
            }
            
            // 移除 ce: 前缀
            String actualId = id.substring(3);
            
            // 解析 namespace:id 格式
            String[] parts = actualId.split(":", 2);
            if (parts.length == 2) {
                return Key.of(parts[0], parts[1]);
            }
            
            LOGGER.warning("Invalid CraftEngine ID format after 'ce:' prefix: " + actualId + " (expected: namespace:id)");
            return null;
        } catch (Exception e) {
            LOGGER.warning("Failed to parse CraftEngine Key: " + id);
            return null;
        }
    }
}
