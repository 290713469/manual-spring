/**
 * 
 */
package com.yrk.spring.beans.factory;

/**
 * 单例工厂的顶层设计
 * @author runkaiyang
 *
 */
public interface YBeanFactory {
	
	Object getBean(String name);

}
