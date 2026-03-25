package com.plusls.MasaGadget.impl.mod_tweak.malilib.pinyinSouSuo;

import com.google.common.collect.ImmutableMap;
import com.plusls.MasaGadget.game.Configs;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.towdium.pinin.Keyboard;
import me.towdium.pinin.PinIn;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PinInHelper {
    @Getter(lazy = true)
    private static final PinInHelper instance = new PinInHelper();
    private static final Map<PinYinSouSuoKeyboard, Keyboard> keyboardMapping = ImmutableMap.<PinYinSouSuoKeyboard, Keyboard>builder()
            .put(PinYinSouSuoKeyboard.QUANPIN, Keyboard.QUANPIN)
            .put(PinYinSouSuoKeyboard.FUZZY, Keyboard.QUANPIN)
            .put(PinYinSouSuoKeyboard.SUPER_FUZZY, Keyboard.QUANPIN)
            .build();

    private final PinIn pinIn = new PinIn().config().accelerate(true).commit();

    public PinIn getPinInInstance() {
        return this.pinIn;
    }

    public void commitConfig() {
        PinIn.Config config = this.pinIn.config();
        PinYinSouSuoKeyboard mode = (PinYinSouSuoKeyboard) Configs.pinyinSouSuoKeyboard.getOptionListValue();
        config.keyboard(PinInHelper.keyboardMapping.getOrDefault(mode, Keyboard.QUANPIN));

        boolean fuzzy = mode == PinYinSouSuoKeyboard.FUZZY || mode == PinYinSouSuoKeyboard.SUPER_FUZZY;
        config.fAng2An(fuzzy);
        config.fIng2In(fuzzy);
        config.fEng2En(fuzzy);
        config.fZh2Z(fuzzy);
        config.fSh2S(fuzzy);
        config.fCh2C(fuzzy);
        config.fU2V(fuzzy);
        config.commit();
        // Pre-warm normalizeBasic to avoid first-call JIT stutter
        warmUpNormalizer();
    }

    private static void warmUpNormalizer() {
        // Run a dummy normalization to force JIT compilation before user interaction
        String dummy = normalizeBasicStatic("\u4e0b\u754c"); // \u4e0b\u754c = \u4e0b\u754c (xia jie)
        if (dummy.isEmpty()) { /* prevent DCE */ }
    }

    public boolean contains(String s1, String s2) {
        if (s2 == null || s2.isEmpty()) {
            return true;
        }
        String normalizedQuery = normalizePinyin(s2.toLowerCase());

        if (this.pinIn.contains(s1, normalizedQuery)) {
            return true;
        }

        return containsByConvertedPinyin(s1, normalizedQuery);
    }

    private boolean containsByConvertedPinyin(String text, String normalizedQuery) {
        if (normalizedQuery.isEmpty()) {
            return true;
        }

        boolean superFuzzy = Configs.pinyinSouSuoKeyboard.getOptionListValue() == PinYinSouSuoKeyboard.SUPER_FUZZY;
        StringBuilder full = new StringBuilder();
        StringBuilder initials = new StringBuilder();
        List<String> syllables = superFuzzy ? new ArrayList<>() : null;

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
                            if (!normalizedPy.isEmpty()) {
                                full.append(normalizedPy);
                                initials.append(normalizedPy.charAt(0));
                                if (superFuzzy) {
                                    syllables.add(normalizedPy);
                                }
                                appended = true;
                            }
                            break;
                        }
                    }
                }
            }
            if (!appended) {
                String normalized = normalizePinyin(String.valueOf(Character.toLowerCase(c)));
                if (!normalized.isEmpty()) {
                    full.append(normalized);
                    initials.append(normalized.charAt(0));
                }
            }
        }

        String fullStr = full.toString();
        String initialsStr = initials.toString();
        if (fullStr.contains(normalizedQuery) || initialsStr.contains(normalizedQuery)) {
            return true;
        }

        return superFuzzy && containsBySyllablePrefixDp(syllables, normalizedQuery);
    }

    private static boolean containsBySyllablePrefixDp(List<String> syllables, String query) {
        if (syllables == null || syllables.isEmpty() || query.isEmpty()) {
            return false;
        }

        int n = syllables.size();
        int m = query.length();
        for (int start = 0; start < n; start++) {
            boolean[][] dp = new boolean[n + 1][m + 1];
            dp[start][0] = true;

            for (int i = start; i < n; i++) {
                for (int j = 0; j <= m; j++) {
                    if (!dp[i][j]) {
                        continue;
                    }
                    if (j == m) {
                        return true;
                    }

                    String token = syllables.get(i);
                    int max = Math.min(token.length(), m - j);
                    for (int len = 1; len <= max; len++) {
                        if (!query.regionMatches(j, token, 0, len)) {
                            break;
                        }
                        dp[i + 1][j + len] = true;
                    }

                    String dropU = token.replace("u", "");
                    if (!dropU.isEmpty() && !dropU.equals(token)) {
                        int maxDropU = Math.min(dropU.length(), m - j);
                        for (int len = 1; len <= maxDropU; len++) {
                            if (!query.regionMatches(j, dropU, 0, len)) {
                                break;
                            }
                            dp[i + 1][j + len] = true;
                        }
                    }
                }
            }

            if (dp[n][m]) {
                return true;
            }
        }

        return false;
    }

    // Static variant used for warm-up (avoids instance state dependency)
    private static String normalizeBasicStatic(String py) {
        py = py.toLowerCase(Locale.ROOT);
        py = py.replace('\u00fc', 'v');
        String nfd = Normalizer.normalize(py, Normalizer.Form.NFD);
        StringBuilder sb = new StringBuilder(nfd.length());
        for (int i = 0; i < nfd.length(); i++) {
            char c = nfd.charAt(i);
            if (Character.getType(c) != Character.NON_SPACING_MARK && c >= 'a' && c <= 'z') {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public String normalizeBasic(String py) {
        py = py.toLowerCase(Locale.ROOT);
        py = py.replace('\u00fc', 'v');
        // No-regex path: avoids pattern compile cost on first call
        String nfd = Normalizer.normalize(py, Normalizer.Form.NFD);
        StringBuilder sb = new StringBuilder(nfd.length());
        for (int i = 0; i < nfd.length(); i++) {
            char c = nfd.charAt(i);
            if (Character.getType(c) != Character.NON_SPACING_MARK && c >= 'a' && c <= 'z') {
                sb.append(c);
            }
        }
        return sb.toString();
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
