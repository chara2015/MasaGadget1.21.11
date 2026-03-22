package com.plusls.MasaGadget.impl.mod_tweak.tweakeroo.inventoryPreviewSupportSelect;

import com.plusls.MasaGadget.game.Configs;
import com.plusls.MasaGadget.impl.generic.HitResultHandler;
import com.plusls.MasaGadget.util.ModId;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.hotkeys.IMouseInputHandler;
import net.minecraft.client.input.MouseButtonEvent;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.config.Hotkeys;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;
import top.hendrixshen.magiclib.MagicLib;

import net.minecraft.core.registries.BuiltInRegistries;

public class MouseScrollInputHandler implements IMouseInputHandler {
    @Getter
    private static final MouseScrollInputHandler instance = new MouseScrollInputHandler();

    @ApiStatus.Internal
    public void init() {
        InputEventHandler.getInputManager().registerMouseInputHandler(instance);
    }

    @Override
    public boolean onMouseScroll(double mouseX, double mouseY, double amount) {
        Player player = Minecraft.getInstance().player;

        if (!MagicLib.getInstance().getCurrentPlatform().isModLoaded(ModId.tweakeroo) ||
                !Configs.inventoryPreviewSupportSelect.getBooleanValue() ||
                !((fi.dy.masa.malilib.config.IConfigBoolean)(Object) FeatureToggle.TWEAK_INVENTORY_PREVIEW).getBooleanValue() ||
                !HitResultHandler.getInstance().getLastInventoryPreviewStatus()) {
            return false;
        }

        if (amount < 0) {
            InventoryOverlayRenderHandler.getInstance().scrollerUp();
        } else if (amount > 0) {
            InventoryOverlayRenderHandler.getInstance().scrollerDown();
        }

        return !MagicLib.getInstance().getCurrentPlatform().isModLoaded(ModId.litematica) ||
                !fi.dy.masa.litematica.config.Configs.Generic.TOOL_ITEM_ENABLED.getBooleanValue() ||
                player == null ||
                //$$ !BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem()).toString()
                //$$         .contains(fi.dy.masa.litematica.config.Configs.Generic.TOOL_ITEM.getStringValue());
                //#else
                !BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem()).toString()
                        .contains(fi.dy.masa.litematica.config.Configs.Generic.TOOL_ITEM.getStringValue());
        //#endif
    }

    @Override
    public boolean onMouseClick(MouseButtonEvent click, boolean eventButtonState) {
        if (MagicLib.getInstance().getCurrentPlatform().isModLoaded(ModId.tweakeroo) &&
                Configs.inventoryPreviewSupportSelect.getBooleanValue() &&
                ((fi.dy.masa.malilib.config.IConfigBoolean)(Object) FeatureToggle.TWEAK_INVENTORY_PREVIEW).getBooleanValue() &&
                Hotkeys.INVENTORY_PREVIEW.getKeybind().isKeybindHeld() &&
                click.input() == 2 &&
                eventButtonState) {
            InventoryOverlayRenderHandler.getInstance().switchSelectInventory();
        }

        return false;
    }
}
