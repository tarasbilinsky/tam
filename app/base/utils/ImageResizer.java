package base.utils;



/*
 *



writer.write(null, image, iwp);
writer.dispose();
 *
 */

import java.io.*;

public class ImageResizer {

    public static boolean isJPEG(InputStream is) {
        DataInputStream ins = new DataInputStream(is);
        try {
            if (ins.readInt() == 0xffd8ffe0) {
                return true;
            } else {
                return false;

            }

        } catch (IOException e) {
            return false;
        } finally {
            try {
                ins.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static InputStream getScaledInstance(InputStream is, int targetWidth, int targetHeight, String imageFormat, int quality){

        javaxt.io.Image img = new javaxt.io.Image(is);
        if(img.getBufferedImage()==null) return null;
        img.rotate();
        img.resize(targetWidth,targetHeight,true);
        img.setOutputQuality(quality);
        File tmpFile;
        try {
            tmpFile = File.createTempFile("temp-file-name", "."+imageFormat);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        img.saveAs(tmpFile);

        try {
            return new FileInputStream(tmpFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }



    }


}
