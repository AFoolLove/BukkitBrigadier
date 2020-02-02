/*
 * Copyright (c) 2020. InShin. All rights reserved.
 */

package me.inshin.bukkitbrigadier;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

/**
 * 插件主类继承该类更容易使用哦
 * 适合只有单个命令的插件
 */
public abstract class BukkitBrigadierJavaPlugin extends JavaPlugin {
    private String command;

    /**
     * 创建命令
     *
     * @return 命令构建器
     */
    @Nullable
    protected abstract LiteralArgumentBuilder<Object> onCreateCommand();

    @Override
    public void onEnable() {
        // 实例化命令
        LiteralArgumentBuilder<Object> builder = onCreateCommand();
        if (builder != null) {
            // 得到root命令
            this.command = builder.getLiteral();
            // 注册命令
            BukkitBrigadier.register(builder);
        }
    }

    @Override
    public void onDisable() {
        if (this.command != null) {
            // 注销命令
            BukkitBrigadier.unregister(this.command);
        }
    }
}
