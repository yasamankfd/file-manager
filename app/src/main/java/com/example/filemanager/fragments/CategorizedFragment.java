package com.example.filemanager.fragments;
import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.filemanager.AsyncFileTypes;
import com.example.filemanager.FileAddapter;
import com.example.filemanager.FileOpener;
import com.example.filemanager.R;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class CategorizedFragment extends Fragment  {
    String[] sortitems = {"size","date","name"};
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
    String type;
    File path;
    EditText searchbar ;
    View view;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Nullable


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_categorized,container,false);

        Bundle b = this.getArguments();
        path = Environment.getExternalStorageDirectory();
        type = b.getString("fileType");
        searchbar = view.findViewById(R.id.search22);
        ImageView search = view.findViewById(R.id.img_search3);
        search.setOnClickListener(view1 -> {
            Animation animFadein = AnimationUtils.loadAnimation(getContext(),R.anim.fade_in);
            search.startAnimation(animFadein);
            searchbar.setEnabled(true);

        });
        searchbar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ArrayList<File> searched = new ArrayList<>();
                PopupMenu popupMenu = new PopupMenu(getContext(),searchbar);
                popupMenu.getMenuInflater().inflate(R.menu.searchmenu,popupMenu.getMenu());
                for(File f : fileList)
                {
                    if(f.getName().startsWith(String.valueOf(charSequence)) && !charSequence.equals(""))
                    {
                        searched.add(f);
                        popupMenu.getMenu().add(f.getName());
                    }
                }
                if(searched.size()>0)
                { popupMenu.show(); }else;// Toast.makeText(getContext(), "Not found", Toast.LENGTH_LONG).show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        try {
                            FileOpener.openfile(getContext(),searched.get(menuItem.getOrder()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return true;
                    }
                });
            }
            @Override
            public void afterTextChanged(Editable editable) { }
        });
        ImageView sort = view.findViewById(R.id.img_sort3);
        sort.setOnClickListener(view -> {
            Animation animFadein = AnimationUtils.loadAnimation(getContext(),R.anim.fade_in);
            sort.startAnimation(animFadein);
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
    private void displayFiles(String sortType) throws IOException {
        RecyclerView recyclerView = view.findViewById(R.id.recycler_internal);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),1));
        fileList = new ArrayList<>();
        ArrayList<File> arrayList = new ArrayList<>();

        ArrayList<File> a = new ArrayList<>();
        File pathForFiles ;
        if(type.equals("download")){
            pathForFiles = new File(Environment.DIRECTORY_DOWNLOADS);
        }else pathForFiles = Environment.getExternalStorageDirectory();
        AsyncFileTypes asyncFiles = new AsyncFileTypes(pathForFiles,type);
        asyncFiles.execute();

        try {
            a = asyncFiles.get();
            asyncFiles.cancel(true);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        fileList.addAll(a);
        for( File singleFile : fileList)
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
            arrayList.sort(new InternalFragment.AlphanumFileComparator());
        }
        fileAddapter = new FileAddapter(getContext(), fileList);
        recyclerView.setAdapter(fileAddapter);
    }

//    @Override
//    public void onFileClicked(File file) {
//        if(file.isDirectory()){
//            Bundle bundle = new Bundle();
//            bundle.putString("path",file.getAbsolutePath());
//            CategorizedFragment internalFragment = new CategorizedFragment();
//            internalFragment.setArguments(bundle);
//            assert getFragmentManager() != null;
//            getFragmentManager().beginTransaction().replace(R.id.fragment_container,internalFragment).addToBackStack(null).commit();
//        }else {
//
//            try {
//                FileOpener.openfile(getContext(),file);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

//    @Override
//    public void onFileLongClicked(File file,int position) {
//
//        final Dialog optionDialog = new Dialog(getContext());
//        optionDialog.setContentView(R.layout.option_dialog);
//        optionDialog.setTitle("select Option :");
//        ListView options = (ListView) optionDialog.findViewById(R.id.list);
//        CustomAdapter customAdapter = new CustomAdapter();
//        options.setAdapter(customAdapter);
//        optionDialog.show();
//        options.setOnItemClickListener((adapterView, view, i, l) -> {
//            String selectedItem = adapterView.getItemAtPosition(i).toString();
//            switch (selectedItem){
//                case "details":
//                    AlertDialog.Builder detailDialog = new AlertDialog.Builder(getContext());
//                    detailDialog.setTitle("details");
//                    final TextView details = new TextView(getContext());
//                    detailDialog.setView(details);
//                    Date lastModified = new Date(file.lastModified());
//                    SimpleDateFormat formatter = new SimpleDateFormat("dd/mm/yyyy HH:mm:ss");
//                    String formattedDate = formatter.format(lastModified);
//                    details.setText("file name : "+file.getName()+"\n"+
//                            "size : "+ Formatter.formatShortFileSize(getContext(),file.length())+"\n"+
//                            "path : "+file.getAbsolutePath()+"\n"+
//                            "last modified : "+formattedDate);
//                    detailDialog.setPositiveButton("ok", (dialogInterface, i13) -> optionDialog.cancel());
//
//                    AlertDialog alertdialog = detailDialog.create();
//                    alertdialog.show();
//                    break;
//                case "rename":
//                   AlertDialog.Builder renameDialog = new AlertDialog.Builder(getContext());
//                   renameDialog.setTitle("rename file : ");
//                   final EditText name = new EditText(getContext());
//                   renameDialog.setView(name);
//                   renameDialog.setPositiveButton("ok", (dialogInterface, i1) -> {
//                       String new_name = name.getEditableText().toString();
//                       String extension = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("."));
//                       File current = new File(file.getAbsolutePath());
//                       File destination = new File(file.getAbsolutePath().replace(file.getName(),new_name+extension));
//                       if(current.renameTo(destination)){
//                           fileList.set(position,destination);
//                            fileAddapter.notifyItemChanged(position);
//                           Toast.makeText(getContext(),"renamed!",Toast.LENGTH_LONG).show();
//                       }else Toast.makeText(getContext(),"can not be renamed!",Toast.LENGTH_LONG).show();
//                   });
//                    renameDialog.setNegativeButton("cancel", (dialogInterface, i12) -> optionDialog.cancel());
//                    AlertDialog alertDialog_rename = renameDialog.create();
//                    alertDialog_rename.show();
//                    break;
//                case "delete":
//                    AlertDialog.Builder deletedialog =  new AlertDialog.Builder(getContext());
//                    deletedialog.setTitle("delete "+file.getName()+" ?");
//                    deletedialog.setPositiveButton("yes", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            file.delete();
//                            fileList.remove(position);
//                            fileAddapter.notifyDataSetChanged();
//                        }
//                    });
//                    deletedialog.setNegativeButton("no", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            optionDialog.cancel();
//
//                        }
//                    });
//                    AlertDialog alertDialog_delete = deletedialog.create();
//                    alertDialog_delete.show();
//                    break;
//                case "share" :
//                    if(!file.isDirectory())
//                    {
//                        String nam = file.getName();
//                        Intent share = new Intent();
//                        share.setAction(Intent.ACTION_SEND);
//                        share.setType("image/jpeg");
//
//                        Uri p = FileProvider.getUriForFile(
//                                requireContext(),
//                                BuildConfig.APPLICATION_ID + ".provider",
//                                file);
//
//                        share.putExtra(Intent.EXTRA_STREAM,p);
//                        startActivity(Intent.createChooser(share,"share "+ nam));
//                    }else Toast.makeText(getContext(),"directory cant be shared !",Toast.LENGTH_SHORT).show();
//
//                    break;
//
//            }
//        });
//    }

//    class CustomAdapter extends BaseAdapter{
//
//        @Override
//        public int getCount() {
//            return items.length;
//        }
//
//        @Override
//        public Object getItem(int i) {
//            return items[i];
//        }
//
//        @Override
//        public long getItemId(int i) {
//            return 0;
//        }
//
//        @Override
//        public View getView(int i, View view, ViewGroup viewGroup) {
//            View myView = getLayoutInflater().inflate(R.layout.option_layout,null);
//            ImageView img = myView.findViewById(R.id.imgOption);
//            TextView txt = myView.findViewById(R.id.txtOption);
//            txt.setText(items[i]);
//            if(items[i].equals("details")){
//                img.setImageResource(R.drawable.ic_info);
//            }else if(items[i].equals("delete")){
//                img.setImageResource(R.drawable.ic_delete);
//            }if(items[i].equals("share")){
//                img.setImageResource(R.drawable.ic_share);
//            }else if(items[i].equals("rename")){
//                img.setImageResource(R.drawable.ic_rename);
//            }
//            return myView;
//        }
//    }

}
