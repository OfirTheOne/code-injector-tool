package analyzerUtils;

import java.io.File;
import java.io.IOException;

import com.google.common.base.Charsets;
import com.google.common.io.CharSink;
import com.google.common.io.CharSource;
import com.google.common.io.Files;
import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;

public class CodeReformatter {
	
	public static void googleCodeReformater(String filePath, String outputPath) {
		CharSource source = Files.asCharSource(new File(filePath), Charsets.UTF_8);
		CharSink output = Files.asCharSink(new File(outputPath), Charsets.UTF_8);
		try {
			new Formatter().formatSource(source, output);
		} catch (FormatterException e) {
			System.err.println("Error in formating action - FormatterException, " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Error in formating action - IOException, " + e.getMessage());
			e.printStackTrace();
		}
	}
}
