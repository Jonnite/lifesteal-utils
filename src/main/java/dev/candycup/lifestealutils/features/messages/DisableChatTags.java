package dev.candycup.lifestealutils.features.messages;

/**
 * Simple utility to remove the chat tag that appears after the rank bracket.
 * Example: "[LEGEND+] [No-Life] Player: ..." -> "[LEGEND+] Player: ..."
 */
public final class DisableChatTags {
   private DisableChatTags() {
   }

   /**
    * Remove the second bracketed token that appears before the username and colon in a
    * standard public chat message (e.g. "[RANK] [TAG] User: msg" -> "[RANK] User: msg").
    * Messages that don't follow this schema are returned untouched.
    */
   public static String removeTag(String message) {
      if (message == null || message.isEmpty()) return message;

      StringBuilder visible = new StringBuilder();
      java.util.List<BracketSpan> spans = new java.util.ArrayList<>();

      boolean inTag = false;
      int bracketStartVisible = -1;
      int bracketStartRaw = -1;

      for (int i = 0; i < message.length(); i++) {
         char c = message.charAt(i);
         if (inTag) {
            if (c == '>') inTag = false;
            continue;
         }

         if (c == '<') {
            inTag = true;
            continue;
         }

         int visibleIndex = visible.length();
         visible.append(c);

         if (c == '[') {
            bracketStartVisible = visibleIndex;
            bracketStartRaw = i;
         } else if (c == ']' && bracketStartVisible >= 0) {
            spans.add(new BracketSpan(bracketStartVisible, visibleIndex + 1, bracketStartRaw, i + 1));
            bracketStartVisible = -1;
            bracketStartRaw = -1;
         }
      }

      int colonIndex = visible.indexOf(":");
      if (colonIndex < 0) return message; // Not a user chat schema

      java.util.List<BracketSpan> beforeColon = new java.util.ArrayList<>();
      for (BracketSpan span : spans) {
         if (span.visibleStart < colonIndex) beforeColon.add(span);
      }

      // We only strip when there are at least two bracketed tokens before the username.
      if (beforeColon.size() < 2) return message;

      // Require a plausible username between the last bracket and the colon to avoid touching
      // system/status messages that may also contain brackets.
      BracketSpan lastBracket = beforeColon.get(beforeColon.size() - 1);
      String betweenLastBracketAndColon = visible.substring(lastBracket.visibleEnd, colonIndex).trim();
      if (betweenLastBracketAndColon.isEmpty()) return message;

      BracketSpan target = beforeColon.get(1); // the second bracketed segment before the username

      int start = target.rawStart;
      int end = target.rawEnd;

      // Include wrapping color tags that only paint the bracket characters, to avoid leaving
      // empty MiniMessage tags like <dark_gray></dark_gray>. cough cough lsn
      int maybeOpenTagStart = message.lastIndexOf('<', Math.max(0, start - 1));
      if (maybeOpenTagStart >= 0) {
         int maybeOpenTagEnd = message.indexOf('>', maybeOpenTagStart);
         if (maybeOpenTagEnd == start - 1) {
            start = maybeOpenTagStart;
         }
      }
      if (end < message.length() && message.charAt(end) == '<') {
         int maybeCloseTagEnd = message.indexOf('>', end);
         if (maybeCloseTagEnd != -1) {
            end = maybeCloseTagEnd + 1;
         }
      }

      // Absorb one adjacent space so we don't leave a double-space gap after removal.
      if (start > 0 && Character.isWhitespace(message.charAt(start - 1))) {
         start -= 1;
      } else if (end < message.length() && Character.isWhitespace(message.charAt(end))) {
         end += 1;
      }

      StringBuilder sb = new StringBuilder(message);
      sb.delete(start, end);

      String cleaned = sb.toString();
      // If two or more spaces remain immediately after a closing bracket, collapse them.
      cleaned = cleaned.replaceAll("\\]\\s{2,}", "] ");
      // Drop any empty formatting tags that might be left behind (e.g. <dark_gray></dark_gray>)
      cleaned = cleaned.replaceAll("<[^>/]+></[^>]+>", "");

      // Collapse any whitespace runs (including non-breaking spaces) left behind
      return cleaned.replaceAll("[\\s\\u00A0]+", " ").trim();
   }

   private static final class BracketSpan {
      final int visibleStart;
      final int visibleEnd;
      final int rawStart;
      final int rawEnd;

      BracketSpan(int visibleStart, int visibleEnd, int rawStart, int rawEnd) {
         this.visibleStart = visibleStart;
         this.visibleEnd = visibleEnd;
         this.rawStart = rawStart;
         this.rawEnd = rawEnd;
      }
   }
}
