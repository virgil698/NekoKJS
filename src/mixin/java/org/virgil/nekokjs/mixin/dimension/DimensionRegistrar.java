package org.virgil.nekokjs.mixin.dimension;

import org.virgil.nekokjs.mixin.bridge.Bridge;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import java.util.Optional;
import java.util.OptionalLong;

/**
 * 维度注册器
 * 负责将自定义维度注册到 Minecraft 的注册表系统
 */
public class DimensionRegistrar {
    
    private static final String LOG_PREFIX = "[NekoKJS-Dimension] ";
    
    private static void log(String message) {
        System.out.println(LOG_PREFIX + message);
    }
    
    private static void logError(String message) {
        System.err.println(LOG_PREFIX + message);
    }
    
    private static void logError(String message, Throwable e) {
        System.err.println(LOG_PREFIX + message);
        e.printStackTrace();
    }
    
    /**
     * 注册自定义维度
     * 
     * @param registryAccess 注册表访问器
     * @param config 维度配置数据
     * @return 是否注册成功
     */
    public static boolean registerDimension(RegistryAccess registryAccess, Bridge.DimensionConfigData config) {
        try {
            String dimensionId = config.dimensionId;
            
            log("Registering custom dimension: " + dimensionId);
            
            // 创建 ResourceLocation
            ResourceLocation location = ResourceLocation.parse(dimensionId);
            
            // 创建 DimensionType
            DimensionType dimensionType = createDimensionType(config.typeConfig);
            
            // 创建 ChunkGenerator
            ChunkGenerator chunkGenerator = createChunkGenerator(
                registryAccess, 
                config.generatorType
            );
            
            // 创建 LevelStem
            ResourceKey<DimensionType> dimensionTypeKey = ResourceKey.create(
                Registries.DIMENSION_TYPE, 
                location
            );
            
            // 注册 DimensionType
            // 使用反射调用 lookupOrThrow 避免编译时依赖问题
            Object dimensionTypeRegistryObj;
            try {
                java.lang.reflect.Method lookupMethod = registryAccess.getClass()
                    .getMethod("lookupOrThrow", ResourceKey.class);
                dimensionTypeRegistryObj = lookupMethod.invoke(
                    registryAccess, 
                    Registries.DIMENSION_TYPE
                );
            } catch (Exception e) {
                logError("Failed to get dimension type registry: " + e.getMessage());
                throw new RuntimeException(e);
            }
            
            Holder<DimensionType> dimensionTypeHolder = registerDimensionType(
                dimensionTypeRegistryObj, 
                dimensionTypeKey, 
                dimensionType
            );
            
            // 创建 LevelStem
            LevelStem levelStem = new LevelStem(dimensionTypeHolder, chunkGenerator);
            
            // 注册 LevelStem
            ResourceKey<LevelStem> levelStemKey = ResourceKey.create(Registries.LEVEL_STEM, location);
            Object levelStemRegistryObj;
            try {
                java.lang.reflect.Method lookupMethod = registryAccess.getClass()
                    .getMethod("lookupOrThrow", ResourceKey.class);
                levelStemRegistryObj = lookupMethod.invoke(
                    registryAccess, 
                    Registries.LEVEL_STEM
                );
            } catch (Exception e) {
                logError("Failed to get level stem registry: " + e.getMessage());
                throw new RuntimeException(e);
            }
            registerLevelStem(levelStemRegistryObj, levelStemKey, levelStem);
            
            log("Successfully registered dimension: " + dimensionId);
            return true;
            
        } catch (Exception e) {
            logError("Failed to register dimension: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 从配置创建 DimensionType
     */
    private static DimensionType createDimensionType(Bridge.DimensionConfigData.DimensionTypeData config) {
        // 从数据对象获取配置
        OptionalLong fixedTime = config.fixedTime != null 
            ? OptionalLong.of(config.fixedTime) 
            : OptionalLong.empty();
        
        boolean hasSkyLight = config.hasSkyLight;
        boolean hasCeiling = config.hasCeiling;
        boolean ultraWarm = config.ultraWarm;
        boolean natural = config.natural;
        double coordinateScale = config.coordinateScale;
        boolean bedWorks = config.bedWorks;
        boolean respawnAnchorWorks = config.respawnAnchorWorks;
        int minY = config.minY;
        int height = config.height;
        int logicalHeight = config.logicalHeight;
        float ambientLight = config.ambientLight;
        
        // 使用默认值
        TagKey<Block> infiniburnTag = BlockTags.INFINIBURN_OVERWORLD;
        ResourceLocation effectsLocation = ResourceLocation.parse("minecraft:overworld");
        
        // 创建 MonsterSettings（使用默认值）
        DimensionType.MonsterSettings monsterSettings = new DimensionType.MonsterSettings(
            false, // piglinSafe
            true,  // hasRaids
            net.minecraft.util.valueproviders.UniformInt.of(0, 7), // monsterSpawnLightTest
            0      // monsterSpawnBlockLightLimit
        );
        
        return new DimensionType(
            fixedTime,
            hasSkyLight,
            hasCeiling,
            ultraWarm,
            natural,
            coordinateScale,
            bedWorks,
            respawnAnchorWorks,
            minY,
            height,
            logicalHeight,
            infiniburnTag,
            effectsLocation,
            ambientLight,
            Optional.empty(), // cloudHeight
            monsterSettings
        );
    }
    
    /**
     * 创建 ChunkGenerator
     */
    private static ChunkGenerator createChunkGenerator(
        RegistryAccess registryAccess,
        String generatorType
    ) {
        switch (generatorType.toLowerCase()) {
            case "flat":
                return createFlatGenerator(registryAccess);
            
            case "noise":
            default:
                return createNoiseGenerator(registryAccess);
        }
    }
    
    /**
     * 创建平坦生成器
     */
    private static ChunkGenerator createFlatGenerator(RegistryAccess registryAccess) {
        // 使用默认的平坦世界配置
        // 注意：FlatLevelSource.getDefault 的签名可能因版本而异
        // 这里使用简化的方式，直接返回 null 让 Minecraft 使用默认生成器
        logError("Flat generator creation not fully implemented, using noise generator");
        return createNoiseGenerator(registryAccess);
    }
    
    /**
     * 创建噪声生成器
     */
    private static ChunkGenerator createNoiseGenerator(RegistryAccess registryAccess) {
        try {
            // 获取生物群系注册表 - 完全使用 Object 和反射
            java.lang.reflect.Method lookupMethod = registryAccess.getClass()
                .getMethod("lookupOrThrow", ResourceKey.class);
            
            Object biomeRegistryObj = lookupMethod.invoke(registryAccess, Registries.BIOME);
            
            // 获取平原生物群系 - 使用反射
            java.lang.reflect.Method getOrThrowMethod = biomeRegistryObj.getClass()
                .getMethod("getOrThrow", ResourceKey.class);
            Object plainsBiomeObj = getOrThrowMethod.invoke(
                biomeRegistryObj,
                net.minecraft.world.level.biome.Biomes.PLAINS
            );
            @SuppressWarnings("unchecked")
            net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> plainsBiome = 
                (net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome>) plainsBiomeObj;
            
            BiomeSource biomeSource = new net.minecraft.world.level.biome.FixedBiomeSource(plainsBiome);
            
            // 获取噪声设置 - 使用反射
            Object noiseSettingsRegistryObj = lookupMethod.invoke(
                registryAccess, 
                Registries.NOISE_SETTINGS
            );
            
            // 获取主世界的噪声设置 - 使用反射
            java.lang.reflect.Method getOrThrowMethod2 = noiseSettingsRegistryObj.getClass()
                .getMethod("getOrThrow", ResourceKey.class);
            Object noiseSettingsObj = getOrThrowMethod2.invoke(
                noiseSettingsRegistryObj,
                NoiseGeneratorSettings.OVERWORLD
            );
            @SuppressWarnings("unchecked")
            Holder<NoiseGeneratorSettings> noiseSettings = 
                (Holder<NoiseGeneratorSettings>) noiseSettingsObj;
            
            // 创建噪声生成器
            return new NoiseBasedChunkGenerator(biomeSource, noiseSettings);
            
        } catch (Exception e) {
            logError("Failed to create noise generator: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 注册 DimensionType 到注册表
     * 使用反射来修改不可变的注册表
     */
    private static Holder<DimensionType> registerDimensionType(
        Object registryObj,  // 使用 Object 避免泛型问题
        ResourceKey<DimensionType> key,
        DimensionType dimensionType
    ) {
        try {
            // 如果注册表已经包含该键，返回现有的 - 使用反射
            try {
                java.lang.reflect.Method containsKeyMethod = registryObj.getClass()
                    .getMethod("containsKey", ResourceKey.class);
                Boolean contains = (Boolean) containsKeyMethod.invoke(registryObj, key);
                
                if (contains) {
                    logError("DimensionType already registered: " + key.location());
                    java.lang.reflect.Method getOrThrowMethod = registryObj.getClass()
                        .getMethod("getOrThrow", ResourceKey.class);
                    @SuppressWarnings("unchecked")
                    Holder<DimensionType> holder = (Holder<DimensionType>) getOrThrowMethod.invoke(registryObj, key);
                    return holder;
                }
            } catch (Exception e) {
                // 如果反射失败，继续尝试注册
                logError("Failed to check if dimension exists: " + e.getMessage());
            }
            
            // 使用反射来修改注册表
            if (registryObj instanceof net.minecraft.core.MappedRegistry) {
                net.minecraft.core.MappedRegistry<DimensionType> mappedRegistry = 
                    (net.minecraft.core.MappedRegistry<DimensionType>) registryObj;
                
                try {
                    // 临时解冻注册表
                    java.lang.reflect.Field frozenField = net.minecraft.core.MappedRegistry.class.getDeclaredField("frozen");
                    frozenField.setAccessible(true);
                    boolean wasFrozen = frozenField.getBoolean(mappedRegistry);
                    
                    if (wasFrozen) {
                        frozenField.setBoolean(mappedRegistry, false);
                    }
                    
                    // 注册新的 DimensionType - 使用反射
                    java.lang.reflect.Method registerMethod = net.minecraft.core.MappedRegistry.class
                        .getDeclaredMethod("register", ResourceKey.class, Object.class, net.minecraft.core.RegistrationInfo.class);
                    registerMethod.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    Holder.Reference<DimensionType> holder = (Holder.Reference<DimensionType>) registerMethod.invoke(
                        mappedRegistry,
                        key,
                        dimensionType,
                        net.minecraft.core.RegistrationInfo.BUILT_IN
                    );
                    
                    // 重新冻结注册表
                    if (wasFrozen) {
                        frozenField.setBoolean(mappedRegistry, true);
                    }
                    
                    log("Successfully registered DimensionType: " + key.location());
                    return holder;
                    
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    logError("Failed to access registry fields: " + e.getMessage());
                    // 降级到使用主世界类型
                    try {
                        java.lang.reflect.Method getOrThrowMethod = registryObj.getClass()
                            .getMethod("getOrThrow", ResourceKey.class);
                        @SuppressWarnings("unchecked")
                        Holder<DimensionType> holder = (Holder<DimensionType>) getOrThrowMethod.invoke(
                            registryObj, BuiltinDimensionTypes.OVERWORLD);
                        return holder;
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
            
            logError("Registry is not MappedRegistry, using fallback");
            try {
                java.lang.reflect.Method getOrThrowMethod = registryObj.getClass()
                    .getMethod("getOrThrow", ResourceKey.class);
                @SuppressWarnings("unchecked")
                Holder<DimensionType> holder = (Holder<DimensionType>) getOrThrowMethod.invoke(
                    registryObj, BuiltinDimensionTypes.OVERWORLD);
                return holder;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            
        } catch (Exception e) {
            logError("Failed to register DimensionType: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 注册 LevelStem 到注册表
     * 使用反射来修改不可变的注册表
     */
    private static void registerLevelStem(
        Object registryObj,  // 使用 Object 避免泛型问题
        ResourceKey<LevelStem> key,
        LevelStem levelStem
    ) {
        try {
            // 检查是否已存在 - 使用反射
            try {
                java.lang.reflect.Method containsKeyMethod = registryObj.getClass()
                    .getMethod("containsKey", ResourceKey.class);
                Boolean contains = (Boolean) containsKeyMethod.invoke(registryObj, key);
                
                if (contains) {
                    logError("LevelStem already registered: " + key.location());
                    return;
                }
            } catch (Exception e) {
                // 如果反射失败，继续尝试注册
                logError("Failed to check if level stem exists: " + e.getMessage());
            }
            
            // 使用反射来修改注册表
            if (registryObj instanceof net.minecraft.core.MappedRegistry) {
                net.minecraft.core.MappedRegistry<LevelStem> mappedRegistry = 
                    (net.minecraft.core.MappedRegistry<LevelStem>) registryObj;
                
                try {
                    // 临时解冻注册表
                    java.lang.reflect.Field frozenField = net.minecraft.core.MappedRegistry.class.getDeclaredField("frozen");
                    frozenField.setAccessible(true);
                    boolean wasFrozen = frozenField.getBoolean(mappedRegistry);
                    
                    if (wasFrozen) {
                        frozenField.setBoolean(mappedRegistry, false);
                    }
                    
                    // 注册新的 LevelStem - 使用反射
                    java.lang.reflect.Method registerMethod = net.minecraft.core.MappedRegistry.class
                        .getDeclaredMethod("register", ResourceKey.class, Object.class, net.minecraft.core.RegistrationInfo.class);
                    registerMethod.setAccessible(true);
                    registerMethod.invoke(
                        mappedRegistry,
                        key,
                        levelStem,
                        net.minecraft.core.RegistrationInfo.BUILT_IN
                    );
                    
                    // 重新冻结注册表
                    if (wasFrozen) {
                        frozenField.setBoolean(mappedRegistry, true);
                    }
                    
                    log("Successfully registered LevelStem: " + key.location());
                    
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    logError("Failed to access registry fields: " + e.getMessage());
                    throw new RuntimeException("Cannot register LevelStem", e);
                }
            } else {
                logError("Registry is not MappedRegistry, cannot register LevelStem");
                throw new RuntimeException("Unsupported registry type");
            }
            
        } catch (Exception e) {
            logError("Failed to register LevelStem: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
