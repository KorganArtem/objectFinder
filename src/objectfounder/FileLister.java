/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objectfounder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 *
 * @author korgan
 */
public class FileLister {
    public void list() throws IOException{
        File rootFolder = new File("C:\\smartdoc\\SCAN");
        int folderIndex = 0;
        for(File entry : getListInFolder(rootFolder)){
            folderIndex++;
            if(entry.isDirectory()){
                for(File lowEntry : getListInFolder(entry)){
                    //System.out.println(folderIndex+ " --> " + lowEntry.getAbsolutePath()+ " " + lowEntry.getName()+ "  " + lowEntry.getUsableSpace());
                    moveFile(lowEntry, folderIndex, lowEntry.getName());
                }
            }
        }
    }
    private File[] getListInFolder(File folder){
        File[] fileList = folder.listFiles();
        return fileList;
    }
    private void moveFile(File srcFile, int idFolder, String fileName) throws IOException{
        String shortName = idFolder+"000"+fileName.split("_")[2];
        File dstFile = new File("C:\\smartdoc\\TRASH\\"+shortName);
        System.out.println(srcFile + "   " + dstFile.getName() );
        Files.copy(srcFile.toPath(), dstFile.toPath());
    }

}
