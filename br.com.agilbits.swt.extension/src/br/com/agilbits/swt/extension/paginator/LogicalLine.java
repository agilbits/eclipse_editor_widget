package br.com.agilbits.swt.extension.paginator;

import java.util.List;

import org.eclipse.swt.custom.IExtendedStyledTextContent;
import org.eclipse.swt.custom.StyleRangesApplier;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;

public class LogicalLine {
    private IExtendedStyledTextContent content;
    private int lineIndex;
    private TextLayout textLayout;
    private Point baseLocation;
    private Point continuousLocation;

    private ChildrenCalculator calculator;

    private int lineHeight;
    private int boundsHeight;
    private boolean textInvalid;
    private boolean layoutInvalid;
    private boolean hasInvalidLocation;
    private int fullHeight;

    public LogicalLine(Device device, IExtendedStyledTextContent content, int lineIndex) {
        this.content = content;
        this.lineIndex = lineIndex;
        textLayout = new TextLayout(device);
        hasInvalidLocation = true;
        calculator = new ChildrenCalculator(this);

        invalidateLineHeight();
        invalidateBoundsHeight();
        invalidateText();
        invalidateLayout();
    }

    private void invalidateLineHeight() {
        lineHeight = -1;
        fullHeight = -1;
    }

    private void invalidateBoundsHeight() {
        boundsHeight = -1;
        fullHeight = -1;
    }

    protected void invalidateText() {
        textInvalid = true;
        fullHeight = -1;
    }

    protected void invalidateLayout() {
        layoutInvalid = true;
        fullHeight = -1;
    }

    public void setFont(Font font) {
        textLayout.setFont(font);
//        if (font != null)
//            textLayout.setMaximumLineHeight(font.getFontData()[0].getHeight());
        invalidateLineHeight();
        invalidateBoundsHeight();
    }

    public int getFullHeight() {
        if (fullHeight < 0) {
            List<ExtendedTextLayout> children = getChildren();
            ExtendedTextLayout lastChild = children.get(children.size() - 1);
            int heightToLastChild = lastChild.getLocation().y - getBaseLocation().y;
            fullHeight = heightToLastChild + lastChild.getHeight();
        }
        return fullHeight;
    }

    public int getHeight() {
        return getBoundsHeight() + getParagraphSpacing();
    }

    protected int getBoundsHeight() {
        if (boundsHeight < 0)
            boundsHeight = textLayout.getBounds().height;
        return boundsHeight;
    }

    protected int getParagraphSpacing() {
        int lineHeight = getLineHeight();
        return lineHeight * content.getParagraphSpacing(lineIndex);
    }

    protected int getLineHeight() {
        if (lineHeight < 0)
            lineHeight = textLayout.getLineBounds(0).height;
        return lineHeight;
    }

    public TextLayout getTextLayout() {
        updateText();
        updateLayoutValues();
        return textLayout;
    }

    public TextLayout getTextLayout(StyleRangesApplier applier) {
        updateText();
        updateLayoutValues();

        // FIXME Handle styles correctly
        textLayout.setStyle(new TextStyle(), 0, textLayout.getText().length());
        int offset = content.getOffsetAtLine(lineIndex);
        applier.applyStyles(offset, textLayout.getText(), textLayout);
        return textLayout;
    }

    public void updateLayout() {
        invalidateLayout();
        updateLayoutValues();
    }

    private void updateText() {
        if (hasInvalidText()) {
            textLayout.setText(content.getLine(lineIndex));
            invalidateBoundsHeight();
            textInvalid = false;
        }
    }

    private void updateLayoutValues() {
        if (layoutInvalid) {
            textLayout.setAlignment(content.getParagraphAlignment(lineIndex));
            textLayout.setWidth(content.getRightMargin(lineIndex)
                    - content.getLeftMargin(lineIndex));
            invalidateBoundsHeight();
            layoutInvalid = false;
        }
    }

    // FIXME deveria ser protected. Só não é pros plugin tests poderem chamar
    public void setBaseLocation(Point baseLocation) {
        this.baseLocation = baseLocation;
        hasInvalidLocation = false;
    }

    protected Point getLocation() {
        return getChildren().get(0).getLocation();
    }

    protected Point getSpacedBaseLocation() {
        int x = baseLocation.x + content.getLeftMargin(lineIndex);
        int paragraphSpacing = lineIndex == 0 ? 0 : getParagraphSpacing();
        int y = baseLocation.y + paragraphSpacing;
        return new Point(x, y);
    }

    protected Point getBaseLocation() {
        return this.baseLocation;
    }

    protected Point getContinuousBaseLocation() {
        return continuousLocation;
    }

    protected Point getContinuousLocation() {
        int x = continuousLocation.x + content.getLeftMargin(lineIndex);
        int y = continuousLocation.y + getParagraphSpacing();
        return new Point(x, y);
    }

    protected void setContinuousBaseLocation(Point continuousLocation) {
        this.continuousLocation = continuousLocation;
    }

    public void dispose() {
        textLayout.dispose();
        calculator.dispose();
        calculator = null;
    }

    public void changeLineIndex(int newLineIndex) {
        lineIndex = newLineIndex;
        hasInvalidLocation = true;
        fullHeight = -1;
    }

    public void computeChildren(boolean forceUpdate, int availableHeight, int breakSize,
            int pageHeight, int minimumLines, int blockSize) {
        calculator.update(forceUpdate, availableHeight, breakSize, pageHeight, minimumLines,
                          blockSize);
    }

    public List<ExtendedTextLayout> getChildren() {
        return calculator.getChildren();
    }

    public int getLineIndex() {
        return lineIndex;
    }

    // FIXME Testes
    public ExtendedTextLayout getChild(int relativeOffset) {
        for (ExtendedTextLayout child : getChildren())
            if (relativeOffset < child.getOffset() + child.getText().length())
                return child;

        return getChildren().get(getChildren().size() - 1);
    }

    // FIXME Testes
    public ExtendedTextLayout getChild(int x, int y) {
        int startLocation = getLocation().y;
        for (ExtendedTextLayout child : getChildren())
            if (y <= child.getLocation().y + child.getHeight() - startLocation)
                return child;

        return getChildren().get(getChildren().size() - 1);
    }

    public boolean hasInvalidLocation() {
        return hasInvalidLocation || getBaseLocation() == null;
    }

    public boolean hasInvalidText() {
        return textInvalid;
    }

    public void setLocationValid() {
        hasInvalidLocation = false;
    }

    public int getBreakDecorationSize() {
        return content.getBreakDecorationSize(lineIndex);
    }
}
