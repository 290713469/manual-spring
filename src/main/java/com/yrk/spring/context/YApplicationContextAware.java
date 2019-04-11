/**
 * 
 */
package com.yrk.spring.context;

/**
 * 通过解耦的方式获得IOC容器的顶层设计
 * 后面将通过一个监听器去扫描所有类，只要实现此接口，将自动调用setApplicationContext 方法，将IOC容器注入到目标中
 * @author Runkai Yang
 *
 */
public interface YApplicationContextAware {

	void setApplicationContext(YApplicationContext applicationContext);
}
