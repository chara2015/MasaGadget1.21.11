package com.plusls.MasaGadget.mixin.mod_tweak.malilib.pinyinSouSuo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.plusls.MasaGadget.game.Configs;
import com.plusls.MasaGadget.impl.mod_tweak.malilib.pinyinSouSuo.PinInHelper;
import fi.dy.masa.malilib.gui.widgets.WidgetListBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import top.hendrixshen.magiclib.api.dependency.annotation.Dependencies;
import top.hendrixshen.magiclib.api.dependency.annotation.Dependency;

@Dependencies(conflict = @Dependency("jecharacters"))
@Mixin(value = WidgetListBase.class, remap = false)
public class MixinWidgetListBase {
    @WrapOperation(
            method = "matchesFilter(Ljava/lang/String;Ljava/lang/String;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/String;contains(Ljava/lang/CharSequence;)Z"
            )
    )
    private boolean patchMatchLogic(String instance, CharSequence s, Operation<Boolean> original) {
        if (Configs.pinyinSouSuo.getBooleanValue() && PinInHelper.getInstance().contains(instance, s.toString())) {
            return true;
        }

        return original.call(instance, s);
    }
}
