# WebMvc

### 为什么封装？

**原生Servlet的浏览器---服务器请求响应过程**
	1.发送请求(资源)
	2.接收请求信息(资源名字 请求参数)
	3.解析请求(拆分资源名)
	4.参考web.xml配置文件( 请求---类名的关系) 
	5.找寻真正请求的资源(Servlet类)
	6.Servlet类做为控制类(接收请求 调用业务方法 根据业务结果给予响应)
	7.接收响应信息
	8.响应信息解析并展示

**原生Servlet的问题**：控制层的Servlet类个数好多，在控制层需要继承Servlet并重写service/doPost doGet方法，以及每个请求需要配置8行xml,很多冗余的问题
	

**封装需要解决的问题**：
	最主要的目的**降低配置文件的冗余**
	**减少Controller类的个数**
		一个功能点对应一个类(原生Servlet)
		一个功能点对应一个类中的某一个方法(封装之后)

### 项目描述

<<<<<<< HEAD

​			基于Servlet进行封装的web框架,可有效减少控制层的类的个数,可更高效进行开发。
=======
​			该项目在我学完JavaEE和学了SpringMVC框架后，为了加强自己的基础和能更深入解框架的本质，自己实现了框架的常用功能，该框架其目的是效减少控制层的类的个数及配置文件的冗余,可帮助程序员更高效进行开发。

### 项目核心
>>>>>>> origin/master

 核心逻辑在该类：WebMvc/webmvc/src/main/java/com/zsk/servlet/**DispatcherServlet**.java ，所有的请求都会   到达该类下的**service**方法，由该类进行分发请求。想深入理解的同学可以看该类下的源码哦

**项目结构**

![QQ截图20200915222058](C:\Users\Lenovo\Desktop\操作系统\图片\QQ截图20200915222058.png)
## 环境搭建

### 	1.引入依懒

````java
<!-- https://mvnrepository.com/artifact/javax.servlet/javax.servlet-api -->
    <dependency>
      <groupId>javax.servlet</groupId>
      <artifactId>javax.servlet-api</artifactId>
      <version>4.0.1</version>
      <scope>provided</scope>
    </dependency>
    <!-- https://mvnrepository.com/artifact/com.google.code.gson/gson -->
	用于返回json数据
    <dependency>
      <groupId>com.google.code.gson</groupId>
      <artifactId>gson</artifactId>
      <version>2.8.2</version>
    </dependency>
````

### 2.创建.properties文件（该文件用户配置需要扫描的包）

在classpath目录下创建Application.properties文件

**注：必须是Application.properties，否则会报文件找不到异常**



### 3.web.xml配置

DispatcherServlet--统一请求处理类

```java
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"
         version="4.0">
             
    <!--只拦截controller请求-->
        <servlet>
            <servlet-name>mvc</servlet-name>
            <servlet-class>Mymvc.servlet.DispatcherServlet</servlet-class>
        </servlet>
        <servlet-mapping>
            <servlet-name>mvc</servlet-name>
                
            <!--只拦截.do请求--> 这里可根据自己的请求类型配置需要拦截的请求
            <url-pattern>*.do</url-pattern>
        </servlet-mapping>
</web-app>
```

这样我们就完成了基本的环境搭建了



## 如何使用

有俩种使用方式：非注解编程和注解编程

### 非注解编程

#### 1.发起请求

```java
 <a href="TestController.do?method=test2&name=zzt&pass=666">测试</a><br>
```

对于 TestController.do来说，**TestController表示对于的类**，而.do不是必须的，可根据需求配置

对于method=test来说, test表示TestController类下的方法

name=admin&pass=admin 表示参数（可有可无）



#### 2.编写控制层Conntroller

```java
public class TestController {

    public  String test(@Param("name") String name, @Param("pass")String pass){
        System.out.println("接收到参数啦"+name+"---"+pass);
        return "success";
    }
}
```

##### **1.参数的类型：**

**1.1可包装成对象、Map、单个类型**

**1.2@Param注解 当我们需要接收参数时使用的注解 （推荐），当然我们也可以使用原生Servlet接收参数的方式1.3可在参数中注入 HttpServletResponse、HttpServletRequest**

##### ***2.返回值***

```java
//请求转发 
public String demo2(){

        return "index.html/redirect";
    }

//请求重定向 
public String demo2(){

        return "index.html/forward";
    }

//返回JSON数据 
public String demo2(){

        return "success";
    }
```





#### 3.编写Application.properties

```java
TestController=Mymvc.conntroller.TestController
    
注：TestController表示类名
    Mymvc.conntroller.TestController表示类全名
```

这样我们就可以访问该请求了

### 注解编程（推荐使用）

**@RequestMapping  用于请求和方法的映射 **

**@ResponseBody     用于返回JSON数据**

#### 1.发起请求

```java
 <a href="demo/des.do?name=admin&pass=admin">点击进入</a><br>
 <a href="demo/des.do?name=zzt&id=666">点击进入</a><br>
```

name=admin&pass=admin 表示参数（可有可无）

#### 2.编写控制层Conntroller

```java
@RequestMapping("/demo")
public class DemoController {
    @ResponseBody//返回JSON数据
    @RequestMapping("/des.do")//请求于方法的对应关系
    public String demo(){
        return "success";
    }
}
```

**注：在类上的注解@RequestMapping("/demo")不是必须的，但建议写上哦**

#### 3.编写Application.properties

```java
scanPackage=Mymvc.conntroller
    
    scanPackage 用于查找配置扫描的包(是必须的)
    Mymvc.conntroller  表示需要扫描的包(通常为控制层)

```

这样我们就可以访问该请求了
## Model

**该类用于保存数据（一次请求携带数据requset和一次会话session）**

**方法**

**setRequsetAttribute(key,value)**

**setsessionAttribute(key，value)**

````java
@RequestMapping("/demo")
public class DemoController {
    @ResponseBody
    @RequestMapping("/des.do")
    public String demo(){
        ModelAndView model=new ModelAndView();
        model.setRequsetAttribute("key","value");//一次请求
        model.setsessionAttribute("key","value");//一次会话
        return "success";
    }
}

````


