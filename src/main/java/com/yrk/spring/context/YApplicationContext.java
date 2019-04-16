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
 * IOC核心容器
 * @author Runkai Yang
 *
 */
public class YApplicationContext extends YDefaultListableBeanFactory implements YBeanFactory {

	private String[] configLocations;
	private YBeanDefinitionReader beanDefinitionReader;
	//单例IOC容器缓存
	private Map<String, Object> factoryBeanObjectCache = new HashMap<String, Object>();
	//通用的IOC容器
	private Map<String, YBeanWrapper> factoryBeanInstanceCache = new HashMap<String, YBeanWrapper>();
	
	public YApplicationContext(String... configLocations) {
		this.configLocations = configLocations;
		try {
			refresh();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	

	public void refresh()  throws Exception{
		
		// 1. 定位， 定位配置文件
		beanDefinitionReader = new YBeanDefinitionReader(this.configLocations);
		
		// 2. 加载配置文件，扫描相关的类，把他们封装成BeanDefinition
		List<YBeanDefinition> beanDefinitions = beanDefinitionReader.loadBeanDefinitions();
		
		// 3. 注册，把配置信息放到IOC容器里面 (伪IOC容器)
		doRegisterBeanDefinition(beanDefinitions);
		
		// 4. 把不是延迟加载的类，提前初始化
		doAutowried();
	}

	// 只处理非延迟加载的类
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
	                throw new Exception("The " + beanDefinition.getFactoryBeanName() + " is exists!!");
	            }
	            super.getBeanDefinitionMap().put(beanDefinition.getFactoryBeanName(),beanDefinition);
	        }
	      //到这里为止，容器初始化完毕
	    }

	//依赖注入，从这里开始，通过读取BeanDefinition中的信息
	    //然后，通过反射机制创建一个实例并返回
	    //Spring做法是，不会把最原始的对象放出去，会用一个BeanWrapper来进行一次包装
	    //装饰器模式：
	    //1、保留原来的OOP关系
	    //2、我需要对它进行扩展，增强（为了以后AOP打基础）
	 @Override
		public Object getBean(String name) {
			Object instance = null;
			YBeanPostProcessor postProcessor = new YBeanPostProcessor();
			instance = instantiateBean(name, this.getBeanDefinitionMap().get(name));
			if (instance == null) {
				return null;
			}
	        postProcessor.postProcessBeforeInitialization(instance,name);
	        
			
			YBeanWrapper beanWrapper = new YBeanWrapper(instance);
			
			factoryBeanInstanceCache.put(name, beanWrapper);
			
			// 注入
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
					if(this.factoryBeanInstanceCache.get(autoWiredBeanName) == null){ continue; }
					field.set(instance, this.factoryBeanInstanceCache.get(autoWiredBeanName).getWrappedInstance());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		private Object instantiateBean(String name, YBeanDefinition beanDefinition) {
			//1、拿到要实例化的对象的类名
			String beanName = beanDefinition.getBeanClassName();
			Object instance = null;
		
			//2、反射实例化，得到一个对象
			try {
				//假设默认就是单例
				if (this.factoryBeanObjectCache.containsKey(beanName)) {
					instance = this.factoryBeanObjectCache.get(beanName);
				} else {
					Class<?> beanClass = Class.forName(beanName);
					instance = beanClass.newInstance();
					this.factoryBeanObjectCache.put(beanName, instance);
					this.factoryBeanObjectCache.put(beanDefinition.getFactoryBeanName(), instance);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return instance;
			
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
