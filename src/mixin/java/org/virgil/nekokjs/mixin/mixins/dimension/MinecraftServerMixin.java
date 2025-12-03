package org.virgil.nekokjs.mixin.mixins.dimension;

import org.virgil.nekokjs.mixin.bridge.Bridge;
import org.virgil.nekokjs.mixin.bridge.BridgeManager;
import org.virgil.nekokjs.mixin.dimension.DimensionRegistrar;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MinecraftServer Mixin
 * 用于注入服务器生命周期事件和自定义维度
 * 
 * 功能：
 * 1. 服务器启动完成时触发 onServerStarted
 * 2. 服务器每 tick 触发 onServerTick
 * 3. 在世界加载前注入自定义维度
 */
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    
    @Shadow
    public abstract RegistryAccess.Frozen registryAccess();
    
    /**
     * 在服务器启动完成后注入
     * 注入点：runServer 方法中第一次调用 Logger.info 之后
     */
    @Inject(
            method = "runServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;)V",
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    private void nekokjs$onServerStarted(CallbackInfo ci) {
        if (BridgeManager.INSTANCE.getBridge() != null) {
            BridgeManager.INSTANCE.getBridge().onServerStarted();
        }
    }

    /**
     * 在服务器每 tick 时注入
     * 注入点：tickServer 方法开始时
     */
    @Inject(
            method = "tickServer",
            at = @At("HEAD")
    )
    private void nekokjs$onServerTick(CallbackInfo ci) {
        if (BridgeManager.INSTANCE.getBridge() != null) {
            BridgeManager.INSTANCE.getBridge().onServerTick();
        }
    }
    
    /**
     * 在世界加载前注入自定义维度
     * 注入点：loadWorld0 方法开始时（Leaves/Luminol 都有这个方法）
     * 
     * 注意：这个方法在 CraftBukkit 中用于加载世界
     */
    @Inject(
            method = "loadWorld0",
            at = @At("HEAD")
    )
    private void nekokjs$onBeforeLoadWorlds(String levelId, CallbackInfo ci) {
        if (BridgeManager.INSTANCE.getBridge() != null) {
            // 获取自定义维度配置
            java.util.List<Bridge.DimensionConfigData> configs = 
                BridgeManager.INSTANCE.getBridge().getCustomDimensionConfigs();
            
            if (!configs.isEmpty()) {
                System.out.println("[NekoKJS] Loading " + configs.size() + " custom dimension(s)...");
                
                // 获取注册表访问器
                RegistryAccess registryAccess = this.registryAccess();
                
                // 注册每个自定义维度
                int successCount = 0;
                for (Bridge.DimensionConfigData config : configs) {
                    try {
                        if (DimensionRegistrar.registerDimension(registryAccess, config)) {
                            successCount++;
                        }
                    } catch (Exception e) {
                        System.err.println("[NekoKJS] Failed to register dimension: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                
                System.out.println("[NekoKJS] Successfully registered " + successCount + "/" + configs.size() + " custom dimension(s)");
            }
        }
    }
}
