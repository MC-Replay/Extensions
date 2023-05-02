package mc.replay.extensions;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Map;
import java.util.jar.JarFile;

final class ExtensionLoaderUtils {

    private ExtensionLoaderUtils() {
    }

    public static JarFile createJarFile(File file) throws IOException {
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

    public static ExtensionConfig getConfig(ClassLoader classLoader, String ymlFile) {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = classLoader.getResourceAsStream(ymlFile)) {
            Map<String, Object> data = yaml.load(inputStream);
            return new ExtensionConfig(data);
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }
}