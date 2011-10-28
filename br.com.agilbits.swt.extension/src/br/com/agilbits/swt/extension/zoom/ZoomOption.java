package br.com.agilbits.swt.extension.zoom;

import org.eclipse.swt.custom.ExtendedStyledText;

public interface ZoomOption {
    public double getScalingFactor(ExtendedStyledText styledText);
    
    public boolean equals(Object other);
    
    public int hashCode();
    
    public Float getZoomCode();
}
