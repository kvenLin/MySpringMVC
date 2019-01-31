# MySpringMVC
手写SpringMVC的实现深入理解IOC容器和DI
## 发送请求后SpringMVC运行的主要流程
* 前端根据url发送请求

* 请求后后端通过自定义的DispatcherServlet获取request

* 通过request获取当前访问的url

* handlerMapping实际上就是一个Map,分别存放url和对应的Method方法

* 通过获取method对象来获取对应的Class,再由Class名称获取原始的beanName,通过ioc容器由beanName获取bean对象进行注入
## 具体分析
![流程图解](https://github.com/kvenLin/MySpringMVC/blob/master/web2/image/2019-01-31%2010-12-52%E5%B1%8F%E5%B9%95%E6%88%AA%E5%9B%BE.png?raw=true)

[参考博客](https://blog.csdn.net/Box_clf/article/details/86717306)
## 面试常问问题
* Spring的生命周期是多久?
> 答: 因为ioc中的map对所有的bean进行了强引用,
所以Spring的生命周期随Spring容器的启动出现,
随Spring容器的消失而消失.
* Spring Bean的作用域和生命周期?
* Spring容器中的bean可以分为5个范围:
    * singleton：
    >这种bean范围是默认的，这种范围确保不管接受到多少个请求，
    每个容器中只有一个bean的实例，单例的模式由bean factory自身来维护。
    
    * prototype：
    >原形范围与单例范围相反，为每一个bean请求提供一个实例。
    
    * request：
    >在请求bean范围内会每一个来自客户端的网络请求创建一个实例，
    在请求完成以后，bean会失效并被垃圾回收器回收。
    
    * Session：
    >与请求范围类似，确保每个session中有一个bean的实例，
    在session过期后，bean会随之失效。
    
    * global-session：
    >global-session和Portlet应用相关。
    当你的应用部署在Portlet容器中工作时，它包含很多portlet。
    如果你想要声明让所有的portlet共用全局的存储变量的话，
    那么这全局变量需要存储在global-session中。
* Spring Bean是不是线程安全的?
> 答: bean的线程安全取决于bean的内部逻辑;
即在bean不定义成员变量的情况下是线程安全的,
若成员变量只存在读的操作没有写的操作也是线程安全的.
