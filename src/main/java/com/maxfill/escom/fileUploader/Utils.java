package com.maxfill.escom.fileUploader;

public final class Utils{

    private Utils() {
    }

    /* Формирование полного пути для папки */
    public static String getPath(Folder folder){
        StringBuilder sb = new StringBuilder();
        if (folder.getParent() != null){
            sb.append(getPath(folder.getParent()));
        }
        if (sb.length() > 0){
            sb.append("->");
        }
        sb.append(folder.getName());
        return sb.toString();
    }
}
