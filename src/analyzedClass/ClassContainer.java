package analyzedClass;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map.Entry;

import analyzedClass.analyzedMethod.MethodInfo;
import analyzedClass.analyzedMethod.MethodsDataSet;

public class ClassContainer {

	final private Class<?> cls;
	private ClassConfiguration classConfiguration = null;
	private MethodsDataSet methodsDataSet;
	private ArrayList<StringBuilder> codeBeforMethods = null;

	/**
	 * 
	 * construct 'methodsDataSet' class member and calling
	 * 'methodsDataSet.buildDataSet' method
	 * 
	 * @param cls
	 *            instance of the class object
	 * @param classCode
	 *            list of the (complete) class's code lines
	 */
	public ClassContainer(Class<?> cls, ArrayList<StringBuilder> classCode) {
		this.cls = cls;
		this.methodsDataSet = new MethodsDataSet(cls);
		this.codeBeforMethods = this.methodsDataSet.buildDataSet(classCode);
	}

	/**
	 * setting the 'classConfiguration' class member to classConfig
	 * 
	 * @param classConfig
	 *            ClassConfiguration object containing the class configuration
	 */
	public void setClassConfiguration(ClassConfiguration classConfig) {
		classConfiguration = classConfig;
	}

	public MethodsDataSet getMethodsDataSet() {
		return this.methodsDataSet;
	}

	/**
	 * write the newly constructed class, calling 'buildClassConfiguration'
	 * method than write the code lines in 'codeBeforMethods'. then write the
	 * methods code lines in 'methodsDataSet' class member
	 * 
	 * @param outputClassPath
	 *            path of the file to write the newly constructed class
	 * 
	 */
	public void constructNewClass(Path outputClassPath) {
		buildClassConfiguration();

		FileWriter writer = null;
		try {
			writer = new FileWriter(outputClassPath.toFile());
			// first write the code lines before all declarations
			for (StringBuilder line : this.codeBeforMethods) {
				writer.write(line + "\n");
			}

			// write the methods code lines
			for (Entry<String, MethodInfo> entry : methodsDataSet.getEntrySet()) {
				for (StringBuilder line : entry.getValue().methodCodeLines) {
					writer.write(line + "\n");
				}
			}
			writer.write("}\n"); // closing the class code

		} catch (IOException e) {
			System.err.println("error writing to the file " + e.getMessage() + e.getStackTrace());
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

	/**
	 * @return class object of the class we operate on
	 */
	public Class<?> getClassObj() {
		return cls;
	}

	/*
	 * ... inner ...
	 */

	/**
	 * according to 'classConfiguration' class member setting the code lines in
	 * the member 'codeBeforMethods'. handling the class package, libraries
	 * imports, class name, extends, interfaces, new class member definitions
	 */
	private void buildClassConfiguration() {
		if (this.codeBeforMethods != null) {
			int classDeclarationIndex = findClassDeclarationIndex();
			setPackageDeclarationCodeLine();
			setNewClassDeclarationCodeLine(classDeclarationIndex);
			setMemberAndMethodsClassCodeLines(classDeclarationIndex);
			setImportsCodeLines(classDeclarationIndex);
			System.out.println(this.codeBeforMethods.toString());
		}
	}

	private int findClassDeclarationIndex() {
		String classDeclarationRegEx = "^((\\s|\\t)*(public class ).*(\\{).*)$";
		int classDeclarationIndex = -1;
		for (int i = 0; i < codeBeforMethods.size(); i++) {
			String line = codeBeforMethods.get(i).toString();
			if (line.matches(classDeclarationRegEx)) {
				classDeclarationIndex = i;
				break;
			}
		}
		return classDeclarationIndex;

	}

	private void setPackageDeclarationCodeLine() {
		if (classConfiguration.packageName != null) {
			String line = codeBeforMethods.get(0).toString();
			if (line.matches("^((\\s|\\t)*(package ).*)$")) {
				codeBeforMethods.set(0, new StringBuilder("package " + classConfiguration.packageName + ";"));
			} else {
				codeBeforMethods.add(0, new StringBuilder("package " + classConfiguration.packageName + ";"));
			}
		}
	}

	private void setNewClassDeclarationCodeLine(int classDeclarationIndex) {
		String classHasImplRegEx = "^(.*\\s(implements).*\\{.*)$";

		String line = codeBeforMethods.get(classDeclarationIndex).toString();

		String implClassesSection = "";
		if (classConfiguration.implementsClasses != null) {
			System.out.println("Oni the donky dick !");
			implClassesSection = " implements ";
			for (int i = 0; i < classConfiguration.implementsClasses.length; i++) {
				String impl = classConfiguration.implementsClasses[i];
				implClassesSection += i == 0 ? impl : ", " + impl;
			}

			// check if class already implements from other classes
			if (line.matches(classHasImplRegEx)) {
				int endImplIndex = line.lastIndexOf("implements") + 10; // "implements".length();
				int startCurlyBraces = line.lastIndexOf("{");
				String implClasses = line.substring(endImplIndex, startCurlyBraces);
				implClassesSection += ", " + implClasses;
			}
		}

		// TODO :: need to handle Extends case
System.out.println(implClassesSection);
		// run over the class definition
		codeBeforMethods.set(classDeclarationIndex,
				new StringBuilder("public class " + classConfiguration.className + implClassesSection + " {"));
	}

	private void setMemberAndMethodsClassCodeLines(int classDeclarationIndex) {
		int memberCount = classDeclarationIndex;
		if (classConfiguration.classMembersCodeLines != null) {
		
			for (String memberLine : classConfiguration.classMembersCodeLines) {
				memberCount++;
				codeBeforMethods.add(memberCount, new StringBuilder(memberLine));
			}
		}
		setNewClassMethodsCodeLines(memberCount);
	}
	
	private void setNewClassMethodsCodeLines(int position) {
		if (classConfiguration.classMethodCodeLines != null) {
			int methodCount = position;
			for (String methodLine : classConfiguration.classMethodCodeLines) {
				methodCount++;
				codeBeforMethods.add(methodCount, new StringBuilder(methodLine));
			}
		}
	}

	private void setImportsCodeLines(int classDeclarationIndex) {
		if (classConfiguration.importsCodeLine != null) {
			for (String importCodeLine : classConfiguration.importsCodeLine) {
				codeBeforMethods.add(classDeclarationIndex, new StringBuilder(importCodeLine));
			}
		}
	}

}
