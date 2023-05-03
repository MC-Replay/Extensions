package mc.replay.extensions;

import mc.replay.extensions.exception.InvalidConfigurationException;
import mc.replay.extensions.exception.InvalidExtensionException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class JavaExtensionLoader implements ExtensionLoaderMethods {

    final ClassFinder classFinder;
    final Map<String, JavaExtensionClassLoader> loaders = new HashMap<>();

    private final File folder;

    public JavaExtensionLoader(@NotNull File folder) {
        if (folder == null) {
            throw new IllegalArgumentException("Extension folder is null.");
        }

        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("Extension folder is no folder.");
        }

        this.folder = folder;
        this.classFinder = new ClassFinder(this);
    }

    @Override
    public @NotNull Collection<JavaExtension> getExtensions() {
        return new TreeSet<>(this.loaders.values().stream().map(JavaExtensionClassLoader::getExtension).toList());
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    public JavaExtension getExtensionByName(@NotNull String name) {
        JavaExtensionClassLoader loader = this.loaders.get(name);
        return loader != null ? loader.getExtension() : null;
    }

    public void loadExtensions() throws IOException, InvalidExtensionException {
        File[] listFiles = this.folder.listFiles();
        if (listFiles == null) return;

        File[] files = Arrays.stream(listFiles)
                .filter(x -> x.getName().endsWith(".jar"))
                .toArray(File[]::new);

        for (File file : files) {
            this.loadExtension(file);
        }
    }

    @ApiStatus.Experimental
    public void loadExtension(@NotNull String fileName) throws IOException, InvalidExtensionException {
        if (!fileName.endsWith(".jar")) fileName += ".jar";

        File file = new File(this.folder, fileName);
        this.loadExtension(file);
    }

    @ApiStatus.Experimental
    public void loadExtension(@NotNull File file) throws IOException, InvalidExtensionException {
        if (!file.exists() || !file.getParentFile().equals(this.folder)) {
            throw new InvalidExtensionException("File '%s' doesn't exist or is not in folder of this loader.".formatted(file.getName()));
        }

        JavaExtensionClassLoader loader = this.loadExtensionFromFile(file);
        if (loader == null) {
            throw new InvalidExtensionException("Couldn't create class loader for extension file '%s'".formatted(file.getName()));
        }

        JavaExtension extension = loader.getExtension();

        if (extension != null) {
            this.loaders.put(extension.getName(), loader);
        }
    }

    public void unloadExtensions() throws IOException {
        for (JavaExtension extension : this.getExtensions()) {
            this.unloadExtension(extension);
        }
    }

    public void unloadExtension(@NotNull JavaExtension extension) throws IOException {
        this.unloadExtension(extension.getName());
    }

    public void unloadExtension(@NotNull String extensionName) throws IOException {
        JavaExtensionClassLoader loader = this.loaders.remove(extensionName);
        if (loader == null) {
            throw new IllegalArgumentException("Extension '%s' was not loaded.".formatted(extensionName));
        }

        loader.close();
    }

    private JavaExtensionClassLoader loadExtensionFromFile(File file) throws IOException, InvalidExtensionException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        ExtensionConfig config;
        try {
            config = ExtensionLoaderUtils.getConfig(file);
        } catch (InvalidConfigurationException exception) {
            throw new InvalidExtensionException(exception);
        }

        JavaExtensionClassLoader extensionClassLoader = new JavaExtensionClassLoader(this, file, this.folder, config, classLoader);
        JavaExtension extension = extensionClassLoader.getExtension();
        if (extension == null) return null;

        return extensionClassLoader;
    }
}
