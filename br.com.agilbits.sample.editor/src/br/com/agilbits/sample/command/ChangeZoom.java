package br.com.agilbits.sample.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

import br.com.agilbits.sample.SampleEditor;

public class ChangeZoom extends AbstractHandler {
    public Object execute(ExecutionEvent event) throws ExecutionException {
        SampleEditor activeEditor = (SampleEditor) HandlerUtil.getActiveEditor(event);
        activeEditor.changeZoom();
        return null;
    }
}
