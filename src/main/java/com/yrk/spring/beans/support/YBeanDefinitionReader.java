/**
 * 
 */
package com.yrk.spring.beans.support;

import java.io.File;
import java.io.FileInputStream;
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

	private final String SCANNER_PACKAGE = "scanPackage";
	private Properties properties = new Properties();
	private List<String> registyBeanClasses = new ArrayList<String>();
	
	public YBeanDefinitionReader(String... locations) {
		//ͨ��URL��λ�ҵ���Ӧ���ļ���ת��Ϊ�ļ���������ȡ
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

	public List<YBeanDefinition> loadBeanDefinitions() {
		List<YBeanDefinition> results = new ArrayList<YBeanDefinition>();
		try {
			for (String beanClass : registyBeanClasses) {
				Class<?> clazz = Class.forName(beanClass);
				if (!clazz.isInterface()) {
					//beanName���������:
	                //1��Ĭ������������ĸСд
	                //2���Զ�������
	                //3���ӿ�ע��
	                results.add(doCreateBeanDefinition(toLowerFirstCase(clazz.getSimpleName()),clazz.getName()));
	                results.add(doCreateBeanDefinition(clazz.getName(),clazz.getName()));
	                Class<?>[] interfaces = clazz.getInterfaces();
	                for (Class<?> interfaceClazz : interfaces) {
	                	results.add(doCreateBeanDefinition(interfaceClazz.getName(),clazz.getName()));
	                }
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
	
	private String toLowerFirstCase(String simpleName) {
		char[] chars = simpleName.toCharArray();
		chars[0] += 32;
		return new String(chars);
	}
}
