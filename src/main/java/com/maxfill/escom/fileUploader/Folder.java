package com.maxfill.escom.fileUploader;

public class Folder{
    private final Integer id;
    private final String name;
    private final Boolean readonly;

    private Folder parent;
    private boolean childsLoaded;

    public Folder(Integer id, String name, Boolean readonly) {
        this.id = id;
        this.name = name;
        this.readonly = readonly;
    }

    public Folder(Folder parent, Integer id, String name, Boolean readonly) {
        this.id = id;
        this.name = name;
        this.readonly = readonly;
        this.parent = parent;
    }

    /* gets & sets */

    public Integer getId() {
        return id;
    }

    public Boolean getReadonly() {
        return readonly;
    }

    public String getName() {
        return name;
    }

    public Folder getParent() {
        return parent;
    }
    public void setParent(Folder parent) {
        this.parent = parent;
    }

    public boolean isChildsLoaded() {
        return childsLoaded;
    }
    public void setChildsLoaded(boolean childsLoaded) {
        this.childsLoaded = childsLoaded;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        Folder folder = (Folder) o;

        if(!id.equals(folder.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
