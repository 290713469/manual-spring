/**
 * 
 */
package com.yrk.spring.beans.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

	private final String SCANNER_PACKAGE = "scanPackage";
	private Properties properties = new Properties();
	private List<String> registyBeanClasses = new ArrayList<String>();
	
	public YBeanDefinitionReader(String... locations) {
		//通过URL定位找到对应的文件，转化为文件流，并读取
		FileInputStream fis = null;
		try {
			fis = (FileInputStream) this.getClass().getClassLoader().getResourceAsStream(locations[0]);
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
		URL url = this.getClass().getClassLoader().getResource("/" + properies.replaceAll("\\.", "/"));
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

	public List<YBeanDefinition> loadBeanDefinitions() {
		List<YBeanDefinition> results = new ArrayList<YBeanDefinition>();
		for (String beanClass : registyBeanClasses) {
			YBeanDefinition beanDefinition = doCreateBeanDefinition(beanClass);
			if (beanDefinition == null) {
				continue;
			}
			results.add(beanDefinition);
		}
		return results;
	}
	
	private YBeanDefinition doCreateBeanDefinition(String className) {
		try {
				Class<?> clazz = Class.forName(className);
				if (!clazz.isInterface()) {
					YBeanDefinition beanDefinition = new YBeanDefinition();
					beanDefinition.setBeanClassName(className);
					beanDefinition.setFactoryBeanName(toLowerFirstCase(clazz.getSimpleName()));
					beanDefinition.setLazyInit(false);
					return beanDefinition;
				}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private String toLowerFirstCase(String simpleName) {
		char[] chars = simpleName.toCharArray();
		chars[0] += 32;
		return new String(chars);
	}
}
