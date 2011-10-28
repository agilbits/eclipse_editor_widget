package br.com.agilbits.swt.extension.zoom;

import org.eclipse.swt.custom.ExtendedStyledText;

public class FitToWidthZoom implements ZoomOption {
    public static final Float CODE = -2f;
    
    public double getScalingFactor(ExtendedStyledText styledText) {
        double fullWidth = styledText.getPageWidth() + 2 * ExtendedStyledText.EXTERNAL_PAGE_MARGIN;
        return styledText.getClientArea().width / fullWidth;
    }

    @Override
    public int hashCode() {
        return 13;
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
