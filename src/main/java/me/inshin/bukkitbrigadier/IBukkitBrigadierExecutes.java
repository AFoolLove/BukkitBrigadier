/*
 * Copyright (c) 2020. InShin. All rights reserved.
 */

package me.inshin.bukkitbrigadier;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@FunctionalInterface
public interface IBukkitBrigadierExecutes<S> {
    int run(@NotNull CommandContext<Object> context, @NotNull Optional<S> sender) throws CommandSyntaxException;
}
