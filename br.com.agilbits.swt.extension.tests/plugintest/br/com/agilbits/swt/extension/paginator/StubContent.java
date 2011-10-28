package br.com.agilbits.swt.extension.paginator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.custom.IExtendedStyledTextContent;
import org.eclipse.swt.custom.TextChangeListener;

public class StubContent implements IExtendedStyledTextContent {
    private List<String> lines;
    private List<Integer> spacings;
    private List<Boolean> canBreaks;
    private List<Boolean> canBreakAfters;
    private List<Integer> minimumLines;
    private List<Boolean> mergeWithNexts;

    public StubContent(int numberOfLines, String lineContent) {
        lines = new ArrayList<String>(numberOfLines);
        spacings = new ArrayList<Integer>(numberOfLines);
        canBreaks = new ArrayList<Boolean>(numberOfLines);
        canBreakAfters = new ArrayList<Boolean>(numberOfLines);
        minimumLines = new ArrayList<Integer>(numberOfLines);
        mergeWithNexts = new ArrayList<Boolean>(numberOfLines);
        for (int i = 0; i < numberOfLines; i++) 
            addLine(i, lineContent);
    }

    public int getLineCount() {
        return lines.size();
    }

    public int getCharCount() {
        int count = -1;
        for (String line : lines)
            count += line.length() + 1;
        return count;
    }

    public String getLineDelimiter() {
        return "\n";
    }

    public String getLine(int lineIndex) {
        return lines.get(lineIndex);
    }

    public int getLineAtOffset(int offset) {
        int currentOffset = 0;
        for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
            int lineEnd = currentOffset + lines.get(lineIndex).length();
            if (currentOffset <= offset && offset <= lineEnd)
                return lineIndex;
            currentOffset = lineEnd + 1;
        }
        return -1;
    }

    public int getOffsetAtLine(int lineIndex) {
        return 0;
    }

    public String getTextRange(int start, int length) {
        return null;
    }

    public void replaceTextRange(int start, int replaceLength, String text) {}

    public void setText(String text) {}

    public int getLeftMargin(int lineIndex) {
        return 0;
    }

    public int getRightMargin(int lineIndex) {
        return 100;
    }

    public int getLineSpacing(int lineIndex) {
        return 0;
    }

    public int getParagraphSpacing(int lineIndex) {
        return spacings.get(lineIndex);
    }

    public int getParagraphAlignment(int lineIndex) {
        return 0;
    }

    public void addTextChangeListener(TextChangeListener listener) {}

    public void removeTextChangeListener(TextChangeListener listener) {}

    public void changeTextForLine(int lineIndex, String lineText) {
        lines.set(lineIndex, lineText);
    }

    public void removeLine(int lineIndex) {
        lines.remove(lineIndex);
        spacings.remove(lineIndex);
        canBreaks.remove(lineIndex);
        canBreakAfters.remove(lineIndex);
        minimumLines.remove(lineIndex);
        mergeWithNexts.remove(lineIndex);
    }

    public void setParagraphSpacing(int lineIndex, int spacing) {
        spacings.set(lineIndex, spacing);
    }

    public void addLine(int lineIndex, String text) {
        lines.add(lineIndex, text);
        spacings.add(lineIndex, 0);
        canBreaks.add(lineIndex, true);
        canBreakAfters.add(lineIndex, true);
        minimumLines.add(lineIndex, 1);
        mergeWithNexts.add(lineIndex, false);
    }

    public boolean shouldMergeWithNext(int lineIndex) {
        return mergeWithNexts.get(lineIndex);
    }

    public int getBreakDecorationSize(int lineIndex) {
        return 0;
    }

    @Override
    public boolean canBreak(int lineIndex) {
        return canBreaks.get(lineIndex);
    }

    @Override
    public boolean canBreakAfter(int lineIndex) {
        return canBreakAfters.get(lineIndex);
    }

    @Override
    public int getMinimumLinesToFit(int lineIndex) {
        return minimumLines.get(lineIndex);
    }

    public void setCanBreak(int lineIndex, boolean canBreak) {
        canBreaks.set(lineIndex, canBreak);
    }

    public void setCanBreakAfter(int lineIndex, boolean canBreakAfter) {
        canBreakAfters.set(lineIndex, canBreakAfter);
    }

    public void setMinimumLines(int lineIndex, int minimum) {
        minimumLines.set(lineIndex, minimum);
    }

    public void setMergeWithNext(int lineIndex, boolean merge) {
        mergeWithNexts.set(lineIndex, merge);
    }
}
