package com.blank.writotext.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.File;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.blank.writotext.R;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

    private Context context;
    private Cursor cursor;

    public FileAdapter(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFile;
        TextView textFile,textFile2;
        CardView cv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFile = itemView.findViewById(R.id.fileIMG);
            textFile = itemView.findViewById(R.id.fileNAME);
            textFile2 = itemView.findViewById(R.id.filePATH);
            cv = itemView.findViewById(R.id.fileCV);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recents_main, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        if (!cursor.moveToPosition(position)) {
            return;
        }

        final String name = cursor.getString(cursor.getColumnIndex(FileContract.FileEntry.COLUMN_NAME));
        int img = cursor.getInt(cursor.getColumnIndex(FileContract.FileEntry.COLUMN_IMG));
        final String path = cursor.getString(cursor.getColumnIndex(FileContract.FileEntry.COLUMN_PATH));
        long id = cursor.getLong(cursor.getColumnIndex(FileContract.FileEntry._ID));
        String date = cursor.getString(cursor.getColumnIndex(FileContract.FileEntry.COLUMN_TIMESTAMP));
        ArrayList<String> temp = new ArrayList<>();
        temp.add(Long.toString(id));
        temp.add(name);
        holder.textFile2.setText(date);
        holder.textFile.setText(name);
        holder.imgFile.setImageResource(img);
        holder.itemView.setTag(temp);
        holder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewfiles(path+name);
            }

        });
        holder.cv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent intentShareFile = new Intent(Intent.ACTION_SEND);
                File fileWithinMyDir = new File(path+name);

                if(fileWithinMyDir.exists()) {
                    Uri uri = FileProvider.getUriForFile(context, "com.blank.writotext.fileprovider", fileWithinMyDir);
                    intentShareFile.setType("application/pdf");
                    intentShareFile.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intentShareFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intentShareFile.putExtra(Intent.EXTRA_STREAM, uri);

                    intentShareFile.putExtra(Intent.EXTRA_SUBJECT,
                            "Sharing File...");
                    intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing File...");

                    context.startActivity(Intent.createChooser(intentShareFile, "Share File"));
                }
                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        if (cursor != null) {
            cursor.close();
        }

        cursor = newCursor;

        if (newCursor != null) {
            notifyDataSetChanged();
        }
    }

    private void viewfiles(String filePath) {

        File file=new File(filePath);
        Uri uri = FileProvider.getUriForFile(context, "com.blank.writotext.fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(intent);
    }


}