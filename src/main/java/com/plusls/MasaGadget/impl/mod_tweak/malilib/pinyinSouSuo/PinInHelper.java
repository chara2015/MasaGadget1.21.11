package com.plusls.MasaGadget.impl.mod_tweak.malilib.pinyinSouSuo;

import com.google.common.collect.ImmutableMap;
import com.plusls.MasaGadget.game.Configs;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.towdium.pinin.Keyboard;
import me.towdium.pinin.PinIn;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PinInHelper {
    @Getter(lazy = true)
    private static final PinInHelper instance = new PinInHelper();
    private static final Map<PinYinSouSuoKeyboard, Keyboard> keyboardMapping = ImmutableMap.<PinYinSouSuoKeyboard, Keyboard>builder()
            .put(PinYinSouSuoKeyboard.QUANPIN, Keyboard.QUANPIN)
            .put(PinYinSouSuoKeyboard.DAQIAN, Keyboard.DAQIAN)
            .put(PinYinSouSuoKeyboard.XIAOHE, Keyboard.XIAOHE)
            .put(PinYinSouSuoKeyboard.ZIRANMA, Keyboard.ZIRANMA)
            .put(PinYinSouSuoKeyboard.SOUGOU, Keyboard.SOUGOU)
            .put(PinYinSouSuoKeyboard.GUOBIAO, Keyboard.GUOBIAO)
            .put(PinYinSouSuoKeyboard.MICROSOFT, Keyboard.MICROSOFT)
            .put(PinYinSouSuoKeyboard.PINYINPP, Keyboard.PINYINPP)
            .put(PinYinSouSuoKeyboard.ZIGUANG, Keyboard.ZIGUANG)
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
        // First try PinIn's native contains (works for single syllable pinyin)
        if (this.pinIn.contains(s1, s2)) {
            return true;
        }
        // Fallback: convert s1 to full pinyin and initials, then do String.contains
        // This handles multi-character pinyin like "caofangkuai" for "草方块"
        return containsByConvertedPinyin(s1, s2);
    }

    private boolean containsByConvertedPinyin(String text, String query) {
        if (query.isEmpty()) return true;
        // Build full pinyin and initials for the text
        StringBuilder full = new StringBuilder();
        StringBuilder initials = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= '\u4e00' && c <= '\u9fff') {
                me.towdium.pinin.elements.Char charData = this.pinIn.getChar(c);
                me.towdium.pinin.elements.Pinyin[] pinyins = charData != null ? charData.pinyins() : null;
                if (pinyins != null && pinyins.length > 0) {
                    String py = pinyins[0].toString();
                    if (py != null && !py.isEmpty()) {
                        // Apply fuzzy pinyin normalization to the pinyin string
                        py = normalizePinyin(py);
                        full.append(py);
                        initials.append(py.charAt(0));
                        continue;
                    }
                }
            }
            full.append(c);
            initials.append(c);
        }
        // Also normalize the query with the same fuzzy rules
        String normalizedQuery = normalizePinyin(query);
        String fullStr = full.toString();
        String initialsStr = initials.toString();
        return fullStr.contains(normalizedQuery) || initialsStr.contains(normalizedQuery);
    }

    /**
     * Apply fuzzy pinyin normalization based on current config flags.
     * Both the text's pinyin and the query are normalized the same way,
     * so fuzzy matching works correctly.
     */
    public String normalizePinyin(String py) {
        // Apply each fuzzy rule: both directions (e.g. zh->z and z->zh are handled
        // by normalizing both sides to the same form, here we normalize to the shorter form)
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
