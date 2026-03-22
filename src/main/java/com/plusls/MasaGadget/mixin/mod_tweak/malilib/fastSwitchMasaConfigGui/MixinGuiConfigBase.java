package com.plusls.MasaGadget.mixin.mod_tweak.malilib.fastSwitchMasaConfigGui;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.plusls.MasaGadget.game.Configs;
import com.plusls.MasaGadget.api.gui.MasaGadgetDropdownList;
import com.plusls.MasaGadget.impl.mod_tweak.malilib.fastSwitchMasaConfigGui.FastMasaGuiSwitcher;
import com.plusls.MasaGadget.util.ModId;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.GuiListBase;
import fi.dy.masa.malilib.gui.widgets.WidgetBase;
import fi.dy.masa.malilib.gui.widgets.WidgetConfigOption;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptions;
import fi.dy.masa.malilib.interfaces.IStringValue;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import top.hendrixshen.magiclib.api.dependency.DependencyType;
import top.hendrixshen.magiclib.api.dependency.annotation.Dependencies;
import top.hendrixshen.magiclib.api.dependency.annotation.Dependency;
import top.hendrixshen.magiclib.api.platform.PlatformType;
import top.hendrixshen.magiclib.impl.malilib.config.gui.SelectorDropDownList;

@Dependencies(
        require = {
                @Dependency(ModId.malilib),
                @Dependency(ModId.mod_menu),
                @Dependency(dependencyType = DependencyType.PLATFORM, platformType = PlatformType.FABRIC_LIKE)
        }
)
@Dependencies(require = @Dependency(dependencyType = DependencyType.PLATFORM, platformType = PlatformType.FORGE_LIKE))
@Mixin(value = GuiConfigsBase.class, remap = false)
public abstract class MixinGuiConfigBase extends GuiListBase<GuiConfigsBase.ConfigOptionWrapper, WidgetConfigOption, WidgetListConfigOptions> implements MasaGadgetDropdownList {
    protected MixinGuiConfigBase(int listX, int listY) {
        super(listX, listY);
    }

    @Unique
    private SelectorDropDownList<IStringValue> masa_gadget$masaModGuiList;

    @Override
    public void masa_gadget_mod$addFastSwitcherWidget() {
        if (Configs.fastSwitchMasaConfigGui.getBooleanValue()) {
            this.masa_gadget$masaModGuiList = new SelectorDropDownList<>(
                    this.width - 111, 10, 100, 16, 200, 5,
                    FastMasaGuiSwitcher.getInstance().getModNameList());
            this.masa_gadget$masaModGuiList.setSelectedEntry(FastMasaGuiSwitcher.getInstance().getModName(this.getClass()));
            this.masa_gadget$masaModGuiList.setEntryChangeListener(entry ->
                    GuiBase.openGui(FastMasaGuiSwitcher.getInstance().getConfigScreenFactory(entry).create(this.getParent())));
            this.addWidget(this.masa_gadget$masaModGuiList);
        }
    }

    // Force blocking malilib's intrinsic dropdown list
    @Dynamic
    @WrapWithCondition(
            method = {"initGui", "buildConfigSwitcher"},
            at = @At(
                    value = "INVOKE",
                    target = "Lfi/dy/masa/malilib/gui/GuiConfigsBase;addWidget(Lfi/dy/masa/malilib/gui/widgets/WidgetBase;)Lfi/dy/masa/malilib/gui/widgets/WidgetBase;"
            ),
            require = 0
    )
    private boolean blockInherentDropdownList(GuiConfigsBase instance, WidgetBase widgetBase) {
        return !Configs.fastSwitchMasaConfigGui.getBooleanValue();
    }
}
