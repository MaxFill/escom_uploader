package com.maxfill.escom.fileUploader;

import com.maxfill.escom.fileUploader.folders.Folder;
import java.util.Optional;

public final class Utils{

    private Utils() {
    }

    /* Формирование полного пути для папки */
    public static String getPath(Folder folder){
        StringBuilder sb = new StringBuilder();
        Optional<Folder> parent = Optional.ofNullable(folder.getParent());
        if (parent.isPresent()){
            sb.append(getPath(folder.getParent()));
        }
        if (sb.length() > 0){
            sb.append("->");
        }
        sb.append(folder.getName());
        return sb.toString();
    }
}
