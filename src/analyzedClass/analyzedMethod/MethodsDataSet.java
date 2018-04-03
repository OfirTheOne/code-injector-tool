package analyzedClass.analyzedMethod;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

public class MethodsDataSet {

	private final Class<?> cls;
	private boolean buildDataSetCalled = false;
	private final HashMap<String, MethodInfo> dataSet;

	/**
	 * @param cls
	 *            instance of the class object
	 */
	public MethodsDataSet(Class<?> cls) {
		this.cls = cls;
		this.dataSet = new HashMap<String, MethodInfo>();
	}

	/**
	 * @param className
	 *            string of the class name
	 * @throws ClassNotFoundException
	 */
	public MethodsDataSet(String className) throws ClassNotFoundException {
		this(Class.forName(className));
	}

	/**
	 * only if it's the first time buildDataSet called - setting 'dataSet' class
	 * member by calling 'divideClassCodeToMethods' method
	 * 
	 * @param classCode
	 *            list of the (complete) class's code lines
	 * 
	 * @return list of the code lines located before the methods definition. if
	 *         it's the first time buildDataSet called (buildDataSetCalled ==
	 *         false) the returned value is the list of code returned from the
	 *         'divideClassCodeToMethods' method. if it's not the first time
	 *         this method called the returned value is null.
	 * 
	 */
	public ArrayList<StringBuilder> buildDataSet(ArrayList<StringBuilder> classCode) {
		if (!buildDataSetCalled) {
			buildDataSetCalled = !buildDataSetCalled;
			Method[] methods = cls.getDeclaredMethods();
			return this.divideClassCodeToMethods(methods, classCode);
		}
		return null;

	}

	/**
	 * @param methodName
	 *            name of the method
	 * @return MethodInfo object represent the method
	 */
	public MethodInfo getMethodInfo(String methodName) {
		return dataSet.get(methodName);
	}

	/**
	 * @return set of the entries of the methods map.
	 */
	public Set<Entry<String, MethodInfo>> getEntrySet() {
		return dataSet.entrySet();
	}

	/**
	 * @return class object of the class we operate on
	 */
	public Class<?> getClassObj() {
		return cls;
	}

	/**
	 * @return class name of the class we operate
	 */
	public String getClassName() {
		return cls.getName();
	}

	
	/*
	 *  ... inner ...
	 * */
	
	/**
	 * Initialize the dataSet class member with the relevant data. divide the
	 * class code line by methods.
	 * 
	 * @param methodsNames
	 *            list of the class methods's names
	 * @param classCode
	 *            list of the class's code lines
	 * 
	 * @return list of the code before the methods definition
	 */
	private ArrayList<StringBuilder> divideClassCodeToMethods(Method[] methods, ArrayList<StringBuilder> classCode) {

		ArrayList<StringBuilder> codeBeforMethods = new ArrayList<StringBuilder>();
		boolean foundFirstMethod = false;
		for (int lineIndex = 0; lineIndex < classCode.size(); lineIndex++) {
			StringBuilder line = classCode.get(lineIndex);
			Method method = this.isMethodDecleration(line.toString(), methods);

			if (method != null) { // ==> 'line' is a method Declaration
				foundFirstMethod = true;
				int endMethodScope = MethodOperations.findEndOfScope(classCode, lineIndex);
				ArrayList<StringBuilder> methodCodeLines = this.copyMethodCodeScope(lineIndex, endMethodScope,
						classCode);
				dataSet.put(method.getName(), new MethodInfo(method, methodCodeLines));
				lineIndex = endMethodScope;
			} else if (!foundFirstMethod) {
				// foundFirstMethod = false ==> meaning we not found yet any
				// method declaration
				// saving those code lines in 'codeBeforMethods' list
				codeBeforMethods.add(line);
			}
		}
		return codeBeforMethods;
	}

	/**
	 * @param start
	 *            index of the first code line of the method
	 * @param end
	 *            index of the last code line of the method
	 * 
	 * @return new list object containing the lines from the list 'classCode'
	 *         range between [start, end)
	 */
	private ArrayList<StringBuilder> copyMethodCodeScope(int start, int end, ArrayList<StringBuilder> classCode) {
		ArrayList<StringBuilder> returnedCodeLines = null;
		if (end < classCode.size()) {
			returnedCodeLines = new ArrayList<StringBuilder>();

			for (int i = start; i <= end; i++) {
				returnedCodeLines.add(classCode.get(i));
			}
		}
		return returnedCodeLines;
	}

	/**
	 * @param codeLine
	 *            one line of code
	 * @param methodsNames
	 *            list of methods names
	 * 
	 * @return if 'codeLine' is a (code of the) declaration of one of the
	 *         methods in the list 'methodsNames' will return that method name,
	 *         else return null.
	 */
	private Method isMethodDecleration(String codeLine, Method[] methods) {

		for (Method method : methods) {
			if (codeLine.matches("^(.*\\s(" + method.getName() + ")(\\().*\\{)$")) {
				return method;
			}
		}
		return null;
	}

}
