/**
 * 
 */
package com.yrk.spring.webmvc.servlet;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yrk.spring.annotation.YController;
import com.yrk.spring.annotation.YRequestMapping;
import com.yrk.spring.context.YApplicationContext;

/**
 * @author runkaiyang
 *
 */
public class ManualServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String CONTEXT_CONFIG_LOCATIONS = "contextConfigLocations";
	private YApplicationContext applicationContext = null;
	 private List<YHandlerMapping> handlerMappings = new ArrayList<YHandlerMapping>();

	    private Map<YHandlerMapping,YHandlerAdapter> handlerAdapters = new HashMap<YHandlerMapping,YHandlerAdapter>();

	    private List<YViewResolver> viewResolvers = new ArrayList<YViewResolver>();

	@Override
	public void init(ServletConfig config) throws ServletException {
		
		//1. ��ʼ��ApplicationContext
		applicationContext = new YApplicationContext(config.getInitParameter(CONTEXT_CONFIG_LOCATIONS));
		//2. ��ʼ��spring mvc �Ŵ���� 
		initStrategies(applicationContext);
	}
	
	protected void initStrategies(YApplicationContext context) {
		initMultipartResolver(context);
		initLocaleResolver(context);
		initThemeResolver(context);
		initHandlerMappings(context);
		initHandlerAdapters(context);
		initHandlerExceptionResolvers(context);
		initRequestToViewNameTranslator(context);
		initViewResolvers(context);
		initFlashMapManager(context);
	}
	
	private void initFlashMapManager(YApplicationContext context) {
		// TODO Auto-generated method stub
		
	}

	private void initViewResolvers(YApplicationContext context) {
		//�õ�ģ��Ĵ��Ŀ¼
        String templateRoot = context.getConfig().getProperty("templateRoot");
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();

        File templateRootDir = new File(templateRootPath);
        String[] templates = templateRootDir.list();
        for (int i = 0; i < templates.length; i ++) {
            //������Ҫ��Ϊ�˼��ݶ�ģ�壬����ģ��Spring��List����
            //����д�Ĵ����м��ˣ���ʵֻ����Ҫһ��ģ��Ϳ��Ը㶨
             //ֻ��Ϊ�˷��棬���л��Ǹ��˸�List
            this.viewResolvers.add(new YViewResolver(templateRoot));
        }
	}

	private void initRequestToViewNameTranslator(YApplicationContext context) {
		// TODO Auto-generated method stub
		
	}

	private void initHandlerExceptionResolvers(YApplicationContext context) {
		// TODO Auto-generated method stub
		
	}

	private void initHandlerAdapters(YApplicationContext context) {
		 //��һ��requet������һ��handler�����������ַ����ģ��Զ��䵽handler�е��β�

        //�����֪����Ҫ�õ�HandlerMapping���ܸɻ�
        //����ζ�ţ��м���HandlerMapping���м���HandlerAdapter
        for (YHandlerMapping handlerMapping : this.handlerMappings) {
            this.handlerAdapters.put(handlerMapping,new YHandlerAdapter());
        }
	}

	private void initHandlerMappings(YApplicationContext context) {
		
		String[] beanDefinitionNames = context.getBeanDefinitionNames();
		try {
		for (String beanName : beanDefinitionNames) {
			Object obj = context.getBean(beanName);
			Class<?> clazz = obj.getClass();
			if (!clazz.isAnnotationPresent(YController.class)) {
				continue;
			}
			
			String baseUrl = "";
            //��ȡController��url����
            if(clazz.isAnnotationPresent(YRequestMapping.class)){
                YRequestMapping requestMapping = clazz.getAnnotation(YRequestMapping.class);
                baseUrl = requestMapping.value();
            }

            //��ȡMethod��url����
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {

                //û�м�RequestMappingע���ֱ�Ӻ���
                if(!method.isAnnotationPresent(YRequestMapping.class)){ continue; }

                //ӳ��URL
                YRequestMapping requestMapping = method.getAnnotation(YRequestMapping.class);
                //  /demo/query

                //  (//demo//query)

                String regex = ("/" + baseUrl + "/" + requestMapping.value().replaceAll("\\*",".*")).replaceAll("/+", "/");
                Pattern pattern = Pattern.compile(regex);

                this.handlerMappings.add(new YHandlerMapping(pattern,obj,method));
//                log.info("Mapped " + regex + "," + method);
            }
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	private void initThemeResolver(YApplicationContext context) {
		// TODO Auto-generated method stub
		
	}

	private void initLocaleResolver(YApplicationContext context) {
		// TODO Auto-generated method stub
		
	}

	private void initMultipartResolver(YApplicationContext context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
		doPost(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
		try {
			doDispatch(req, resp);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception{
        //1��ͨ����request���õ�URL��ȥƥ��һ��HandlerMapping
        YHandlerMapping handler = getHandler(req);

        if(handler == null){
            //new ModelAndView("404")
            return;
        }

        //2��׼������ǰ�Ĳ���
        YHandlerAdapter ha = getHandlerAdapter(handler);

        //3�������ĵ��÷���,����ModelAndView�洢��Ҫ��ҳ����ֵ����ҳ��ģ�������
        YModelAndView mv = ha.handle(req,resp,handler);


        processDispatchResult(req, resp, mv);


    }
	
	private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, YModelAndView mv) {
        //�Ѹ��ҵ�ModleAndView���һ��HTML��OuputStream��json��freemark��veolcity
        //ContextType
        if(null == mv){return;}

    }

    private YHandlerAdapter getHandlerAdapter(YHandlerMapping handlerMapping) {
        return null;
    }


    private YHandlerMapping getHandler(HttpServletRequest req) throws Exception{
        if(this.handlerMappings.isEmpty()){ return null; }

        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");

        for (YHandlerMapping handler : this.handlerMappings) {
            try{
                Matcher matcher = handler.getPattern().matcher(url);
                //���û��ƥ���ϼ�����һ��ƥ��
                if(!matcher.matches()){ continue; }

                return handler;
            }catch(Exception e){
                throw e;
            }
        }
        return null;
    }
}
