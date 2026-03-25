package com.plusls.MasaGadget.impl.feature.entityInfo;

import com.google.common.collect.Queues;
import com.plusls.MasaGadget.game.Configs;
import com.plusls.MasaGadget.util.MiscUtil;
import com.plusls.MasaGadget.util.SyncUtil;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import top.hendrixshen.magiclib.MagicLib;
import top.hendrixshen.magiclib.api.event.minecraft.render.RenderEntityListener;
import top.hendrixshen.magiclib.api.event.minecraft.render.RenderLevelListener;
import top.hendrixshen.magiclib.api.render.context.LevelRenderContext;
import top.hendrixshen.magiclib.impl.render.TextRenderer;
import top.hendrixshen.magiclib.impl.render.context.EntityRenderContext;
import top.hendrixshen.magiclib.util.minecraft.render.RenderUtil;

import java.util.Queue;

public class EntityInfoRenderer implements RenderEntityListener, RenderLevelListener {
    @Getter
    private static final EntityInfoRenderer instance = new EntityInfoRenderer();
    private final Queue<Entity> queue = Queues.newConcurrentLinkedQueue();

    @ApiStatus.Internal
    public void init() {
        MagicLib.getInstance().getEventManager().register((Class) RenderEntityListener.class, (top.hendrixshen.magiclib.api.event.Listener) (Object) this);
        MagicLib.getInstance().getEventManager().register((Class) RenderLevelListener.class, (top.hendrixshen.magiclib.api.event.Listener) (Object) this);
    }

    private static TextRenderer rotationAround(@NotNull TextRenderer renderer, @NotNull Position centerPos, double range) {
        Entity cameraEntity = Minecraft.getInstance().player;
        if (cameraEntity == null) {
            return renderer.at(centerPos.x(), centerPos.y(), centerPos.z());
        }
        Position camPos = cameraEntity.position();
        float xAngle = (float) Mth.atan2(camPos.z() - centerPos.z(), camPos.x() - centerPos.x());
        float zAngle = (float) Mth.atan2(camPos.x() - centerPos.x(), camPos.z() - centerPos.z());
        return renderer.at(range * Mth.cos(xAngle) + centerPos.x(), centerPos.y(), range * Mth.cos(zAngle) + centerPos.z());
    }

    public void preRenderEntity(Entity entity, EntityRenderContext renderContext) {
        // NO-OP
    }

    public void postRenderEntity(Entity entity, EntityRenderContext renderContext) {
        // NO-OP: RenderEntityListener is not available in MC 1.21.10+
    }

    @Override
    public void preRenderLevel(ClientLevel level, LevelRenderContext renderContext) {
        // NO-OP
    }

    @Override
    public void postRenderLevel(ClientLevel level, LevelRenderContext renderContext) {
        if (Configs.renderNextRestockTime.getBooleanValue() ||
                Configs.renderTradeEnchantedBook.getBooleanValue() ||
                Configs.renderZombieVillagerConvertTime.getBooleanValue()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                if (Configs.renderNextRestockTime.getBooleanValue() || Configs.renderTradeEnchantedBook.getBooleanValue()) {
                    level.getEntitiesOfClass(Villager.class, mc.player.getBoundingBox().inflate(64)).forEach(this.queue::add);
                }
                if (Configs.renderZombieVillagerConvertTime.getBooleanValue()) {
                    level.getEntitiesOfClass(ZombieVillager.class, mc.player.getBoundingBox().inflate(64)).forEach(this.queue::add);
                }
            }
        }

        float partialTick = RenderUtil.getPartialTick();

        for (Entity entity : this.queue) {
            if (entity instanceof Villager) {
                Villager villager = MiscUtil.cast(SyncUtil.syncEntityDataFromIntegratedServer(entity));
                TextRenderer renderer = TextRenderer.create();

                if (Configs.renderNextRestockTime.getBooleanValue()) {
                    Component info = VillagerNextRestockTimeInfo.getInfo(villager);

                    if (info != null) {
                        renderer.addLine(info);
                    }
                }

                if (Configs.renderTradeEnchantedBook.getBooleanValue()) {
                    VillagerTradeEnchantedBookInfo.getInfo(villager).forEach(renderer::addLine);
                }

                if (villager.isSleeping()) {
                    Position position = entity.getEyePosition(partialTick);
                    renderer.at(position.x(), position.y() + 0.4F, position.z());
                } else {
                    EntityInfoRenderer.rotationAround(renderer, entity.getEyePosition(partialTick), 0.6);
                }

                renderer.bgColor((int) (Minecraft.getInstance().options.getBackgroundOpacity(0.25F) * 255.0F) << 24)
                        .fontScale(0.015F)
                        .seeThrough()
                        .render();
            } else if (entity instanceof ZombieVillager) {
                ZombieVillager zombieVillager = MiscUtil.cast(SyncUtil.syncEntityDataFromIntegratedServer(entity));
                EntityInfoRenderer.rotationAround(TextRenderer.create(), entity.getEyePosition(partialTick), 0.6)
                        .text(ZombieVillagerConvertTimeInfo.getInfo(zombieVillager))
                        .bgColor((int) (Minecraft.getInstance().options.getBackgroundOpacity(0.25F) * 255.0F) << 24)
                        .fontScale(0.015F)
                        .seeThrough()
                        .render();
            }
        }

        this.queue.clear();
    }
}
