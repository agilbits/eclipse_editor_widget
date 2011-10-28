package org.eclipse.swt.custom;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public class ScaleHelper {
    private ExtendedStyledText styledText;

    public ScaleHelper(ExtendedStyledText styledText) {
        this.styledText = styledText;
    }

    public int scale(double value) {
        return (int) (value * styledText.getScalingFactor());
    }

    public Point scalePoint(int x, int y) {
        return new Point(scale(x), scale(y));
    }

    public Rectangle scale(Rectangle rectangle) {
        return new Rectangle(scale(rectangle.x), scale(rectangle.y), scale(rectangle.width),
                             scale(rectangle.height));
    }

    public int unscale(int value) {
        return (int) (value / styledText.getScalingFactor());
    }

    public Point unscalePoint(int x, int y) {
        return new Point(unscale(x), unscale(y));
    }

    public Rectangle unscale(Rectangle rectangle) {
        return new Rectangle(unscale(rectangle.x), unscale(rectangle.y), unscale(rectangle.width),
                             unscale(rectangle.height));
    }

    public double rawUnscale(double value) {
        return value / styledText.getScalingFactor();
    }
}
