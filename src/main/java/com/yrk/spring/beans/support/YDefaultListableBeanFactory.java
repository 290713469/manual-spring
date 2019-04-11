/**
 * 
 */
package com.yrk.spring.beans.support;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.yrk.spring.beans.factory.config.YBeanDefinition;
import com.yrk.spring.context.support.YAbstractApplicationContext;

/**
 * @author Runkai Yang
 *
 */
public class YDefaultListableBeanFactory extends YAbstractApplicationContext {
	
	private final Map<String, YBeanDefinition> beanDefinitionMap = new ConcurrentHashMap<String, YBeanDefinition>(256);

	public Map<String, YBeanDefinition> getBeanDefinitionMap() {
		return beanDefinitionMap;
	}


}
