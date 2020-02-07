/*
 * Copyright (c) 2020. InShin. All rights reserved.
 */

package me.inshin.bukkitbrigadier;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_15_R1.CraftServer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 所有未经过检查的转换都是1.13到1.15.2均可以正常转换
 * 如果抛出转换异常，那么这个插件版本对该服务器毫无用处
 */
class BrigadierManager {
    public static Boolean DEBUG_LOG = false;
    private static final Class<?> VANILLA_COMMAND_WRAPPER_CLAZZ; // class org.bukkit.craftbukkit.v1_15_R1.command.VanillaCommandWrapper
    private static final Class<?> MC_SERVER_CLAZZ; // class net.minecraft.server.v1_15_R1.MinecraftServer
    private static final Class<?> MC_COMMAND_DISPATCHER; // class net.minecraft.server.v1_15_R1.CommandDispatcher

    public static boolean syncCommandsRegister = true;
    public static boolean syncCommandsUnregister = false;

    static {
        try {
            Class<?> serverClazz = Bukkit.getServer().getClass();
            // package org.bukkit.craftbukkit.v1_15_R1
            String _package = serverClazz.getPackage().getName();
            // class org.bukkit.craftbukkit.v1_15_R1.VanillaCommandWrapper
            VANILLA_COMMAND_WRAPPER_CLAZZ = serverClazz.getClassLoader().loadClass(_package + ".command.VanillaCommandWrapper");

            // package net.minecraft.server.v1_15_R1
            _package = _package.replaceFirst("org.bukkit.craftbukkit", "net.minecraft.server");
            // class net.minecraft.server.v1_15_R1.CommandDispatcher
            MC_COMMAND_DISPATCHER = serverClazz.getClassLoader().loadClass(_package + ".CommandDispatcher");
            // class net.minecraft.server.v1_15_R1.MinecraftServer
            MC_SERVER_CLAZZ = serverClazz.getClassLoader().loadClass(_package + ".MinecraftServer");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 创建一个MC专用的命令包装类，包装命令
     * 该类所有描述以及权限被bukkit归类为minecraft所属（可直接更改
     *
     * @return 被包装后的命令集
     */
    private static Set<Command> newInstanceCommands(@Nullable Object mcCommandDispatcher, Collection<LiteralArgumentBuilder<Object>> builders) {
        try {
            Constructor<?> constructor = VANILLA_COMMAND_WRAPPER_CLAZZ.getConstructor(MC_COMMAND_DISPATCHER, CommandNode.class);
            HashSet<Command> build = new HashSet<>(builders.size());
            for (LiteralArgumentBuilder<Object> builder : builders) {
                Object command = constructor.newInstance(mcCommandDispatcher == null ? getMcCommandDispatcher(false) : mcCommandDispatcher, builder.build());
                if (command instanceof Command) {
                    build.add((Command) command);
                }
            }
            return build;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Nullable
    @SuppressWarnings("unchecked")
    public static CommandDispatcher<Object> getCommandDispatcher(@Nullable Object mcCommandDispatcher) {
        try {
            if (mcCommandDispatcher == null) {
                mcCommandDispatcher = getMcCommandDispatcher(false);
            }
            Class<?> mcCommandDispatcherClazz = mcCommandDispatcher.getClass();
            for (Method method : mcCommandDispatcherClazz.getDeclaredMethods()) {
                if (method.getReturnType().isAssignableFrom(CommandDispatcher.class)) {
                    return (CommandDispatcher<Object>) method.invoke(mcCommandDispatcher);
                }
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    @NotNull
    public static Object getMcCommandDispatcher(boolean vanilla) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        // object org.bukkit.craftbukkit.v1_15_R1.CraftServer
        Object mcServer = Bukkit.getServer().getClass().getMethod("getServer").invoke(Bukkit.getServer());
        // object net.minecraft.server.v1_15_R1.CommandDispatcher
        if (vanilla) {
            return MC_SERVER_CLAZZ.getField("vanillaCommandDispatcher").get(mcServer);
        }
        return MC_SERVER_CLAZZ.getField("commandDispatcher").get(mcServer);
    }

    /**
     * 添加指令集
     *
     * @param pluginClazz 插件
     * @param builders    指令集
     */
    public static <T extends JavaPlugin> void addRootCommands(@NotNull Class<T> pluginClazz, @Nullable Object mcCommandDispatcher, @NotNull Collection<LiteralArgumentBuilder<Object>> builders) {
        if (builders.isEmpty()) {
            return;
        }
        try {
            if (mcCommandDispatcher == null) {
                mcCommandDispatcher = getMcCommandDispatcher(false);
            }
            Server server = Bukkit.getServer();
            SimpleCommandMap commandMap = (SimpleCommandMap) server.getClass().getMethod("getCommandMap").invoke(server);
            if (commandMap != null) {
                Set<Command> commands = newInstanceCommands(mcCommandDispatcher, builders);
                if (commands != null && !commands.isEmpty()) {
                    T plugin = JavaPlugin.getPlugin(pluginClazz);
                    String name = plugin.getName().toLowerCase();
                    for (Command command : commands) {
                        command.setDescription("A InShin build command.");
                        if (command.getPermission() != null && command.getPermission().contains("minecraft.command.")) {
                            command.setPermission(null);
                        }
                    }

                    Map<String, Command> knownCommands = getKnownCommands(commandMap);
                    Iterator<Map.Entry<String, Command>> iterator = knownCommands.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, Command> next = iterator.next();
                        for (Command command : commands) {
                            if (next.getKey().equals(command.getLabel())
                                    || next.getKey().equals(name + ":" + command.getLabel())
                                    || next.getKey().equals("minecraft:" + command.getLabel())) {
                                iterator.remove();
                                if (DEBUG_LOG) {
                                    System.out.println(String.format("remove command(%s) %s\n", next.getKey(), next.getValue()));
                                }
                            }
                        }
                    }
                    commandMap.registerAll(name, new ArrayList<>(commands));

                    CommandDispatcher<Object> commandDispatcher = getCommandDispatcher(mcCommandDispatcher);
                    if (commandDispatcher != null) {
                        for (LiteralArgumentBuilder<Object> builder : builders) {
                            Command command = commandMap.getCommand(builder.getLiteral());
                            if (command != null) {
                                commandDispatcher.register(builder);
                                if (DEBUG_LOG) {
                                    System.out.println(String.format("register command(%s) %s\n", command.getLabel(), command));
                                }
                            }
                        }
                        if (syncCommandsRegister && commands.stream().anyMatch(Command::isRegistered)) {
                            syncCommands();
                        }
                    }
                }
            }

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public static <T extends JavaPlugin> void removeRootCommands(Class<T> pluginClazz) {
        try {
            Server server = Bukkit.getServer();
            SimpleCommandMap commandMap = (SimpleCommandMap) server.getClass().getMethod("getCommandMap").invoke(server);
            if (commandMap != null) {
                final T plugin = JavaPlugin.getPlugin(pluginClazz);
                final String name = plugin.getName().toLowerCase();
                Map<String, Command> knownCommands = getKnownCommands(commandMap);
                Collection<String> commands = new ArrayList<>(plugin.getDescription().getCommands().size());
                for (String command : plugin.getDescription().getCommands().keySet()) {
                    Command knownCommand = knownCommands.get(command);
                    if (knownCommand.unregister(commandMap)) {
                        commands.add(command);
                        Command remove = knownCommands.remove(command);
                        if (DEBUG_LOG) {
                            System.out.println(String.format("unregister command(%s) %s\n", command, remove));
                        }
                        remove = knownCommands.remove(name + ":" + command);
                        commands.add(name + ":" + command);
                        if (DEBUG_LOG) {
                            System.out.println(String.format("unregister command(%s:%s) %s\n", name, command, remove));
                        }
                    }
                }
                CommandDispatcher<Object> commandDispatcher = getCommandDispatcher(null);
                if (commandDispatcher != null) {
                    remove0(commandDispatcher.getRoot(), commands);
                }
                if (syncCommandsUnregister && !commands.isEmpty()) {
                    syncCommands();
                }
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    /**
     * {@link CraftServer#getCommandMap()}
     * {@link SimpleCommandMap#knownCommands}
     */
    @SuppressWarnings("unchecked")
    @NotNull
    private static Map<String, Command> getKnownCommands(SimpleCommandMap commandMap) throws NoSuchFieldException, IllegalAccessException {
        Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
        knownCommandsField.setAccessible(true);
        return (Map<String, Command>) knownCommandsField.get(commandMap);
    }

    /**
     * 批量移除一个节点中指定的命令
     *
     * @param node     节点
     * @param commands 需要移除的命令
     * @throws NoSuchFieldException   应该不会抛吧
     * @throws IllegalAccessException 应该不会抛吧
     */
    private static void remove0(CommandNode<Object> node, Collection<String> commands) throws NoSuchFieldException, IllegalAccessException {
        if (commands.isEmpty()) {
            return;
        }
        Field childrenField = CommandNode.class.getDeclaredField("children");
        Field literalsField = CommandNode.class.getDeclaredField("literals");
        Field argumentsField = CommandNode.class.getDeclaredField("arguments");
        childrenField.setAccessible(true);
        literalsField.setAccessible(true);
        argumentsField.setAccessible(true);
        Map<?, ?> children = (Map<?, ?>) childrenField.get(node);
        Map<?, ?> literals = (Map<?, ?>) literalsField.get(node);
        Map<?, ?> arguments = (Map<?, ?>) argumentsField.get(node);

        for (String command : commands) {
            children.remove(command);
            literals.remove(command);
            arguments.remove(command);
        }
    }

    private static void syncCommands() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Bukkit.getServer().getClass().getDeclaredMethod("syncCommands").invoke(Bukkit.getServer());
    }
}
