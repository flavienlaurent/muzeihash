package com.flavienlaurent.muzei.hash;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingsActivity extends Activity {

    private static final String TAG = "SettingsActivity";

    private ListView mList;
    private ImageButton mAdd;
    private EditText mHashTag;
    private HashTagAdapter mHashTagAdapter;

    private static final Pattern pattern = Pattern.compile("\\s");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mList = (ListView) findViewById(R.id.list);
        mAdd = (ImageButton) findViewById(R.id.add);
        mHashTag = (EditText) findViewById(R.id.hashtag);

        mHashTagAdapter = new HashTagAdapter();
        mList.setAdapter(mHashTagAdapter);

        mAdd.setOnClickListener(mAddClickListener);

        mHashTag.setImeOptions(EditorInfo.IME_ACTION_DONE);

        mHashTag.setOnEditorActionListener(mOnEditorActionListener);
    }

    private TextView.OnEditorActionListener mOnEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addHashTag();
                return true;
            }
            return false;
        }
    };

    private View.OnClickListener mAddClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            addHashTag();
        }
    };

    private void addHashTag() {
        String hashtag = mHashTag.getText().toString();
        Matcher matcher = pattern.matcher(hashtag);

        if(TextUtils.isEmpty(hashtag) || !hashtag.startsWith("#") || matcher.find()) {
            Toast.makeText(SettingsActivity.this, "Bad hashtag format.", Toast.LENGTH_SHORT).show();
            return;
        }
        mHashTagAdapter.add(hashtag);
        mHashTag.setText(null);
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
            ImageButton delete = (ImageButton) convertView.findViewById(R.id.delete);

            final String tag = getItem(position);
            hashtag.setText(tag);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    remove(tag);
                }
            });

            return convertView;
        }
    }
}
