package dev.candycup.lifestealutils.hud;

public record HudPosition(float x, float y) {
   public static HudPosition clamp(float x, float y) {
      return new HudPosition(clamp01(x), clamp01(y));
   }

   private static float clamp01(float value) {
      return Math.max(0F, Math.min(1F, value));
   }
}
