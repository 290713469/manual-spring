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
		//1. ��ʼ��
		instantiateBean(name, new YBeanDefinition());
		//2. ע��
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
		
		// ��λ�����ļ�
		beanDefinitionReader = new YBeanDefinitionReader(this.configLocations);
		//���������ļ���ɨ����ص��࣬��װ��BeanDefinition
		List<YBeanDefinition> beanDefinitions = beanDefinitionReader.loadBeanDefinitions();
		
		//ע�ᣬ��������Ϣ�ŵ�IOC����
		doRegisterBeanDefinition(beanDefinitions);
		
		//�Ѳ����ӳټ��ص��࣬��ǰ��ʼ��
		doAutowried();
	}

	//ֻ������ӳټ���
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
