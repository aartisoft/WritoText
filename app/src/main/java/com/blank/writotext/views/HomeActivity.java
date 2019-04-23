package com.blank.writotext.views;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.blank.writotext.R;
import com.blank.writotext.utils.FileContract;
import com.blank.writotext.utils.FileAdapter;
import com.blank.writotext.utils.SQLiteDatabaseHandler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.os.Environment.getExternalStoragePublicDirectory;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FileAdapter fileAdapter;

    private SQLiteDatabase db;
    private FloatingActionButton fab, fab_cam, fab_gal ;
    private Animation fabOpen, fabClose, rotateForward, rotateBackward ;
    boolean isOpen = false ;
    private TextView emptyView;
    private String pathToFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},3);

        addDirectory();
        SQLiteDatabaseHandler dbHelper = new SQLiteDatabaseHandler(this);
        db = dbHelper.getWritableDatabase();

        recyclerView = findViewById(R.id.fileRV);
        emptyView = findViewById(R.id.empty_view);


        visibility();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        fileAdapter = new FileAdapter(this, getAllItems());
        recyclerView.setAdapter(fileAdapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                removeItem((ArrayList<String>)viewHolder.itemView.getTag());
                Toast.makeText(HomeActivity.this,"Deleted",Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(recyclerView);


        fab = findViewById(R.id.fab);
        fab_cam = findViewById(R.id.fab2);
        fab_gal = findViewById(R.id.fab1);

        fabOpen = AnimationUtils.loadAnimation(this, R.anim.fab_open) ;
        fabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close) ;

        rotateForward = AnimationUtils.loadAnimation(this, R.anim.rotate_for) ;
        rotateBackward = AnimationUtils.loadAnimation(this, R.anim.rotate_back) ;

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateFab();
            }
        });

        fab_cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateFab();
                dispatchPictureTakerAction();
            }
        });

        fab_gal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateFab();
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, 2);
            }
        });
    }

    public void visibility(){
        String count = "SELECT count(*) FROM Files";
        Cursor mcursor = db.rawQuery(count, null);
        mcursor.moveToFirst();
        int icount = mcursor.getInt(0);
        if(icount>0) {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
        else {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }
    }
    public void addDirectory(){

        File direct = new File(Environment.getExternalStorageDirectory() + "/WritoText");
        if(!direct.exists())
        {
            if(direct.mkdir()); //directory is created;

        }

    }

    private void removeItem(ArrayList<String> temp) {
        db.delete(FileContract.FileEntry.TABLE_NAME,
                FileContract.FileEntry._ID + "=" + Long.parseLong(temp.get(0)), null);
        fileAdapter.swapCursor(getAllItems());
        visibility();
        File file = new File(Environment.getExternalStorageDirectory() + "/WritoText", temp.get(1));
        file.delete();
    }

    private Cursor getAllItems() {
        return db.query(
                FileContract.FileEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                FileContract.FileEntry.COLUMN_TIMESTAMP + " DESC"
        );
    }

    @Override
    public void onRestart() {
        super.onRestart();
        visibility();
        fileAdapter.swapCursor(getAllItems());
        fileAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            if (requestCode == 1){

                Intent intent = new Intent(HomeActivity.this, CameraActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("pic", pathToFile);
                intent.putExtras(bundle);
                startActivity(intent);
                fileAdapter.swapCursor(getAllItems());
            }
            else if (requestCode == 2){
                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();

                Intent intent = new Intent(this, CameraActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("pic", picturePath);
                intent.putExtras(bundle);
                startActivity(intent);
                fileAdapter.swapCursor(getAllItems());
            }
        }

    }

    private void dispatchPictureTakerAction(){
        Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(takePic.resolveActivity(getPackageManager())!=null){
            File photoFile = null;
            photoFile = createPhotoFile();

            if (photoFile!=null){
                pathToFile = photoFile.getAbsolutePath();
                Uri photoURI = FileProvider.getUriForFile(HomeActivity.this, "com.blank.writotext.fileprovider", photoFile);
                takePic.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePic, 1);
            }

        }
    }

    private File createPhotoFile(){
        String name = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile(name, ".jpg", storageDir);
        } catch (IOException e) {
            Log.d("mylog", "Excep : " + e.toString());
        }
        return image;
    }

    private void animateFab() {

        if(isOpen) {

            fab.startAnimation(rotateForward);
            fab_cam.startAnimation(fabClose);
            fab_gal.startAnimation(fabClose);
            fab_cam.setClickable(false);
            fab_gal.setClickable(false);
            isOpen = false;

        }else {

            fab.startAnimation(rotateBackward);
            fab_cam.startAnimation(fabOpen);
            fab_gal.startAnimation(fabOpen);
            fab_cam.setClickable(true);
            fab_gal.setClickable(true);
            isOpen = true ;

        }
    }
}
