package dev.candycup.lifestealutils.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.candycup.lifestealutils.Config;
import dev.candycup.lifestealutils.LifestealServerDetector;
import dev.candycup.lifestealutils.features.alliances.Alliances;
import net.kyori.adventure.platform.modcommon.MinecraftClientAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public class PlayerEntityMixin {
   @ModifyReturnValue(method = "getDisplayName", at = @At("RETURN"))
   public Component prependTier(Component original) {
      if (!Config.getEnableAlliances()) return original;
      if (!LifestealServerDetector.isOnLifestealServer()) return original;
      if (original == null) return null;

      String serialized = MiniMessage.miniMessage().serialize(MinecraftClientAudiences.of().asAdventure(original));
      String lastWord = Alliances.getLastVisibleWord(serialized);
      if (lastWord == null || lastWord.isBlank()) return original;
      if (!Alliances.isAlliedName(lastWord)) return original;

      return Alliances.colorizeNameTag(original);
   }

}
