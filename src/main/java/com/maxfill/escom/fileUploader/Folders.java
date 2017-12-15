package com.maxfill.escom.fileUploader;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class Folders implements TreeModel{
    @Override
    public Object getRoot() {
        return null;
    }

    @Override
    public Object getChild(Object o, int i) {
        return null;
    }

    @Override
    public int getChildCount(Object o) {
        return 0;
    }

    @Override
    public boolean isLeaf(Object o) {
        return false;
    }

    @Override
    public void valueForPathChanged(TreePath treePath, Object o) {

    }

    @Override
    public int getIndexOfChild(Object o, Object o1) {
        return 0;
    }

    @Override
    public void addTreeModelListener(TreeModelListener treeModelListener) {

    }

    @Override
    public void removeTreeModelListener(TreeModelListener treeModelListener) {

    }
}
