package analyzedClass.analyzedMethod;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class MethodInfo {

	private String methodName;
	private Method method;
	public ArrayList<StringBuilder> methodCodeLines;
	// methods that being called from this method
	private ArrayList<String> depandencies; 
	// if dependencies[i] called from loop calledFromLoop[i] = 1
	private ArrayList<Boolean> calledFromLoop; 

	public String getMethodName() {
		return this.methodName;
	}
	
	public MethodInfo(Method method, ArrayList<StringBuilder> methodCodeLines) {
		this.method = method;
		this.methodName = method.getName(); 
		this.methodCodeLines = methodCodeLines;

		// handle in future
		this.depandencies = new ArrayList<String>();
		this.calledFromLoop = new ArrayList<Boolean>();

	}

	public Method getMethodIntance() {
		return this.method;
	}
	
	public void addDepandency(String name, boolean inLoop) {
		depandencies.add(name);
		calledFromLoop.add(inLoop);
	}

	/**
	 * Access the DataSet member and retrieving the code line of the method by
	 * using MethodAnalyer.findDependentForStatements method
	 * 
	 * @return Order pairs of dependent for statement
	 */
	public ArrayList<Integer[]> getDependentLoopStatementIndexPairs() {
		return MethodAnalyzer.findDependentLoopStatements(method ,methodCodeLines);
	}
	
	public ArrayList<Integer> getRecursionCallStatementsIndex() {
		return MethodAnalyzer.findRecursionCallStatements(method, methodCodeLines);
	}
	
}
