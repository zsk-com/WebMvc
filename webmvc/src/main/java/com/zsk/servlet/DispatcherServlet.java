package com.zsk.servlet;

import com.zsk.annotation.RequestMapping;
import com.zsk.annotation.ResponseBody;
import com.zsk.annotation.Param;
import com.zsk.util.Mession;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.*;

/**
 * 根据web.xml配置请求统一处理类
 */
public class DispatcherServlet extends HttpServlet {
    //读取配置文件，存在缓存中
    private HashMap<String, String> inputMap = new HashMap<>();

    //将对象的控制权反转（IOC)
    private HashMap<String, Object> iocMap = new HashMap<>();

    //存类和方法对对应关系----目的自动注入 类对象-----类下所有的方法
    // Map<String, Method> 请求与方法的关系
    private Map<Object, Map<String, Method>> dimap = new HashMap<>();

    //用于存储请求名与类名之间的关系
    private Map<String, String> methodWithRealNameMap = new HashMap();

    //小弟1号,读取配置文件，获取包名
    private void load() throws IOException {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("Application.properties");
        Properties properties = new Properties();
        properties.load(is);
        //遍历集合
        Enumeration keys = properties.propertyNames();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String value = properties.getProperty(key);
            inputMap.put(key, value);
        }
    }

    //小弟2号，解析请求的uri
    private String analyissURI(String uri) {
        //  /wedMVC/login 截取请求名 substring()方法截取[  )左闭右开
        uri = uri.substring(uri.lastIndexOf("/") + 1);//类名;
        return uri;
    }

    //小弟3号 获取对象 并获取该类下的所有方法存入dimap
    private Object getObject(String classForName) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class clazz = Class.forName(classForName);
        //控制该对象为单例的
        Object obj = this.iocMap.get(classForName);
        if (obj == null) {
            obj = clazz.newInstance();
            this.iocMap.put(classForName, obj);
            //获取所有方法
            Method[] mds = clazz.getDeclaredMethods();
            //methodMap 请求与方法的关系
            Map<String, Method> methodMap = new HashMap<>();
            for (Method md : mds) {
                RequestMapping annotation = md.getAnnotation(RequestMapping.class);
                if (annotation != null) {
                    String value=annotation.value();
                    value=value.substring(value.indexOf("/")+1);
                    methodMap.put(value, md);
                }
                String name = md.getName();
                methodMap.put(name, md);
            }
            dimap.put(obj, methodMap);
        }
        return obj;
    }

    //小弟4号，获取方法
    private Method getMethod(String method, Object obj) throws NoSuchMethodException {
        Map<String, Method> methodMap = dimap.get(obj);
        Method md = methodMap.get(method);
        return md;
    }

    //自动注入
    private Object[] setDI(Method method, HttpServletRequest request, HttpServletResponse response) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        //分析这个method
        //获取方法上所有参数对象
        Parameter[] parame = method.getParameters();
        //存储参数名称
        Object[] parObj = new Object[parame.length];
        for (int i = 0; i < parame.length; i++) {
            //获取参数注解
            Param parmName = parame[i].getAnnotation(Param.class);
            //如有有注解，证明为基本类型或String
            if (parmName != null) {
                //获取请求携带的参数
                String parVaue = request.getParameter(parmName.value());
                if (parVaue != null) {//参数不为空才注入
                    //获取参数的类型
                    Class clazzType = parame[i].getType();
                    if (clazzType == String.class) {
                        parObj[i] = new String(parVaue);
                    } else if (clazzType == Integer.class || clazzType == int.class) {
                        parObj[i] = new Integer(parVaue);
                    } else if (clazzType == Long.class || clazzType == long.class) {
                        parObj[i] = new Long(parVaue);
                    } else if (clazzType == Short.class || clazzType == short.class) {
                        parObj[i] = new Short(parVaue);
                    } else if (clazzType == Byte.class || clazzType == byte.class) {
                        parObj[i] = new Byte(parVaue);
                    } else if (clazzType == Float.class || clazzType == float.class) {
                        parObj[i] = new Float(parVaue);
                    } else if (clazzType == Double.class || clazzType == double.class) {
                        parObj[i] = new Double(parVaue);
                    } else if (clazzType == Boolean.class || clazzType == boolean.class) {
                        parObj[i] = new Boolean(parVaue);
                    }
                }
            } else {
                //对象或者是map
                Class classType = parame[i].getType();
                if (classType.isArray()) {
                    //为数组
                }
                if (classType == HttpServletRequest.class) {
                    parObj[i] = request;
                    continue;
                }
                if (classType == HttpServletResponse.class) {
                    parObj[i] = response;
                    continue;
                }
                Object object = classType.newInstance();
                if (object instanceof Map) {
                    //将对象造型成map
                    Map<String, String> map = (Map<String, String>) object;
                    //获取所有请求的名字
                    Enumeration en = request.getParameterNames();
                    while (en.hasMoreElements()) {
                        String key = (String) en.nextElement();
                        String value = request.getParameter(key);
                        map.put(key, value);
                    }
                    parObj[i] = map;
                } else if (object instanceof Object) {
                    //是一个对象
                    //获取所有的属性
                    Field[] files = classType.getDeclaredFields();
                    for (Field field : files) {

                        //2.set方法赋值法
                        String fieldName = field.getName();
                        //拼接set方法
                        String one = fieldName.substring(0, 1).toUpperCase();
                        String two = fieldName.substring(1);
                        StringBuilder builderMethod = new StringBuilder("set");
                        builderMethod.append(one);
                        builderMethod.append(two);
                        //获取属性的类型
                        Class clazzT = field.getType();
                        //查找set方法
                        Method md = classType.getDeclaredMethod(builderMethod.toString(), clazzT);
                        //获取请求的参数
                        String value = request.getParameter(fieldName);
                        //判断该参数的类型 并执行
                        if (clazzT == String.class) {
                            md.invoke(object, value);
                        } else if (clazzT == Integer.class || classType == int.class) {
                            md.invoke(object, Integer.parseInt(value));
                        } else if (clazzT == Float.class || classType == float.class) {
                            md.invoke(object, Float.parseFloat(value));
                        } else if (clazzT == Long.class || classType == Long.class) {
                            md.invoke(object, Long.parseLong(value));
                        } else if (clazzT == Short.class || classType == short.class) {
                            md.invoke(object, Short.parseShort(value));
                        } else if (clazzT == byte.class || classType == byte.class) {
                            md.invoke(object, Byte.parseByte(value));
                        } else if (clazzT == Double.class || classType == double.class) {
                            md.invoke(object, Double.parseDouble(value));
                        } else {
                            //抛异常
                        }
                    }
                    parObj[i] = object;
                }
            }
        }

        //通过request.getParameter()获取请求的参数 存起来

        //所有参数处理完毕 返回   Object[]装载的是当前这个method方法所有参数的值
        return parObj;
    }

    //转发 重定向
    private void setReqResp(HttpServletRequest requset) {
        //创建对象
        Model view = new Model();
        //处理requset存值
        HashMap<String, String> requsetmap = view.getRequsetAll();
        Iterator<String> it1 = requsetmap.keySet().iterator();
        while (it1.hasNext()) {
            String key = it1.next();
            String value = requsetmap.get(key);
            requset.setAttribute(key, value);
        }
        //处理session
        HttpSession session = requset.getSession();
        HashMap<String, String> responsemap = view.getSessionAll();
        Iterator<String> it2 = responsemap.keySet().iterator();
        while (it2.hasNext()) {
            String key = it2.next();
            String value = responsemap.get(key);
            session.setAttribute(key, value);
        }
    }
    //该方法对象创建时就加载
    public void init() {
        try {
            this.load();
            this.ScanAnnction();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //扫描注解RequsetMapping
    public void ScanAnnction() {
        //获取需要扫描的包
        String scanPackage = inputMap.get("scanPackage");
        if (scanPackage != null) {//证明需要分析
            //分析包有多少个要扫描
            String[] split = scanPackage.split(",");
            for (String scanNaame : split) {
                //循环一次获取一个包路径,将包名的点换成/，获取全路径
                URL url = Thread.currentThread().getContextClassLoader().getResource(scanNaame.replace(".", "/"));
                if (url == null) {
                    //证明配置错误,找下一个包
                    continue;
                }
                //获取全路径
                String path = url.getPath();
                //根据包路径创建一个与包对应的File对象
                File file = new File(path);
                //获取所有的子对象
                File[] files = file.listFiles(new FileFilter() {
                    //过滤.class文件
                    @Override
                    public boolean accept(File pathname) {
                        if (pathname.isFile() && pathname.getName().endsWith(".class")) {
                            return true;
                        }
                        return false;
                    }
                });
                for (File f : files) {
                    //没一次获取一个类名.class文件
                    String fileName = f.getName();
                    //获取类的名字
                    String name = fileName.substring(0, fileName.indexOf("."));
                    //获取类全名
                    String forname = scanNaame + "." + name;
                    System.out.println("路径" + forname);

                    //反射获取类
                    try {
                        Class clazz = Class.forName(forname);
                        RequestMapping annotation = (RequestMapping) clazz.getAnnotation(RequestMapping.class);
                        if (annotation != null) {//证明类上有注解
                            inputMap.put(annotation.value(), forname);
                        }
                        //获取类下的所有方法
                        Method[] methods = clazz.getMethods();
                        for (Method method : methods) {
                            //获取方法上的注解
                            RequestMapping methodAnnotation = method.getAnnotation(RequestMapping.class);
                            if (methodAnnotation != null) {
                                //获取注解里的请求名
                                String value=methodAnnotation.value();
                                value=value.substring(value.indexOf("/")+1);
                                methodWithRealNameMap.put(value, forname);
                            }
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }


            }
        }

    }

    public void disposeRespones(Method method, Object type, HttpServletResponse response, HttpServletRequest request) throws Exception {
        if (type != null) {
            if (type instanceof String) {
                //8.反回值中拆分看是以什么方式响应 welco.jsp/redirect 重定向  /forw转发     不带/ json格式
                //pathh 为路径
                String path = (String) type;
                if (!"".equals(path) && !"null".equals(path) && path!=null) {
                    if (path.lastIndexOf("/")==-1){//json
                        Mession mess = new Mession(type);
                        response.getWriter().append(mess.toJSON());
                        return;
                    }
                    //转发或重定向
                    String pathh = path.substring(0, path.lastIndexOf("/"));
                    //rueslt 为响应方式
                    String ruselt = path.substring(path.lastIndexOf("/") + 1);
                    //进行转发处理
                    if ("redirect".equals(ruselt)) {
                        response.sendRedirect(pathh);
                    } else if ("forward".equals(ruselt)) {
                        request.getRequestDispatcher(pathh).forward(request, response);
                    } else {
                        throw new Exception("返回的信息有误，无法处理啦");
                    }
                }
            } else {
                //json形式
                ResponseBody annotation = method.getAnnotation(ResponseBody.class);
                if (annotation != null) {
                    Mession mess = new Mession(type);
                    response.getWriter().append(mess.toJSON());
                } else {
                    throw new Exception("请配置注解RequsetBody");
                }
            }
        }


    }

    /**
     * 核心处理类，分发请求
     * @param requset
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void service(HttpServletRequest requset, HttpServletResponse response) throws ServletException, IOException {
        try {
            //1.获取请求名
            String uri = requset.getRequestURI();
            //login.do
            uri = this.analyissURI(uri);
            //2.读取配置文件，获取类全名
            //截取类名
//            String forName=uri;
//            if (uri.indexOf(".")!=-1){//证明非.后最的请求
                 String forName = uri.substring(0, uri.indexOf("."));
//            }
            //通过类名获取类全名
            String classForName = this.inputMap.get(forName);
            if (classForName == null) {//证明类上没有配置
                classForName = methodWithRealNameMap.get(uri);

            }
            //3.创建反射对象,并获取类的所有方法
            Object obj = this.getObject(classForName);
            //4.获取请求携带的参数(方法名)
            String md = requset.getParameter("method");
            if (md == null) {//证明不是类名.do方式请求
                md = uri;
            }
            //5.获取方法
            Method method = this.getMethod(md, obj);
            //6.处理请求的参数，将结果返回,提供方法---自动注入
            Object[] objects = this.setDI(method, requset, response);
            //7.执行方法 ,并传递requset对象和response对象
            Object path = method.invoke(obj, objects);
            //.处理存值 requset.attr session.attr
            this.setReqResp(requset);
            //8.处理响应信息
            this.disposeRespones(method, path, response, requset);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
