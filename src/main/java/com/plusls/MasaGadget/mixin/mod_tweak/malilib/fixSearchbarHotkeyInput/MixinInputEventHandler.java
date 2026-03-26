package com.plusls.MasaGadget.mixin.mod_tweak.malilib.fixSearchbarHotkeyInput;

import com.plusls.MasaGadget.game.Configs;
import com.plusls.MasaGadget.util.ModId;
import fi.dy.masa.malilib.event.InputEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.hendrixshen.magiclib.api.dependency.annotation.Dependencies;
import top.hendrixshen.magiclib.api.dependency.annotation.Dependency;

@Dependencies(require = @Dependency(ModId.malilib))
@Mixin(value = InputEventHandler.class, remap = false)
public class MixinInputEventHandler {
    @Inject(method = "onKeyInput", at = @At("HEAD"), cancellable = true)
    private void masa_gadget$preventHotkeyWhenTyping(KeyEvent keyEvent, int keyCode, Minecraft minecraft,
                                                     CallbackInfoReturnable<Boolean> cir) {
        if (!Configs.fixSearchbarHotkeyInput.getBooleanValue()) {
            return;
        }

        Screen screen = minecraft.screen;
        if (screen == null) {
            return;
        }

        if (masa_gadget$isTextInputFocused(screen)) {
            // Let the focused text input consume key events first.
            // Prevent malilib/global hotkeys from being triggered while typing.
            cir.setReturnValue(false);
        }
    }

    private static boolean masa_gadget$isTextInputFocused(Screen screen) {
        GuiEventListener focused = screen.getFocused();
        if (focused instanceof EditBox editBox && editBox.isFocused()) {
            return true;
        }

        for (GuiEventListener child : screen.children()) {
            if (child instanceof EditBox editBox && editBox.isFocused()) {
                return true;
            }
        }

        return false;
    }
}
