package mc.replay.extensions;

import mc.replay.extensions.exception.InvalidExtensionException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public final class JavaExtensionClassLoader extends URLClassLoader {

    private final JavaExtensionLoader loader;
    private final Map<String, Class<?>> classes = new ConcurrentHashMap<>();

    private final File file;
    private final URL url;
    private final JarFile jarFile;
    private final Manifest manifest;

    private final JavaExtension extension;

    JavaExtensionClassLoader(@NotNull JavaExtensionLoader loader, @NotNull File mainFolder, @NotNull File file, @NotNull ExtensionConfig config, @Nullable ClassLoader parent) throws IOException, InvalidExtensionException {
        super("Extension-" + config.getName(), new URL[]{file.toURI().toURL()}, parent);

        this.loader = loader;
        this.file = file;

        this.jarFile = ExtensionLoaderUtils.createJarFile(file);
        this.manifest = this.jarFile.getManifest();
        this.url = file.toURI().toURL();

        try {
            Class<?> clazz = Class.forName(config.getMain(), true, this);
            Class<? extends JavaExtension> javaExtensionClass = clazz.asSubclass(JavaExtension.class);

            JavaExtension extension;
            try {
                extension = this.getExtension(javaExtensionClass);
            } catch (Exception exception) {
                throw new InvalidExtensionException("Could not create extension instance for '%s'".formatted(this.file.getName()), exception);
            }

            extension.init(loader, config, mainFolder);

            this.extension = extension;
        } catch (Exception exception) {
            throw new InvalidExtensionException("", exception);
        }
    }

    public JavaExtension getExtension() {
        return this.extension;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return this.loadClass0(name, resolve, true);
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        if (name.startsWith("mc.replay.extensions.")) {
            throw new ClassNotFoundException(name);
        }

        Class<?> result = this.classes.get(name);

        if (result == null) {
            String path = name.replace('.', '/').concat(".class");
            JarEntry entry = this.jarFile.getJarEntry(path);

            if (entry != null) {
                byte[] classBytes;

                try (InputStream in = this.jarFile.getInputStream(entry)) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream(Math.max(32, in.available()));
                    byte[] buffer = new byte[8192];

                    while (true) {
                        int r = in.read(buffer);
                        if (r == -1) break;

                        out.write(buffer, 0, r);
                    }

                    classBytes = out.toByteArray();
                } catch (IOException exception) {
                    throw new ClassNotFoundException(name, exception);
                }

                int dot = name.lastIndexOf('.');
                if (dot != -1) {
                    String packageName = name.substring(0, dot);
                    Package packageByPackageName = this.getDefinedPackage(packageName);
                    if (packageByPackageName == null) {
                        try {
                            if (this.manifest != null) {
                                this.definePackage(packageName, this.manifest, this.url);
                            } else {
                                this.definePackage(packageName, null, null, null, null, null, null, null);
                            }
                        } catch (IllegalArgumentException exception) {
                            throw new IllegalStateException("Cannot find package " + packageName);
                        }
                    }
                }

                CodeSigner[] signers = entry.getCodeSigners();
                CodeSource source = new CodeSource(this.url, signers);

                result = this.defineClass(name, classBytes, 0, classBytes.length, source);
            }

            if (result == null) {
                result = super.findClass(name);
            }

            this.classes.put(name, result);
        }

        return result;
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            this.jarFile.close();
        }
    }

    public Class<?> loadClass0(@NotNull String name, boolean resolve, boolean checkGlobal) throws ClassNotFoundException {
        try {
            return super.loadClass(name, resolve);
        } catch (ClassNotFoundException ignored) {
        }

        if (checkGlobal) {
            Class<?> result = this.loader.classFinder.getClassByName(name, resolve, this);
            if (result != null && result.getClassLoader() instanceof JavaExtensionClassLoader) {
                return result;
            }
        }

        throw new ClassNotFoundException(name);
    }

    private JavaExtension getExtension(Class<? extends JavaExtension> extensionClass) throws Exception {
        Constructor<?> constructor = extensionClass.getConstructor();
        return (JavaExtension) constructor.newInstance();
    }

    @Override
    public String toString() {
        return "ExtensionClassLoader{" +
                "extension=" + this.extension +
                ", file=" + this.file +
                ", url=" + this.url +
                '}';
    }
}