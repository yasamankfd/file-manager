package com.example.filemanager.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filemanager.BuildConfig;
import com.example.filemanager.FileAddapter;
import com.example.filemanager.FileOpener;
import com.example.filemanager.OnFileSelectedListener;
import com.example.filemanager.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CategorizedFragment extends Fragment implements OnFileSelectedListener {

    List<File> fileList;
    FileAddapter fileAddapter;
    File storage;
    String data = "it is null now";
    String[] items = {"details","rename","delete","share"};
    ActivityResultLauncher<Intent> activityResultLauncher;
    File path;

    View view;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Nullable


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_categorized,container,false);

        ImageView img_back = view.findViewById(R.id.img_back);
        

        
//        String internalStorage = System.getenv("EXTERNAL_STORAGE");
//        assert internalStorage != null;
//        storage = new File(internalStorage);

        Bundle b = this.getArguments();
        if(b.getString("fileType").equals("download"))
        {
            path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        }
        else if(b.getString("fileType").equals("image"))
        {
            path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        } if(b.getString("fileType").equals("music"))
        {
            path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);

        }if(b.getString("fileType").equals("video"))
        {
            path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);

        }if(b.getString("fileType").equals("doc"))
        {
            path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

        }if(b.getString("fileType").equals("apk"))
        {
            path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

        }


        runtimePermission();
        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void runtimePermission() {


        if(checkpermission())
        {

            displayFiles();
        }else{
            requestpermission();
            displayFiles();
        }
//        Dexter.withContext(getContext()).withPermissions(Manifest.permission.MANAGE_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE).withListener(
//                new MultiplePermissionsListener() {
//                    @Override
//                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
//                        displayFiles();
//                    }
//
//                    @Override
//                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
//                        permissionToken.continuePermissionRequest();
//                    }
//                }).check();


    }




    private void requestpermission() {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.R){
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s",new Object[]{getContext().getPackageName()})));
                startActivity(intent);
            }catch (Exception e){
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }
        }else{
            String[] pers = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(getActivity(),pers,30);
        }
    }

    private boolean checkpermission() {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.R){
            return Environment.isExternalStorageManager();
        }else{
            int read = ContextCompat.checkSelfPermission(getContext(),Manifest.permission.READ_EXTERNAL_STORAGE);
            int write = ContextCompat.checkSelfPermission(getContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE);
return read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED;
             }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 30:
                if(grantResults.length>0)
                {
                    boolean readper = grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean writeper = grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if(readper && writeper){
                        Toast.makeText(getContext(), "permission granted", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getContext(), "permission denied", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(getContext(), "you denied permission", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public ArrayList<File> findFiles(File file){
        ArrayList<File> arrayList = new ArrayList<>();
        File[] files = file.listFiles();

        assert files != null;
        for(File singleFile : files)
        {

            if (singleFile.isDirectory() && !singleFile.isHidden())
            {
                arrayList.addAll(findFiles(singleFile));
            }else {

                switch (getArguments().getString("fileType")){
                    case "image":
                        if(singleFile.getName().toLowerCase().endsWith(".jpeg") || singleFile.getName().toLowerCase().endsWith(".jpg") || singleFile.getName().toLowerCase().endsWith(".png"))
                        {
                            arrayList.add(singleFile);
                        }
                        break;
                    case "video":
                        if (singleFile.getName().toLowerCase().endsWith(".mp4")|| singleFile.getName().toLowerCase().endsWith(".mkv") || singleFile.getName().toLowerCase().endsWith(".avi"))
                        {
                            arrayList.add(singleFile);
                        }
                        break;
                    case "music":
                        if(singleFile.getName().toLowerCase().endsWith(".mp3")|| singleFile.getName().toLowerCase().endsWith(".wav"))
                        {
                            arrayList.add(singleFile);
                        }
                        break;
                    case "apk":
                        if(singleFile.getName().toLowerCase().endsWith(".apk"))
                        {
                            arrayList.add(singleFile);
                        }
                        break;
                    case "doc":
                        if(singleFile.getName().toLowerCase().endsWith(".pdf")|| singleFile.getName().toLowerCase().endsWith(".docx"))
                        {
                            arrayList.add(singleFile);
                        }
                        break;
                    case "download":
                        arrayList.add(singleFile);
                        break;

                }
            }
        }
//        for( File singleFile : files)
//        {
//                arrayList.add(singleFile);
//        }
        return arrayList;
    }
    private void displayFiles() {
        RecyclerView recyclerView = view.findViewById(R.id.recycler_internal);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),1));
        fileList = new ArrayList<>(findFiles(path));
        fileAddapter = new FileAddapter(getContext(), fileList, this);
        recyclerView.setAdapter(fileAddapter);

    }

    @Override
    public void onFileClicked(File file) {
        if(file.isDirectory()){
            Bundle bundle = new Bundle();
            bundle.putString("path",file.getAbsolutePath());
            CategorizedFragment internalFragment = new CategorizedFragment();
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
    public void onFileLongClicked(File file,int position) {

        final Dialog optionDialog = new Dialog(getContext());
        optionDialog.setContentView(R.layout.option_dialog);
        optionDialog.setTitle("select Option :");
        ListView options = (ListView) optionDialog.findViewById(R.id.list);
        CustomAdapter customAdapter = new CustomAdapter();
        options.setAdapter(customAdapter);
        optionDialog.show();
        options.setOnItemClickListener((adapterView, view, i, l) -> {
            String selectedItem = adapterView.getItemAtPosition(i).toString();
            switch (selectedItem){
                case "details":
                    AlertDialog.Builder detailDialog = new AlertDialog.Builder(getContext());
                    detailDialog.setTitle("details");
                    final TextView details = new TextView(getContext());
                    detailDialog.setView(details);
                    Date lastModified = new Date(file.lastModified());
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/mm/yyyy HH:mm:ss");
                    String formattedDate = formatter.format(lastModified);
                    details.setText("file name : "+file.getName()+"\n"+
                            "size : "+ Formatter.formatShortFileSize(getContext(),file.length())+"\n"+
                            "path : "+file.getAbsolutePath()+"\n"+
                            "last modified : "+formattedDate);
                    detailDialog.setPositiveButton("ok", (dialogInterface, i13) -> optionDialog.cancel());

                    AlertDialog alertdialog = detailDialog.create();
                    alertdialog.show();
                    break;
                case "rename":
                   AlertDialog.Builder renameDialog = new AlertDialog.Builder(getContext());
                   renameDialog.setTitle("rename file : ");
                   final EditText name = new EditText(getContext());
                   renameDialog.setView(name);
                   renameDialog.setPositiveButton("ok", (dialogInterface, i1) -> {
                       String new_name = name.getEditableText().toString();
                       String extension = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("."));
                       File current = new File(file.getAbsolutePath());
                       File destination = new File(file.getAbsolutePath().replace(file.getName(),new_name+extension));
                       if(current.renameTo(destination)){
                           fileList.set(position,destination);
                            fileAddapter.notifyItemChanged(position);
                           Toast.makeText(getContext(),"renamed!",Toast.LENGTH_LONG).show();
                       }else Toast.makeText(getContext(),"can not be renamed!",Toast.LENGTH_LONG).show();
                   });
                    renameDialog.setNegativeButton("cancel", (dialogInterface, i12) -> optionDialog.cancel());
                    AlertDialog alertDialog_rename = renameDialog.create();
                    alertDialog_rename.show();
                    break;
                case "delete":
                    AlertDialog.Builder deletedialog =  new AlertDialog.Builder(getContext());
                    deletedialog.setTitle("delete "+file.getName()+" ?");
                    deletedialog.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            file.delete();
                            fileList.remove(position);
                            fileAddapter.notifyDataSetChanged();
                        }
                    });
                    deletedialog.setNegativeButton("no", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            optionDialog.cancel();

                        }
                    });
                    AlertDialog alertDialog_delete = deletedialog.create();
                    alertDialog_delete.show();
                    break;
                case "share" :
                    if(!file.isDirectory())
                    {
                        String nam = file.getName();
                        Intent share = new Intent();
                        share.setAction(Intent.ACTION_SEND);
                        share.setType("image/jpeg");

                        Uri p = FileProvider.getUriForFile(
                                requireContext(),
                                BuildConfig.APPLICATION_ID + ".provider",
                                file);

                        share.putExtra(Intent.EXTRA_STREAM,p);
                        startActivity(Intent.createChooser(share,"share "+ nam));
                    }else Toast.makeText(getContext(),"directory cant be shared !",Toast.LENGTH_SHORT).show();

                    break;

            }
        });
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
