/*
 * Copyright (c) 2020. InShin. All rights reserved.
 */

package me.inshin.bukkitbrigadier;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public final class BukkitBrigadierPlugin extends BukkitBrigadierJavaPlugin {

    @Override
    public void onEnable() {
        BrigadierManager.DEBUG_LOG = getConfig().getBoolean("debug-log", false);
        super.onEnable();

        ConfigurationSection updateCommands = getConfig().getConfigurationSection("update-commands");
        if (updateCommands != null) {
            BrigadierManager.syncCommandsRegister = updateCommands.getBoolean("register", true);
            BrigadierManager.syncCommandsUnregister = updateCommands.getBoolean("unregister", false);
        }
    }

    @Override
    protected Collection<LiteralArgumentBuilder<Object>> onCreateCommands() {
        return Collections.singletonList(
                LiteralArgumentBuilder.literal("bar")
                        .executes(
                                BukkitBrigadier.senderExecutes((context, base) -> {
                                    base.ifPresent(sender -> sender.sendMessage(String.format("[%s] 嘤？？", getName())));
                                    return 1;
                                })
                        )
                        .then(
                                LiteralArgumentBuilder.literal("bar")
                                        .executes(BukkitBrigadier.senderExecutes((context, base) -> {
                                            base.ifPresent(sender -> sender.sendMessage(String.format("[%s] 嘤嘤？", getName())));
                                            return 1;
                                        }))
                                        .then(
                                                RequiredArgumentBuilder.argument("嘤嘤？", StringArgumentType.greedyString())
                                                        .executes(BukkitBrigadier.senderExecutes((context, base) -> {
                                                            base.ifPresent(sender -> sender.sendMessage(String.format("[%s] %s", getName(), StringArgumentType.getString(context, "嘤嘤？"))));
                                                            return 1;
                                                        }))
                                                        .suggests((context, builder) -> {
                                                            Optional<String> argument = BukkitBrigadier.getContextArgument(context, "嘤嘤？", String.class);
                                                            builder.suggest(argument.isPresent() ? "嘤嘤嘤" : "嘤嘤怪");
                                                            return builder.buildFuture();
                                                        })
                                        )
                        )
        );
    }
}
