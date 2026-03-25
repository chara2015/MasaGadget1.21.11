package com.plusls.MasaGadget.mixin.mod_tweak.malilib.pinyinSouSuo;

import com.plusls.MasaGadget.game.Configs;
import com.plusls.MasaGadget.impl.mod_tweak.malilib.pinyinSouSuo.PinInHelper;
import me.towdium.pinin.elements.Char;
import me.towdium.pinin.elements.Pinyin;
import net.minecraft.client.searchtree.SuffixArray;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.hendrixshen.magiclib.api.dependency.annotation.Dependencies;
import top.hendrixshen.magiclib.api.dependency.annotation.Dependency;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Dependencies(conflict = @Dependency("jecharacters"))
@Mixin(value = SuffixArray.class, priority = 900)
public abstract class MixinSuffixArray<T> {
    @Shadow
    public abstract void add(T object, String string);

    @Unique
    private static final int MAX_ALIAS_COUNT = 384;

    @Unique
    private boolean masa_gadget$addingPinyin = false;

    @Inject(method = "add", at = @At("HEAD"))
    private void masa_gadget$addPinyinEntry(T object, String string, CallbackInfo ci) {
        if (masa_gadget$addingPinyin || !Configs.pinyinSouSuo.getBooleanValue()) {
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

        me.towdium.pinin.PinIn pinIn = PinInHelper.getInstance().getPinInInstance();
        List<List<String>> optionsByChar = new ArrayList<>();

        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            List<String> options = new ArrayList<>();

            if (c >= '\u4e00' && c <= '\u9fff') {
                Char charData = pinIn.getChar(c);
                Pinyin[] pinyins = charData != null ? charData.pinyins() : null;
                if (pinyins != null && pinyins.length > 0) {
                    String py = pinyins[0].toString();
                    if (py != null && !py.isEmpty()) {
                        String full = PinInHelper.getInstance().normalizeBasic(py);
                        if (!full.isEmpty()) {
                            options.add(full);
                            if (full.length() > 2) {
                                String minusOne = full.substring(0, full.length() - 1);
                                if (!minusOne.equals(full)) {
                                    options.add(minusOne);
                                }
                            }
                            String initial = String.valueOf(full.charAt(0));
                            if (!initial.equals(full)) {
                                options.add(initial);
                            }
                        }
                    }
                }
            }

            if (options.isEmpty()) {
                String normalized = PinInHelper.getInstance().normalizeBasic(String.valueOf(c));
                if (normalized.isEmpty()) {
                    normalized = String.valueOf(c);
                }
                options.add(normalized);
            }

            optionsByChar.add(options);
        }

        Set<String> aliases = new LinkedHashSet<>();
        masa_gadget$buildAliases(optionsByChar, 0, new StringBuilder(), aliases);

        // Extra cheap tolerance: one "drop-u" variant for each alias
        List<String> snapshot = new ArrayList<>(aliases);
        for (String alias : snapshot) {
            if (aliases.size() >= MAX_ALIAS_COUNT) {
                break;
            }
            String noU = alias.replace("u", "");
            if (!noU.isEmpty()) {
                aliases.add(noU);
            }
        }

        masa_gadget$addingPinyin = true;
        try {
            for (String alias : aliases) {
                if (!alias.isEmpty() && !alias.equals(string)) {
                    this.add(object, alias);
                }
            }
        } finally {
            masa_gadget$addingPinyin = false;
        }
    }

    @Unique
    private static void masa_gadget$buildAliases(List<List<String>> optionsByChar, int idx, StringBuilder current, Set<String> out) {
        if (out.size() >= MAX_ALIAS_COUNT) {
            return;
        }
        if (idx >= optionsByChar.size()) {
            out.add(current.toString());
            return;
        }

        List<String> options = optionsByChar.get(idx);
        int oldLen = current.length();
        for (String option : options) {
            current.append(option);
            masa_gadget$buildAliases(optionsByChar, idx + 1, current, out);
            current.setLength(oldLen);
            if (out.size() >= MAX_ALIAS_COUNT) {
                return;
            }
        }
    }
}
