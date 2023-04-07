package com.github.hms11rn.spu;


import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ServerInfo;

import static com.github.hms11rn.spu.ServerPackUnlocker.LOGGER;

/**
 * Gets the current running server, current resource pack, and adds server to settings
 *
 * @author hms11rn
 */
public class PlayerConnectionEventHandler implements ClientPlayConnectionEvents.Join, ClientPlayConnectionEvents.Disconnect {


    /**
     * String ServerName, Boolean isPackEnabled, Integer locationInPack
     */
    public String currentServer;
    Settings settings;

    /**
     * Passes through settings
     * @param modSettings Mod settings
     */
    public PlayerConnectionEventHandler(Settings modSettings) {
        this.settings = modSettings;
    }

    /**
     * When player joins server it sets currentServer
     * than it checks if Settings already has this server, if not it adds it
     * @param handler event handler, not used
     * @param sender gets who sent the packet, not used
     * @param client Instance of Minecraft Client
     */
    @Override
    public void onPlayReady(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client) {
        if (client.isInSingleplayer()) // If it's single-player, there is no server resource packs, so it returns.
            return;
        ServerInfo serverInfo = client.getCurrentServerEntry(); // Obtains server info of the server that was joined.
        if (serverInfo == null)
            return;
        this.currentServer = serverInfo.address;
        LOGGER.info("SPU: Server {" + serverInfo.address + "} requires Resource Pack: " + serverInfo.getResourcePackPolicy());

        if (serverInfo.getResourcePackPolicy() == ServerInfo.ResourcePackPolicy.PROMPT || serverInfo.getResourcePackPolicy() == ServerInfo.ResourcePackPolicy.ENABLED) {
            if (settings.containsServer(serverInfo.address)) {
                LOGGER.info("ClientPlayConnectionEvents::onPlayReady(---) was called, and ResourcePackPolicy is PROMPT or ENABLED -> Server requires a resource pack");

            } else {
                settings.addServer(serverInfo.address, true, Settings.TOP);
            }
        } else {
            if (settings.containsServer(client.getCurrentServerEntry().address)) { // Do nothing
                LOGGER.info("This server is in Servers with pack list, but does not have any server resource pack attached to it");

            }
        }
    }


    @Override
    public void onPlayDisconnect(ClientPlayNetworkHandler handler, MinecraftClient client) {
        currentServer = null;
        ServerPackUnlocker.modInstance.currentServerResourcePack = null;
        LOGGER.info("Player disconnected from server, setting currentServer and currentServerResourcePack to null");
    }
}
