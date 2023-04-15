package com.github.hms11rn.spu;

import com.google.gson.Gson;
import net.minecraft.client.MinecraftClient;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.hms11rn.spu.ServerPackUnlocker.LOGGER;

/**
 * Settings class to load and write to file Server Resource Pack Locations
 * @author hms11rn
 */
public class Settings {

    private Map<String, Map.Entry<Boolean, Integer>> serversWithPack;

    /**
     * Top location of resource pack
     */
    public static int TOP = -1;

    public static boolean DEFAULT_ENABLED = true;

    File configFile;

    public Settings() {
        serversWithPack = new HashMap<>(); // for testing, to avoid nulls
        loadSettings();
    }


    /**
     * Check if Settings contains this server already
     * @param server server to check
     * @return if serversWithPack contains this server
     */
    public boolean containsServer(String server) {
        return serversWithPack.containsKey(server);
    }

    /**
     * Changes index of server, the index is from the bottom of the Resource Pack list
     * <br>
     * An index of -1 is the top of the list
     * @param server Server to change the Resource Pack index
     * @param index index of server from the bottom
     */
    public void setIndex(String server, int index) {
        if ( serversWithPack.get(server) == null) { // Null safe
            serversWithPack.put(server, packOptionsEntry(true, Settings.TOP));
            return;
        }
        serversWithPack.put(server, packOptionsEntry(isEnabled(server), index));
    }

    /**
     * @return index of resource pack of that server
     */
    public int getIndex(String server) {
        return serversWithPack.get(server).getValue();
    }
    /**
     * Sets if a server pack is enabled or not
     * @param server server to change
     * @param bool value
     */
    public void setEnabled(String server, boolean bool) {
        if ( serversWithPack.get(server) == null) {
            serversWithPack.put(server, packOptionsEntry(bool, Settings.TOP));
            return;
        }
        serversWithPack.put(server, packOptionsEntry(bool, serversWithPack.get(server).getValue()));
    }

    /**
     * Adds server to serversWithPack
     * @param server server to add
     * @param enabled default enabled
     * @param index default index
     */
    public void addServer(String server, boolean enabled, int index) {
        if (serversWithPack.containsKey(server))
            return;
        serversWithPack.put(server, packOptionsEntry(enabled, index));

    }

    /**
     *  Checks if server resource pack is enabled in serversWithPack
     * @param server server to check
     * @return if the resource pack for the server is enabled
     */
    public boolean isEnabled(String server) {
        if (!containsServer(server))
            return false;
        return serversWithPack.get(server).getKey();
    }

    /**
     * Check if config file exists and if not creates one
     */
    private void createFile() {
        String configPath = MinecraftClient.getInstance().runDirectory.getAbsolutePath() + "\\config\\ServerPackUnlocker.json";
        File configFile = new File(configPath);
        try {
            if (!configFile.exists()) {
                LOGGER.info("Attempting to create config file: " + configPath);
                if (configFile.createNewFile())
                    LOGGER.info("Created Config file: " + configFile.getName());
            }
        } catch (IOException e) {
            LOGGER.error("Error creating config file: ", e.getStackTrace());
        }
        this.configFile = configFile;
    }
    /**
     * Loads settings from file
     */
    public void loadSettings() {
        ServerPackSetting[] serverPackSettings;
        createFile();
        Gson gson = new Gson();
        try {
            String sti = FileUtils.readFileToString(configFile, StandardCharsets.UTF_8);
            serverPackSettings = gson.fromJson(sti, ServerPackSetting[].class);
            serversWithPack = fromArray(serverPackSettings);
        } catch (IOException e) {
            LOGGER.error("Error writing to file: ", e.getStackTrace());
        }
    }

    /**
     * Writes settings into file using {@link Gson}
     */
    public void writeSettings() {
        createFile();
        Gson gson = new Gson();
        String sti = gson.toJson(fromMap(serversWithPack));
        try {
            FileWriter fw = new FileWriter(configFile);
            fw.write(sti);
            fw.close();

        } catch (IOException e) {
            LOGGER.error("Error writing to file: ", e.getStackTrace());
        }

    }


    /**
     * Creates an Entry for serverWithPack
     */
    private static Map.Entry<Boolean, Integer> packOptionsEntry(boolean enabled, int location) {

        return new Map.Entry<Boolean, Integer>() {

            @Override
            public Boolean getKey() {
                return enabled;
            }

            @Override
            public Integer getValue() {
                return location;
            }

            @Override
            public Integer setValue(Integer value) {

                return null; // not used so sets it to null
            }
        };
    }

    /**
     * Settings for a Server Resource Pack,
     * the reason why it's a separate class, is because {@link Gson} can't deserialize Lists, but can deserialize classes
     */
    private static class ServerPackSetting {
        String name;
        boolean enabled;
        int index;

        ServerPackSetting(String name, boolean enabled, int index) {
            this.name = name;
            this.enabled = enabled;
            this.index = index;
        }

        public String getName() {
            return name;
        }


        public boolean isEnabled() {
            return enabled;
        }


        public int getIndex() {
            return index;
        }


    }

    /**
     * Converts a Map of Server Resource Packs into an array of ServerPackSetting so {@link Gson} can deserialize it and write it to a file
     */
    static ServerPackSetting[] fromMap(Map<String, Map.Entry<Boolean, Integer>> map) {
        List<ServerPackSetting> list = new ArrayList<>();
        for (Map.Entry<String, Map.Entry<Boolean, Integer>> e : map.entrySet()) {
            String name = e.getKey();
            boolean enabled = e.getValue().getKey();
            int index = e.getValue().getValue();
            list.add(new ServerPackSetting(name, enabled, index));
        }
        return list.toArray(new ServerPackSetting[list.size()]);
    }

    /**
     * converts an array of ServerPackSetting back into a map of Server Resource Packs
     */
    static Map<String, Map.Entry<Boolean, Integer>> fromArray(ServerPackSetting[] serverPackSettings) {
        Map<String, Map.Entry<Boolean, Integer>> packsReturn = new HashMap<>();
        if (serverPackSettings == null)
            return new HashMap<>();
        for (ServerPackSetting serverPackSetting : serverPackSettings) {
            packsReturn.put(serverPackSetting.getName(), packOptionsEntry(serverPackSetting.isEnabled(), serverPackSetting.getIndex()));
        }
        return packsReturn;
    }
}
