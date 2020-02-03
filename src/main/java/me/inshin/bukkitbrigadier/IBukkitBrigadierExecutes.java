/*
 * Copyright (c) 2020. InShin. All rights reserved.
 */

package me.inshin.bukkitbrigadier;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface IBukkitBrigadierExecutes<S> {
    int run(@NotNull CommandContext<Object> context, @Nullable S sender) throws CommandSyntaxException;

    /**
     * 获取参数值
     * 注：不存在时会抛出异常
     *
     * @param context Context
     * @param name    参数名称
     * @param clazz   参数值的类型
     * @param <T>     值类型
     * @return 参数值
     */
    default <T> T getValue(@NotNull CommandContext<Object> context, String name, Class<T> clazz) {
        return context.getArgument(name, clazz);
    }

    /**
     * 获取参数值，不存在时返回一个默认值
     *
     * @param context Context
     * @param name    参数名称
     * @param def     默认值
     * @param clazz   参数值的类型
     * @param <T>     值类型
     * @return 参数值
     */
    default <T> T getOrDefault(@NotNull CommandContext<Object> context, @Nullable String name, T def, Class<T> clazz) {
        try {
            return context.getArgument(name, clazz);
        } catch (IllegalArgumentException e) {
            if (!e.getMessage().startsWith("No such argument")) {
                e.printStackTrace();
            }
        }
        return def;
    }
}
