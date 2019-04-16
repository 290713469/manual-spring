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
	
	/**
	 * 根据beanName从IOC容器中获取一个实例bean
	 * @param name
	 * @return
	 */
	Object getBean(String name);

}
