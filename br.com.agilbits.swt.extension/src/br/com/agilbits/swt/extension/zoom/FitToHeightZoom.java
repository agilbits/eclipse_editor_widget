package br.com.agilbits.swt.extension.zoom;

import org.eclipse.swt.custom.ExtendedStyledText;

public class FitToHeightZoom implements ZoomOption {
    public static final Float CODE = -1f;

    public double getScalingFactor(ExtendedStyledText styledText) {
        double fullHeight = styledText.getPageHeight() + 2 * ExtendedStyledText.EXTERNAL_PAGE_MARGIN;
        return styledText.getClientArea().height / fullHeight;
    }

    @Override
    public int hashCode() {
        return 17;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        return true;
    }

    public Float getZoomCode() {
        return CODE;
    }
}
