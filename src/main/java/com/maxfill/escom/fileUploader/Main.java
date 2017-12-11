package com.maxfill.escom.fileUploader;

import com.google.gson.Gson;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.commons.lang3.StringUtils;

public class Main {
    private static final String DEFAULT_URL = "https://localhost:8443/escom-bpm-web";
    private static final String CONFIG_FILE = "escom-file-uploader.properties";
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    
    private static final String PROP_SERVER_URL = "SERVER_URL";
    private static final String PROP_TOKEN = "TOKEN";
    private static final String PROP_FOLDER_ID = "FOLDER_ID";
    
    private String uploadFile;
    private String serverURL;
    private String token;
    private String folderId;
    
    public static void main(String[] args) throws Exception{        
        if (args.length == 0) return;

        Main main = new Main();
        main.uploadFile = args[0];
        main.checkToken(main);        
        main.uploadFile();
    }
    
    private void checkToken(Main main) throws Exception{
        main.initLoadParams();
        if (main.getToken().isEmpty()){
            main.openLoginDialog();
            return;
        } 
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

        HttpPost httppost = new HttpPost(serverURL + "/checkToken");               
        StringBuilder sb = new StringBuilder();
        sb.append("{").append("token : '").append(token).append("' ").append("}");
        StringEntity postingString = new StringEntity(sb.toString());

        httppost.setHeader("Content-type", "application/json");
        httppost.setEntity(postingString);        

        try (CloseableHttpResponse response = httpclient.execute(httppost)) {
            System.out.println("Process check token status = " + response.getStatusLine());
            if (response.getStatusLine().getStatusCode() != 200){
                token = "";
                saveProperties();
            }
            if (response.getStatusLine().getStatusCode() == 401){                
                main.openLoginDialog();
            }
        }
    }
    
    /* загрузка параметров из файла конфигурации */
    private void initLoadParams(){
        try {
            File file = new File(CONFIG_FILE);
            if (file.exists()){
                Properties properties = new Properties();
                properties.load(new FileInputStream(file));
                serverURL = (String) properties.get(PROP_SERVER_URL);
                token = (String) properties.get(PROP_TOKEN);
                folderId = (String) properties.get(PROP_FOLDER_ID);
            } else {
                file.createNewFile();                
                serverURL = DEFAULT_URL;
                token = "";
                folderId = "";
                saveProperties();
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } 
    }
    
    public void uploadFile() throws Exception{
        if (StringUtils.isBlank(token) || StringUtils.isBlank(uploadFile)) return;        
        File upload = new File(uploadFile);
        if (!upload.exists()) return;
        
        проверить наличие папки!0
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

        HttpPost httppost = new HttpPost(serverURL + "/upload");       

        HttpEntity reqEntity = MultipartEntityBuilder.create()
                .addPart("token", new StringBody(token, ContentType.TEXT_PLAIN)) 
                .addPart("folder", new StringBody(folderId, ContentType.TEXT_PLAIN)) 
                .addPart("file", new FileBody(upload))
                .build();

        httppost.setEntity(reqEntity);        
        try (CloseableHttpResponse response = httpclient.execute(httppost)){
            System.out.println("Process upload file status = " + response.getStatusLine());
        }
    }   
    
    /**
     * Передаёт на сервер учётные данные пользователя. В случае успешной аутентификации получает с сервера token
     * @param url
     * @param login
     * @param password
     * @return true если пользователь авторизовался и false если логин некорректный
     * @throws Exception 
     */
    public boolean loginToServer(String url, String login, char[] password) throws Exception{
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

        serverURL = url;
        HttpPost httppost = new HttpPost(serverURL + "/login");               
        StringBuilder sb = new StringBuilder();
        sb.append("{")
                .append("login : '").append(login).append("', ")
                .append("pwl : '").append(String.valueOf(password)).append("'")
            .append("}");
        StringEntity postingString = new StringEntity(sb.toString());

        httppost.setHeader("Content-type", "application/json");
        httppost.setEntity(postingString);        

        try (CloseableHttpResponse response = httpclient.execute(httppost)) {
            if (response.getStatusLine().getStatusCode() != 200) return false;
            HttpEntity resEntity = response.getEntity();            
            if (resEntity == null) return false;
            String json_string = EntityUtils.toString(resEntity);
            Gson gson = new Gson();
            Map<String, String> tokenMap = gson.fromJson(json_string, Map.class);                
            token = tokenMap.get("token");
            EntityUtils.consume(resEntity);
            saveProperties();
            return true;            
        }
    }
    
    public void saveProperties(){
        OutputStream output = null;
        try {
            Properties properties = new Properties();
            output = new FileOutputStream(CONFIG_FILE);
            properties.setProperty(PROP_SERVER_URL, serverURL);
            properties.setProperty(PROP_TOKEN, token);
            properties.setProperty(PROP_FOLDER_ID, folderId);
            properties.store(output, null);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (output != null) output.close();
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    }    
    
    private void openLoginDialog(){
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                LoginDialog dialog = new LoginDialog(new javax.swing.JFrame(), true, Main.this);
                
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }
    
    public String getToken(){
        return token;
    }
    public String getServerURL() {
        return serverURL;
    } 
}