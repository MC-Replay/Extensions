package mc.replay.extensions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ExtensionConfig {

    private final String main;
    private final String name;
    private final String version;
    private final List<String> depends;

    private final Map<String, Object> data;

    ExtensionConfig(Map<String, Object> data) {
        this.data = data;

        this.main = this.load("main", null);
        this.name = this.load("name", null);
        this.version = this.load("version", null);
        this.depends = this.load("depends", new ArrayList<>());
    }

    public String getMain() {
        return this.main;
    }

    public String getName() {
        return this.name;
    }

    public String getVersion() {
        return this.version;
    }

    public List<String> getDepends() {
        return this.depends;
    }

    public <T> T get(@NotNull String key, @UnknownNullability T defaultValue) {
        return this.load(key, defaultValue);
    }

    @SuppressWarnings("unchecked")
    private <T> T load(String key, T defaultValue) {
        Object data = this.data.get(key);
        if (data == null) return defaultValue;

        try {
            return (T) data;
        } catch (Exception exception) {
            exception.printStackTrace();
            return defaultValue;
        }
    }
}
