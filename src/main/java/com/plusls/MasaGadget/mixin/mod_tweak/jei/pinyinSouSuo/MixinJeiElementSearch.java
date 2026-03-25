package com.plusls.MasaGadget.mixin.mod_tweak.jei.pinyinSouSuo;

import com.plusls.MasaGadget.game.Configs;
import com.plusls.MasaGadget.impl.mod_tweak.malilib.pinyinSouSuo.PinInHelper;
import mezz.jei.gui.ingredients.IListElement;
import mezz.jei.gui.search.ElementPrefixParser;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.hendrixshen.magiclib.api.dependency.annotation.Dependencies;
import top.hendrixshen.magiclib.api.dependency.annotation.Dependency;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

@Dependencies(require = @Dependency("jei"))
@Mixin(targets = "mezz.jei.gui.search.ElementSearch", remap = false)
public class MixinJeiElementSearch {
    @Shadow
    private Map<Object, IListElement<?>> allElements;

    @Inject(method = "getSearchResults", at = @At("RETURN"), cancellable = true)
    private void masa_gadget$appendPinyinResults(
            ElementPrefixParser.TokenInfo tokenInfo,
            CallbackInfoReturnable<Set<IListElement<?>>> cir) {
        if (!Configs.pinyinSouSuo.getBooleanValue()) {
            return;
        }

        String token = tokenInfo.token();
        if (token == null || token.isEmpty()) {
            return;
        }

        PinInHelper helper = PinInHelper.getInstance();
        String normalizedQuery = helper.normalizePinyin(token.toLowerCase());
        if (normalizedQuery.isEmpty()) {
            return;
        }

        Set<IListElement<?>> existing = cir.getReturnValue();

        Set<IListElement<?>> extra = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
        for (Map.Entry<Object, IListElement<?>> entry : this.allElements.entrySet()) {
            IListElement<?> element = entry.getValue();
            if (existing.contains(element)) {
                continue;
            }

            mezz.jei.api.ingredients.ITypedIngredient<?> typed = element.getTypedIngredient();
            String displayName = masa_gadget$getDisplayName(typed);
            if (displayName != null && helper.contains(displayName, normalizedQuery)) {
                extra.add(element);
            }
        }

        if (!extra.isEmpty()) {
            Set<IListElement<?>> merged = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
            merged.addAll(existing);
            merged.addAll(extra);
            cir.setReturnValue(merged);
        }
    }

    @Unique
    private static <T> String masa_gadget$getDisplayName(mezz.jei.api.ingredients.ITypedIngredient<T> typed) {
        try {
            T ingredient = typed.getIngredient();
            if (ingredient instanceof net.minecraft.world.item.ItemStack stack) {
                return stack.getHoverName().getString();
            }
        } catch (Throwable ignored) {
            // no-op
        }
        return null;
    }
}
