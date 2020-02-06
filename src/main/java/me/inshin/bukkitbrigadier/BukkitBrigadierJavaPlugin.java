/*
 * Copyright (c) 2020. InShin. All rights reserved.
 */

package me.inshin.bukkitbrigadier;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * 插件主类继承该类更容易使用哦
 */
public abstract class BukkitBrigadierJavaPlugin extends JavaPlugin {
    /**
     * 创建命令
     *
     * @return 命令构建器
     */
    @Nullable
    protected abstract Collection<LiteralArgumentBuilder<Object>> onCreateCommands();

    @Override
    public void onEnable() {
        // 实例化命令
        Collection<LiteralArgumentBuilder<Object>> builders = onCreateCommands();
        if (builders != null && !builders.isEmpty()) {
            // 注册命令
            BukkitBrigadier.registers(getClass(), builders);
        }
    }

    @Override
    public void onDisable() {
        // 注销命令
        BukkitBrigadier.unregisters(getClass());
    }
}
