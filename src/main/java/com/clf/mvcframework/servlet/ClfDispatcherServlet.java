package com.clf.mvcframework.servlet;

import com.clf.mvcframework.annotation.MyAutowired;
import com.clf.mvcframework.annotation.MyController;
import com.clf.mvcframework.annotation.MyRequestMapping;
import com.clf.mvcframework.annotation.MyService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * @Author: clf
 * @Date: 19-1-30
 * @Description:
 * 完成整个Spring的初始化阶段
 */
public class ClfDispatcherServlet extends HttpServlet {

    /**
     * 自定义加载配置信息的Context
     */
    private Properties contextConfig = new Properties();
    /**
     * 存放所有类对象的集合
     */
    private List<String> classNames = new ArrayList<String>();
    /**
     * 自定义ioc容器
     */
    private Map<String, Object> ioc = new HashMap<String, Object>();
    /**
     * 自定义一个HandlerMapping
     */
    private Map<String, Method> handlerMapping = new HashMap<String, Method>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //调用doGet或者doPost方法 反射调用,将结果输出到浏览器
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500 Exception" + Arrays.toString(e.getStackTrace()));
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws InvocationTargetException, IllegalAccessException {
        if (this.handlerMapping.isEmpty()){ return; }
        //获取请求的绝对路径
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        //获取相对路径
        url.replace(contextPath, "").replaceAll("/+", "/");
        if (!this.handlerMapping.containsKey(url)){
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        //通过HandlerMapping获取当前请求的方法
        Method method = this.handlerMapping.get(url);
        //获取请求的实参
        Map<String, String[]> params = req.getParameterMap();
        String beanName = lowerFirstCase(method.getDeclaringClass().getSimpleName());
        String name = null;
        if (params.containsKey("name")){
            name = params.get("name")[0];
        }
        method.invoke(ioc.get(beanName), new Object[]{req, resp, name});
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        /**
         * 1.加载配置文件
         */
        System.out.println("获取初始参数:" + config.getInitParameter("contextConfigLocation"));
        doLoadConfig(config.getInitParameter("contextConfigLocation"));
        /**
         * 2.解析配置文件,扫描所有相关的类
         */
        //获取到properties中的scanPackage=com.clf.demo
        doScanner(contextConfig.getProperty("scanPackage"));
        /**
         * 3.初始化所有相关的类.并且保存到IOC容器中
         */
        doInstance();
        /**
         * 4.完成自动化的依赖注入,DI
         */
        doAutowired();
        /**
         * 5.创建HandlerMapping将url和method建立对应关系
         */
        initHandlerMapping();
        System.out.println("======================SpringMVC is initializing=====================");
    }

    private void initHandlerMapping() {
        if (ioc.isEmpty()){ return; }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            //如果不是一个Controller直接跳过
            if (!clazz.isAnnotationPresent(MyController.class)){ continue; }
            String baseUrl = "";
            //判断是否拥有RequestMapping注解
            if (clazz.isAnnotationPresent(MyRequestMapping.class)){
                MyRequestMapping requestMapping = clazz.getAnnotation(MyRequestMapping.class);
                //获取Controller上的baseUrl
                baseUrl = requestMapping.value();
            }
            Method[] methods = clazz.getMethods();
            //遍历类的方法
            for (Method method : methods) {
                //如果没有MyRequestMapping直接跳过
                if (!method.isAnnotationPresent(MyRequestMapping.class)) { continue; }
                MyRequestMapping requestMapping = method.getAnnotation(MyRequestMapping.class);
                //获取完整的url,同时使用正则将多个 "/" 替换成只有一个
                String url = ("/" + baseUrl + "/" + requestMapping.value()).replaceAll("/+", "/");
                handlerMapping.put(url, method);
                System.out.println("Mapped: " + url + "," + method);
            }
        }
    }

    private void doAutowired() {
        if (ioc.isEmpty()){ return; }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            //获取当前类的所有的属性
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            //遍历属性
            for (Field field : fields) {
                //判断是否是需要进行依赖注入的属性
                if (!field.isAnnotationPresent(MyAutowired.class)){ return; }
                MyAutowired autowired = field.getAnnotation(MyAutowired.class);
                //获取自定的注入的beanName
                String beanName = autowired.value();
                if ("".equals(beanName)){
                    //获取到接口的全称
                    beanName = field.getType().getName();
                }
                //强制赋值
                field.setAccessible(true);
                try {
                    //从ioc获取实例进行注入
                    field.set(entry.getValue(), ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    continue;
                }

            }
        }
    }

    private void doInstance() {
        //如果list为空, 则没有扫描到任何东西,直接返回
        if (classNames.isEmpty()){ return; }
        try {
            //对扫描到的类进行反射
            for (String className : classNames) {
                //根据className获取到对应的类
                Class<?> clazz = Class.forName(className);
                //并不是所有的手写spring类都需要反射,只对加入自定义注解的进行反射
                if (clazz.isAnnotationPresent(MyController.class)){
                    //获取类的类名,而不包括包名,同时将获取到的类名首字母小写
                    String beanName = lowerFirstCase(clazz.getSimpleName());
                    ioc.put(beanName, clazz.newInstance());
                }else if (clazz.isAnnotationPresent(MyService.class)){
                    //1.类名首字母小写
                    //2.自定义命名优先
                    MyService service = clazz.getAnnotation(MyService.class);
                    String beanName = service.value();
                    //判断是否有自定义beanName
                    if ("".equals(beanName)){
                        //没有自定义的命名就使用类名首字母小写
                        beanName = lowerFirstCase(clazz.getSimpleName());
                    }
                    Object instance = clazz.newInstance();
                    ioc.put(beanName, instance);
                    //3.用接口的全称作为key, 用接口的实现类的实例作为值
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> i : interfaces) {
                        //如果存在一个接口的多个实例抛出异常
                        if (ioc.containsKey(i.getName())){
                            throw new Exception("The beanName is exists");
                        }
                        ioc.put(i.getName(), instance);
                    }
                }else {
                    continue;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 类名首字母小写
     * @param simpleName
     * @return
     */
    private String lowerFirstCase(String simpleName) {
        char [] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    private void doScanner(String scanPackage) {
        //将 com.clf.demo 格式转换成 /com/clf/demo
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replace(".", "/"));
        //拿到文件目录
        File classDir = new File(url.getFile());
        for (File file : classDir.listFiles()) {
            //目录下可能还有多级子包,需要进行递归判断
            if (file.isDirectory()){
                //如果是子文件夹,则进行继续扫描
                doScanner(scanPackage + "." + file.getName());
            }else {
                if (!file.getName().endsWith(".class")){
                    continue;
                }
                //如果拿到的是一个文件,则将对应的路径放到容器中
                String className = (scanPackage + "." + file.getName().replace(".class", "").trim());
                classNames.add(className);
            }
        }
    }

    private void doLoadConfig(String contextConfigLocation) {
        //从类路径下去取得properties
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            //加载所有配置信息
            contextConfig.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
