package analyzedClass.analyzedMethod;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class MethodOperations {

	/**
	 * using reflection, fetching aClass's declared methods names
	 * 
	 * @param aClass
	 *            class of the object to fetch the methods names
	 * @return list of aClass's methods names
	 */
	public static ArrayList<String> getMethodsName(Class<?> aClass) {

		// Get only the declared methods 
		Method[] methods = aClass.getDeclaredMethods();
		ArrayList<String> methodsName = new ArrayList<String>();
		// Loop through the methods and print out their names
		for (Method method : methods) {
			methodsName.add(method.getName());
		}
		return methodsName;
	}
	

	/**
	 * using reflection, fetching a Class object of 'className' class and
	 * fetching from it all his declared methods names
	 * 
	 * @param className
	 *            name of the class to fetch the methods names from
	 * @return list of aClass's methods names
	 * @exception ClassNotFoundException
	 *                from Class.forName(className)
	 */
	public static ArrayList<String> getMethodsName(String className) {
		Class<?> cls = null;
		try {
			cls = Class.forName(className);
		} catch (ClassNotFoundException e) {
			// incorrect class name
			e.printStackTrace();
		}
		Method[] methods = cls.getDeclaredMethods();
		ArrayList<String> methodsName = new ArrayList<String>();
		// Loop through the methods and print out their names
		for (Method method : methods) {
			methodsName.add(method.getName());
		}
		return methodsName;
	}

	/**
	 * find the index of the end of the scope.
	 * scan 'codeBuffer' for the end of the scope, setting counter to 1,
	 * incrementing it when finds '{' and decreasing when finds '}', when the
	 * counter == 0 we found the line than ends the scope returning its index,
	 * (same logic as using a stack)
	 * 
	 * @param codeBuffer
	 *            list of the code lines
	 * @param startPoint
	 *            index of the line that start the scope
	 * 
	 * @return index of the line that ends the scope
	 */
	public static int findEndOfScope(ArrayList<StringBuilder> codeBuffer, int startPoint) {
		int i = startPoint + 1;
		String line;
		int countCurlyBraces = 1;
		for (; i < codeBuffer.size(); i++) {

			line = codeBuffer.get(i).toString();
			// maybe using recursion for the inside scope searching after inside
			// for loop

			// if found '{' add to the counter
			if (line.matches(".*(\\{).*")) {
				countCurlyBraces++;
			}
			// if found '}' subtract from the counter
			if (line.matches(".*(\\}).*")) {
				countCurlyBraces--;
			}
			// when the counter equals to 0 we at the end of the for scope
			if (countCurlyBraces == 0) {
				// found the line with the } pair of the staring {
				break;
			}
		}
		return i;

	}

}
