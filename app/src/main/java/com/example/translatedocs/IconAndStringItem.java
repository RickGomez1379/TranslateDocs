package com.example.translatedocs;

public class IconAndStringItem {
    private final String string;
    private final int icon;

    public IconAndStringItem(String _string, int _icon) {
        string = _string;
        this.icon = _icon;
    }

    public String getString() {
        return string;
    }

    public int getIcon() {
        return icon;
    }
}
