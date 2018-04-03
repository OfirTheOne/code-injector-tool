package analyzerUtils;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;

/**
 * TODO : runProgram METHOD IN SOME CASES DOSE NOT WORKING
 * as good practice : the use in java.io.File will be done with java.nio.Path
 * class insted (recomended by Oracle) . the use in String class will be done
 * with the class java.lang.StringBuffer (more efficient) .
 */

public class JavaOperations {

	/**
	 * compile .java file by execute 'javac'.
	 * 
	 * @param classNameWithPath
	 *            path and name of the class file to compile, must end with '.java'
	 * @param dependencyJars
	 *            the essensial jars needed by the file to compile
	 * @param outputFilePath
	 *            path and name of file to write the output stream and error
	 *            stream of the running program
	 * @see outputFilePath
	 * @throws Exception
	 *             from the method execCommandWriteStreamToFile
	 **/
	private static void compileCode(Path fileCodeName, Path[] dependencyJars, Path outputFilePath) throws Exception {
		StringBuffer command = new StringBuffer();
		command.append("javac ");
		if (dependencyJars != null && dependencyJars.length > 0) {
			command.append("-cp ");
			for (Path jar : dependencyJars) {
				command.append(jar.toString() + "; ");
			}
		}
		command.append(fileCodeName.toString());
		execCommandWriteStreamToFile(command.toString(), outputFilePath);
	}

	/**
	 * run .java file by execute 'java' command . the file must be compiled
	 * 
	 * @param classNameWithPath
	 *            path and name of the class file to run, must end with '.java'
	 * @param dependencyJars
	 *            the essensial jars needed by the file to run
	 * @param outputFilePath
	 *            path and name of file to write the output stream and error
	 *            stream of the running program
	 * @see outputFilePath
	 * @throws Exception
	 *             from the method execCommandWriteStreamToFile
	 **/
	private static void runProgram(Path fileCodeName, Path[] dependencyJars, Path outputFilePath) throws Exception {
		StringBuilder command = new StringBuilder();
		command.append("java ");
		if (dependencyJars != null && dependencyJars.length > 0) {
			command.append("-cp ");
			for (Path jar : dependencyJars) {
				command.append(jar.toString()+"; ");
			}
		}
		command.append(fileCodeName.toString());
		execCommandWriteStreamToFile(command.toString(), outputFilePath);
	}

	/**
	 * disassemble .class file to readable java byte-code using 'javap -c'
	 * command
	 * 
	 * @param classNameWithPath
	 *            path and name of the class file to disassemble, must end with
	 *            '.class'
	 * @param outputFilePath
	 *            path and name of file to write the output code-byte
	 * @see outputFilePath
	 * @throws Exception
	 *             from the method execCommandWriteStreamToFile
	 **/
	private static void disassemblyCode(Path classNameWithPath, Path outputFilePath) throws Exception {
		String command = "javap -c";
		execCommandWriteStreamToFile(command + " " + classNameWithPath.toString(), outputFilePath); // throws
																									// Exception
	}

	/**
	 * exceute the command and write the output and error stream to
	 * outputFilePath file
	 * 
	 * @param command
	 *            command to exceute
	 * @param outputFilePath
	 *            path and name of file to write the output and error stream
	 * @see outputFilePath
	 * @throws Exception
	 **/
	private static void execCommandWriteStreamToFile(String command, Path outputFilePath) throws Exception {
		Process pro = Runtime.getRuntime().exec(command); // throws IOException
		printStreamToFile(pro, outputFilePath); // throws Exception
	}

	/*
	 * ... Inner methods 
	 */

	// write the content in the input stream and error stream and the exit value
	// of the process 'pro' to the file the the path 'outputFilePath'
	private static void printStreamToFile(Process pro, Path outputFilePath) throws Exception {
		FileWriter writer = null;
		try {
			writer = new FileWriter(outputFilePath.toFile());
			printLines("Input", pro.getInputStream(), writer); // throws
																// Exception
			printLines("Error", pro.getErrorStream(), writer); // throws
																// Exception
			pro.waitFor();
			StringBuilder exitValue = new StringBuilder("exitValue: ").append(pro.exitValue()).append("\n");

			writer.write(exitValue.toString());

		} catch (IOException e) {
			System.err.println("error writing to the file" + e.getMessage());
		} finally {
			if (writer != null) {
				try {
					writer.flush();
					writer.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}

			}
		}

	}

	// write string from stream 'ins' to the file loaded in 'writer'
	private static void printLines(String headline, InputStream ins, FileWriter writer) throws Exception {
		String line = null;
		BufferedReader in = new BufferedReader(new InputStreamReader(ins));
		writer.write(headline + System.lineSeparator());
		while ((line = in.readLine()) != null) {
			writer.write(line + System.lineSeparator());
		}
	}

}
