/**
 * 
 */
package com.yrk.spring.context;

import java.util.List;
import java.util.Map.Entry;

import com.yrk.spring.beans.YBeanWrapper;
import com.yrk.spring.beans.factory.YBeanFactory;
import com.yrk.spring.beans.factory.config.YBeanDefinition;
import com.yrk.spring.beans.support.YBeanDefinitionReader;
import com.yrk.spring.beans.support.YDefaultListableBeanFactory;

/**
 * @author Runkai Yang
 *
 */
public class YApplicationContext extends YDefaultListableBeanFactory implements YBeanFactory {

	private String[] configLocations;
	private YBeanDefinitionReader beanDefinitionReader;
	
	public YApplicationContext(String[] configLocations) {
		this.configLocations = configLocations;
		refresh();
	}
	
	@Override
	public Object getBean(String name) {
		//1. 初始化
		instantiateBean(name, new YBeanDefinition());
		//2. 注入
		populateBean(name, new YBeanDefinition(), new YBeanWrapper());
		return null;
	}
	
	private void populateBean(String name, YBeanDefinition yBeanDefinition, YBeanWrapper yBeanWrapper) {
		// TODO Auto-generated method stub
		
	}

	private void instantiateBean(String name, YBeanDefinition yBeanDefinition) {
		// TODO Auto-generated method stub
		
	}

	public void refresh() {
		
		// 定位配置文件
		beanDefinitionReader = new YBeanDefinitionReader(this.configLocations);
		//加载配置文件，扫描相关的类，封装成BeanDefinition
		List<YBeanDefinition> beanDefinitions = beanDefinitionReader.loadBeanDefinitions();
		
		//注册，把配置信息放到IOC容器
		doRegisterBeanDefinition(beanDefinitions);
		
		//把不是延迟加载的类，提前初始化
		doAutowried();
	}

	//只处理非延迟加载
	private void doAutowried() {
		for (Entry<String, YBeanDefinition> entry : super.getBeanDefinitionMap().entrySet()) {
			String factoryBeanName = entry.getKey();
			if (!entry.getValue().isLazyInit()) {
				getBean(factoryBeanName);
			}
		}
	}

	private void doRegisterBeanDefinition(List<YBeanDefinition> beanDefinitions) {
		beanDefinitions.forEach(beanDefinition -> {
			super.getBeanDefinitionMap().put(beanDefinition.getFactoryBeanName(), beanDefinition);
		});
	}

}
