package com.plusls.MasaGadget.impl.mod_tweak.malilib.pinyinSouSuo;

import com.google.common.collect.ImmutableMap;
import com.plusls.MasaGadget.game.Configs;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.towdium.pinin.Keyboard;
import me.towdium.pinin.PinIn;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PinInHelper {
    @Getter(lazy = true)
    private static final PinInHelper instance = new PinInHelper();
    private static final Map<PinYinSouSuoKeyboard, Keyboard> keyboardMapping = ImmutableMap.<PinYinSouSuoKeyboard, Keyboard>builder()
            .put(PinYinSouSuoKeyboard.QUANPIN, Keyboard.QUANPIN)
            .build();

    private final PinIn pinIn = new PinIn().config().accelerate(true).commit();

    public PinIn getPinInInstance() {
        return this.pinIn;
    }

    public void commitConfig() {
        PinIn.Config config = this.pinIn.config();
        config.keyboard(PinInHelper.keyboardMapping.getOrDefault((PinYinSouSuoKeyboard) Configs.pinyinSouSuoKeyboard.getOptionListValue(), Keyboard.QUANPIN));
        config.fZh2Z(Configs.pinyinSouSuoFZh2Z.getBooleanValue());
        config.fSh2S(Configs.pinyinSouSuoFSh2S.getBooleanValue());
        config.fCh2C(Configs.pinyinSouSuoFCh2C.getBooleanValue());
        config.fAng2An(Configs.pinyinSouSuoFAng2An.getBooleanValue());
        config.fIng2In(Configs.pinyinSouSuoFIng2In.getBooleanValue());
        config.fEng2En(Configs.pinyinSouSuoFEng2En.getBooleanValue());
        config.fU2V(Configs.pinyinSouSuoFU2V.getBooleanValue());
        config.commit();
    }

    public boolean contains(String s1, String s2) {
        if (s2 == null || s2.isEmpty()) {
            return true;
        }
        String normalizedQuery = normalizePinyin(s2.toLowerCase());

        // Try PinIn native search first (for compatibility and speed)
        if (this.pinIn.contains(s1, normalizedQuery)) {
            return true;
        }

        return containsByConvertedPinyin(s1, normalizedQuery);
    }

    private boolean containsByConvertedPinyin(String text, String normalizedQuery) {
        if (normalizedQuery.isEmpty()) {
            return true;
        }
        StringBuilder full = new StringBuilder();
        StringBuilder initials = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            boolean appended = false;
            if (c >= '\u4e00' && c <= '\u9fff') {
                me.towdium.pinin.elements.Char charData = this.pinIn.getChar(c);
                me.towdium.pinin.elements.Pinyin[] pinyins = charData != null ? charData.pinyins() : null;
                if (pinyins != null && pinyins.length > 0) {
                    for (me.towdium.pinin.elements.Pinyin pinyin : pinyins) {
                        if (pinyin == null) {
                            continue;
                        }
                        String py = pinyin.toString();
                        if (py != null && !py.isEmpty()) {
                            String normalizedPy = normalizePinyin(py.toLowerCase());
                            full.append(normalizedPy);
                            initials.append(normalizedPy.charAt(0));
                            appended = true;
                            break;
                        }
                    }
                }
            }
            if (!appended) {
                char lower = Character.toLowerCase(c);
                full.append(lower);
                initials.append(lower);
            }
        }
        String fullStr = full.toString();
        String initialsStr = initials.toString();
        return fullStr.contains(normalizedQuery) || initialsStr.contains(normalizedQuery);
    }

    /**
     * Apply fuzzy pinyin normalization based on current config flags.
     * Both the text's pinyin and the query are normalized the same way,
     * so fuzzy matching works correctly.
     */
    public String normalizeBasic(String py) {
        py = py.toLowerCase(Locale.ROOT);
        py = py.replace('ü', 'v');
        py = Normalizer.normalize(py, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        py = py.replaceAll("[^a-z]", "");
        return py;
    }

    public String normalizePinyin(String py) {
        py = normalizeBasic(py);
        if (this.pinIn.fZh2Z()) {
            py = py.replace("zh", "z");
        }
        if (this.pinIn.fSh2S()) {
            py = py.replace("sh", "s");
        }
        if (this.pinIn.fCh2C()) {
            py = py.replace("ch", "c");
        }
        if (this.pinIn.fAng2An()) {
            py = py.replace("ang", "an");
        }
        if (this.pinIn.fIng2In()) {
            py = py.replace("ing", "in");
        }
        if (this.pinIn.fEng2En()) {
            py = py.replace("eng", "en");
        }
        if (this.pinIn.fU2V()) {
            py = py.replace("u", "v");
        }
        return py;
    }
}
