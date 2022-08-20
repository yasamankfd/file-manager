package com.example.filemanager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
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
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_SEND_MULTIPLE);
                                    intent.putExtra(Intent.EXTRA_SUBJECT, "Here are some files.");
                                    intent.setType("image/jpeg"); /* This example is sharing jpeg images. */

                                    ArrayList<Uri> files = new ArrayList<Uri>();

                                    for(File f : selectedItems /* List of the files you want to send */) {
                                        Uri uri = FileProvider.getUriForFile(
                                                view.getContext(),
                                                view.getContext().getPackageName()+".provider", f);
                                        files.add(uri);
                                    }

                                    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
                                    view.getContext().startActivity(intent);
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

        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation animFadein = AnimationUtils.loadAnimation(context,R.anim.fade_in);
                holder.more.startAnimation(animFadein);
                File ff = file.get(holder.getAdapterPosition());
                PopupMenu popupMenu = new PopupMenu(view.getContext(),holder.more);
                popupMenu.getMenuInflater().inflate(R.menu.moremenu,popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if(menuItem.getTitle().equals("rename"))
                        {
                            AlertDialog.Builder renameDialog = new AlertDialog.Builder(view.getContext());
                            renameDialog.setTitle("rename file : ");
                            final EditText name1 = new EditText(view.getContext());
                            renameDialog.setView(name1);
                            renameDialog.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i1) {
                                    String new_name = name1.getEditableText().toString();
                                    String extension = ff.getAbsolutePath().substring(ff.getAbsolutePath().lastIndexOf("."));
                                    File current = new File(ff.getAbsolutePath());
                                    File destination = new File(ff.getAbsolutePath().replace(ff.getName(), new_name + extension));
                                    if (current.renameTo(destination)) {
                                        file.set(menuItem.getOrder(),destination);
                                        notifyItemChanged(menuItem.getOrder());
                                        Toast.makeText(view.getContext(), "renamed!", Toast.LENGTH_LONG).show();
                                    } else
                                        Toast.makeText(view.getContext(), "can not be renamed!", Toast.LENGTH_LONG).show();
                                }
                            });
                                renameDialog.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i12) {
                                        dialogInterface.cancel();
                                    }
                                });
                                AlertDialog alertDialog_rename = renameDialog.create();
                                alertDialog_rename.show();

                        }else {
                            AlertDialog.Builder detailDialog = new AlertDialog.Builder(context);
                            detailDialog.setTitle("details");
                            final TextView details = new TextView(context);
                            detailDialog.setView(details);
                            Date lastModified = new Date(ff.lastModified());
                            SimpleDateFormat formatter = new SimpleDateFormat("dd/mm/yyyy HH:mm:ss");
                            String formattedDate = formatter.format(lastModified);
                            details.setText("file name : "+ff.getName()+"\n"+
                                    "size : "+ Formatter.formatShortFileSize(context,ff.length())+"\n"+
                                    "path : "+ff.getAbsolutePath()+"\n"+
                                    "last modified : "+formattedDate);
                            detailDialog.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i13) {
                                    dialogInterface.cancel();
                                }
                            });

                            AlertDialog alertdialog = detailDialog.create();
                            alertdialog.show();
                        }
                        return true;
                    }
                });


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
        ImageView imgFile , imgCheck , more;
        TextView tvSize,tvName,date;
        public FileHolder(@NonNull View itemView) {
            super(itemView);
            more = itemView.findViewById(R.id.more);
            tvName = itemView.findViewById(R.id.tv_fileName);
            tvSize = itemView.findViewById(R.id.tvFileSize);
            date = itemView.findViewById(R.id.tvFiledate);
            imgFile = itemView.findViewById(R.id.img_fileType);
            imgCheck = itemView.findViewById(R.id.iv_check);

        }
    }

    public int getItemCount() {
        return file.size();
    }
}
