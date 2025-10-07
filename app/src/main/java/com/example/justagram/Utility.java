package com.example.justagram;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

public class Utility {
    public static void showMessageBox(String content,Context t) {
        
        // 1. Instantiate an AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(t);

        // 2. Chain methods to set the dialog characteristics
        builder.setTitle("Justagram");
        builder.setMessage(content);

        // Add a single button
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Code to run when OK is clicked.
                // For this example, we just close the dialog.
                dialog.dismiss();
            }
        });

        // 3. Create the AlertDialog
        AlertDialog dialog = builder.create();

        // 4. Show the dialog
        dialog.show();
    }

}
