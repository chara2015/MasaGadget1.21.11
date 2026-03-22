package com.plusls.MasaGadget.util;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;

public class VillagerDataUtil {
    public static ResourceKey<VillagerProfession> getVillagerProfession(Villager villager) {
        return villager.getVillagerData().profession().unwrapKey().orElse(null);
    }
}
