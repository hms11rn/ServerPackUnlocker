package com.github.hms11rn.spu;


import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ServerInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

import static com.github.hms11rn.spu.ServerPackUnlocker.LOGGER;

/**
 * Gets the current running server, current resource pack, and adds server to settings
 *
 * @author hms11rn
 */
public class PlayerConnectionEventHandler implements ClientPlayConnectionEvents.Join, ClientPlayConnectionEvents.Disconnect {

    public String currentServer;
    Settings settings;

    /**
     * Used to pass through {@link Settings}
     */
    public PlayerConnectionEventHandler(Settings modSettings) {
        this.settings = modSettings;
    }

    /**
     * Gets called when player joins a server,
     * this method sets {@link PlayerConnectionEventHandler#currentServer}, and if this server is not already in resource pack list, it adds it
     */
    @Override
    public void onPlayReady(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client) {
        if (client.isInSingleplayer()) // If it's single-player, there is no server resource packs, so it returns.
            return;
        ServerInfo serverInfo = client.getCurrentServerEntry(); // Obtains server info of the server that was joined.
        if (serverInfo == null) // avoiding null pointers
            return;
        this.currentServer = serverInfo.address;

        if (serverInfo.getResourcePackPolicy() == ServerInfo.ResourcePackPolicy.PROMPT || serverInfo.getResourcePackPolicy() == ServerInfo.ResourcePackPolicy.ENABLED) {
            if (settings.containsServer(serverInfo.address)) {
                LOGGER.info("Servers with Resource Pack list contains " + serverInfo.address);
            } else {
                settings.addServer(serverInfo.address, Settings.DEFAULT_ENABLED, Settings.TOP);
            }
        } else { // Server Resource pack is disabled by user. Do nothing
            if (settings.containsServer(client.getCurrentServerEntry().address)) {
                LOGGER.info(serverInfo.address + ": is in Servers with pack list, but ResourcePackPolicy is set to DISABLED");
            }
        }
    }


    /**
     * currentServer and currentServerResourcePack are used by {@link com.github.hms11rn.spu.mixin.ResourcePackManagerMixin#buildEnabledProfilesInjections(Collection, CallbackInfoReturnable)}
     * to check if the player is currently in a server, so its important that when the player disconnects, currentServer and currentServerResourcePack are set to null.
     * <br>
     * another thing this method is used for is to checks if {@link net.minecraft.client.option.GameOptions#resourcePacks} still contains the server resource pack if disconnecting,
     * because if it does contain it, after existing {@link net.minecraft.client.gui.screen.pack.PackScreen} it's going to do an unnecessary resource reload
     */
    @Override
    public void onPlayDisconnect(ClientPlayNetworkHandler handler, MinecraftClient client) {
        if (ServerPackUnlocker.modInstance.currentServerResourcePack == null) return;
        if (client.options.resourcePacks.contains(ServerPackUnlocker.modInstance.currentServerResourcePack.getName()))
            client.options.resourcePacks.remove(ServerPackUnlocker.modInstance.currentServerResourcePack.getName());
        currentServer = null;
        ServerPackUnlocker.modInstance.currentServerResourcePack = null;
        LOGGER.info("Player disconnected from server, setting currentServer and currentServerResourcePack to null");
    }
}
