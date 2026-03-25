package com.plusls.MasaGadget.mixin.mod_tweak.tweakeroo.inventoryPreviewSupportTradeOfferList;

import com.plusls.MasaGadget.game.Configs;
import com.plusls.MasaGadget.impl.generic.HitResultHandler;
import com.plusls.MasaGadget.util.ModId;
import com.plusls.MasaGadget.util.VillagerDataUtil;
import fi.dy.masa.malilib.render.GuiContext;
import fi.dy.masa.malilib.render.InventoryOverlay;
import fi.dy.masa.malilib.render.InventoryOverlayType;
import fi.dy.masa.malilib.util.GuiUtils;
import fi.dy.masa.tweakeroo.renderer.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import top.hendrixshen.magiclib.api.dependency.annotation.Dependencies;
import top.hendrixshen.magiclib.api.dependency.annotation.Dependency;
import top.hendrixshen.magiclib.libs.com.llamalad7.mixinextras.sugar.Local;

@Dependencies(require = @Dependency(ModId.tweakeroo))
@Mixin(value = RenderUtils.class, remap = false)
public class MixinRenderUtils {
    @Unique
    private static final int masa_gadget$maxTradeOfferSize = 9;

    @ModifyVariable(
            method = "renderPlayerInventoryOverlay",
            at = @At(
                    value = "INVOKE",
                    target = "Lfi/dy/masa/malilib/util/GuiUtils;getScaledWindowWidth()I",
                    remap = false
            ),
            ordinal = 0
    )
    private static Container renderTradeOfferList(
            Container inv,
            @Local(argsOnly = true) GuiContext guiGraphics
    ) {
        if (!Configs.inventoryPreviewSupportTradeOfferList.getBooleanValue()) {
            return inv;
        }

        Entity entity = HitResultHandler.getInstance().getHitEntity().orElse(null);

        if (!(entity instanceof Villager)) {
            return inv;
        }

        Villager villager = (Villager) entity;

        ResourceKey<VillagerProfession> noneKey = VillagerProfession.NONE;
        if (noneKey != null && noneKey.equals(VillagerDataUtil.getVillagerProfession(villager))) {
            return inv;
        }

        SimpleContainer simpleInventory = new SimpleContainer(MixinRenderUtils.masa_gadget$maxTradeOfferSize);

        for (MerchantOffer tradeOffer : villager.getOffers()) {
            for (int i = 0; i < simpleInventory.getContainerSize(); ++i) {
                ItemStack itemStack = simpleInventory.getItem(i);

                if (itemStack.isEmpty()) {
                    simpleInventory.setItem(i, tradeOffer.getResult().copy());
                    break;
                }
            }
        }

        int x = GuiUtils.getScaledWindowWidth() / 2 - 88;
        int y = GuiUtils.getScaledWindowHeight() / 2 - 5;
        int slotOffsetX = 8;
        int slotOffsetY = 8;
        InventoryOverlayType type = InventoryOverlayType.GENERIC;
        DyeColor dye = DyeColor.GREEN;
        InventoryOverlay.renderInventoryBackground(
                guiGraphics,
                type,
                x,
                y,
                MixinRenderUtils.masa_gadget$maxTradeOfferSize,
                MixinRenderUtils.masa_gadget$maxTradeOfferSize,
                dye.getTextureDiffuseColor()
        );
        InventoryOverlay.renderInventoryStacks(
                guiGraphics,
                type,
                simpleInventory,
                x + slotOffsetX,
                y + slotOffsetY,
                MixinRenderUtils.masa_gadget$maxTradeOfferSize,
                0,
                MixinRenderUtils.masa_gadget$maxTradeOfferSize
        );
        return inv;
    }
}
