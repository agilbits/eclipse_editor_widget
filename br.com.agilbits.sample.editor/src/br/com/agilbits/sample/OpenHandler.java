package br.com.agilbits.sample;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

public class OpenHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) {
        Shell shell = HandlerUtil.getActiveShell(event);
        FileSelector selector = new FileSelector(shell);
        File file = selector.open();
        if (file != null) {
            IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
            SampleEditorInput input = SampleEditorInputFactory.createEditorInput(file);
            try {
                page.openEditor(input, SampleEditor.ID);
            }
            catch (PartInitException e) {
                e.printStackTrace();
            }
        }
        
        return null;
    }
}
