package br.com.agilbits.sample;

import java.io.File;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;

public class SampleEditorInputFactory implements IElementFactory {
    private static final String ID = "br.com.agilbits.sample.SampleEditorInputFactory"; //$NON-NLS-1$
    private static final String FILE_PATH = "SamplePath"; //$NON-NLS-1$

    public static String getFactoryId() {
        return ID;
    }

    public static void saveState(IMemento memento, SampleEditorInput input) {
        File file = input.getFile();
        if(file != null)
            memento.putString(FILE_PATH, file.getAbsolutePath().toString());
    }

    public IAdaptable createElement(IMemento memento) {
        return createEditorInput(new File(memento.getString(FILE_PATH)));
    }

    public static SampleEditorInput createEditorInput(File file) {
        return new SampleEditorInput(file);
    }
}
