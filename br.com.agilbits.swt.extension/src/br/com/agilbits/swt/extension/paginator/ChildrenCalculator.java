package br.com.agilbits.swt.extension.paginator;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;

public class ChildrenCalculator {
    private LogicalLine line;
    private List<ExtendedTextLayout> children;

    public ChildrenCalculator(LogicalLine line) {
        this.line = line;
        children = new LinkedList<ExtendedTextLayout>();
    }

    public List<ExtendedTextLayout> getChildren() {
        return children;
    }

    // FIXME Refactor like there is no tomorrow
    public void update(boolean forceUpdate, int availableHeight, int breakSize, int pageHeight,
            int minimumLines, int blockSize) {
        if (forceUpdate) {
            dispose();
            children = new LinkedList<ExtendedTextLayout>();
        }

        int textAvailableHeight = availableHeight;
        Point initialLocation = line.getSpacedBaseLocation();
        Point location = new Point(initialLocation.x, initialLocation.y);
        boolean hasPageBreak;
        boolean blockWontFit = false;
        if (lineFits(textAvailableHeight, minimumLines)) {
            textAvailableHeight -= line.getParagraphSpacing();
            hasPageBreak = availableHeight == pageHeight;
            if (!lineFits(textAvailableHeight, blockSize)) {
                textAvailableHeight -= getDecorationSize();
                blockWontFit = true;
            }
        }
        else {
            location.y += availableHeight + breakSize + getDecorationSize()
                    - line.getParagraphSpacing();
            textAvailableHeight = pageHeight;
            hasPageBreak = true;
        }

        TextLayout masterLayout = line.getTextLayout();
        String text = masterLayout.getText();
        int currentOffset = 0;

        int childIndex = 0;
        Rectangle bounds = masterLayout.getBounds(currentOffset, currentOffset + text.length());
        while (bounds.height > textAvailableHeight) {
            int breakingOffset = getBreakingOffset(textAvailableHeight, text, blockWontFit);
            if (breakingOffset > 0) {
                String first = text.substring(0, breakingOffset);
                if (childIndex >= children.size())
                    children.add(childIndex,
                                 createChildLayout(location, first, currentOffset, hasPageBreak));
                else {
                    ExtendedTextLayout child = children.get(childIndex);
                    updateChild(masterLayout, child, location, first, currentOffset, hasPageBreak);
                }
                childIndex++;
            }

            location.y += textAvailableHeight + breakSize + getDecorationSize();
            if (blockWontFit)
                location.y += getDecorationSize();
            textAvailableHeight = pageHeight - getDecorationSize();
            text = text.substring(breakingOffset);
            currentOffset += breakingOffset;
            hasPageBreak = true;
            bounds = masterLayout.getBounds(currentOffset, currentOffset + text.length());
            blockWontFit = false;
        }
        if (childIndex >= children.size())
            children.add(childIndex, createChildLayout(location, text, currentOffset, hasPageBreak));
        else {
            ExtendedTextLayout child = children.get(childIndex);
            updateChild(masterLayout, child, location, text, currentOffset, hasPageBreak);
        }
        childIndex++;

        while (childIndex < children.size())
            children.remove(childIndex);
    }

    private int getBreakingOffset(int availableHeight, String text, boolean blockWontFit) {
        TextLayout textLayout = line.getTextLayout();
        TextLayout newLayout = new TextLayout(textLayout.getDevice());
        newLayout.setFont(textLayout.getFont());
        newLayout.setWidth(textLayout.getWidth());
        newLayout.setAlignment(textLayout.getAlignment());
        newLayout.setText(text);
        int[] lineOffsets = newLayout.getLineOffsets();
        newLayout.dispose();

        int lineHeight = line.getLineHeight();
        int decorationSize = blockWontFit ? 0 : getDecorationSize();
        int lineAfterBreak = (availableHeight - decorationSize) / lineHeight;
        return lineOffsets[lineAfterBreak];
    }

    private ExtendedTextLayout createChildLayout(Point location, String text, int childOffset,
            boolean hasPageBreak) {
        Point relativeLocation = getRelativeLocation(location);
        return new ExtendedTextLayout(line, text, relativeLocation, hasPageBreak, childOffset);
    }

    private void updateChild(TextLayout masterLayout, ExtendedTextLayout child, Point location,
            String text, int childOffset, boolean hasPageBreak) {
        child.setPageBreak(hasPageBreak);
        child.setOffset(childOffset);
        child.setLocation(getRelativeLocation(location));
        child.setText(text);
        child.updateLayout(masterLayout);
    }

    private Point getRelativeLocation(Point location) {
        Point lineLocation = line.getSpacedBaseLocation();
        return new Point(location.x - lineLocation.x, location.y - lineLocation.y);
    }

    private boolean lineFits(int availableHeight, int minimumLines) {
        return availableHeight >= getMinimumSpace(minimumLines);
    }

    private int getMinimumSpace(int minimumLines) {
        return minimumLines * line.getLineHeight();
    }

    private int getDecorationSize() {
        return line.getBreakDecorationSize() * line.getLineHeight();
    }

    public void dispose() {
        Disposer disposer = new Disposer(getChildren());
        disposer.start();
        children = null;
    }
}
