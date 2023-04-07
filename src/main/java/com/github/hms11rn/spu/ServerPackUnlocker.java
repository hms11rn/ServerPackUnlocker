package com.github.hms11rn.spu;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.resource.ResourcePackProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unlocks server resource packs, saves the resource packs index to file,
 * and everytime the player joins a server it puts the resource pack in the index from the previous time the player joined that server
 * @author hms11rn
 */
public class ServerPackUnlocker implements ModInitializer {


	public static ServerPackUnlocker modInstance;

	/**
	 * Instance of PlayerConnectionEventHandler, used to get current server
	 */
	public PlayerConnectionEventHandler playerJoinServerHandler;

	/**
	 current server resource pack used by ResourcePackManagerMixin
	 */
	public ResourcePackProfile currentServerResourcePack;

	/**
	 * Used in PackScreenMixin and ResourcePackManagerMixin
	 */
	public int justClosedPackScreen = 0;


	/**
	 * Mod settings
	 */
	public Settings settings;


	public static Logger LOGGER = LoggerFactory.getLogger("spu");

	/**
	 * Initializes settings, sets modInstance, and initializes playerJoinServerHandler and adds it to fabric events
	 */
	@Override
	public void onInitialize() {
		this.settings = new Settings();
		modInstance = this;
		playerJoinServerHandler = new PlayerConnectionEventHandler(settings);
		ClientPlayConnectionEvents.JOIN.register(playerJoinServerHandler);
		ClientPlayConnectionEvents.DISCONNECT.register(playerJoinServerHandler);

	}

	/**
	 *  util method to check if the resource pack should be enabled for the current server
	 * @return if server resource pack should be enabled
	 */
	public boolean shouldEnableServerPack() {
		return  settings.isEnabled(playerJoinServerHandler.currentServer);

	}

	/**
	 * Util method to get current server
	 * @return current server
	 */
	public String getCurrentServer() {
		return playerJoinServerHandler.currentServer;
	}

}


// String pattern for server with pack is "IP:Enabled:AbovePack"