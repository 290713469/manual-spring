/**
 * 
 */
package com.yrk.spring.context;

/**
 * ͨ������ķ�ʽ���IOC�����Ķ������
 * ���潫ͨ��һ��������ȥɨ�������ֻ࣬Ҫʵ�ִ˽ӿڣ����Զ�����setApplicationContext ��������IOC����ע�뵽Ŀ����
 * @author Runkai Yang
 *
 */
public interface YApplicationContextAware {

	void setApplicationContext(YApplicationContext applicationContext);
}
