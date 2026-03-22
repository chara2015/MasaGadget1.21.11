package com.plusls.MasaGadget;

/**
 * Central mod metadata. All version/name/author info lives here.
 * build.gradle reads constants from this file for the output jar name and fabric.mod.json.
 */
@SuppressWarnings("unused")
public final class ModInfo {
    private ModInfo() {}

    public static final String MOD_ID          = "masa_gadget_mod";
    public static final String MOD_NAME        = "MasaGadget";
    public static final String MOD_VERSION     = "v0.2";
    public static final String MOD_DESCRIPTION = "Added some features to the Masa collection of mods.";
    public static final String MOD_AUTHORS     = "plusls, Hendrix-Shen, chara201x, claude-opus";
    public static final String MOD_LICENSE     = "LGPL-3.0";
    public static final String MOD_HOMEPAGE    = "https://blog.plusls.com/";
    public static final String MOD_SOURCES     = "https://github.com/plusls/MasaGadget";
}
