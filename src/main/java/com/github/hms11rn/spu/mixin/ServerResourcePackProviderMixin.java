package com.github.hms11rn.spu.mixin;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.hms11rn.spu.ServerConnectionHandler;

import net.minecraft.client.resource.ServerResourcePackProvider;

@Mixin(ServerResourcePackProvider.class)
public class ServerResourcePackProviderMixin {

	Logger LOGGER = LoggerFactory.getLogger("spu");

	@ModifyArg(method = "loadServerPack(Ljava/io/File;Lnet/minecraft/resource/ResourcePackSource;)Ljava/util/concurrent/CompletableFuture;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ResourcePackProfile;of(Ljava/lang/String;Lnet/minecraft/text/Text;ZLnet/minecraft/resource/ResourcePackProfile$PackFactory;Lnet/minecraft/resource/ResourcePackProfile$Metadata;Lnet/minecraft/resource/ResourceType;Lnet/minecraft/resource/ResourcePackProfile$InsertionPosition;ZLnet/minecraft/resource/ResourcePackSource;)Lnet/minecraft/resource/ResourcePackProfile;"), index = 7)
	private boolean loadServerPack(boolean isPinned) {
		LOGGER.info("Mixin/: Joined server that requires Resource Pack.");
		ServerConnectionHandler.requiresPack = true;
		return false;
	}

	@ModifyArg(method = "loadServerPack(Ljava/io/File;Lnet/minecraft/resource/ResourcePackSource;)Ljava/util/concurrent/CompletableFuture;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ResourcePackProfile;of(Ljava/lang/String;Lnet/minecraft/text/Text;ZLnet/minecraft/resource/ResourcePackProfile$PackFactory;Lnet/minecraft/resource/ResourcePackProfile$Metadata;Lnet/minecraft/resource/ResourceType;Lnet/minecraft/resource/ResourcePackProfile$InsertionPosition;ZLnet/minecraft/resource/ResourcePackSource;)Lnet/minecraft/resource/ResourcePackProfile;"), index = 2)
	private boolean forcedEnabled(boolean enabled) {
		return false;
	}

	@Inject(method = "verifyFile",
	at = @At(value = "HEAD"), cancellable = true)
	private void noSha1Check(String sha1, File file, CallbackInfoReturnable<Boolean> ci) {
		ci.setReturnValue(true);
	}
}