package com.plusls.MasaGadget.mixin.mod_tweak.tweakeroo.backportI18nSupport;

import com.plusls.MasaGadget.util.ModId;
import fi.dy.masa.tweakeroo.config.ConfigBooleanClient;
import org.spongepowered.asm.mixin.Mixin;
import top.hendrixshen.magiclib.api.dependency.DependencyType;
import top.hendrixshen.magiclib.api.dependency.annotation.Dependencies;
import top.hendrixshen.magiclib.api.dependency.annotation.Dependency;
import top.hendrixshen.magiclib.api.platform.PlatformType;

@Dependencies(
        require = {
                @Dependency(value = ModId.tweakeroo, versionPredicates = "<0.11.1"),
                @Dependency(dependencyType = DependencyType.PLATFORM, platformType = PlatformType.FABRIC_LIKE)
        }
)
@Dependencies(
        require = {
                @Dependency(value = ModId.minecraft, versionPredicates = "<1.18-"),
                @Dependency(dependencyType = DependencyType.PLATFORM, platformType = PlatformType.FORGE_LIKE)
        }
)
@Mixin(value = ConfigBooleanClient.class, remap = false)
public class MixinConfigBooleanClient {
}
