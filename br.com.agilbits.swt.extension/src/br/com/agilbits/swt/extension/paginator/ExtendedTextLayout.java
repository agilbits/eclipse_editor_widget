package br.com.agilbits.swt.extension.paginator;

import org.eclipse.swt.custom.StyleRangesApplier;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;

public class ExtendedTextLayout implements Disposable {
    private LogicalLine logicalLine;
    private String text;
    private Point location;
    private boolean hasPageBreak;
    private int offset;

    private TextLayout layout;
    private int height;

    public ExtendedTextLayout(LogicalLine line, String text, Point location, boolean hasPageBreak,
            int offset) {
        this.logicalLine = line;
        this.text = text;
        this.location = location;
        this.hasPageBreak = hasPageBreak;
        this.offset = offset;
    }

    private void initializeLayout() {
        TextLayout masterLayout = logicalLine.getTextLayout();
        this.layout = new TextLayout(masterLayout.getDevice());
        this.layout.setText(text);
        updateLayout(masterLayout);
    }

    public String getText() {
        return text;
    }

    public Point getLocation() {
        Point lineLocation = logicalLine.getSpacedBaseLocation();
        return new Point(lineLocation.x + location.x, lineLocation.y + location.y);
    }

    public int getHeight() {
        if (layout == null)
            initializeLayout();
        return height;
    }

    public TextLayout getLayout() {
        if (layout == null)
            initializeLayout();
        else
            // Needed because the applier might change my text to capitalize it
            layout.setText(getText());
        return layout;
    }

    public TextLayout getTextLayout(StyleRangesApplier applier, int lineOffset) {
        // FIXME Handle styles correctly
        TextLayout textLayout = getLayout();
        textLayout.setStyle(new TextStyle(), 0, getText().length());
        applier.applyStyles(lineOffset + offset, getText(), textLayout);
        return textLayout;
    }

    public boolean hasPageBreak() {
        return hasPageBreak;
    }

    public int getOffset() {
        return offset;
    }

    public void dispose() {
        if (layout != null)
            layout.dispose();
    }

    public void setPageBreak(boolean newPageBreak) {
        hasPageBreak = newPageBreak;
    }

    public void setOffset(int newOffset) {
        offset = newOffset;
    }

    public void setLocation(Point newLocation) {
        location = newLocation;
    }

    public void updateLayout(TextLayout masterLayout) {
        if (layout != null) {
            Font font = masterLayout.getFont();
            this.layout.setFont(font);
            this.layout.setWidth(masterLayout.getWidth());
            this.layout.setAlignment(masterLayout.getAlignment());
//            if (font != null)
//                this.layout.setMaximumLineHeight(font.getFontData()[0].getHeight());
            height = layout.getBounds().height;
        }
    }

    public void setText(String newText) {
        if (!getText().equals(newText)) {
            text = newText;
            if (layout != null) {
                layout.setText(newText);
                height = layout.getBounds().height;
            }
        }
    }
}
