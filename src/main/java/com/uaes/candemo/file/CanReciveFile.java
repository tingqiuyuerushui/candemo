package com.uaes.candemo.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CanReciveFile implements WriteFile {
    private String filePath;

    public CanReciveFile(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void writeFile(byte[] b) {
        try {
            FileOutputStream fos = new FileOutputStream(new File(filePath),true);
            if(fos != null){
                fos.write(b);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
