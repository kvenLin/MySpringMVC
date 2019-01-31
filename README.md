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
* Spring Bean是不是线程安全的?
> 答: bean的线程安全取决于bean的内部逻辑;
即在bean不定义成员变量的情况下是线程安全的,
若成员变量只存在读的操作没有写的操作也是线程安全的.
