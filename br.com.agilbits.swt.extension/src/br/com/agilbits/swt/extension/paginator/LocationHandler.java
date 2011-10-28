package br.com.agilbits.swt.extension.paginator;

import org.eclipse.swt.custom.PageInformation;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;

public class LocationHandler {
    private static final int CARET_BEFORE = 0;
    private static final int CARET_AFTER = 1;

    private Paginator paginator;
    private PageInformation pageInfo;

    public LocationHandler(Paginator paginator, PageInformation pageInfo) {
        this.paginator = paginator;
        this.pageInfo = pageInfo;
    }

    public int getHeight(int clientAreaHeight) {
        if (pageInfo.shouldDrawPageBreaks())
            return (paginator.getPageCount() * paginator.getPageHeight())
                    + Paginator.EXTERNAL_MARGIN;
        else
            return getLayoutEnd(paginator.getLineCount() - 1) + Paginator.EXTERNAL_MARGIN
                    + clientAreaHeight / 2;
    }

    public int getLinePixel(int lineIndex) {
        if (lineIndex == paginator.getLineCount())
            return getLayoutEnd(lineIndex - 1);

        LogicalLine logicalLine = paginator.getLogicalLine(lineIndex);
        return getLocation(logicalLine).y;
    }

    protected Point getLocation(LogicalLine logicalLine) {
        paginator.updateLocationsIfNeeded(logicalLine);
        if (pageInfo.shouldDrawPageBreaks())
            return logicalLine.getLocation();
        else
            return logicalLine.getContinuousLocation();
    }

    private int getLayoutEnd(int lineIndex) {
        LogicalLine logicalLine = paginator.getLogicalLine(lineIndex);
        paginator.updateLocationsIfNeeded(logicalLine);
        if (pageInfo.shouldDrawPageBreaks())
            return logicalLine.getBaseLocation().y + logicalLine.getFullHeight();
        else
            return logicalLine.getContinuousBaseLocation().y + logicalLine.getHeight();
    }

    public int getLineIndex(int y) {
        int lineIndex = binarySearch(y, 0, paginator.getLineCount() - 1);

        if (lineIndex > 0 && isLastOfPage(y, lineIndex - 1))
            return lineIndex - 1;
        else
            return lineIndex;
    }

    private int binarySearch(int y, int start, int end) {
        if (start == end)
            return start;

        int middle = (start + end) / 2;

        if (isBeforeLineIndexEnd(y, middle))
            return binarySearch(y, start, middle);
        else
            return binarySearch(y, middle + 1, end);
    }

    private boolean isBeforeLineIndexEnd(int y, int lineIndex) {
        return y <= getLayoutEnd(lineIndex);
    }

    private boolean isLastOfPage(int y, int lineIndex) {
        return pageInfo.shouldDrawPageBreaks() && isAtBottomOfPage(y, lineIndex);
    }

    private boolean isAtBottomOfPage(int y, int lineIndex) {
        if (lineIndex + 1 >= paginator.getLineCount())
            return false;

        ExtendedTextLayout child = paginator.getLogicalLine(lineIndex + 1).getChildren().get(0);
        int bottomOfPage = child.getLocation().y - paginator.getUnusedUpperArea();
        return child.hasPageBreak() && y <= bottomOfPage;
    }

    public int getOffset(LogicalLine logicalLine, int xInLogicalLine, int yInLogicalLine,
            int[] trailing) {
        if (pageInfo.shouldDrawPageBreaks())
            return getPagesOffset(logicalLine, xInLogicalLine, yInLogicalLine, trailing);
        else
            return getContinuousOffset(logicalLine, xInLogicalLine, yInLogicalLine, trailing);
    }

    private int getPagesOffset(LogicalLine logicalLine, int xInLogicalLine, int yInLogicalLine,
            int[] trailing) {
        ExtendedTextLayout child = logicalLine.getChild(xInLogicalLine, yInLogicalLine);
        int relativeY = yInLogicalLine - (child.getLocation().y - logicalLine.getLocation().y);

        int relativeOffset = 0;
        boolean yIsInBottomMargin = relativeY < -paginator.getUnusedUpperArea();
        boolean childIsFirstContent = logicalLine.getLineIndex() == 0 && child.getOffset() == 0;
        if (yIsInBottomMargin && !childIsFirstContent) {
            relativeOffset = -1;
            if (trailing != null)
                trailing[0] = CARET_AFTER;
        }
        else if (relativeY > child.getHeight()) {
            relativeOffset = child.getText().length() - 1;
            if (trailing != null)
                trailing[0] = CARET_AFTER;
        }
        else if (relativeY >= 0)
            relativeOffset = child.getLayout().getOffset(xInLogicalLine, relativeY, trailing);
        else if (trailing != null)
            trailing[0] = CARET_BEFORE;

        return child.getOffset() + relativeOffset;
    }

    private int getContinuousOffset(LogicalLine logicalLine, int xInLogicalLine,
            int yInLogicalLine, int[] trailing) {
        TextLayout textLayout = logicalLine.getTextLayout();
        if (yInLogicalLine > logicalLine.getBoundsHeight()) {
            if (trailing != null)
                trailing[0] = CARET_AFTER;
            return textLayout.getText().length() - 1;
        }
        else if (yInLogicalLine < 0) {
            if (trailing != null)
                trailing[0] = CARET_BEFORE;
            return 0;
        }
        else
            return textLayout.getOffset(xInLogicalLine, yInLogicalLine, trailing);
    }

    public Point getLocation(LogicalLine logicalLine, int offsetInLine, boolean caretLeading) {
        if (offsetInLine == 0)
            caretLeading = true;
        else if (!caretLeading)
            offsetInLine--;
        else if (offsetInLine == logicalLine.getTextLayout().getText().length()) {
            offsetInLine--;
            caretLeading = false;
        }

        return computeLocation(logicalLine, offsetInLine, caretLeading);
    }

    private Point computeLocation(LogicalLine logicalLine, int offsetInLine, boolean caretLeading) {
        Point locationInLayout;
        Point location;
        if (pageInfo.shouldDrawPageBreaks()) {
            ExtendedTextLayout child = logicalLine.getChild(offsetInLine);
            int relativeOffset = offsetInLine - child.getOffset();

            locationInLayout = child.getLayout().getLocation(relativeOffset, !caretLeading);
            location = child.getLocation();
        }
        else {
            locationInLayout = logicalLine.getTextLayout().getLocation(offsetInLine, !caretLeading);
            location = getLocation(logicalLine);
        }
        return new Point(location.x + locationInLayout.x, location.y + locationInLayout.y);
    }

    public Rectangle getLineBounds(int lineIndex, int visualLineIndex) {
        LogicalLine logicalLine = paginator.getLogicalLine(lineIndex);

        if (pageInfo.shouldDrawPageBreaks())
            return getChildrenLineBounds(logicalLine, visualLineIndex);
        else
            return logicalLine.getTextLayout().getLineBounds(visualLineIndex);
    }

    private Rectangle getChildrenLineBounds(LogicalLine logicalLine, int visualLineIndex) {
        int lineCount = 0;
        for (ExtendedTextLayout child : logicalLine.getChildren()) {
            TextLayout layout = child.getLayout();
            if (layout.getLineCount() + lineCount > visualLineIndex) {
                Rectangle bounds = layout.getLineBounds(visualLineIndex - lineCount);
                bounds.y += child.getLocation().y - logicalLine.getLocation().y;
                return bounds;
            }
            lineCount += layout.getLineCount();
        }

        return null;
    }

    public Rectangle getBounds(int lineIndex, int relativeOffset) {
        LogicalLine logicalLine = paginator.getLogicalLine(lineIndex);
        if (pageInfo.shouldDrawPageBreaks()) {
            ExtendedTextLayout child = logicalLine.getChild(relativeOffset);
            int startInChild = relativeOffset - child.getOffset();
            Rectangle bounds = child.getLayout().getBounds(startInChild, startInChild);
            bounds.y += child.getLocation().y - logicalLine.getLocation().y;
            return bounds;
        }
        else {
            TextLayout textLayout = logicalLine.getTextLayout();
            return textLayout.getBounds(relativeOffset, relativeOffset);
        }
    }
}
