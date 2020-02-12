package com.zzq.tomcat.utils;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import javax.servlet.http.HttpServlet;
import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectUtil {


    public Map<String,WebXml> projectInfos = new HashMap<>();

    final String JAVAEE_SUFFIX = "/WEB-INF/web.xml";

    public static ProjectUtil projectUtil;

    private ProjectUtil() {
    }


    public static ProjectUtil getInstance() {
        if (null == projectUtil) {
            synchronized (ProjectUtil.class) {
                if (null == projectUtil) {
                    projectUtil = new ProjectUtil();
                }
            }
        }
        return projectUtil;
    }

    final String webApps = "/home/zzq/work/self/simple-servlet-web/target/";

    public void load() {
        File[] projects = new File(webApps).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });

        //解压war包 省略......

        for (File project : projects) {
            File webXmlFile = new File(project.getPath() + JAVAEE_SUFFIX);
            if (webXmlFile.exists()){
                //解析xml
                System.out.println(project.getName()+" 项目加载到服务中");
                WebXml webXml = new WebXml();
                webXml.setProjectPath(project.getPath());
                doLoadMethodDefinitions(webXml);
                webXml.loadServlet();
                projectInfos.put(project.getName(),webXml);
            }
        }


    }

    /**
     * zhouzhongqing
     * 2019年5月21日09:44:31
     * 载入对方法的配置
     **/
    private void doLoadMethodDefinitions(WebXml webXml) {
        try {
            // 创建saxReader对象
            SAXReader reader = new SAXReader();
            File file = new File(webXml.projectPath + JAVAEE_SUFFIX);
            // 通过read方法读取一个文件 转换成Document对象
            Document document = reader.read(file);
            // 获取根节点元素对象
            Element node = document.getRootElement();
            elementMethod(node,webXml);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Element中的element方法和elements方法的使用
     *
     * @param node
     */
    private void elementMethod(Element node,WebXml webXml) {
        // 获取node节点中，子节点的元素名称为supercars的元素节点。
         List<Element> servletMapping = node.elements("servlet-mapping");
        for (Element servletMapp : servletMapping) {
            String servletName = (String) servletMapp.element("servlet-name").getData();
            String urlPattern = (String) servletMapp.element("url-pattern").getData();
            webXml.getServletMapping().put(urlPattern,servletName);
        }

        for (Element servlet : (List<Element>)node.elements("servlet")) {
            String servletName = (String) servlet.element("servlet-name").getData();
            String servletClass = (String) servlet.element("servlet-class").getData();
            webXml.getServlets().put(servletName,servletClass);
        }

        /* List<Element> es = node.elements("method");
        for (Element e : es) {
            String methodName = e.attributeValue("methodName");
            String attributeMethod = e.attributeValue("attributeMethod");
            String className = e.attributeValue("className");

        }*/

    }


    public static void main(String[] args) {
        ProjectUtil.getInstance().load();
    }

    public class WebXml{
        private String projectPath;


        /**有哪些servlet **/
        private final Map<String,String> servlets = new HashMap<>();


        /**servlet- url映射**/
        private final Map<String,String> servletMapping = new HashMap<>();

        Map<String,HttpServlet> servletInstances = new HashMap<>();

        public String getProjectPath() {
            return projectPath;
        }

        public void setProjectPath(String projectPath) {
            this.projectPath = projectPath;
        }

        public Map<String, String> getServlets() {
            return servlets;
        }

        public Map<String, HttpServlet> getServletInstances() {
            return servletInstances;
        }

        public void setServletInstances(Map<String, HttpServlet> servletInstances) {
            this.servletInstances = servletInstances;
        }

        public Map<String, String> getServletMapping() {
            return servletMapping;
        }

        public void loadServlet(){
            try {
                URL servletUrl = new URL("file:"+projectPath+"/WEB-INF/classes/");
                ClassLoader classLoader = new URLClassLoader(new URL[]{servletUrl});
                //创建对象
                servlets.forEach((k,v)->{
                    try {
                        Class<?> clz = classLoader.loadClass(v.toString());
                        HttpServlet httpServlet = (HttpServlet) clz.newInstance();
                        servletInstances.put(k,httpServlet);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    }
                });

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

    }
}
