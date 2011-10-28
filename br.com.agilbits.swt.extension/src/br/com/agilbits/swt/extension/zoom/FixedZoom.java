package br.com.agilbits.swt.extension.zoom;

import org.eclipse.swt.custom.ExtendedStyledText;

public class FixedZoom implements ZoomOption {
    private static final double EPSILON = 1e-3;
    private double zoom;

    public FixedZoom(double zoom) {
        this.zoom = zoom;
    }

    public double getScalingFactor(ExtendedStyledText styledText) {
        return zoom;
    }

    @Override
    public int hashCode() {
        long temp = (long) (zoom / EPSILON);
        return 31 + (int) (temp ^ (temp >>> 32));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FixedZoom other = (FixedZoom) obj;
        return Math.abs(zoom - other.zoom) <= EPSILON;
    }

    public Float getZoomCode() {
        return (float) zoom;
    }
}
