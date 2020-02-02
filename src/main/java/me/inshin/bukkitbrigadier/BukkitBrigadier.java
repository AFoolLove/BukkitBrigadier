/*
 * Copyright (c) 2020. InShin. All rights reserved.
 */

package me.inshin.bukkitbrigadier;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class BukkitBrigadier {
    private static final Class<?> MC_SERVER_CLAZZ;

    static {
        try {
            Class<?> serverClazz = Bukkit.getServer().getClass();
            String mcServerClazzName = serverClazz.getPackage().getName().replaceFirst("org.bukkit.craftbukkit", "net.minecraft.server") + ".MinecraftServer";
            MC_SERVER_CLAZZ = serverClazz.getClassLoader().loadClass(mcServerClazzName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 注册命令
     * @param builder 命令构建器
     */
    public static void register(@NotNull LiteralArgumentBuilder<Object> builder) {
        CommandDispatcher<Object> commandDispatcher = getCommandDispatcher();
        if (commandDispatcher != null) {
            commandDispatcher.register(builder);
        }
    }

    /**
     * 注销命令
     * @param cmd 要注销的命令
     */
    public static void unregister(@NotNull String cmd) {
        CommandDispatcher<Object> commandDispatcher = getCommandDispatcher();
        if (commandDispatcher != null) {
            try {
                removeCommand(commandDispatcher.getRoot(), "children", cmd);
                removeCommand(commandDispatcher.getRoot(), "literals", cmd);
                removeCommand(commandDispatcher.getRoot(), "arguments", cmd);
            } catch (NoSuchFieldException | IllegalAccessException e) {
//                e.printStackTrace();
            }
        }
    }

    /**
     * 批量注销命令
     * @param commands 要注销的命令
     */
    public static void unregisters(@NotNull String... commands) {
        if (commands.length == 0) {
            return;
        }
        if (commands.length == 1) {
            unregister(commands[0]);
            return;
        }

        CommandDispatcher<Object> commandDispatcher = getCommandDispatcher();
        if (commandDispatcher != null) {
            try {
                for (String command : commands) {
                    removeCommand(commandDispatcher.getRoot(), "children", command);
                    removeCommand(commandDispatcher.getRoot(), "literals", command);
                    removeCommand(commandDispatcher.getRoot(), "arguments", command);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 创建一个命令解析器，只能转换为 {@link org.bukkit.entity.Entity} 能转换的类
     * @param clazz 要转换的类
     * @param object 命令
     * @param <T> 类泛型
     * @return 命令
     */
    @NotNull
    public static <T> Command<Object> executes(@NotNull final Class<T> clazz, @NotNull final IBukkitBrigadierExecutes<T> object) {
        return context -> {
            try {
                Object entity = context.getSource().getClass().getMethod("getEntity").invoke(context.getSource());
                if (entity != null) {
                    Object bukkitEntity = entity.getClass().getMethod("getBukkitEntity").invoke(entity);
                    if (clazz.isAssignableFrom(bukkitEntity.getClass())) {
                        return object.run(context, clazz.cast(bukkitEntity));
                    }
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            return object.run(context, null);
        };
    }

    /**
     * 创建一个能直接调用 CommandSender 的命令解析器
     * @param sender 命令
     * @return 命令
     */
    @NotNull
    public static Command<Object> senderExecutes(@NotNull final IBukkitBrigadierExecutes<CommandSender> sender) {
        return executes(CommandSender.class, sender);
    }

    @Nullable
    private static CommandDispatcher<Object> getCommandDispatcher() {
        try {
            Object mcServer = Bukkit.getServer().getClass().getMethod("getServer").invoke(Bukkit.getServer());
            Object mcCommandDispatcher = MC_SERVER_CLAZZ.getMethod("getCommandDispatcher").invoke(mcServer);

            Class<?> mcCommandDispatcherClazz = mcCommandDispatcher.getClass();
            for (Method method : mcCommandDispatcherClazz.getDeclaredMethods()) {
                if (method.getReturnType().isAssignableFrom(CommandDispatcher.class)) {
                    return (CommandDispatcher<Object>) method.invoke(mcCommandDispatcher);
                }
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void removeCommand(RootCommandNode<Object> root, String name, String cmd) throws NoSuchFieldException, IllegalAccessException {
        Field field = CommandNode.class.getDeclaredField(name);
        field.setAccessible(true);
        Object object = field.get(root);
        if (object instanceof Map) {
            ((Map) object).remove(cmd);
        }
    }
}
