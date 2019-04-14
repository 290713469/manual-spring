/**
 * 
 */
package com.yrk.spring.beans;

/**
 * @author Runkai Yang
 *
 */
public class YBeanWrapper {
	
	private Object wrappedInstane;
	private Class<?> wrappedClass;
	
	
	
	public YBeanWrapper(Object wrappedInstane) {
		this.wrappedInstane = wrappedInstane;
		wrappedClass = this.wrappedInstane.getClass();
	}

	public Object getWrappedInstance() {
		return wrappedInstane;
	}

	public Class<?> getWrappedClass() {
		return wrappedClass;
	}

}
