package com.developers.wajbaty.Utils;

public class EmojiUtil {

    private final static int OFFSET = 127397;

    static public String countryCodeToEmoji(String code) {

        if (code == null || code.length() != 2) {
            return "";
        }

        if (code.equalsIgnoreCase("uk")) {
            code = "gb";
        }

        code = code.toUpperCase();

        StringBuilder emojiStr = new StringBuilder();

        for (int i = 0; i < code.length(); i++) {
            emojiStr.appendCodePoint(code.charAt(i) + OFFSET);
        }

        return emojiStr.toString();
    }

}
