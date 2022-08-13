package com.example.filemanager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filemanager.fragments.InternalFragment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileAddapter extends RecyclerView.Adapter<FileAddapter.FileHolder> {
    Activity activity;
    private final Context context;
    private final List<File> file;
    List<File> selectedItems = new ArrayList<>();
    boolean isEnable = false;
    boolean isSelectAll = false;
    //private final OnFileSelectedListener listener;

    public FileAddapter(Context context, List<File> file) {
        this.context = context;
        this.file = file;
        //this.listener = listener;
    }

    @NonNull
    @Override

    public FileHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FileHolder(LayoutInflater.from(context).inflate(R.layout.file_container,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull FileHolder holder, int position) {
        Date lastModified = new Date(file.get(position).lastModified());
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy     HH:mm");
        String formattedDateString = formatter.format(lastModified);
        String ss = file.get(position).getName();
        holder.tvName.setText(ss);
        holder.date.setText(formattedDateString);

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
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(!isEnable){
                    ActionMode.Callback callback = new ActionMode.Callback() {
                        @Override
                        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                            MenuInflater menuInflater = actionMode.getMenuInflater();
                            menuInflater.inflate(R.menu.delmenu,menu);
                            return true;
                        }
                        @Override
                        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                            isEnable = true;
                            ClickItem(holder);
                            return true;
                        }
                        @Override
                        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                            int id = menuItem.getItemId();
                            switch (id){
                                case R.id.menu_delete:
                                    for(File f : selectedItems){
                                        f.delete();
                                        file.remove(f); }
                                    actionMode.finish();
                                    break;
                                case R.id.menu_selectAll:
                                {
                                    if(selectedItems.size() == file.size()){
                                        actionMode.finish();
                                        isSelectAll = false;
                                        selectedItems.clear();
                                    }else{
                                        isSelectAll = true;
                                        selectedItems.clear();
                                        selectedItems.addAll(file);
                                    }
                                    notifyDataSetChanged();
                                }
                                    break;
                                case R.id.menu_share:
                            }
                            return true;
                        }

                        @Override
                        public void onDestroyActionMode(ActionMode actionMode) {
                            isEnable = false;
                            isSelectAll = false;
                            selectedItems.clear();
                            notifyDataSetChanged();
                        }
                    };
                    ((AppCompatActivity) view.getContext()).startActionMode(callback);
                }else { ClickItem(holder); }
                return true;
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File ff = file.get(holder.getAdapterPosition());
                if(isEnable)
                {
                    ClickItem(holder);
                }else
                {
                    if(ff.isDirectory()){
            Bundle bundle = new Bundle();
            bundle.putString("path",ff.getAbsolutePath());
            InternalFragment internalFragment = new InternalFragment();
            internalFragment.setArguments(bundle);
            ((FragmentActivity)context).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, internalFragment).addToBackStack("crop_type").commit();
        }else
        {
            try {
                FileOpener.openfile(context,ff);
            } catch ( IOException e) {
                e.printStackTrace();
            }
        }
                }
            }
        });
        if(isSelectAll){
            holder.imgCheck.setVisibility(View.VISIBLE);
            holder.itemView.setBackgroundColor(Color.GREEN);
        }else{
            holder.imgCheck.setVisibility(View.GONE);
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
    }
    private void ClickItem(FileHolder holder) {
        File thisFile = file.get(holder.getAdapterPosition());
        if(holder.imgCheck.getVisibility() == View.GONE){
            holder.imgCheck.setVisibility(View.VISIBLE);
            holder.itemView.setBackgroundColor(Color.GREEN);
            selectedItems.add(thisFile);
        }else{
            holder.imgCheck.setVisibility(View.GONE);
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            selectedItems.remove(thisFile);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class FileHolder extends RecyclerView.ViewHolder{
        ImageView imgFile , imgCheck;
        TextView tvSize,tvName,date;
        public FileHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_fileName);
            tvSize = itemView.findViewById(R.id.tvFileSize);
            date = itemView.findViewById(R.id.tvFiledate);
            imgFile = itemView.findViewById(R.id.img_fileType);
            imgCheck = itemView.findViewById(R.id.iv_check);
//            itemView.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View view) {
//                    isSelected = true;
//                    if(selectedItems.contains(file.get(getAdapterPosition()))){
//                        itemView.setBackgroundColor(Color.TRANSPARENT);
//                        selectedItems.remove(file.get(getAdapterPosition()));
//                    }else{
//                        itemView.setBackgroundResource(R.color.teal_700);
//                        selectedItems.add(file.get(getAdapterPosition()));
//                    }
//                    if(selectedItems.size() == 0){
//                        isSelected = false;
//                    }
//                    return true;
//                }
//            });
//            itemView.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View view) {
//                    if(isSelected){
//                        if(selectedItems.contains(file.get(getAdapterPosition()))){
//                            itemView.setBackgroundColor(Color.TRANSPARENT);
//                            selectedItems.remove(file.get(getAdapterPosition()));
//                        }else {
//                            itemView.setBackgroundResource(R.color.teal_700);
//                            selectedItems.add(file.get(getAdapterPosition()));
//                        }
//                        if(selectedItems.size() == 0){
//                            isSelected = false;
//                        }
//                    }else;
//                    return false;
//                }
//            });
        }
    }


    @SuppressLint("SetTextI18n")
//    @Override
//    public void onBindViewHolder(@NonNull FileViewHolder holder, @SuppressLint("RecyclerView") int position) {
//
//        Date lastModified = new Date(file.get(position).lastModified());
//        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy     HH:mm");
//        String formattedDateString = formatter.format(lastModified);
//        String ss = file.get(position).getName();
//        holder.tvName.setText(ss);
//        holder.date.setText(formattedDateString);
//
//
//        holder.tvName.setSelected(true);
//        int items = 0;
//        if(file.get(position).isDirectory())
//        {
//            File[] files = file.get(position).listFiles();
//            if(files!=null)
//            {
//                for(File singleFile : files){
//                if(!singleFile.isHidden())
//                {
//                    items+=1;
//                }
//            }
//            }
//            String s = String.valueOf(items);
//            holder.tvSize.setText(s+"Files");
//
//        }else {
//            String s = Formatter.formatShortFileSize(context, file.get(position).length());
//            if(s != null)
//            {
//                holder.tvSize.setText(s);
//
//            }else  holder.tvSize.setText("null");
//
//        }
//        if( file.get(position).getName().toLowerCase().endsWith(".png")){
//            holder.imgFile.setImageResource(R.drawable.ic_photo);
//        }else if(file.get(position).getName().toLowerCase().endsWith(".jpg") ){
//            holder.imgFile.setImageResource(R.drawable.ic_photo);
//        }else if(file.get(position).getName().toLowerCase().endsWith(".jpeg") ){
//            holder.imgFile.setImageResource(R.drawable.ic_photo);
//        }else
//        if(file.get(position).getName().toLowerCase().endsWith(".pdf")){
//            holder.imgFile.setImageResource(R.drawable.ic_pdf);
//        }else
//        if(file.get(position).getName().toLowerCase().endsWith(".docx")){
//            holder.imgFile.setImageResource(R.drawable.ic_doc);
//        }else
//        if( file.get(position).getName().toLowerCase().endsWith(".wav")){
//            holder.imgFile.setImageResource(R.drawable.ic_music);
//        }else
//            if(file.get(position).getName().toLowerCase().endsWith(".mp3") || file.get(position).getName().toLowerCase().endsWith(".mkv")){
//            holder.imgFile.setImageResource(R.drawable.ic_music);
//        }else
//        if(file.get(position).getName().toLowerCase().endsWith(".mp4")){
//            holder.imgFile.setImageResource(R.drawable.ic_movie);
//        }else
//        if(file.get(position).getName().toLowerCase().endsWith(".apk")){
//            holder.imgFile.setImageResource(R.drawable.ic_app);
//        }else if(file.get(position).isDirectory()){
//            holder.imgFile.setImageResource(R.drawable.ic_folder);
//        }else {
//            holder.imgFile.setImageResource(R.drawable.ic_unkown);
//        }
//
//        holder.container.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                listener.onFileClicked(file.get(position));
//            }
//        });
//
//        holder.container.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//                listener.onFileLongClicked(file.get(position), position);
//                return true;
//            }
//        });
//    }

    @Override
    public int getItemCount() {
        return file.size();
    }
}
