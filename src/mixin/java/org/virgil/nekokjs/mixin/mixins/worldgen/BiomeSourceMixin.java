package org.virgil.nekokjs.mixin.mixins.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.virgil.nekokjs.mixin.bridge.BridgeManager;

/**
 * BiomeSource Mixin
 * 用于拦截生物群系选择过程
 * 
 * 功能：
 * 1. 拦截生物群系获取 (getNoiseBiome)
 * 2. 允许 JavaScript 脚本自定义生物群系分布
 * 
 * 注意：改为 Mixin MultiNoiseBiomeSource（最常用的实现类）
 */
@Mixin(MultiNoiseBiomeSource.class)
public abstract class BiomeSourceMixin {
    
    /**
     * 拦截生物群系获取
     * 在 MultiNoiseBiomeSource.getNoiseBiome 方法返回前注入
     * 
     * @param x x 坐标（四分之一方块）
     * @param y y 坐标（四分之一方块）
     * @param z z 坐标（四分之一方块）
     * @param sampler 气候采样器
     * @param cir 回调信息
     */
    @Inject(
            method = "getNoiseBiome(IIILnet/minecraft/world/level/biome/Climate$Sampler;)Lnet/minecraft/core/Holder;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void nekokjs$onGetNoiseBiome(
            int x,
            int y,
            int z,
            Climate.Sampler sampler,
            CallbackInfoReturnable<Holder<Biome>> cir
    ) {
        if (BridgeManager.INSTANCE.getBridge() != null) {
            try {
                Holder<Biome> customBiome = BridgeManager.INSTANCE.getBridge()
                        .onBiomeSelect(x, y, z, sampler);
                
                if (customBiome != null) {
                    cir.setReturnValue(customBiome);
                }
            } catch (Exception e) {
                // 忽略错误
            }
        }
    }
}
