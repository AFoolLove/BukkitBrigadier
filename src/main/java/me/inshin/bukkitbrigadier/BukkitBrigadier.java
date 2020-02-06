/*
 * Copyright (c) 2020. InShin. All rights reserved.
 */

package me.inshin.bukkitbrigadier;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

public class BukkitBrigadier {
    /**
     * 注册命令
     *
     * @param builders 命令构建器
     */
    public static <T extends JavaPlugin> void registers(@NotNull Class<T> pluginClazz, @NotNull Collection<LiteralArgumentBuilder<Object>> builders) {
        BrigadierManager.addRootCommands(pluginClazz, null, builders);
    }

    /**
     * 注销命令
     *
     * @param pluginClazz 要注销的命令的插件
     */
    public static <T extends JavaPlugin> void unregisters(@NotNull Class<T> pluginClazz) {
        BrigadierManager.removeRootCommands(pluginClazz);
    }

    /**
     * 创建一个命令解析器，只能转换为 {@link org.bukkit.entity.Entity} 能转换的类
     *
     * @param clazz    要转换的类
     * @param executes 命令
     * @param <T>      类泛型
     * @return 命令
     */
    @NotNull
    public static <T> Command<Object> executes(@NotNull final Class<T> clazz, @NotNull final IBukkitBrigadierExecutes<T> executes) {
        return context -> {
            if (clazz == CommandSender.class) {
                CommandSender sender = getContextSender(context);
                if (sender != null) {
                    return executes.run(context, clazz.cast(sender));
                }
            }
            Entity entity = getContextEntity(context);
            if (entity != null && clazz.isAssignableFrom(entity.getClass())) {
                return executes.run(context, clazz.cast(entity));
            }
            return executes.run(context, null);
        };
    }

    /**
     * 创建一个能直接调用 CommandSender 的命令解析器
     *
     * @param executes 命令
     * @return 命令
     */
    @NotNull
    public static Command<Object> senderExecutes(@NotNull final IBukkitBrigadierExecutes<CommandSender> executes) {
        return executes(CommandSender.class, executes);
    }

    /**
     * 获取到执行命令的实体
     * 在 suggests 中也能获取到
     *
     * @param context Context
     * @return 执行命令的实体
     */
    public static Entity getContextEntity(@NotNull CommandContext<Object> context) {
        Class<?> sourceClazz = context.getSource().getClass();
        try {
            // object net.minecraft.server.v1_15_R1.CommandListenerWrapper#getEntity
            Object entity = sourceClazz.getMethod("getEntity").invoke(context.getSource());
            if (entity != null) {
                // object net.minecraft.server.v1_15_R1.Entity#getBukkitEntity
                return (Entity) entity.getClass().getMethod("getBukkitEntity").invoke(entity);
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取到命令发送者
     * 在 suggests 中也能获取到
     *
     * @param context Context
     * @return 命令发送者
     */
    public static CommandSender getContextSender(@NotNull CommandContext<Object> context) {
        try {
            Class<?> sourceClazz = context.getSource().getClass();
            // object net.minecraft.server.v1_15_R1.CommandListenerWrapper#getBukkitSender
            Object bukkitSender = sourceClazz.getMethod("getBukkitSender").invoke(context.getSource());
            if (bukkitSender instanceof CommandSender) {
                return (CommandSender) bukkitSender;
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取参数的值，并转换成指定的类型
     * <p>
     * 说实话这个挺糟糕的，在原方法中
     * 参数还没有值（玩家正打算输入）的时候，这时获取参数值无效（玩家还没输入，还是空的）就会直接抛出异常
     * 还不给判断有没有的方法，我当时就***了
     *
     * @param context Context
     * @param name    参数名
     * @param def     未获取到时返回的值
     * @param clazz   参数值的类型
     * @return 参数的值
     */
    public static <T> T getContextArgument(@NotNull CommandContext<Object> context, @NotNull String name, @Nullable T def, Class<T> clazz) {
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
