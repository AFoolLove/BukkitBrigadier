/*
 * Copyright (c) 2020. InShin. All rights reserved.
 */

package me.inshin.bukkitbrigadier;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import java.util.Collection;
import java.util.Collections;

public final class BukkitBrigadierPlugin extends BukkitBrigadierJavaPlugin {

    @Override
    public void onEnable() {
        BrigadierManager.DEBUG_LOG = getConfig().getBoolean("debug-log", false);
        super.onEnable();
    }

    @Override
    protected Collection<LiteralArgumentBuilder<Object>> onCreateCommands() {
        return Collections.singletonList(
                LiteralArgumentBuilder.literal("bar")
                        .executes(
                                BukkitBrigadier.senderExecutes((context, sender) -> {
                                    sender.sendMessage(String.format("[%s] 嘤？？", getName()));
                                    return 1;
                                })
                        )
                        .then(
                                LiteralArgumentBuilder.literal("bar")
                                        .executes(BukkitBrigadier.senderExecutes((context, sender) -> {
                                            sender.sendMessage(String.format("[%s] 嘤嘤？", getName()));
                                            return 1;
                                        }))
                                        .then(
                                                RequiredArgumentBuilder.argument("嘤嘤？", StringArgumentType.greedyString())
                                                        .executes(BukkitBrigadier.senderExecutes((context, sender) -> {
                                                            sender.sendMessage(String.format("[%s] %s", getName(), StringArgumentType.getString(context, "嘤嘤？")));
                                                            return 1;
                                                        }))
                                                        .suggests((context, builder) -> {
                                                            String argument = BukkitBrigadier.getContextArgument(context, "嘤嘤？", null, String.class);
                                                            if (argument != null) {
                                                                builder.suggest("嘤嘤嘤");
                                                            } else {
                                                                builder.suggest("嘤嘤怪");
                                                            }
                                                            return builder.buildFuture();
                                                        })
                                        )
                        )
        );
    }
}
