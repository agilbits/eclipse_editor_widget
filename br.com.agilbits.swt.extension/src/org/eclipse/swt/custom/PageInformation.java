package org.eclipse.swt.custom;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

public class PageInformation {
    private Point dpi;
    private double width;
    private double height;

    private double topMargin;
    private double bottomMargin;
    private boolean shouldDrawPageBreaks;

    public PageInformation(Point dotsPerInch, double widthInInches, double heightInInches) {
        this.dpi = dotsPerInch;
        this.width = widthInInches;
        this.height = heightInInches;
        this.shouldDrawPageBreaks = false;
    }

    public PageInformation(double widthInInches, double heightInInches) {
        this(Display.getDefault().getDPI(), widthInInches, heightInInches);
    }

    public int getPageWidth() {
        return (int) Math.round(dpi.x * width);
    }

    public int getPageHeight() {
        return (int) Math.round(dpi.y * height);
    }

    public int getAvailableHeight() {
        return getPageHeight() - getTopMargin() - getBottomMargin();
    }

    public void setTopMargin(double topMargin) {
        this.topMargin = topMargin;
    }

    public int getTopMargin() {
        return (int) Math.round(dpi.y * topMargin);
    }

    public void setBottomMargin(double bottomMargin) {
        this.bottomMargin = bottomMargin;
    }

    public int getBottomMargin() {
        return (int) Math.round(dpi.y * bottomMargin);
    }

    public boolean shouldDrawPageBreaks() {
        return shouldDrawPageBreaks;
    }

    public void setDrawPageBreaks(boolean shouldDrawPages) {
        this.shouldDrawPageBreaks = shouldDrawPages;
    }

    public PageInformation copy() {
        PageInformation copy = new PageInformation(dpi, width, height);
        copy.topMargin = topMargin;
        copy.bottomMargin = bottomMargin;
        return copy;
    }
}
