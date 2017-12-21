package com.maxfill.escom.fileUploader;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maxfill.escom.fileUploader.folders.Folder;
import org.apache.commons.lang3.StringUtils;
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
import org.apache.commons.cli.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final String DEFAULT_URL = "https://localhost:8443/escom-bpm-web";
    private static final String CONFIG_FILE = "escom-file-uploader.properties";
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    
    private static final String PROP_SERVER_URL = "SERVER_URL";
    private static final String PROP_TOKEN = "TOKEN";
    private static final String PROP_FOLDER_ID = "FOLDER_ID";
    private static final String PROP_FOLDER_NAME = "FOLDER_NAME";

    private static final String LOGIN_REQUIRED_MSG = "Login is required on the server!";
    private static final String ARG_UPLOAD = "File download to the server and creating a document.";
    private static final String ARG_FOLDER = "Open dialog window for select the folder on the server into which you will load files and create documents.";
    private static final String ARG_HELP = "Show this help.";

    private static final Options options = new Options();

    private String uploadFile;
    private String serverURL;
    private String token;
    private String errMsg;
    private String folderId;
    private String folderName;

    public static void main(String[] args) throws Exception{        
        if (args.length == 0) return;

        Option optUpload = new Option("u", "upload", true, ARG_UPLOAD);
        optUpload.setRequired(false);
        optUpload.setArgName("file");
        options.addOption(optUpload);

        Option optFolder = new Option("f", "folder", false, ARG_FOLDER);
        optFolder.setRequired(false);
        options.addOption(optFolder);
        options.addOption("h", "help", false, ARG_HELP);

        CommandLineParser parser = new BasicParser();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }

        if (cmd.hasOption("h") || (!cmd.hasOption("upload") && !cmd.hasOption("folder")) ){
            showHelp();
            return;
        }

        Main main = new Main();

        if (cmd.hasOption("upload")) {
            main.uploadFile = cmd.getOptionValue("upload");
            main.start();
        } else
            if (cmd.hasOption("folder")){
                main.getFolder(new WindowAdapter(){
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                });
            }
    }

    private void start(){
        WindowListener returnToStart = new WindowAdapter(){
            public void windowClosing(WindowEvent e) {
                start();
            }
        };
        try {
            if (checkToken()) {
                if (checkFolder(returnToStart)){
                    uploadFile();
                }
            } else {
                openLoginDialog(returnToStart);
            }
        } catch (Exception ex){
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Проверка папки
     */
    private boolean checkFolder(WindowListener windowListener) throws Exception{
        if (StringUtils.isBlank(folderId)){
            getFolder(windowListener);
            return false;
        }
        return true;
    }

    /**
     * Проверка ключа
     */
    private boolean checkToken() {
        initLoadParams();
        if (StringUtils.isBlank(getToken())) {
            errMsg = LOGIN_REQUIRED_MSG;
            return false;
        }

        try {
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
                //System.out.println("Process check token status = " + response.getStatusLine());
                if(response.getStatusLine().getStatusCode() == 200) return true;
                token = "";
                saveProperties();
                errMsg = LOGIN_REQUIRED_MSG;
            }
        } catch (Exception ex){
            LOGGER.log(Level.SEVERE, null, ex);
            errMsg = ex.getMessage();
        }
        return false;
    }

    /**
     * Получение папки
     */
    private void getFolder(WindowListener windowListener) {
        if (checkToken()) {
            openFolderSelecterDialog(windowListener);
        } else {
            WindowListener returnToGetFolder = new WindowAdapter(){
                public void windowClosing(WindowEvent e) {
                    getFolder(windowListener);
                }
            };
            openLoginDialog(returnToGetFolder);
        }
    }

    /**
     * Передача файла на сервер
     */
    public void uploadFile() throws Exception{
        if (StringUtils.isBlank(token) || StringUtils.isBlank(uploadFile)) return;        
        File upload = new File(uploadFile);
        if (!upload.exists()) return;
        
        //ToDo проверить наличие папки!
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
            System.out.println("INFO: Process upload file status = " + response.getStatusLine());
        }
    }   
    
    /**
     * Передаёт на сервер учётные данные пользователя. В случае успешной аутентификации получает с сервера token
     * @return true если пользователь авторизовался и false если логин некорректный
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

    /**
     * Передаёт на сервер запрос на получение вложенных папок
     */
    public List<Folder> loadFolders(Folder folder) throws Exception{
        String parent = String.valueOf(folder.getId());
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

        HttpPost httppost = new HttpPost(serverURL + "/folders");
        StringBuilder sb = new StringBuilder();
        sb.append("{")
                .append("token : '").append(token).append("', ")
                .append("parent : '").append(parent).append("'")
                .append("}");
        StringEntity postingString = new StringEntity(sb.toString());

        httppost.setHeader("Content-type", "application/json");
        httppost.setEntity(postingString);

        try (CloseableHttpResponse response = httpclient.execute(httppost)) {
            if (response.getStatusLine().getStatusCode() != 200) return null;
            HttpEntity resEntity = response.getEntity();
            if (resEntity == null) return null;
            String json_string = EntityUtils.toString(resEntity);

            Gson gson = new Gson();
            //GsonBuilder jBuilder = new GsonBuilder();
            //jBuilder.registerTypeAdapter(Folder.class, new FolderConverter());
            //Gson gson = jBuilder.create();
            Type fooType = new TypeToken<List<Folder>>() {}.getType();
            List<Folder> folders = gson.fromJson(json_string, fooType);
            folders.stream().forEach(f-> f.setParent(folder));
            EntityUtils.consume(resEntity);
            return folders;
        }
    }

    /**
     * Загрузка параметров из файла конфигурации
     */
    private void initLoadParams(){
        serverURL = DEFAULT_URL;
        token = "";
        folderId = "";
        folderName = "";
        try {
            File file = new File(CONFIG_FILE);
            if (file.exists()){
                Properties properties = new Properties();
                properties.load(new FileInputStream(file));
                serverURL = (String) properties.get(PROP_SERVER_URL);
                token = (String) properties.get(PROP_TOKEN);
                folderId = (String) properties.get(PROP_FOLDER_ID);
                folderName = (String) properties.getProperty(PROP_FOLDER_NAME);
            } else {
                file.createNewFile();
                saveProperties();
            }
        } catch (IOException | NullPointerException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Сохранение параметров в файл конфигурации
     */
    public void saveProperties(){
        OutputStream output = null;
        try {
            Properties properties = new Properties();
            output = new FileOutputStream(CONFIG_FILE);
            properties.setProperty(PROP_SERVER_URL, serverURL);
            properties.setProperty(PROP_TOKEN, token);
            if (StringUtils.isNotBlank(folderId)) {
                properties.setProperty(PROP_FOLDER_ID, folderId);
            }
            if (StringUtils.isNotBlank(folderName)) {
                properties.setProperty(PROP_FOLDER_NAME, folderName);
            }
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

    /**
     * Отображение диалога выбора папки
     */
    private void openFolderSelecterDialog(WindowListener windowListener){
        java.awt.EventQueue.invokeLater(
                () -> {
                    FolderSelecter dialog = new FolderSelecter(Main.this);
                    dialog.setResizable(true);
                    dialog.setPreferredSize(new Dimension(550, 350));
                    dialog.pack();
                    dialog.setLocationRelativeTo(null);
                    dialog.addWindowListener(windowListener);
                    dialog.setVisible(true);
                });
    }

    /**
     * Отображение диалога логина
     */
    private void openLoginDialog(WindowListener windowListener){
        //java.awt.EventQueue.invokeLater(
        //        () -> {
                    LoginUser dialog = new LoginUser(Main.this);
                    dialog.setResizable(true);
                    dialog.setPreferredSize(new Dimension(450, 250));
                    dialog.pack();
                    dialog.setLocationRelativeTo(null);
                    dialog.addWindowListener(windowListener);
                    dialog.setVisible(true);
          //      });
    }

    /**
     * Вывод справки по использованию программы
     */
    private static void showHelp() {
        printHelp(
                options, // опции по которым составляем help
                80, // ширина строки вывода
                "Options", // строка предшествующая выводу
                "----------------------", // строка следующая за выводом
                3, // число пробелов перед выводом опции
                5, // число пробелов перед выводом опцисания опции
                true, // выводить ли в строке usage список команд
                System.out // куда производить вывод
        );
    }

    public static void printHelp(
            final Options options,
            final int printedRowWidth,
            final String header,
            final String footer,
            final int spacesBeforeOption,
            final int spacesBeforeOptionDescription,
            final boolean displayUsage,
            final OutputStream out) {
        final String commandLineSyntax = "java escom-upload.jar";
        final PrintWriter writer = new PrintWriter(out);
        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(
                writer,
                printedRowWidth,
                commandLineSyntax,
                header,
                options,
                spacesBeforeOption,
                spacesBeforeOptionDescription,
                footer,
                displayUsage);
        writer.flush();
    }

    public String getToken(){
        return token;
    }
    public String getServerURL() {
        return serverURL;
    }
    public String getErrMsg() {
        return errMsg;
    }

    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

    public String getFolderName() {
        return folderName;
    }
    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }
}