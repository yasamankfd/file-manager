package com.example.filemanager.fragments;

import android.Manifest;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filemanager.FileAddapter;
import com.example.filemanager.FileOpener;
import com.example.filemanager.OnFileSelectedListener;
import com.example.filemanager.R;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
public class InternalFragment extends Fragment implements OnFileSelectedListener {

    File storage;
    String data = "it is null now";
    String[] items = {"details","rename","delete","share"};

    View view;

    @Nullable


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_internal,container,false);

        TextView tv_pathHolder = view.findViewById(R.id.tv_pathHolder);
        ImageView img_back = view.findViewById(R.id.img_back);
        

        
        String internalStorage = System.getenv("EXTERNAL_STORAGE");
        assert internalStorage != null;
        storage = new File(internalStorage);


        try{
            Bundle bundle = getArguments();
            if(bundle!=null)
            {
                data = getArguments().getString("path");
                storage = new File(data);
            }


        }catch (Exception e){
            e.printStackTrace();
        }

        tv_pathHolder.setText(storage.getAbsolutePath());
        runtimePermission();
        return view;
    }

    private void runtimePermission() {
        Dexter.withContext(getContext()).withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE).withListener(
                new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        displayFiles();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();


    }

    public ArrayList<File> findFiles(File file){
        ArrayList<File> arrayList = new ArrayList<>();
        File[] files = file.listFiles();

        assert files != null;
        for(File singleFile : files)
        {

            if (singleFile.isDirectory() && !singleFile.isHidden())
            {
                arrayList.add(singleFile);
            }
        }
        for( File singleFile : files)
        {
//            if(singleFile.getName().toLowerCase().endsWith(".jpg") ||
//                    singleFile.getName().toLowerCase().endsWith(".png") ||
//                    singleFile.getName().toLowerCase().endsWith(".wav") ||
//                    singleFile.getName().toLowerCase().endsWith(".mp4") ||
//                    singleFile.getName().toLowerCase().endsWith(".pdf") ||
//                    singleFile.getName().toLowerCase().endsWith(".docx") ||
//                    singleFile.getName().toLowerCase().endsWith(".apk") ||
//                    singleFile.getName().toLowerCase().endsWith(".mp3") ||
//                    singleFile.getName().toLowerCase().endsWith(".mkv") ||
//                    singleFile.getName().toLowerCase().endsWith(".jpeg"))

            {
                arrayList.add(singleFile);
            }

        }
        return arrayList;
    }
    private void displayFiles() {
        RecyclerView recyclerView = view.findViewById(R.id.recycler_internal);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),1));
        List<File> fileList = new ArrayList<>(findFiles(storage));
        FileAddapter fileAddapter = new FileAddapter(getContext(), fileList, this);
        recyclerView.setAdapter(fileAddapter);

    }

    @Override
    public void onFileClicked(File file) {
        if(file.isDirectory()){
            Bundle bundle = new Bundle();
            bundle.putString("path",file.getAbsolutePath());
            InternalFragment internalFragment = new InternalFragment();
            internalFragment.setArguments(bundle);
            assert getFragmentManager() != null;
            getFragmentManager().beginTransaction().replace(R.id.fragment_container,internalFragment).addToBackStack(null).commit();
        }else {

            try {
                FileOpener.openfile(getContext(),file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onFileLongClicked(File file) {

        final Dialog optionDialog = new Dialog(getContext());
        optionDialog.setContentView(R.layout.option_dialog);
        optionDialog.setTitle("select Option :");
        ListView options = (ListView) optionDialog.findViewById(R.id.list);
        CustomAdapter customAdapter = new CustomAdapter();
        options.setAdapter(customAdapter);
        optionDialog.show();
    }

    class CustomAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(int i) {
            return items[i];
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View myView = getLayoutInflater().inflate(R.layout.option_layout,null);
            ImageView img = myView.findViewById(R.id.imgOption);
            TextView txt = myView.findViewById(R.id.txtOption);
            txt.setText(items[i]);
            if(items[i].equals("details")){
                img.setImageResource(R.drawable.ic_info);
            }else if(items[i].equals("delete")){
                img.setImageResource(R.drawable.ic_delete);
            }if(items[i].equals("share")){
                img.setImageResource(R.drawable.ic_share);
            }else if(items[i].equals("rename")){
                img.setImageResource(R.drawable.ic_rename);
            }
            return myView;
        }
    }
}
