package org.virgil.nekokjs.mixin.mixins.worldgen;

import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.virgil.nekokjs.mixin.bridge.BridgeManager;

import java.util.concurrent.CompletableFuture;

/**
 * NoiseBasedChunkGenerator Mixin
 * 用于拦截噪声地形生成过程
 * 
 * 功能：
 * 1. 拦截地表生成 (buildSurface)
 * 2. 拦截噪声填充 (fillFromNoise)
 * 3. 拦截洞穴雕刻 (applyCarvers)
 */
@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseBasedChunkGeneratorMixin {
    
    /**
     * 拦截地表生成
     * 在 buildSurface 方法开始时注入
     * 允许 JavaScript 脚本自定义地表生成
     */
    @Inject(
            method = "buildSurface(Lnet/minecraft/server/level/WorldGenRegion;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/chunk/ChunkAccess;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void nekokjs$onBuildSurface(
            WorldGenRegion level,
            StructureManager structureManager,
            RandomState random,
            ChunkAccess chunk,
            CallbackInfo ci
    ) {
        if (BridgeManager.INSTANCE.getBridge() != null) {
            boolean handled = BridgeManager.INSTANCE.getBridge()
                    .onChunkSurfaceGenerate(level, chunk);
            
            if (handled) {
                // 如果脚本处理了地表生成，取消原版逻辑
                ci.cancel();
            }
        }
    }
    
    /**
     * 拦截噪声填充
     * 在 fillFromNoise 方法开始时注入
     * 允许 JavaScript 脚本自定义地形噪声
     */
    @Inject(
            method = "fillFromNoise",
            at = @At("HEAD"),
            cancellable = true
    )
    private void nekokjs$onFillFromNoise(
            Blender blender,
            RandomState randomState,
            StructureManager structureManager,
            ChunkAccess chunk,
            CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir
    ) {
        if (BridgeManager.INSTANCE.getBridge() != null) {
            CompletableFuture<ChunkAccess> result = BridgeManager.INSTANCE.getBridge()
                    .onChunkNoiseGenerate(chunk, randomState);
            
            if (result != null) {
                // 如果脚本提供了自定义噪声生成，使用脚本的结果
                cir.setReturnValue(result);
            }
        }
    }
    
    /**
     * 拦截洞穴雕刻
     * 在 applyCarvers 方法开始时注入
     * 允许 JavaScript 脚本自定义洞穴生成
     */
    @Inject(
            method = "applyCarvers",
            at = @At("HEAD"),
            cancellable = true
    )
    private void nekokjs$onApplyCarvers(
            WorldGenRegion level,
            long seed,
            RandomState random,
            BiomeManager biomeManager,
            StructureManager structureManager,
            ChunkAccess chunk,
            CallbackInfo ci
    ) {
        if (BridgeManager.INSTANCE.getBridge() != null) {
            boolean handled = BridgeManager.INSTANCE.getBridge()
                    .onChunkCarverGenerate(level, chunk, seed);
            
            if (handled) {
                // 如果脚本处理了洞穴雕刻，取消原版逻辑
                ci.cancel();
            }
        }
    }
}
