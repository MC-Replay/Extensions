[![](https://jitpack.io/v/MC-Replay/Extensions.svg)](https://jitpack.io/#MC-Replay/Extensions)

# Extensions
This extension system allows developers to create jar files that can be loaded as extensions into other projects. This is achieved using a custom loader that can dynamically load classes from the extension jar files at runtime.

Once an extension is loaded, it can be used just like any other class in the project. The main project can access the extension's methods and fields, and can even instantiate objects from the extension's classes. This extension system provides a flexible way to add functionality to a project without having to modify the project's code directly. Extensions can be developed and maintained separately, and can be added or removed from the project as needed.

Here is a code example of a discord bot using the extension system:

```java
public abstract class BotExtension extends JavaExtension {

    private Bot discordBot;

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void log(String message) {
        this.discordBot.log("[" + getConfig().getName() + "] " + message);
    }
}
```

```java
public final class BotExtensionHandler extends JavaExtensionLoader {

    public BotExtensionHandler(HarmBot bot, File extensionFolder) {
        super(extensionFolder);

        try {
            this.loadExtensions();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        for (JavaExtension extension : getExtensions()) {
            if (extension instanceof BotExtension botExtension) {
                try {
                    JavaReflections.getField(BotExtension.class, Bot.class, "discordBot").set(botExtension, bot);

                    botExtension.onEnable();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }

        bot.log("Enabled " + getExtensions().size() + " extensions");
    }

    public void disable() {
        for (JavaExtension extension : getExtensions()) {
            if (extension instanceof BotExtension botExtension) {
                botExtension.onDisable();
            }
        }

        try {
            this.unloadExtensions();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
```

```java
@Getter
public class DiscordSyncExtension extends BotExtension {

    @Getter
    private static DiscordSyncExtension instance;

    private UserStorage userStorage;

    @Override
    public void onEnable() {
        instance = this;
        this.userStorage = new UserStorage(this.getHarmBot().getDatabase());

        HarmBotAPI.getCommandHandler().registerSlashCommands(
                new DiscordSyncCommand(),
                new LinkCommand()
        );

        HarmBotAPI.registerEvent(
                new SyncListeners()
        );

        DiscordSyncTasks.start();
    }
}
```

![image](https://user-images.githubusercontent.com/72739475/236324065-9c07f516-111a-49be-88d0-23bd0d76d070.png)

![image](https://user-images.githubusercontent.com/72739475/236324163-42819b2b-0ea3-44fc-9760-b75b7d766ad5.png)

