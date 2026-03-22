package com.plusls.MasaGadget.impl.feature.entityInfo;

import com.plusls.MasaGadget.mixin.accessor.AccessorVillager;
import com.plusls.MasaGadget.util.PcaSyncProtocol;
import com.plusls.MasaGadget.util.VillagerDataUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.NotNull;
import top.hendrixshen.magiclib.util.minecraft.ComponentUtil;

public class VillagerNextRestockTimeInfo {
    public static Component getInfo(@NotNull Villager villager) {
        long nextRestockTime;
        long nextWorkTime;
        long timeOfDay = villager.level().getDayTime() % 24000;

        ResourceKey<VillagerProfession> profession = VillagerDataUtil.getVillagerProfession(villager);
        ResourceKey<VillagerProfession> profNone = VillagerProfession.NONE;
        ResourceKey<VillagerProfession> profNitwit = VillagerProfession.NITWIT;

        if (profession == null || profession.equals(profNone) || profession.equals(profNitwit)) {
            return null;
        }

        if (!Minecraft.getInstance().hasSingleplayerServer() && !PcaSyncProtocol.enable) {
            return ComponentUtil.tr("masa_gadget_mod.message.no_data").withStyle(ChatFormatting.YELLOW);
        }

        if (timeOfDay >= 2000 && timeOfDay <= 9000) {
            nextWorkTime = 0;
        } else {
            nextWorkTime = timeOfDay < 2000 ? 2000 - timeOfDay : 24000 - timeOfDay + 2000;
        }

        int numberOfRestocksToday = ((AccessorVillager) villager).masa_gadget_mod$getNumberOfRestocksToday();
        long lastRestockGameTime = ((AccessorVillager) villager).masa_gadget_mod$getLastRestockGameTime();

        if (numberOfRestocksToday == 0) {
            nextRestockTime = 0;
        } else if (numberOfRestocksToday < 2) {
            nextRestockTime = Math.max(lastRestockGameTime + 2400 - villager.level().getGameTime(), 0);
        } else {
            nextRestockTime = 0x7fffffffffffffffL;
        }

        nextRestockTime = Math.min(nextRestockTime, Math.max(lastRestockGameTime + 12000L - villager.level().getGameTime(), 0));

        if (needsRestock(villager)) {
            if (timeOfDay + nextRestockTime > 8000) {
                nextRestockTime = 24000 - timeOfDay + 2000;
            } else {
                nextRestockTime = Math.max(nextRestockTime, nextWorkTime);
            }
        } else {
            nextRestockTime = 0;
        }

        if (nextRestockTime == 0) {
            return ComponentUtil.simple("OK").withStyle(ChatFormatting.GREEN);
        }

        return ComponentUtil.simple(String.format("%d", nextRestockTime));
    }

    private static boolean needsRestock(@NotNull Villager villager) {
        ResourceKey<VillagerProfession> profession = VillagerDataUtil.getVillagerProfession(villager);
        ResourceKey<VillagerProfession> profNone = VillagerProfession.NONE;

        if (profession != null && !profession.equals(profNone)) {
            try {
                for (MerchantOffer offer : villager.getOffers()) {
                    if (offer.isOutOfStock()) {
                        return true;
                    }
                }
            } catch (Exception ignored) {
                // Villager offers are not available on the client (e.g. villager is dead or being killed)
                return false;
            }
        }

        return false;
    }
}
