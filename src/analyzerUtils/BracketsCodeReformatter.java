package analyzerUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BracketsCodeReformatter {
	static Pattern onlyForLoopDeclaration = Pattern.compile("^(\\s|\\t)*for(\\s)*\\((.*?)\\)");
	static Pattern onlyWhileLoopDeclaration = Pattern.compile("^(\\s|\\t)*while(\\s)*\\((.*?)\\)");
	static Pattern onlyIfStatementDeclaration = Pattern.compile("^(\\s|\\t)*if(\\s)*\\((.*?)\\)");
	static Pattern onlyElseStatementDeclaration = Pattern.compile("^(\\s|\\t)*else(\\s)*");
	static Pattern onlyElifStatementDeclaration = Pattern.compile("^(\\s|\\t)*else if(\\s)*\\((.*?)\\)");

	static Pattern inlineForLoopWithoutBrackets = Pattern.compile("^(\\s|\\t)*for(\\s)*\\((.*?)\\)(\\s)*+[^{}]+");	
	static Pattern inlineWhileLoopWithoutBrackets = Pattern.compile("^(\\s|\\t)*while(\\s)*\\((.*?)\\)(\\s)*+[^{}]+");
	static Pattern inlineIfStatementWithoutBrackets = Pattern.compile("^(\\s|\\t)*if(\\s)*\\((.*?)\\)(\\s)*+[^{}]+");
	static Pattern inlineElseStatementWithoutBrackets = Pattern.compile("^(\\s|\\t)*else[^ if](\\s)*[^{}]+");
	static Pattern inlineElifStatementWithoutBrackets = Pattern.compile("^(\\s|\\t)*else if(\\s)*\\((.*?)\\)(\\s)*+[^{}]+");

	static Pattern newlineForLoopWithoutBrackets = Pattern.compile("^(\\s|\\t)*for(\\s)*\\((.*?)\\)(\\s|\\t)*$");
	static Pattern newlineWhileLoopWithoutBrackets = Pattern.compile("^(\\s|\\t)*while(\\s)*\\((.*?)\\)(\\s|\\t)*$");
	static Pattern newlineIfStatementWithoutBrackets = Pattern.compile("^(\\s|\\t)*if(\\s)*\\((.*?)\\)(\\s|\\t)*$");
	static Pattern newlineElseStatementWithoutBrackets = Pattern.compile("^(\\s|\\t)*else(\\s)*(\\s|\\t)*$");
	static Pattern newlineElifStatementWithoutBrackets = Pattern.compile("^(\\s|\\t)*else if(\\s)*\\((.*?)\\)(\\s|\\t)*$");

	static Pattern commentForLoopWithoutBrackets = Pattern.compile("^(\\s|\\t)*for(\\s)*\\((.*?)\\)(\\s)*+(//)+");
	static Pattern commentWhileLoopWithoutBrackets = Pattern.compile("^(\\s|\\t)*while(\\s)*\\((.*?)\\)(\\s)*+(//)+");
	static Pattern commentIfStatementWithoutBrackets = Pattern.compile("^(\\s|\\t)*if(\\s)*\\((.*?)\\)(\\s)*+(//)+");
	static Pattern commentElseStatementWithoutBrackets = Pattern.compile("^(\\s|\\t)*else(\\s)*(//)+");
	static Pattern commentElifStatementWithoutBrackets = Pattern.compile("^(\\s|\\t)*else if(\\s)*\\((.*?)\\)(\\s)*+(//)+");


	/**
	 * Checks for in-line declaration.
	 * If line has loop/condition declaration followed by non-comment command it will considered as in-line declaration.
	 * 
	 * @param list
	 * 		The program's input inserted into ArrayList data structure.
	 */
	private static void checkInlineDecleration(ArrayList<String> list) {
		int listSize = list.size();
		int currentCheckedIndex = 0;
		String currentCheckedLine = null;
		String foundStatement = null;
		boolean done;

		for (; currentCheckedIndex < listSize; currentCheckedIndex++) {
			done = false;
			currentCheckedLine = list.get(currentCheckedIndex);		
			foundStatement = typeOfInlineStatement(currentCheckedLine);
			
			if (foundStatement != null) {
				done = inlineFix(list, currentCheckedIndex, foundStatement);
			}

			if ( done ) {
				listSize++;
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
	public static String typeOfInlineStatement(String currentCheckedLine) {
		
		Matcher inlineStatement;
		
		inlineStatement = inlineForLoopWithoutBrackets.matcher(currentCheckedLine);
		
		if (inlineStatement.find()) {
			return "for";
		}
		
		inlineStatement = inlineWhileLoopWithoutBrackets.matcher(currentCheckedLine);
		if (inlineStatement.find()) {
			return "while";
		}
		
		inlineStatement = inlineIfStatementWithoutBrackets.matcher(currentCheckedLine);
		
		if(inlineStatement.find()) {
			return "if";
		}

		inlineStatement = inlineElifStatementWithoutBrackets.matcher(currentCheckedLine);
		if (inlineStatement.find()) {
			return "else if";
		}
		
		inlineStatement = inlineElseStatementWithoutBrackets.matcher(currentCheckedLine);
		if (inlineStatement.find()) {
			return "else";
		}
		
		return null;
	}
	
	
	
	/**
	 * This method fix in-line statements if found.
	 * @param list
	 * 		The program's input inserted into ArrayList data structure.
	 * @param currentCheckedIndex
	 * 		The current index in the list we want to check for.
	 * @param type
	 * 		The expression that we want to check - loop or condition.
	 * @return
	 * 		Return true if fix action has done. The action could fail if the expression is followed by a comment.
	 */
	private static boolean inlineFix(ArrayList<String> list, int currentCheckedIndex, String type) {
		Matcher declarationOnly = null;
		boolean isComment = false;
		String currentCheckedLine = list.get(currentCheckedIndex);

		isComment = checkIfCommented(currentCheckedLine, type);
		
		if (!isComment) {
			switch (type) {

			case "for":
				declarationOnly = onlyForLoopDeclaration.matcher(currentCheckedLine);
				break;

			case "while":
				declarationOnly = onlyWhileLoopDeclaration.matcher(currentCheckedLine);
				break;

			case "if":
				declarationOnly = onlyIfStatementDeclaration.matcher(currentCheckedLine);
				break;
				
			case "else if":
				declarationOnly = onlyElifStatementDeclaration.matcher(currentCheckedLine);
				break;
				
			case "else":
				declarationOnly = onlyElseStatementDeclaration.matcher(currentCheckedLine);
				break;
			}
			
			
			if (declarationOnly.find()) {
				splitInlineStatement(list, currentCheckedIndex, declarationOnly.group(0), type);
				return true;
			}
		}
		return false;
	}
	

	
	/**
	 * Splits the string into two different lines (loop / condition declaration + rest of the string).
	 * @param list
	 * 		The program's input inserted into ArrayList data structure.
	 * @param currentCheckedIndex
	 * 		The current index in the list we want to check for.
	 * @param statementOnly
	 * 		Only the loop / condition declaration at the beginning of the checked string (without the rest of the string).
	 * @param type
	 * 		The expression that we want to check - loop or condition.
	 */
	private static void splitInlineStatement(ArrayList<String> list, int currentCheckedIndex, String statementOnly,
			String type) {
		String lineWithoutStatementDec = null;
		String currentCheckedLine = list.get(currentCheckedIndex);
		list.remove(currentCheckedIndex);

		switch (type) {
		case "for":
			lineWithoutStatementDec = currentCheckedLine.replaceFirst("^(\\s|\\t)*for(\\s)*\\((.*?)\\)", "");
			break;
		case "while":
			lineWithoutStatementDec = currentCheckedLine.replaceFirst("^(\\s|\\t)*while(\\s)*\\((.*?)\\)", "");
			break;
		case "if":
			lineWithoutStatementDec = currentCheckedLine.replaceFirst("^(\\s|\\t)*if(\\s)*\\((.*?)\\)", "");
			break;
		case "else if":
			lineWithoutStatementDec = currentCheckedLine.replaceFirst("^(\\s|\\t)*else if(\\s)*\\((.*?)\\)", "");
			break;
		case "else":
			lineWithoutStatementDec = currentCheckedLine.replaceFirst("^(\\s|\\t)*else", "");
			break;
		}

		list.add(currentCheckedIndex, statementOnly);
		list.add(currentCheckedIndex + 1, lineWithoutStatementDec);
	}

	
	/**
	 * Checks for loop or condition declaration line with no brackets.
	 * @param list
	 * 		The program's input inserted into ArrayList data structure.
	 */
	private static void checkDeclarationsWithoutBrackets(ArrayList<String> list) {
		String currentCheckedLine;
		int currentCheckedIndex = 0;
		int listSize = list.size();

		for (; currentCheckedIndex < listSize; currentCheckedIndex++) {
			currentCheckedLine = list.get(currentCheckedIndex);
			if (recursiveCheck(list, currentCheckedIndex, currentCheckedLine)) {
				listSize += addBrackets(list, currentCheckedIndex);
			}
		}
	}

	
	/**
	 * Adds curly brackets to loop or condition declaration without curly brackets.
	 * @param list
	 * 		The program's input inserted into ArrayList data structure.
	 * @param currentCheckedIndex
	 * 		The current index in the list we want to check for.
	 * @return
	 */
	private static int addBrackets(ArrayList<String> list, int currentCheckedIndex) {
		
		int numOfPotentialBrackets = 1;
		int closerBracketIndex = currentCheckedIndex + 2;
		String currentCheckedLine = list.get(currentCheckedIndex);
		String fixedDaclarationWithBracket = addOpenBracketToEndOfDeclaration(currentCheckedLine);
		Object[] answer;
		
		list.remove(currentCheckedIndex);
		list.add(currentCheckedIndex, fixedDaclarationWithBracket);
		
		for (int i = currentCheckedIndex + 1; i < list.size(); i++) {
			if (lineIsComment(list.get(i))) {
				closerBracketIndex++;
				continue;
			}
			
			answer = checkForInnerScopes(list, i);
			
			if (recursiveCheck(list, i, list.get(i))) {
				addBrackets(list, i);
				if((boolean)answer[0] == true) {
					closerBracketIndex = i + ((int)answer[1] + 1);
					i += (int)answer[1] - 1;
					continue;
				} else {
					currentCheckedLine = list.get(i);
					fixedDaclarationWithBracket = addOpenBracketToEndOfDeclaration(currentCheckedLine);
					list.remove(i);
					list.add(i, fixedDaclarationWithBracket);
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
			list.add(closerBracketIndex, "}");
		}

		return numOfPotentialBrackets;
	}

	
	/**
	 * Adds open curly brackets to loop or condition declaration without curly brackets.
	 * @param currentCheckedLine - Declaration without opening bracket
	 * @return The given checkedLine with opening bracket after declaration 
	 */
	private static String addOpenBracketToEndOfDeclaration(String currentCheckedLine) {
		
		String bracketLine;
		Matcher matcher;
		
		if (commentForLoopWithoutBrackets.matcher(currentCheckedLine).find()) {
			matcher = onlyForLoopDeclaration.matcher(currentCheckedLine);
			if (matcher.find()) {
				bracketLine = matcher.group(0);
				currentCheckedLine = currentCheckedLine.replaceFirst("^((\\s|\\t)*for(\\s)*\\((.*?)\\))",
						bracketLine + " {");
			}
		} else if (commentWhileLoopWithoutBrackets.matcher(currentCheckedLine).find()) {
			matcher = onlyWhileLoopDeclaration.matcher(currentCheckedLine);
			if (matcher.find()) {
				bracketLine = matcher.group(0);
				currentCheckedLine = currentCheckedLine.replaceFirst("^(\\s|\\t)*while(\\s)*\\((.*?)\\)",
						bracketLine + " {");
			}
		} else if (commentIfStatementWithoutBrackets.matcher(currentCheckedLine).find()) {
			matcher = onlyIfStatementDeclaration.matcher(currentCheckedLine);
			if (matcher.find()) {
				bracketLine = matcher.group(0);
				currentCheckedLine = currentCheckedLine.replaceFirst("^(\\s|\\t)*if(\\s)*\\((.*?)\\)",
						bracketLine + " {");
			}
		} else if (commentElifStatementWithoutBrackets.matcher(currentCheckedLine).find()) {
			matcher = onlyElifStatementDeclaration.matcher(currentCheckedLine);
			if (matcher.find()) {
				bracketLine = matcher.group(0);
				currentCheckedLine = currentCheckedLine.replaceFirst("^(\\s|\\t)*else if(\\s)*", bracketLine + " {");
			}
		} else if (commentElseStatementWithoutBrackets.matcher(currentCheckedLine).find()) {
			matcher = onlyElseStatementDeclaration.matcher(currentCheckedLine);
			if (matcher.find()) {
				bracketLine = matcher.group(0);
				currentCheckedLine = currentCheckedLine.replaceFirst("^(\\s|\\t)*else(\\s)*", bracketLine + " {");
			}
		} else {
			currentCheckedLine = currentCheckedLine + " {";
		}
		
		return currentCheckedLine;
	}

	
	private static Object[] checkForInnerScopes(ArrayList<String> list, int currentCheckedIndex) {
		
		String currentCheckedLine;
		int numOfLinesToIgnore = 0;
		Object[] answer = new Object[2];
		answer[0] = false;

		currentCheckedLine = list.get(++currentCheckedIndex);
		while (lineIsComment(currentCheckedLine)) {
			numOfLinesToIgnore++;
			currentCheckedIndex++;
			currentCheckedLine = list.get(currentCheckedIndex);
		}

		if (lineContainsOpeningBracket(currentCheckedLine)) {
			numOfLinesToIgnore++;
			while (lineIsComment(currentCheckedLine) || !lineContainsClosingBracket(currentCheckedLine)) {
				numOfLinesToIgnore++;
				currentCheckedIndex++;
				currentCheckedLine = list.get(currentCheckedIndex);
			}
			
			answer[0] = true;	
		}
		
		answer[1] = numOfLinesToIgnore;
		return answer;
	}

	
	private static boolean lineIsComment(String currentCheckedLine) {
		
		boolean lineIsComment = false;
		Matcher lineIsCommentMathcer;
		Pattern lineIsCommentPattern = Pattern.compile("^(\\s|\\t)*(//)");
		
		lineIsCommentMathcer = lineIsCommentPattern.matcher(currentCheckedLine);
		
		if (lineIsCommentMathcer.find()) {
			lineIsComment = true;
		}
		
		return lineIsComment;
	}
	
	
	private static boolean lineContainsOpeningBracket(String currentCheckedLine) {
		
		boolean lineContainsOpenBracket = false;
		Matcher lineContainsOpenBracketMatcher;
		Pattern lineContainsOpenBracketPattern = Pattern.compile(".*?\\{.*?");
		
		lineContainsOpenBracketMatcher = lineContainsOpenBracketPattern.matcher(currentCheckedLine);
		
		if (lineContainsOpenBracketMatcher.find()) {
			lineContainsOpenBracket = true;
		}
		
		return lineContainsOpenBracket;
	}
	
	
	private static boolean lineContainsClosingBracket(String currentCheckedLine) {
			
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
	 * @param line
	 * 		Checked line in the program's input.
	 * @return
	 * 		True if the method check succeed, else false.
	 */
	private static boolean recursiveCheck(ArrayList<String> list, int currentCheckedIndex, String line) {
		
		String nextLine = null;
		Matcher isBracket = null;
		Pattern openBracket = Pattern.compile("^(\\s|\\t)*\\{");
		
		if (newlineForLoopWithoutBrackets.matcher(line).find()
				|| commentForLoopWithoutBrackets.matcher(line).find()
				|| newlineWhileLoopWithoutBrackets.matcher(line).find()
				|| commentWhileLoopWithoutBrackets.matcher(line).find()
				|| newlineIfStatementWithoutBrackets.matcher(line).find()
				|| commentIfStatementWithoutBrackets.matcher(line).find()
				|| newlineElifStatementWithoutBrackets.matcher(line).find()
				|| commentElifStatementWithoutBrackets.matcher(line).find()
				|| newlineElseStatementWithoutBrackets.matcher(line).find()
				|| commentElseStatementWithoutBrackets.matcher(line).find()	) {

			nextLine = list.get(++currentCheckedIndex);
			while(lineIsComment(nextLine)) {
				nextLine = list.get(++currentCheckedIndex);
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
	 * @param type
	 * 		The expression that we want to check - loop or condition.
	 * @return
	 */
	public static boolean checkIfCommented(String currentCheckedLine, String type) {

		Matcher comment;

		switch (type) {
		case "for":
			comment = commentForLoopWithoutBrackets.matcher(currentCheckedLine);
			if (comment.find()) {
				return true;
			}
			break;

		case "while":
			comment = commentWhileLoopWithoutBrackets.matcher(currentCheckedLine);
			if (comment.find()) {
				return true;
			}
			break;

		case "if":
			comment = commentIfStatementWithoutBrackets.matcher(currentCheckedLine);
			if (comment.find()) {
				return true;
			}
			break;
			
		case "else if":
			comment = commentElifStatementWithoutBrackets.matcher(currentCheckedLine);
			if (comment.find()) {
				return true;
			}
			break;
			
		case "else":
			comment = commentElseStatementWithoutBrackets.matcher(currentCheckedLine);
			if (comment.find()) {
				return true;
			}
			break;
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

		checkInlineDecleration(list);
		checkDeclarationsWithoutBrackets(list);
		analyzerUtils.FileOperations.writeCodeToFileString(list, new File(reformattedFilePath));
	}
}