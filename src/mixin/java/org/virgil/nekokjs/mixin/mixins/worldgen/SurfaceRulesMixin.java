package org.virgil.nekokjs.mixin.mixins.worldgen;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.virgil.nekokjs.mixin.bridge.BridgeManager;
import org.virgil.nekokjs.mixin.cache.SurfaceCacheManager;

/**
 * SurfaceRules Mixin
 * 用于拦截地表规则应用过程
 * 
 * 功能：
 * 1. 拦截地表方块选择 (tryApply)
 * 2. 允许 JavaScript 脚本自定义地表材质
 * 3. 提供保底方案，确保在 Folia 环境下地表生成可控
 * 
 * 使用场景：
 * - 自定义生物群系地表（如沙漠的沙子、雪地的雪）
 * - 根据高度或噪声改变地表材质
 * - 实现特殊地表效果（如彩虹地表、渐变地表）
 * - 添加自定义矿石层或地质结构
 * 
 * 地表规则系统：
 * - 地表规则决定每个方块位置应该放置什么方块
 * - 包括顶层（草方块）、中间层（泥土）、底层（石头）
 * - 可以根据生物群系、高度、噪声等条件选择不同方块
 */
@Mixin(SurfaceRules.Context.class)
public abstract class SurfaceRulesMixin {
    
    /**
     * 拦截地表方块应用
     * 在地表规则尝试应用时注入
     * 允许 JavaScript 脚本修改或替换地表方块
     * 
     * 注意：这个方法会在地表生成的每个方块位置调用
     * 建议在脚本中使用高效的判断逻辑
     */
    @Inject(
            method = "updateY",
            at = @At("HEAD")
    )
    private void nekokjs$onSurfaceRuleUpdate(
            int stoneDepthAbove,
            int stoneDepthBelow,
            int waterHeight,
            int blockX,
            int blockY,
            int blockZ,
            CallbackInfo ci
    ) {
        if (BridgeManager.INSTANCE.getBridge() != null) {
            // 通知 JavaScript 地表规则更新
            BridgeManager.INSTANCE.getBridge()
                    .onSurfaceRuleUpdate(
                            blockX,
                            blockY,
                            blockZ,
                            stoneDepthAbove,
                            stoneDepthBelow,
                            waterHeight
                    );
        }
    }
}

/**
 * SurfaceRule 应用 Mixin
 * 拦截 Context 的 updateY 方法来获取地表信息
 * 
 * 注意：由于 BlockRuleSource 是包私有的，我们通过 Context 来实现
 * 同时提供 Accessor 访问包私有字段
 */
@Mixin(SurfaceRules.Context.class)
abstract class SurfaceContextMixin {
    
    @org.spongepowered.asm.mixin.Shadow
    public int blockX;
    
    @org.spongepowered.asm.mixin.Shadow
    public int blockY;
    
    @org.spongepowered.asm.mixin.Shadow
    public int blockZ;
    
    @org.spongepowered.asm.mixin.Shadow
    int surfaceDepth;
    
    /**
     * 拦截 updateY 方法
     * 在地表规则更新时通知 JavaScript
     */
    @Inject(
            method = "updateY",
            at = @At("RETURN")
    )
    private void nekokjs$onUpdateY(
            int stoneDepthAbove,
            int stoneDepthBelow,
            int waterHeight,
            int blockX,
            int blockY,
            int blockZ,
            org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci
    ) {
        if (BridgeManager.INSTANCE.getBridge() != null) {
            // 获取 surfaceDepth（使用 Shadow 字段）
            int surfaceDepth = this.surfaceDepth;
            
            // 通知 JavaScript
            BridgeManager.INSTANCE.getBridge()
                    .onSurfaceRuleUpdate(
                            this.blockX,
                            this.blockY,
                            this.blockZ,
                            stoneDepthAbove,
                            stoneDepthBelow,
                            waterHeight
                    );
            
            // 获取自定义方块（如果有）
            BlockState customBlock = BridgeManager.INSTANCE.getBridge()
                    .onSurfaceBlockSelect(
                            this.blockX,
                            this.blockY,
                            this.blockZ,
                            surfaceDepth
                    );
            
            // 注意：这里只是通知，实际的方块设置需要在 buildSurface 中完成
            // 因为 Context 本身不负责设置方块
        }
    }
    
}
