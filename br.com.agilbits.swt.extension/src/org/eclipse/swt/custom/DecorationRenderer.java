package org.eclipse.swt.custom;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;

public interface DecorationRenderer {
    public void drawHeader(GC gc, ExtendedStyledText styledText, int offset, int pageStartY,
            Rectangle trim);

    public void decorateLine(GC gc, int layoutOffset, Point layoutLocation,
            TextLayout layout, Rectangle trim);

    public void drawFooter(GC gc, ExtendedStyledText styledText, int offset, int pageEndY,
            Rectangle trim);
}
