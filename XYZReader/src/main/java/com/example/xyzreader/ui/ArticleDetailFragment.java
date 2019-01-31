package com.example.xyzreader.ui;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ArticleDetailFragment";
    private static final String ARG_ITEM_ID = "item_id";
    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private final GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {

    }

    public static ArticleDetailFragment newInstance(long itemId) {

        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().containsKey(ARG_ITEM_ID)) {

            mItemId = getArguments().getLong(ARG_ITEM_ID);

        }
        setHasOptionsMenu(true);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        //noinspection deprecation
        getLoaderManager().initLoader(0, null, this);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        bindViews();

        return mRootView;

    }

    private Date parsePublishedDate() {

        try {

            String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            return dateFormat.parse(date);

        } catch (ParseException ex) {

            Log.e(TAG, ex.getMessage());
            Log.i(TAG, "passing today's date instead");
            return new Date();

        }

    }

    private void bindViews() {

        if (mRootView == null) {

            return;

        }

        TextView titleTextView = mRootView.findViewById(R.id.article_title);
        TextView byLineTextView = mRootView.findViewById(R.id.article_byline);
        RecyclerView bodyRecyclerView = mRootView.findViewById(R.id.body_RV_item);

        if (mCursor != null) {
            titleTextView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
            Date publishedDate = parsePublishedDate();
            if (!publishedDate.before(START_OF_EPOCH.getTime())) {

                byLineTextView.setText(getString(R.string.placeholder_text, DateUtils
                                .getRelativeTimeSpanString(publishedDate.getTime(),
                                        System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                                        DateUtils.FORMAT_ABBREV_ALL).toString(),
                        mCursor.getString(ArticleLoader.Query.AUTHOR)));

            } else {

                // showing the string for all dates below 1902
                byLineTextView.setText(
                        getString(R.string.placeholder_text, outputFormat.format(publishedDate),
                                mCursor.getString(ArticleLoader.Query.AUTHOR)));

            }
            String bodyText =
                    mCursor.getString(ArticleLoader.Query.BODY).replace("\r\n\r\n", "\n\n");
            bodyText = bodyText.replace("\r\n    ", "\n    ");
            bodyText = bodyText.replace("\r\n", " ");
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            bodyRecyclerView.setLayoutManager(layoutManager);
            String[] text = bodyText.split("\n\n");
            BodyAdapter adapter = new BodyAdapter(text);
            bodyRecyclerView.setAdapter(adapter);
            mRootView.findViewById(R.id.scrollview).setVisibility(View.VISIBLE);
            mRootView.findViewById(R.id.progress_bar).setVisibility(View.GONE);

        } else {

            mRootView.findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);

        }

    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);

    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> cursorLoader, Cursor cursor) {

        if (!isAdded()) {

            if (cursor != null) {

                cursor.close();

            }
            return;

        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {

            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;

        }

        bindViews();

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> cursorLoader) {

        mCursor = null;
        bindViews();

    }

    private class BodyAdapter extends RecyclerView.Adapter<ViewHolder> {

        private String[] text;

        BodyAdapter(String[] text) {

            this.text = text;

        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view = getLayoutInflater().inflate(R.layout.list_item_text, parent, false);
            return new ViewHolder(view);

        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

            holder.body.setText(text[position]);
        }

        @Override
        public int getItemCount() {

            return text.length;

        }

    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView body;

        ViewHolder(View view) {

            super(view);
            body = view.findViewById(R.id.body_text);

        }

    }
}
