package analyzedClass.analyzedMethod;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class MethodAnalyzer {

	// note : all the patterns will match non empty scop loops
	private static Pattern containsForLoopStatement =  Pattern.compile("for \\((.*);(.*);(.*)\\).*\\{") ;
	private static int[] forLoopConditionLocations = {1,2};
	private static Pattern containsWhileLoopStatement = Pattern.compile("while \\((.*)\\).*\\{") ;
	private static int[] whileLoopConditionLocations = {1};
	private static Pattern containsForEachLoopStatement =  Pattern.compile("for \\((.*):(.*)\\).*\\{") ;
	private static int[] forEachLoopConditionLocations = {2};


	public MethodAnalyzer() {}

	/**
	 * finds the index of all the dependent for loop statements and the index of
	 * there ending lines.
	 * 
	 * @param method
	 *            instance of the referred method.s
	 * @param methodCode
	 *            list of the method's code.
	 * @return list of ordered pairs , first contains index of the dependent for
	 *         statements, the second contains the index of the end.
	 */
	public static ArrayList<Integer[]> findDependentLoopStatements(Method method, ArrayList<StringBuilder> methodCode) {

		// scan all the method's code lines

		ArrayList<String> paramsNames = MethodAnalyzer.getParametersNames(method);
		ArrayList<Integer[]> orderedPairdependentLoopLines = new ArrayList<Integer[]>();
		int lineIndex = 0;

		for (StringBuilder line : methodCode) {
			// step 1 - update the dependencies parameters
			MethodAnalyzer.scanForMoreDependenciesParameters(line, paramsNames);

			// step 2 - check for any loop satement

			// =========== regular for loop
			Matcher forStatementMatcher = isCodeLineMatchePattern(containsForLoopStatement, line);
			if (forStatementMatcher != null) {
				if (MethodAnalyzer.isStatementDependent(forStatementMatcher, forLoopConditionLocations, paramsNames)) {
					int endScope = MethodOperations.findEndOfScope(methodCode, lineIndex);
					orderedPairdependentLoopLines.add(new Integer[]{lineIndex,endScope});
				}			
				lineIndex++;
				continue; // next iteretion
			} 

			// =========== while loop
			Matcher whileStatementMatcher = isCodeLineMatchePattern(containsWhileLoopStatement, line);
			if (whileStatementMatcher != null) {
				if (MethodAnalyzer.isStatementDependent(whileStatementMatcher, whileLoopConditionLocations, paramsNames)) {
					int endScope = MethodOperations.findEndOfScope(methodCode, lineIndex);
					orderedPairdependentLoopLines.add(new Integer[]{lineIndex,endScope});
				}
				lineIndex++;
				continue; // next iteretion
			}

			// =========== forEach loop
			Matcher forEachStatementMatcher = isCodeLineMatchePattern(containsForEachLoopStatement, line);
			if (forEachStatementMatcher != null) {
				if (MethodAnalyzer.isStatementDependent(forEachStatementMatcher, forEachLoopConditionLocations, paramsNames)) {
					int endScope = MethodOperations.findEndOfScope(methodCode, lineIndex);
					orderedPairdependentLoopLines.add(new Integer[]{lineIndex,endScope});
				}
			}
			lineIndex++;
		}
		return orderedPairdependentLoopLines;
	}

	public static ArrayList<Integer> findRecursionCallStatements(Method method, ArrayList<StringBuilder> methodCode) {
		Pattern recursionCallStatement =  Pattern.compile(method.getName() + "\\((.*)\\)");
		ArrayList<Integer> recursionCallLines = new ArrayList<Integer>();
		int lineIndex = 0;

		for (StringBuilder line : methodCode) {
			Matcher recursionCallMatcher = isCodeLineMatchePattern(recursionCallStatement, line);
			if (recursionCallMatcher != null) {
				recursionCallLines.add(lineIndex);
			}
			lineIndex++;
		}
		return recursionCallLines;
	}
	
	/**	Scan the current line and check if there is new assignments of method's parameters,
	 * if there is add them into ParamsNames ArrayList   
	 * 
	 * @param line current line that we check  
	 * @param paramsNames ArrayList of parameters names  
	 */
 	private static void scanForMoreDependenciesParameters(StringBuilder line,ArrayList<String> paramsNames){
		//TODO:: handle arr[i] = arr.length
		//TODO:: handle size = getArrayLength();
		//TODO:: when checking the right side check if literal
		//TODO:: handle += and -=
		//TODO:: need to handle: ternary if , for, while 

		if(line.toString().matches("(.* if ){0}(.* for ){0}(.* while ){0}(.*)( \\= )(.*);")) {
			Boolean isDepend = false;

			String leftSideVariable = null;
			ArrayList<String> rightSideVariables = MethodAnalyzer.extractVariableFromStatement(line.toString().replace(";", ""));
			leftSideVariable = rightSideVariables.remove(0);

			// check if right side variables appear in paramsName array
			for (String currentVariable : rightSideVariables) {
				if(MethodAnalyzer.isContainsParam(currentVariable, paramsNames)) {
					isDepend = true;
					if(leftSideVariable.contains("[")) {
						leftSideVariable = leftSideVariable.substring(0, leftSideVariable.indexOf("["));
					}
					if(!paramsNames.contains(leftSideVariable)) {
						paramsNames.add(leftSideVariable);
						return;
					}
				}
			}

			// the assignment not contains dependencies and left side in paramsNames array
			if(paramsNames.contains(leftSideVariable) && !(isDepend == true)) {
				paramsNames.remove(leftSideVariable);
				return;
			}
		}


	}


	/** extract all the variables on the specific line. 
	 * 
	 * @param line
	 * @return Array List of all variables in line
	 */
	private static ArrayList<String> extractVariableFromStatement(String line) {
		//String scanForLeftSide;
		String leftSide = null;
		Boolean isAssignmentChar = false; 
		ArrayList<String> variablesArrayList = new ArrayList<>();
		String[] lineArr = line.toString().split(" ");
		for (int i = 0; i < lineArr.length; i++) {
			if(lineArr[i].equals("=")) {
				if(!isAssignmentChar) {
					leftSide = lineArr[i - 1];
					variablesArrayList.add(leftSide);
				}
				for (int j = i +1; j < lineArr.length; j++) {
					//filter empty cells
					if(!lineArr[j].equals("")) {
						if(lineArr[j].contains("[")) {
							String variable = lineArr[j];
							while(!lineArr[j].contains("]")){
								variable+= lineArr[++j];
							}
							variablesArrayList.add(variable);
							break;
						}	
						variablesArrayList.add(lineArr[j]);
					}
				}
				break;
			}
			else if(lineArr[i].contains("[")) {
				leftSide = lineArr[i];
				while(!lineArr[i].contains("]")){
					leftSide+= lineArr[++i];
				}
				variablesArrayList.add(leftSide);
				if(lineArr[i + 1].equals("=")) {
					isAssignmentChar = true;
				}
			}
		}

		return variablesArrayList ;
	}


	/**
	 * determine if a code line effected by any of the parameters in
	 * paramsNames list
	 * 
	 * @param statementMatcher
	 *            matcher creater from a pattern.matcher() of a code line matched some loop pattern.
	 * @param groupNumbers
	 * 			  array of the group numbers the matcher found, for this method to check for any dependencies.
	 * @param paramsNames
	 *            list of the parameters names the method receive.
	 * 
	 * @return true if the for code line effected by any of the parameters in
	 *         paramsNames list, else false.
	 */
	private static boolean isStatementDependent(Matcher statementMatcher,int[] groupNumbers, ArrayList<String> paramsNames) {
		boolean isDependent = false;
		// check if the method contains the code line receives any parameters
		if (paramsNames.size() > 0) {
			for(int n : groupNumbers) {
				String condition = statementMatcher.group(n);
				if (MethodAnalyzer.isContainsParam(condition, paramsNames)) {
					isDependent = true;
					break;
				}
			}

		}
		return isDependent;
	}


	private static boolean isContainsParam(String line, ArrayList<String> paramsNames) {
		boolean result = false;
		for (String param : paramsNames) {
			// space - param - ( "[" or "." or "(" - anything ) or none
			if (line.matches(".*(\\s)?" + param + "(((\\[)|(\\.)|(\\())?.*)")) {
				result = true;
			}
		}
		return result;
	}

	private static ArrayList<String> getParametersNames(Method method) {
		ArrayList<String> paramsNames = new ArrayList<String>();
		Parameter[] parameters = method.getParameters();
		for (Parameter p : parameters) {
			paramsNames.add(p.getName());
		}

		return paramsNames;
	}

	// used to match any code line to for / forEach / while statement patterns 
	private static Matcher isCodeLineMatchePattern(Pattern patterm, StringBuilder line) {
		Matcher matcher = patterm.matcher(line);
		if(matcher.find()) {
			return matcher;
		} else {
			return null;
		}
	}




}
