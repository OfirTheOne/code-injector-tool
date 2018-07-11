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
	ArrayList<String> codeList;
	int codeSize;
	
	/**
	 * Module constructor - responsible for initialize statements patterns lists.
	 * @param codeList - the requested code to be reformatted represented by list of strings.
	 */
	public BracketsCodeReformatter(ArrayList<String> codeList) {
		this.codeList = codeList;
		this.codeSize = codeList.size();
		
		this.statementsDeclarations = new ArrayList<Pattern>();
		statementsDeclarations.add(Pattern.compile("^(\\s|\\t)*for(\\s)*\\(((?!(if\\\\s)|(else\\\\s)|(for\\\\s)|(while\\\\s)).*)\\)"));
		statementsDeclarations.add(Pattern.compile("^(\\s|\\t)*while(\\s)*\\(((?!(if\\\\s)|(else\\\\s)|(for\\\\s)|(while\\\\s)).*)\\)"));
		statementsDeclarations.add(Pattern.compile("^(\\s|\\t)*if(\\s)*\\(((?!(if\\s)|(else\\s)|(for\\s)|(while\\s)).*)\\)"));
		statementsDeclarations.add(Pattern.compile("^(\\s|\\t)*else(\\s)*"));
		statementsDeclarations.add(Pattern.compile("^(\\s|\\t)*else if(\\s)*\\(((?!(if\\\\s)|(else\\\\s)|(for\\\\s)|(while\\\\s)).*)\\)"));
		
		this.inlineDeclarations = new ArrayList<>();
		inlineDeclarations.add(Pattern.compile("^(\\s|\\t)*for(\\s)*\\(((?!(if\\\\s)|(else\\\\s)|(for\\\\s)|(while\\\\s)).*)\\)(\\s)*+[^{}(\\s)]+"));
		inlineDeclarations.add(Pattern.compile("^(\\s|\\t)*while(\\s)*\\(((?!(if\\\\s)|(else\\\\s)|(for\\\\s)|(while\\\\s)).*)\\)(\\s)*+[^{}(\\s)]+"));
		inlineDeclarations.add(Pattern.compile("^(\\s|\\t)*if(\\s)*\\(((?!(if\\\\s)|(else\\\\s)|(for\\\\s)|(while\\\\s)).*)\\)(\\s)*+[^{}(\\s)]+"));
		inlineDeclarations.add(Pattern.compile("^(\\s|\\t)*else[^ if](\\s)*[^{}(\\s)]+"));
		inlineDeclarations.add(Pattern.compile("^(\\s|\\t)*else if(\\s)*\\(((?!(if\\\\s)|(else\\\\s)|(for\\\\s)|(while\\\\s)).*)\\)(\\s)*+[^{}(\\s)]+"));
		
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
	private void checkInlineDecleration() {
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
				done = inlineStatementFix(currentCheckedIndex, foundStatement);
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
	private boolean inlineStatementFix(int currentCheckedIndex, Integer statementIndex) {
		Matcher declarationOnly = null;
		String currentCheckedLine = codeList.get(currentCheckedIndex);
		boolean isComment = checkIfCommented(currentCheckedLine, statementIndex);
		
		if (!isComment) {
			declarationOnly = statementsDeclarations.get(statementIndex).matcher(currentCheckedLine);
			if (declarationOnly.find()) {
				splitInlineStatement(currentCheckedIndex, declarationOnly.group(0), statementIndex);
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
	private void splitInlineStatement(int currentCheckedIndex, String statementOnly,
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
	private void checkDeclarationsWithoutBrackets(int index) {
		String currentCheckedLine;
		int currentCheckedIndex;
		
		for (currentCheckedIndex = index; currentCheckedIndex < codeSize; currentCheckedIndex++) {
			currentCheckedLine = codeList.get(currentCheckedIndex);
			if (lineIsStatementWithoutBrackets(currentCheckedLine) && !lineIsFollowedByOpenBracket(currentCheckedIndex)) {
				codeSize += addBrackets(currentCheckedIndex);
			}
		}
	}
	
	/**
	 * Checks whether the provided line (string) is loop / condition statement without opening bracket in the same line.
	 * @param currentCheckedLine
	 * @return
	 */
	private boolean lineIsStatementWithoutBrackets(String currentCheckedLine) {
		
		boolean statementWithoutBracketsHasFound = false;
		
		for (Pattern statementPattern : newlineDeclarations) {
			if(statementPattern.matcher(currentCheckedLine).find()) {
				statementWithoutBracketsHasFound = true;
			}
		}
		
		if (statementWithoutBracketsHasFound != true) {
			for (Pattern statementPattern : commentedDeclarations) {
				if(statementPattern.matcher(currentCheckedLine).find()) {
					statementWithoutBracketsHasFound = true;
				}
			}
		}
		return statementWithoutBracketsHasFound;
	}
	
	/**
	 * Adds opening and closing brackets to loop / condition declaration.
	 * In addition - this method handle cases when the relevant new scope includes other statements.
	 * @param codeList
	 * 		The program's input inserted into ArrayList data structure.
	 * @param currentCheckedIndex
	 * 		The current index in the list we want to check for.
	 * @return
	 */
	private int addBrackets(int currentCheckedIndex) {
		int numOfLinesToIgnore;
		int numOfPotentialBrackets = 1;
		int closerBracketIndex = currentCheckedIndex + 2;
		int nextLineIndex;
		String currentCheckedLine = codeList.get(currentCheckedIndex);

		addOpenBracketToEndOfDeclaration(currentCheckedIndex, currentCheckedLine);
		for (nextLineIndex = currentCheckedIndex + 1; nextLineIndex < codeList.size(); nextLineIndex++) {
			if (lineIsCommented(codeList.get(nextLineIndex))) {
				closerBracketIndex++;
				continue;
			}

			if (lineIsStatementWithoutBrackets(codeList.get(nextLineIndex)) && !lineIsFollowedByOpenBracket(nextLineIndex)) {
				codeSize += addBrackets(nextLineIndex);
				numOfLinesToIgnore = checkForInnerScopes(nextLineIndex);
				if (numOfLinesToIgnore > 0) {
					closerBracketIndex = currentCheckedIndex + numOfLinesToIgnore + 1;
					continue;
				} 
			} else {
				numOfLinesToIgnore = checkForInnerScopes(nextLineIndex);
				if (numOfLinesToIgnore > 0) {
					closerBracketIndex = currentCheckedIndex + numOfLinesToIgnore + 1;
				}
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
	private void addOpenBracketToEndOfDeclaration(int currentCheckedIndex, String currentCheckedLine) {
		
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
		
		codeList.remove(currentCheckedIndex);
		codeList.add(currentCheckedIndex, currentCheckedLine);
	}

	/**
	 * Checks whether the current scoop contains additional scoops.
	 * @param currentCheckedIndex
	 * @return numOfLinesToIgnore - the number of lines to skip over (indicates the correct line for the closing bracket).
	 */
	private int checkForInnerScopes(int currentCheckedIndex) {
		
		String currentCheckedLine;
		int numOfLinesToIgnore = 0;
		int numOfOpenBracketsFound = 0;
		
		currentCheckedLine = codeList.get(currentCheckedIndex);
		if (lineContainsOpeningBracket(currentCheckedLine)) {
			numOfOpenBracketsFound++;
			numOfLinesToIgnore++;
			currentCheckedLine = codeList.get(++currentCheckedIndex);
		} else if (lineIsFollowedByOpenBracket(currentCheckedIndex)) {
			numOfLinesToIgnore++;
			currentCheckedLine = codeList.get(++currentCheckedIndex);
		}
		while (lineIsCommented(currentCheckedLine) || !lineContainsClosingBracket(currentCheckedLine) || numOfOpenBracketsFound > 1) {
			if (lineContainsOpeningBracket(currentCheckedLine)) {
				numOfOpenBracketsFound++;
				numOfLinesToIgnore++;
			}
			if (lineContainsClosingBracket(currentCheckedLine)) {
				numOfOpenBracketsFound--;
			}
			
			numOfLinesToIgnore++;
			currentCheckedIndex++;
			currentCheckedLine = codeList.get(currentCheckedIndex);
		}
		
		return numOfLinesToIgnore;	
	}
	
	/**
	 * Checks whether the provided line (string) is a comment (starts with "//).
	 * @param currentCheckedLine
	 * @return true in case of commented line, false otherwise.
	 */
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
	
	/**
	 * Checks whether the provided line (string) contains opening bracket.
	 * @param currentCheckedLine
	 * @return true in case of opening bracket, false otherwise.
	 */
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
	
	/**
	 * Checks whether the provided line (string) contains closing bracket.
	 * @param currentCheckedLine
	 * @return true in case of closing bracket, false otherwise.
	 */
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
	 * Checking if loop or condition declaration's followed by open bracket (checking the following line in code).
	 * @param currentCheckedLine
	 * 		Checked line in the program's input.
	 * @return
	 * 		True if the method check succeed, else false.
	 */
	private boolean lineIsFollowedByOpenBracket(int currentCheckedIndex) {
		
		String nextLine = null;
		Matcher isBracket = null;
		Pattern openBracket = Pattern.compile("^(\\s|\\t)*\\{");

		nextLine = codeList.get(++currentCheckedIndex);
		while (lineIsCommented(nextLine)) {
			nextLine = codeList.get(++currentCheckedIndex);
		}

		isBracket = openBracket.matcher(nextLine);

		if (isBracket.find()) {
			return true;
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
	 * The initiator method of this module.
	 * 		Checks the entire program for loops and conditions declaration without scope.
	 * @param inputFilePath
	 * 		The path of the input file (program / class).
	 * @param reformattedFilePath
	 * 		The path to write the new reformatted file.
	 */
	public static void bracketsReformatter(String inputFilePath, String reformattedFilePath) {

		ArrayList<String> codeList = analyzerUtils.FileOperations
				.fileContentToArrayListStringOnly(new File(inputFilePath), true);
		BracketsCodeReformatter bracketsCodeReformatter = new BracketsCodeReformatter(codeList);
		bracketsCodeReformatter.checkInlineDecleration();
		bracketsCodeReformatter.checkDeclarationsWithoutBrackets(0);
		analyzerUtils.FileOperations.writeCodeToFileString(codeList, new File(reformattedFilePath));
	}
}