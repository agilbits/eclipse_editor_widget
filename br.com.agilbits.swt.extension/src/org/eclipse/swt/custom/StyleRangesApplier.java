package org.eclipse.swt.custom;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;

public class StyleRangesApplier {
    private static final String CAPITALIZE_STR = "CAPITALIZE"; //$NON-NLS-1$
    private static final Map<Font, Map<Integer, Font>> FONT_CACHE = new HashMap<Font, Map<Integer, Font>>();

    private StyledText styledText;

    public StyleRangesApplier(StyledText styledText) {
        this.styledText = styledText;
    }

    public void applyStyles(int lineOffset, String line, TextLayout textLayout) {
        StyledTextEvent event = styledText.getLineStyleData(lineOffset, line);

        if (event.styles != null) {
            applyCapitalize(textLayout, event.styles, lineOffset);

            for (StyleRange styleRange : event.styles) {
                TextStyle style = new TextStyle(styleRange);
                style.font = getStyledFont(getFont(styleRange), styleRange.fontStyle);

                int startInLayout = styleRange.start - lineOffset;
                int endInLayout = startInLayout + styleRange.length - 1;
                textLayout.setStyle(style, startInLayout, endInLayout);
            }
        }
    }

    private Font getFont(StyleRange styleRange) {
        return styleRange.font == null ? styledText.getFont() : styleRange.font;
    }

    private Font getStyledFont(Font regularFont, int style) {
        Map<Integer, Font> styleCache = getStyleCacheFor(regularFont);
        if (!styleCache.containsKey(style)) 
            styleCache.put(style, buildStyledFont(regularFont, style));
        return styleCache.get(style);
    }

    private Map<Integer, Font> getStyleCacheFor(Font regularFont) {
        if (!FONT_CACHE.containsKey(regularFont))
            FONT_CACHE.put(regularFont, new HashMap<Integer, Font>());
        return FONT_CACHE.get(regularFont);
    }

    private Font buildStyledFont(Font regularFont, int style) {
        Font styled = regularFont;
        switch (style) {
        case SWT.BOLD:
        case SWT.ITALIC:
        case SWT.BOLD | SWT.ITALIC:
            styled = new Font(regularFont.getDevice(), getFontData(regularFont, style));
        }

        return styled;
    }

    private FontData[] getFontData(Font regularFont, int style) {
        FontData[] fontDatas = regularFont.getFontData();
        for (int i = 0; i < fontDatas.length; i++)
            fontDatas[i].setStyle(style);
        return fontDatas;
    }

    private void applyCapitalize(TextLayout textLayout, StyleRange[] styles, int lineOffset) {
        String text = textLayout.getText();
        StringBuilder stringBuilder = new StringBuilder(text);
        for (StyleRange styleRange : styles) {
            if (CAPITALIZE_STR.equals(styleRange.data)) {
                int start = styleRange.start - lineOffset;
                int end = Math.min(text.length() - 1, start + styleRange.length - 1);

                if (0 <= start && start <= end) {
                    String upperCaseRange = text.substring(start, end + 1).toUpperCase();
                    stringBuilder.replace(start, end + 1, upperCaseRange);
                }
            }
        }
        textLayout.setText(stringBuilder.toString());
    }
}
