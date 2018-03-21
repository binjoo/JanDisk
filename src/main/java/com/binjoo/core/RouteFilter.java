package com.binjoo.core;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.binjoo.utils.AppConfig;
import com.binjoo.utils.CharsetUtils;

import freemarker.ext.servlet.HttpSessionHashModel;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

@SuppressWarnings("unchecked")
public class RouteFilter implements Filter {
    private ServletContext context;
    private Configuration cfg;

    private String CONFIG_FILE_PATH = AppConfig.getPro("filePath");

    private List<String> ignoreExts = new ArrayList<String>();

    @Override
    public void init(FilterConfig cfg) throws ServletException {
        ignoreExts.add(".css");
        ignoreExts.add(".js");
        ignoreExts.add(".eot");
        ignoreExts.add(".svg");
        ignoreExts.add(".ttf");
        ignoreExts.add(".woff");

//        try {
//            freemarker.log.Logger.selectLoggerLibrary(freemarker.log.Logger.LIBRARY_NONE);
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }

        this.context = cfg.getServletContext();
        this.cfg = new Configuration();
        this.cfg.setDefaultEncoding("UTF-8");
        this.cfg.setClassForTemplateLoading(this.getClass(), "/../../");
//        this.cfg.setObjectWrapper(new DefaultObjectWrapper());
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chi)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        HttpSession session = request.getSession(true);
        request.setCharacterEncoding(CharsetUtils.UTF_8);
        response.setCharacterEncoding(CharsetUtils.UTF_8);

        request.setAttribute("xxx", "qweqwe");
        
        try {
            Template temp = this.cfg.getTemplate("index.ftl");
            Writer out = response.getWriter();

            ParaMap data = new ParaMap();
            data.put("url", "xxx");
            data.put("request", request);
            data.put("response", response);
            data.put("session", new HttpSessionHashModel(session, cfg.getObjectWrapper()));
            data.put("ms", System.currentTimeMillis());
            temp.process(data, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

}
