package dev.candycup.lifestealutils.ui;

import dev.candycup.lifestealutils.features.qol.PoiDirectionalIndicator;
import dev.candycup.lifestealutils.hud.HudElementManager;
import dev.candycup.lifestealutils.hud.HudPosition;
import dev.candycup.lifestealutils.interapi.MessagingUtils;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class HudElementEditor extends Screen {
   public static final Identifier EDITOR_LAYER_ID = Identifier.fromNamespaceAndPath("lifestealutils", "hud_editor");
   private static Identifier draggingId;
   private static float dragOffsetX;
   private static float dragOffsetY;
   private static boolean lastLeftDown;

   private static PoiDirectionalIndicator poiDirectionalIndicator;

   public HudElementEditor(Component component) {
      super(component);
   }

   /**
    * Sets the POI directional indicator for preview rendering in the editor.
    *
    * @param indicator the directional indicator to render
    */
   public static void setPoiDirectionalIndicator(PoiDirectionalIndicator indicator) {
      poiDirectionalIndicator = indicator;
   }

   @Override
   public void onClose() {
      this.minecraft.setScreen(null);
      draggingId = null;
      lastLeftDown = false;
   }

   @Override
   public void renderBlurredBackground(GuiGraphics guiGraphics) {
   }

   public static HudElement editorLayer() {
      return (drawContext, tickCounter) -> {
         Minecraft minecraft = Minecraft.getInstance();
         if (!(minecraft.screen instanceof HudElementEditor)) {
            return;
         }

         double mouseX = minecraft.mouseHandler.getScaledXPos(minecraft.getWindow());
         double mouseY = minecraft.mouseHandler.getScaledYPos(minecraft.getWindow());
         boolean leftDown = GLFW.glfwGetMouseButton(minecraft.getWindow().handle(), GLFW.GLFW_MOUSE_BUTTON_1) == GLFW.GLFW_PRESS;

         int guiWidth = minecraft.getWindow().getGuiScaledWidth();
         int guiHeight = minecraft.getWindow().getGuiScaledHeight();

         int screenTopAlphaBlack = 0x80000000;
         int screenBottomAlphaBlack = 0x40000000;
         drawContext.fillGradient(0, 0, guiWidth, guiHeight, screenTopAlphaBlack, screenBottomAlphaBlack);
         int blockTopAlphaBlack = 0xA0000000;
         int blockBottomAlphaBlack = 0x50000000;
         drawContext.fillGradient(0, 0, guiWidth, guiHeight, blockTopAlphaBlack, blockBottomAlphaBlack);

         Component title = MessagingUtils.miniMessage("<red><bold>Lifesteal Utils HUD Editor</bold></red>");
         Component subtitle = MessagingUtils.miniMessage("<gray><italic>Access more timers at /lsu config!</italic></gray>");
         int titleY = 8;
         drawContext.drawString(minecraft.font, title, 8, titleY, 0xFFFFFFFF, true);
         drawContext.drawString(minecraft.font, subtitle, 8, titleY + minecraft.font.lineHeight + 2, 0xFFFFFFFF, true);

         int gridSpacing = 32;
         int gridTopAlpha = 0x40;
         int gridBottomAlpha = 0x18;
         int gridTopColor = (gridTopAlpha << 24) | 0x00FFFFFF;
         int gridBottomColor = (gridBottomAlpha << 24) | 0x00FFFFFF;
         int centerX = guiWidth / 2;
         for (int x = centerX; x < guiWidth; x += gridSpacing) {
            drawContext.fillGradient(x, 0, x + 1, guiHeight, gridTopColor, gridBottomColor);
         }
         for (int x = centerX - gridSpacing; x >= 0; x -= gridSpacing) {
            drawContext.fillGradient(x, 0, x + 1, guiHeight, gridTopColor, gridBottomColor);
         }
         for (int y = 0; y < guiHeight; y += gridSpacing) {
            float t = guiHeight == 0 ? 0F : (float) y / (float) guiHeight;
            int alpha = Mth.floor(Mth.lerp(t, gridTopAlpha, gridBottomAlpha));
            int color = (alpha << 24) | 0x00FFFFFF;
            drawContext.fill(0, y, guiWidth, y + 1, color);
         }

         // render and handle the directional indicator as a draggable element
         if (poiDirectionalIndicator != null) {
            poiDirectionalIndicator.ensurePositionRegistered(guiWidth, guiHeight);
            poiDirectionalIndicator.render(drawContext, guiWidth, guiHeight);

            // get indicator position and size for hit testing
            Identifier indicatorId = poiDirectionalIndicator.getHudElementId();
            int indicatorSize = poiDirectionalIndicator.getTextureSize();
            HudPosition indicatorPos = HudElementManager.positionFor(indicatorId);
            int indicatorX = pixelCoordinate(indicatorPos.x(), guiWidth, indicatorSize);
            int indicatorY = pixelCoordinate(indicatorPos.y(), guiHeight, indicatorSize);

            boolean hoveringIndicator = mouseX >= indicatorX && mouseX <= indicatorX + indicatorSize
                    && mouseY >= indicatorY && mouseY <= indicatorY + indicatorSize;

            // start dragging indicator
            if (leftDown && !lastLeftDown && hoveringIndicator) {
               draggingId = indicatorId;
               dragOffsetX = (float) mouseX - indicatorX;
               dragOffsetY = (float) mouseY - indicatorY;
            }

            boolean isDraggingIndicator = draggingId != null && draggingId.equals(indicatorId);
            if (isDraggingIndicator) {
               HudElementManager.updatePositionFromPixels(
                       indicatorId,
                       (float) mouseX - dragOffsetX,
                       (float) mouseY - dragOffsetY,
                       guiWidth,
                       guiHeight,
                       indicatorSize,
                       indicatorSize
               );
            }

            // draw indicator bounding box when hovering or dragging
            if (hoveringIndicator || isDraggingIndicator) {
               int boxLeft = indicatorX - 2;
               int boxTop = indicatorY - 2;
               int boxRight = indicatorX + indicatorSize + 2;
               int boxBottom = indicatorY + indicatorSize + 2;
               int strokeColor = isDraggingIndicator ? 0xC066FF66 : 0xC0FFFFFF;
               drawContext.fill(boxLeft, boxTop, boxRight, boxTop + 1, strokeColor);
               drawContext.fill(boxLeft, boxBottom - 1, boxRight, boxBottom, strokeColor);
               drawContext.fill(boxLeft, boxTop, boxLeft + 1, boxBottom, strokeColor);
               drawContext.fill(boxRight - 1, boxTop, boxRight, boxBottom, strokeColor);
            }
         }

         List<HudElementManager.RenderedHudElement> elements = HudElementManager.renderables(minecraft.font, guiWidth, guiHeight);
         for (HudElementManager.RenderedHudElement element : elements) {
            HudElementManager.RenderedHudElement current = element;
            boolean hovering = mouseX >= current.x() && mouseX <= current.x() + current.textWidth()
                    && mouseY >= current.y() && mouseY <= current.y() + current.textHeight();
            if (leftDown && !lastLeftDown && hovering) {
               draggingId = current.definition().id();
               dragOffsetX = (float) mouseX - current.x();
               dragOffsetY = (float) mouseY - current.y();
            }

            boolean isDragging = draggingId != null && draggingId.equals(current.definition().id());
            if (isDragging) {
               HudElementManager.updatePositionFromPixels(
                       current.definition().id(),
                       (float) mouseX - dragOffsetX,
                       (float) mouseY - dragOffsetY,
                       guiWidth,
                       guiHeight,
                       current.textWidth(),
                       current.textHeight()
               );
               current = HudElementManager.renderable(current.definition(), minecraft.font, guiWidth, guiHeight);
            }

            int boxLeft = Mth.floor(current.x()) - 4;
            int boxTop = Mth.floor(current.y()) - 4;
            int boxRight = boxLeft + current.textWidth() + 8;
            int boxBottom = boxTop + current.textHeight() + 8;
            boolean visible = hovering || isDragging;
            if (visible) {
               int strokeColor = 0xC0FFFFFF;
               drawContext.fill(boxLeft, boxTop, boxRight, boxTop + 1, strokeColor);
               drawContext.fill(boxLeft, boxBottom - 1, boxRight, boxBottom, strokeColor);
               drawContext.fill(boxLeft, boxTop, boxLeft + 1, boxBottom, strokeColor);
               drawContext.fill(boxRight - 1, boxTop, boxRight, boxBottom, strokeColor);
            }

            int color = isDragging ? 0xFF66FF66 : (hovering ? 0xFFFFFFFF : 0xFFCCCCCC);
            drawContext.drawString(minecraft.font, current.component(), current.x(), current.y(), color, true);
         }

         if (draggingId != null && lastLeftDown && !leftDown) {
            HudElementManager.saveLayout();
            draggingId = null;
         }

         lastLeftDown = leftDown;
      };
   }

   /**
    * Calculates pixel coordinate from normalized position.
    */
   private static int pixelCoordinate(float normalized, int guiSize, int elementSize) {
      int available = Math.max(guiSize - elementSize, 0);
      float clamped = Mth.clamp(normalized, 0F, 1F);
      return Mth.floor(clamped * available);
   }
}
