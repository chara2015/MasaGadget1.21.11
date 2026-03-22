package com.plusls.MasaGadget.mixin.mod_tweak.tweakeroo.inventoryPreviewSupportSelect;

import com.plusls.MasaGadget.game.Configs;
import com.plusls.MasaGadget.impl.mod_tweak.tweakeroo.inventoryPreviewSupportSelect.InventoryOverlayRenderHandler;
import com.plusls.MasaGadget.util.ModId;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.hendrixshen.magiclib.api.dependency.annotation.Dependencies;
import top.hendrixshen.magiclib.api.dependency.annotation.Dependency;
import top.hendrixshen.magiclib.api.render.context.RenderContext;

//#if MC > 12006
//$$ import net.minecraft.client.DeltaTracker;
//#endif

//#if MC > 11904
//$$ import net.minecraft.client.gui.GuiGraphics;
//#elseif MC > 11502
import com.mojang.blaze3d.vertex.PoseStack;
//#endif

@Dependencies(require = @Dependency(ModId.tweakeroo))
@Mixin(value = Gui.class, priority = 1100)
public abstract class MixinInGameHud {
    @Inject(method = "render", at = @At("RETURN"))
    private void onGameOverlayPost(
            net.minecraft.client.gui.GuiGraphics poseStackOrGuiGraphics,
            net.minecraft.client.DeltaTracker deltaTracker,
            CallbackInfo ci
    ) {
        if (Configs.inventoryPreviewSupportSelect.getBooleanValue()) {
            InventoryOverlayRenderHandler.getInstance().render(RenderContext.gui(poseStackOrGuiGraphics));
        }
    }
}
