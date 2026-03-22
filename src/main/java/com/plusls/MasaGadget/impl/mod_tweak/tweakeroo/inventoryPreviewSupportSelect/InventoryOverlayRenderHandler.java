package com.plusls.MasaGadget.impl.mod_tweak.tweakeroo.inventoryPreviewSupportSelect;

import com.plusls.MasaGadget.game.Configs;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.GuiUtils;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.hendrixshen.magiclib.api.render.context.GuiRenderContext;

import java.util.List;
import java.util.stream.Collectors;

public class InventoryOverlayRenderHandler {
    @Getter(lazy = true)
    private static final InventoryOverlayRenderHandler instance = new InventoryOverlayRenderHandler();
    private static final int UN_SELECTED = 114514;

    private int selectedSlot = InventoryOverlayRenderHandler.UN_SELECTED;
    private int currentSlot = -1;
    private int renderX = -1;
    private int renderY = -1;
    private ItemStack itemStack = null;
    private boolean selectInventory = false;
    private boolean renderingSubInventory = false;
    private int subSelectedSlot = InventoryOverlayRenderHandler.UN_SELECTED;
    private int subCurrentSlot = -1;
    private int subRenderX = -1;
    private int subRenderY = -1;
    private ItemStack subItemStack = null;

    public static void onHitCallback(@Nullable HitResult hitResult, boolean oldStatus, boolean stateChanged) {
        if (!((fi.dy.masa.malilib.config.IConfigBoolean)(Object) FeatureToggle.TWEAK_INVENTORY_PREVIEW).getBooleanValue() ||
                !Configs.inventoryPreviewSupportSelect.getBooleanValue()) {
            return;
        }

        if (!oldStatus) {
            InventoryOverlayRenderHandler.getInstance().resetSelectedSlot();
        }
    }

    public void render(@NotNull GuiRenderContext renderContext) {
        if (this.currentSlot == 0) {
            return;
        }

        if (this.selectedSlot != InventoryOverlayRenderHandler.UN_SELECTED &&
                this.adjustSelectedSlot() &&
                this.itemStack != null) {
            this.attachToSubShulkerBoxView(renderContext);
            this.attachToMainInventoryView(renderContext);
        }

        this.dropState();
    }

    private void attachToMainInventoryView(GuiRenderContext renderContext) {
        if (!this.selectInventory) {
            this.renderSlotHighlight(renderContext, this.renderX, this.renderY);
            this.renderTooltip(renderContext, this.itemStack, this.renderX, this.renderY);
        }
    }

    private void attachToSubShulkerBoxView(GuiRenderContext renderContext) {
        if (!this.selectInventory) {
            return;
        }

        if (!(this.itemStack.getItem() instanceof BlockItem) ||
                !(((BlockItem) this.itemStack.getItem()).getBlock() instanceof ShulkerBoxBlock)) {
            this.switchSelectInventory();
            return;
        }

        this.renderSlotHighlight(renderContext, this.renderX, this.renderY);
        this.renderingSubInventory = true;
        RenderUtils.renderShulkerBoxPreview(
                (fi.dy.masa.malilib.render.GuiContext) renderContext.getGuiComponent(),
                this.itemStack,
                GuiUtils.getScaledWindowWidth() / 2 - 96,
                GuiUtils.getScaledWindowHeight() / 2 + 30,
                true
        );
        this.renderingSubInventory = false;

        if (this.subSelectedSlot != InventoryOverlayRenderHandler.UN_SELECTED &&
                this.adjustSubSelectedSlot() &&
                this.subItemStack != null
        ) {
            renderContext.pushMatrix();
            this.renderSlotHighlight(renderContext, this.subRenderX, this.subRenderY);
            this.renderTooltip(renderContext, this.subItemStack, this.subRenderX, this.subRenderY);
            renderContext.popMatrix();
        }
    }

    private boolean adjustSelectedSlot() {
        int oldSelectedSlot = this.selectedSlot;

        if (this.currentSlot > 0) {
            while (this.selectedSlot < 0) {
                this.selectedSlot += this.currentSlot;
            }
            this.selectedSlot %= this.currentSlot;
        }

        return oldSelectedSlot == this.selectedSlot;
    }

    private boolean adjustSubSelectedSlot() {
        int oldSelectedSlot = this.subSelectedSlot;

        if (this.subCurrentSlot > 0) {
            while (this.subSelectedSlot < 0) {
                this.subSelectedSlot += this.subCurrentSlot;
            }
            this.subSelectedSlot %= this.subCurrentSlot;
        }

        return oldSelectedSlot == this.subSelectedSlot;
    }

    public void updateState(int x, int y, ItemStack stack) {
        if (this.renderingSubInventory) {
            if (this.subCurrentSlot++ == this.subSelectedSlot) {
                this.subRenderX = x;
                this.subRenderY = y;
                this.subItemStack = stack;
            }
        } else {
            if (this.currentSlot++ == this.selectedSlot) {
                this.renderX = x;
                this.renderY = y;
                this.itemStack = stack;
            }
        }
    }

    private void dropState() {
        this.currentSlot = 0;
        this.itemStack = null;
        this.renderX = 0;
        this.renderY = 0;
        this.subCurrentSlot = 0;
        this.subItemStack = null;
        this.subRenderX = 0;
        this.subRenderY = 0;
    }

    public void switchSelectInventory() {
        this.selectInventory = !this.selectInventory;
        this.subSelectedSlot = InventoryOverlayRenderHandler.UN_SELECTED;
    }

    private void resetSelectedSlot() {
        this.selectedSlot = InventoryOverlayRenderHandler.UN_SELECTED;

        if (this.selectInventory) {
            this.switchSelectInventory();
        }
    }

    public void scrollerUp() {
        this.moveSelectedSlot(1);
    }

    public void scrollerDown() {
        this.moveSelectedSlot(-1);
    }

    private void moveSelectedSlot(int n) {
        if (this.selectInventory) {
            if (this.subSelectedSlot == InventoryOverlayRenderHandler.UN_SELECTED) {
                this.subSelectedSlot = 0;
            } else {
                this.subSelectedSlot += n;
            }
        } else {
            if (this.selectedSlot == InventoryOverlayRenderHandler.UN_SELECTED) {
                this.selectedSlot = 0;
            } else {
                this.selectedSlot += n;
            }
        }
    }

    private void renderSlotHighlight(@NotNull GuiRenderContext renderContext, int x, int y) {
        renderContext.getGuiComponent().fillGradient(
                x,
                y,
                x + 16,
                y + 16,
                0x80FFFFFF,
                0x80FFFFFF
        );
    }

    private void renderTooltip(GuiRenderContext renderContext, @NotNull ItemStack itemStack, int x, int y) {
        Minecraft mc = Minecraft.getInstance();
        List<Component> tooltipLines = Screen.getTooltipFromItem(mc, itemStack);
        List<ClientTooltipComponent> clientComponents = tooltipLines.stream()
                .map(c -> ClientTooltipComponent.create(c.getVisualOrderText()))
                .collect(Collectors.toList());
        renderContext.getGuiComponent().renderTooltip(
                mc.font,
                clientComponents,
                x,
                y,
                DefaultTooltipPositioner.INSTANCE,
                null
        );
    }
}
