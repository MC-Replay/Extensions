package mc.replay.extensions;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;

public abstract class JavaExtension implements Comparable<JavaExtension> {

    protected ExtensionLoaderMethods extensionLoaderMethods;
    protected ExtensionConfig config;
    protected File mainFolder;

    void setExtensionLoaderMethods(ExtensionLoaderMethods extensionLoaderMethods) {
        this.extensionLoaderMethods = extensionLoaderMethods;
    }

    void setConfig(ExtensionConfig config) {
        this.config = config;
    }

    void setMainFolder(File mainFolder) {
        this.mainFolder = mainFolder;
    }

    public ExtensionConfig getConfig() {
        return this.config;
    }

    public String getName() {
        return this.config.getName();
    }

    public String getVersion() {
        return this.config.getVersion();
    }

    public File getMainFolder() {
        return this.mainFolder;
    }

    public Collection<JavaExtension> getExtensions() {
        return this.extensionLoaderMethods.getExtensions();
    }

    public JavaExtension getExtensionByName(String name) {
        return this.extensionLoaderMethods.getExtensionByName(name);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public File getDirectory() {
        File folder = new File(this.mainFolder, this.config.getName().replaceAll(" ", "-"));

        if (!folder.isDirectory() || !folder.exists()) {
            folder.mkdirs();
        }

        return folder;
    }

    @Override
    public int compareTo(@NotNull JavaExtension o) {
        for (String dependency : this.config.getDepends()) {
            if (dependency.equalsIgnoreCase(this.config.getName())) return 0;

            if (o.getConfig().getName().equalsIgnoreCase(dependency) || dependency.equalsIgnoreCase("all")) {
                return 1;
            }
        }

        return -1;
    }
}
