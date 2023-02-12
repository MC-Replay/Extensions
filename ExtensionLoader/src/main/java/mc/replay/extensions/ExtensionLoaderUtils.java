package mc.replay.extensions;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.jar.JarFile;

public class ExtensionLoaderUtils {

    public static URLClassLoader getURLClassLoader(File file) {
        try {
            return new URLClassLoader(new URL[]{file.toURI().toURL()}, ExtensionLoaderUtils.class.getClassLoader());
        } catch (MalformedURLException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public static JarFile getJarFile(File file) {
        try {
            return new JarFile(file);
        } catch (IOException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public static ExtensionConfig getConfig(ClassLoader classLoader, String ymlFile) {
        URL resource = classLoader.getResource(ymlFile);
        if (resource == null) return null;

        Yaml yaml = new Yaml();
        try (InputStream inputStream = new FileInputStream(resource.getFile())) {
            Map<String, Object> data = yaml.load(inputStream);
            return new ExtensionConfig(data);
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public static JavaExtension getExtensionFromJarEntry(ClassLoader classLoader, ExtensionConfig config) {
        try {
            Class<?> clazz = Class.forName(config.getMain(), false, classLoader);
            Class<? extends JavaExtension> javaExtension = clazz.asSubclass(JavaExtension.class);

            return javaExtension.getConstructor().newInstance();
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }
}
