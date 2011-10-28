/*******************************************************************************
 * Copyright (c) 2007, 2008 OnPositive Technologies (http://www.onpositive.com/) and others. All
 * rights reserved. This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html Contributors: OnPositive Technologies
 * (http://www.onpositive.com/) - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.custom;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.widgets.Caret;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ScrollBar;

import br.com.agilbits.swt.extension.zoom.FitToWidthZoom;
import br.com.agilbits.swt.extension.zoom.ZoomOption;

public class ExtendedStyledText extends StyledText {
    private static final int TOP_INDEX_LINESPACES = 2;
    private double MINIMUM_ZOOM = 0.5;
    static final int GAP_LINES = 3;
    public static final int EXTERNAL_PAGE_MARGIN = 10;

    private PageInformation pageInformation;
    private ZoomOption currentZoom;
    private ScaleHelper helper;
    private int averageCharHeight;

    private DecorationRenderer pageInformationRenderer;
    private Color externalBackground;
    private boolean ignore;

    public ExtendedStyledText(Composite parent, int style) {
        super(parent, (style | SWT.DOUBLE_BUFFERED | SWT.H_SCROLL) & ~SWT.WRAP);
        currentZoom = new FitToWidthZoom();
        helper = new ScaleHelper(this);
        initializeRenderer();
        initializeWrap();
        setMargins(0, 0, 0, 0);
        ignore = false;
    }

    private void initializeWrap() {
        checkWidget();
        wordWrap = true;
        setVariableLineHeight();
        resetCache(0, content.getLineCount());
        horizontalScrollOffset = 0;

        setScrollBars(true);
        setCaretLocation();
        super.redraw();
    }

    /**
     * We don't want to do anything because we always have wordWrap.
     */
    @Override
    public void setWordWrap(boolean wrap) {}

    private void initializeRenderer() {
        renderer = new ExtendedRenderer(getDisplay(), this);
        renderer.setContent(content);
        renderer.setFont(getFont(), tabLength);
    }

    /**
     * Can only return null if the constructor is not finished.
     */
    public ExtendedRenderer getRenderer() {
        if (renderer instanceof ExtendedRenderer)
            return (ExtendedRenderer) renderer;
        else
            return null;
    }

    public void setPageInformation(PageInformation pageInformation, DecorationRenderer pageRenderer) {
        reset();

        this.pageInformation = pageInformation;
        this.pageInformationRenderer = pageRenderer;

        ExtendedRenderer renderer = getRenderer();
        renderer.setPageInformation(pageInformation);

        redraw();
    }

    public int getPageWidth() {
        if (pageInformation == null)
            return 1;
        else
            return pageInformation.getPageWidth();
    }

    public int getPageHeight() {
        if (pageInformation == null)
            return 1;
        else
            return pageInformation.getPageHeight();
    }

    public int getScaledPageWidth() {
        if (helper == null)
            return 1;
        else
            return helper.scale(getPageWidth());
    }

    private int getScaledFullWidth() {
        return getScaledPageWidth() + getAdditionalScaledPageWidth();
    }

    public int getAdditionalScaledPageWidth() {
        return 2 * getScaledExternalPageMargin();
    }

    private int getScaledExternalPageMargin() {
        return helper != null ? helper.scale(EXTERNAL_PAGE_MARGIN) : EXTERNAL_PAGE_MARGIN;
    }

    // TODO Esse método é simples de testar
    protected int getLeftMargin(int line) {
        if (line == -1)
            return getScaledExternalPageMargin();

        int lineMargin = 0;
        if (content instanceof IExtendedStyledTextContent) {
            IExtendedStyledTextContent styledTextContent = (IExtendedStyledTextContent) content;
            lineMargin = styledTextContent.getLeftMargin(line);
        }

        return helper.scale(EXTERNAL_PAGE_MARGIN + lineMargin);
    }

    @Override
    void calculateTopIndex(int delta) {
        int oldTopIndex = topIndex;
        int oldTopIndexY = topIndexY;

        topIndex = getRenderer().getLineIndex(delta);

        int newEditorTopLocation = getTopPixel();
        int newTopLocation = getRenderer().getLocation(topIndex, 0, true).y;
        topIndexY = newTopLocation - newEditorTopLocation;

        if (topIndex != oldTopIndex || oldTopIndexY != topIndexY) {
            renderer.calculateClientArea();
            setScrollBars(false);
        }
    }

    @Override
    Rectangle getBoundsAtOffset(int offset) {
        int lineIndex = content.getLineAtOffset(offset);
        int lineOffset = content.getOffsetAtLine(lineIndex);
        String line = content.getLine(lineIndex);
        Rectangle bounds;
        if (line.length() == 0)
            bounds = new Rectangle(0, 0, 0, renderer.getLineHeight());
        else
            bounds = getRenderer().getBounds(lineIndex, offset - lineOffset);

        bounds.x += getLeftMargin(lineIndex) - horizontalScrollOffset;
        bounds.y += getLinePixel(lineIndex);

        return bounds;
    }

    @Override
    public int getLinePixel(int lineIndex) {
        checkWidget();
        if (getRenderer() == null)
            return super.getLinePixel(lineIndex);
        else
            return getRenderer().getLinePixel(lineIndex) - getTopPixel();
    }

    /**
     * Overridden just to fix the wordWrap caret bug.
     */
    @Override
    void doLineEnd() {
        int caretLine = getCaretLine();
        int lineOffset = content.getOffsetAtLine(caretLine);
        int lineEndOffset;

        TextLayout layout = renderer.getTextLayout(caretLine);
        int offsetInLine = caretOffset - lineOffset;
        int lineIndex = getVisualLineIndex(layout, offsetInLine);
        int[] offsets = layout.getLineOffsets();
        lineEndOffset = lineOffset + offsets[lineIndex + 1];
        renderer.disposeTextLayout(layout);

        int OFFSETS_EXTRA_OFFSET = 1;
        boolean isLastLine = offsets.length == lineIndex + 1 + OFFSETS_EXTRA_OFFSET;
        if (!isLastLine)
            lineEndOffset--;

        if (caretOffset < lineEndOffset) {
            setCaretOffset(lineEndOffset, PREVIOUS_OFFSET_TRAILING);
            showCaret();
        }
    }

    @Override
    void doLineDown(boolean select) {
        int caretLine = getCaretLine();
        int lineCount = content.getLineCount();
        int y = 0;
        boolean lastLine = false;

        int lineOffset = content.getOffsetAtLine(caretLine);
        int offsetInLine = caretOffset - lineOffset;
        TextLayout layout = renderer.getTextLayout(caretLine);
        int lineIndex = getVisualLineIndex(layout, offsetInLine);
        int layoutLineCount = layout.getLineCount();
        if (lineIndex >= layoutLineCount - 1) {
            lastLine = caretLine == lineCount - 1;
            caretLine++;
        }
        else {
            Rectangle lineBounds = getRenderer().getLineBounds(caretLine, lineIndex + 1);
            y = lineBounds.y + lineBounds.height / 2;
        }

        renderer.disposeTextLayout(layout);

        if (lastLine) {
            if (select)
                setCaretOffset(content.getCharCount(), SWT.DEFAULT);
        }
        else {
            int[] alignment = new int[1];
            int offset = getOffsetAtPoint(columnX, y, caretLine, alignment);
            setCaretOffset(offset, alignment[0]);
        }
        int oldColumnX = columnX;
        int oldHScrollOffset = getHorizontalPixel();
        if (select) {
            setMouseWordSelectionAnchor();
            // select first and then scroll to reduce flash when key
            // repeat scrolls lots of lines
            doSelection(ST.COLUMN_NEXT);
        }
        showCaret();
        int hScrollChange = oldHScrollOffset - getHorizontalPixel();
        columnX = oldColumnX + hScrollChange;
    }

    @Override
    void doLineUp(boolean select) {
        int caretLine = getCaretLine();
        boolean firstLine = false;

        int lineOffset = content.getOffsetAtLine(caretLine);
        int offsetInLine = caretOffset - lineOffset;
        TextLayout layout = renderer.getTextLayout(caretLine);
        int lineIndex = getVisualLineIndex(layout, offsetInLine);
        if (lineIndex == 0) {
            firstLine = caretLine == 0;
            if (!firstLine) {
                caretLine--;
                renderer.disposeTextLayout(layout);
                layout = renderer.getTextLayout(caretLine);
                lineIndex = layout.getLineCount();
            }
        }

        renderer.disposeTextLayout(layout);

        if (firstLine) {
            if (select)
                setCaretOffset(0, SWT.DEFAULT);
        }
        else {
            Rectangle lineBounds = getRenderer().getLineBounds(caretLine, lineIndex - 1);
            int y = lineBounds.y + lineBounds.height / 2;
            int[] alignment = new int[1];
            int offset = getOffsetAtPoint(columnX, y, caretLine, alignment);
            setCaretOffset(offset, alignment[0]);
        }
        int oldColumnX = columnX;
        int oldHScrollOffset = getHorizontalPixel();
        if (select)
            setMouseWordSelectionAnchor();
        showCaret();
        if (select)
            doSelection(ST.COLUMN_PREVIOUS);
        int hScrollChange = oldHScrollOffset - getHorizontalPixel();
        columnX = oldColumnX + hScrollChange;
    }

    @Override
    void handleMouseUp(Event event) {
        if (!ignore)
            super.handleMouseUp(event);
    }

    @Override
    void handleMouseMove(Event event) {
        if (!ignore)
            super.handleMouseMove(event);
    }

    @Override
    void handleMouseDown(Event event) {
        if (ignore)
            return;

        // Makes triple click select the line break before the paragraph, not after.
        if (event.count != 1 && event.count % 2 == 1 && doubleClickEnabled && !blockSelection) {
            int offset = getOffsetAtPoint(event.x, event.y, null);
            int lineIndex = content.getLineAtOffset(offset);
            int lineOffset = Math.max(0, content.getOffsetAtLine(lineIndex) - 1);
            int lineEnd = content.getCharCount();
            if (lineIndex + 1 < content.getLineCount())
                lineEnd = content.getOffsetAtLine(lineIndex + 1) - 1;
            setSelection(lineOffset, lineEnd - lineOffset, false, false);
            sendSelectionEvent();

            doubleClickSelection = new Point(selection.x, selection.y);
            showCaret();
        }
        else
            super.handleMouseDown(event);
    }

    @Override
    void handleResize(Event event) {
        int oldHeight = clientAreaHeight;
        Rectangle clientArea = getClientArea();
        clientAreaHeight = clientArea.height;
        clientAreaWidth = clientArea.width;

        if (oldHeight != clientAreaHeight && oldHeight == 0)
            topIndexY = 0;
        setCaretLocation();

        renderer.calculateClientArea();

        claimRightFreeSpace();
        claimBottomFreeSpace();

        setScrollBars(true);
        /*
         * StyledText allows any value for horizontalScrollOffset when clientArea is zero in
         * setHorizontalPixel() and setHorizontalOffset(). Fixes bug 168429.
         */
        if (clientAreaWidth != 0) {
            ScrollBar horizontalBar = getHorizontalBar();
            if (horizontalBar != null && horizontalBar.getVisible()
                    && horizontalScrollOffset != horizontalBar.getSelection()) {
                horizontalBar.setSelection(horizontalScrollOffset);
                horizontalScrollOffset = horizontalBar.getSelection();
            }
        }

        updateCaretVisibility();
        setAlignment();
    }

    /**
     * Scrolls text to the right to use new space made available by a resize.
     */
    @Override
    void claimRightFreeSpace() {
        int newHorizontalOffset = Math.max(0, getScaledFullWidth() - clientAreaWidth);
        if (newHorizontalOffset < getHorizontalPixel()) {
            // item is no longer drawn past the right border of the client area
            // align the right end of the item with the right border of the
            // client area (window is scrolled right).
            scrollHorizontal(newHorizontalOffset - getHorizontalPixel(), true);
            setScrollBars(true);
        }
    }

    @Override
    int getOffsetAtPoint(int x, int y, int[] trailing, boolean inTextOnly) {
        if (inTextOnly && y + getVerticalScrollOffset() < 0 || x + getHorizontalPixel() < 0)
            return -1;

        int lineIndex = getLineIndex(y);
        int lineOffset = content.getOffsetAtLine(lineIndex);
        x += getHorizontalPixel() - getLeftMargin(lineIndex);
        y -= getLinePixel(lineIndex);
        int offset = 0;
        if (getRenderer() != null)
            offset = getRenderer().getOffset(lineIndex, x, y, trailing);
        return offset + lineOffset;
    }

    @Override
    public int getLineIndex(int y) {
        if (getRenderer() == null)
            return super.getLineIndex(y);
        else
            return getRenderer().getLineIndex(y);
    }

    @Override
    int getOffsetAtPoint(int x, int y, int lineIndex, int[] alignment) {
        TextLayout layout = renderer.getTextLayout(lineIndex);
        x += getHorizontalPixel() - getLeftMargin(lineIndex);
        int[] trailing = new int[1];
        int offsetInLine = layout.getOffset(x, y, trailing);
        if (getRenderer() != null)
            offsetInLine = getRenderer().getOffset(lineIndex, x, y, trailing);

        if (alignment != null)
            alignment[0] = OFFSET_LEADING;
        if (trailing[0] != 0) {
            int lineInParagraph = layout.getLineIndex(offsetInLine + trailing[0]);
            int lineStart = layout.getLineOffsets()[lineInParagraph];
            boolean atLineEnd = offsetInLine + trailing[0] == lineStart;
            if (atLineEnd) {
                // Bug fix: We don't increment the offset in line because we want a click after the
                // line to stay at the end of the line
                // offsetInLine += trailing[0];
                if (alignment != null)
                    alignment[0] = PREVIOUS_OFFSET_TRAILING;
            }
            else {
                String line = content.getLine(lineIndex);
                int level = 0;
                if (alignment != null) {
                    int offset = offsetInLine;
                    while (offset > 0 && Character.isDigit(line.charAt(offset)))
                        offset--;
                    if (offset == 0 && Character.isDigit(line.charAt(offset)))
                        level = isMirrored() ? 1 : 0;
                    else
                        level = layout.getLevel(offset) & 0x1;
                }
                offsetInLine += trailing[0];
                if (alignment != null) {
                    int trailingLevel = layout.getLevel(offsetInLine) & 0x1;
                    if ((level ^ trailingLevel) != 0)
                        alignment[0] = PREVIOUS_OFFSET_TRAILING;
                    else
                        alignment[0] = OFFSET_LEADING;
                }
            }
        }
        renderer.disposeTextLayout(layout);
        return offsetInLine + content.getOffsetAtLine(lineIndex);
    }

    @Override
    Point getPointAtOffset(int offset) {
        int lineIndex = content.getLineAtOffset(offset);
        String line = content.getLine(lineIndex);
        int lineOffset = content.getOffsetAtLine(lineIndex);
        int offsetInLine = offset - lineOffset;
        int lineLength = line.length();
        if (lineIndex < content.getLineCount() - 1) {
            int endLineOffset = content.getOffsetAtLine(lineIndex + 1) - 1;
            // Looks like it might happen if we are typing an accent at the end of the line
            if (lineLength < offsetInLine && offsetInLine <= endLineOffset)
                offsetInLine = lineLength;
        }

        Point point = null;

        ExtendedRenderer renderer = getRenderer();
        if (renderer != null && offsetInLine <= lineLength)
            point = renderer.getLocation(lineIndex, offsetInLine, caretAlignment == OFFSET_LEADING);
        else
            point = new Point(0, 0);

        point.x -= getHorizontalPixel();
        point.y -= getTopPixel();
        return point;
    }

    /**
     * Renders the invalidated area specified in the paint event.
     *
     * @param event
     *            paint event
     */
    @Override
    void handlePaint(Event event) {
        if (event.width == 0 || event.height == 0 || clientAreaWidth == 0 || clientAreaHeight == 0)
            return;

        drawExternalPageMargins(event);

        GC gc = event.gc;
        gc.setTransform(createTransform(gc.getDevice()));

        getRenderer().drawArea(gc, areaToDrawFrom(event));
    }

    private Rectangle areaToDrawFrom(Event event) {
        int drawX = EXTERNAL_PAGE_MARGIN;
        int drawWidth = pageInformation.getPageWidth();

        int drawY = (int) Math.floor(helper.rawUnscale(verticalScrollOffset + event.y));
        int drawHeight = (int) Math.ceil(helper.rawUnscale(event.height));

        return new Rectangle(drawX, drawY, drawWidth, drawHeight);
    }

    private Transform createTransform(Device device) {
        Transform transform = new Transform(device);
        transform.translate(-horizontalScrollOffset, -verticalScrollOffset);
        float scale = (float) getScalingFactor();
        transform.scale(scale, scale);
        return transform;
    }

    private void drawExternalPageMargins(Event event) {
        double pageStart = getScaledExternalPageMargin() - horizontalScrollOffset;
        double pageWidth = getScaledPageWidth();
        double pageEnd = pageStart + pageWidth;

        if (event.x < pageStart || event.x + event.width > pageEnd) {
            GC gc = event.gc;
            Color tmp = gc.getBackground();

            gc.setBackground(getExternalBackgroundColor());
            gc.fillRectangle(event.x, event.y, event.width, event.height);

            gc.setBackground(tmp);
        }
    }

    @Override
    void sendSelectionEvent() {
        super.sendSelectionEvent();
        super.redraw();
    }

    /**
     * Returns the smallest bounding rectangle that includes the characters between two offsets.
     *
     * @param start
     *            offset of the first character included in the bounding box
     * @param end
     *            offset of the last character included in the bounding box
     * @return bounding box of the text between start and end
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created
     *                the receiver</li>
     *                </ul>
     * @exception IllegalArgumentException
     *                <ul>
     *                <li>ERROR_INVALID_RANGE when start and/or end are outside the widget content</li>
     *                </ul>
     * @since 3.1
     */
    @Override
    public Rectangle getTextBounds(int start, int end) {
        Rectangle bounds = super.getTextBounds(start, end);
        int lineStart = getLineAtOffset(start);
        bounds.x += getLeftMargin(lineStart);
        return bounds;
    }

    @Override
    void internalRedrawRange(int start, int length) {
        if (length <= 0)
            return;
        int end = start + length;
        int startLine = content.getLineAtOffset(start);
        int endLine = content.getLineAtOffset(end);
        int partialBottomIndex = getPartialBottomIndex();
        int partialTopIndex = getPartialTopIndex();
        if (startLine > partialBottomIndex || endLine < partialTopIndex)
            return;
        if (partialTopIndex > startLine) {
            startLine = partialTopIndex;
            start = 0;
        }
        else
            start -= content.getOffsetAtLine(startLine);
        if (partialBottomIndex < endLine) {
            endLine = partialBottomIndex + 1;
            end = 0;
        }
        else
            end -= content.getOffsetAtLine(endLine);

        TextLayout layout = renderer.getTextLayout(startLine);
        int lineX = getLeftMargin(startLine) - getHorizontalPixel();// <>
        int startLineY = getLinePixel(startLine);
        int[] offsets = layout.getLineOffsets();
        int startIndex = layout.getLineIndex(Math.min(start, layout.getText().length()));

        /* Redraw end of line before start line if wrapped and start offset is first char */
        if (startIndex > 0 && offsets[startIndex] == start) {
            Rectangle rect = layout.getLineBounds(startIndex - 1);
            rect.x = rect.width;
            rect.width = clientAreaWidth - rect.x;// <>
            rect.x += lineX;
            rect.y += startLineY;
            super.redraw(rect.x, rect.y, rect.width, rect.height, false);
        }

        if (startLine == endLine) {
            int endIndex = layout.getLineIndex(Math.min(end, layout.getText().length()));
            if (startIndex == endIndex) {
                /*
                 * Redraw rect between start and end offset if start and end offsets are in same
                 * wrapped line
                 */
                Rectangle rect = layout.getBounds(start, end - 1);
                rect.x += lineX;
                rect.y += startLineY;
                super.redraw(rect.x, rect.y, rect.width, rect.height, false);
                renderer.disposeTextLayout(layout);
                return;
            }
        }

        /* Redraw start line from the start offset to the end of client area */
        Rectangle startRect = layout.getBounds(start, offsets[startIndex + 1] - 1);
        if (startRect.height == 0) {
            Rectangle bounds = layout.getLineBounds(startIndex);
            startRect.x = bounds.width;
            startRect.y = bounds.y;
            startRect.height = bounds.height;
        }
        startRect.x += lineX;
        startRect.y += startLineY;
        startRect.width = clientAreaWidth - startRect.x;
        super.redraw(startRect.x, startRect.y, startRect.width, startRect.height, false);

        /* Redraw end line from the beginning of the line to the end offset */
        if (startLine != endLine) {
            renderer.disposeTextLayout(layout);
            layout = renderer.getTextLayout(endLine);
            offsets = layout.getLineOffsets();
        }
        int endIndex = layout.getLineIndex(Math.min(end, layout.getText().length()));
        Rectangle endRect = layout.getBounds(offsets[endIndex], end - 1);
        if (endRect.height == 0) {
            Rectangle bounds = layout.getLineBounds(endIndex);
            endRect.y = bounds.y;
            endRect.height = bounds.height;
        }
        endRect.x += lineX;
        endRect.y += getLinePixel(endLine);
        super.redraw(endRect.x, endRect.y, endRect.width, endRect.height, false);
        renderer.disposeTextLayout(layout);

        /* Redraw all lines in between start and end line */
        int y = startRect.y + startRect.height;
        if (endRect.y > y)
            super.redraw(getLeftMargin(-1), y, clientAreaWidth - getLeftMargin(-1), endRect.y - y,
                         false);// <>
    }

    /**
     * Scrolls down the text to use new space made available by a resize or by deleted lines.
     */
    @Override
    void claimBottomFreeSpace() {
        // TODO Precisa implementar algum comportamento quando tem pageInformation.
        if (pageInformation == null) {
            int bottomIndex = getPartialBottomIndex();
            int height = getLinePixel(bottomIndex + 1);
            int gap = GAP_LINES * averageCharHeight;
            if (clientAreaHeight > height + gap)
                scrollVertical(-getAvailableHeightAbove(clientAreaHeight - height - gap), true);
        }
    }

    @Override
    public void setFont(Font font) {
        Point offset = getSelection();
        int caretOffset = getCaretOffset();

        if (font == null || font.isDisposed())
            return;

        super.setFont(font);
        GC gc = new GC(getDisplay());
        gc.setFont(getFont());
        FontMetrics metrics = gc.getFontMetrics();
        averageCharHeight = metrics.getHeight();

        setCaretOffset(caretOffset);
        setSelection(offset);

        gc.dispose();
    }

    public void setScale(ZoomOption newScale) {
        topIndexY = helper.unscale(topIndexY);

        currentZoom = newScale;
        getRenderer().resetCaches();

        topIndexY = helper.scale(topIndexY);

        calculateScrollBars();
        scrollToTop();
        redraw();
    }

    public ZoomOption getZoom() {
        return currentZoom;
    }

    public double getScalingFactor() {
        return Math.max(MINIMUM_ZOOM, currentZoom.getScalingFactor(this));
    }

    public ScaleHelper getScaleHelper() {
        return helper;
    }

    @Override
    void setScrollBars(boolean vertical) {
        if (getHorizontalBar() != null)
            getHorizontalBar().setVisible(clientAreaWidth < getScaledFullWidth());

        int inactive = 1;
        if (vertical || !isFixedLineHeight()) {
            ScrollBar verticalBar = getVerticalBar();
            if (verticalBar != null) {
                int maximum = renderer.getHeight();
                /*
                 * only set the real values if the scroll bar can be used (ie. because the thumb
                 * size is less than the scroll maximum) avoids flashing on Motif, fixes 1G7RE1J and
                 * 1G5SE92
                 */
                if (clientAreaHeight < maximum) {
                    verticalBar.setMaximum(maximum);
                    verticalBar.setThumb(clientAreaHeight);
                    verticalBar.setPageIncrement(clientAreaHeight);
                }
                else if (verticalBar.getThumb() != inactive || verticalBar.getMaximum() != inactive)
                    verticalBar.setValues(verticalBar.getSelection(), verticalBar.getMinimum(),
                                          inactive, inactive, verticalBar.getIncrement(), inactive);
            }
        }
        ScrollBar horizontalBar = getHorizontalBar();
        if (horizontalBar != null) {
            int maximum = getScaledFullWidth();
            /*
             * only set the real values if the scroll bar can be used (ie. because the thumb size is
             * less than the scroll maximum) avoids flashing on Motif, fixes 1G7RE1J and 1G5SE92
             */
            if (clientAreaWidth < maximum) {
                horizontalBar.setMaximum(maximum);
                horizontalBar.setThumb(clientAreaWidth);
                horizontalBar.setPageIncrement(clientAreaWidth);
            }
            else if (horizontalBar.getThumb() != inactive || horizontalBar.getMaximum() != inactive)
                horizontalBar.setValues(horizontalBar.getSelection(), horizontalBar.getMinimum(),
                                        inactive, inactive, horizontalBar.getIncrement(), inactive);
        }
    }

    /**
     * A mouse move event has occurred. See if we should start auto-scrolling. If the move position
     * is outside of the client area, initiate auto-scrolling. Otherwise, we've moved back into the
     * widget so end auto-scrolling.
     */
    @Override
    void doAutoScroll(Event event) {
        if (event.y > clientAreaHeight)
            doAutoScroll(SWT.DOWN, event.y - clientAreaHeight);
        else if (event.y < 0)
            doAutoScroll(SWT.UP, -event.y);
        else if (event.x < 0)
            doAutoScroll(ST.COLUMN_PREVIOUS, -event.x);
        else if (event.x > clientAreaWidth)
            doAutoScroll(ST.COLUMN_NEXT, event.x - clientAreaWidth);
        else
            endAutoScroll();
    }

    @Override
    void doPageUp(boolean select, int height) {
        if (height == -1)
            super.doPageUp(select, height);
        else {
            int oldColumnX = columnX;
            int oldHScrollOffset = horizontalScrollOffset;

            height = getAvailableHeightAbove(height);
            scrollVertical(-height, true);

            int lineIndex = getTopIndex();
            int relativeY = -getLinePixel(lineIndex);
            int[] alignment = new int[1];
            int offset = getOffsetAtPoint(columnX, relativeY, lineIndex, alignment);
            setCaretOffset(offset, alignment[0]);
            if (select)
                doSelection(ST.COLUMN_PREVIOUS);
            if (height == 0)
                setCaretLocation();
            showCaret();

            int hScrollChange = oldHScrollOffset - horizontalScrollOffset;
            columnX = oldColumnX + hScrollChange;
        }
    }

    @Override
    void doPageDown(boolean select, int height) {
        if (height == -1)
            super.doPageDown(select, height);
        else {
            int oldColumnX = columnX;
            int oldHScrollOffset = horizontalScrollOffset;

            height = getAvailableHeightBellow(height);
            scrollVertical(height, true);

            int lineIndex = getPartialBottomIndex();
            int linePixel = getLinePixel(lineIndex);
            int relativeY = clientAreaHeight - linePixel;
            int[] alignment = new int[1];
            int offset = getOffsetAtPoint(columnX, relativeY, lineIndex, alignment);
            setCaretOffset(offset, alignment[0]);
            if (select)
                doSelection(ST.COLUMN_NEXT);
            if (height == 0)
                setCaretLocation();
            showCaret();

            int hScrollChange = oldHScrollOffset - horizontalScrollOffset;
            columnX = oldColumnX + hScrollChange;
        }
    }

    @Override
    void scrollText(int srcY, int destY) {
        int leftMargin = getScaledExternalPageMargin();// +
        if (srcY == destY)
            return;
        int deltaY = destY - srcY;
        int scrollWidth = clientAreaWidth - leftMargin, scrollHeight;
        if (deltaY > 0)
            scrollHeight = clientAreaHeight - srcY;
        else
            scrollHeight = clientAreaHeight - destY;

        scroll(leftMargin, destY, leftMargin, srcY, scrollWidth, scrollHeight, true);
        if ((0 < srcY + scrollHeight) && (0 > srcY))
            super.redraw(leftMargin, deltaY, scrollWidth, 0, false);
        if ((0 < destY + scrollHeight) && (0 > destY))
            super.redraw(leftMargin, 0, scrollWidth, 0, false);
        if ((clientAreaHeight < srcY + scrollHeight) && (clientAreaHeight > srcY))
            super.redraw(leftMargin, clientAreaHeight + deltaY, scrollWidth, 0, false);
        if ((clientAreaHeight < destY + scrollHeight) && (clientAreaHeight > destY))
            super.redraw(leftMargin, clientAreaHeight, scrollWidth, 0, false);
    }

    @Override
    public void setTopIndex(int topIndex) {
        checkWidget();
        if (getCharCount() == 0)
            return;
        int lineCount = content.getLineCount();
        topIndex = Math.max(0, Math.min(lineCount - 1, topIndex));
        int pixel = getLinePixel(topIndex);
        if (pixel > 0)
            pixel = getAvailableHeightBellow(pixel);
        else
            pixel = getAvailableHeightAbove(pixel);
        int space = renderer.getLineHeight() * TOP_INDEX_LINESPACES;
        scrollVertical(pixel - space, true);
    }

    @Override
    void modifyContent(Event event, boolean updateCaret) {
        super.modifyContent(event, updateCaret);

        int caretOffset = getCaretOffset();
        Point point = getPointAtOffset(caretOffset);
        int gap = GAP_LINES * getLineHeight(caretOffset);

        if (point.y + gap > clientAreaHeight) {
            int verticalScrollOffset = point.y - clientAreaHeight / 2;
            scrollVertical(verticalScrollOffset, true);
        }
    }

    /**
     * Scrolls the widget vertically.
     *
     * @param pixel
     *            the delta to apply to obtain the new vertical scroll offset
     * @param adjustScrollBar
     *            true= the scroll thumb will be moved to reflect the new scroll offset. false = the
     *            scroll thumb will not be moved
     * @return true=the widget was scrolled false=the widget was not scrolled
     */
    @Override
    boolean scrollVertical(int pixels, boolean adjustScrollBar) {
        return scrollVertical(pixels, adjustScrollBar, true);
    }

    private boolean scrollVertical(int pixels, boolean adjustScrollBar, boolean calculateTopIndex) {
        if (pixels == 0)
            return false;
        pixels = helper.scale(helper.unscale(pixels));

        if (verticalScrollOffset != -1) {
            ScrollBar verticalBar = getVerticalBar();
            int newVerticalScrollOffset = getVerticalScrollOffset() + pixels;
            if (verticalBar != null && adjustScrollBar)
                verticalBar.setSelection(newVerticalScrollOffset);

            int delta = Math.abs(pixels);
            int sourceY = Math.max(0, pixels);
            int destinationY = -Math.min(0, pixels);

            int scrollHeight = clientAreaHeight - delta;
            if (scrollHeight > 0)
                scroll(0, destinationY, 0, sourceY, clientAreaWidth, scrollHeight, true);

            boolean middleOfScreenNeedsRedraw = delta > scrollHeight;
            if (middleOfScreenNeedsRedraw) {
                int redrawY = Math.max(0, scrollHeight);
                int redrawHeight = Math.min(clientAreaHeight, delta - scrollHeight);
                super.redraw(0, redrawY, clientAreaWidth, redrawHeight, true);
            }

            verticalScrollOffset = newVerticalScrollOffset;
            // The verticalScrollOffset is already update therefore we do not need to pass any delta
            // to calculate the new top index
            if (calculateTopIndex)
                calculateTopIndex(0);
        }
        else {
            if (calculateTopIndex)
                calculateTopIndex(pixels);
            super.redraw();
        }
        setCaretLocation();
        return true;
    }

    @Override
    int getVerticalScrollOffset() {
        if (verticalScrollOffset == -1) {
            int topPixel = getRenderer().getLinePixel(topIndex);
            verticalScrollOffset = topPixel - topIndexY;
        }
        return verticalScrollOffset;
    }

    @Override
    boolean scrollHorizontal(int pixels, boolean adjustScrollBar) {
        if (pixels == 0)
            return false;

        ScrollBar horizontalBar = getHorizontalBar();
        if (horizontalBar != null && adjustScrollBar)
            horizontalBar.setSelection(getHorizontalPixel() + pixels);

        // TODO testar essa lógica
        int delta = Math.abs(pixels);
        int sourceX = Math.max(0, pixels);
        int destX = -Math.min(0, pixels);

        int scrollWidth = clientAreaWidth - delta;

        if (scrollWidth > 0)
            scroll(destX, 0, sourceX, 0, scrollWidth, clientAreaHeight, true);

        boolean scrolledOutOfScreenContent = delta > scrollWidth;
        if (scrolledOutOfScreenContent)
            super.redraw(scrollWidth, 0, delta - scrollWidth, clientAreaHeight, true);

        horizontalScrollOffset += pixels;
        setCaretLocation();

        return true;
    }

    @Override
    public void setCaret(Caret caret) {
        if (getCaret() == null)
            defaultCaret = caret;

        super.setCaret(caret);
    }

    @Override
    void setCaretOffset(int offset, int alignment) {
        int length = getCharCount();
        if (offset < 0)
            offset = 0;
        else if (offset > length)
            offset = length;

        super.setCaretOffset(offset, alignment);
    }

    public int getLeftFieldSize() {
        return EXTERNAL_PAGE_MARGIN;
    }

    @Override
    void setSelection(int start, int length, boolean sendEvent, boolean doBlock) {
        int validStart = toValidOffset(start);
        int validEnd = toValidOffset(validStart + length);

        super.setSelection(validStart, validEnd - validStart, sendEvent, doBlock);
    }

    private int toValidOffset(int offset) {
        if (offset > getCharCount())
            offset = getCharCount();
        else if (offset < 0)
            offset = 0;
        return offset;
    }

    public Runnable print(Printer printer, DecorationRenderer pageRenderer) {
        checkWidget();
        if (printer == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        return new ExtendedPrinting(this, printer, pageInformation, pageRenderer);
    }

    public DecorationRenderer getPageDecorationRenderer() {
        return pageInformationRenderer;
    }

    public void scrollToTop() {
        int topIndexLocation = getRenderer().getLocation(topIndex, 0, true).y;
        int newLocation = topIndexLocation - topIndexY;

        int minimumOffset = -getVerticalScrollOffset();
        int maximumOffset = getVerticalBar().getMaximum() - getVerticalScrollOffset();
        int toScroll = Math.max(minimumOffset,
                                Math.min(newLocation - getVerticalScrollOffset(), maximumOffset));
        scrollVertical(toScroll, true, false);
    }

    @Override
    int getAvailableHeightAbove(int height) {
        int maxHeight = getVerticalScrollOffset();
        if (maxHeight == -1) {
            int lineIndex = topIndex - 1;
            maxHeight = -topIndexY;
            maxHeight += getRenderer().getLocation(lineIndex, 0, true).y;
        }
        return Math.min(height, maxHeight);
    }

    @Override
    int getAvailableHeightBellow(int height) {
        int totalHeight = getRenderer().getHeight();
        int availableHeight = totalHeight - getVerticalScrollOffset();
        return Math.min(height, availableHeight - clientAreaHeight);
    }

    public void setExternalBackgroundColor(Color external) {
        this.externalBackground = external;
    }

    public Color getExternalBackgroundColor() {
        return externalBackground;
    }

    public void setIgnoreMouseEvent(boolean shouldIgnore) {
        ignore = shouldIgnore;
        if (ignore)
            clickCount = 0;
    }
}
