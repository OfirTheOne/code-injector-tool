package injectionDriver;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import analyzedClass.*;
import analyzerUtils.*;
import analyzerAlgo.CodeInjector;


public class Driver {

	// previously called injectorProcess
	public static void main(String[] args) {
		
		String originalFilePath = args[0];
		String reformattedFilePath = args[1];
		String injectedFilePath = args[2];
		String classPath = args[3];
		String className = args[4];
		String configJsonPath = args[5];

		
		Class<?> cls = FileOperations.getClassObjectFromClassFile(new File(classPath), className);
		
		if(cls == null) {
			System.out.println("can't load class file.");
			return;
		}
		
		ClassConfiguration classConfig = FileOperations.readConfigFromJson(configJsonPath);
		
		if(classConfig == null) {
			System.out.println("can't load config json file.");
			return;
		}
		
		ClassContainer classContainer = null;

		
		// =================== step 1 ===================
		// add brackets for loops and conditions if missing
		BracketsCodeReformatter.bracketsReformatter(originalFilePath, reformattedFilePath);
		
		// =================== step 2 ===================
		// reformat by google style code the original code and copy it to reformattedCodeFile
		CodeReformatter.googleCodeReformater(reformattedFilePath, reformattedFilePath);

		
		// =================== step 3 ===================
		// creating array list of the code in reformatted Code File
		File reformattedCodeFile = new File(reformattedFilePath);
		ArrayList<StringBuilder> code = FileOperations.fileContentToArrayListString(reformattedCodeFile, true);
		
		
		/* *
		 * create ClassContainer -
		 * maintain the class code in an object to manage correctly all 
		 * the changes that take place in the code
		 * * */
		// =================== step 4 ===================
		// + inject the variable definition code lines 
		// + inject the dependencies code line
		// + divide the class code to methods  
		classContainer = new ClassContainer(cls, code);
		
		
		// =================== step 5 ===================
		// start scanning and the code injection
		CodeInjector codeInjector = new CodeInjector();
		codeInjector.mainScanner(classContainer.getMethodsDataSet());
		
		
		// =================== step 6 ===================
		// define the new class configuration and set them
		classContainer.setClassConfiguration(classConfig);
		
		
		// =================== step 7 ===================
		// write all the code lines in classContainer to the injectedCodeFile
		Path injectedCodeFile = Paths.get(injectedFilePath);
		classContainer.constructNewClass(injectedCodeFile);
		
		System.out.println("Done with the injector process.");
		
	}
	
}
