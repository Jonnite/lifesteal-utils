package dev.candycup.lifestealutils.mixin;

import dev.candycup.lifestealutils.Config;
import dev.candycup.lifestealutils.features.shortcuts.TitleScreenQuickJoin;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
   protected TitleScreenMixin(Component title) {
      super(title);
   }

   @Inject(method = "init", at = @At("TAIL"))
   public void init(CallbackInfo ci) {
      if (!Config.getQuickJoinButtonEnabled()) return;
      int l = this.height / 4 + 48;
      SpriteIconButton textIconButtonWidget2 =
              this.addRenderableWidget(
                      TitleScreenQuickJoin.getQuickJoinWidget(this)
              );
      textIconButtonWidget2.setPosition(this.width / 2 + 104, l);
   }
}
