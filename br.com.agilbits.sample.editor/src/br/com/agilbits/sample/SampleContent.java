package br.com.agilbits.sample;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentAdapter;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.IExtendedStyledTextContent;
import org.eclipse.swt.custom.TextChangeListener;
import org.eclipse.swt.custom.TextChangedEvent;
import org.eclipse.swt.custom.TextChangingEvent;

public class SampleContent implements IDocumentAdapter, IDocumentListener,
		IExtendedStyledTextContent {
	private IDocument document;
	private List<TextChangeListener> listeners;
	private DocumentEvent currentEvent;
	private int previousLength;
	private DocumentEvent mementoEvent;

	public SampleContent() {
		listeners = new LinkedList<TextChangeListener>();
		mementoEvent = new DocumentEvent();
	}

	public int getLeftMargin(int lineIndex) {
		switch (lineIndex % 20) {
		case 4:
		case 7:
		case 9:
		case 11:
			return 20 * 8;
		case 5:
			return 15 * 9;
		case 6:
		case 8:
		case 10:
		case 12:
			return 10 * 10;
		default:
			return 20;
		}
	}

	public int getRightMargin(int lineIndex) {
		return 75 * 10;
	}

	public int getLineSpacing(int lineIndex) {
		switch (lineIndex % 20) {
		case 9:
		case 2:
		case 1:
		case 4:
		case 15:
			return 2;
		case 5:
		case 10:
		case 18:
		case 19:
			return 3;
		default:
			return 1;
		}
	}

	public int getParagraphAlignment(int lineIndex) {
		return SWT.LEFT;
	}

	public int getParagraphSpacing(int lineIndex) {
		switch (lineIndex % 20) {
		case 0:
		case 1:
			return 1;
		case 2:
		case 14:
			return 2;
		case 5:
		case 6:
		case 8:
		case 10:
		case 12:
			return 0;
		default:
			return 1;
		}
	}

	public void setDocument(IDocument document) {
		if (this.document != null)
			this.document.removePrenotifiedDocumentListener(this);

		this.document = document;

		if (this.document != null)
			this.document.addPrenotifiedDocumentListener(this);
	}

	public void addTextChangeListener(TextChangeListener listener) {
		this.listeners.add(listener);
	}

	public int getCharCount() {
		if (document == null)
			return 0;
		return document.getLength();
	}

	public String getLine(int lineIndex) {
		try {
			IRegion lineRegion = document.getLineInformation(lineIndex);
			return document.get(lineRegion.getOffset(), lineRegion.getLength());
		} catch (Throwable e) {
			e.printStackTrace();
			return "";
		}
	}

	public int getLineAtOffset(int offset) {
		try {
			return document.getLineOfOffset(offset);
		} catch (Throwable e) {
			e.printStackTrace();
			return 0;
		}
	}

	public int getLineCount() {
		if (document == null)
			return 1;
		return document.getNumberOfLines();
	}

	public String getLineDelimiter() {
		return "\n";
	}

	public int getOffsetAtLine(int lineIndex) {
		try {
			return document.getLineOffset(lineIndex);
		} catch (Throwable e) {
			System.out.println("Retornou 0"); //$NON-NLS-1$
			e.printStackTrace();
			return 0;
		}
	}

	public String getTextRange(int start, int length) {
		try {
			return document.get(start, length);
		} catch (Throwable e) {
			System.out.println("Retornou 0"); //$NON-NLS-1$
			e.printStackTrace();
			return ""; //$NON-NLS-1$
		}
	}

	public void removeTextChangeListener(TextChangeListener listener) {
		this.listeners.remove(listener);
	}

	public void replaceTextRange(int start, int replaceLength, String text) {
		try {
			document.replace(start, replaceLength, text);
		} catch (Throwable e) {
			System.out.println("replace text range do scriptcontent fez nada"); //$NON-NLS-1$
			e.printStackTrace();
		}
	}

	public void setText(String text) {
		if (document != null)
			document.set(text);
	}

	/*
	 * @see IDocumentListener#documentChanged(DocumentEvent)
	 */
	public void documentChanged(DocumentEvent event) {
		// check whether the given event is the one which was remembered
		if (currentEvent == null || event != currentEvent)
			return;

		if (isPatchedEvent(event)
				|| (event.getOffset() == 0 && event.getLength() == previousLength))
			fireTextSet();
		else
			fireTextChanged();
	}

	/*
	 * @see IDocumentListener#documentAboutToBeChanged(DocumentEvent)
	 */
	public void documentAboutToBeChanged(DocumentEvent event) {

		previousLength = document.getLength();
		currentEvent = event;
		rememberEventData(currentEvent);
		fireTextChanging();
	}

	/**
	 * Checks whether this event has been changed between
	 * <code>documentAboutToBeChanged</code> and <code>documentChanged</code>.
	 * 
	 * @param event
	 *            the event to be checked
	 * @return <code>true</code> if the event has been changed,
	 *         <code>false</code> otherwise
	 */
	private boolean isPatchedEvent(DocumentEvent event) {
		return mementoEvent.fOffset != event.fOffset
				|| mementoEvent.fLength != event.fLength
				|| mementoEvent.fText != event.fText;
	}

	/**
	 * Makes a copy of the given event and remembers it.
	 * 
	 * @param event
	 *            the event to be copied
	 */
	private void rememberEventData(DocumentEvent event) {
		mementoEvent.fOffset = event.fOffset;
		mementoEvent.fLength = event.fLength;
		mementoEvent.fText = event.fText;
	}

	/**
	 * Sends a text changed event to all registered listeners.
	 */
	private void fireTextChanged() {
		TextChangedEvent event = new TextChangedEvent(this);

		for (TextChangeListener listener : listeners) {
			listener.textChanged(event);
		}
	}

	/**
	 * Sends a text set event to all registered listeners.
	 */
	private void fireTextSet() {
		TextChangedEvent event = new TextChangedEvent(this);

		for (TextChangeListener listener : listeners) {
			listener.textSet(event);
		}
	}

	/**
	 * Sends the text changing event to all registered listeners.
	 */
	private void fireTextChanging() {

		try {
			IDocument document = currentEvent.getDocument();
			if (document == null)
				return;

			TextChangingEvent event = new TextChangingEvent(this);
			event.start = currentEvent.fOffset;
			event.replaceCharCount = currentEvent.fLength;
			event.replaceLineCount = document.getNumberOfLines(
					currentEvent.fOffset, currentEvent.fLength) - 1;
			event.newText = currentEvent.fText;
			event.newCharCount = (currentEvent.fText == null ? 0
					: currentEvent.fText.length());
			event.newLineCount = (currentEvent.fText == null ? 0 : document
					.computeNumberOfLines(currentEvent.fText));

			for (TextChangeListener listener : listeners) {
				listener.textChanging(event);
			}
		} catch (BadLocationException e) {
		}
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public boolean shouldMergeWithNext(int lineIndex) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean canBreak(int lineIndex) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean canBreakAfter(int lineIndex) {
		// TODO Auto-generated method stub
		return false;
	}

	public int getMinimumLinesToFit(int lineIndex) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getBreakDecorationSize(int lineIndex) {
		// TODO Auto-generated method stub
		return 0;
	}
}
