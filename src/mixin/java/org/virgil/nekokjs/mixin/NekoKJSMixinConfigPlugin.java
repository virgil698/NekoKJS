package org.virgil.nekokjs.mixin;

import org.leavesmc.plugin.mixin.condition.ConditionalMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * NekoKJS Mixin 配置插件
 * 继承 ConditionalMixinConfigPlugin 以支持条件 Mixin
 * 
 * 这个类用于配置 Mixin 的加载行为，可以在这里：
 * 1. 动态决定是否加载某个 Mixin
 * 2. 修改 Mixin 的配置
 * 3. 添加自定义的 Mixin 处理逻辑
 */
public class NekoKJSMixinConfigPlugin extends ConditionalMixinConfigPlugin {
    
    @Override
    public void onLoad(String mixinPackage) {
        try {
            System.out.println("[NekoKJS] Mixin 配置开始加载，包名: " + mixinPackage);
            super.onLoad(mixinPackage);
            System.out.println("[NekoKJS] Mixin 配置加载完成");
        } catch (Exception e) {
            System.err.println("[NekoKJS] Mixin 配置加载失败！");
            e.printStackTrace();
            throw e;
        }
    }
    
    @Override
    public String getRefMapperConfig() {
        return null;
    }
    
    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        try {
            System.out.println("[NekoKJS] 检查 Mixin: " + mixinClassName + " -> " + targetClassName);
            boolean result = super.shouldApplyMixin(targetClassName, mixinClassName);
            System.out.println("[NekoKJS] Mixin " + mixinClassName + " 应用结果: " + result);
            return result;
        } catch (Exception e) {
            System.err.println("[NekoKJS] Mixin 应用检查失败: " + mixinClassName);
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
        try {
            System.out.println("[NekoKJS] 接受目标类: " + myTargets);
            super.acceptTargets(myTargets, otherTargets);
        } catch (Exception e) {
            System.err.println("[NekoKJS] 接受目标类失败！");
            e.printStackTrace();
            throw e;
        }
    }
    
    @Override
    public List<String> getMixins() {
        try {
            List<String> mixins = super.getMixins();
            System.out.println("[NekoKJS] 获取 Mixin 列表: " + mixins);
            return mixins;
        } catch (Exception e) {
            System.err.println("[NekoKJS] 获取 Mixin 列表失败！");
            e.printStackTrace();
            throw e;
        }
    }
}
