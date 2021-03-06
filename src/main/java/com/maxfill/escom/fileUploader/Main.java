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
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Stream;
import org.apache.commons.lang3.time.DateFormatUtils;

public class Main {
    private static final String DEFAULT_URL = "https://localhost:8443/escom-bpm-web";
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    
    private static final String PROP_SERVER_URL = "SERVER_URL";
    private static final String PROP_TOKEN = "TOKEN";
    private static final String PROP_FOLDER_ID = "FOLDER_ID";
    private static final String PROP_FOLDER_NAME = "FOLDER_NAME";

    private static final String LOGIN_REQUIRED_MSG = "Login is required on the server!";
    private static final String ARG_UPLOAD = "File(s) download to the server and creating a document(s).";
    private static final String ARG_FOLDER = "Open dialog window for select the folder on the server into which you will load files and create documents.";
    private static final String ARG_HELP = "Show this help.";
    private static final String ARG_DELETE = "Delete the file(s) after uploading it to the server. No delete folder(s)!";
    private static final String ARG_RECURSIVE = "Download files from subfolders.";
    private static final String ARG_CONFIG = "path to file for save configuration.";

    private static final Options options = new Options();

    private String uploadPath;
    private String serverURL;
    private String token;
    private String errMsg;
    private String folderId;
    private String folderName;
    private Boolean isDeleteFile = false;
    private Boolean isRecursive = false;
    private Boolean isNeedSelectFolder = false;
    private String pathConfFile;

    public static void main(String[] args) throws Exception{
        if (args.length == 0) return;

        Option optUpload = new Option("u", "upload", true, ARG_UPLOAD);
        optUpload.setRequired(false);
        optUpload.setOptionalArg(false);
        optUpload.setArgName("path");
        options.addOption(optUpload);

        Option optFolder = new Option("f", "folder", false, ARG_FOLDER);
        optFolder.setRequired(false);
        options.addOption(optFolder);
        options.addOption("h", "help", false, ARG_HELP);
        options.addOption("d", "delete", false, ARG_DELETE);
        options.addOption("r", "recursive", false, ARG_RECURSIVE);

        Option optConfig = new Option("c", "conf", true, ARG_CONFIG);
        optFolder.setRequired(true);
        options.addOption(optConfig);

        CommandLineParser parser = new BasicParser();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }

        if (cmd.hasOption("h") || !cmd.hasOption("conf") || (!cmd.hasOption("upload") && !cmd.hasOption("folder")) ){
            showHelp();
            return;
        }

        Main main = new Main();

        if (cmd.hasOption("upload")) {
            if (cmd.hasOption("folder")) {
                main.isNeedSelectFolder = true;
            }
            if (cmd.hasOption("delete")) {
                main.isDeleteFile = true;
            }
            if (cmd.hasOption("recursive")) {
                main.isRecursive = true;
            }
            main.pathConfFile = cmd.getOptionValue("conf");
            main.uploadPath = cmd.getOptionValue("upload");
            main.startUpload();
        } else
            if (cmd.hasOption("folder")){
                main.getFolder(() -> System.exit(0));
            } else {
                showHelp();
            }
    }

    private void startUpload(){
        if (uploadPath == null) {
            throw new RuntimeException("ERROR: command line argument path download is blank!");
        }

        try {
            if (checkToken()) {
                checkTargetFolder(()->uploadFiles());
            } else {
                openLoginDialog(()->startUpload());
            }
        } catch (Exception ex){
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Загрузка файлов
     */
    private void uploadFiles() {
        try {
            File source = new File(uploadPath);
            if (!source.exists()){
                throw new RuntimeException("ERROR: path download is not exists!");
            }
            StringBuilder logName = new StringBuilder("upload_");
            logName.append(DateFormatUtils.format(new Date(), "yyyy-MM-dd_HH-mm")).append(".log");
            Path logPath = FileSystems.getDefault().getPath(logName.toString());
            FileHandler fh = new FileHandler(logPath.toString());
            Formatter formatter = getFormater();
            fh.setFormatter(formatter);
            LOGGER.addHandler(fh);
            AtomicInteger error = new AtomicInteger(0);
            AtomicInteger counter = new AtomicInteger(0);
            int index = 0;
            if (source.isFile()){
                uploadFile(source, index, error, fh);
            } else
                if (source.isDirectory()) {
                    try {
                        Stream <Path> files = null;
                        if (isRecursive) {
                            files = Files.walk(source.toPath());
                        } else {
                            files = Files.list(source.toPath());
                        }
                        if (files != null) {                            
                            files.filter(Files::isRegularFile).forEach(path -> {
                                uploadFile(path.toFile(), counter.getAndIncrement(), error, fh);
                            });
                        }
                    }catch (IOException ex){
                        LOGGER.log(Level.SEVERE, null, ex);
                        throw new RuntimeException(ex);
                    }
                }
            if (error.get() == 0){
                System.out.println("*************************************************************");
                System.out.println("INFO: Successfully! The [" + counter.get() + "] file(s) were downloaded without errors." );
                System.out.println("*************************************************************");
            } else {
                System.out.println("*************************************************************");
                System.out.println("INFO: File(s) download complete. Number of errors [" + error.get()+ "]. See log file for detailed information!");
                System.out.println("*************************************************************");
            }
        } catch (IOException | SecurityException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private Formatter getFormater(){
        return new Formatter() {
            @Override
            public String format(LogRecord record) {
                SimpleDateFormat logTime = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                Calendar cal = new GregorianCalendar();
                cal.setTimeInMillis(record.getMillis());
                return record.getLevel() + " "
                        + logTime.format(cal.getTime())
                        + " || "                        
                        + record.getSourceMethodName()
                        + "() : "
                        + MessageFormat.format(record.getMessage(), record.getParameters())+ "\n";                
            }
        };
    }
    
    /**
     * Проверка папки на сервере, в которую будут грузиться файлы
     */
    private boolean checkTargetFolder(Callback callback) throws Exception{
        if (StringUtils.isBlank(folderId) || isNeedSelectFolder){
            getFolder(callback);
            return false;
        }
        return true;
    }

    /**
     * Проверка ключа
     */
    private boolean checkToken() {
        initLoadParams(new File( pathConfFile));
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
    private void getFolder(Callback callback) {
        if (checkToken()) {
            openFolderSelecterDialog(callback);
        } else {
            openLoginDialog(()->getFolder(callback));
        }
    }

    /**
     * Передача файла на сервер
     * @param uploadFile
     * @param index
     * @param error
     * @param fh
     */
    public void uploadFile(File uploadFile, int index, AtomicInteger error, FileHandler fh) {
        if (index > 500) return;
        SSLContextBuilder builder = new SSLContextBuilder();
        try {
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
            CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

            HttpPost httppost = new HttpPost(serverURL + "/upload");

            HttpEntity reqEntity = MultipartEntityBuilder.create()
                    .addPart("token", new StringBody(token, ContentType.TEXT_PLAIN))
                    .addPart("folder", new StringBody(folderId, ContentType.TEXT_PLAIN.withCharset("UTF-8")))
                    .addPart("fileName", new StringBody(uploadFile.getName(), ContentType.TEXT_PLAIN.withCharset("UTF-8")))
                    .addPart("file", new FileBody(uploadFile))
                    .setCharset(Charset.forName("UTF-8"))
                    .build();

            httppost.setEntity(reqEntity);
            try (CloseableHttpResponse response = httpclient.execute(httppost)) {
                switch(response.getStatusLine().getStatusCode()) {
                    case 401: {
                        System.out.println("ERROR: [" + index + "] user unauthorized on the server!");
                        LOGGER.log(Level.SEVERE, "[{0}] user unauthorized on the server!", index);
                        error.getAndIncrement();
                        break;
                    }
                    case 409: {
                        System.out.println("ERROR: [" + index + "] the upload file [" + uploadFile.getName() + "] allredy exist in the folder [" + folderName +"]!");
                        LOGGER.log(Level.SEVERE, "[{0}] the upload file [{1}] allredy exist in the folder [{2}]!", new Object[]{index, uploadFile.getName(), folderName});
                        error.getAndIncrement();
                        break;
                    }
                    case 204: {
                        System.out.println("ERROR: [" + index + "] user not have access to upload file in the server folder [" + folderName +"]");
                        LOGGER.log(Level.SEVERE, "[{0}] user not have access to upload file in the server folder [{1}]", new Object[]{index, folderName});
                        error.getAndIncrement();
                        break;
                    }
                    case 400: {
                        System.out.println("ERROR: [" + index + "] the folder [" + folderName + "] is not found on the server");
                        LOGGER.log(Level.SEVERE, "[{0}] the folder [{1}] is not found on the server", new Object[]{index, folderName});
                        error.getAndIncrement();
                        break;
                    }
                    case 500: {
                        System.out.println("ERROR: [" + index + "] A server error occurred while downloading the file [" + uploadFile.getName() + "] ");
                        LOGGER.log(Level.SEVERE, "[{0}] A server error occurred while downloading the file [{1}] ", new Object[]{index, uploadFile.getName()});
                        error.getAndIncrement();
                        break;
                    }
                    case 200: {
                        //System.out.println("INFO: [" + index + "] The file " + uploadFile.getName() + " uploaded to the server");
                        LOGGER.log(Level.INFO, "  [{0}] The file [{1}] uploaded to the server.", new Object[]{index, uploadFile.getName()});
                        if(isDeleteFile) {
                            uploadFile.delete();
                        }
                        break;
                    }
                    default: {
                        System.out.println("INFO: Process upload file status = " + response.getStatusLine());
                        break;
                    }
                }
            }
        }catch (IOException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException ex){
            LOGGER.log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }   
    
    /**
     * Передаёт на сервер учётные данные пользователя. В случае успешной аутентификации получает с сервера token
     * @param url
     * @param login
     * @param password
     * @return true если пользователь авторизовался и false если логин некорректный
     * @throws java.lang.Exception
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
     * @param folder
     * @return 
     * @throws java.lang.Exception
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
    private void initLoadParams(File file){
        serverURL = DEFAULT_URL;
        token = "";
        folderId = "";
        folderName = "";
        try {
            if (file.exists()){
                loadProperties(file);
            } else {
                file.createNewFile();
                saveProperties();
            }
        } catch (IOException | NullPointerException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private void loadProperties(File file) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(file));
        serverURL = (String) properties.get(PROP_SERVER_URL);
        token = (String) properties.get(PROP_TOKEN);
        folderId = (String) properties.get(PROP_FOLDER_ID);
        folderName = (String) properties.getProperty(PROP_FOLDER_NAME);
    }

    /**
     * Сохранение параметров в файл конфигурации
     */
    public void saveProperties(){
        OutputStream output = null;
        try {
            Properties properties = new Properties();
            output = new FileOutputStream(pathConfFile);
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
    private void openFolderSelecterDialog(Callback callback){
        java.awt.EventQueue.invokeLater(
                () -> {
                    FolderSelecter dialog = new FolderSelecter(Main.this, callback);
                    dialog.setResizable(true);
                    dialog.setPreferredSize(new Dimension(550, 350));
                    dialog.pack();
                    dialog.setLocationRelativeTo(null);
                    dialog.setVisible(true);
                });
    }

    /**
     * Отображение диалога логина
     */
    private void openLoginDialog(Callback callback){
        java.awt.EventQueue.invokeLater(
                () -> {
                    LoginUser dialog = new LoginUser(Main.this, callback);
                    dialog.setResizable(true);
                    dialog.setPreferredSize(new Dimension(450, 250));
                    dialog.pack();
                    dialog.setLocationRelativeTo(null);
                    dialog.setVisible(true);
                });
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

    public Boolean getNeedSelectFolder() {
        return isNeedSelectFolder;
    }

    public String getFolderName() {
        return folderName;
    }
    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }
}