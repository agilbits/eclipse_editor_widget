package br.com.agilbits.swt.extension.drawer;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.DecorationRenderer;
import org.eclipse.swt.custom.ExtendedStyledText;
import org.eclipse.swt.custom.ScaleHelper;
import org.eclipse.swt.custom.StyleRangesApplier;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;

import br.com.agilbits.swt.extension.paginator.ExtendedTextLayout;
import br.com.agilbits.swt.extension.paginator.LogicalLine;
import br.com.agilbits.swt.extension.paginator.Paginator;

public class PageDrawer {
    private GC gc;
    private ExtendedStyledText styledText;
    private ScaleHelper helper;
    private Paginator paginator;
    private StyleRangesApplier applier;
    private boolean shouldDrawPageBreaks;

    public PageDrawer(GC gc, ExtendedStyledText extendedStyledText, Paginator paginator,
            boolean shouldDrawPageBreaks) {
        this.gc = gc;
        this.styledText = extendedStyledText;
        this.helper = styledText.getScaleHelper();
        this.paginator = paginator;
        this.shouldDrawPageBreaks = shouldDrawPageBreaks;
        this.applier = new StyleRangesApplier(styledText);
    }

    public void draw(Rectangle area) {
        drawPageBackground(area);

        int lineCount = styledText.getLineCount();
        int firstNotDrawnLine = drawAreaWithText(area, lineCount);
        if (firstNotDrawnLine == lineCount) {
            if (shouldDrawPageBreaks)
                drawLastFooter(styledText.getCharCount());

            drawLastExternalMargin(area);
        }
    }

    private void drawLastFooter(int charCount) {
        DecorationRenderer renderer = styledText.getPageDecorationRenderer();
        renderer.drawFooter(gc, styledText, charCount, getRelativeEndOfPageY(), null);
    }

    private void drawLastExternalMargin(Rectangle area) {
        Color backgroundColor = styledText.getExternalBackgroundColor();
        gc.setBackground(backgroundColor);

        int relativeEndOfPageY = getRelativeEndOfPageY();
        int height = area.y + area.height - relativeEndOfPageY;
        if (height > 0)
            gc.fillRectangle(area.x, relativeEndOfPageY, area.width, height);
    }

    private int getRelativeEndOfPageY() {
        int unscaledHeight = helper.unscale(styledText.getClientArea().height);
        return paginator.getHeight(unscaledHeight) - Paginator.EXTERNAL_MARGIN;
    }

    private void drawPageBackground(Rectangle area) {
        gc.setBackground(styledText.getBackground());
        gc.fillRectangle(area.x, area.y - 1, area.width, area.height + 2);
    }

    private int drawAreaWithText(Rectangle area, int lineCount) {
        int startLine = Math.max(paginator.getLineIndex(area.y) - 1, 0);
        int lineY = paginator.getLinePixel(startLine);
        int endY = area.y + area.height;

        int line;
        for (line = startLine; lineY < endY && line < lineCount; line++)
            lineY = drawLogicalLine(area, line);

        if (line < lineCount)
            drawLogicalLine(area, line);

        return line;
    }

    private int drawLogicalLine(Rectangle area, int lineIndex) {
        LogicalLine logical = paginator.getLogicalLine(lineIndex);
        int startingOffset = styledText.getOffsetAtLine(lineIndex);

        if (shouldDrawPageBreaks) {
            List<ExtendedTextLayout> children = logical.getChildren();
            int lastY = 0;
            for (ExtendedTextLayout child : children) {
                drawLayout(startingOffset, child);
                int childLocation = child.getLocation().y;
                if (child.hasPageBreak())
                    drawCompletePageBreak(area, childLocation, startingOffset + child.getOffset());
                lastY = childLocation + child.getHeight();
            }
            return lastY;
        }
        else {
            drawLayout(lineIndex, logical);
            if (lineIndex == 0)
                drawPageBreak(area, 0);

            Point layoutLocation = paginator.getLocation(logical);
            return layoutLocation.y + logical.getHeight();
        }
    }

    private void drawCompletePageBreak(Rectangle area, int firstLineY, int offset) {
        int decorationSize = paginator.getDecorationSize(offset)
                * helper.unscale(styledText.getLineHeight());
        int y = firstLineY - paginator.getUnusedUpperArea() - decorationSize;
        int breakHeight = Paginator.EXTERNAL_MARGIN;

        DecorationRenderer renderer = styledText.getPageDecorationRenderer();
        if (offset > 0)
            renderer.drawFooter(gc, styledText, offset - 1, y, null);

        drawPageBreak(area, y);

        renderer.drawHeader(gc, styledText, offset, y + breakHeight, null);
    }

    private void drawPageBreak(Rectangle area, int y) {
        gc.setBackground(styledText.getExternalBackgroundColor());
        gc.fillRectangle(area.x, y, area.width, Paginator.EXTERNAL_MARGIN);
    }

    private void drawLayout(int lineIndex, LogicalLine logical) {
        int offset = styledText.getOffsetAtLine(lineIndex);
        TextLayout textLayout = logical.getTextLayout(applier);
        Point location = paginator.getLocation(logical);
        Point selection = getSelectionInLayout(lineIndex);
        drawTextLayout(offset, textLayout, location, selection);
    }

    private void drawLayout(int offset, ExtendedTextLayout layout) {
        int lineOffset = offset + layout.getOffset();
        int lineLength = layout.getText().length();
        TextLayout textLayout = layout.getTextLayout(applier, offset);
        Point selection = getSelectionRelativeTo(lineOffset, lineLength);
        drawTextLayout(lineOffset, textLayout, layout.getLocation(), selection);
    }

    private void drawTextLayout(int offset, TextLayout textLayout, Point location, Point selection) {
        Color selectionForeground = styledText.getSelectionForeground();
        Color selectionBackground = styledText.getSelectionBackground();
        textLayout.draw(gc, location.x, location.y, selection.x, selection.y, selectionForeground,
                        selectionBackground, SWT.DELIMITER_SELECTION);

        DecorationRenderer renderer = styledText.getPageDecorationRenderer();
        renderer.decorateLine(gc, offset, location, textLayout, null);
    }

    private Point getSelectionInLayout(int line) {
        StyledTextContent content = styledText.getContent();
        int lineOffset = content.getOffsetAtLine(line);
        int lineLength = content.getLine(line).length();
        return getSelectionRelativeTo(lineOffset, lineLength);
    }

    private Point getSelectionRelativeTo(int lineOffset, int lineLength) {
        Point selection = styledText.getSelection();
        int start = Math.max(0, selection.x - lineOffset);
        int end = Math.min(lineLength, selection.y - lineOffset);
        return new Point(start, end - 1);
    }
}
