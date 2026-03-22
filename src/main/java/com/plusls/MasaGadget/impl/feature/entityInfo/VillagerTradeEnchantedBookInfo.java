package com.plusls.MasaGadget.impl.feature.entityInfo;

import com.google.common.collect.Lists;
import com.plusls.MasaGadget.util.PcaSyncProtocol;
import com.plusls.MasaGadget.util.VillagerDataUtil;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.NotNull;
import top.hendrixshen.magiclib.api.compat.minecraft.world.item.ItemStackCompat;
import top.hendrixshen.magiclib.util.minecraft.ComponentUtil;

import java.util.Collections;
import java.util.List;

public class VillagerTradeEnchantedBookInfo {
    public static @NotNull List<Component> getInfo(@NotNull Villager villager) {
        ResourceKey<VillagerProfession> profession = VillagerDataUtil.getVillagerProfession(villager);
        ResourceKey<VillagerProfession> profLibrarian = VillagerProfession.LIBRARIAN;

        if (!profLibrarian.equals(profession)) {
            return Collections.emptyList();
        }

        if (!Minecraft.getInstance().hasSingleplayerServer() && !PcaSyncProtocol.enable) {
            return Lists.newArrayList(ComponentUtil.tr("masa_gadget_mod.message.no_data").withStyle(ChatFormatting.YELLOW));
        }

        List<Component> ret = Lists.newArrayList();

        for (MerchantOffer tradeOffer : villager.getOffers()) {
            ItemStack sellItem = tradeOffer.getResult();
            ItemStackCompat sellItemCompat = ItemStackCompat.of(sellItem);

            if (!sellItemCompat.is(Items.ENCHANTED_BOOK)) {
                continue;
            }

            ItemEnchantments enchantmentData = EnchantmentHelper.getEnchantmentsForCrafting(sellItem);

            for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantmentData.entrySet()) {
                int level = entry.getIntValue();
                int cost = tradeOffer.getBaseCostA().getCount();
                int minCost = 2 + 3 * level;
                int maxCost = minCost + 4 + level * 10;

                if (entry.getKey().is(EnchantmentTags.DOUBLE_TRADE_PRICE)) {
                    minCost *= 2;
                    maxCost *= 2;
                }

                ChatFormatting cast_color;
                int one_third = (maxCost - minCost) / 3;

                if (cost <= one_third + minCost) {
                    cast_color = ChatFormatting.GREEN;
                } else if (cost <= one_third * 2 + minCost) {
                    cast_color = ChatFormatting.WHITE;
                } else {
                    cast_color = ChatFormatting.RED;
                }

                ChatFormatting enchantment_level_color;

                if (level == entry.getKey().value().getMaxLevel()) {
                    enchantment_level_color = ChatFormatting.GOLD;
                } else {
                    enchantment_level_color = ChatFormatting.WHITE;
                }

                ret.add(((MutableComponent) Enchantment.getFullname(entry.getKey(), entry.getIntValue())).withStyle(enchantment_level_color));
                ret.add(ComponentUtil.simple(String.format("%d(%d-%d)", cost, minCost, maxCost)).withStyle(cast_color));
            }

            break;
        }

        return ret;
    }
}
