package com.example.filemanager;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.example.filemanager.fragments.HomeFragment;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AsyncRecent extends AsyncTask<Void,Void,ArrayList<File>> {
    File files ;

    public AsyncRecent(File files) {
        this.files = files;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public class findFiles3 implements FileVisitor<Path> {


        ArrayList<File> arrayList = new ArrayList<>();

        @Override
        public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes basicFileAttributes) {

            File ff = new File(String.valueOf(path));
            String s = ff.getName();
            if((s.contains("Sent") || s.contains("cache") || s.contains("Android") || s.contains("Cache") || s.startsWith(".") || s.contains("Backup") || s.contains("Database"))&& ff.isDirectory()){
                return  SKIP_SUBTREE;
            }else
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
            File ff = new File(String.valueOf(file));
            String s = ff.getName();
            LocalDate now = LocalDate.now(),lastmonth = now.minusDays(2);
            Date lastModified = new Date(ff.lastModified());
            Date lastMonth = java.util.Date.from(lastmonth.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

                if (attr.isSymbolicLink()) {
                    System.out.format("Symbolic link: %s ", file);
                } else if(!lastModified.before(lastMonth)){
                    if (!ff.isDirectory() && attr.isRegularFile() && !ff.isHidden() && (s.endsWith(".jpg") || s.endsWith(".jpeg") || s.endsWith(".png") ||
                            s.endsWith(".mp3") || s.endsWith(".mp4") || s.endsWith(".docx") ||
                            s.endsWith(".pdf") || s.endsWith(".apk") || s.endsWith(".wav") ||
                            s.endsWith(".mkv") || s.endsWith(".srt") || s.endsWith(".avi") ||
                            s.endsWith(".gif") || s.endsWith(".txt") || s.endsWith(".pptx") ||
                            s.endsWith(".xml") || s.endsWith(".xls") ) ) {
                        arrayList.add(new File(String.valueOf(file)));
                        System.out.println("-----------------------"+file);
                    }} else {
                    //System.out.format("Other: %s ", file);
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
            //System.out.format("Directory: %s%n", dir);
            File ff = new File(String.valueOf(dir));
            String s = ff.getName();
            if((s.contains("sent") || s.contains("cache") || s.contains("Android") || s.contains("voice") || s.startsWith("."))&& ff.isDirectory()){
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
        ArrayList<File> result2 = new ArrayList<>();
        try {
            findFiles3 f = new findFiles3();
           Files.walkFileTree(Paths.get(Environment.getExternalStorageDirectory().getAbsolutePath()),f);
           result = f.getres();


        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("0000000000000000000000000000000000000000000000000000");
        result.sort(Comparator.comparing(File::lastModified).reversed());
        System.out.println("1111111111111111111111111111111111111111111111111111");

        int j;
        if(result.size()>200)
        {
            j=200;
        }else j=result.size();
        for(int i=0;i<j;i++)
        {
            result2.add(result.get(i));
        }
        return result2;
    }
}
