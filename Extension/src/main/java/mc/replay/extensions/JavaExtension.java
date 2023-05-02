package mc.replay.extensions;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public abstract class JavaExtension implements Comparable<JavaExtension> {

    private ExtensionLoaderMethods extensionLoaderMethods;
    private ExtensionConfig config;
    private File mainFolder;
    private boolean isLoaded;

    void setExtensionLoaderMethods(ExtensionLoaderMethods extensionLoaderMethods) {
        this.extensionLoaderMethods = extensionLoaderMethods;
    }

    void setConfig(ExtensionConfig config) {
        this.config = config;
    }

    void setMainFolder(File mainFolder) {
        this.mainFolder = mainFolder;
    }

    void setIsLoaded(boolean isLoaded) {
        this.isLoaded = isLoaded;
    }

    public void onLoad() {

    }

    public void onEnable() {

    }

    public void onDisable() {

    }

    public boolean isLoaded() {
        return this.isLoaded;
    }

    public ExtensionConfig getConfig() {
        return this.config;
    }

    public File getMainFolder() {
        return this.mainFolder;
    }

    public JavaExtension getExtensionByName(String name) {
        return this.extensionLoaderMethods.getExtensionByName(name);
    }

    public File getDirectory() {
        File extensionFolder = new File(this.mainFolder.toPath() + "/" + this.config.getName().replaceAll(" ", "-") + "/");
        if (!extensionFolder.exists()) extensionFolder.mkdirs();
        return extensionFolder;
    }

    @Override
    public int compareTo(@NotNull JavaExtension o) {
        for (String dependency : this.config.getDepends()) {
            if (dependency.equalsIgnoreCase(this.config.getName())) continue;

            if (o.getConfig().getName().equalsIgnoreCase(dependency) || dependency.equalsIgnoreCase("all")) {
                return 1;
            }
        }

        return -1;
    }
}
