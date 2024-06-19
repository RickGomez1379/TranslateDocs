package com.example.translatedocs;

public class LanguageItem {
    private final String languageCode;
    private final int flagImage;

    public LanguageItem(String languageCode, int flagImage) {
        this.languageCode = languageCode;
        this.flagImage = flagImage;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public int getFlagImage() {
        return flagImage;
    }
}
