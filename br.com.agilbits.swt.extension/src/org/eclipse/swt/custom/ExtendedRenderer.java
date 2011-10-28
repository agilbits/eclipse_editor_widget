package org.eclipse.swt.custom;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;

import br.com.agilbits.swt.extension.drawer.PageDrawer;
import br.com.agilbits.swt.extension.paginator.LogicalLine;
import br.com.agilbits.swt.extension.paginator.Paginator;

public class ExtendedRenderer extends StyledTextRenderer {
    private ExtendedStyledText extendedStyledText;
    private ScaleHelper helper;

    private Paginator paginator;
    private PageInformation pageInformation;

    public ExtendedRenderer(Device device, ExtendedStyledText styledText) {
        super(device, styledText);
        extendedStyledText = styledText;
        helper = extendedStyledText.getScaleHelper();
    }

    public void resetCaches() {
        reset();
        lineCount = content.getLineCount();
        lineWidth = new int[lineCount];
        lineHeight = new int[lineCount];
        reset(0, lineCount);
    }

    @Override
    void calculate(int startLine, int numberOfLines) {
        if (paginator == null)
            super.calculate(startLine, numberOfLines);
        else {
            int endLine = startLine + numberOfLines;
            if (startLine < 0 || endLine > lineCount)
                return;

            for (int lineIndex = startLine; lineIndex < endLine; lineIndex++) {
                if (lineWidth[lineIndex] == -1 || lineHeight[lineIndex] == -1) {
                    LogicalLine logicalLine = paginator.getLogicalLine(lineIndex);
                    lineHeight[lineIndex] = helper.scale(logicalLine.getHeight());
                    lineWidth[lineIndex] = helper.scale(logicalLine.getTextLayout().getWidth());
                }
            }
        }
    }

    @Override
    TextLayout getTextLayout(int lineIndex) {
        StyleRangesApplier applier = new StyleRangesApplier(styledText);
        if (paginator != null)
            return paginator.getLogicalLine(lineIndex).getTextLayout(applier);
        else
            return super.getTextLayout(lineIndex);
    }

    @Override
    void disposeTextLayout(TextLayout layout) {
        // TODO Is there something to do here?
    }

    public void drawArea(GC gc, Rectangle area) {
        PageDrawer drawer = new PageDrawer(gc, extendedStyledText, paginator,
                                           pageInformation.shouldDrawPageBreaks());
        drawer.draw(area);
    }

    @Override
    void setFont(Font font, int tabs) {
        super.setFont(font, tabs);
        if (paginator != null)
            paginator.setFont(font);
    }

    @Override
    void setContent(StyledTextContent content) {
        super.setContent(content);
        if (IExtendedStyledTextContent.class.isAssignableFrom(content.getClass())
                && paginator != null)
            paginator.setContent(styledText.getDisplay(), (IExtendedStyledTextContent) content);
    }

    public StyledTextContent getContent() {
        return content;
    }

    @Override
    void textChanging(TextChangingEvent event) {
        if (paginator != null)
            paginator.textChanging(event);
        super.textChanging(event);
    }

    @Override
    int getLineHeight() {
        return helper.scale(super.getLineHeight());
    }

    public void setPageInformation(PageInformation pageInformation) {
        this.pageInformation = pageInformation;
        if (paginator == null) {
            paginator = new Paginator(pageInformation);
            paginator.setFont(extendedStyledText.getFont());
            setContent(content);
        }
        else
            paginator.setPageInformation(pageInformation);
    }

    @Override
    public int getHeight() {
        if (paginator != null) {
            int unscaledHeight = helper.unscale(extendedStyledText.clientAreaHeight);
            return helper.scale(paginator.getHeight(unscaledHeight));
        }
        else
            return super.getHeight();
    }

    @Override
    public int getWidth() {
        if (paginator != null)
            return helper.scale(paginator.getWidth());
        else
            return super.getWidth();
    }

    /**
     * Given a line index, this method answers the vertical pixel where this line starts relative to
     * the whole widget, considering scale.
     * 
     * @param lineIndex
     *            The line index to query the line pixel
     * @return
     */
    public int getLinePixel(int lineIndex) {
        if (paginator != null)
            return helper.scale(paginator.getLinePixel(lineIndex));
        else
            return 0;
    }

    /**
     * Given a y relative to the client area, this method answers the line index corresponding to
     * that y. Relies on the styled text getTopPixel() method.
     * 
     * @param y
     *            The y relative to the client area.
     * @return
     */
    public int getLineIndex(int y) {
        if (paginator != null) {
            int totalY = extendedStyledText.getTopPixel() + y;
            return paginator.getLineIndex(helper.unscale(totalY));
        }
        else
            return 0;
    }

    public int getPage(int offset) {
        if (paginator != null)
            return paginator.getPage(offset);
        else
            return 0;
    }

    public int getPageCount() {
        if (paginator != null)
            return paginator.getPageCount();
        else
            return 1;
    }

    public int getModelLocation(int offset) {
        if (paginator != null)
            return paginator.getModelLocation(offset);
        else
            return 0;
    }

    public int getModelLength() {
        if (paginator != null)
            return paginator.getModelLength();
        else
            return 0;
    }

    public int getAvailableHeight() {
        if (pageInformation != null)
            return pageInformation.getAvailableHeight();
        else
            return 1;
    }

    public void layoutChanged() {
        if (paginator != null)
            paginator.layoutChanged();
    }

    public void setShowPageBreaks(boolean shouldDrawPageBreaks) {
        if (pageInformation != null)
            pageInformation.setDrawPageBreaks(shouldDrawPageBreaks);
        styledText.redraw();
    }

    public Point getLocation(int lineIndex, int offsetInLine, boolean caretLeading) {
        if (paginator != null) {
            Point location = paginator.getLocation(lineIndex, offsetInLine, caretLeading);
            return helper.scalePoint(location.x, location.y);
        }
        else
            return new Point(0, 0);
    }

    public int getOffset(int lineIndex, int x, int y, int[] trailing) {
        if (paginator != null)
            return paginator.getOffset(lineIndex, helper.unscale(x), helper.unscale(y), trailing);
        else
            return 0;
    }

    public Rectangle getBounds(int lineIndex, int relativeOffset) {
        if (paginator != null) {
            Rectangle bounds = paginator.getBounds(lineIndex, relativeOffset);
            return helper.scale(bounds);
        }
        else
            return new Rectangle(0, 0, 0, 0);
    }

    public Rectangle getLineBounds(int lineIndex, int visualLineIndex) {
        if (paginator != null) {
            Rectangle bounds = paginator.getLineBounds(lineIndex, visualLineIndex);
            return helper.scale(bounds);
        }
        else
            return new Rectangle(0, 0, 0, 0);
    }

    public int getFullLineHeight(int lineIndex) {
        if (paginator != null)
            return helper.scale(paginator.getLogicalLine(lineIndex).getFullHeight());
        else
            return 0;
    }
    
    @Override
    boolean hasLink(int offset) {
        // Overriden to optimize since we do not support links
        return false;
    }
}
