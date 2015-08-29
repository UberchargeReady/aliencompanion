package com.george.redditreader.Fragments;


import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;

import com.george.redditreader.Adapters.PostListAdapterOld;
import com.george.redditreader.ClickListeners.FooterListeners.SearchFooterListener;
import com.george.redditreader.LoadTasks.LoadSearchTask;
import com.george.redditreader.enums.LoadType;
import com.george.redditreader.R;
import com.george.redditreader.api.retrieval.params.SearchSort;
import com.george.redditreader.api.retrieval.params.TimeSpan;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment {

    private AppCompatActivity activity;
    //private HttpClient restClient;
    public ProgressBar mainProgressBar;
    public ProgressBar footerProgressBar;
    public ListView contentView;
    public Button showMore;
    public PostListAdapterOld postListAdapterOld;
    public SearchSort searchSort;
    public TimeSpan timeSpan;
    public String searchQuery;
    public String subreddit;
    public boolean hasPosts;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        //restClient = new RedditHttpClient();
        searchQuery = activity.getIntent().getStringExtra("query");
        subreddit = activity.getIntent().getStringExtra("subreddit");
        //if(subreddit!=null) Log.d("subreddit extra value", subreddit);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (AppCompatActivity) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.activity = null;
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        setActionBarTitle();
        setActionBarSubtitle();
        createFooter();
        if(!hasPosts)
            showMore.setVisibility(View.GONE);
        else showMore.setVisibility(View.VISIBLE);
    }

    private void createFooter() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.footer_layout, null);
        contentView.addFooterView(view);
        footerProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        footerProgressBar.setVisibility(View.GONE);
        showMore = (Button) view.findViewById(R.id.showMore);
        showMore.setOnClickListener(new SearchFooterListener(activity, this));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_list_old, container, false);
        mainProgressBar = (ProgressBar) view.findViewById(R.id.progressBar2);
        contentView = (ListView) view.findViewById(R.id.listView);

        if(postListAdapterOld == null) {
            Log.d("PostListFragment", "Loading posts...");
            setSearchSort(SearchSort.RELEVANCE);
            setTimeSpan(TimeSpan.ALL);
            LoadSearchTask task = new LoadSearchTask(activity, this, LoadType.init);
            task.execute();
        }
        else {
            mainProgressBar.setVisibility(View.GONE);
            contentView.setAdapter(postListAdapterOld);
        }

        return view;
    }

    //Reload posts list
    public void refreshList() {
        contentView.setVisibility(View.GONE);
        mainProgressBar.setVisibility(View.VISIBLE);
        LoadSearchTask task = new LoadSearchTask(activity, this, LoadType.refresh);
        task.execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_refresh:
                refreshList();
                return true;
            case R.id.action_sort:
                showSortPopup(activity.findViewById(R.id.action_sort));
                return true;
            case R.id.action_search:
                showDialog(new SearchRedditDialogFragment());
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDialog(DialogFragment fragment) {
        FragmentManager fm = getFragmentManager();
        fragment.show(fm, "dialog");
    }

    public String getSubreddit() {
        return subreddit;
    }

    public void setSearchQuery(String query) {
        this.searchQuery = query;
    }

    private void showSortPopup(View v) {
        PopupMenu popupMenu = new PopupMenu(activity, v);
        popupMenu.inflate(R.menu.menu_search_sort);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_sort_hot:
                        setSearchSort(SearchSort.HOT);
                        showSortTimePopup(activity.findViewById(R.id.action_sort));
                        return true;
                    case R.id.action_sort_new:
                        setSearchSort(SearchSort.NEW);
                        showSortTimePopup(activity.findViewById(R.id.action_sort));
                        return true;
                    case R.id.action_sort_relevance:
                        setSearchSort(SearchSort.RELEVANCE);
                        showSortTimePopup(activity.findViewById(R.id.action_sort));
                        return true;
                    case R.id.action_sort_top:
                        setSearchSort(SearchSort.TOP);
                        showSortTimePopup(activity.findViewById(R.id.action_sort));
                        return true;
                    case R.id.action_sort_comments:
                        setSearchSort(SearchSort.COMMENTS);
                        showSortTimePopup(activity.findViewById(R.id.action_sort));
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

    private void showSortTimePopup(View v) {
        PopupMenu popupMenu = new PopupMenu(activity, v);
        popupMenu.inflate(R.menu.menu_search_sort_time);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_sort_hour:
                        setTimeSpan(TimeSpan.HOUR);
                        setActionBarSubtitle();
                        refreshList();
                        return true;
                    case R.id.action_sort_day:
                        setTimeSpan(TimeSpan.DAY);
                        setActionBarSubtitle();
                        refreshList();
                        return true;
                    case R.id.action_sort_week:
                        setTimeSpan(TimeSpan.WEEK);
                        setActionBarSubtitle();
                        refreshList();
                        return true;
                    case R.id.action_sort_month:
                        setTimeSpan(TimeSpan.MONTH);
                        setActionBarSubtitle();
                        refreshList();
                        return true;
                    case R.id.action_sort_year:
                        setTimeSpan(TimeSpan.YEAR);
                        setActionBarSubtitle();
                        refreshList();
                        return true;
                    case R.id.action_sort_all:
                        setTimeSpan(TimeSpan.ALL);
                        setActionBarSubtitle();
                        refreshList();
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

    public void setSearchSort(SearchSort searchSort) {
        this.searchSort = searchSort;
    }

    public void setTimeSpan(TimeSpan timeSpan) {
        this.timeSpan = timeSpan;
    }

    public void setActionBarTitle() {
        activity.getSupportActionBar().setTitle(searchQuery);
    }

    public void setActionBarSubtitle() {
        activity.getSupportActionBar().setSubtitle(searchSort.value()+": "+timeSpan.value());
    }

}
