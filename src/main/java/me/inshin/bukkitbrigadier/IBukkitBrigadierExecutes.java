/*
 * Copyright (c) 2020. InShin. All rights reserved.
 */

package me.inshin.bukkitbrigadier;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

@FunctionalInterface
public interface IBukkitBrigadierExecutes<T> {
    int run(@NotNull CommandContext<Object> context, @Nullable T sender) throws CommandSyntaxException;
}
