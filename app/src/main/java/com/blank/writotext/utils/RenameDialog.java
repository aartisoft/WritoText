package com.blank.writotext.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.blank.writotext.R;

import java.util.ArrayList;

public class RenameDialog extends DialogFragment {

    private EditText editText;
    private RenameDialogListener listener;
    Bitmap bitmap;
    ArrayList<String> list = new ArrayList<>();

    public RenameDialog(Bitmap bitmap, ArrayList<String> list){
        this.bitmap=bitmap;
        this.list=list;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_layout, null);

        builder.setView(view)
                .setTitle("Rename")
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String fname = editText.getText().toString();
                        if(bitmap==null){
                            listener.applyTextocr(list,fname);
                        }else listener.applyTexts(bitmap,fname);

                    }
                });

        editText = view.findViewById(R.id.editName);
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (RenameDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement RenameDialogListener");
        }
    }

    public interface RenameDialogListener {
        void applyTexts(Bitmap bitmap,String fname);
        void applyTextocr(ArrayList<String> list,String fname);
    }
}

