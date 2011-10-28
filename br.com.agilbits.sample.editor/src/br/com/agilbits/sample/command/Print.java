package br.com.agilbits.sample.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

import br.com.agilbits.sample.SampleEditor;

public class Print extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        IEditorPart uncastEditor = HandlerUtil.getActiveEditor(event);
        SampleEditor editor = (SampleEditor) uncastEditor;

        StyledText fTextWidget = editor.getTextWidget();
        final Shell shell = fTextWidget.getShell();

        if (Printer.getPrinterList().length == 0) {
            String title = "Sem impressora";
            String msg = "Ai nao da pra imprimi neh?";
            MessageDialog.openWarning(shell, title, msg);
            return null;
        }

        final PrintDialog dialog = new PrintDialog(shell, SWT.PRIMARY_MODAL);
        final PrinterData data = dialog.open();

        if (data != null) {
            Printer printer = new Printer(data);
            Runnable styledTextPrinter = fTextWidget.print(printer);
            styledTextPrinter.run();
            printer.dispose();
        }

        return null;
    }
}
