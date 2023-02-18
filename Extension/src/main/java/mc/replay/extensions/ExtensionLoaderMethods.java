package mc.replay.extensions;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface ExtensionLoaderMethods {

    Collection<JavaExtension> getExtensions();

    <T extends JavaExtension> T getExtensionByName(@NotNull String string);
}
