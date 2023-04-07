package com.github.hms11rn.spu.mixin;

import com.github.hms11rn.spu.ServerPackUnlocker;
import net.minecraft.client.gui.screen.pack.PackScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

/**
 * Used to get when the resource pack screen is closed
 *
 * @author hms11rn
 */
@Mixin(PackScreen.class)
public class PackScreenMixin {

    /**
     * Hooks into close() and sets justClosedPackScreen to 1
     * so {@link com.github.hms11rn.spu.mixin.ResourcePackManagerMixin#buildEnabledProfilesInjections(Collection, CallbackInfoReturnable)} knows
     * that the build of enabled profiles is because resource pack screen was closed
     * @param in not used
     */
    @Inject(method = "close", at = @At("HEAD"))
    private void closedPackScreen(CallbackInfo in) {
        if (ServerPackUnlocker.modInstance.getCurrentServer() != null)
             ServerPackUnlocker.modInstance.justClosedPackScreen = 1;
    }
}
