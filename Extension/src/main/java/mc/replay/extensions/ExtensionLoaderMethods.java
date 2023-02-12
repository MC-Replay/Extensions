package mc.replay.extensions;

import org.jetbrains.annotations.NotNull;

public interface ExtensionLoaderMethods {

    <T extends JavaExtension> T getExtensionByName(@NotNull String string);
}
