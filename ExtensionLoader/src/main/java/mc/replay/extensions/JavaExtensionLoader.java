package mc.replay.extensions;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class JavaExtensionLoader implements ExtensionLoaderMethods {

    private final File folder;
    private final HashMap<String, JavaExtension> extensions;

    private final Map<String, ReentrantReadWriteLock> classLoadLock = new HashMap<>();
    private final Map<String, Integer> classLoadLockCount = new HashMap<>();
    private final List<ExtensionClassLoader> loaders = new CopyOnWriteArrayList<>();

    public JavaExtensionLoader(File folder) {
        if (folder == null) {
            throw new IllegalArgumentException("Extension folder is null.");
        }

        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("Extension folder is no folder.");
        }

        this.folder = folder;
        this.extensions = this.loadAllExtensions();

        for (JavaExtension extension : extensions.values()) {
            extension.onLoad();
        }

        for (JavaExtension extension : extensions.values()) {
            extension.onEnable();
        }
    }

    public HashMap<String, JavaExtension> loadAllExtensions() {
        HashMap<String, JavaExtension> extensions = new HashMap<>();

        File[] listFiles = this.folder.listFiles();
        if (listFiles == null) {
            return extensions;
        }

        File[] files = Arrays.stream(listFiles)
                .filter(x -> x.getName().endsWith(".jar"))
                .toArray(File[]::new);

        for (File file : files) {
            try {
                JavaExtension extension = this.loadExtensionFromFile(file);

                if (extension != null) {
                    extensions.put(extension.getConfig().getName(), extension);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        return extensions;
    }

    @SuppressWarnings("unchecked")
    @Override
    public JavaExtension getExtensionByName(@NotNull String name) {
        return this.extensions.get(name);
    }

    public JavaExtension loadExtensionFromFile(File file) throws IOException, InvalidExtensionException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        ExtensionConfig config = ExtensionLoaderUtils.getConfig(classLoader, "extension.yml");
        if (config == null) return null;

        //TODO check main, name, version null

        try (ExtensionClassLoader extensionClassLoader = new ExtensionClassLoader(this, config, file, classLoader)) {
            JavaExtension extension = extensionClassLoader.getExtension();

            extension.setExtensionLoaderMethods(this);
            extension.setConfig(config);
            extension.setMainFolder(this.folder);
            return extension;
        }
    }

    public void unloadExtension(@NotNull JavaExtension extension) {
        try {
            for (ExtensionClassLoader loader : this.loaders) {
                if (loader.getExtension().equals(extension)) {
                    this.loaders.remove(loader);
                    loader.close();
                    break;
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    Class<?> getClassByName(String name, boolean resolve, ExtensionClassLoader requester) {
        ReentrantReadWriteLock lock;
        synchronized (this.classLoadLock) {
            lock = this.classLoadLock.computeIfAbsent(name, (x) -> new ReentrantReadWriteLock());
            this.classLoadLockCount.compute(name, (x, prev) -> (prev == null) ? 1 : prev + 1);
        }

        lock.writeLock().lock();

        try {
            if (requester != null) {
                try {
                    return requester.loadClass0(name, false, false);
                } catch (ClassNotFoundException ignored) {
                }
            }

            for (ExtensionClassLoader loader : this.loaders) {
                try {
                    return loader.loadClass0(name, resolve, false);
                } catch (ClassNotFoundException ignored) {
                }
            }
        } finally {
            synchronized (this.classLoadLock) {
                lock.writeLock().unlock();

                if (this.classLoadLockCount.get(name) == 1) {
                    this.classLoadLock.remove(name);
                    this.classLoadLockCount.remove(name);
                } else {
                    this.classLoadLockCount.computeIfPresent(name, (x, prev) -> prev - 1);
                }
            }
        }

        return null;
    }
}
