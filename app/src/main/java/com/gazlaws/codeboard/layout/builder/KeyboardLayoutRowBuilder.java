package com.gazlaws.codeboard.layout.builder;

import com.gazlaws.codeboard.layout.Box;
import com.gazlaws.codeboard.layout.Key;

import java.util.ArrayList;
import java.util.List;

public class KeyboardLayoutRowBuilder {

    private Box box; // the dimensions of a row
    private ArrayList<KeyInfo> keys = new ArrayList<>();
    private float gap = 0;

    public ArrayList<Key> build() throws KeyboardLayoutException {
        checkAndUpdateDefaults();
        if (keys.size() == 0){
            throw new KeyboardLayoutException("Row cannot be built without any keys");
        }
        // A split row carries one central split marker (isSplitGap). It reserves a FIXED gap (a
        // fraction of the row width, so the gap is identical on every row no matter how many keys
        // the row has) and lays out the keys on each side independently to fill their half. Without
        // a marker the row is distributed across the full width as usual.
        int splitIndex = -1;
        for (int i = 0; i < keys.size(); i++) {
            if (keys.get(i).isSplitGap) {
                splitIndex = i;
                break;
            }
        }
        if (splitIndex >= 0) {
            return buildSplit(splitIndex);
        }
        return buildRange(keys, box);
    }

    /** Lays out a list of keys across a box, sharing the width in proportion to each key's size
     *  (the classic, non-split behaviour). */
    private ArrayList<Key> buildRange(List<KeyInfo> rowKeys, Box rowBox) throws KeyboardLayoutException {
        int count = rowKeys.size();
        float availableWidth = rowBox.width - gap * Math.max(0, count - 1);
        float availableHeight = rowBox.height;
        if (availableWidth <= 0){
            throw new KeyboardLayoutException("Not enough space to fit keys in row");
        }
        float totalRequestedSize = 0;
        for (KeyInfo info : rowKeys){
            totalRequestedSize += info.size;
        }
        float cursorX = rowBox.x;
        float cursorY = rowBox.y;
        ArrayList<Key> result = new ArrayList<>();
        for (KeyInfo info : rowKeys){
            float width = availableWidth/totalRequestedSize*info.size;
            float height = availableHeight;
            Box keyBox = Box.create(cursorX, cursorY, width, height);
            cursorX += keyBox.width + gap;
            result.add(buildKeyFromBlueprint(info, keyBox));
        }
        return result;
    }

    /** Reserves a fixed central gap (a fraction of the row width, carried on the split marker's
     *  size) and fills the left and right halves independently. Equal half widths plus the same gap
     *  fraction on every row are what keep the centre gap and the column alignment consistent, even
     *  when rows hold different numbers of keys. */
    private ArrayList<Key> buildSplit(int splitIndex) throws KeyboardLayoutException {
        KeyInfo marker = keys.get(splitIndex);
        float gapFraction = marker.size;
        if (gapFraction < 0) gapFraction = 0;
        if (gapFraction > 0.8f) gapFraction = 0.8f;
        float gapWidth = box.width * gapFraction;
        float halfWidth = (box.width - gapWidth) / 2f;
        ArrayList<Key> result = new ArrayList<>();
        List<KeyInfo> left = keys.subList(0, splitIndex);
        List<KeyInfo> right = keys.subList(splitIndex + 1, keys.size());
        if (!left.isEmpty()) {
            result.addAll(buildRange(left, Box.create(box.x, box.y, halfWidth, box.height)));
        }
        if (!right.isEmpty()) {
            result.addAll(buildRange(right,
                    Box.create(box.x + halfWidth + gapWidth, box.y, halfWidth, box.height)));
        }
        return result;
    }

    public KeyboardLayoutRowBuilder addKey(KeyInfo key) {
        keys.add(key);
        return this;
    }

    /** Inserts a central split marker at the midpoint of this row's keys (the left half gets the
     *  extra key when the count is odd). No-op outside 2..maxKeys keys. {@code gapFraction} is the
     *  fraction of the row width to reserve for the central gap (see {@link KeyInfo#isSplitGap}). */
    public void insertMidpointGap(float gapFraction, int maxKeys) {
        int n = keys.size();
        if (n < 2 || n > maxKeys) {
            return;
        }
        KeyInfo spacer = new KeyInfo();
        spacer.label = "";
        spacer.code = 0;
        spacer.size = gapFraction;
        spacer.isSpacer = true;
        spacer.isSplitGap = true;
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
