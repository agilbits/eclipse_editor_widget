package br.com.agilbits.sample.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

import br.com.agilbits.sample.SampleEditor;

public class AddHelloWorld extends AbstractHandler {
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IEditorPart editor = HandlerUtil.getActiveEditor(event);
        if (editor != null) {
            SampleEditor sampleEditor = (SampleEditor) editor;
            int caret = sampleEditor.getCaretOffset();
            try {
                sampleEditor.getDocument().replace(caret, 0, "\nHello World\n");
            }
            catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
