import mc.replay.extensions.JavaExtensionLoader;

import java.io.File;

public class LoaderTest {

    public static void main(String[] args) {
        JavaExtensionLoader loader = new JavaExtensionLoader(new File("C:/Users/win 10/Desktop/Development/MC-Replay/Extensions/extensions/"));
        loader.loadAllExtensions();
    }
}
