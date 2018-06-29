package analyzedClass.analyzedMethod;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class MethodAnalyzer {

	// note : all the patterns will match non empty scope loops
	private static Pattern containsForLoopStatement =  Pattern.compile("for \\((.*);(.*);(.*)\\).*\\{") ;
	private static int[] forLoopConditionLocations = {1,2};
	private static Pattern containsWhileLoopStatement = Pattern.compile("while \\((.*)\\).*\\{") ;
	private static int[] whileLoopConditionLocations = {1};
	private static Pattern containsForEachLoopStatement =  Pattern.compile("for \\((.*):(.*)\\).*\\{") ;
	private static int[] forEachLoopConditionLocations = {2};
	private static Pattern isRegularAssignmentStatement = Pattern.compile("(.* if ){0}(.* for ){0}(.* while ){0}(.*)( \\= )(.*);");
	private static Pattern isSpecialnAssignmentStatement = Pattern.compile("(.* if ){0}(.* for ){0}(.* while ){0}(.*)((\\*=)|(\\+=)|(\\-=)|(\\/=)|(\\%=))(.*);");
	private static int[] specialAssignmentConditionLocations = {5,6,7,8,9};


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

			// step 2 - check for any loop statement

			// =========== regular for loop
			Matcher forStatementMatcher = isCodeLineMatchePattern(containsForLoopStatement, line);
			if (forStatementMatcher != null) {
				if (MethodAnalyzer.isStatementDependent(forStatementMatcher, forLoopConditionLocations, paramsNames)) {
					int endScope = MethodOperations.findEndOfScope(methodCode, lineIndex);
					orderedPairdependentLoopLines.add(new Integer[]{lineIndex,endScope});
				}			
				lineIndex++;
				continue; // next iteration
			} 

			// =========== while loop
			Matcher whileStatementMatcher = isCodeLineMatchePattern(containsWhileLoopStatement, line);
			if (whileStatementMatcher != null) {
				if (MethodAnalyzer.isStatementDependent(whileStatementMatcher, whileLoopConditionLocations, paramsNames)) {
					int endScope = MethodOperations.findEndOfScope(methodCode, lineIndex);
					orderedPairdependentLoopLines.add(new Integer[]{lineIndex,endScope});
				}
				lineIndex++;
				continue; // next iteration
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

	/** Scan the current line and manage ParamsNames ArrayList in case there is assignments of method's parameters
	 *  by calling the appropriate method. 
	 * 
	 * @param line current line that we check  
	 * @param paramsNames ArrayList of parameters names  
	 */
 	private static void scanForMoreDependenciesParameters(StringBuilder line,ArrayList<String> paramsNames){
		//TODO:: handle size = getArrayLength();
		//TODO:: when checking the right side check if literal
		//TODO:: need to handle: ternary if
		//TODO:: Handle recursive calls
		String delimiter = null;
		Matcher regularAssignmentMatcher = isRegularAssignmentStatement.matcher(line);
		if(regularAssignmentMatcher.matches()) {
			delimiter = "=";
			handleAssignment(line,paramsNames,delimiter);
		}
		else {
			Matcher specialAssignmentMatcher = isSpecialnAssignmentStatement.matcher(line);
			if(specialAssignmentMatcher.matches()) {
				delimiter = getDelimiterType(line,specialAssignmentMatcher,specialAssignmentConditionLocations);
				if(delimiter != null) {
					handleAssignment(line,paramsNames,delimiter);	
				}
			}
		}
	}
	
	/** get assignment line and decide which assignment operator are being used
	 * 
	 * @param line current line that being analyze
	 * @param statementMatcher the pattern that current line match
	 * @param groupNumber array of the group numbers the matcher found
	 * @return the delimiter that use in the current line
	 */
	private static String getDelimiterType(StringBuilder line,Matcher statementMatcher, int[] groupNumber) {
		String delimiter = null;
		for (int i : groupNumber) {
			delimiter = statementMatcher.group(i);
			if(line.toString().contains(delimiter)) {
				break;
			}
		}
		
		return delimiter;
	}

	/**
	 * In case of regular assignment ( = ) - check for depended parameters:
	 * if left side depended and right side depended don't remove from paramsArray,   
	 * if left side depended and right side not depended remove from paramsArray,
	 * if left side not depended and right side depended add to paramsArray,
	 * if left side not depended and right side not depended don't do nothing
	 * ------------------------------------------------------------------------------
	 * In case of special assignment ( /= or %= or *= or += or -= ) - check for depended parameters:
	 * if left side depended and right side depended don't remove from paramsArray,  
	 * if left side depended and right side not depended don't remove from paramsArray,
	 * if left side not depended and right side depended add to paramsArray,
	 * if left side not depended and right side not depended don't do nothing 
	 * 
	 * @param line current line that we check  
	 * @param paramsNames ArrayList of parameters names
	 */
	private static void handleAssignment(StringBuilder line,ArrayList<String> paramsNames,String delimiter) {
		Boolean isDepend = false;
		String leftSideVariable = null;
		if(delimiter != null) {
			ArrayList<String> rightSideVariables = MethodAnalyzer.extractVariableFromStatement(line.toString().replace(";", ""), delimiter);
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

			/*in this point right side not depend but if we handle *= or += case there no need to check left side,
			 * if left side depend we will not remove him from paramsNames ArrayList
			 * if left side not depend he isn't exist and we don't need to remove him from paramsNames ArrayList */ 

			if(delimiter.equals("=")) {
				// the assignment not contains dependencies and left side in paramsNames array
				if(paramsNames.contains(leftSideVariable) && !(isDepend == true)) {
					paramsNames.remove(leftSideVariable);
					return;
				}
			}
		}
	}

	/** extract all the variables on the specific line. 
	 * 
	 * @param line current line that being analyzed
	 * @return Array List of all variables in line
	 */
	private static ArrayList<String> extractVariableFromStatement(String line, String delimiter) {
		String leftSide = null;
		//Boolean isAssignmentChar = false; 
		ArrayList<String> variablesArrayList = new ArrayList<>();
		String[] lineArr = line.toString().split(" ");
		for (int i = 0; i < lineArr.length; i++) {
			if(lineArr[i].equals(delimiter)) {
				if(lineArr[i - 1].contains("]")) {
					while(!lineArr[i].contains("[")) {
						i--;
					}
					leftSide = lineArr[i];
					while(!lineArr[i].contains("]")){
						leftSide+= lineArr[++i];
					}
				}
				else {
					leftSide = lineArr[i - 1];
				}
				variablesArrayList.add(leftSide);
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
			/*else if(lineArr[i].contains("[")) {
				leftSide = lineArr[i];
				while(!lineArr[i].contains("]")){
					leftSide+= lineArr[++i];
				}
				variablesArrayList.add(leftSide);
				if(lineArr[i + 1].equals("=")) {
					isAssignmentChar = true;
				}
			}*/
		}

		return variablesArrayList ;
	}


	/**
	 * determine if a code line effected by any of the parameters in
	 * paramsNames list
	 * 
	 * @param statementMatcher
	 *            matcher was created from a pattern.matcher() of a code line matched some loop pattern.
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
