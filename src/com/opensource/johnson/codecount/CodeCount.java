package com.opensource.johnson.codecount;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Hello world!
 *
 */
public class CodeCount 
{
	public String sourceDirName;
	public String ignoreFileName;
	public String outputFileName;
	public ArrayList<AnalysisResult> analysisResults = new ArrayList<AnalysisResult>();
	
    public static void main( String[] args )
    {
    	CodeCount cc = new CodeCount();
        Options options = new Options();
        options.addOption("h", false, "帮助");
        options.addOption("d", true, "指定要统计的文件夹");
        options.addOption("i", true, "指定忽略的文件格式文件");
        options.addOption("o", true, "指定统计输出结果的csv文件名");
        
        CommandLineParser parse = new GnuParser();
        CommandLine lines = null;
        try {
			lines = parse.parse(options, args);
		} catch (ParseException e) {
			e.printStackTrace();
		} finally {
			if(null == lines){
				System.exit(1);
			}
		}
        HelpFormatter formatter = new HelpFormatter();
        
        if(!lines.hasOption("d")){
        	formatter.printHelp( "java -jar CodeCount.jar [options] -d <dir>", options );
        	System.exit(1);
        }else{
        	cc.sourceDirName = lines.getOptionValue("d");
        }
        
        if(lines.hasOption("i")){
        	cc.ignoreFileName = lines.getOptionValue("i");
        }
        
        cc.count();
        
        if(lines.hasOption("o")){
        	cc.outputFileName = lines.getOptionValue("o");
        	if(!cc.outputFileName.equalsIgnoreCase("") && cc.outputFileName.endsWith(".csv")){
        		cc.exportCsvFile();
        	}else{
        		formatter.printHelp( "java -jar CodeCount.jar [options] -d <dir>", options );
            	System.exit(1);
        	}
        }
        
        cc.printResult();
    }
    
    public void exportCsvFile(){
    	PrintWriter bw = null;
    	String fileName = "";
    	long commentLines = 0, commentTotalLines = 0;
    	long blankLines = 0, blankTotalLines = 0;
    	long codeLines = 0, codeTotalLines = 0;
    	long totalLines = 0, tTotalLines = 0;
    	try {
			bw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.outputFileName),"GB2312")));
			bw.println("文件名,代码行,注释行,空行,总行数");
			for(AnalysisResult ar: analysisResults){
				fileName = ar.getFileName();
				commentLines = ar.getCommentLines();
				blankLines   = ar.getBlankLines();
				codeLines	 = ar.getCodeLines();
				totalLines	 = ar.getTotalLines();
				bw.println(fileName + "," +codeLines + "," + commentLines + "," + blankLines + "," + totalLines);
				commentTotalLines += commentLines;
				blankTotalLines  += blankLines;
				codeTotalLines  += codeLines;
				tTotalLines += totalLines;
			}
			bw.println(" ," + codeTotalLines + "," + commentTotalLines + "," + blankTotalLines + "," + tTotalLines);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(bw != null){
				bw.close();
			}
		}
    }
    
    public void printResult(){
    	long comments = 0;
    	long blanks = 0;
    	long codes = 0;
    	long totals = 0;
    	for(AnalysisResult ar: analysisResults){
    		comments += ar.getCommentLines();
    		blanks   += ar.getBlankLines();
    		codes    += ar.getCodeLines();
    		totals	 += ar.getTotalLines();
    		System.out.println(ar.getFileName() + 
    				": codeLines(" + ar.getCodeLines() + ") " +
    				"commentLines(" + ar.getCommentLines() + ") " +
    				"blankLines(" + ar.getBlankLines() + ") " +
    				"totalLines(" + ar.getTotalLines() + ")");
    	}
    	System.out.println("---------------------totals---------------------------");
    	System.out.println("Totals:" + 
				"codeLines(" + codes + ") " +
				"commentLines(" + comments + ") " +
				"blankLines(" + blanks + ") " +
				"totalLines(" + totals + ")");
    }

	private void count() {
		File sourceDir = new File(this.sourceDirName);
		if(!sourceDir.exists())	return;
		
		recurrenceCount(sourceDir);

	}

	private void recurrenceCount(File sourceDir) {
		if(sourceDir.isDirectory()){
			File[] files = sourceDir.listFiles();
			for(File f : files){
				if(f.isDirectory()){
					recurrenceCount(f);
				}else{
					analysisFile(f);
				}
			}
		}else{
			analysisFile(sourceDir);
		}	
	}

	private void analysisFile(File sourceDir) {
		BufferedReader br = null; 
		AnalysisResult ar = null;
		try {
			br = new BufferedReader(new FileReader(sourceDir));
			ar = new AnalysisResult(sourceDir.getCanonicalPath());
			analysisResults.add(ar);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Pattern commentSingalLinePattern = Pattern.compile("(^//.*)|(^/[*].*[*]/$)");
		Pattern commentStartLinePattern = Pattern.compile("^/[*].*");
		Pattern commentEndLinePattern = Pattern.compile("[*]/$");
		Pattern blankLinePattern = Pattern.compile("^[\\s&&[^\\n]]*$");
		boolean commentFlag = false;
		String line = null;
		try {
			while((line = br.readLine()) != null){
				line = line.trim();
//				System.out.println(line);
				if(commentFlag){
					ar.setCommentLines(ar.getCommentLines() + 1);
					if(commentEndLinePattern.matcher(line).find()){
						commentFlag = false;
					}
				}else if(commentStartLinePattern.matcher(line).find()){
					ar.setCommentLines(ar.getCommentLines() + 1);
					commentFlag = true;
				}else if(commentSingalLinePattern.matcher(line).find()){
					ar.setCommentLines(ar.getCommentLines() + 1);
				}else if(blankLinePattern.matcher(line).find()){
					ar.setBlankLines(ar.getBlankLines() + 1);
				}else{
					ar.setCodeLines(ar.getCodeLines() + 1);
				}
				ar.setTotalLines(ar.getTotalLines() + 1);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(br != null){
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
