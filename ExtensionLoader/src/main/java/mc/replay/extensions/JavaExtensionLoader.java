package mc.replay.extensions;

import mc.replay.extensions.exception.ExtensionNotLoadedException;
import mc.replay.extensions.exception.InvalidExtensionException;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;
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
    }

    public void loadExtensions() {
        for (ExtensionClassLoader loader : this.loaders) {
            try {
                loader.getExtension().onLoad();
                loader.getExtension().setIsLoaded(true);
            } catch (Exception exception) {
                System.err.println("Error while loading extension " + loader.getExtension().getConfig().getName() + ":");
                exception.printStackTrace();
            }
        }
    }

    public void enableExtensions() {
        for (ExtensionClassLoader loader : this.loaders) {
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
        for (ExtensionClassLoader loader : this.loaders) {
            try {
                loader.getExtension().onDisable();
            } catch (Exception exception) {
                System.err.println("Error while disabling extension " + loader.getExtension().getConfig().getName() + ":");
                exception.printStackTrace();
            }
        }
    }

    @Override
    public Collection<JavaExtension> getExtensions() {
        return this.extensions.values();
    }

    @SuppressWarnings("unchecked")
    @Override
    public JavaExtension getExtensionByName(@NotNull String name) {
        return this.extensions.get(name);
    }

    private HashMap<String, JavaExtension> loadAllExtensions() {
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

    public JavaExtension loadExtensionFromFile(File file) throws IOException, InvalidExtensionException {
        ClassLoader classLoader = this.getClass().getClassLoader();

        try (ExtensionClassLoader extensionClassLoader = new ExtensionClassLoader(this, file, classLoader)) {
            JavaExtension extension = extensionClassLoader.getExtension();
            if (extension == null) return null;

            extension.setExtensionLoaderMethods(this);
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

    private void checkNotNull(Object reference, Object errorMessage) {
        if (reference == null) {
            if (errorMessage != null) {
                throw new NullPointerException(String.valueOf(errorMessage));
            } else {
                throw new NullPointerException();
            }
        }
    }
}
