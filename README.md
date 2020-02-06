# BukkitBrigadier
基于 ```mojang``` 的 ```brigadier``` 为 ```bukkit``` 制作的库  
可通过用 ```brigadier``` 构建命令  

 ```1.13``` 和 ```1.15.2``` 正常运行，中间的版本未测试  
```Minecraft 1.13``` 才开始由 ```brigadier``` 处理命令


##构建环境
```
java 1.8
craftbukkit 1.15.2 // 用到了反射，但可能兼容低版本
maven
```

# 安装
## Maven
## Gradle
没想到吧，都没写

# 使用
1. 在 ```plugin.yml``` 添加前置插件 ```BukkitBrigadier```  
如果没有前置插件那么可能是这样的  
   ```yaml
   depend:
     - 'BukkitBrigadier'
     - '其它插件'
   ```
   或者这样的
   ```yaml
   depend: ['BukkitBrigadier','其它插件']
   ```

2. 注册命令与注销命令（注销命令无效）  
注册命令：  
```BukkitBrigadier.registers(Collection<LiteralArgumentBuilder<Object>> builders)```  
注销命令：传入你的插件主类即可。e.g: ```JavaPlugin.class```  
```BukkitBrigadier.unregisters(Class pluginClazz)```  

3. 自动完成注册  
如果你的插件主类没有需要继承除了```JavaPlugin```以外的要求，那么不妨继承此类  
```me.inshin.bukkitbrigadier.BukkitBrigadierJavaPlugin```  
该类自动注册与注销命令，只需要实现 ```onCreateCommands``` 方法  
   ```
   protected abstract Collection<LiteralArgumentBuilder<Object>> onCreateCommands();
   public void onEnable();
   public void onDisable();
   ```
   继承时注意 ```onEnable()``` 和 ```onDisable()```  ，如果要重写时需要执行 ```super.onEnable()``` 和 ```super.onDisable()```  
4. 实现命令时使用 ```BukkitBrigadier.senderExecutes```  
如果你够飘的话可以不使用

# 示例
继承 ```BukkitBrigadierJavaPlugin``` 并实现 ```onCreateCommands()``` 方法    
```java
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
```  
## 图片
```
/foo
[BukkitBrigadier] 嘤？

    |bar|
/foo b
[BukkitBrigadier] 嘤嘤？

        |嘤嘤怪|
/foo bar 
        |嘤嘤嘤|
/foo bar 嘤
[BukkitBrigadier] 嘤

``` 
样子大概就是这样吧
 
## 已知 BUG
- 被认为 ```A Mojang provided command.``` （可自己修改）  

## 计划
- 添加常用的 ```ArgumentType``` ，比如 ```Targets```  等  
