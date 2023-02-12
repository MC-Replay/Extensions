package mc.replay.extensions;

import java.io.File;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.jar.JarFile;

public class JavaExtensionLoader implements ExtensionLoaderMethods {

    private final File folder;
    private final HashMap<String, JavaExtension> extensions;

    public JavaExtensionLoader(File folder) {
        if (folder == null) {
            throw new IllegalArgumentException("Extension folder is null.");
        }

        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("Extension folder is no folder.");
        }

        this.folder = folder;
        this.extensions = this.retrieveFromDirectory();

        for (JavaExtension extension : extensions.values()) {
            extension.onLoad();
        }

        for (JavaExtension extension : extensions.values()) {
            extension.onEnable();
        }
    }

    public HashMap<String, JavaExtension> retrieveFromDirectory() {
        HashMap<String, JavaExtension> extensions = new HashMap<>();

        File[] listFiles = this.folder.listFiles();
        if (listFiles == null) {
            return extensions;
        }

        File[] files = Arrays.stream(listFiles)
                .filter(x -> x.getName().endsWith(".jar"))
                .toArray(File[]::new);

        for (File file : files) {
            JavaExtension extension = this.retrieveFromDirectory(file);

            if (extension != null) {
                extensions.put(extension.getConfig().getName(), extension);
            }
        }

        return extensions;
    }

    private JavaExtension retrieveFromDirectory(File file) {
        URLClassLoader urlClassLoader = ExtensionLoaderUtils.getURLClassLoader(file);
        if (urlClassLoader == null) return null;

        JarFile jarFile = ExtensionLoaderUtils.getJarFile(file);
        if (jarFile == null) return null;

        ClassLoader classLoader = this.getClass().getClassLoader();
        ExtensionConfig config = ExtensionLoaderUtils.getConfig(classLoader, "extension.yml");
        if (config == null) return null;

        //TODO check main, name, version null

        JavaExtension javaExtension = ExtensionLoaderUtils.getExtensionFromJarEntry(classLoader, config);
        if (javaExtension == null) return null;

        javaExtension.setExtensionLoaderMethods(this);
        javaExtension.setConfig(config);
        javaExtension.setMainFolder(this.folder);
        return javaExtension;
    }

    @Override
    public JavaExtension getExtensionByName(String name) {
        return this.extensions.get(name);
    }
}
