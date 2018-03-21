package com.binjoo.filter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.binjoo.core.ParaMap;
import com.binjoo.utils.AppConfig;
import com.binjoo.utils.CharsetUtils;

import freemarker.ext.servlet.HttpSessionHashModel;
import freemarker.template.Configuration;
import freemarker.template.Template;

@SuppressWarnings("unchecked")
public class RouteFilter implements Filter {
    private String contextPath;
    private Configuration cfg;

    private String CONFIG_FILE_PATH = AppConfig.getPro("filePath");

    private String BASEPATH = null;

    private ParaMap CACHE_FOLDER_LSIT = new ParaMap();

    private List<String> ignoreExts = new ArrayList<String>();

    @Override
    public void init(FilterConfig cfg) throws ServletException {
        this.contextPath = cfg.getServletContext().getContextPath();
        this.cfg = new Configuration();
        this.cfg.setDefaultEncoding(CharsetUtils.UTF_8);
        this.cfg.setClassForTemplateLoading(this.getClass(), "/../../");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chi)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        HttpSession session = request.getSession(true);
        request.setCharacterEncoding(CharsetUtils.UTF_8);
        response.setCharacterEncoding(CharsetUtils.UTF_8);
        ParaMap data = new ParaMap();
        Template template = this.cfg.getTemplate("index.ftl");
        Writer out = response.getWriter();

        try {
            String servletPath = request.getServletPath();

            if (servletPath.startsWith("/assets")) {
                chi.doFilter(request, response);
                return;
            }

            File pathFile = new File(this.CONFIG_FILE_PATH + servletPath);

            // 如果文件不存在，则通知浏览器404
            if (!pathFile.exists()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "404 Not Found");
                return;
            }

            if (pathFile.isDirectory()) {
                data.put("parent", this.getParentPath(servletPath, pathFile));
                data.put("fileList", this.getFolderList(pathFile));
            } else if (pathFile.isFile()) {
                this.downloadFile(pathFile, response);
                return;
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "500 Internal Server Error");
                return;
            }

            data.put("basePath", BASEPATH == null ? this.getBasePath(request) : BASEPATH);
            data.put("request", request);
            data.put("response", response);
            data.put("session", new HttpSessionHashModel(session, cfg.getObjectWrapper()));
            data.put("ms", System.currentTimeMillis());
            out = response.getWriter();
            template.process(data, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            out.flush();
            out.close();
        }
    }

    @Override
    public void destroy() {
    }

    private void downloadFile(File pathFile, HttpServletResponse response) throws Exception {
        InputStream fis = new BufferedInputStream(new FileInputStream(pathFile));
        byte[] buffer = new byte[fis.available()];
        fis.read(buffer);
        fis.close();
        response.reset();
        String filename = URLEncoder.encode(pathFile.getName(), CharsetUtils.UTF_8);
        response.addHeader("Content-Disposition", "attachment;filename=" + filename);
        response.addHeader("Content-Length", "" + pathFile.length());
        OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
        response.setContentType("application/octet-stream");
        toClient.write(buffer);
        toClient.flush();
        toClient.close();
    }

    private String getParentPath(String servletPath, File pathFile) throws Exception {
        if ("/".equals(servletPath)) {
            return null;
        }
        return this.cutPath(pathFile.getParentFile());
    }

    private String cutPath(File file) throws Exception {
        String path = file.getPath().substring(CONFIG_FILE_PATH.length());
        path = path.replace("\\", "/");
        path = URLEncoder.encode(path, "UTF-8");
        path = path.replace("%2F", "/");
        path = path.replace("+", "%20");
        return path;
    }

    /**
     * 是否为一个目录（文件夹）
     * 
     * @param uri
     * @throws Exception
     */
    private List<ParaMap> getFolderList(File pathFile) throws Exception {
        // 优先读取缓存信息
        if (CACHE_FOLDER_LSIT.containsKey(pathFile.getPath())) {
            return CACHE_FOLDER_LSIT.getList(pathFile.getPath());
        }

        List<ParaMap> fileList = new ArrayList<ParaMap>();
        List<File> files = Arrays.asList(pathFile.listFiles());

        Collections.sort(files, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                if (f1.isDirectory() && f2.isFile())
                    return -1;
                if (f1.isFile() && f2.isDirectory())
                    return 1;
                return f1.getName().compareTo(f2.getName());
            }
        });

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (File file : files) {
            ParaMap temp = new ParaMap();

            temp.put("is_folder", file.isDirectory());
            temp.put("path", this.cutPath(file));
            temp.put("name", file.getName());
            temp.put("size", this.getFileSize(file.length()));
            temp.put("last_modified", sdf.format(new Date(file.lastModified())));
            fileList.add(temp);
        }
        CACHE_FOLDER_LSIT.put("pathFile.getPath()", fileList);
        return fileList;
    }

    /**
     * 计算文件大小
     * 
     * @param length
     * @return
     */
    private String getFileSize(long length) {
        long SIZE_KB = 1024;
        long SIZE_MB = SIZE_KB * 1024;
        long SIZE_GB = SIZE_MB * 1024;

        if (length >= SIZE_GB) {
            return String.format("%.2f GB", (float) length / SIZE_GB);
        } else if (length >= SIZE_MB) {
            float f = (float) length / SIZE_MB;
            // return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
            return String.format("%.2f MB", f);
        } else if (length >= SIZE_KB) {
            float f = (float) length / SIZE_KB;
            // return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
            return String.format("%.2f KB", f);
        } else
            return String.format("%d B", length);
    }

    /**
     * 获得请求根地址
     * 
     * @param request
     * @return
     */
    private String getBasePath(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int port = request.getServerPort();
        String path = request.getContextPath();
        String basePath = scheme + "://" + serverName + ":" + port + path;
        return basePath;
    }
}
