package br.com.agilbits.sample.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import br.com.agilbits.sample.SampleEditor;

public class ChangeFont extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        SampleEditor editor = (SampleEditor) HandlerUtil.getActiveEditor(event);

        Shell shell = HandlerUtil.getActiveShell(event);
        FontDialog dialog = new FontDialog(shell);
        dialog.setFontList(editor.getFont().getFontData());
        FontData fontData = dialog.open();
        
        editor.setFont(new Font(shell.getDisplay(), fontData));
        
        return null;
    }
}
