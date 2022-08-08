package com.example.filemanager;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;

public class FileOpener {
    public static  void openfile(Context context, File file) throws IOException {

        File selectedFile = file;
        Uri uri = FileProvider.getUriForFile(context,context.getPackageName()+".provider",selectedFile);
        Intent intent = new Intent(Intent.ACTION_VIEW);

        if(uri.toString().contains(".doc")){
            intent.setDataAndType(uri,"application/msword");

        }else if(uri.toString().contains(".pdf")){
            intent.setDataAndType(uri,"application/pdf");

        }else if(uri.toString().contains(".mp3") || uri.toString().contains(".wav")){
            intent.setDataAndType(uri,"audio/x-wav");

        }else if(uri.toString().contains(".png") || uri.toString().contains(".jpg") || uri.toString().contains(".jpeg")){
            intent.setDataAndType(uri,"image/jpeg");

        }else if(uri.toString().contains(".mp4")){
            intent.setDataAndType(uri,"video/*");

        }else if(uri.toString().contains(".apk"))
            {
                intent.setDataAndType(uri,"application/vnd.android.package-archive");
            }else{intent.setDataAndType(uri,"*/*");}

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            context.startActivity(intent);
        }
        catch (ActivityNotFoundException e) {
            Toast.makeText(context,
                    "No Application Available to open file !",
                    Toast.LENGTH_SHORT).show();
        }

    }
}
