package com.maxfill.escom.fileUploader.folders;

public class Folder{
    private final Integer id;
    private final String name;
    private final Boolean readOnly;

    private Folder parent;
    private boolean childsLoaded;

    public Folder(Integer id, String name, Boolean readonly) {
        this.id = id;
        this.name = name;
        this.readOnly = readonly;
    }

    public Folder(Folder parent, Integer id, String name, Boolean readonly) {
        this.id = id;
        this.name = name;
        this.readOnly = readonly;
        this.parent = parent;
    }

    /* gets & sets */

    public Integer getId() {
        return id;
    }

    public Boolean getReadOnly() {
        return readOnly;
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
