package com.plusls.MasaGadget.mixin.mod_tweak.common;

import com.plusls.MasaGadget.api.gui.MasaGadgetDropdownList;
import fi.dy.masa.malilib.gui.GuiBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiBase.class, remap = false)
public abstract class MixinGuiBase {
    @Inject(method = "initGui", at = @At("RETURN"))
    private void handleInitGui(CallbackInfo ci) {
        if (this instanceof MasaGadgetDropdownList) {
            ((MasaGadgetDropdownList) this).masa_gadget_mod$addFastSwitcherWidget();
        }
    }
}
