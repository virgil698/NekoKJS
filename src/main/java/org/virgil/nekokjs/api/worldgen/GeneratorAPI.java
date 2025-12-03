package org.virgil.nekokjs.api.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

/**
 * 自定义生成器 API
 * 提供自定义世界生成器的创建和管理
 */
public class GeneratorAPI {
    
    private static final Logger LOGGER = Logger.getLogger("NekoKJS-Generator");
    private static final Map<String, CustomGenerator> customGenerators = new HashMap<>();
    
    /**
     * 注册自定义生成器
     * 
     * @param generatorId 生成器 ID
     * @param config 生成器配置
     * @return 是否注册成功
     */
    public static boolean registerGenerator(String generatorId, Map<String, Object> config) {
        try {
            CustomGenerator generator = new CustomGenerator(generatorId, config);
            customGenerators.put(generatorId, generator);
            LOGGER.info("Registered custom generator: " + generatorId);
            return true;
        } catch (Exception e) {
            LOGGER.severe("Failed to register generator: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 获取自定义生成器
     * 
     * @param generatorId 生成器 ID
     * @return 生成器实例
     */
    public static CustomGenerator getGenerator(String generatorId) {
        return customGenerators.get(generatorId);
    }
    
    /**
     * 创建使用自定义生成器的世界
     * 
     * @param worldName 世界名称
     * @param generatorId 生成器 ID
     * @return 创建的世界
     */
    public static World createWorldWithGenerator(String worldName, String generatorId) {
        try {
            CustomGenerator generator = customGenerators.get(generatorId);
            if (generator == null) {
                LOGGER.warning("Generator not found: " + generatorId);
                return null;
            }
            
            WorldCreator creator = new WorldCreator(worldName);
            creator.generator(generator.toBukkitGenerator());
            
            World world = creator.createWorld();
            LOGGER.info("Created world with custom generator: " + worldName);
            return world;
        } catch (Exception e) {
            LOGGER.severe("Failed to create world with generator: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 自定义生成器类
     */
    public static class CustomGenerator {
        private final String id;
        private final GeneratorType type;
        private final Map<String, Object> config;
        
        // 生成器配置
        private int baseHeight = 64;
        private int heightVariation = 32;
        private double noiseScale = 0.05;
        private boolean generateCaves = true;
        private boolean generateOres = true;
        private String defaultBiome = "minecraft:plains";
        
        public CustomGenerator(String id, Map<String, Object> config) {
            this.id = id;
            this.config = config;
            
            // 解析配置
            if (config.containsKey("type")) {
                String typeStr = config.get("type").toString().toUpperCase();
                this.type = GeneratorType.valueOf(typeStr);
            } else {
                this.type = GeneratorType.NOISE;
            }
            
            if (config.containsKey("baseHeight")) {
                Object height = config.get("baseHeight");
                if (height instanceof Number) {
                    this.baseHeight = ((Number) height).intValue();
                }
            }
            
            if (config.containsKey("heightVariation")) {
                Object variation = config.get("heightVariation");
                if (variation instanceof Number) {
                    this.heightVariation = ((Number) variation).intValue();
                }
            }
            
            if (config.containsKey("noiseScale")) {
                Object scale = config.get("noiseScale");
                if (scale instanceof Number) {
                    this.noiseScale = ((Number) scale).doubleValue();
                }
            }
            
            if (config.containsKey("generateCaves")) {
                Object caves = config.get("generateCaves");
                if (caves instanceof Boolean) {
                    this.generateCaves = (Boolean) caves;
                }
            }
            
            if (config.containsKey("generateOres")) {
                Object ores = config.get("generateOres");
                if (ores instanceof Boolean) {
                    this.generateOres = (Boolean) ores;
                }
            }
            
            if (config.containsKey("defaultBiome")) {
                this.defaultBiome = config.get("defaultBiome").toString();
            }
        }
        
        /**
         * 转换为 Bukkit 生成器
         */
        public org.bukkit.generator.ChunkGenerator toBukkitGenerator() {
            return new org.bukkit.generator.ChunkGenerator() {
                private final Random random = new Random();
                
                @Override
                public void generateNoise(org.bukkit.generator.WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
                    switch (type) {
                        case FLAT:
                            generateFlat(chunkData);
                            break;
                        case VOID:
                            // 虚空世界，不生成任何方块
                            break;
                        case AMPLIFIED:
                            generateAmplifiedTerrain(chunkX, chunkZ, chunkData, random);
                            break;
                        case ISLANDS:
                            generateIslands(chunkX, chunkZ, chunkData, random);
                            break;
                        case CAVES:
                            generateCaveWorld(chunkX, chunkZ, chunkData, random);
                            break;
                        case NOISE:
                        default:
                            generateNoiseTerrain(chunkX, chunkZ, chunkData, random);
                            break;
                    }
                }
                
                @Override
                public void generateSurface(org.bukkit.generator.WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
                    // 地表装饰
                    if (type == GeneratorType.NOISE) {
                        generateSurfaceLayer(chunkData);
                    }
                }
                
                @Override
                public void generateBedrock(org.bukkit.generator.WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
                    // 基岩层
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            chunkData.setBlock(x, worldInfo.getMinHeight(), z, org.bukkit.Material.BEDROCK);
                        }
                    }
                }
                
                @Override
                public void generateCaves(org.bukkit.generator.WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
                    if (generateCaves) {
                        // 简单的洞穴生成
                        generateSimpleCaves(chunkX, chunkZ, chunkData, random);
                    }
                }
                
                private void generateFlat(ChunkData chunkData) {
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            // 基岩
                            chunkData.setBlock(x, 0, z, org.bukkit.Material.BEDROCK);
                            // 石头层
                            for (int y = 1; y < baseHeight - 4; y++) {
                                chunkData.setBlock(x, y, z, org.bukkit.Material.STONE);
                            }
                            // 泥土层
                            for (int y = baseHeight - 4; y < baseHeight - 1; y++) {
                                chunkData.setBlock(x, y, z, org.bukkit.Material.DIRT);
                            }
                            // 草方块
                            chunkData.setBlock(x, baseHeight - 1, z, org.bukkit.Material.GRASS_BLOCK);
                        }
                    }
                }
                
                private void generateNoiseTerrain(int chunkX, int chunkZ, ChunkData chunkData, Random random) {
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            int worldX = chunkX * 16 + x;
                            int worldZ = chunkZ * 16 + z;
                            
                            // 使用简单的柏林噪声
                            double noise = generateNoise(worldX, worldZ);
                            int height = baseHeight + (int) (noise * heightVariation);
                            
                            // 限制高度范围
                            height = Math.max(1, Math.min(height, 255));
                            
                            // 生成地形
                            for (int y = 1; y <= height; y++) {
                                if (y < height - 4) {
                                    chunkData.setBlock(x, y, z, org.bukkit.Material.STONE);
                                } else if (y < height) {
                                    chunkData.setBlock(x, y, z, org.bukkit.Material.DIRT);
                                } else {
                                    chunkData.setBlock(x, y, z, org.bukkit.Material.GRASS_BLOCK);
                                }
                            }
                        }
                    }
                }
                
                private void generateSurfaceLayer(ChunkData chunkData) {
                    // 添加地表装饰（草、花等）
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            for (int y = chunkData.getMaxHeight() - 1; y >= chunkData.getMinHeight(); y--) {
                                if (chunkData.getType(x, y, z) == org.bukkit.Material.GRASS_BLOCK) {
                                    // 有概率生成草
                                    if (random.nextDouble() < 0.1) {
                                        chunkData.setBlock(x, y + 1, z, org.bukkit.Material.SHORT_GRASS);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
                
                private void generateSimpleCaves(int chunkX, int chunkZ, ChunkData chunkData, Random random) {
                    // 简单的洞穴生成逻辑
                    for (int i = 0; i < 3; i++) {
                        int caveX = random.nextInt(16);
                        int caveY = 10 + random.nextInt(50);
                        int caveZ = random.nextInt(16);
                        int caveRadius = 2 + random.nextInt(3);
                        
                        for (int x = Math.max(0, caveX - caveRadius); x < Math.min(16, caveX + caveRadius); x++) {
                            for (int y = Math.max(1, caveY - caveRadius); y < Math.min(255, caveY + caveRadius); y++) {
                                for (int z = Math.max(0, caveZ - caveRadius); z < Math.min(16, caveZ + caveRadius); z++) {
                                    double distance = Math.sqrt(
                                        Math.pow(x - caveX, 2) +
                                        Math.pow(y - caveY, 2) +
                                        Math.pow(z - caveZ, 2)
                                    );
                                    if (distance < caveRadius) {
                                        chunkData.setBlock(x, y, z, org.bukkit.Material.AIR);
                                    }
                                }
                            }
                        }
                    }
                }
                
                private double generateNoise(int x, int z) {
                    // 简单的柏林噪声实现
                    double noise = 0;
                    double amplitude = 1;
                    double frequency = noiseScale;
                    
                    for (int octave = 0; octave < 4; octave++) {
                        noise += perlinNoise(x * frequency, z * frequency) * amplitude;
                        amplitude *= 0.5;
                        frequency *= 2;
                    }
                    
                    return noise;
                }
                
                private double perlinNoise(double x, double z) {
                    // 简化的柏林噪声
                    int xi = (int) Math.floor(x) & 255;
                    int zi = (int) Math.floor(z) & 255;
                    double xf = x - Math.floor(x);
                    double zf = z - Math.floor(z);
                    
                    double u = fade(xf);
                    double v = fade(zf);
                    
                    int a = (xi + zi * 57) * 57;
                    int b = (xi + 1 + zi * 57) * 57;
                    int c = (xi + (zi + 1) * 57) * 57;
                    int d = (xi + 1 + (zi + 1) * 57) * 57;
                    
                    return lerp(v,
                        lerp(u, grad(a, xf, zf), grad(b, xf - 1, zf)),
                        lerp(u, grad(c, xf, zf - 1), grad(d, xf - 1, zf - 1))
                    );
                }
                
                private double fade(double t) {
                    return t * t * t * (t * (t * 6 - 15) + 10);
                }
                
                private double lerp(double t, double a, double b) {
                    return a + t * (b - a);
                }
                
                private double grad(int hash, double x, double z) {
                    int h = hash & 3;
                    double u = h < 2 ? x : z;
                    double v = h < 2 ? z : x;
                    return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
                }
                
                // 新生成器类型实现
                
                /**
                 * 放大地形生成（高山）
                 */
                private void generateAmplifiedTerrain(int chunkX, int chunkZ, ChunkData chunkData, Random random) {
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            int worldX = chunkX * 16 + x;
                            int worldZ = chunkZ * 16 + z;
                            
                            // 使用更大的高度变化
                            double noise = generateNoise(worldX, worldZ);
                            int height = baseHeight + (int) (noise * heightVariation * 3); // 3倍放大
                            height = Math.max(1, Math.min(255, height));
                            
                            // 生成地形
                            for (int y = 1; y <= height; y++) {
                                if (y < height - 4) {
                                    chunkData.setBlock(x, y, z, org.bukkit.Material.STONE);
                                } else if (y < height) {
                                    chunkData.setBlock(x, y, z, org.bukkit.Material.DIRT);
                                } else {
                                    chunkData.setBlock(x, y, z, org.bukkit.Material.GRASS_BLOCK);
                                }
                            }
                        }
                    }
                }
                
                /**
                 * 浮空岛生成
                 */
                private void generateIslands(int chunkX, int chunkZ, ChunkData chunkData, Random random) {
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            int worldX = chunkX * 16 + x;
                            int worldZ = chunkZ * 16 + z;
                            
                            // 使用噪声决定是否生成岛屿
                            double islandNoise = generateNoise((int)(worldX * 0.01), (int)(worldZ * 0.01));
                            
                            if (islandNoise > 0.3) { // 只在噪声值高的地方生成岛屿
                                double heightNoise = generateNoise(worldX, worldZ);
                                int islandBase = baseHeight + (int) (heightNoise * 20);
                                int islandHeight = 5 + random.nextInt(10);
                                
                                for (int y = islandBase; y < islandBase + islandHeight; y++) {
                                    // 岛屿边缘逐渐变小
                                    double edgeFactor = 1.0 - ((double) (y - islandBase) / islandHeight);
                                    if (random.nextDouble() < edgeFactor) {
                                        if (y < islandBase + islandHeight - 1) {
                                            chunkData.setBlock(x, y, z, org.bukkit.Material.STONE);
                                        } else {
                                            chunkData.setBlock(x, y, z, org.bukkit.Material.GRASS_BLOCK);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                /**
                 * 洞穴世界生成（反转地形）
                 */
                private void generateCaveWorld(int chunkX, int chunkZ, ChunkData chunkData, Random random) {
                    // 填充整个区块
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            for (int y = 1; y < 128; y++) {
                                chunkData.setBlock(x, y, z, org.bukkit.Material.STONE);
                            }
                        }
                    }
                    
                    // 雕刻出洞穴空间
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            int worldX = chunkX * 16 + x;
                            int worldZ = chunkZ * 16 + z;
                            
                            for (int y = 10; y < 120; y++) {
                                double caveNoise = generateNoise(worldX * 0.1, y * 0.1, worldZ * 0.1);
                                if (caveNoise > 0.2) { // 创建空气空间
                                    chunkData.setBlock(x, y, z, org.bukkit.Material.AIR);
                                }
                            }
                        }
                    }
                }
                
                /**
                 * 3D 噪声生成（用于洞穴世界）
                 */
                private double generateNoise(double x, double y, double z) {
                    return perlinNoise(x * noiseScale, z * noiseScale) * 
                           Math.sin(y * 0.1); // 添加 Y 轴变化
                }
            };
        }
        
        public String getId() {
            return id;
        }
        
        public GeneratorType getType() {
            return type;
        }
    }
    
    /**
     * 生成器类型
     */
    public enum GeneratorType {
        FLAT,          // 平坦世界
        VOID,          // 虚空世界
        NOISE,         // 噪声地形
        AMPLIFIED,     // 放大地形（高山）
        ISLANDS,       // 浮空岛
        CAVES,         // 洞穴世界
        CUSTOM         // 完全自定义
    }
}
