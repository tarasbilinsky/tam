package base.utils;

import java.io.*;
import java.net.URI;
import java.util.Deque;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Zip {
    public static void zip(File directory, File zipfile) throws IOException {
        URI base = directory.toURI();
        Deque<File> queue = new LinkedList<File>();
        queue.push(directory);
        OutputStream out = new FileOutputStream(zipfile);
        Closeable res = out;
        try {
            ZipOutputStream zout = new ZipOutputStream(out);
            while (!queue.isEmpty()) {
                directory = queue.pop();
                for (File kid : directory.listFiles()) {
                    String name = base.relativize(kid.toURI()).getPath();
                    if (kid.isDirectory()) {
                        queue.push(kid);
                        name = name.endsWith("/") ? name : name + "/";
                        zout.putNextEntry(new ZipEntry(name));
                    } else {
                        zout.putNextEntry(new ZipEntry(name));
                        copy(kid, zout);
                        zout.closeEntry();
                    }
                }
            }
            zout.close();
        } finally {
            res.close();
        }
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        while (true) {
            int readCount = in.read(buffer);
            if (readCount < 0) {
                break;
            }
            out.write(buffer, 0, readCount);
        }
    }

    public static void copy(File file, OutputStream out) throws IOException {
        InputStream in = new FileInputStream(file);
        try {
            copy(in, out);
        } finally {
            in.close();
        }
    }

    public static void copy(String s, OutputStream out) throws IOException {
        out.write(s.getBytes("UTF-8"));
    }


    public static void unzip(String filename, String destinationname) {
        try {
            byte[] buf = new byte[1024000];
            ZipInputStream zipinputstream = null;
            ZipEntry zipentry;
            zipinputstream = new ZipInputStream(
                    new FileInputStream(filename));

            zipentry = zipinputstream.getNextEntry();
            while (zipentry != null) {
                //for each entry to be extracted
                String entryName = destinationname + zipentry.getName();
                entryName = entryName.replace('/', File.separatorChar);
                entryName = entryName.replace('\\', File.separatorChar);
                //System.out.println("entryname " + entryName);
                int n;
                FileOutputStream fileoutputstream;
                File newFile = new File(entryName);
                if (zipentry.isDirectory()) {
                    if (!newFile.mkdirs()) {
                        break;
                    }
                    zipentry = zipinputstream.getNextEntry();
                    continue;
                }

                fileoutputstream = new FileOutputStream(entryName);

                while ((n = zipinputstream.read(buf, 0, 1024)) > -1) {
                    fileoutputstream.write(buf, 0, n);
                }

                fileoutputstream.close();
                zipinputstream.closeEntry();
                zipentry = zipinputstream.getNextEntry();

            }//while

            zipinputstream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
