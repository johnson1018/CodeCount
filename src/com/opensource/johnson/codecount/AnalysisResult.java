package com.opensource.johnson.codecount;

public class AnalysisResult {
	private long commentLines;
	private long blankLines;
	private long codeLines;
	private long totalLines;
	private String fileName;
	
	public AnalysisResult(String fileName) {
		this.fileName = fileName;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public long getCommentLines() {
		return commentLines;
	}
	public void setCommentLines(long commentLines) {
		this.commentLines = commentLines;
	}
	public long getBlankLines() {
		return blankLines;
	}
	public void setBlankLines(long blankLines) {
		this.blankLines = blankLines;
	}
	public long getCodeLines() {
		return codeLines;
	}
	public void setCodeLines(long codeLines) {
		this.codeLines = codeLines;
	}
	public long getTotalLines() {
		return totalLines;
	}
	public void setTotalLines(long totalLines) {
		this.totalLines = totalLines;
	}
	
	
}
