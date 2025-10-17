package com.example.justagram.Services;

public class Validation {
    private static final int MIN_CAPTION_LENGTH = 8;

    public static boolean checkCaptionLength(String caption) {
        return caption != null && caption.length() >= MIN_CAPTION_LENGTH;
    }
}
