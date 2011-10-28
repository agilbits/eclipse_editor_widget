package br.com.agilbits.sample.app;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class Activator extends AbstractUIPlugin {

    private static final String ID = "ExtendedStyledTextRCP";

    public static ImageDescriptor getImageDescriptor(String imageName) {
        return imageDescriptorFromPlugin(ID, "icons/" + imageName + ".png");
    }
}
