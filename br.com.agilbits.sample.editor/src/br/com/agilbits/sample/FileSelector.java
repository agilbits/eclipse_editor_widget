package br.com.agilbits.sample;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public class FileSelector {
    private static final String TXT = ".txt"; //$NON-NLS-1$
    private static final String USER_DIR = "user.dir"; //$NON-NLS-1$
    private static final String LAST_DIR = "last.dir"; //$NON-NLS-1$

    static {
        System.setProperty(LAST_DIR, System.getProperty(USER_DIR));
    }

    private FileDialog dialog;

    public FileSelector(Shell shell) {
        dialog = new FileDialog(shell, SWT.OPEN);
        dialog.setText("Open");
        dialog.setFilterExtensions(new String[] { "*" + TXT }); //$NON-NLS-1$
        dialog.setFilterPath(System.getProperty(LAST_DIR));
    }

    public File open() {
        String path = dialog.open();
        if (path == null || path.length() == 0)
            return null;

        File file = new File(path);
        System.setProperty(LAST_DIR, file.getParent());
        return file;
    }
}
