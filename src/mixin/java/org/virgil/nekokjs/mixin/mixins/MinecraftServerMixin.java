package org.virgil.nekokjs.mixin.mixins;

import org.virgil.nekokjs.mixin.bridge.BridgeManager;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MinecraftServer Mixin
 * 用于注入服务器生命周期事件
 * 
 * 功能：
 * 1. 服务器启动完成时触发 onServerStarted
 * 2. 服务器每 tick 触发 onServerTick
 */
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    
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
}
