package com.gazlaws.codeboard.layout.builder;

import com.gazlaws.codeboard.layout.Box;
import com.gazlaws.codeboard.layout.Key;

import java.util.ArrayList;

public class KeyboardLayoutRowBuilder {

    private Box box; // the dimensions of a row
    private ArrayList<KeyInfo> keys = new ArrayList<>();
    private float gap = 0;

    public ArrayList<Key> build() throws KeyboardLayoutException {
        checkAndUpdateDefaults();
        if (keys.size() == 0){
            throw new KeyboardLayoutException("Row cannot be built without any keys");
        }
        float availableWidth = box.width - gap * (keys.size() - 1);
        float availableHeight = box.height;
        if (availableWidth <= 0){
            throw new KeyboardLayoutException("Not enough space to fit keys in row");
        }
        float totalRequestedSize = 0;
        for (KeyInfo info : keys){
            totalRequestedSize += info.size;
        }
        float cursorX = box.x;
        float cursorY = box.y;
        ArrayList<Key> result = new ArrayList<>();
        for (KeyInfo info : keys){
            float width = availableWidth/totalRequestedSize*info.size;
            float height = availableHeight;
            Box box = Box.create(cursorX, cursorY, width, height);
            cursorX += box.width + gap;
            Key key = buildKeyFromBlueprint(info, box);
            result.add(key);
        }
        return result;
    }

    public KeyboardLayoutRowBuilder addKey(KeyInfo key) {
        keys.add(key);
        return this;
    }

    /** Inserts a central split gap at the midpoint of this row's keys. The left half gets the
     *  extra key when the count is odd. No-op outside 2..maxKeys keys. */
    public void insertMidpointGap(float gap, int maxKeys) {
        int n = keys.size();
        if (n < 2 || n > maxKeys) {
            return;
        }
        KeyInfo spacer = new KeyInfo();
        spacer.label = "";
        spacer.code = 0;
        spacer.size = gap;
        spacer.isSpacer = true;
        keys.add((n + 1) / 2, spacer);
    }

    public KeyboardLayoutRowBuilder setBox(Box size){
        this.box = size;
        return this;
    }

    public KeyboardLayoutRowBuilder setGap(float size){
        this.gap = size;
        return this;
    }

    private void checkAndUpdateDefaults() {
        if (box == null){
            box = Box.create(0,0,0,0);
        }
    }

    private static Key buildKeyFromBlueprint(KeyInfo info, Box box) {
        Key key = new Key();
        key.box = box;
        key.info = info;
        return key;
    }
}
