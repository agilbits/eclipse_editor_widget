package br.com.agilbits.swt.extension.paginator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.custom.ExtendedStyledText;
import org.eclipse.swt.custom.IExtendedStyledTextContent;
import org.eclipse.swt.custom.PageInformation;
import org.eclipse.swt.custom.TextChangingEvent;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public class Paginator {
    public static final int EXTERNAL_MARGIN = ExtendedStyledText.EXTERNAL_PAGE_MARGIN;
    private IExtendedStyledTextContent content;
    private PageInformation pageInfo;
    private List<LogicalLine> logicalLines;
    private Font font;
    private Device device;
    private LocationHandler locationHandler;

    public Paginator(PageInformation pageInfo) {
        this.pageInfo = pageInfo;
        this.logicalLines = new ArrayList<LogicalLine>();
        this.locationHandler = new LocationHandler(this, pageInfo);
    }

    public int getPageCount() {
        return getPage(content.getCharCount()) + 1;
    }

    protected int getLineCount() {
        return logicalLines.size();
    }

    public int getHeight(int clientAreaHeight) {
        return locationHandler.getHeight(clientAreaHeight);
    }

    public int getWidth() {
        return pageInfo.getPageWidth() + (2 * EXTERNAL_MARGIN);
    }

    public void setContent(Device device, IExtendedStyledTextContent content) {
        dispose();
        this.device = device;
        this.content = content;
        for (int line = 0; line < content.getLineCount(); line++) {
            LogicalLine logicalLine = new LogicalLine(device, content, line);
            if (font != null)
                logicalLine.setFont(font);
            logicalLines.add(logicalLine);
        }
        updateLocations(true, 0);
    }

    public void setPageInformation(PageInformation pageInformation) {
        this.pageInfo = pageInformation;
        this.locationHandler = new LocationHandler(this, pageInfo);
        updateLocations(true, 0);
    }

    public void setFont(Font font) {
        this.font = font;
        for (LogicalLine logicalLine : logicalLines)
            logicalLine.setFont(font);
        updateLocations(true, 0);
    }

    private void updateLocations(boolean forceUpdate, int startingLine) {
        int lineIndex = startingLine - 1;
        while (lineIndex > 0)
            if (logicalLines.get(lineIndex).hasInvalidLocation())
                lineIndex--;
            else
                break;

        if (lineIndex > 0) {
            LogicalLine previous = logicalLines.get(lineIndex);
            int startingY = previous.getBaseLocation().y + previous.getFullHeight();
            int startingContinuousY = previous.getContinuousBaseLocation().y + previous.getHeight();
            calculateLocations(forceUpdate, lineIndex + 1, startingY, startingContinuousY);
        }
        else
            calculateLocations(forceUpdate, 0, 0, EXTERNAL_MARGIN);
    }

    private void calculateLocations(boolean forceUpdate, int startingLine, int startingY,
            int startingContinuousY) {
        int unusedUpperArea = getUnusedUpperArea();
        int y = startingY;
        int continuousY = startingContinuousY;
        int pageHeight = getPageHeight();
        int pageLimit = pageHeight - pageInfo.getBottomMargin();

        int breakSize = pageInfo.getBottomMargin() + unusedUpperArea;
        int availablePageHeight = pageInfo.getAvailableHeight();
        for (int lineIndex = startingLine; lineIndex < logicalLines.size(); lineIndex++) {
            LogicalLine logicalLine = logicalLines.get(lineIndex);

            int yInPage = y % pageHeight;
            if (yInPage == 0) {
                y += unusedUpperArea;
                yInPage = y % pageHeight;
            }

            if (logicalLine.hasInvalidLocation() || forceUpdate) {
                Point baseLocation = new Point(EXTERNAL_MARGIN, y);
                logicalLine.setContinuousBaseLocation(new Point(EXTERNAL_MARGIN, continuousY));
                if (forceUpdate || !baseLocation.equals(logicalLine.getBaseLocation())) {
                    boolean forceCompute = logicalLine.hasInvalidText();
                    logicalLine.setBaseLocation(baseLocation);

                    int minimumLines = getMinimumLines(lineIndex);
                    int blockSize = getBlockSizeFrom(lineIndex);

                    int availableHeight = pageLimit - yInPage;
                    logicalLine.computeChildren(forceUpdate || forceCompute, availableHeight,
                                                breakSize, availablePageHeight, minimumLines,
                                                blockSize);
                }
                else
                    logicalLine.setLocationValid();
            }

            y += logicalLine.getFullHeight();
            continuousY += logicalLine.getHeight();
        }
    }

    private int getMinimumLines(int lineIndex) {
        int minimumLines = 0;
        while (lineIndex < getLineCount() && !content.canBreak(lineIndex)) {
            int lineCount = logicalLines.get(lineIndex).getTextLayout().getLineCount();
            minimumLines += content.getParagraphSpacing(lineIndex) + lineCount;
            if (content.canBreakAfter(lineIndex))
                return minimumLines;

            lineIndex++;
        }

        if (lineIndex < getLineCount()) {
            int blockSize = getBlockSizeFrom(lineIndex);
            int minimumLinesToFit = Math.min(content.getMinimumLinesToFit(lineIndex), blockSize);
            minimumLines += content.getParagraphSpacing(lineIndex) + minimumLinesToFit;
        }

        return minimumLines;
    }

    private int getBlockSizeFrom(int lineIndex) {
        int size = logicalLines.get(lineIndex).getTextLayout().getLineCount();
        while (lineIndex < getLineCount() && content.shouldMergeWithNext(lineIndex++))
            size += logicalLines.get(lineIndex).getTextLayout().getLineCount();

        return size;
    }

    public int getUnusedUpperArea() {
        return EXTERNAL_MARGIN + pageInfo.getTopMargin();
    }

    public int getDecorationSize(int offset) {
        return content.getBreakDecorationSize(content.getLineAtOffset(offset));
    }

    public void dispose() {
        for (LogicalLine logicalLine : logicalLines)
            logicalLine.dispose();
        logicalLines.clear();
    }

    public LogicalLine getLogicalLine(int lineIndex) {
        return logicalLines.get(lineIndex);
    }

    public boolean hasPageBreakWithinLine(int line) {
        if (line >= logicalLines.size())
            return false;

        LogicalLine logicalLine = getLogicalLine(line);
        List<ExtendedTextLayout> children = logicalLine.getChildren();
        return children.size() > 1 || children.get(0).hasPageBreak();
    }

    public int getLinePixel(int lineIndex) {
        return locationHandler.getLinePixel(lineIndex);
    }

    public int getLineIndex(int y) {
        return locationHandler.getLineIndex(y);
    }

    public Rectangle getLineBounds(int lineIndex, int visualLineIndex) {
        return locationHandler.getLineBounds(lineIndex, visualLineIndex);
    }

    public Rectangle getBounds(int lineIndex, int relativeOffset) {
        return locationHandler.getBounds(lineIndex, relativeOffset);
    }

    public int getOffset(int lineIndex, int xInLogicalLine, int yInLogicalLine, int[] trailing) {
        LogicalLine logicalLine = getLogicalLine(lineIndex);
        if (logicalLine.getTextLayout().getText().length() == 0)
            return 0;
        else
            return locationHandler.getOffset(logicalLine, xInLogicalLine, yInLogicalLine, trailing);
    }

    public int getPageHeight() {
        return pageInfo.getPageHeight() + EXTERNAL_MARGIN;
    }

    public int getPage(int offset) {
        int lineIndex = content.getLineAtOffset(offset);
        int startingOffset = content.getOffsetAtLine(lineIndex);
        LogicalLine logicalLine = logicalLines.get(lineIndex);
        updateLocationsIfNeeded(logicalLine);

        ExtendedTextLayout child = logicalLine.getChild(offset - startingOffset);
        return child.getLocation().y / getPageHeight();
    }

    public int getOffsetForPage(int page) {
        int pageStartY = page * getPageHeight() + EXTERNAL_MARGIN;
        int lineIndex = getLineIndex(pageStartY);
        int linePixel = getLinePixel(lineIndex);
        return content.getOffsetAtLine(lineIndex)
                + getOffset(lineIndex, 0, pageStartY - linePixel, null);
    }

    protected void updateLocationsIfNeeded(LogicalLine logicalLine) {
        if (logicalLine.hasInvalidLocation())
            updateLocations(false, logicalLine.getLineIndex());
    }

    public Point getLocation(LogicalLine logicalLine) {
        return locationHandler.getLocation(logicalLine);
    }

    public Point getLocation(int lineIndex, int offsetInLine, boolean caretLeading) {
        LogicalLine logicalLine = getLogicalLine(lineIndex);
        return locationHandler.getLocation(logicalLine, offsetInLine, caretLeading);
    }

    public int getModelLocation(int offset) {
        int lineIndex = content.getLineAtOffset(offset);
        LogicalLine logicalLine = getLogicalLine(lineIndex);
        updateLocationsIfNeeded(logicalLine);

        int relativeOffset = offset - content.getOffsetAtLine(lineIndex);
        ExtendedTextLayout child = logicalLine.getChild(relativeOffset);

        int yInModelPage;
        if (child.hasPageBreak())
            yInModelPage = 0;
        else {
            int relativeY = logicalLine.getBaseLocation().y % getPageHeight();
            yInModelPage = relativeY - getUnusedUpperArea();
        }

        return getPage(offset) * pageInfo.getAvailableHeight() + yInModelPage;
    }

    public int getModelLength() {
        int lastChar = content.getCharCount();
        int lastLine = content.getLineCount() - 1;
        LogicalLine logicalLine = getLogicalLine(lastLine);
        return getModelLocation(lastChar) + logicalLine.getHeight();
    }

    public void layoutChanged() {
        for (int lineIndex = 0; lineIndex < logicalLines.size(); lineIndex++)
            logicalLines.get(lineIndex).updateLayout();
        updateLocations(true, 0);
    }

    public void textChanging(TextChangingEvent event) {
        int changedLine = content.getLineAtOffset(event.start);
        int firstLine = changedLine + 1;

        disposeRemovedLines(firstLine, event.replaceLineCount);
        createAddedLines(firstLine, event.newLineCount);

        logicalLines.get(changedLine).invalidateText();
        logicalLines.get(changedLine).invalidateLayout();
        logicalLines.get(changedLine).setBaseLocation(null);
        while(changedLine > 0 && content.shouldMergeWithNext(changedLine - 1)) {
            changedLine--;
            logicalLines.get(changedLine).setBaseLocation(null);
        }

        for (int invalidLine = changedLine; invalidLine < logicalLines.size(); invalidLine++)
            logicalLines.get(invalidLine).changeLineIndex(invalidLine);
    }

    private void disposeRemovedLines(int firstLine, int removedLineCount) {
        if (removedLineCount > 0)
            for (int removedLine = removedLineCount - 1; removedLine >= 0; removedLine--) {
                LogicalLine removedLogicalLine = logicalLines.remove(firstLine + removedLine);
                removedLogicalLine.dispose();
            }
    }

    private void createAddedLines(int firstLine, int newLineCount) {
        if (newLineCount > 0)
            for (int newLine = 0; newLine < newLineCount; newLine++) {
                LogicalLine newLogicalLine = new LogicalLine(device, content, newLine + firstLine);
                if (font != null)
                    newLogicalLine.setFont(font);
                logicalLines.add(newLine + firstLine, newLogicalLine);
            }
    }
}
