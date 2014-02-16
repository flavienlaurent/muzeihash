package com.flavienlaurent.muzei.hash;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.doomonafireball.betterpickers.hmspicker.HmsPickerBuilder;
import com.doomonafireball.betterpickers.hmspicker.HmsPickerDialogFragment;
import com.haarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.haarman.listviewanimations.itemmanipulation.SwipeDismissAdapter;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingsActivity extends FragmentActivity implements OnDismissCallback, HmsPickerDialogFragment.HmsPickerDialogHandler {

    private static final String TAG = "SettingsActivity";

    private ListView mList;
    private EditText mHashTag;
    private View mEmpty;
    private TextView mConfigConnection;
    private TextView mConfigFreq;

    private HashTagAdapter mHashTagAdapter;
    private String mLastHashTag;

    private static final Pattern pattern = Pattern.compile("\\s");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setupActionBar();

        mList = (ListView) findViewById(R.id.list);
        mHashTag = (EditText) findViewById(R.id.hashtag);
        mEmpty = findViewById(R.id.empty);
        mConfigConnection = (TextView) findViewById(R.id.config_connection);
        mConfigFreq = (TextView) findViewById(R.id.config_freq);

        setupList();
        setupHashTagEditText();
        setupConfig();

        mEmpty.setOnClickListener(mOnEmptyClickListener);
    }

    private void setupConfig() {
        mConfigFreq.setOnClickListener(mOnConfigFreqClickListener);
        mConfigConnection.setOnClickListener(mOnConfigConnectionClickListener);

        updateConfigFreq();
        updateConfigConnection();
    }

    private void updateConfigFreq() {
        mConfigFreq.setText(getString(R.string.config_every, Utils.convertDurationtoString(PreferenceHelper.getConfigFreq(this))));
    }

    private void updateConfigConnection() {
        switch (PreferenceHelper.getConfigConnection(this)) {
            case PreferenceHelper.CONNECTION_ALL:
                mConfigConnection.setText(R.string.config_connection_all);
                mConfigConnection.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_config_connection_all, 0, 0, 0);
                break;
            case PreferenceHelper.CONNECTION_WIFI:
                mConfigConnection.setText(R.string.config_connection_wifi);
                mConfigConnection.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_config_connection_wifi, 0, 0, 0);
                break;
        }
    }

    private View.OnClickListener mOnConfigFreqClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            HmsPickerBuilder hpb = new HmsPickerBuilder()
                    .setFragmentManager(getSupportFragmentManager())
                    .setStyleResId(R.style.BetterPickersDialogFragment);
            hpb.show();
        }
    };

    @Override
    public void onDialogHmsSet(int reference, int hours, int minutes, int seconds) {
        int duration = hours * 3600000 + minutes * 60000 + seconds * 1000;
        PreferenceHelper.setConfigFreq(this, duration);
        updateConfigFreq();
    }

    private View.OnClickListener mOnConfigConnectionClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int connection = PreferenceHelper.getConfigConnection(SettingsActivity.this);
            if(connection == PreferenceHelper.CONNECTION_WIFI) {
                mConfigConnection.setText(R.string.config_connection_all);
                PreferenceHelper.setConfigConnection(SettingsActivity.this, PreferenceHelper.CONNECTION_ALL);
            } else if(connection == PreferenceHelper.CONNECTION_ALL) {
                mConfigConnection.setText(R.string.config_connection_wifi);
                PreferenceHelper.setConfigConnection(SettingsActivity.this, PreferenceHelper.CONNECTION_WIFI);
            }
            updateConfigConnection();
        }
    };

    private void setupList() {
        mHashTagAdapter = new HashTagAdapter();
        SwipeDismissAdapter swipeDismissAdapter = new SwipeDismissAdapter(mHashTagAdapter, this);
        swipeDismissAdapter.setAbsListView(mList);
        mList.setAdapter(swipeDismissAdapter);
        mList.setEmptyView(mEmpty);
        mList.setOnItemClickListener(mOnHashTagItemClickListener);
    }

    private AdapterView.OnItemClickListener mOnHashTagItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String hashtag = mHashTagAdapter.getItem(position);
            hashtag = hashtag.substring(1); //remove #
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/explore/" + hashtag)));
        }
    };

    private final View.OnClickListener mOnEmptyClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mHashTag.requestFocus()) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mHashTag, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    };

    private void setupActionBar() {
        final LayoutInflater inflater = getLayoutInflater();
        View actionBarView = inflater.inflate(R.layout.ab_activity_settings, null);
        actionBarView.findViewById(R.id.actionbar_done).setOnClickListener(mOnActionBarDoneClickListener);
        getActionBar().setCustomView(actionBarView);
    }

    private View.OnClickListener mOnActionBarDoneClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };

    private void setupHashTagEditText() {
        mHashTag.setImeActionLabel(getString(R.string.add), EditorInfo.IME_ACTION_DONE);
        mHashTag.setOnEditorActionListener(mOnEditorActionListener);
    }

    private TextView.OnEditorActionListener mOnEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addHashTag();
            }
            return false;
        }
    };

    private void addHashTag() {
        String hashtag = mHashTag.getText().toString();
        Matcher matcher = pattern.matcher(hashtag);

        if(!hashtag.startsWith("#")) {
            hashtag = "#" + hashtag;
        }

        if(TextUtils.isEmpty(hashtag) || matcher.find()) {
            Toast.makeText(SettingsActivity.this, "Bad hashtag format.", Toast.LENGTH_SHORT).show();
            return;
        }
        mLastHashTag = hashtag;
        mHashTagAdapter.add(hashtag);
        mHashTag.setText(null);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void animatedAdd(final ViewGroup view) {
        final TextView overlay = (TextView) view.findViewById(R.id.overlay);

        ViewGroup hostView = (ViewGroup) findViewById(android.R.id.content);
        final ViewGroupOverlay viewGroupOverlay = hostView.getOverlay();
        viewGroupOverlay.add(overlay);
        overlay.offsetTopAndBottom(mList.getTop());

        float width = overlay.getPaint().measureText(mLastHashTag);

        overlay.setPivotX(width);
        overlay.setPivotY(0.0f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(overlay, "scaleX", 1, 3f),
                ObjectAnimator.ofFloat(overlay, "scaleY", 1, 3f),
                ObjectAnimator.ofFloat(overlay, "alpha", 1, 0.0f)
        );
        set.start();
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                viewGroupOverlay.remove(overlay);
                view.addView(overlay);
            }
        });
    }

    @Override
    public void onDismiss(AbsListView absListView, int[] reverseSortedPositions) {
        for (int position : reverseSortedPositions) {
            mHashTagAdapter.remove(position);
        }
    }

    private class HashTagAdapter extends BaseAdapter {

        private List<String> mTags;

        private HashTagAdapter() {
            mTags = PreferenceHelper.tagsFromPref(SettingsActivity.this);
        }

        void add(String hashtag) {
            if(mTags.contains(hashtag)) {
                return;
            }
            mTags.add(0, hashtag);
            updateTagsInPref();
            notifyDataSetChanged();
        }

        void remove(int position) {
            mTags.remove(position);
            updateTagsInPref();
            notifyDataSetChanged();
        }

        void remove(String hashtag) {
            mTags.remove(hashtag);
            updateTagsInPref();
            notifyDataSetChanged();
        }

        private void updateTagsInPref() {
            PreferenceHelper.tagsToPref(SettingsActivity.this, mTags);
        }

        @Override
        public int getCount() {
            return mTags.size();
        }

        @Override
        public String getItem(int position) {
            return mTags.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.listitem_tag, parent, false);
            }

            TextView hashtag = (TextView) convertView.findViewById(R.id.hashtag);
            TextView overlay = (TextView) convertView.findViewById(R.id.overlay);

            final String tag = getItem(position);
            hashtag.setText(tag);
            if(overlay != null && tag.equals(mLastHashTag) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                overlay.setText(tag);
                animatedAdd((ViewGroup) convertView);
                mLastHashTag = null;
            }

            return convertView;
        }
    }
}
