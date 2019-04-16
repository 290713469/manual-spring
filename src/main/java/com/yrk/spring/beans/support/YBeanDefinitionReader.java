/**
 * 
 */
package com.yrk.spring.beans.support;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.yrk.spring.beans.factory.config.YBeanDefinition;

/**
 * @author Runkai Yang
 *
 */
public class YBeanDefinitionReader {
	
	//固定配置文件中的key
	private final String SCANNER_PACKAGE = "scanPackage";
	private Properties properties = new Properties();
	private List<String> registyBeanClasses = new ArrayList<String>();
	
	public YBeanDefinitionReader(String... locations) {
		//通过URL定位找到其所对应的配置文件，然后转化为文件流
		InputStream fis = null;
		try {
			fis = (InputStream) this.getClass().getClassLoader().getResourceAsStream(locations[0].replaceAll("classpath:", ""));
			properties.load(fis);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		doScanner(properties.getProperty(SCANNER_PACKAGE));
	}
	
	private void doScanner(String properies) {
		//转化成文件路径，实际上就是把.转化成/
		URL url = this.getClass().getResource("/" + properies.replaceAll("\\.", "/"));
		File baseFile = new File(url.getFile());
		for (File file : baseFile.listFiles()) {
			if (file.isDirectory()) {
				doScanner(properies + "." + file.getName());
			} else {
				if (!file.getName().endsWith(".class")) {
					continue;
				}
				String className = properies + "." + file.getName().replace(".class", "");
				registyBeanClasses.add(className);
				
			}
		}
	}
	
	public Properties getConfig() {
		return this.properties;
	}
	
	// 把配置文件中扫描到的所有配置信息转化成YBeanDefinition对象，以便于之后IOC操作方便
	public List<YBeanDefinition> loadBeanDefinitions() {
		List<YBeanDefinition> results = new ArrayList<YBeanDefinition>();
		try {
			for (String beanClass : registyBeanClasses) {
				Class<?> clazz = Class.forName(beanClass);
				//如果是一个借口，是不能实例化的，用实现类实例化
				if (clazz.isInterface()) {continue;}
				// beanName有三种情况
				// 1. 默认是类名首字母小写
				// 2. 自定义名字
				// 3. 接口注入
				results.add(doCreateBeanDefinition(toLowerFirstCase(clazz.getSimpleName()),clazz.getName()));
                results.add(doCreateBeanDefinition(clazz.getName(),clazz.getName()));
                Class<?>[] interfaces = clazz.getInterfaces();
                for (Class<?> interfaceClazz : interfaces) {
                	// 如果是一个借口有多个实现类，只能覆盖，否则只能自定义名字。
                	results.add(doCreateBeanDefinition(interfaceClazz.getName(),clazz.getName()));
                }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return results;
	}
	
	private YBeanDefinition doCreateBeanDefinition(String factoryBeanName,String beanClassName) {
		YBeanDefinition beanDefinition = new YBeanDefinition();
		beanDefinition.setBeanClassName(beanClassName);
        beanDefinition.setFactoryBeanName(factoryBeanName);
		beanDefinition.setLazyInit(false);
		return beanDefinition;
	}
	
	//如果类名本身是小写字母，确实会出问题
    //但是我要说明的是：这个方法是我自己用，private的
    //传值也是自己传，类也都遵循了驼峰命名法
    //默认传入的值，存在首字母小写的情况，也不可能出现非字母的情况

    //为了简化程序逻辑，就不做其他判断了，大家了解就OK
    //其实用写注释的时间都能够把逻辑写完了
	private String toLowerFirstCase(String simpleName) {
		char[] chars = simpleName.toCharArray();
		//之所以加，是因为大小写字母的ASCII码相差32，
        // 而且大写字母的ASCII码要小于小写字母的ASCII码
        //在Java中，对char做算学运算，实际上就是对ASCII码做算学运算
		chars[0] += 32;
		return new String(chars);
	}
}
