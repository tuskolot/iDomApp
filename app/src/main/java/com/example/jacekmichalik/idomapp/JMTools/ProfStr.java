package com.example.jacekmichalik.idomapp.JMTools;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

/*
    Przykład użycia:

    MessageBox.show( this, "Title","Message",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // on click event handler
                }
            });
 */
public class ProfStr{

    public static void ask(Context context, String title, String message , DialogInterface.OnClickListener onOKClick) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setCancelable(false);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", onOKClick);
        builder.setNegativeButton("Anuluj", null);
        try{
            builder.create().show();
        }
        catch (Exception e){
            Log.d("MESSAGEBOX",e.toString());
        }
    }

    public static void show(Context context, String title, String message ) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

//        builder.setCancelable(false);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", null);
        try{
            builder.create().show();
        }
        catch (Exception e){
            Log.d("MESSAGEBOX",e.toString());
        }
    }

    public static int  random_my(int maxi){
        return (int)Math.round(Math.random()*maxi);
    }

}
