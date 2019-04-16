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
		
		//1、初始化ApplicationContext
		applicationContext = new YApplicationContext(config.getInitParameter(CONTEXT_CONFIG_LOCATIONS));
		//2、初始化Spring MVC 九大组件
		initStrategies(applicationContext);
	}
	
	protected void initStrategies(YApplicationContext context) {
		//多文件上传的组件， 如果请求类型是multipart， 将通过MultipartResolver进行文件上传解析
		initMultipartResolver(context);
		//初始化本地语言环境
		initLocaleResolver(context);
		//初始化模板处理器
		initThemeResolver(context);
		
		// YHandlerMapping 用来保存Controller中配置的RequestMapping和Method的一个对应关系
		// 通过handlerMapping, 将请求映射到处理器
		initHandlerMappings(context);
		//初始化参数适配器， HandlerAdapter用来动态匹配Method参数， 包括类型转换，动态赋值
		initHandlerAdapters(context);
		//初始化异常拦截器
		initHandlerExceptionResolvers(context);
		//直接解析请求到视图名
		initRequestToViewNameTranslator(context);
		
		//通过viewResolver解析逻辑视图到具体视图实现
		initViewResolvers(context);
		// flash映射管理器，参数缓存器
		initFlashMapManager(context);
	}
	
	

	private void initViewResolvers(YApplicationContext context) {
		// 在页面敲一个http://localhost/first.html， 解决页面名字和模板文件关联的问题
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

	

	private void initHandlerAdapters(YApplicationContext context) {
		// 在初始化阶段，我们能做的就是讲这些参数的名字或者类型按照一定顺序保存下来
		// 因为后面用反射调用的时候，传的形参是一个数组
		// 可以通过记录这些参数的位置index，从数组中填值，这样的话就和参数顺序无关了。
        for (YHandlerMapping handlerMapping : this.handlerMappings) {
            this.handlerAdapters.put(handlerMapping,new YHandlerAdapter());
        }
	}
	
	// 将Controller中配置的RequestMapping和Method进行一一对应
	private void initHandlerMappings(YApplicationContext context) {
		// 获取容器中所有的实例
		String[] beanDefinitionNames = context.getBeanDefinitionNames();
		try {
		for (String beanName : beanDefinitionNames) {
			Object beanInstance = context.getBean(beanName);
			Class<?> clazz = beanInstance.getClass();
			if (!clazz.isAnnotationPresent(YController.class)) {
				continue;
			}
			
			String baseUrl = "";
            if(clazz.isAnnotationPresent(YRequestMapping.class)){
                YRequestMapping requestMapping = clazz.getAnnotation(YRequestMapping.class);
                baseUrl = requestMapping.value();
            }

            Method[] methods = clazz.getMethods();
            for (Method method : methods) {

                if(!method.isAnnotationPresent(YRequestMapping.class)){ continue; }

                YRequestMapping requestMapping = method.getAnnotation(YRequestMapping.class);
                //  /demo/query

                //  (//demo//query)

                String regex = ("/" + baseUrl + "/" + requestMapping.value().replaceAll("\\*",".*")).replaceAll("/+", "/");
                Pattern pattern = Pattern.compile(regex);

                this.handlerMappings.add(new YHandlerMapping(pattern,beanInstance,method));
//                log.info("Mapped " + regex + "," + method);
            }
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	private void initFlashMapManager(YApplicationContext context) {}
	private void initRequestToViewNameTranslator(YApplicationContext context) {}
	private void initHandlerExceptionResolvers(YApplicationContext context) {}
	private void initThemeResolver(YApplicationContext context) {}
	private void initLocaleResolver(YApplicationContext context) {}
	private void initMultipartResolver(YApplicationContext context) {}

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
        	processDispatchResult(req,resp,new YModelAndView("404"));
            return;
        }

      //2、准备调用前的参数
        YHandlerAdapter ha = getHandlerAdapter(handler);

      //3、真正的调用方法,返回ModelAndView存储了要穿页面上值，和页面模板的名称
        YModelAndView mv = ha.handle(req,resp,handler);

      //这一步才是真正的输出
        processDispatchResult(req, resp, mv);


    }
	
	private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, YModelAndView mv) throws Exception{
		//把给我的ModleAndView变成一个HTML、OuputStream、json、freemark、veolcity
        //ContextType
        if(null == mv){return;}
      //如果ModelAndView不为null，怎么办？
        if(this.viewResolvers.isEmpty()){return;}

        for (YViewResolver viewResolver : this.viewResolvers) {
            YView view = viewResolver.resolveViewName(mv.getViewName(),null);
            view.render(mv.getModel(),req,resp);
            return;
        }
    }

    private YHandlerAdapter getHandlerAdapter(YHandlerMapping handler) {
    	if(this.handlerAdapters.isEmpty()){return null;}
        YHandlerAdapter ha = this.handlerAdapters.get(handler);
        if(ha.supports(handler)){
            return ha;
        }
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
