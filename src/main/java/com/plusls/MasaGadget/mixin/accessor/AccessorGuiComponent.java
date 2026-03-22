package com.plusls.MasaGadget.mixin.accessor;

import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

// GuiComponent was removed in 1.21+. This accessor is kept for compatibility but is no longer used.
@Pseudo
@Mixin(targets = "net.minecraft.client.gui.GuiComponent", remap = false)
public interface AccessorGuiComponent {
}
