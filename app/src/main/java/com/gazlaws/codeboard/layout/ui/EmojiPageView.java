package com.gazlaws.codeboard.layout.ui;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gazlaws.codeboard.CodeBoardIME;
import com.gazlaws.codeboard.KeyboardPreferences;
import com.gazlaws.codeboard.layout.EmojiData;
import com.gazlaws.codeboard.theme.UiTheme;

import java.util.List;

/**
 * The emoji page, opened by the {emoji} key: a row of category tabs, a scrollable grid of emojis
 * (GridView), and a bottom row with ABC (back to letters), space, and backspace. Tapping an emoji
 * types it and records it in the "recent" category (index 0, stored in preferences). The page is
 * sized like the normal keyboard (same height fraction of the screen) and uses the active theme.
 */
public class EmojiPageView extends LinearLayout {

    private static final int TAB_ROW_HEIGHT_DP = 42;
    private static final int BOTTOM_ROW_HEIGHT_DP = 48;
    private static final int GRID_CELL_HEIGHT_DP = 48;
    private static final int GRID_COLUMN_WIDTH_DP = 52;
    private static final int REPEAT_INITIAL_MS = 400; // same feel as KeyboardButtonView repeats
    private static final int REPEAT_INTERVAL_MS = 50;

    private final CodeBoardIME ime;
    private final UiTheme theme;
    private final KeyboardPreferences prefs;
    private final EmojiAdapter adapter = new EmojiAdapter();
    private final TextView[] tabViews = new TextView[EmojiData.CATEGORY_ICONS.length];
    private final Handler repeatHandler = new Handler(Looper.getMainLooper());
    private Runnable repeatRunnable;
    private int selectedCategory;
    private String[] currentEmojis = {};

    public EmojiPageView(Context context, CodeBoardIME ime, UiTheme theme, KeyboardPreferences prefs) {
        super(context);
        this.ime = ime;
        this.theme = theme;
        this.prefs = prefs;
        setOrientation(VERTICAL);
        setBackgroundColor(theme.backgroundColor);
        float density = getResources().getDisplayMetrics().density;

        // Category tabs (index 0 = recent).
        LinearLayout tabRow = new LinearLayout(context);
        tabRow.setOrientation(HORIZONTAL);
        for (int i = 0; i < EmojiData.CATEGORY_ICONS.length; i++) {
            final int index = i;
            TextView tab = new TextView(context);
            tab.setText(EmojiData.CATEGORY_ICONS[i]);
            tab.setGravity(Gravity.CENTER);
            tab.setTextSize(TypedValue.COMPLEX_UNIT_PX, theme.fontHeight * 0.8f);
            tab.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ime.keyFeedback();
                    selectCategory(index);
                }
            });
            tabViews[i] = tab;
            tabRow.addView(tab, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        }
        addView(tabRow, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, (int) (TAB_ROW_HEIGHT_DP * density)));

        // The scrollable emoji grid.
        GridView grid = new GridView(context);
        grid.setNumColumns(GridView.AUTO_FIT);
        grid.setColumnWidth((int) (GRID_COLUMN_WIDTH_DP * density));
        grid.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        grid.setVerticalSpacing((int) (2 * density));
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < currentEmojis.length) {
                    String emoji = currentEmojis[position];
                    ime.keyFeedback();
                    ime.commitEmoji(emoji);
                    prefs.addRecentEmoji(emoji);
                }
            }
        });
        addView(grid, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        // Bottom row: ABC (back to the letters page), space, backspace.
        LinearLayout bottomRow = new LinearLayout(context);
        bottomRow.setOrientation(HORIZONTAL);
        int fg = theme.foregroundPaint.getColor();

        TextView abc = makeBottomKey(context, "ABC", fg);
        abc.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ime.keyFeedback();
                ime.exitEmojiPage();
            }
        });
        bottomRow.addView(abc, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));

        TextView space = makeBottomKey(context, "␣", fg);
        space.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ime.keyFeedback();
                ime.onText(" ");
            }
        });
        bottomRow.addView(space, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 2f));

        TextView backspace = makeBottomKey(context, "⌫", fg);
        backspace.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        ime.keyFeedback();
                        ime.onKey(-5, null);
                        startBackspaceRepeat();
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        stopBackspaceRepeat();
                        return true;
                    default:
                        return false;
                }
            }
        });
        bottomRow.addView(backspace, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));

        addView(bottomRow, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, (int) (BOTTOM_ROW_HEIGHT_DP * density)));

        // Start on recents when there are any, otherwise on smileys.
        List<String> recent = prefs.getRecentEmojis();
        selectCategory(recent.isEmpty() ? 1 : 0);
    }

    private TextView makeBottomKey(Context context, String label, int fgColor) {
        TextView key = new TextView(context);
        key.setText(label);
        key.setTextColor(fgColor);
        key.setGravity(Gravity.CENTER);
        key.setTextSize(TypedValue.COMPLEX_UNIT_PX, theme.fontHeight);
        return key;
    }

    private void selectCategory(int index) {
        selectedCategory = index;
        if (index == 0) {
            List<String> recent = prefs.getRecentEmojis();
            currentEmojis = recent.toArray(new String[0]);
        } else {
            currentEmojis = EmojiData.CATEGORIES[index];
        }
        for (int i = 0; i < tabViews.length; i++) {
            boolean selected = i == index;
            tabViews[i].setAlpha(selected ? 1f : 0.45f);
            tabViews[i].setBackgroundColor(selected
                    ? theme.previewBodyPaint.getColor() : Color.TRANSPARENT);
        }
        adapter.notifyDataSetChanged();
    }

    private void startBackspaceRepeat() {
        stopBackspaceRepeat();
        repeatRunnable = new Runnable() {
            @Override
            public void run() {
                ime.onKey(-5, null);
                repeatHandler.postDelayed(this, REPEAT_INTERVAL_MS);
            }
        };
        repeatHandler.postDelayed(repeatRunnable, REPEAT_INITIAL_MS);
    }

    private void stopBackspaceRepeat() {
        if (repeatRunnable != null) {
            repeatHandler.removeCallbacks(repeatRunnable);
            repeatRunnable = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopBackspaceRepeat();
    }

    /** Same height as the normal keyboard: the screen height times the size setting. */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float fraction = metrics.heightPixels > metrics.widthPixels
                ? theme.portraitSize
                : theme.landscapeSize;
        int width = metrics.widthPixels;
        int height = (int) (metrics.heightPixels * fraction);
        super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    /** Grid cells: one TextView per emoji. */
    private class EmojiAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return currentEmojis.length;
        }

        @Override
        public Object getItem(int position) {
            return currentEmojis[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView cell;
            if (convertView instanceof TextView) {
                cell = (TextView) convertView;
            } else {
                cell = new TextView(parent.getContext());
                cell.setGravity(Gravity.CENTER);
                cell.setTextSize(TypedValue.COMPLEX_UNIT_PX, theme.fontHeight * 1.1f);
                float density = getResources().getDisplayMetrics().density;
                cell.setLayoutParams(new AbsListViewLayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, (int) (GRID_CELL_HEIGHT_DP * density)));
            }
            cell.setText(currentEmojis[position]);
            return cell;
        }
    }

    /** Alias to keep the adapter readable (GridView items need AbsListView.LayoutParams). */
    private static class AbsListViewLayoutParams extends android.widget.AbsListView.LayoutParams {
        AbsListViewLayoutParams(int w, int h) {
            super(w, h);
        }
    }
}
