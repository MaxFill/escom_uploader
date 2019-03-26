/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.maxfill.escom.fileUploader;

import com.maxfill.escom.fileUploader.folders.Folder;
import junit.framework.TestCase;

/**
 *
 * @author Maxim
 */
public class UtilsTest extends TestCase {
    
    private final Folder folder = new Folder(0, "FolderName", Boolean.TRUE);
    
    public UtilsTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of getPath method, of class Utils.
     */
    public void testGetPath() {      
        String expResult = "FolderName";
        String result = Utils.getPath(folder);
        assertEquals(expResult, result);
    }
        
    public void testGetPath2() {
        Folder parent = new Folder(1, "ParentName", Boolean.TRUE);
        folder.setParent(parent);
        String expResult = "ParentName->FolderName";
        String result = Utils.getPath(folder);
        assertEquals(expResult, result);
    }
    
    
}
