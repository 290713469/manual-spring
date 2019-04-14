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
		
		//1. 初始化ApplicationContext
		applicationContext = new YApplicationContext(config.getInitParameter(CONTEXT_CONFIG_LOCATIONS));
		//2. 初始化spring mvc 九大组件 
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
		//拿到模板的存放目录
        String templateRoot = context.getConfig().getProperty("templateRoot");
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();

        File templateRootDir = new File(templateRootPath);
        String[] templates = templateRootDir.list();
        for (int i = 0; i < templates.length; i ++) {
            //这里主要是为了兼容多模板，所有模仿Spring用List保存
            //在我写的代码中简化了，其实只有需要一个模板就可以搞定
             //只是为了仿真，所有还是搞了个List
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
		 //把一个requet请求变成一个handler，参数都是字符串的，自动配到handler中的形参

        //可想而知，他要拿到HandlerMapping才能干活
        //就意味着，有几个HandlerMapping就有几个HandlerAdapter
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
            //获取Controller的url配置
            if(clazz.isAnnotationPresent(YRequestMapping.class)){
                YRequestMapping requestMapping = clazz.getAnnotation(YRequestMapping.class);
                baseUrl = requestMapping.value();
            }

            //获取Method的url配置
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {

                //没有加RequestMapping注解的直接忽略
                if(!method.isAnnotationPresent(YRequestMapping.class)){ continue; }

                //映射URL
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
        //1、通过从request中拿到URL，去匹配一个HandlerMapping
        YHandlerMapping handler = getHandler(req);

        if(handler == null){
            //new ModelAndView("404")
            return;
        }

        //2、准备调用前的参数
        YHandlerAdapter ha = getHandlerAdapter(handler);

        //3、真正的调用方法,返回ModelAndView存储了要穿页面上值，和页面模板的名称
        YModelAndView mv = ha.handle(req,resp,handler);


        processDispatchResult(req, resp, mv);


    }
	
	private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, YModelAndView mv) {
        //把给我的ModleAndView变成一个HTML、OuputStream、json、freemark、veolcity
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
                //如果没有匹配上继续下一个匹配
                if(!matcher.matches()){ continue; }

                return handler;
            }catch(Exception e){
                throw e;
            }
        }
        return null;
    }
}
