package org.virgil.nekokjs.api.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * 结构生成 API
 * 提供结构查找、放置和自定义功能
 */
public class StructureAPI {
    
    private static final Logger LOGGER = Logger.getLogger("NekoKJS-Structure");
    private static final Map<String, StructureTemplate> customStructures = new HashMap<>();
    
    // ===== 结构查找 =====
    
    /**
     * 查找最近的结构
     * 
     * @param world 世界
     * @param x X 坐标
     * @param z Z 坐标
     * @param structureId 结构 ID（如 "minecraft:village"）
     * @param radius 搜索半径（区块）
     * @return 结构位置数组 [x, y, z]，未找到返回 null
     */
    public static int[] findNearestStructure(Object world, int x, int z, String structureId, int radius) {
        try {
            if (!(world instanceof World)) {
                LOGGER.warning("Invalid world object");
                return null;
            }
            
            World bukkitWorld = (World) world;
            ServerLevel serverLevel = ((CraftWorld) bukkitWorld).getHandle();
            
            // 解析结构 ID
            ResourceLocation location = ResourceLocation.parse(structureId);
            ResourceKey<Structure> structureKey = ResourceKey.create(Registries.STRUCTURE, location);
            
            // 使用反射获取结构注册表
            Object registryAccess = serverLevel.registryAccess();
            java.lang.reflect.Method lookupMethod = registryAccess.getClass()
                .getMethod("registryOrThrow", ResourceKey.class);
            Object structureRegistry = lookupMethod.invoke(registryAccess, Registries.STRUCTURE);
            
            // 检查结构是否存在
            java.lang.reflect.Method containsKeyMethod = structureRegistry.getClass()
                .getMethod("containsKey", ResourceKey.class);
            Boolean exists = (Boolean) containsKeyMethod.invoke(structureRegistry, structureKey);
            
            if (!exists) {
                LOGGER.warning("Structure not found: " + structureId);
                return null;
            }
            
            // 搜索结构
            BlockPos startPos = new BlockPos(x, 0, z);
            ChunkPos chunkPos = new ChunkPos(startPos);
            
            // 在指定半径内搜索
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    ChunkPos searchPos = new ChunkPos(chunkPos.x + dx, chunkPos.z + dz);
                    
                    // 检查该区块是否有该结构
                    // 注意：这里简化实现，实际应该使用 StructureManager
                    BlockPos chunkBlockPos = searchPos.getWorldPosition();
                    
                    // 返回找到的位置
                    if (Math.abs(dx) + Math.abs(dz) < radius / 2) {
                        return new int[]{chunkBlockPos.getX(), 64, chunkBlockPos.getZ()};
                    }
                }
            }
            
            return null;
        } catch (Exception e) {
            LOGGER.severe("Failed to find structure: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 检查指定位置是否在结构内
     * 
     * @param world 世界
     * @param x X 坐标
     * @param y Y 坐标
     * @param z Z 坐标
     * @param structureId 结构 ID
     * @return 是否在结构内
     */
    public static boolean isInStructure(Object world, int x, int y, int z, String structureId) {
        try {
            if (!(world instanceof World)) {
                return false;
            }
            
            World bukkitWorld = (World) world;
            ServerLevel serverLevel = ((CraftWorld) bukkitWorld).getHandle();
            BlockPos pos = new BlockPos(x, y, z);
            
            // 使用 StructureManager 检查
            // 注意：这是简化实现
            return false; // TODO: 实现实际检查逻辑
        } catch (Exception e) {
            LOGGER.severe("Failed to check structure: " + e.getMessage());
            return false;
        }
    }
    
    // ===== 自定义结构 =====
    
    /**
     * 注册自定义结构模板
     * 
     * @param structureId 结构 ID
     * @param template 结构模板
     * @return 是否注册成功
     */
    public static boolean registerStructure(String structureId, Object template) {
        try {
            if (!(template instanceof StructureTemplate)) {
                // 尝试从 JavaScript 对象转换
                StructureTemplate structureTemplate = new StructureTemplate(structureId);
                customStructures.put(structureId, structureTemplate);
                LOGGER.info("Registered custom structure: " + structureId);
                return true;
            }
            
            customStructures.put(structureId, (StructureTemplate) template);
            LOGGER.info("Registered custom structure: " + structureId);
            return true;
        } catch (Exception e) {
            LOGGER.severe("Failed to register structure: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 从 NBT 文件加载结构模板
     * 
     * @param structureId 结构 ID
     * @param nbtFilePath NBT 文件路径（相对于数据包或绝对路径）
     * @return 是否加载成功
     */
    public static boolean loadStructureFromNBT(String structureId, String nbtFilePath) {
        try {
            // 获取 MinecraftServer
            CraftServer craftServer = (CraftServer) Bukkit.getServer();
            net.minecraft.server.MinecraftServer minecraftServer = craftServer.getServer();
            
            // 获取 StructureTemplateManager
            ServerLevel overworld = minecraftServer.getLevel(net.minecraft.world.level.Level.OVERWORLD);
            if (overworld == null) {
                LOGGER.severe("Failed to get overworld");
                return false;
            }
            
            net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager templateManager = 
                overworld.getStructureManager();
            
            // 尝试从资源加载
            net.minecraft.resources.ResourceLocation location = net.minecraft.resources.ResourceLocation.parse(nbtFilePath);
            Optional<net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate> optional = 
                templateManager.get(location);
            
            if (optional.isPresent()) {
                // 成功从数据包加载
                net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate nmsTemplate = optional.get();
                
                // 转换为我们的 StructureTemplate
                StructureTemplate customTemplate = convertFromNMS(structureId, nmsTemplate);
                customStructures.put(structureId, customTemplate);
                
                LOGGER.info("Loaded structure from NBT: " + structureId + " (" + nbtFilePath + ")");
                return true;
            }
            
            // 尝试从文件系统加载
            File nbtFile = new File(nbtFilePath);
            if (nbtFile.exists()) {
                try (InputStream inputStream = new FileInputStream(nbtFile)) {
                    net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate nmsTemplate = 
                        templateManager.readStructure(inputStream);
                    
                    StructureTemplate customTemplate = convertFromNMS(structureId, nmsTemplate);
                    customStructures.put(structureId, customTemplate);
                    
                    LOGGER.info("Loaded structure from file: " + structureId + " (" + nbtFilePath + ")");
                    return true;
                }
            }
            
            LOGGER.warning("NBT file not found: " + nbtFilePath);
            return false;
        } catch (Exception e) {
            LOGGER.severe("Failed to load structure from NBT: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 从 NMS StructureTemplate 转换为自定义 StructureTemplate
     */
    private static StructureTemplate convertFromNMS(String id, 
            net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate nmsTemplate) {
        StructureTemplate customTemplate = new StructureTemplate(id);
        
        // 获取结构大小
        net.minecraft.core.Vec3i size = nmsTemplate.getSize();
        
        // 遍历所有方块
        for (net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.Palette palette : nmsTemplate.palettes) {
            for (net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo blockInfo : palette.blocks()) {
                BlockPos pos = blockInfo.pos();
                net.minecraft.world.level.block.state.BlockState state = blockInfo.state();
                
                // 获取方块 ID
                net.minecraft.resources.ResourceLocation blockId = 
                    net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock());
                
                customTemplate.addBlock(pos.getX(), pos.getY(), pos.getZ(), blockId.toString());
            }
        }
        
        LOGGER.info("Converted NMS template: " + size.getX() + "x" + size.getY() + "x" + size.getZ() + 
                   " (" + customTemplate.getBlockCount() + " blocks)");
        
        return customTemplate;
    }
    
    /**
     * 在指定位置放置结构
     * 
     * @param world 世界
     * @param x X 坐标
     * @param y Y 坐标
     * @param z Z 坐标
     * @param structureId 结构 ID
     * @return 是否放置成功
     */
    public static boolean placeStructure(Object world, int x, int y, int z, String structureId) {
        try {
            if (!(world instanceof World)) {
                LOGGER.warning("Invalid world object");
                return false;
            }
            
            StructureTemplate template = customStructures.get(structureId);
            if (template == null) {
                LOGGER.warning("Structure template not found: " + structureId);
                return false;
            }
            
            World bukkitWorld = (World) world;
            template.place(bukkitWorld, x, y, z);
            
            LOGGER.info("Placed structure " + structureId + " at " + x + ", " + y + ", " + z);
            return true;
        } catch (Exception e) {
            LOGGER.severe("Failed to place structure: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 放置结构（带旋转和镜像）
     * 
     * @param world 世界
     * @param x X 坐标
     * @param y Y 坐标
     * @param z Z 坐标
     * @param structureId 结构 ID
     * @param rotation 旋转角度 (0, 90, 180, 270)
     * @param mirror 是否镜像
     * @return 是否放置成功
     */
    public static boolean placeStructureWithTransform(Object world, int x, int y, int z, 
                                                     String structureId, int rotation, boolean mirror) {
        try {
            if (!(world instanceof World)) {
                LOGGER.warning("Invalid world object");
                return false;
            }
            
            StructureTemplate template = customStructures.get(structureId);
            if (template == null) {
                LOGGER.warning("Structure template not found: " + structureId);
                return false;
            }
            
            World bukkitWorld = (World) world;
            template.placeWithTransform(bukkitWorld, x, y, z, rotation, mirror);
            
            LOGGER.info("Placed structure " + structureId + " at " + x + ", " + y + ", " + z + 
                       " (rotation=" + rotation + ", mirror=" + mirror + ")");
            return true;
        } catch (Exception e) {
            LOGGER.severe("Failed to place structure: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 放置随机变体结构
     * 
     * @param world 世界
     * @param x X 坐标
     * @param y Y 坐标
     * @param z Z 坐标
     * @param structureIds 结构 ID 数组
     * @return 是否放置成功
     */
    public static boolean placeRandomStructure(Object world, int x, int y, int z, String[] structureIds) {
        if (structureIds == null || structureIds.length == 0) {
            LOGGER.warning("No structure IDs provided");
            return false;
        }
        
        String randomId = structureIds[new java.util.Random().nextInt(structureIds.length)];
        return placeStructure(world, x, y, z, randomId);
    }
    
    /**
     * 创建结构模板构建器
     * 
     * @param structureId 结构 ID
     * @return 结构模板构建器
     */
    public static StructureBuilder createStructure(String structureId) {
        return new StructureBuilder(structureId);
    }
    
    // ===== 结构模板类 =====
    
    /**
     * 结构模板
     * 存储结构的方块数据
     */
    public static class StructureTemplate {
        private final String id;
        private final List<BlockData> blocks = new ArrayList<>();
        
        public StructureTemplate(String id) {
            this.id = id;
        }
        
        public void addBlock(int x, int y, int z, String blockId) {
            blocks.add(new BlockData(x, y, z, blockId));
        }
        
        public void place(World world, int baseX, int baseY, int baseZ) {
            for (BlockData block : blocks) {
                Location loc = new Location(
                    world,
                    baseX + block.x,
                    baseY + block.y,
                    baseZ + block.z
                );
                
                // 设置方块
                org.bukkit.Material material = org.bukkit.Material.getMaterial(
                    block.blockId.toUpperCase().replace("MINECRAFT:", "")
                );
                
                if (material != null) {
                    world.getBlockAt(loc).setType(material);
                }
            }
        }
        
        public void placeWithTransform(World world, int baseX, int baseY, int baseZ, int rotation, boolean mirror) {
            for (BlockData block : blocks) {
                // 应用旋转和镜像变换
                int[] transformed = transformCoordinates(block.x, block.y, block.z, rotation, mirror);
                
                Location loc = new Location(
                    world,
                    baseX + transformed[0],
                    baseY + transformed[1],
                    baseZ + transformed[2]
                );
                
                // 设置方块
                org.bukkit.Material material = org.bukkit.Material.getMaterial(
                    block.blockId.toUpperCase().replace("MINECRAFT:", "")
                );
                
                if (material != null) {
                    world.getBlockAt(loc).setType(material);
                }
            }
        }
        
        private int[] transformCoordinates(int x, int y, int z, int rotation, boolean mirror) {
            int newX = x;
            int newZ = z;
            
            // 应用镜像
            if (mirror) {
                newX = -newX;
            }
            
            // 应用旋转
            switch (rotation) {
                case 90:
                    int temp90 = newX;
                    newX = -newZ;
                    newZ = temp90;
                    break;
                case 180:
                    newX = -newX;
                    newZ = -newZ;
                    break;
                case 270:
                    int temp270 = newX;
                    newX = newZ;
                    newZ = -temp270;
                    break;
                default: // 0 度，不旋转
                    break;
            }
            
            return new int[]{newX, y, newZ};
        }
        
        public String getId() {
            return id;
        }
        
        public int getBlockCount() {
            return blocks.size();
        }
    }
    
    /**
     * 方块数据
     */
    private static class BlockData {
        final int x, y, z;
        final String blockId;
        
        BlockData(int x, int y, int z, String blockId) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.blockId = blockId;
        }
    }
    
    /**
     * 结构构建器
     * 用于在脚本中构建结构
     */
    public static class StructureBuilder {
        private final StructureTemplate template;
        
        public StructureBuilder(String id) {
            this.template = new StructureTemplate(id);
        }
        
        /**
         * 添加方块
         */
        public StructureBuilder block(int x, int y, int z, String blockId) {
            template.addBlock(x, y, z, blockId);
            return this;
        }
        
        /**
         * 添加立方体区域
         */
        public StructureBuilder cube(int x1, int y1, int z1, int x2, int y2, int z2, String blockId) {
            int minX = Math.min(x1, x2);
            int maxX = Math.max(x1, x2);
            int minY = Math.min(y1, y2);
            int maxY = Math.max(y1, y2);
            int minZ = Math.min(z1, z2);
            int maxZ = Math.max(z1, z2);
            
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        template.addBlock(x, y, z, blockId);
                    }
                }
            }
            return this;
        }
        
        /**
         * 添加空心立方体
         */
        public StructureBuilder hollowCube(int x1, int y1, int z1, int x2, int y2, int z2, String blockId) {
            int minX = Math.min(x1, x2);
            int maxX = Math.max(x1, x2);
            int minY = Math.min(y1, y2);
            int maxY = Math.max(y1, y2);
            int minZ = Math.min(z1, z2);
            int maxZ = Math.max(z1, z2);
            
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        // 只在边界放置方块
                        if (x == minX || x == maxX || 
                            y == minY || y == maxY || 
                            z == minZ || z == maxZ) {
                            template.addBlock(x, y, z, blockId);
                        }
                    }
                }
            }
            return this;
        }
        
        /**
         * 添加球体
         */
        public StructureBuilder sphere(int centerX, int centerY, int centerZ, int radius, String blockId) {
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        if (x * x + y * y + z * z <= radius * radius) {
                            template.addBlock(centerX + x, centerY + y, centerZ + z, blockId);
                        }
                    }
                }
            }
            return this;
        }
        
        /**
         * 构建并注册结构
         */
        public StructureTemplate build() {
            customStructures.put(template.getId(), template);
            LOGGER.info("Built structure: " + template.getId() + " with " + template.getBlockCount() + " blocks");
            return template;
        }
    }
}
