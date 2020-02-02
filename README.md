# BukkitBrigadier
基于 ```mojang``` 的 ```brigadier``` 为 ```bukkit``` 制作的库  
可通过用 ```brigadier``` 构建命令  
使用这个就相当于抛弃 ```bukkit``` 的命令规范了

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
```me.inshin.bukkitbrigadier.BukkitBrigadier.register(com.mojang.brigadier.builder.LiteralArgumentBuilder builder)```  
```me.inshin.bukkitbrigadier.BukkitBrigadier.unregister(String command)``` // command 为你的根命令  如果你的命令为 ```/foo bar```  那么传入 ```foo```  

3. 根据你的要求继承不同的预初始化类（未完全完成）  
```me.inshin.bukkitbrigadier.BukkitBrigadierJavaPlugin```  // 使用单个命令  
```me.inshin.bukkitbrigadier.MultipleBukkitBrigadierJavaPlugin```  // 使用多个命令  
   ```
   protected abstract LiteralArgumentBuilder<Object> onCreateCommand();
   public void onEnable();
   public void onDisable();
   ```
   继承时注意 ```onEnable()``` 和 ```onDisable()```  ，如果要重写时需要先执行 ```super.onEnable()``` 和 ```super.onDisable()```  
4. 实现命令时使用 ```BukkitBrigadier.senderExecutes```  
如果你够飘的话可以不使用

# 示例
继承 ```BukkitBrigadierJavaPlugin```  
实现 ```onCreateCommand()``` 方法  
```
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
                                                if (sender instanceof Player) {
                                                    sender.sendMessage(String.format("[%s] %s", getName(), StringArgumentType.getString(context, "嘤嘤？")));
                                                }
                                                return 1;
                                            }))
                                            .suggests((context, builder) -> {
                                                builder.suggest("嘤嘤嘤");
                                                return builder.buildFuture();
                                            })
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

        |嘤嘤嘤|
/foo bar 嘤
[BukkitBrigadier] 嘤

``` 
样子大概就是这样吧
 
## 已知 BUG
- 无法动态注销命令，即使是 ```/reload```
- ？被 ```/help``` 识别为 ```A Mojang provided command.```  

## 计划
- 尽量修复无法动态注销命令的 BUG
- 添加常用的 ```ArgumentType``` ，比如 ```Targets```  等  
注：服务器兼容版本未测试
