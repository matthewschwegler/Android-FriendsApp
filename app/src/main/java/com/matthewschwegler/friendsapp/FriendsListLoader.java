package com.matthewschwegler.friendsapp;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matt Schwegler on 2/24/2016.
 *
 * Can be used as boiler plate for loader
 */
public class FriendsListLoader extends AsyncTaskLoader<List<Friend>> {
    private static final String LOG_TAG = FriendsListLoader.class.getSimpleName();
    private List<Friend> mFriends;
    private ContentResolver mContentResolver;
    private Cursor mCursor;

    public FriendsListLoader(Context context, Uri uri, ContentResolver contentResolver) {
        super(context);
        mContentResolver = contentResolver;
    }

    @Override
    public List<Friend> loadInBackground() {
        String[] projection = { BaseColumns._ID,
                FriendsContract.FriendsColumns.FRIENDS_NAME,
                FriendsContract.FriendsColumns.FRIENDS_PHONE,
                FriendsContract.FriendsColumns.FRIENDS_EMAIL };
        List<Friend> entries = new ArrayList<Friend>();

        mCursor = mContentResolver.query(FriendsContract.URI_TABLE, projection, null, null, null);
        if(mCursor != null) {
            if(mCursor.moveToFirst()) {
                do {
                    int _id = mCursor.getInt(mCursor.getColumnIndex(BaseColumns._ID));
                    String name = mCursor.getString(
                            mCursor.getColumnIndex(FriendsContract.FriendsColumns.FRIENDS_NAME));
                    String phone = mCursor.getString(
                            mCursor.getColumnIndex(FriendsContract.FriendsColumns.FRIENDS_PHONE));
                    String email = mCursor.getString(
                            mCursor.getColumnIndex(FriendsContract.FriendsColumns.FRIENDS_EMAIL));
                    Friend friend = new Friend(_id, name, phone, email);
                    entries.add(friend);
                } while(mCursor.moveToNext());
            }
        }
        return entries;
    }

    @Override
    public void deliverResult(List<Friend> friends) {
        if(isReset()) {
            if(friends != null) {
                mCursor.close();
            }
        }

        List<Friend> oldFriendList = mFriends;
        if(mFriends == null || mFriends.size() == 0) {
            Log.d(LOG_TAG, "+++++++++ No Data returned");
        }
        mFriends = friends;
        if(isStarted()) {
            super.deliverResult(friends);
        }
        if(oldFriendList != null && oldFriendList != friends) {
            mCursor.close();
        }
    }

    @Override
    protected void onStartLoading() {
        if(mFriends != null) {
            deliverResult(mFriends);
        }

        if(takeContentChanged() | mFriends == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();
        if(mCursor != null) {
            mCursor.close();
        }

        mFriends = null;
    }

    @Override
    public void onCanceled(List<Friend> friends) {
        super.onCanceled(friends);
        if(mCursor != null) {
            mCursor.close();
        }
    }

    @Override
    public void forceLoad() {
        super.forceLoad();
    }
}
