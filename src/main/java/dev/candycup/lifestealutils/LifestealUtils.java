package dev.candycup.lifestealutils;

//? if >1.21.8
import com.mojang.blaze3d.platform.InputConstants;
import dev.candycup.lifestealutils.hud.HudDisplayLayer;
import dev.candycup.lifestealutils.hud.HudElementDefinition;
import dev.candycup.lifestealutils.hud.HudElementManager;
import dev.candycup.lifestealutils.features.timers.BasicTimerManager;
import dev.candycup.lifestealutils.ui.HudElementEditor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LifestealUtils implements ClientModInitializer {
   private static final Logger LOGGER = LoggerFactory.getLogger("lifesteal-utils");
   //? if >1.21.8
   private static KeyMapping.Category LIFESTEAL_UTIL_BINDS;
   private static KeyMapping openHudEditorKeyBinding;

   @Override
   public void onInitializeClient() {
      LOGGER.info("Lifesteal Utils initializing. I LOVE FABRIC !!!!!!");
      Config.load();

      HudElementManager.init();
        BasicTimerManager.configure(FeatureFlagController.getBasicTimers());
      for (HudElementDefinition definition : BasicTimerManager.hudDefinitions()) {
         HudElementManager.register(definition);
      }

      HudElementRegistry.attachElementAfter(
              VanillaHudElements.CHAT,
              HudDisplayLayer.LSU_HUD_LAYER_ID,
              HudDisplayLayer.lsuHudLayer()
      );

      HudElementRegistry.attachElementAfter(
              VanillaHudElements.CHAT,
              HudElementEditor.EDITOR_LAYER_ID,
              HudElementEditor.editorLayer()
      );

      //? if >1.21.8 {
      LIFESTEAL_UTIL_BINDS = KeyMapping.Category.register(
              Identifier.fromNamespaceAndPath("lifesteal-utils", "lifesteal_utils")
      );

      openHudEditorKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
              "key.lifesteal-utils.open_hud_editor",
              InputConstants.Type.KEYSYM,
              GLFW.GLFW_KEY_H,
              LIFESTEAL_UTIL_BINDS
      ));
      //?} else {
      /*openHudEditorKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
              "key.lifesteal-utils.open_hud_editor",
              GLFW.GLFW_KEY_H,
              "category.lifesteal-utils.lifesteal_utils"
      ));
      *///?}

      ClientTickEvents.END_CLIENT_TICK.register(client -> {
         if (client.player == null) return;
         if (openHudEditorKeyBinding.consumeClick()) {
            if (client.screen != null) return;
            client.setScreen(new HudElementEditor(
                    net.minecraft.network.chat.Component.literal("HUD Element Editor")
            ));
         }
                        BasicTimerManager.tick();
      });

      ClientCommandRegistrationCallback.EVENT.register((dispatcher, registry) -> {
         dispatcher.register(
                 ClientCommandManager.literal("lsu")
                         .executes(commandContext -> {
                             Minecraft client = Minecraft.getInstance();
                             client.execute(() -> client.setScreen(Config.getConfigScreen(client.screen)));
                             return 1;
                         })
                         .then(ClientCommandManager.literal("config")
                                 .executes(commandContext -> {
                                     Minecraft client = Minecraft.getInstance();
                                     client.execute(() -> client.setScreen(Config.getConfigScreen(client.screen)));
                                     return 1;
                                 }))
                         .then(ClientCommandManager.literal("edit-hud")
                                 .executes(commandContext -> {
                                     Minecraft client = Minecraft.getInstance();
                                     client.execute(() -> client.setScreen(new HudElementEditor(
                                             net.minecraft.network.chat.Component.literal("HUD Element Editor")
                                     )));
                                     return 1;
                                 }))
         );
      });
   }

}