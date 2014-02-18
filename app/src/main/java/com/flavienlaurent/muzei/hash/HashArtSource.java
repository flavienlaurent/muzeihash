package com.flavienlaurent.muzei.hash;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import retrofit.ErrorHandler;
import retrofit.RestAdapter;
import retrofit.RetrofitError;

public class HashArtSource extends RemoteMuzeiArtSource {

    private static final String TAG = "HashArtSource";
    private static final String SOURCE_NAME = "HashArtSource";

    private GooglePlusService mGooglePlusService;
    private List<GooglePlusService.Item> mItems;

    public HashArtSource() {
        super(SOURCE_NAME);
    }

    private int getRotateTimeMillis() {
        return PreferenceHelper.getConfigFreq(this);
    }

    private boolean isConnectedAsPreferred() {
        if(PreferenceHelper.getConfigConnection(this) == PreferenceHelper.CONNECTION_WIFI) {
            return Utils.isWifiConnected(this);
        }
        return true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setUserCommands(BUILTIN_COMMAND_ID_NEXT_ARTWORK);
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(GooglePlusService.API_URL)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setErrorHandler(new ErrorHandler() {
                    @Override
                    public Throwable handleError(RetrofitError retrofitError) {
                        int statusCode = retrofitError.getResponse().getStatus();
                        if (retrofitError.isNetworkError() || (500 <= statusCode && statusCode < 600)) {
                            return new RetryException();
                        }
                        scheduleUpdate(System.currentTimeMillis() + getRotateTimeMillis());
                        return retrofitError;
                    }
                })
                .build();
        mGooglePlusService = restAdapter.create(GooglePlusService.class);
        mItems = new ArrayList<GooglePlusService.Item>();
    }

    @Override
    protected void onTryUpdate(int reason) throws RetryException {
        if (!isConnectedAsPreferred()) {
            scheduleUpdate(System.currentTimeMillis() + getRotateTimeMillis());
            return;
        }

        String currentToken = (getCurrentArtwork() != null) ? getCurrentArtwork().getToken() : null;

        List<String> hashtags = PreferenceHelper.tagsFromPref(getApplicationContext());
        if(hashtags.isEmpty()) {
            Log.w(TAG, "no hashtags.");
            scheduleUpdate(System.currentTimeMillis() + getRotateTimeMillis());
            return;
        }

        GooglePlusService.Result result = null;
        if(reason != UPDATE_REASON_USER_NEXT || mItems.isEmpty()) {
            mItems.clear();
            for(String hashtag : hashtags) {
                try {
                    result = mGooglePlusService.fetch(hashtag);
                    if(result == null) {
                        Log.w(TAG, "pb while fetching from google+, retry");
                        throw new RetryException();
                    }

                    mItems.addAll(result.items);
                } catch (RetrofitError e) {
                    Log.e(TAG, "error while fetching from google+", e);
                }
            }
            filterItems();
        }

        Log.i(TAG, "There are " + mItems.size() + " available items.");

        if (mItems.isEmpty()) {
            Log.w(TAG, "no photos returned from API.");
            scheduleUpdate(System.currentTimeMillis() + getRotateTimeMillis());
            return;
        }

        Random random = new Random();
        GooglePlusService.Item item;
        GooglePlusService.Attachment attachment;
        String token;
        while (true) {
            item = mItems.get(random.nextInt(mItems.size()));
            attachment = item.object.attachments.get(0);
            token = attachment.id;
            if (mItems.size() <= 1 || !TextUtils.equals(token, currentToken)) {
                break;
            }
        }

        String author = item.object.actor == null ? null : item.object.actor.displayName;
        String displayName = attachment.displayName;
        String viewIntentUrl = item.url;

        //author
        if(TextUtils.isEmpty(author)) {
            author = item.actor.displayName;
        }

        //displayname
        if(TextUtils.isEmpty(displayName)) {
            displayName = item.object.title;
        }

        //viewIntentUrl
        if(TextUtils.isEmpty(viewIntentUrl)) {
            viewIntentUrl = attachment.fullImage.url;
        }

        //finally
        if(TextUtils.isEmpty(displayName)) {
            displayName = "...";
        }
        if(TextUtils.isEmpty(author)) {
            author = "...";
        }

        Log.i(TAG, "artwork: " + displayName + ",fullImage:" + attachment.fullImage.url + ",viewUrl:" + viewIntentUrl);

        publishArtwork(new Artwork.Builder()
                .title(displayName)
                .byline(author)
                .imageUri(Uri.parse(attachment.fullImage.url))
                .token(attachment.id)
                .viewIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(viewIntentUrl)))
                .build());

        scheduleUpdate(System.currentTimeMillis() + getRotateTimeMillis());
    }

    private void filterItems() {
        Iterator<GooglePlusService.Item> iterator = mItems.iterator();
        while (iterator.hasNext()) {
            GooglePlusService.Item item = iterator.next();
            if(! isGoodCandidate(item)) {
                iterator.remove();
            }
        }
    }

    private boolean isGoodCandidate(GooglePlusService.Item item) {
        if(item.object == null) {
            return false;
        }
        List<GooglePlusService.Attachment> attachments = item.object.attachments;
        if(attachments == null || attachments.isEmpty()) {
            return false;
        }
        GooglePlusService.Attachment attachment = attachments.get(0);
        if(attachment.fullImage == null || TextUtils.isEmpty(attachment.fullImage.url)) {
            return false;
        }
        return true;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Don't remove this, calls MuzeiArtSource implementation
        super.onHandleIntent(intent);

        if (intent.getExtras().containsKey("configFreq")) {
            // getRotateTimeMillis already have the correct updated value
            scheduleUpdate(System.currentTimeMillis() + getRotateTimeMillis());
        }
    }
}