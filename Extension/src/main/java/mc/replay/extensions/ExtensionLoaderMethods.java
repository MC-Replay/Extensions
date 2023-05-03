package mc.replay.extensions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface ExtensionLoaderMethods {

    @NotNull Collection<JavaExtension> getExtensions();

    <T extends JavaExtension> @Nullable T getExtensionByName(@NotNull String string);
}
