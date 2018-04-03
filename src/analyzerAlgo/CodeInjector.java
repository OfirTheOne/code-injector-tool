package analyzerAlgo;

import java.util.ArrayList;
import java.util.Map.Entry;
import analyzedClass.analyzedMethod.MethodInfo;
import analyzedClass.analyzedMethod.MethodsDataSet;

public class CodeInjector {

	//private final Pattern containsEmptyScopeLoop = Pattern.compile("^(\\t|\\s)*(for|while)( \\().*(\\{\\})");
	
	// const names of elements in the code
	private final String injectedCounterVarName = "analyzerCounters";

	int injCount = 0; // count the injected counters in this class
	int loopCount = 0; // count the dependent loops in this class 
	/**
	 * main loop that scan the whole code file assuming it as been reformatted
	 * and handles the injections.
	 * 
	 * @param methodsData
	 *            the MethodsData object for the 
	 * @return amount of counters injections
	 */
	public int mainScanner(MethodsDataSet methodsDataSet) {
		int injectionCount = 0;
		for(Entry<String, MethodInfo> entry : methodsDataSet.getEntrySet()) {
			injectionCount += methodInjector(entry.getValue());
		}
		return injectionCount;
	}

	private int methodInjector(MethodInfo methodInfo) {
		
		/* some terms that used in the fallowing code :
		 * 1. 'most inner' loop = a loop that do not contains another loop inside her scope.
		 * 2. 'outer' loop = a loop that contains another loop inside her scope and might be contained in a loop.
		 * 3. 'external' loop = a loop that contains another loop inside her scope and DO NOT contained in a loop.
		 * */
		ArrayList<StringBuilder> methodCode = methodInfo.methodCodeLines;
		injectRecursionCall(methodCode, 1, injCount); // inject counter in at the start of the method to count the number of calls
		injCount++;
		ArrayList<Integer[]> orderPairs = methodInfo.getDependentLoopStatementIndexPairs();

		ArrayList<Integer> stackStartPoint = new ArrayList<Integer>();
		ArrayList<Integer> stackEndPoint = new ArrayList<Integer>();
		
		//boolean isExternalLoop = true; // a flag for an interval that "start a scope"
		int offset = 0;
		Integer popEndPoint;
		int popCount;
		
		// iterete over all the dependent loop code lines, and inject the relevant code lines.
		for(int i = 0; i < orderPairs.size(); i++) {
		
			// handle a case of single loop in a method and if the current item is the last pair in the list 
			// note: always refer to the last loop as a most inner one
			if(orderPairs.size()-1 == i) {
				Integer[] pair = orderPairs.get(i);
				int startScope = pair[0];
				injectInnerLoop(methodCode, startScope + 1 + offset, injCount, loopCount);
				offset++;
				injCount++;
				
				popCount = 0;
				while(stackEndPoint.size() > 0) {
					popEndPoint = stackEndPoint.remove(stackEndPoint.size()-1);
					injectOutterLoopExitPoint(methodCode, popEndPoint + offset, injCount, loopCount-popCount); // shift all the code below one index down
					popCount++;
					offset++;
				}
				
			} else {
				Integer[] curPair = orderPairs.get(i);
				Integer[] nextPair = orderPairs.get(i+1);

				int curStartScope = curPair[0];
				int curEndScope = curPair[1];
				int nextStartScope = nextPair[0];
				int nextEndScope = nextPair[1];

				
				if(curEndScope < nextEndScope) { // the loop is 'most inner'
					// note: the inject of 'most inner' loop shift the code only one down 
					injectInnerLoop(methodCode, curStartScope + 1 + offset , injCount, loopCount);
					offset++;
					if(stackEndPoint.size() > 0) {
						popEndPoint = -1; 
						popCount = 0;
						while(popEndPoint < nextStartScope && stackEndPoint.size() > 0) {
							popEndPoint = stackEndPoint.remove(stackEndPoint.size()-1);
							injectOutterLoopExitPoint(methodCode, popEndPoint + offset, injCount, loopCount-popCount); // shift all the code below one index down
							popCount++;
							offset++;
						}
					}
					loopCount++;
					injCount++;
					
				} else { // the loop is outer loop
					
					injectOutterLoopEntryPoint(methodCode, curStartScope + 1 + offset, loopCount); // shift all the code below one index down
					stackStartPoint.add(curStartScope);
					stackEndPoint.add(curEndScope);
					offset++;
					loopCount++;
				}
			}
		}
		return injCount;
	}

	private void injectInnerLoop(ArrayList<StringBuilder> methodCode, int startLine, int injCount, int intervalLoop) {
		StringBuilder injectionCode = 
				new StringBuilder(injectedCounterVarName+".incCount("+(injCount)+");\n" +
						injectedCounterVarName+".setEntryTrue("+(intervalLoop)+");");

		methodCode.add(startLine, injectionCode);
	}

	private void injectOutterLoopEntryPoint(ArrayList<StringBuilder> methodCode, int startLine, int intervalLoop) {
		StringBuilder injectionCode = 
				new StringBuilder(injectedCounterVarName+".setEntryTrue("+(intervalLoop)+");\n"+
						injectedCounterVarName+".setEntryFalse("+(intervalLoop+1)+");");
		methodCode.add(startLine, injectionCode);
	}

	private void injectOutterLoopExitPoint(ArrayList<StringBuilder> methodCode, int startLine, int injCount, int intervalLoop) {
		StringBuilder injectionCode = 
				new StringBuilder("if(!"+injectedCounterVarName+".getEntryValue("+(intervalLoop)+")){\n"+
						injectedCounterVarName+".incCount("+(injCount)+");\n}\n");
		methodCode.add(startLine, injectionCode);
	}
	
	private void injectRecursionCall(ArrayList<StringBuilder> methodCode, int lineIndex, int injCount) {
		StringBuilder injectionCode = 
				new StringBuilder(injectedCounterVarName+".incCount("+(injCount)+");");
		methodCode.add(lineIndex, injectionCode);
	}

}
