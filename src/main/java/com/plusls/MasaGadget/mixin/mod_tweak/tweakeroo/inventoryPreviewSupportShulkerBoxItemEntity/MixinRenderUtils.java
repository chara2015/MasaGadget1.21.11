package com.plusls.MasaGadget.mixin.mod_tweak.tweakeroo.inventoryPreviewSupportShulkerBoxItemEntity;

import com.plusls.MasaGadget.game.Configs;
import com.plusls.MasaGadget.impl.generic.HitResultHandler;
import com.plusls.MasaGadget.util.ModId;
import fi.dy.masa.tweakeroo.renderer.RenderUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import top.hendrixshen.magiclib.api.dependency.annotation.Dependencies;
import top.hendrixshen.magiclib.api.dependency.annotation.Dependency;

//#if MC >= 12106
//$$ import com.mojang.logging.LogUtils;
//$$ import net.minecraft.world.level.storage.TagValueInput;
//$$ import net.minecraft.world.level.storage.ValueInput;
//$$ import net.minecraft.util.ProblemReporter;
//$$ import org.slf4j.Logger;
//$$ import org.spongepowered.asm.mixin.Unique;
//#endif

//#if MC > 12004
//$$ import net.minecraft.client.Minecraft;
//$$ import net.minecraft.core.component.DataComponents;
//$$ import net.minecraft.world.item.component.CustomData;
//#endif

@Dependencies(require = @Dependency(ModId.tweakeroo))
@Mixin(value = RenderUtils.class, remap = false)
public abstract class MixinRenderUtils {
    @Unique
    private static final org.slf4j.Logger masa_gadget$logger = com.mojang.logging.LogUtils.getLogger();

    @ModifyVariable(
            method = "renderInventoryOverlay",
            at = @At(
                    value = "INVOKE",
                    target = "Lfi/dy/masa/malilib/util/GuiUtils;getScaledWindowWidth()I",
                    remap = false
            ),
            ordinal = 0
    )
    private static Container modifyInv(Container inv) {
        Container ret = inv;
        Entity traceEntity = HitResultHandler.getInstance().getHitEntity().orElse(null);

        if (Configs.inventoryPreviewSupportShulkerBoxItemEntity.getBooleanValue() &&
                ret == null &&
                traceEntity instanceof ItemEntity) {
            ItemStack itemStack = ((ItemEntity) traceEntity).getItem();
            Item item = itemStack.getItem();
            //#if MC > 12004
            //$$ CustomData invData = itemStack.get(DataComponents.BLOCK_ENTITY_DATA);
            //$$
            //$$ if (invData == null) {
            //$$     return null;
            //$$ }
            //$$
            //$$ CompoundTag invNbt = invData.copyTag();
            //#else
            net.minecraft.world.item.component.TypedEntityData<net.minecraft.world.level.block.entity.BlockEntityType<?>> beData =
                    itemStack.get(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA);
            CompoundTag invNbt = beData != null ? beData.copyTagWithoutId() : null;
            //#endif

            NonNullList<ItemStack> stacks = NonNullList.withSize(27, ItemStack.EMPTY);

            if (item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof ShulkerBoxBlock) {
                ret = new SimpleContainer(27);

                try (net.minecraft.util.ProblemReporter.ScopedCollector collector = new net.minecraft.util.ProblemReporter.ScopedCollector(masa_gadget$logger)) {
                    net.minecraft.world.level.storage.ValueInput input = net.minecraft.world.level.storage.TagValueInput.create(collector, net.minecraft.client.Minecraft.getInstance().level.registryAccess(), invNbt);
                    net.minecraft.world.ContainerHelper.loadAllItems(input, stacks);
                }

                for (int i = 0; i < 27; i++) {
                    ret.setItem(i, stacks.get(i));
                }
            }
        }

        return ret;
    }
}
