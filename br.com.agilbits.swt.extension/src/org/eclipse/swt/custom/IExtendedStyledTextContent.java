package org.eclipse.swt.custom;

public interface IExtendedStyledTextContent extends StyledTextContent, Cloneable {
    /**
     * @return The number of pixels in the left margin
     */
    int getLeftMargin(int lineIndex);

    /**
     * @return The number of pixels of the right margin relative to the left of the page
     */
    int getRightMargin(int lineIndex);

    /**
     * @return the line spacing (for wrapped, screen-visible lines) in number of lines
     */
    int getLineSpacing(int lineIndex);

    /**
     * @return the paragraph (logical, document line) spacing in number of lines
     */
    int getParagraphSpacing(int lineIndex);

    /**
     * @return the alignment (SWT.LEFT, SWT.CENTER or SWT.RIGHT) of the specified line
     */
    int getParagraphAlignment(int lineIndex);

    /**
     * Decides whether the logical line is part of a block with the next one.
     * 
     * @return true if this logical line is a block with the next one,<br/>
     *         false if there is no next one or if it is not part of a block and can be separated by
     *         a page break
     */
    boolean shouldMergeWithNext(int lineIndex);

    /**
     * Decides whether the logical line can be broken within itself.
     * 
     * @return true if this logical line can be broken,<br/>
     *         false if it should be displayed as a single block
     */
    boolean canBreak(int lineIndex);

    /**
     * Decides whether the logical line allows breaking after itself. This method is only called if
     * canBreak returns false for this lineIndex.
     * 
     * @return true if page breaks are allowed after the text of this logical line,<br/>
     *         false otherwise.
     */
    boolean canBreakAfter(int lineIndex);

    /**
     * Informs how many lines for a breakable logical line are minimum to include part of its text
     * in the page.
     * 
     * @return the number of lines required to be shown on the current page in order not to push
     *         this logical line. If the lines available are less than this number, this logical
     *         line will only be drawn on the next page.
     */
    int getMinimumLinesToFit(int lineIndex);

    int getBreakDecorationSize(int lineIndex);
}
