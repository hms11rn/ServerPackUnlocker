package com.github.hms11rn.spu.mixin;

import com.github.hms11rn.spu.ServerPackUnlocker;
import net.minecraft.client.resource.ClientBuiltinResourcePackProvider;
import net.minecraft.resource.ResourcePackProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import static com.github.hms11rn.spu.ServerPackUnlocker.LOGGER;

/**
 * Changes a lot of parameters in ServerResourcePackProvider so the server resource pack is not locked in place.
 *
 * @author hms11rn
 */
@Mixin(ClientBuiltinResourcePackProvider.class)
public class ServerResourcePackProviderMixin {
	/**
	 * Shadows serverContainer from ServerResourcePackProvider
	 */
	@Shadow
	private ResourcePackProfile serverContainer;

	/**
	 * Removes the pin from the Resource Pack, that way it's not pinned to the top and can be moved.
	 * @return false
	 */

	@ModifyArg(method = "loadServerPack(Ljava/io/File;Lnet/minecraft/resource/ResourcePackSource;)Ljava/util/concurrent/CompletableFuture;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ResourcePackProfile;<init>(Ljava/lang/String;ZLjava/util/function/Supplier;Lnet/minecraft/text/Text;Lnet/minecraft/text/Text;Lnet/minecraft/resource/ResourcePackCompatibility;Lnet/minecraft/resource/ResourcePackProfile$InsertionPosition;ZLnet/minecraft/resource/ResourcePackSource;)V"),
			index = 7)
	private boolean isPinned(boolean isPinned) {
		ServerPackUnlocker.LOGGER.info("ServerResourcePack::loadServerPack(ResourcePackSource) was called, that means this server has a Resource Pack.");
		return false;
	}

	/**
	 * Disables forcing the resource pack to be enabled.
	 * @return false
	 */
	@ModifyArg(method = "loadServerPack(Ljava/io/File;Lnet/minecraft/resource/ResourcePackSource;)Ljava/util/concurrent/CompletableFuture;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ResourcePackProfile;<init>(Ljava/lang/String;ZLjava/util/function/Supplier;Lnet/minecraft/text/Text;Lnet/minecraft/text/Text;Lnet/minecraft/resource/ResourcePackCompatibility;Lnet/minecraft/resource/ResourcePackProfile$InsertionPosition;ZLnet/minecraft/resource/ResourcePackSource;)V"),
			index = 1)
	private boolean forcedEnabled(boolean enabled) {
		return false;
	}


	/**
	 * Hooks into loadServerPack()
	 * right before the method returns, after serverContainer was set, and copies it.
	 */
	@Inject(method = "loadServerPack(Ljava/io/File;Lnet/minecraft/resource/ResourcePackSource;)Ljava/util/concurrent/CompletableFuture;" ,
	at = @At("TAIL"))
	public void setCurrentServerResourcePack(CallbackInfoReturnable<CompletableFuture<Void>> ci) {
		ServerPackUnlocker.modInstance.currentServerResourcePack = serverContainer;
		LOGGER.info("Server Pack was loaded: " + serverContainer.getName());

	}

	/**
	 * Disables sha1 check, sha1 check compares server file and client file hashes. <br>
	 * sha1 check is <b>not</b> used to check if the current pack is outdated.
	 */
	@Inject(method = "verifyFile", at = @At(value = "HEAD"), cancellable = true)
	private void noSha1Check(String sha1, File file, CallbackInfoReturnable<Boolean> ci) {
		// sets return value to true, that way all packs are validated
		ci.setReturnValue(true);
	}
}