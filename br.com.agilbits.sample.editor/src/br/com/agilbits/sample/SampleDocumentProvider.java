package br.com.agilbits.sample;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.texteditor.AbstractDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class SampleDocumentProvider extends AbstractDocumentProvider implements IDocumentProvider {

    @Override
    protected IAnnotationModel createAnnotationModel(Object element) throws CoreException {
        return new AnnotationModel();
    }

    @Override
    protected IDocument createDocument(Object element) throws CoreException {
        SampleEditorInput input = (SampleEditorInput) element;
        String content = getContent(input.getFile());
        Document document = new Document(content) {
            @Override
            public int getLineOffset(int line) throws BadLocationException {
                if (line == -1)
                    return 0;
                return super.getLineOffset(line);
            }
        };
        return document;
    }

    private String getContent(File file) {
        try {
            FileReader reader = new FileReader(file);
            int character;
            StringBuilder builder = new StringBuilder();
            while ((character = reader.read()) >= 0) {
                char c = (char) character;
                builder.append(c);
            }
                
            return builder.toString();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    protected void doSaveDocument(IProgressMonitor monitor, Object element, IDocument document,
            boolean overwrite) throws CoreException {}

    @Override
    protected IRunnableContext getOperationRunner(IProgressMonitor monitor) {
        return null;
    }
}
