package com.plusls.MasaGadget.mixin.mod_tweak.malilib.pinyinSouSuo;

import com.plusls.MasaGadget.game.Configs;
import com.plusls.MasaGadget.impl.mod_tweak.malilib.pinyinSouSuo.PinInHelper;
import net.minecraft.client.searchtree.SuffixArray;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.hendrixshen.magiclib.api.dependency.annotation.Dependencies;
import top.hendrixshen.magiclib.api.dependency.annotation.Dependency;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Dependencies(conflict = @Dependency("jecharacters"))
@Mixin(value = SuffixArray.class, priority = 900)
public abstract class MixinSuffixArray<T> {
    @Unique
    private final Map<T, List<String>> masa_gadget$pinyinSources = new LinkedHashMap<>();
    @Unique
    private int masa_gadget$creativeItemCount = 0;

    @Inject(method = "add", at = @At("HEAD"))
    private void masa_gadget$cacheSearchSource(T object, String string, CallbackInfo ci) {
        if (!Configs.pinyinSouSuo.getBooleanValue() || string == null || string.isEmpty()) {
            return;
        }

        String source = string;
        if (source.indexOf('.') >= 0) {
            String translated = Component.translatable(source).getString();
            if (translated != null && !translated.equals(source)) {
                source = translated;
            }
        }

        boolean hasChinese = false;
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            if (c >= '\u4e00' && c <= '\u9fff') {
                hasChinese = true;
                break;
            }
        }

        if (!hasChinese) {
            return;
        }

        this.masa_gadget$pinyinSources
                .computeIfAbsent(object, k -> new ArrayList<>())
                .add(source);

        this.masa_gadget$creativeItemCount = this.masa_gadget$pinyinSources.size();

        // Preheat pinyin cache; helper clears only when creative item count increases.
        PinInHelper.getInstance().preheatSource(source, this.masa_gadget$creativeItemCount);
    }

    @Inject(method = "search", at = @At("RETURN"), cancellable = true)
    private void masa_gadget$appendPinyinMatches(String query, CallbackInfoReturnable<List<T>> cir) {
        if (!Configs.pinyinSouSuo.getBooleanValue() || query == null || query.isEmpty()) {
            return;
        }

        PinInHelper helper = PinInHelper.getInstance();
        Set<T> merged = new LinkedHashSet<>(cir.getReturnValue());

        for (Map.Entry<T, List<String>> entry : this.masa_gadget$pinyinSources.entrySet()) {
            if (merged.contains(entry.getKey())) {
                continue;
            }

            for (String source : entry.getValue()) {
                if (helper.contains(source, query)) {
                    merged.add(entry.getKey());
                    break;
                }
            }
        }

        cir.setReturnValue(new ArrayList<>(merged));
    }
}
