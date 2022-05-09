package com.example.filemanager;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

public class AsyncFileTypes extends AsyncTask<Void,Void, ArrayList<File>> {

    File files ;
    String type;

    public AsyncFileTypes(File files,String type) {
        this.files = files;
        this.type = type;

    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public class find implements FileVisitor<Path> {


        ArrayList<File> arrayList = new ArrayList<>();

        @Override
        public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes basicFileAttributes) {

            File ff = new File(String.valueOf(path));
            String s = ff.getName();
            if((s.contains("Android") || s.contains("voice") || s.startsWith(".") || s.contains("Cache") || s.contains("cache") || s.contains("Sent"))&& ff.isDirectory()){
                return  SKIP_SUBTREE;
            }else
                return CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
            File ff = new File(String.valueOf(file));
            String s = ff.getName();

                if (attr.isSymbolicLink()) {
                    System.out.format("Symbolic link: %s ", file);
                }
                if (!ff.isDirectory() && attr.isRegularFile() && !ff.isHidden() ) {

                    switch (type){
                        case "doc":
                            if(s.endsWith(".docx") || s.endsWith(".pdf") || s.endsWith(".txt") || s.endsWith(".pptx") || s.endsWith(".xml") || s.endsWith(".xls"))
                            {
                                arrayList.add(new File(String.valueOf(file)));
                            }
                            break;
                        case "image":
                            if(s.endsWith(".png") || s.endsWith(".jpg") || s.endsWith(".jpeg") || s.endsWith(".gif") )
                            {
                                arrayList.add(new File(String.valueOf(file)));
                            }
                            break;
                        case "video":
                            if(s.endsWith(".mp4") || s.endsWith("..mkv") || s.endsWith(".avi") || s.endsWith(".wmv") || s.endsWith(".xml") )
                            {
                                arrayList.add(new File(String.valueOf(file)));
                            }
                            break;
                        case "music":
                            if(s.endsWith(".mp3") || s.endsWith(".wav") || s.endsWith(".aac") )
                            {
                                arrayList.add(new File(String.valueOf(file)));
                            }
                            break;
                        case "apk":
                            if(s.endsWith(".apk"))
                            {
                                arrayList.add(new File(String.valueOf(file)));
                            }
                            break;
                        case "download":
                            arrayList.add(new File(String.valueOf(file)));
                            break;
                    }

                }
                return CONTINUE;

        }

        @Override
        public FileVisitResult visitFileFailed(Path path, IOException e) {
            System.err.println(e);
            return SKIP_SUBTREE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException e) {
            System.out.format("Directory: %s%n", dir);
            File ff = new File(String.valueOf(dir));
            String s = ff.getName();
            if((s.contains("Android") || s.contains("voice") )&& ff.isDirectory()){
                return  SKIP_SUBTREE;
            }else
                return CONTINUE;
        }
        public ArrayList<File> getres(){
            return arrayList;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected ArrayList<File> doInBackground(Void... voids) {
        ArrayList<File> result = new ArrayList<>();
        try {

                find f = new find();
                Files.walkFileTree(Paths.get(Environment.getExternalStorageDirectory().getAbsolutePath()),f);
                result = f.getres();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
