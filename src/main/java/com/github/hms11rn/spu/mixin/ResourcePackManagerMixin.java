package com.github.hms11rn.spu.mixin;


import com.github.hms11rn.spu.ServerPackUnlocker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.github.hms11rn.spu.ServerPackUnlocker.LOGGER;

/**
 * Adds server resource pack to enabled resource packs by hooking into buildEnableProfiles
 *
 * @author TheMysterys
 * @author hms11
 */
@Mixin(ResourcePackManager.class)
public class ResourcePackManagerMixin {


    /**
     * Hooks into {@link net.minecraft.resource.ResourcePackManager#buildEnabledProfiles(java.util.Collection)}
     * and adds Server Pack into enabled packs
     * @param enabledNames string of currently enabled names
     * @param ci  used to cancel and change return value
     */
    @Inject(method = "buildEnabledProfiles", at = @At("RETURN"), cancellable = true)
    public void buildEnabledProfilesInjections( Collection<String> enabledNames, CallbackInfoReturnable<List<ResourcePackProfile>> ci) {
        ResourcePackProfile serverPackProfile = ServerPackUnlocker.modInstance.currentServerResourcePack;
        if (serverPackProfile != null) {
            List<ResourcePackProfile> enabledPacks = new ArrayList<>(ci.getReturnValue());
            // buildEnableProfiles is called 3 times, so making sure that justClosedPackScreen is reset only after the 3 calls
            if (ServerPackUnlocker.modInstance.justClosedPackScreen > 0) {
                ServerPackUnlocker.modInstance.justClosedPackScreen++;
                if (ServerPackUnlocker.modInstance.justClosedPackScreen == 3) {
                    ServerPackUnlocker.modInstance.justClosedPackScreen = 0;
                    LOGGER.info("Enabled resource packs: " + enabledNames);
                }
                boolean doesContain = enabledNames.contains(serverPackProfile.getName());
                ServerPackUnlocker.modInstance.settings.setEnabled(ServerPackUnlocker.modInstance.getCurrentServer(), doesContain);
                ServerPackUnlocker.modInstance.settings.writeSettings();

                return;
            }
            if (ServerPackUnlocker.modInstance.shouldEnableServerPack()) {
                if (!enabledNames.contains(serverPackProfile.getName()))
                    enabledPacks.add(MinecraftClient.getInstance().getResourcePackManager().getProfile(serverPackProfile.getName()));
            }
            ci.setReturnValue(enabledPacks);
        }
    }
}
