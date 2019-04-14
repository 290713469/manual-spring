/**
 * 
 */
package com.yrk.spring.test;

import com.yrk.spring.context.YApplicationContext;

/**
 * @author Runkai Yang
 *
 */
public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		YApplicationContext context = new YApplicationContext("classpath:application.properties");
		System.out.println(context);
	}

}
