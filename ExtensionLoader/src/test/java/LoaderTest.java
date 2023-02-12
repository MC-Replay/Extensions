import mc.replay.extensions.JavaExtensionLoader;

import java.io.File;

public class LoaderTest {

    public static void main(String[] args) {
        JavaExtensionLoader loader = new JavaExtensionLoader(new File("C:/Users/Tom/Documents/Coding/Minecraft/MC-Replay-Extensions/ExtensionLoader/src/test/resources/extensions"));

        loader.loadExtensions();
        loader.enableExtensions();
    }
}
