/*
 * Copyright (c) 2020. InShin. All rights reserved.
 */

package me.inshin.bukkitbrigadier;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;

/**
 * 插件主类继承该类更容易使用哦
 * 适合有多个命令的插件
 * !!!未测试!!!
 */
public abstract class MultipleBukkitBrigadierJavaPlugin extends JavaPlugin {
    private final HashSet<String> commands = new HashSet<>();

    @Nullable
    protected abstract List<LiteralArgumentBuilder<Object>> onCreateCommands();

    @Override
    public void onEnable() {
        List<LiteralArgumentBuilder<Object>> builders = onCreateCommands();
        if (builders != null && !builders.isEmpty()) {
            for (LiteralArgumentBuilder<Object> builder : builders) {
                this.commands.add(builder.getLiteral());
                BukkitBrigadier.register(builder);
            }
        }
    }

    @Override
    public void onDisable() {
        BukkitBrigadier.unregisters(this.commands.toArray(new String[0]));
        this.commands.clear();
    }
}
