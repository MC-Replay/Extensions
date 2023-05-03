package mc.replay.extensions;

import mc.replay.extensions.exception.InvalidConfigurationException;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

final class ExtensionLoaderUtils {

    private ExtensionLoaderUtils() {
    }

    static JarFile createJarFile(File file) throws IOException {
        // Enable multi-release jars for Java 9+
        try {
            final java.lang.reflect.Method runtimeVersionMethod = JarFile.class.getMethod("runtimeVersion");
            final Object runtimeVersion = runtimeVersionMethod.invoke(null);
            @SuppressWarnings("JavaReflectionMemberAccess") final Constructor<JarFile> constructor = JarFile.class.getConstructor(File.class, boolean.class, int.class, runtimeVersion.getClass());
            return constructor.newInstance(file, true, java.util.zip.ZipFile.OPEN_READ, runtimeVersion);
        } catch (Exception ignored) {
            return new JarFile(file);
        }
    }

    static ExtensionConfig getConfig(File file) throws InvalidConfigurationException {
        try (JarFile jarFile = createJarFile(file)) {
            JarEntry entry = jarFile.getJarEntry("extension.yml");
            if (entry == null) {
                throw new InvalidConfigurationException(new FileNotFoundException("Jar does not contain extension.yml"));
            }

            ExtensionConfig config = ExtensionLoaderUtils.getConfig(jarFile, entry);
            if (config == null)
                throw new InvalidConfigurationException("No config file found for extension %s".formatted(file.getName()));
            if (config.getMain() == null)
                throw new InvalidConfigurationException("Extension main cannot be null (%s)".formatted(file.getName()));
            if (config.getName() == null)
                throw new InvalidConfigurationException("Extension name cannot be null (%s)".formatted(file.getName()));
            if (config.getVersion() == null)
                throw new InvalidConfigurationException("Extension version cannot be null (%s)".formatted(file.getName()));

            return config;
        } catch (IOException exception) {
            throw new InvalidConfigurationException(exception);
        }
    }

    private static ExtensionConfig getConfig(JarFile jarFile, JarEntry entry) {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = jarFile.getInputStream(entry)) {
            Map<String, Object> data = yaml.load(inputStream);
            return new ExtensionConfig(data);
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }
}