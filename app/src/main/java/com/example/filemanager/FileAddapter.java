package com.example.filemanager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class FileAddapter extends RecyclerView.Adapter<FileViewHolder> {
    private final Context context;
    private final List<File> file;
    private final OnFileSelectedListener listener;

    public FileAddapter(Context context, List<File> file, OnFileSelectedListener listener) {
        this.context = context;
        this.file = file;
        this.listener = listener;
    }

    @NonNull
    @Override

    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FileViewHolder(LayoutInflater.from(context).inflate(R.layout.file_container,parent,false));

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, @SuppressLint("RecyclerView") int position) {

        String ss = file.get(position).getName();
        holder.tvName.setText(ss);


        holder.tvName.setSelected(true);
        int items = 0;
        if(file.get(position).isDirectory())
        {
            File[] files = file.get(position).listFiles();
            if(files!=null)
            {
                for(File singleFile : files){
                if(!singleFile.isHidden())
                {
                    items+=1;
                }
            }

            }

            String s = String.valueOf(items);
            holder.tvSize.setText(s+"Files");

        }else {
            String s = Formatter.formatShortFileSize(context, file.get(position).length());
            if(s != null)
            {
                holder.tvSize.setText(s);

            }else  holder.tvSize.setText("null");

        }
        if( file.get(position).getName().toLowerCase().endsWith(".png")){
            holder.imgFile.setImageResource(R.drawable.ic_photo);
        }else if(file.get(position).getName().toLowerCase().endsWith(".jpg") ){
            holder.imgFile.setImageResource(R.drawable.ic_photo);
        }else if(file.get(position).getName().toLowerCase().endsWith(".jpeg") ){
            holder.imgFile.setImageResource(R.drawable.ic_photo);
        }else
        if(file.get(position).getName().toLowerCase().endsWith(".pdf")){
            holder.imgFile.setImageResource(R.drawable.ic_pdf);
        }else
        if(file.get(position).getName().toLowerCase().endsWith(".docx")){
            holder.imgFile.setImageResource(R.drawable.ic_doc);
        }else
        if( file.get(position).getName().toLowerCase().endsWith(".wav")){
            holder.imgFile.setImageResource(R.drawable.ic_music);
        }else
            if(file.get(position).getName().toLowerCase().endsWith(".mp3") || file.get(position).getName().toLowerCase().endsWith(".mkv")){
            holder.imgFile.setImageResource(R.drawable.ic_music);
        }else
        if(file.get(position).getName().toLowerCase().endsWith(".mp4")){
            holder.imgFile.setImageResource(R.drawable.ic_movie);
        }else
        if(file.get(position).getName().toLowerCase().endsWith(".apk")){
            holder.imgFile.setImageResource(R.drawable.ic_app);
        }else if(file.get(position).isDirectory()){
            holder.imgFile.setImageResource(R.drawable.ic_folder);
        }else {
            holder.imgFile.setImageResource(R.drawable.ic_unkown);
        }

        holder.container.setOnClickListener(view -> listener.onFileClicked(file.get(position)));

        holder.container.setOnLongClickListener(view -> {
            listener.onFileLongClicked(file.get(position));
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return file.size();
    }
}
