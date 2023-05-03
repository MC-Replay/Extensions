package mc.replay.extensions;

import mc.replay.extensions.exception.ExtensionNotLoadedException;
import mc.replay.extensions.exception.InvalidExtensionException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class JavaExtensionLoader implements ExtensionLoaderMethods {

    private final Map<String, ExtensionClassLoader> extensions = new HashMap<>();

    private final File folder;

    public JavaExtensionLoader(File folder) {
        if (folder == null) {
            throw new IllegalArgumentException("Extension folder is null.");
        }

        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("Extension folder is no folder.");
        }

        this.folder = folder;
        this.loadAllExtensions();
    }

    @Override
    public Collection<JavaExtension> getExtensions() {
        return this.extensions.values().stream().map(ExtensionClassLoader::getExtension).toList();
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    public JavaExtension getExtensionByName(@NotNull String name) {
        ExtensionClassLoader loader = this.extensions.get(name);
        return loader != null ? loader.getExtension() : null;
    }

    public void loadExtensions() {
        for (ExtensionClassLoader loader : this.extensions.values()) {
            try {
                loader.getExtension().onLoad();
                loader.getExtension().setIsLoaded();
            } catch (Exception exception) {
                System.err.println("Error while loading extension " + loader.getExtension().getConfig().getName() + ":");
                exception.printStackTrace();
            }
        }
    }

    public void enableExtensions() {
        for (ExtensionClassLoader loader : this.extensions.values()) {
            if (!loader.getExtension().isLoaded()) {
                throw new ExtensionNotLoadedException("Extension " + loader.getExtension().getConfig().getName() + " is not loaded.");
            }

            try {
                loader.getExtension().onEnable();
            } catch (Exception exception) {
                System.err.println("Error while enabling extension " + loader.getExtension().getConfig().getName() + ":");
                exception.printStackTrace();
            }
        }
    }

    public void disableExtensions() {
        for (ExtensionClassLoader loader : this.extensions.values()) {
            try {
                loader.getExtension().onDisable();
            } catch (Exception exception) {
                System.err.println("Error while disabling extension " + loader.getExtension().getConfig().getName() + ":");
                exception.printStackTrace();
            }
        }
    }

    void loadAllExtensions() {
        File[] listFiles = this.folder.listFiles();
        if (listFiles == null) return;

        File[] files = Arrays.stream(listFiles)
                .filter(x -> x.getName().endsWith(".jar"))
                .toArray(File[]::new);

        for (File file : files) {
            try {
                ExtensionClassLoader loader = this.loadExtensionFromFile(file);
                JavaExtension extension = loader.getExtension();

                if (extension != null) {
                    this.extensions.put(extension.getName(), loader);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    ExtensionClassLoader loadExtensionFromFile(File file) throws IOException, InvalidExtensionException {
        ClassLoader classLoader = this.getClass().getClassLoader();

        try (ExtensionClassLoader extensionClassLoader = new ExtensionClassLoader(this, file, classLoader)) {
            JavaExtension extension = extensionClassLoader.getExtension();
            if (extension == null) return null;

            extension.setExtensionLoaderMethods(this);
            extension.setMainFolder(this.folder);
            return extensionClassLoader;
        }
    }

    final Map<String, ReentrantReadWriteLock> classLoadLock = new HashMap<>();
    final Map<String, Integer> classLoadLockCount = new HashMap<>();

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

            for (ExtensionClassLoader loader : this.extensions.values()) {
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
