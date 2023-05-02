package mc.replay.extensions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExtensionConfig {

    private final String main;
    private final String name;
    private final String version;
    private final List<String> depends;

    protected ExtensionConfig(Map<String, Object> data) {
        this.main = this.load(data, "main", null);
        this.name = this.load(data, "name", null);
        this.version = this.load(data, "version", null);
        this.depends = this.load(data, "depends", new ArrayList<>());
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

    @SuppressWarnings("unchecked")
    <T> T load(Map<String, Object> dataMap, String key, T defaultValue) {
        Object data = dataMap.get(key);
        if (data == null) return defaultValue;

        try {
            return (T) data;
        } catch (Exception exception) {
            exception.printStackTrace();
            return defaultValue;
        }
    }
}
