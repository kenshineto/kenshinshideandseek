package dev.tylerm.khs.configuration;

import dev.tylerm.khs.Main;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

public class ConfigManager {

    private final File file;
    private YamlConfiguration config,defaultConfig;
    private String defaultFilename;

    public static ConfigManager create(String filename) {
        return new ConfigManager(filename, filename);
    }

    public static ConfigManager create(String filename, String defaultFilename) {
        return new ConfigManager(filename, defaultFilename);
    }

    private ConfigManager(String filename, String defaultFilename) {

        File dataFolder = Main.getInstance().getDataFolder();
        File oldDataFolder = new File(Main.getInstance().getDataFolder().getParent() + File.separator + "HideAndSeek");

        this.defaultFilename = defaultFilename;
        this.file = new File(dataFolder, filename);

        if(oldDataFolder.exists()){
            if(!dataFolder.exists()){
                if(!oldDataFolder.renameTo(dataFolder)){
                    throw new RuntimeException("Could not rename folder: " + oldDataFolder.getPath());
                }
            } else {
                throw new RuntimeException("Plugin folders for HideAndSeek & KenshinsHideAndSeek both exists. There can only be one!");
            }

        }

        if (!dataFolder.exists()) {
            if (!dataFolder.mkdirs()) {
                throw new RuntimeException("Failed to make directory: " + file.getPath());
            }
        }

        if (!file.exists()) {
            try{
                InputStream input = Main.getInstance().getResource(defaultFilename);
                if (input == null) {
                    throw new RuntimeException("Could not create input stream for "+defaultFilename);
                }
                java.nio.file.Files.copy(input, file.toPath());
                input.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Could not create input stream for "+file.getPath());
        }
        InputStreamReader reader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
        this.config = new YamlConfiguration();
        try {
            this.config.load(reader);
        } catch(InvalidConfigurationException e) {
            Main.getInstance().getLogger().severe(e.getMessage());
            throw new RuntimeException("Invalid configuration in config file: "+file.getPath());
        } catch(IOException e) {
            throw new RuntimeException("Could not access file: "+file.getPath());
        }

        InputStream input = this.getClass().getClassLoader().getResourceAsStream(defaultFilename);
        if (input == null) {
            throw new RuntimeException("Could not create input stream for "+defaultFilename);
        }
        InputStreamReader default_reader = new InputStreamReader(input, StandardCharsets.UTF_8);
        this.defaultConfig = new YamlConfiguration();
        try {
            this.defaultConfig.load(default_reader);
        } catch(InvalidConfigurationException e) {
            Main.getInstance().getLogger().severe(e.getMessage());
            throw new RuntimeException("Invalid configuration in internal config file: "+defaultFilename);
        } catch(IOException e) {
            throw new RuntimeException("Could not access internal file: "+defaultFilename);
        }

        try{
            fileInputStream.close();
            default_reader.close();
        } catch (IOException e) {
            throw new RuntimeException("Unable to finalize loading of config files.");
        }
    }

    public boolean contains(String path) {
        return config.contains(path);
    }

    @SuppressWarnings("unused")
    public double getDouble(String path) {
        if (!config.contains(path)) {
            return defaultConfig.getDouble(path);
        } else {
            return config.getDouble(path);
        }
    }

    public int getInt(String path) {
        if (!config.contains(path)) {
            return defaultConfig.getInt(path);
        } else {
            return config.getInt(path);
        }
    }

    public int getDefaultInt(String path) {
        return defaultConfig.getInt(path);
    }

    public float getFloat(String path) {
        if (!config.contains(path)) {
            return (float) defaultConfig.getDouble(path);
        } else {
            return (float) config.getDouble(path);
        }
    }

    public String getString(String path) {
        String value = config.getString(path);
        if (value == null) {
            return defaultConfig.getString(path);
        } else {
            return value;
        }
    }

    public String getString(String path, String oldPath) {
        String value = config.getString(path);
        if (value == null) {
            String oldValue = config.getString(oldPath);
            if (oldValue == null) {
                return defaultConfig.getString(path);
            } else {
                return oldValue;
            }
        } else {
            return value;
        }
    }

    public List<String> getStringList(String path) {
        List<String> value = config.getStringList(path);
        if (value == null) {
            return defaultConfig.getStringList(path);
        } else {
            return value;
        }
    }

    public void reset(String path) {
        config.set(path, defaultConfig.get(path));
    }

    public void resetFile(String newDefaultFilename) {
        this.defaultFilename = newDefaultFilename;

        InputStream input = Main.getInstance().getResource(defaultFilename);
        if (input == null) {
            throw new RuntimeException("Could not create input stream for "+defaultFilename);
        }
        InputStreamReader reader = new InputStreamReader(input);
        this.config = YamlConfiguration.loadConfiguration(reader);
        this.defaultConfig = YamlConfiguration.loadConfiguration(reader);

    }

    public boolean getBoolean(String path) {
        if (!config.contains(path)) {
            return defaultConfig.getBoolean(path);
        } else {
            return config.getBoolean(path);
        }
    }

    public ConfigurationSection getConfigurationSection(String path) {
        ConfigurationSection section = config.getConfigurationSection(path);
        if (section == null) {
            return defaultConfig.getConfigurationSection(path);
        } else {
            return section;
        }
    }

    public ConfigurationSection getDefaultConfigurationSection(String path) {
        return defaultConfig.getConfigurationSection(path);
    }

    public void set(String path, Object value) {
        config.set(path, value);
    }

    public void overwriteConfig() {
        try {
            this.config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveConfig() {
        try {
            // open config file
            InputStream is = Main.getInstance().getResource(defaultFilename);
            // if failed error
            if (is == null) {
                throw new RuntimeException("Could not create input stream for "+defaultFilename);
            }
            // manually read in each character to preserve string data
            StringBuilder textBuilder = new StringBuilder(new String("".getBytes(), StandardCharsets.UTF_8));
            Reader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            int c;
            while((c = reader.read()) != -1)
                textBuilder.append((char) c);
            // store yaml file into a string
            String yaml = new String(textBuilder.toString().getBytes(), StandardCharsets.UTF_8);
            // get config values
            Map<String, Object> data = config.getValues(true);
            // write each stored config value into the yaml string
            for(Map.Entry<String, Object> entry: data.entrySet()) {
                // if type isn't supported, skip
                if(!isSupported(entry.getValue())) continue;
                // get index of key in yaml string
                int index = getIndex(yaml, entry.getKey());
                // if index not found, skip
                if (index < 10)  continue;
                // get start and end of the value
                int start = yaml.indexOf(' ', index) + 1;
                int end = yaml.indexOf('\n', index);
                // if end not found, set it to the end of the file
                if (end == -1) end = yaml.length();
                // create new replace sting
                StringBuilder replace = new StringBuilder(new String("".getBytes(), StandardCharsets.UTF_8));
                // get value
                Object value = entry.getValue();
                // if the value is a list,
                if (value instanceof List) {
                    end = yaml.indexOf(']', start) + 1;
                    List<?> list = (List<?>) entry.getValue();
                    if (list.isEmpty()) {
                        // if list is empty, put an empty list
                        replace.append("[]");
                    } else {
                        // if list has values, populate values into the string
                        // get gap before key
                        int gap = whitespaceBefore(yaml, index);
                        String space = new String(new char[gap]).replace('\0', ' ');
                        replace.append("[\n");
                        for (int i = 0; i < list.size(); i++) {
                            replace.append(space).append("  ").append(convert(list.get(i)));
                            if(i != list.size() -1) replace.append(",\n");
                        }
                        replace.append('\n').append(space).append("]");
                    }
                // otherwise just put the value directly
                } else {
                    replace.append(convert(value));
                }
                // replace the new value in the yaml string
                StringBuilder builder = new StringBuilder(yaml);
                builder.replace(start, end, replace.toString());
                yaml = builder.toString();
            }

            // write yaml string to file
            Writer fileWriter = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8));
            fileWriter.write(yaml);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getIndex(String yaml, String key) {
        String[] parts = key.split("\\.");
        int index = 0;
        for(String part : parts) {
            if (index == 0) {
                index = yaml.indexOf("\n" + part + ":", index) + 1;
            } else {
                index = yaml.indexOf(" " + part + ":", index) + 1;
            }
            if (index == 0) break;
        }
        return index;
    }

    public boolean isSupported(Object o) {
        return o instanceof Integer ||
                o instanceof Double ||
                o instanceof String ||
                o instanceof Boolean ||
                o instanceof List;
    }

    public int whitespaceBefore(String yaml, int index) {
        int count = 0;
        for(int i = index - 1; yaml.charAt(i) == ' '; i--) count++;
        return count;
    }

    private String convert(Object o) {
        if(o instanceof String) {
            return "\"" + o + "\"";
        } else if (o instanceof Boolean) {
            return (boolean)o ? "true" : "false";
        }
        return o.toString();
    }

}
