package mc.replay.extensions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;

public abstract class JavaExtension implements Comparable<JavaExtension> {

    private ExtensionLoaderMethods extensionLoaderMethods;
    private ExtensionConfig config;
    private File mainFolder;

    void init(ExtensionLoaderMethods extensionLoaderMethods, ExtensionConfig config, File mainFolder) {
        this.extensionLoaderMethods = extensionLoaderMethods;
        this.config = config;
        this.mainFolder = mainFolder;
    }

    public @NotNull ExtensionConfig getConfig() {
        return this.config;
    }

    public @NotNull String getName() {
        return this.config.getName();
    }

    public @NotNull String getVersion() {
        return this.config.getVersion();
    }

    public @NotNull File getMainFolder() {
        return this.mainFolder;
    }

    public @NotNull Collection<JavaExtension> getExtensions() {
        return this.extensionLoaderMethods.getExtensions();
    }

    public @Nullable JavaExtension getExtensionByName(@NotNull String name) {
        return this.extensionLoaderMethods.getExtensionByName(name);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public @NotNull File getDirectory() {
        File folder = new File(this.mainFolder, this.config.getName().replaceAll(" ", "-"));

        if (!folder.isDirectory() || !folder.exists()) {
            folder.mkdirs();
        }

        return folder;
    }

    @Override
    public final int compareTo(@NotNull JavaExtension o) {
        for (String dependency : this.config.getDepends()) {
            if (dependency.equalsIgnoreCase(this.config.getName())) return 0;

            if (o.getConfig().getName().equalsIgnoreCase(dependency) || dependency.equalsIgnoreCase("all")) {
                return 1;
            }
        }

        return -1;
    }
}
