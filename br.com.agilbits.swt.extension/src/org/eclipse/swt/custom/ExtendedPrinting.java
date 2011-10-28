package org.eclipse.swt.custom;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;

import br.com.agilbits.swt.extension.paginator.ExtendedTextLayout;
import br.com.agilbits.swt.extension.paginator.LogicalLine;
import br.com.agilbits.swt.extension.paginator.Paginator;

public class ExtendedPrinting implements Runnable {
    private ExtendedStyledText styledText;
    private Printer printer;
    private DecorationRenderer decorationRenderer;
    private PageInformation pageInformation;
    private Transform scale;
    private Paginator paginator;
    private StyleRangesApplier applier;
    private GC gc;

    public ExtendedPrinting(ExtendedStyledText styledText, Printer printer,
            PageInformation originalPageInformation, DecorationRenderer decorationRenderer) {
        this.styledText = styledText;
        this.printer = printer;
        this.decorationRenderer = decorationRenderer;

        pageInformation = originalPageInformation.copy();
        pageInformation.setDrawPageBreaks(true);

        scale = new Transform(printer);
        float scaleX = ((float) printer.getDPI().x) / styledText.getDisplay().getDPI().x;
        float scaleY = ((float) printer.getDPI().y) / styledText.getDisplay().getDPI().y;
        scale.scale(scaleX, scaleY);

        paginator = createPaginator(styledText, printer);
        applier = new StyleRangesApplier(styledText);
    }

    private Paginator createPaginator(ExtendedStyledText styledText, Printer printer) {
        Paginator paginator = new Paginator(pageInformation);
        paginator.setFont(styledText.getFont());

        IExtendedStyledTextContent content = (IExtendedStyledTextContent) styledText.getContent();
        paginator.setContent(printer, content);

        return paginator;
    }

    public void run() {
        Rectangle trim = printer.computeTrim(0, 0, 0, 0);

        gc = new GC(printer);
        gc.setTransform(scale);
        gc.setTextAntialias(SWT.ON);
        gc.setFont(styledText.getFont());

        Point range = getOffsetRange();
        // TODO Should consider variables such as copies and collate
        drawLineRange(trim, range.x, range.y);

        paginator.dispose();
        gc.dispose();
    }

    private Point getOffsetRange() {
        int startOffset;
        int endOffset;

        PrinterData data = printer.getPrinterData();
        if (data.scope == PrinterData.PAGE_RANGE) {
            startOffset = paginator.getOffsetForPage(data.startPage - 1);
            endOffset = paginator.getOffsetForPage(data.endPage);
        }
        else {
            startOffset = 0;
            endOffset = styledText.getCharCount();
        }

        return new Point(startOffset, endOffset);
    }

    private void drawLineRange(Rectangle trim, int startOffset, int endOffset) {
        int startingLine = styledText.getLineAtOffset(startOffset);
        int endingLine = styledText.getLineAtOffset(endOffset);

        for (int lineIndex = startingLine; lineIndex <= endingLine; lineIndex++)
            drawChildren(startOffset, endOffset, lineIndex, trim);

        endPage(endOffset - 1, trim);
    }

    private void drawChildren(int startOffset, int endOffset, int lineIndex, Rectangle trim) {
        IExtendedStyledTextContent content = (IExtendedStyledTextContent) styledText.getContent();

        LogicalLine line = paginator.getLogicalLine(lineIndex);
        int lineOffset = content.getOffsetAtLine(lineIndex);

        for (ExtendedTextLayout child : line.getChildren()) {
            int currentOffset = lineOffset + child.getOffset();
            if (isVisible(startOffset, endOffset, currentOffset)) {
                if (child.hasPageBreak())
                    breakPage(startOffset == currentOffset, trim, currentOffset);

                drawChild(child, lineOffset, trim);
            }
        }
    }

    private boolean isVisible(int startOffset, int endOffset, int currentOffset) {
        return startOffset <= currentOffset && currentOffset < endOffset;
    }

    private void breakPage(boolean isFirstPage, Rectangle trim, int currentOffset) {
        if (!isFirstPage)
            endPage(Math.max(0, currentOffset - 1), trim);
        startPage(currentOffset, trim);
    }

    private void endPage(int offset, Rectangle trim) {
        int scaledPageEnd = pageInformation.getPageHeight();
        decorationRenderer.drawFooter(gc, styledText, offset, scaledPageEnd, trim);
        printer.endPage();
    }

    private void startPage(int offset, Rectangle trim) {
        printer.startPage();
        gc.setTransform(scale);
        decorationRenderer.drawHeader(gc, styledText, offset, 0, trim);
    }

    private void drawChild(ExtendedTextLayout child, int lineOffset, Rectangle trim) {
        Point childLocation = getChildLocation(child);
        
        TextLayout textLayout = child.getTextLayout(applier, lineOffset);
        Point paintLocation = new Point(childLocation.x + trim.x, childLocation.y + trim.y);
        textLayout.draw(gc, paintLocation.x, paintLocation.y);

        decorationRenderer.decorateLine(gc, lineOffset, childLocation, textLayout,
                                        trim);
    }

    private Point getChildLocation(ExtendedTextLayout child) {
        Point location = child.getLocation();
        int x = location.x - Paginator.EXTERNAL_MARGIN;

        int yInPage = location.y % paginator.getPageHeight();
        int y = yInPage - Paginator.EXTERNAL_MARGIN;
        return new Point(x, y);
    }
}
