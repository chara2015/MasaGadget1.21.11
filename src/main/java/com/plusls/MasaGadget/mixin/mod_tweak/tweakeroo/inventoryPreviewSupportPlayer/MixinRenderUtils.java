package com.plusls.MasaGadget.mixin.mod_tweak.tweakeroo.inventoryPreviewSupportPlayer;

import com.plusls.MasaGadget.game.Configs;
import com.plusls.MasaGadget.impl.generic.HitResultHandler;
import com.plusls.MasaGadget.util.ModId;
import fi.dy.masa.malilib.render.GuiContext;
import fi.dy.masa.malilib.render.InventoryOverlay;
import fi.dy.masa.malilib.render.InventoryOverlayType;
import fi.dy.masa.malilib.util.GuiUtils;
import fi.dy.masa.tweakeroo.renderer.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import top.hendrixshen.magiclib.api.compat.minecraft.world.entity.player.PlayerCompat;
import top.hendrixshen.magiclib.api.dependency.annotation.Dependencies;
import top.hendrixshen.magiclib.api.dependency.annotation.Dependency;
import top.hendrixshen.magiclib.libs.com.llamalad7.mixinextras.sugar.Local;

@Dependencies(require = @Dependency(ModId.tweakeroo))
@Mixin(value = RenderUtils.class, remap = false)
public abstract class MixinRenderUtils {
    @ModifyVariable(
            method = "renderInventoryOverlay",
            at = @At(
                    value = "INVOKE",
                    target = "Lfi/dy/masa/malilib/util/GuiUtils;getScaledWindowWidth()I",
                    remap = false
            )
    )
    private static Container modifyInv(
            Container inv,
            @Local(argsOnly = true) GuiContext guiGraphics
    ) {
        Container ret = inv;
        Entity traceEntity = HitResultHandler.getInstance().getHitEntity().orElse(null);

        if (Configs.inventoryPreviewSupportPlayer.getBooleanValue() &&
                ret == null && traceEntity instanceof Player) {
            Player player = (Player) traceEntity;
            PlayerCompat playerCompat = PlayerCompat.of(player);
            ret = playerCompat.getInventory();
            int x = GuiUtils.getScaledWindowWidth() / 2 - 88;
            int y = GuiUtils.getScaledWindowHeight() / 2 + 10;
            int slotOffsetX = 8;
            int slotOffsetY = 8;
            InventoryOverlayType type = InventoryOverlayType.GENERIC;
            DyeColor dye = DyeColor.GRAY;
            InventoryOverlay.renderInventoryBackground(
                    guiGraphics,
                    type,
                    x,
                    y,
                    9,
                    27,
                    dye.getTextureDiffuseColor()
            );
            InventoryOverlay.renderInventoryStacks(
                    guiGraphics,
                    type,
                    player.getEnderChestInventory(),
                    x + slotOffsetX,
                    y + slotOffsetY,
                    9,
                    0,
                    27
            );
        }

        return ret;
    }
}
