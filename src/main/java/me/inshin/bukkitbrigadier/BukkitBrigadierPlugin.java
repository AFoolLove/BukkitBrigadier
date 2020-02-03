/*
 * Copyright (c) 2020. InShin. All rights reserved.
 */

package me.inshin.bukkitbrigadier;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import org.bukkit.entity.Player;

public final class BukkitBrigadierPlugin extends BukkitBrigadierJavaPlugin {

//    @Override
//    @Nullable
//    protected LiteralArgumentBuilder<Object> onCreateCommand() {
//        return null;
//    }


    @Override
    public void onEnable() {
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            if (thread.getName().toLowerCase().contains("watchdog")) {
                try {
                    thread.stop();
                } catch (Exception ignored) {
                }
                System.out.println("Kill.");
                break;
            }
        }
        super.onEnable();
    }

    @Override
    protected LiteralArgumentBuilder<Object> onCreateCommand() {
        return LiteralArgumentBuilder.literal("foo")
                .executes(
                        BukkitBrigadier.senderExecutes((context, sender) -> {
                            sender.sendMessage(String.format("[%s] 嘤？", getName()));
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
                                                    try {
                                                        String string = context.getArgument("嘤嘤？", String.class);
                                                        if (string.isEmpty() || "嘤嘤嘤".contains(string)) {
                                                            builder.suggest("嘤嘤嘤");
                                                        }
                                                        return builder.buildFuture();
                                                    } catch (IllegalArgumentException ignored) {
                                                    }
                                                    builder.suggest("嘤嘤嘤");
                                                    return builder.buildFuture();
                                                })
                                )
                );
    }
}
