package dev.candycup.lifestealutils.features.messages;

/**
 * Fixes rank plus coloring by merging the separate colored plus into the rank's color.
 * Example:
 * <bold><#FF7200>HEROIC</#FF7200></bold><green>+</green>
 * becomes
 * <bold><#FF7200>HEROIC+</#FF7200></bold>
 */
public final class RemoveUniquePlusColor {
   private RemoveUniquePlusColor() {
   }

   public static String apply(String message) {
      return apply(message, true);
   }

   public static String apply(String message, boolean normalizeWhitespace) {
      if (message == null || message.isEmpty()) return message;
      String pattern = "(<bold>\\s*<([#A-Za-z0-9_]+)>)([^<>]+)(</[A-Za-z0-9_#]+>\\s*</bold>)(\\s*)<[^>]*>\\+(?:</[^>]*>)?";
      String result = message.replaceAll(pattern, "$1$3+$4");
      if (normalizeWhitespace) {
         result = result.replaceAll("\\s+", " ").trim();
      }
      return result;
   }
}
