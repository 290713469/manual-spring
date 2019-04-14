/**
 * 
 */
package com.yrk.spring.context;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import com.yrk.spring.annotation.YAutowired;
import com.yrk.spring.annotation.YController;
import com.yrk.spring.annotation.YService;
import com.yrk.spring.beans.YBeanWrapper;
import com.yrk.spring.beans.factory.YBeanFactory;
import com.yrk.spring.beans.factory.config.YBeanDefinition;
import com.yrk.spring.beans.factory.config.YBeanPostProcessor;
import com.yrk.spring.beans.support.YBeanDefinitionReader;
import com.yrk.spring.beans.support.YDefaultListableBeanFactory;

/**
 * @author Runkai Yang
 *
 */
public class YApplicationContext extends YDefaultListableBeanFactory implements YBeanFactory {

	private String[] configLocations;
	private YBeanDefinitionReader beanDefinitionReader;
	//单例的IOC容器缓存
	private Map<String, Object> singletonObjects = new HashMap<String, Object>();
	//通用IOC容器
	private Map<String, YBeanWrapper> factoryBeanInstanceCache = new HashMap<String, YBeanWrapper>();
	
	public YApplicationContext(String... configLocations) {
		this.configLocations = configLocations;
		try {
			refresh();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public Object getBean(String name) {
		//1. 初始化
		Object instance = null;
		YBeanPostProcessor postProcessor = new YBeanPostProcessor();
        postProcessor.postProcessBeforeInitialization(instance,name);
        instance = instantiateBean(name, this.getBeanDefinitionMap().get(name));
		
		YBeanWrapper beanWrapper = new YBeanWrapper(instance);
		
		factoryBeanInstanceCache.put(name, beanWrapper);
		
		//2. 注入
		populateBean(name, this.getBeanDefinitionMap().get(name), beanWrapper);
		return factoryBeanInstanceCache.get(name).getWrappedInstance();
	}
	
	private void populateBean(String name, YBeanDefinition beanDefinition, YBeanWrapper beanWrapper) {
		Object instance = beanWrapper.getWrappedInstance();
		if (!(instance.getClass().isAnnotationPresent(YController.class) || instance.getClass().isAnnotationPresent(YService.class))) {
			return;
		}
		
		Field[] fields = instance.getClass().getDeclaredFields();
		for (Field field : fields) {
			if (!field.isAnnotationPresent(YAutowired.class)) {
				continue;
			}
			YAutowired autowired = field.getAnnotation(YAutowired.class);
			String autoWiredBeanName = autowired.value().trim();
			if ("".equals(autoWiredBeanName)) {
				autoWiredBeanName = field.getType().getName();
			}
			field.setAccessible(true);
			try {
				field.set(instance, this.factoryBeanInstanceCache.get(autoWiredBeanName).getWrappedInstance());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private Object instantiateBean(String name, YBeanDefinition beanDefinition) {
		//1. 拿到要实例化对象的类名
		String beanName = beanDefinition.getBeanClassName();
		Object instance = null;
	
		//2. 反射实例化，得到对象
		try {
			if (this.singletonObjects.containsKey(beanName)) {
				instance = this.singletonObjects.get(beanName);
			} else {
				Class<?> beanClass = Class.forName(beanName);
				instance = beanClass.newInstance();
				this.singletonObjects.put(beanName, instance);
				this.singletonObjects.put(beanDefinition.getFactoryBeanName(), instance);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return instance;
		
	}

	public void refresh()  throws Exception{
		
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
	
	 private void doRegisterBeanDefinition(List<YBeanDefinition> beanDefinitions) throws Exception {

	        for (YBeanDefinition beanDefinition: beanDefinitions) {
	            if(super.getBeanDefinitionMap().containsKey(beanDefinition.getFactoryBeanName())){
	                throw new Exception("The “" + beanDefinition.getFactoryBeanName() + "” is exists!!");
	            }
	            super.getBeanDefinitionMap().put(beanDefinition.getFactoryBeanName(),beanDefinition);
	        }
	        //到这里为止，容器初始化完毕
	    }

	 public String[] getBeanDefinitionNames() {
	        return super.getBeanDefinitionMap().keySet().toArray(new  String[super.getBeanDefinitionMap().size()]);
	    }

	    public int getBeanDefinitionCount(){
	        return super.getBeanDefinitionMap().size();
	    }
	    
	    public Properties getConfig(){
	        return this.beanDefinitionReader.getConfig();
	    }
}
