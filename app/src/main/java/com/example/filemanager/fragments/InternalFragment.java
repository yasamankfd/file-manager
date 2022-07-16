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
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class InternalFragment extends Fragment implements OnFileSelectedListener {

    public class AlphanumFileComparator implements Comparator
    {

        private final boolean isDigit(char ch)
        {
            return ch >= 48 && ch <= 57;
        }


        private final String getChunk(String s, int slength, int marker)
        {
            StringBuilder chunk = new StringBuilder();
            char c = s.charAt(marker);
            chunk.append(c);
            marker++;
            if (isDigit(c))
            {
                while (marker < slength)
                {
                    c = s.charAt(marker);
                    if (!isDigit(c))
                        break;
                    chunk.append(c);
                    marker++;
                }
            } else
            {
                while (marker < slength)
                {
                    c = s.charAt(marker);
                    if (isDigit(c))
                        break;
                    chunk.append(c);
                    marker++;
                }
            }
            return chunk.toString();
        }

        public int compare(Object o1, Object o2)
        {
            if (!(o1 instanceof File) || !(o2 instanceof File))
            {
                return 0;
            }
            File f1 = (File)o1;
            File f2 = (File)o2;
            String s1 = f1.getName();
            String s2 = f2.getName();

            int thisMarker = 0;
            int thatMarker = 0;
            int s1Length = s1.length();
            int s2Length = s2.length();

            while (thisMarker < s1Length && thatMarker < s2Length)
            {
                String thisChunk = getChunk(s1, s1Length, thisMarker);
                thisMarker += thisChunk.length();

                String thatChunk = getChunk(s2, s2Length, thatMarker);
                thatMarker += thatChunk.length();

                /** If both chunks contain numeric characters, sort them numerically **/

                int result = 0;
                if (isDigit(thisChunk.charAt(0)) && isDigit(thatChunk.charAt(0)))
                {
                    // Simple chunk comparison by length.
                    int thisChunkLength = thisChunk.length();
                    result = thisChunkLength - thatChunk.length();
                    // If equal, the first different number counts
                    if (result == 0)
                    {
                        for (int i = 0; i < thisChunkLength; i++)
                        {
                            result = thisChunk.charAt(i) - thatChunk.charAt(i);
                            if (result != 0)
                            {
                                return result;
                            }
                        }
                    }
                } else
                {
                    result = thisChunk.compareTo(thatChunk);
                }

                if (result != 0)
                    return result;
            }

            return s1Length - s2Length;
        }
    }

    class CustomAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return longclickitems.length;
        }

        @Override
        public Object getItem(int i) {
            return longclickitems[i];
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
            txt.setText(longclickitems[i]);
            if(longclickitems[i].equals("details")){
                img.setImageResource(R.drawable.ic_info);
            }else if(longclickitems[i].equals("delete")){
                img.setImageResource(R.drawable.ic_delete);
            }if(longclickitems[i].equals("share")){
                img.setImageResource(R.drawable.ic_share);
            }else if(longclickitems[i].equals("rename")){
                img.setImageResource(R.drawable.ic_rename);
            }
            return myView;
        }
    }

    class CustomAdapter2 extends BaseAdapter{

        @Override
        public int getCount() {
            return sortitems.length;
        }

        @Override
        public Object getItem(int i) {
            return sortitems[i];
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View myView = getLayoutInflater().inflate(R.layout.option_layout,null);
            TextView txt = myView.findViewById(R.id.txtOption);
            txt.setText(sortitems[i]);
            ImageView img = myView.findViewById(R.id.imgOption);
            if(sortitems[i].equals("name")){
                img.setImageResource(R.drawable.ic_name);
            }else if(sortitems[i].equals("date")){
                img.setImageResource(R.drawable.ic_date);
            }if(sortitems[i].equals("size")){
                img.setImageResource(R.drawable.ic_size);
            }
            return myView;
        }

    }

    List<File> fileList;
    FileAddapter fileAddapter;
    File storage;
    String data = "it is null now";
    String[] longclickitems = {"details","rename","delete","share"};
    String[] sortitems = {"size","date","name"};


    View view;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Nullable


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_internal,container,false);

        TextView tv_pathHolder = view.findViewById(R.id.tv_pathHolder);

        

        
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

        ImageView sort = view.findViewById(R.id.img_sort);
        sort.setOnClickListener(view -> {
            final Dialog optionDialog = new Dialog(getContext());
            optionDialog.setContentView(R.layout.option_dialog);
            optionDialog.setTitle("select Option :");
            ListView options = (ListView) optionDialog.findViewById(R.id.list);
            CustomAdapter2 customAdapter = new CustomAdapter2();
            options.setAdapter(customAdapter);
            optionDialog.show();
            options.setOnItemClickListener((adapterView, view2, i, l) -> {
                String sortType ="s";
                String selectedItem = adapterView.getItemAtPosition(i).toString();
                switch (selectedItem){
                    case "name":
                        sortType="n";
                        break;
                    case "date":
                        sortType="d";
                        break;
                    case "size" :
                        sortType="s";
                        break;

                }
                optionDialog.cancel();
                try {
                    runtimePermission(sortType);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        });

        tv_pathHolder.setText(storage.getAbsolutePath());
        try {
            runtimePermission("s");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void runtimePermission(String sortType) throws IOException {


        if(checkpermission())
        {

            displayFiles(sortType);
        }else{
            requestpermission();
            displayFiles(sortType);
        }
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

    @RequiresApi(api = Build.VERSION_CODES.O)

    public ArrayList<File> findFiles(File file , String sortType) throws IOException {
        ArrayList<File> arrayList = new ArrayList<>();
        File[] files = file.listFiles();

        assert files != null;

        for( File singleFile : files)
        {
                arrayList.add(singleFile);
        }
        if(sortType.contains("d"))
        {
            arrayList.sort(Comparator.comparing(File::lastModified).reversed());
        }else if(sortType.contains("s"))
        {
            int len = arrayList.size();
            for(int i=0;i<len;i++)
            {
                for(int j=i;j<len;j++)
                {
                    File f1,f2;
                    f1 = arrayList.get(i);
                    f2 = arrayList.get(j);
                    if(Files.size(f1.toPath())>Files.size(f2.toPath()))
                    {
                        File f3 = f1;
                        arrayList.set(i,f2) ;
                        arrayList.set(j,f3);
                    }
                }
            }
        }else if(sortType.contains("n"))
        {
            arrayList.sort(new AlphanumFileComparator());
        }
        return arrayList;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void displayFiles(String sortType) throws IOException {
        RecyclerView recyclerView = view.findViewById(R.id.recycler_internal);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),1));
        fileList = new ArrayList<>(findFiles(storage,sortType));
        fileAddapter = new FileAddapter(getContext(), fileList, this);
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
                    deletedialog.setPositiveButton("yes", (dialogInterface, i14) -> {
                        file.delete();
                        fileList.remove(position);
                        fileAddapter.notifyDataSetChanged();
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


}
