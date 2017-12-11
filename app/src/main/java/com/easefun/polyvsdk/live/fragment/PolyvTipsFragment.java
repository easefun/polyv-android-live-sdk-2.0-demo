package com.easefun.polyvsdk.live.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;

import com.easefun.polyvsdk.live.R;

import java.lang.reflect.Field;

public class PolyvTipsFragment extends DialogFragment {
    private String message;
    private DialogInterface.OnClickListener posListener;

    public void show(FragmentManager manager, String message, DialogInterface.OnClickListener posListener) {
        this.message = message;
        this.posListener = posListener;
        setCancelable(false);
        try {
            Field mDismissed = DialogFragment.class.getDeclaredField("mDismissed");
            mDismissed.setAccessible(true);
            mDismissed.set(this, false);
            Field mShownByMe = DialogFragment.class.getDeclaredField("mShownByMe");
            mShownByMe.setAccessible(true);
            mShownByMe.set(this, true);
            FragmentTransaction ft = manager.beginTransaction();
            ft.add(this, "tipsFragment");
            ft.commitAllowingStateLoss();
        } catch (Exception e) {
            try {
                show(manager, "tipsFragment");
            } catch (Exception e1) {
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).setTitle("提示")
                .setMessage(message)
                .setPositiveButton("进入直播", posListener)
                .show();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.center_view_color_blue));
        return alertDialog;
    }
}
