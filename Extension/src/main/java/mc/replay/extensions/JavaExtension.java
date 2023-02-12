package mc.replay.extensions;

import java.io.File;

public abstract class JavaExtension {

    private ExtensionLoaderMethods extensionLoaderMethods;
    private ExtensionConfig config;
    private File mainFolder;

    void setExtensionLoaderMethods(ExtensionLoaderMethods extensionLoaderMethods) {
        this.extensionLoaderMethods = extensionLoaderMethods;
    }

    void setConfig(ExtensionConfig config){
        this.config = config;
    }

    void setMainFolder(File mainFolder) {
        this.mainFolder = mainFolder;
    }

    void onLoad() {

    }

    void onEnable() {

    }

    void onDisable() {

    }

    public ExtensionConfig getConfig() {
        return this.config;
    }

    public File getMainFolder() {
        return this.mainFolder;
    }

    public JavaExtension getExtensionByName(String name){
        return this.extensionLoaderMethods.getExtensionByName(name);
    }

    public File getDirectory() {
        File extensionFolder = new File(this.mainFolder.toPath() + "/" + this.config.getName().replaceAll(" ", "-") + "/");
        if (!extensionFolder.exists()) extensionFolder.mkdirs();
        return extensionFolder;
    }
}
