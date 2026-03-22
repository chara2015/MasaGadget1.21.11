package com.plusls.MasaGadget.mixin.mod_tweak.malilib.pinyinSouSuo;

import com.plusls.MasaGadget.game.Configs;
import com.plusls.MasaGadget.impl.mod_tweak.malilib.pinyinSouSuo.PinInHelper;
import me.towdium.pinin.elements.Char;
import me.towdium.pinin.elements.Pinyin;
import net.minecraft.client.searchtree.SuffixArray;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.hendrixshen.magiclib.api.dependency.annotation.Dependencies;
import top.hendrixshen.magiclib.api.dependency.annotation.Dependency;

/**
 * When an entry is added to SuffixArray, also add its pinyin romanization.
 * This makes vanilla creative search, JEI, REI, EMI etc. all support pinyin.
 */
@Dependencies(conflict = @Dependency("jecharacters"))
@Mixin(value = SuffixArray.class, priority = 900)
public abstract class MixinSuffixArray<T> {
    @Shadow
    public abstract void add(T object, String string);

    @Unique
    private boolean masa_gadget$addingPinyin = false;

    @Inject(method = "add", at = @At("HEAD"))
    private void masa_gadget$addPinyinEntry(T object, String string, CallbackInfo ci) {
        if (masa_gadget$addingPinyin) {
            return;
        }
        if (!Configs.pinyinSouSuo.getBooleanValue()) {
            return;
        }

        // Check if string contains any Chinese characters
        boolean hasChinese = false;
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (c >= '\u4e00' && c <= '\u9fff') {
                hasChinese = true;
                break;
            }
        }
        if (!hasChinese) {
            return;
        }

        // Build pinyin initials (e.g. "铁剑" -> "tj") and full pinyin (e.g. "tiejian")
        StringBuilder initials = new StringBuilder();
        StringBuilder full = new StringBuilder();

        me.towdium.pinin.PinIn pinIn = PinInHelper.getInstance().getPinInInstance();

        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (c >= '\u4e00' && c <= '\u9fff') {
                Char charData = pinIn.getChar(c);
                Pinyin[] pinyins = charData != null ? charData.pinyins() : null;
                if (pinyins != null && pinyins.length > 0) {
                    String py = pinyins[0].toString();
                    if (py != null && !py.isEmpty()) {
                        initials.append(py.charAt(0));
                        full.append(py);
                        continue;
                    }
                }
            }
            initials.append(c);
            full.append(c);
        }

        masa_gadget$addingPinyin = true;
        try {
            String initialsStr = initials.toString();
            String fullStr = full.toString();
            if (!initialsStr.equals(string)) {
                this.add(object, initialsStr);
            }
            if (!fullStr.equals(string) && !fullStr.equals(initialsStr)) {
                this.add(object, fullStr);
            }
        } finally {
            masa_gadget$addingPinyin = false;
        }
    }
}
