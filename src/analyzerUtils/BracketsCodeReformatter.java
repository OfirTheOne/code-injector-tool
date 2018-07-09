package analyzerUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BracketsCodeReformatter {
	
	ArrayList<Pattern> statementsDeclarations;
	ArrayList<Pattern> inlineDeclarations;
	ArrayList<Pattern> newlineDeclarations;
	ArrayList<Pattern> commentedDeclarations;

	public BracketsCodeReformatter() {
		this.statementsDeclarations = new ArrayList<Pattern>();
		statementsDeclarations.add(Pattern.compile("^(\\s|\\t)*for(\\s)*\\(((?!(if\\\\s)|(else\\\\s)|(for\\\\s)|(while\\\\s)).*)\\)"));
		statementsDeclarations.add(Pattern.compile("^(\\s|\\t)*while(\\s)*\\(((?!(if\\\\s)|(else\\\\s)|(for\\\\s)|(while\\\\s)).*)\\)"));
		statementsDeclarations.add(Pattern.compile("^(\\s|\\t)*if(\\s)*\\(((?!(if\\s)|(else\\s)|(for\\s)|(while\\s)).*)\\)"));
		statementsDeclarations.add(Pattern.compile("^(\\s|\\t)*else(\\s)*"));
		statementsDeclarations.add(Pattern.compile("^(\\s|\\t)*else if(\\s)*\\(((?!(if\\\\s)|(else\\\\s)|(for\\\\s)|(while\\\\s)).*)\\)"));
		
		this.inlineDeclarations = new ArrayList<>();
		inlineDeclarations.add(Pattern.compile("^(\\s|\\t)*for(\\s)*\\(((?!(if\\\\s)|(else\\\\s)|(for\\\\s)|(while\\\\s)).*)\\)(\\s)*+[^{}]+"));
		inlineDeclarations.add(Pattern.compile("^(\\s|\\t)*while(\\s)*\\(((?!(if\\\\s)|(else\\\\s)|(for\\\\s)|(while\\\\s)).*)\\)(\\s)*+[^{}]+"));
		inlineDeclarations.add(Pattern.compile("^(\\s|\\t)*if(\\s)*\\(((?!(if\\\\s)|(else\\\\s)|(for\\\\s)|(while\\\\s)).*)\\)(\\s)*+[^{}]+"));
		inlineDeclarations.add(Pattern.compile("^(\\s|\\t)*else[^ if](\\s)*[^{}]+"));
		inlineDeclarations.add(Pattern.compile("^(\\s|\\t)*else if(\\s)*\\(((?!(if\\\\s)|(else\\\\s)|(for\\\\s)|(while\\\\s)).*)\\)(\\s)*+[^{}]+"));
		
		this.newlineDeclarations = new ArrayList<>();
		newlineDeclarations.add(Pattern.compile("^(\\s|\\t)*for(\\s)*\\(((?!(if\\\\s)|(else\\\\s)|(for\\\\s)|(while\\\\s)).*)\\)(\\s|\\t)*$"));
		newlineDeclarations.add(Pattern.compile("^(\\s|\\t)*while(\\s)*\\(((?!(if\\\\s)|(else\\\\s)|(for\\\\s)|(while\\\\s)).*)\\)(\\s|\\t)*$"));
		newlineDeclarations.add(Pattern.compile("^(\\s|\\t)*if(\\s)*\\(((?!(if\\\\s)|(else\\\\s)|(for\\\\s)|(while\\\\s)).*)\\)(\\s|\\t)*$"));
		newlineDeclarations.add(Pattern.compile("^(\\s|\\t)*else(\\s)*(\\s|\\t)*$"));
		newlineDeclarations.add(Pattern.compile("^(\\s|\\t)*else if(\\s)*\\(((?!(if\\\\s)|(else\\\\s)|(for\\\\s)|(while\\\\s)).*)\\)(\\s|\\t)*$"));
		
		this.commentedDeclarations = new ArrayList<>();
		commentedDeclarations.add(Pattern.compile("^(\\s|\\t)*for(\\s)*\\(((?!(if\\\\s)|(else\\\\s)|(for\\\\s)|(while\\\\s)).*)\\)(\\s)*+(//)+"));
		commentedDeclarations.add(Pattern.compile("^(\\s|\\t)*while(\\s)*\\(((?!(if\\\\s)|(else\\\\s)|(for\\\\s)|(while\\\\s)).*)\\)(\\s)*+(//)+"));
		commentedDeclarations.add(Pattern.compile("^(\\s|\\t)*if(\\s)*\\(((?!(if\\\\s)|(else\\\\s)|(for\\\\s)|(while\\\\s)).*)\\)(\\s)*+(//)+"));
		commentedDeclarations.add(Pattern.compile("^(\\s|\\t)*else(\\s)*(//)+"));
		commentedDeclarations.add(Pattern.compile("^(\\s|\\t)*else if(\\s)*\\(((?!(if\\\\s)|(else\\\\s)|(for\\\\s)|(while\\\\s)).*)\\)(\\s)*+(//)+"));
	}
	
	/**
	 * Checks for in-line declaration.
	 * If line has loop/condition declaration followed by non-comment command it will considered as in-line declaration.
	 * 
	 * @param codeList
	 * 		The program's input inserted into ArrayList data structure.
	 */
	private void checkInlineDecleration(ArrayList<String> codeList) {
		int codeListSize = codeList.size();
		int currentCheckedIndex;
		String currentCheckedLine = null;
		Integer foundStatement;
		boolean done;

		for (currentCheckedIndex = 0; currentCheckedIndex < codeListSize; currentCheckedIndex++) {
			done = false;
			currentCheckedLine = codeList.get(currentCheckedIndex);		
			foundStatement = typeOfInlineStatement(currentCheckedLine);
			
			if (foundStatement != null) {
				done = inlineStatementFix(codeList, currentCheckedIndex, foundStatement);
			}

			if (done == true) {
				codeListSize++;
			}
		}
	}
	
	/**
	 * Checks which expression appears at the beginning of the checked line.
	 * @param currentCheckedLine
	 * 		The current checked line string.
	 * @return
	 * 		The expression at the beginning of the string.
	 */
	public Integer typeOfInlineStatement(String currentCheckedLine) {

		Matcher inlineStatement;
		
		for (Integer statementIndex = 0; statementIndex < inlineDeclarations.size(); statementIndex++) {
			inlineStatement = inlineDeclarations.get(statementIndex).matcher(currentCheckedLine);
			if (inlineStatement.find()) {
				return statementIndex;
			}
		}
		
		return null;
	}
		
	/**
	 * This method fix in-line statements if found.
	 * @param codeList
	 * 		The program's input inserted into ArrayList data structure.
	 * @param currentCheckedIndex
	 * 		The current index in the list we want to check for.
	 * @param statementIndex
	 * 		The expression that we want to check - loop or condition.
	 * @return
	 * 		Return true if fix action has done. The action could fail if the expression is followed by a comment.
	 */
	private boolean inlineStatementFix(ArrayList<String> codeList, int currentCheckedIndex, Integer statementIndex) {
		Matcher declarationOnly = null;
		String currentCheckedLine = codeList.get(currentCheckedIndex);
		boolean isComment = checkIfCommented(currentCheckedLine, statementIndex);
		
		if (!isComment) {
			declarationOnly = statementsDeclarations.get(statementIndex).matcher(currentCheckedLine);
			if (declarationOnly.find()) {
				splitInlineStatement(codeList, currentCheckedIndex, declarationOnly.group(0), statementIndex);
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Splits the string into two different lines (loop / condition declaration + rest of the string).
	 * @param codeList
	 * 		The program's input inserted into ArrayList data structure.
	 * @param currentCheckedIndex
	 * 		The current index in the list we want to check for.
	 * @param statementOnly
	 * 		Only the loop / condition declaration at the beginning of the checked string (without the rest of the string).
	 * @param type
	 * 		The expression that we want to check - loop or condition.
	 */
	private void splitInlineStatement(ArrayList<String> codeList, int currentCheckedIndex, String statementOnly,
			Integer type) {
		String restOfLine = null;
		String currentCheckedLine = codeList.get(currentCheckedIndex);
		
		codeList.remove(currentCheckedIndex);
		restOfLine = currentCheckedLine.replaceFirst(statementsDeclarations.get(type).toString(), "");
		codeList.add(currentCheckedIndex, statementOnly);
		codeList.add(currentCheckedIndex + 1, restOfLine);
	}

	
	/**
	 * Checks for loop or condition declaration line with no brackets.
	 * @param codeList
	 * 		The program's input inserted into ArrayList data structure.
	 */
	private void checkDeclarationsWithoutBrackets(ArrayList<String> codeList) {
		String currentCheckedLine;
		int currentCheckedIndex;
		int codeSize = codeList.size();

		for (currentCheckedIndex = 0; currentCheckedIndex < codeSize; currentCheckedIndex++) {
			currentCheckedLine = codeList.get(currentCheckedIndex);
			if (recursiveCheck(codeList, currentCheckedIndex, currentCheckedLine)) {
				codeSize += addBrackets(codeList, currentCheckedIndex);
			}
		}
	}

	
	/**
	 * Adds curly brackets to loop or condition declaration without curly brackets.
	 * @param codeList
	 * 		The program's input inserted into ArrayList data structure.
	 * @param currentCheckedIndex
	 * 		The current index in the list we want to check for.
	 * @return
	 */
	private int addBrackets(ArrayList<String> codeList, int currentCheckedIndex) {
		
		int numOfPotentialBrackets = 1;
		int closerBracketIndex = currentCheckedIndex + 2;
		String currentCheckedLine = codeList.get(currentCheckedIndex);
		String fixedDaclarationWithBracket = addOpenBracketToEndOfDeclaration(currentCheckedLine);
		Object[] answer;
		
		codeList.remove(currentCheckedIndex);
		codeList.add(currentCheckedIndex, fixedDaclarationWithBracket);
		
		for (int i = currentCheckedIndex + 1; i < codeList.size(); i++) {
			if (lineIsCommented(codeList.get(i))) {
				closerBracketIndex++;
				continue;
			}
			
			answer = checkForInnerScopes(codeList, i);
			
			if (recursiveCheck(codeList, i, codeList.get(i))) {
				addBrackets(codeList, i);
				if((boolean)answer[0] == true) {
					closerBracketIndex = i + ((int)answer[1] + 1);
					i += (int)answer[1] - 1;
					continue;
				} else {
					currentCheckedLine = codeList.get(i);
					fixedDaclarationWithBracket = addOpenBracketToEndOfDeclaration(currentCheckedLine);
					codeList.remove(i);
					codeList.add(i, fixedDaclarationWithBracket);
					numOfPotentialBrackets++;
					closerBracketIndex = i + ((int)answer[1] + 2);
				}
			} else {
				closerBracketIndex += ((int)answer[1]);
				break;
			}
		}
		
		// Adding closing brackets as much
		for (int i = 0; i < numOfPotentialBrackets; i++) {
			codeList.add(closerBracketIndex, "}");
		}

		return numOfPotentialBrackets;
	}

	
	/**
	 * Adds open curly brackets to loop or condition declaration without curly brackets.
	 * @param currentCheckedLine - Declaration without opening bracket
	 * @return The given checkedLine with opening bracket after declaration 
	 */
	private String addOpenBracketToEndOfDeclaration(String currentCheckedLine) {
		
		String bracketLine;
		Matcher matcher;
		boolean commentedStatement = false;
		
		for (Pattern statementPattern : commentedDeclarations) {
			if (statementPattern.matcher(currentCheckedLine).find()) {
				int statementIndex = commentedDeclarations.indexOf(statementPattern);
				matcher = statementsDeclarations.get(statementIndex).matcher(currentCheckedLine);
				bracketLine = matcher.group(0);
				currentCheckedLine = currentCheckedLine.replaceFirst("^((\\s|\\t)*for(\\s)*\\((.*?)\\))",
						bracketLine + " {");
				commentedStatement = true;
				break;
			}
		}
		
		if (commentedStatement == false) {
			currentCheckedLine = currentCheckedLine + " {";
		}
		
		return currentCheckedLine;
	}

	
	private Object[] checkForInnerScopes(ArrayList<String> list, int currentCheckedIndex) {
		
		String currentCheckedLine;
		int numOfLinesToIgnore = 0;
		Object[] answer = new Object[2];
		answer[0] = false;

		currentCheckedLine = list.get(++currentCheckedIndex);
		while (lineIsCommented(currentCheckedLine)) {
			numOfLinesToIgnore++;
			currentCheckedIndex++;
			currentCheckedLine = list.get(currentCheckedIndex);
		}

		if (lineContainsOpeningBracket(currentCheckedLine)) {
			numOfLinesToIgnore++;
			while (lineIsCommented(currentCheckedLine) || !lineContainsClosingBracket(currentCheckedLine)) {
				numOfLinesToIgnore++;
				currentCheckedIndex++;
				currentCheckedLine = list.get(currentCheckedIndex);
			}
			
			answer[0] = true;	
		}
		
		answer[1] = numOfLinesToIgnore;
		return answer;
	}

	
	private boolean lineIsCommented(String currentCheckedLine) {
		
		boolean lineIsComment = false;
		Matcher lineIsCommentMathcer;
		Pattern lineIsCommentPattern = Pattern.compile("^(\\s|\\t)*(//)");
		
		lineIsCommentMathcer = lineIsCommentPattern.matcher(currentCheckedLine);
		
		if (lineIsCommentMathcer.find()) {
			lineIsComment = true;
		}
		
		return lineIsComment;
	}
	
	
	private boolean lineContainsOpeningBracket(String currentCheckedLine) {
		
		boolean lineContainsOpenBracket = false;
		Matcher lineContainsOpenBracketMatcher;
		Pattern lineContainsOpenBracketPattern = Pattern.compile(".*?\\{.*?");
		
		lineContainsOpenBracketMatcher = lineContainsOpenBracketPattern.matcher(currentCheckedLine);
		
		if (lineContainsOpenBracketMatcher.find()) {
			lineContainsOpenBracket = true;
		}
		
		return lineContainsOpenBracket;
	}
	
	
	private boolean lineContainsClosingBracket(String currentCheckedLine) {
			
			boolean lineContainsClosingBracket = false;
			Matcher llineContainsClosingBracketMatcher;
			Pattern lineContainsClosingBracketPattern = Pattern.compile(".*?\\}.*?");
			
			llineContainsClosingBracketMatcher = lineContainsClosingBracketPattern.matcher(currentCheckedLine);
			
			if (llineContainsClosingBracketMatcher.find()) {
				lineContainsClosingBracket = true;
			}
			
			return lineContainsClosingBracket;
		}
	
	
	/**
	 * An auxiliary method for checking if loop or condition declaration isn't followed by concrete command.
	 * @param currentCheckedLine
	 * 		Checked line in the program's input.
	 * @return
	 * 		True if the method check succeed, else false.
	 */
	private boolean recursiveCheck(ArrayList<String> codeList, int currentCheckedIndex, String currentCheckedLine) {
		
		String nextLine = null;
		Matcher isBracket = null;
		Pattern openBracket = Pattern.compile("^(\\s|\\t)*\\{");
		boolean found = false;
		
		for (Pattern statementPattern : newlineDeclarations) {
			if(statementPattern.matcher(currentCheckedLine).find()) {
				found = true;
				break;
			}
		}
		
		if (found == false) {
			for (Pattern statementPattern : commentedDeclarations) {
				if(statementPattern.matcher(currentCheckedLine).find()) {
					found = true;
					break;
				}
			}
		}
		
		if (found) {
			nextLine = codeList.get(++currentCheckedIndex);
			while(lineIsCommented(nextLine)) {
				nextLine = codeList.get(++currentCheckedIndex);
			}
			
			isBracket = openBracket.matcher(nextLine);
			
			if (!isBracket.find()) {
				return true;
			}
		}
		
		return false;
	}

	
	/**
	 * Checks if the current loop / condition declaration is followed by a comment.
	 * @param currentCheckedLine
	 * 		The current checked line string.
	 * @param statementIndex
	 * 		The expression that we want to check - loop or condition.
	 * @return
	 */
	public boolean checkIfCommented(String currentCheckedLine, Integer statementIndex) {

		Matcher commentedStatement = commentedDeclarations.get(statementIndex).matcher(currentCheckedLine);
		if (commentedStatement.find()) {
			return true;
		}

		return false;
	}
	
	/**
	 * The initiator method of this class.
	 * 		Checks the entire program for loops and conditions declaration without scope.
	 * @param inputFilePath
	 * 		The path of the input file (program / class).
	 * @param reformattedFilePath
	 * 		The path to write the new reformatted file.
	 */
	public static void bracketsReformatter(String inputFilePath, String reformattedFilePath) {

		ArrayList<String> list = analyzerUtils.FileOperations
				.fileContentToArrayListStringOnly(new File(inputFilePath), true);
		BracketsCodeReformatter bracketsCodeReformatter = new BracketsCodeReformatter();
		bracketsCodeReformatter.checkInlineDecleration(list);
		bracketsCodeReformatter.checkDeclarationsWithoutBrackets(list);
		analyzerUtils.FileOperations.writeCodeToFileString(list, new File(reformattedFilePath));
	}
}