package br.com.agilbits.sample;

import java.io.File;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

public class SampleEditorInput implements IEditorInput, IPersistableElement {
    private File file;

    public SampleEditorInput(File file) {
        this.file = file;
    }

    public boolean exists() {
        return true;
    }

    public ImageDescriptor getImageDescriptor() {
        return ImageDescriptor.getMissingImageDescriptor();
    }

    public String getName() {
        return "sample editor input";
    }

    public IPersistableElement getPersistable() {
        return this;
    }

    public String getToolTipText() {
        return "this is the sample editor input";
    }

    @SuppressWarnings("rawtypes")
    public Object getAdapter(Class adapter) {
        return null;
    }

    public String getFactoryId() {
        return SampleEditorInputFactory.getFactoryId();
    }

    public void saveState(IMemento memento) {
        SampleEditorInputFactory.saveState(memento, this);
    }

    public File getFile() {
        return this.file;
    }
}
