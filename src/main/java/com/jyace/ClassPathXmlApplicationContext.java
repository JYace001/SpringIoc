package com.jyace;

import com.jyace.entity.UserEntity;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.lang.reflect.Field;
import java.util.List;

public class ClassPathXmlApplicationContext {

    private String xmlPath;
    public ClassPathXmlApplicationContext(String xmlPath){
        this.xmlPath = xmlPath;
    }

    public Object getBean(String beanId) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchFieldException {
        //解析xml器
        SAXReader saxReader = new SAXReader();
        Document read = null;

        try {
            //从项目根路径下读取
            read = saxReader.read(this.getClass().getClassLoader().getResourceAsStream(xmlPath));
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        if(read == null){
            return null;
        }

        //获取根节点资源
        Element root = read.getRootElement();//<beans>
        List<Element> elements = root.elements();//<bean>的集合
        if(elements.size() <= 0){
            return null;
        }
        Object ob = null;
        for (Element element : elements) {//循环<bean>
            String id = element.attributeValue("id");
            if(id == null){
                return null;
            }
            if(!id.equals(beanId)){
                continue;
            }
            //获取实体bean class地址
            String beanClass = element.attributeValue("class");
            //使用反射实例化bean
            Class<?> forNameClass = Class.forName(beanClass);
            ob = forNameClass.newInstance();
            //获取子类对象
            List<Element> attributes = element.elements();//<property>的集合
            if(attributes.size() <= 0){
                return null;
            }
            for(Element et : attributes){//<循环property>
                //使用反射技术为方法赋值
                String name = et.attributeValue("name");
                String value = et.attributeValue("value");
                Field field = forNameClass.getDeclaredField(name);
                field.setAccessible(true);
                field.set(ob , value);
            }
        }
        return ob;
        //1.使用beanId查找配置文件中的bean
        //2.获取对应bean中的classpath配置
        //3.使用java反射机制实体化对象
    }

    public static void main(String[] args) throws ClassNotFoundException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        ClassPathXmlApplicationContext classPathXmlApplicationContext =
                new ClassPathXmlApplicationContext("user.xml");
        UserEntity userEntity = (UserEntity) classPathXmlApplicationContext.getBean("user2");
        System.out.println(userEntity.toString());
    }

}
