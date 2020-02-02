/*
 * Copyright (c) 2020. InShin. All rights reserved.
 */

package me.inshin.bukkitbrigadier;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import org.jetbrains.annotations.Nullable;

public final class BukkitBrigadierPlugin extends BukkitBrigadierJavaPlugin {
    @Override
    @Nullable
    protected LiteralArgumentBuilder<Object> onCreateCommand() {
        return null;
    }

//    @Override
//    protected LiteralArgumentBuilder<Object> onCreateCommand() {
//        return LiteralArgumentBuilder.literal("foo")
//                .executes(
//                        BukkitBrigadier.senderExecutes((context, sender) -> {
//                            sender.sendMessage(String.format("[%s] 嘤？", getName()));
//                            return 1;
//                        })
//                )
//                .then(
//                        LiteralArgumentBuilder.literal("bar")
//                                .executes(BukkitBrigadier.senderExecutes((context, sender) -> {
//                                    sender.sendMessage(String.format("[%s] 嘤嘤？", getName()));
//                                    return 1;
//                                }))
//                                .then(
//                                        RequiredArgumentBuilder.argument("嘤嘤？", StringArgumentType.greedyString())
//                                                .executes(BukkitBrigadier.senderExecutes((context, sender) -> {
//                                                    if (sender instanceof Player) {
//                                                        sender.sendMessage(String.format("[%s] %s", getName(), StringArgumentType.getString(context, "嘤嘤？")));
//                                                    }
//                                                    return 1;
//                                                }))
//                                                .suggests((context, builder) -> {
//                                                    builder.suggest("嘤嘤嘤");
//                                                    return builder.buildFuture();
//                                                })
//                                )
//                );
//    }
}
