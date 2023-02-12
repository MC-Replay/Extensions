import mc.replay.extensions.JavaExtensionLoader;

import java.io.File;

public class LoaderTest {

    public static void main(String[] args) {
        JavaExtensionLoader loader = new JavaExtensionLoader(new File("G:/Prive/Coding/Minecraft/Extensions/ExtensionLoader/src/test/resources/extensions/"));
        loader.retrieveFromDirectory();
    }
}
