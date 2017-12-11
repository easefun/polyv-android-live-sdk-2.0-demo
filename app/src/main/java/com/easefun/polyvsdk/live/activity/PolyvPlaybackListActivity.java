package com.easefun.polyvsdk.live.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.easefun.polyvsdk.live.R;
import com.easefun.polyvsdk.live.fragment.PolyvPlaybackListFragment;

public class PolyvPlaybackListActivity extends FragmentActivity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null)
            savedInstanceState.putParcelable("android:support:fragments", null);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.polyv_activity_playbacklist);

        PolyvPlaybackListFragment playbackListFragment = new PolyvPlaybackListFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fl_playbacklist, playbackListFragment, "playbackListFragment").commit();
    }

    public static Intent newIntent(Context context, String userId, String channelId, String chatUserId, String nickName) {
        Intent intent = new Intent(context, PolyvPlaybackListActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("channelId", channelId);
        intent.putExtra("chatUserId", chatUserId);
        intent.putExtra("nickName", nickName);
        return intent;
    }
}
