package org.virgil.nekokjs.mixin;

import org.leavesmc.plugin.mixin.condition.ConditionalMixinConfigPlugin;

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
        super.onLoad(mixinPackage);
        System.out.println("[NekoKJS] Mixin 配置加载完成，包名: " + mixinPackage);
    }
    
    @Override
    public String getRefMapperConfig() {
        return null;
    }
}
