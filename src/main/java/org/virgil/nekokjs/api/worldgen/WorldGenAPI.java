package org.virgil.nekokjs.api.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import org.bukkit.Bukkit;

import javax.annotation.Nullable;

/**
 * 世界生成 API
 * 提供方块设置、高度获取、噪声生成等功能
 */
public class WorldGenAPI {
    
    /**
     * 在区块中设置方块
     * @param chunk 区块访问器
     * @param x 区块内 X 坐标 (0-15)
     * @param y 世界 Y 坐标
     * @param z 区块内 Z 坐标 (0-15)
     * @param blockId 方块 ID，例如 "minecraft:stone"
     * @return 是否设置成功
     */
    public static boolean setBlock(ChunkAccess chunk, int x, int y, int z, String blockId) {
        try {
            BlockState blockState = getBlockState(blockId);
            if (blockState == null) {
                return false;
            }
            
            // 转换为世界坐标
            int worldX = chunk.getPos().getMinBlockX() + x;
            int worldZ = chunk.getPos().getMinBlockZ() + z;
            BlockPos pos = new BlockPos(worldX, y, worldZ);
            
            // 设置方块
            chunk.setBlockState(pos, blockState, 0);
            return true;
        } catch (Exception e) {
            Bukkit.getLogger().warning("Failed to set block: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 在世界生成区域中设置方块
     * @param level 世界生成区域
     * @param x 世界 X 坐标
     * @param y 世界 Y 坐标
     * @param z 世界 Z 坐标
     * @param blockId 方块 ID
     * @return 是否设置成功
     */
    public static boolean setBlockInWorld(WorldGenRegion level, int x, int y, int z, String blockId) {
        try {
            BlockState blockState = getBlockState(blockId);
            if (blockState == null) {
                return false;
            }
            
            BlockPos pos = new BlockPos(x, y, z);
            level.setBlock(pos, blockState, 3);
            return true;
        } catch (Exception e) {
            Bukkit.getLogger().warning("Failed to set block in world: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取区块中的方块
     * @param chunk 区块访问器
     * @param x 区块内 X 坐标 (0-15)
     * @param y 世界 Y 坐标
     * @param z 区块内 Z 坐标 (0-15)
     * @return 方块 ID，例如 "minecraft:stone"
     */
    public static String getBlock(ChunkAccess chunk, int x, int y, int z) {
        try {
            int worldX = chunk.getPos().getMinBlockX() + x;
            int worldZ = chunk.getPos().getMinBlockZ() + z;
            BlockPos pos = new BlockPos(worldX, y, worldZ);
            
            BlockState blockState = chunk.getBlockState(pos);
            return getBlockId(blockState);
        } catch (Exception e) {
            Bukkit.getLogger().warning("Failed to get block: " + e.getMessage());
            return "minecraft:air";
        }
    }
    
    /**
     * 获取高度（最高固体方块）
     * @param chunk 区块访问器
     * @param x 区块内 X 坐标 (0-15)
     * @param z 区块内 Z 坐标 (0-15)
     * @param heightmapType 高度图类型：WORLD_SURFACE, OCEAN_FLOOR, MOTION_BLOCKING
     * @return 高度值
     */
    public static int getHeight(ChunkAccess chunk, int x, int z, String heightmapType) {
        try {
            Heightmap.Types type = parseHeightmapType(heightmapType);
            return chunk.getHeight(type, x, z);
        } catch (Exception e) {
            Bukkit.getLogger().warning("Failed to get height: " + e.getMessage());
            return 64;
        }
    }
    
    /**
     * 获取高度（默认使用 WORLD_SURFACE）
     */
    public static int getHeight(ChunkAccess chunk, int x, int z) {
        return getHeight(chunk, x, z, "WORLD_SURFACE");
    }
    
    /**
     * 填充区域（立方体）
     * @param chunk 区块访问器
     * @param x1 起始 X (区块内坐标)
     * @param y1 起始 Y
     * @param z1 起始 Z (区块内坐标)
     * @param x2 结束 X (区块内坐标)
     * @param y2 结束 Y
     * @param z2 结束 Z (区块内坐标)
     * @param blockId 方块 ID
     * @return 设置的方块数量
     */
    public static int fillRegion(ChunkAccess chunk, int x1, int y1, int z1, int x2, int y2, int z2, String blockId) {
        int count = 0;
        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2);
        int maxY = Math.max(y1, y2);
        int minZ = Math.min(z1, z2);
        int maxZ = Math.max(z1, z2);
        
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (setBlock(chunk, x, y, z, blockId)) {
                        count++;
                    }
                }
            }
        }
        
        return count;
    }
    
    /**
     * 设置生物群系
     * @param chunk 区块访问器
     * @param x 区块内 X 坐标 (0-15)
     * @param y 世界 Y 坐标
     * @param z 区块内 Z 坐标 (0-15)
     * @param biomeId 生物群系 ID，例如 "minecraft:plains"
     * @return 是否设置成功
     */
    public static boolean setBiome(ChunkAccess chunk, int x, int y, int z, String biomeId) {
        try {
            Holder<Biome> biome = getBiome(biomeId);
            if (biome == null) {
                return false;
            }
            
            // 转换为四分之一方块坐标
            int quartX = x / 4;
            int quartY = y / 4;
            int quartZ = z / 4;
            
            // 设置生物群系
            chunk.setBiome(quartX, quartY, quartZ, biome);
            return true;
        } catch (Exception e) {
            Bukkit.getLogger().warning("Failed to set biome: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取生物群系
     * @param chunk 区块访问器
     * @param x 区块内 X 坐标
     * @param y 世界 Y 坐标
     * @param z 区块内 Z 坐标
     * @return 生物群系 ID
     */
    public static String getBiomeAt(ChunkAccess chunk, int x, int y, int z) {
        try {
            int quartX = x / 4;
            int quartY = y / 4;
            int quartZ = z / 4;
            
            Holder<Biome> biome = chunk.getNoiseBiome(quartX, quartY, quartZ);
            return getBiomeId(biome);
        } catch (Exception e) {
            Bukkit.getLogger().warning("Failed to get biome: " + e.getMessage());
            return "minecraft:plains";
        }
    }
    
    // ===== 辅助方法 =====
    
    /**
     * 根据方块 ID 获取 BlockState
     */
    @Nullable
    private static BlockState getBlockState(String blockId) {
        try {
            ResourceLocation location = ResourceLocation.parse(blockId);
            // 使用 BuiltInRegistries 替代 Bukkit.getServer().getRegistry()
            Block block = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(location);
            return block != null ? block.defaultBlockState() : null;
        } catch (Exception e) {
            Bukkit.getLogger().warning("Invalid block ID: " + blockId);
            return null;
        }
    }
    
    /**
     * 根据 BlockState 获取方块 ID
     */
    private static String getBlockId(BlockState blockState) {
        try {
            // 使用 BuiltInRegistries 替代 Bukkit.getServer().getRegistry()
            ResourceLocation location = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(blockState.getBlock());
            return location != null ? location.toString() : "minecraft:air";
        } catch (Exception e) {
            return "minecraft:air";
        }
    }
    
    /**
     * 根据生物群系 ID 获取 Holder<Biome>
     */
    @Nullable
    private static Holder<Biome> getBiome(String biomeId) {
        try {
            ResourceLocation location = ResourceLocation.parse(biomeId);
            ResourceKey<Biome> key = ResourceKey.create(Registries.BIOME, location);
            // 使用 Minecraft 服务器的注册表访问 - 通过反射避免编译问题
            net.minecraft.server.MinecraftServer server = ((org.bukkit.craftbukkit.CraftServer) Bukkit.getServer()).getServer();
            Object registryAccess = server.registryAccess();
            java.lang.reflect.Method lookupMethod = registryAccess.getClass()
                .getMethod("registryOrThrow", ResourceKey.class);
            Object biomeRegistry = lookupMethod.invoke(registryAccess, Registries.BIOME);
            java.lang.reflect.Method getHolderMethod = biomeRegistry.getClass()
                .getMethod("getHolderOrThrow", ResourceKey.class);
            @SuppressWarnings("unchecked")
            Holder<Biome> holder = (Holder<Biome>) getHolderMethod.invoke(biomeRegistry, key);
            return holder;
        } catch (Exception e) {
            Bukkit.getLogger().warning("Invalid biome ID: " + biomeId);
            return null;
        }
    }
    
    /**
     * 根据 Holder<Biome> 获取生物群系 ID
     */
    private static String getBiomeId(Holder<Biome> biome) {
        try {
            ResourceLocation location = biome.unwrapKey().map(ResourceKey::location).orElse(null);
            return location != null ? location.toString() : "minecraft:plains";
        } catch (Exception e) {
            return "minecraft:plains";
        }
    }
    
    /**
     * 解析高度图类型
     */
    private static Heightmap.Types parseHeightmapType(String type) {
        return switch (type.toUpperCase()) {
            case "WORLD_SURFACE" -> Heightmap.Types.WORLD_SURFACE_WG;
            case "OCEAN_FLOOR" -> Heightmap.Types.OCEAN_FLOOR_WG;
            case "MOTION_BLOCKING" -> Heightmap.Types.MOTION_BLOCKING;
            case "MOTION_BLOCKING_NO_LEAVES" -> Heightmap.Types.MOTION_BLOCKING_NO_LEAVES;
            default -> Heightmap.Types.WORLD_SURFACE_WG;
        };
    }
    
    // ===== 常用方块常量 =====
    
    public static final String AIR = "minecraft:air";
    public static final String STONE = "minecraft:stone";
    public static final String DIRT = "minecraft:dirt";
    public static final String GRASS_BLOCK = "minecraft:grass_block";
    public static final String BEDROCK = "minecraft:bedrock";
    public static final String WATER = "minecraft:water";
    public static final String LAVA = "minecraft:lava";
    public static final String SAND = "minecraft:sand";
    public static final String GRAVEL = "minecraft:gravel";
    public static final String COAL_ORE = "minecraft:coal_ore";
    public static final String IRON_ORE = "minecraft:iron_ore";
    public static final String GOLD_ORE = "minecraft:gold_ore";
    public static final String DIAMOND_ORE = "minecraft:diamond_ore";
    
    // ===== 常用生物群系常量 =====
    
    public static final String PLAINS = "minecraft:plains";
    public static final String DESERT = "minecraft:desert";
    public static final String FOREST = "minecraft:forest";
    public static final String TAIGA = "minecraft:taiga";
    public static final String MOUNTAINS = "minecraft:mountains";
    public static final String OCEAN = "minecraft:ocean";
    public static final String RIVER = "minecraft:river";
    
    // ===== 噪声生成 API =====
    
    /**
     * 简单的 Perlin 噪声生成
     * @param x X 坐标
     * @param y Y 坐标
     * @param z Z 坐标
     * @param scale 缩放比例（越大越平滑）
     * @param seed 种子
     * @return 噪声值 (-1.0 到 1.0)
     */
    public static double perlinNoise(double x, double y, double z, double scale, long seed) {
        // 简化的 Perlin 噪声实现
        x = x / scale;
        y = y / scale;
        z = z / scale;
        
        // 使用种子初始化
        java.util.Random random = new java.util.Random(seed);
        
        // 简单的噪声计算（实际应该使用更复杂的算法）
        double noise = Math.sin(x * 0.1 + random.nextDouble()) * 
                      Math.cos(y * 0.1 + random.nextDouble()) * 
                      Math.sin(z * 0.1 + random.nextDouble());
        
        return Math.max(-1.0, Math.min(1.0, noise));
    }
    
    /**
     * 简单的 Perlin 噪声（2D）
     */
    public static double perlinNoise2D(double x, double z, double scale, long seed) {
        return perlinNoise(x, 0, z, scale, seed);
    }
    
    /**
     * 多层噪声（Octave Noise）
     * @param x X 坐标
     * @param z Z 坐标
     * @param octaves 层数
     * @param persistence 持续度
     * @param scale 缩放
     * @param seed 种子
     * @return 噪声值
     */
    public static double octaveNoise(double x, double z, int octaves, double persistence, double scale, long seed) {
        double total = 0;
        double frequency = 1;
        double amplitude = 1;
        double maxValue = 0;
        
        for (int i = 0; i < octaves; i++) {
            total += perlinNoise2D(x * frequency, z * frequency, scale, seed + i) * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= 2;
        }
        
        return total / maxValue;
    }
    
    /**
     * 生成高度图噪声
     * @param x X 坐标
     * @param z Z 坐标
     * @param baseHeight 基础高度
     * @param variation 变化幅度
     * @param scale 缩放
     * @param seed 种子
     * @return 高度值
     */
    public static int heightNoise(int x, int z, int baseHeight, int variation, double scale, long seed) {
        double noise = octaveNoise(x, z, 4, 0.5, scale, seed);
        // 将 -1~1 的噪声值映射到高度范围
        int height = baseHeight + (int)(noise * variation);
        return Math.max(-64, Math.min(320, height)); // 限制在世界高度范围内
    }
    
    /**
     * 简单的随机数生成
     * @param x X 坐标
     * @param z Z 坐标
     * @param seed 种子
     * @return 0.0 到 1.0 的随机值
     */
    public static double random(int x, int z, long seed) {
        long hash = seed;
        hash = hash * 31 + x;
        hash = hash * 31 + z;
        java.util.Random random = new java.util.Random(hash);
        return random.nextDouble();
    }
    
    /**
     * 平滑插值
     * @param a 起始值
     * @param b 结束值
     * @param t 插值参数 (0.0 到 1.0)
     * @return 插值结果
     */
    public static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
    
    /**
     * 平滑步进函数
     * @param t 输入值 (0.0 到 1.0)
     * @return 平滑后的值
     */
    public static double smoothstep(double t) {
        return t * t * (3 - 2 * t);
    }
    
    // ===== 结构生成 API =====
    
    /**
     * 生成简单的立方体结构
     * @param chunk 区块访问器
     * @param centerX 中心 X (区块内坐标)
     * @param centerY 中心 Y
     * @param centerZ 中心 Z (区块内坐标)
     * @param width 宽度
     * @param height 高度
     * @param depth 深度
     * @param blockId 方块 ID
     * @return 设置的方块数量
     */
    public static int generateCube(ChunkAccess chunk, int centerX, int centerY, int centerZ, 
                                   int width, int height, int depth, String blockId) {
        int halfWidth = width / 2;
        int halfDepth = depth / 2;
        
        return fillRegion(chunk, 
            centerX - halfWidth, centerY, centerZ - halfDepth,
            centerX + halfWidth, centerY + height, centerZ + halfDepth,
            blockId);
    }
    
    /**
     * 生成空心立方体（房间）
     * @param chunk 区块访问器
     * @param centerX 中心 X
     * @param centerY 中心 Y
     * @param centerZ 中心 Z
     * @param width 宽度
     * @param height 高度
     * @param depth 深度
     * @param wallBlock 墙壁方块
     * @param floorBlock 地板方块
     * @return 设置的方块数量
     */
    public static int generateRoom(ChunkAccess chunk, int centerX, int centerY, int centerZ,
                                   int width, int height, int depth, 
                                   String wallBlock, String floorBlock) {
        int count = 0;
        int halfWidth = width / 2;
        int halfDepth = depth / 2;
        
        // 地板
        for (int x = centerX - halfWidth; x <= centerX + halfWidth; x++) {
            for (int z = centerZ - halfDepth; z <= centerZ + halfDepth; z++) {
                if (setBlock(chunk, x, centerY, z, floorBlock)) count++;
            }
        }
        
        // 墙壁
        for (int y = centerY + 1; y < centerY + height; y++) {
            for (int x = centerX - halfWidth; x <= centerX + halfWidth; x++) {
                for (int z = centerZ - halfDepth; z <= centerZ + halfDepth; z++) {
                    // 只在边缘放置方块
                    if (x == centerX - halfWidth || x == centerX + halfWidth ||
                        z == centerZ - halfDepth || z == centerZ + halfDepth) {
                        if (setBlock(chunk, x, y, z, wallBlock)) count++;
                    }
                }
            }
        }
        
        // 天花板
        for (int x = centerX - halfWidth; x <= centerX + halfWidth; x++) {
            for (int z = centerZ - halfDepth; z <= centerZ + halfDepth; z++) {
                if (setBlock(chunk, x, centerY + height, z, wallBlock)) count++;
            }
        }
        
        return count;
    }
    
    /**
     * 生成球体结构
     * @param chunk 区块访问器
     * @param centerX 中心 X
     * @param centerY 中心 Y
     * @param centerZ 中心 Z
     * @param radius 半径
     * @param blockId 方块 ID
     * @param hollow 是否空心
     * @return 设置的方块数量
     */
    public static int generateSphere(ChunkAccess chunk, int centerX, int centerY, int centerZ,
                                     int radius, String blockId, boolean hollow) {
        int count = 0;
        int radiusSquared = radius * radius;
        int innerRadiusSquared = hollow ? (radius - 1) * (radius - 1) : 0;
        
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int y = centerY - radius; y <= centerY + radius; y++) {
                for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                    int dx = x - centerX;
                    int dy = y - centerY;
                    int dz = z - centerZ;
                    int distanceSquared = dx * dx + dy * dy + dz * dz;
                    
                    if (distanceSquared <= radiusSquared && 
                        (!hollow || distanceSquared > innerRadiusSquared)) {
                        if (setBlock(chunk, x, y, z, blockId)) count++;
                    }
                }
            }
        }
        
        return count;
    }
    
    /**
     * 生成圆柱体结构
     * @param chunk 区块访问器
     * @param centerX 中心 X
     * @param baseY 底部 Y
     * @param centerZ 中心 Z
     * @param radius 半径
     * @param height 高度
     * @param blockId 方块 ID
     * @return 设置的方块数量
     */
    public static int generateCylinder(ChunkAccess chunk, int centerX, int baseY, int centerZ,
                                       int radius, int height, String blockId) {
        int count = 0;
        int radiusSquared = radius * radius;
        
        for (int y = baseY; y < baseY + height; y++) {
            for (int x = centerX - radius; x <= centerX + radius; x++) {
                for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                    int dx = x - centerX;
                    int dz = z - centerZ;
                    int distanceSquared = dx * dx + dz * dz;
                    
                    if (distanceSquared <= radiusSquared) {
                        if (setBlock(chunk, x, y, z, blockId)) count++;
                    }
                }
            }
        }
        
        return count;
    }
    
    /**
     * 生成金字塔结构
     * @param chunk 区块访问器
     * @param centerX 中心 X
     * @param baseY 底部 Y
     * @param centerZ 中心 Z
     * @param baseSize 底部尺寸
     * @param blockId 方块 ID
     * @return 设置的方块数量
     */
    public static int generatePyramid(ChunkAccess chunk, int centerX, int baseY, int centerZ,
                                      int baseSize, String blockId) {
        int count = 0;
        int height = baseSize;
        
        for (int y = 0; y < height; y++) {
            int currentSize = baseSize - y;
            int halfSize = currentSize / 2;
            
            for (int x = centerX - halfSize; x <= centerX + halfSize; x++) {
                for (int z = centerZ - halfSize; z <= centerZ + halfSize; z++) {
                    if (setBlock(chunk, x, baseY + y, z, blockId)) count++;
                }
            }
        }
        
        return count;
    }
    
    // ===== 地物放置 API =====
    
    /**
     * 生成树（简单版本）
     * @param chunk 区块访问器
     * @param x X 坐标
     * @param y Y 坐标（地面）
     * @param z Z 坐标
     * @param trunkHeight 树干高度
     * @param leavesRadius 树叶半径
     * @return 是否生成成功
     */
    public static boolean generateTree(ChunkAccess chunk, int x, int y, int z, 
                                       int trunkHeight, int leavesRadius) {
        // 树干
        for (int i = 0; i < trunkHeight; i++) {
            setBlock(chunk, x, y + i, z, "minecraft:oak_log");
        }
        
        // 树叶（球形）
        int leavesY = y + trunkHeight - 1;
        for (int dx = -leavesRadius; dx <= leavesRadius; dx++) {
            for (int dy = -leavesRadius; dy <= leavesRadius; dy++) {
                for (int dz = -leavesRadius; dz <= leavesRadius; dz++) {
                    if (dx * dx + dy * dy + dz * dz <= leavesRadius * leavesRadius) {
                        setBlock(chunk, x + dx, leavesY + dy, z + dz, "minecraft:oak_leaves");
                    }
                }
            }
        }
        
        return true;
    }
    
    /**
     * 生成矿脉
     * @param chunk 区块访问器
     * @param centerX 中心 X
     * @param centerY 中心 Y
     * @param centerZ 中心 Z
     * @param size 矿脉大小
     * @param oreBlock 矿石方块
     * @param seed 随机种子
     * @return 生成的矿石数量
     */
    public static int generateOreVein(ChunkAccess chunk, int centerX, int centerY, int centerZ,
                                      int size, String oreBlock, long seed) {
        int count = 0;
        java.util.Random random = new java.util.Random(seed);
        
        for (int i = 0; i < size; i++) {
            int x = centerX + random.nextInt(3) - 1;
            int y = centerY + random.nextInt(3) - 1;
            int z = centerZ + random.nextInt(3) - 1;
            
            // 只替换石头
            String existing = getBlock(chunk, x, y, z);
            if (existing.equals("minecraft:stone") || existing.equals("minecraft:deepslate")) {
                if (setBlock(chunk, x, y, z, oreBlock)) count++;
            }
        }
        
        return count;
    }
    
    /**
     * 生成花丛
     * @param chunk 区块访问器
     * @param centerX 中心 X
     * @param groundY 地面 Y
     * @param centerZ 中心 Z
     * @param radius 半径
     * @param flowerBlock 花方块
     * @param density 密度 (0.0 到 1.0)
     * @param seed 随机种子
     * @return 生成的花数量
     */
    public static int generateFlowerPatch(ChunkAccess chunk, int centerX, int groundY, int centerZ,
                                          int radius, String flowerBlock, double density, long seed) {
        int count = 0;
        
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                double distance = Math.sqrt((x - centerX) * (x - centerX) + (z - centerZ) * (z - centerZ));
                
                if (distance <= radius && random(x, z, seed) < density) {
                    // 检查是否是草方块
                    String ground = getBlock(chunk, x, groundY, z);
                    if (ground.equals("minecraft:grass_block")) {
                        if (setBlock(chunk, x, groundY + 1, z, flowerBlock)) count++;
                    }
                }
            }
        }
        
        return count;
    }
    
    /**
     * 生成湖泊
     * @param chunk 区块访问器
     * @param centerX 中心 X
     * @param baseY 底部 Y
     * @param centerZ 中心 Z
     * @param radius 半径
     * @param depth 深度
     * @param liquidBlock 液体方块（水或岩浆）
     * @return 生成的方块数量
     */
    public static int generateLake(ChunkAccess chunk, int centerX, int baseY, int centerZ,
                                   int radius, int depth, String liquidBlock) {
        int count = 0;
        int radiusSquared = radius * radius;
        
        for (int y = baseY; y < baseY + depth; y++) {
            int currentRadius = (int)(radius * (1.0 - (double)(y - baseY) / depth));
            int currentRadiusSquared = currentRadius * currentRadius;
            
            for (int x = centerX - currentRadius; x <= centerX + currentRadius; x++) {
                for (int z = centerZ - currentRadius; z <= centerZ + currentRadius; z++) {
                    int dx = x - centerX;
                    int dz = z - centerZ;
                    
                    if (dx * dx + dz * dz <= currentRadiusSquared) {
                        if (setBlock(chunk, x, y, z, liquidBlock)) count++;
                    }
                }
            }
        }
        
        return count;
    }
    
    // ===== 地物常量 =====
    
    public static final String OAK_LOG = "minecraft:oak_log";
    public static final String OAK_LEAVES = "minecraft:oak_leaves";
    public static final String POPPY = "minecraft:poppy";
    public static final String DANDELION = "minecraft:dandelion";
    public static final String BLUE_ORCHID = "minecraft:blue_orchid";
}
