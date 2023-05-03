package mc.replay.extensions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface ExtensionLoaderMethods {

    Collection<JavaExtension> getExtensions();

    @Nullable <T extends JavaExtension> T getExtensionByName(@NotNull String string);
}
