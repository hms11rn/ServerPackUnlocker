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
    public void buildEnabledProfilesInjections(Collection<String> enabledNames, CallbackInfoReturnable<List<ResourcePackProfile>> ci) {
        ServerPackUnlocker spu = ServerPackUnlocker.modInstance;
        ResourcePackProfile serverPackProfile = spu.currentServerResourcePack;
        if (serverPackProfile != null) {
            List<ResourcePackProfile> enabledPacks = new ArrayList<>(ci.getReturnValue());
            // using int for justClosedPackScreen for testing, for final release I'll see if I can use boolean
            if (spu.justClosedPackScreen > 0) {
                if (ServerPackUnlocker.modInstance.justClosedPackScreen == 1) {
                    ServerPackUnlocker.modInstance.justClosedPackScreen = 0;
                    LOGGER.info("Enabled resource packs: " + enabledNames);
                }
                boolean doesContain = enabledNames.contains(serverPackProfile.getName());
                spu.settings.setEnabled(spu.getCurrentServer(), doesContain);
                int packIndex =  new ArrayList<>(enabledNames).indexOf(serverPackProfile.getName());
                if (packIndex == (enabledNames.size() - 1))
                    packIndex = -1;
                spu.settings.setIndex(spu.getCurrentServer(), packIndex);
                spu.settings.writeSettings();

                return;
            }
            if (spu.shouldEnableServerPack()) {
                if (!enabledNames.contains(serverPackProfile.getName())) {
                    int packIndex = spu.settings.getIndex(spu.getCurrentServer());
                        if (packIndex == -1)
                            packIndex = enabledNames.size();
                    enabledPacks.add(packIndex, MinecraftClient.getInstance().getResourcePackManager().getProfile(serverPackProfile.getName()));
                }

            }
            ci.setReturnValue(enabledPacks);
        }
    }
}
