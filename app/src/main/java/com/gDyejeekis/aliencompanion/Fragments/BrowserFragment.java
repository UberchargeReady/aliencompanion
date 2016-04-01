package com.gDyejeekis.aliencompanion.Fragments;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.gDyejeekis.aliencompanion.Activities.BrowserActivity;
import com.gDyejeekis.aliencompanion.Activities.MainActivity;
import com.gDyejeekis.aliencompanion.Activities.PostActivity;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.api.utils.RedditOAuth;

/**
 * A simple {@link Fragment} subclass.
 */
public class BrowserFragment extends Fragment {

    private WebView webView;
    private ProgressBar progressBar;
    private Submission post;
    private AppCompatActivity activity;
    private Bundle webViewBundle;
    private String url;
    private String domain;

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //if (Uri.parse(url).getHost().equals("www.example.com")) {
            //    // This is my web site, so do not override; let my WebView load the page
            //    return false;
            //}
            //// Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
            //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            //startActivity(intent);
            //return true;
            if(url.substring(0, 15).equals("redditoauthtest")) {
                Log.d("geotest", "redirect url: " + url);
                MainActivity.oauthCode = RedditOAuth.getAuthorizationCode(url);
                if(MainActivity.oauthCode != null) MainActivity.setupAccount = true;
                activity.finish();

                //AsyncTask.execute(new Runnable() {
                //    @Override
                //    public void run() {
                //        try {
                //            RedditOAuth.getOAuthToken(new PoliteRedditHttpClient(), code);
                //        } catch (Exception e) {e.printStackTrace();}
                //    }
                //});

            }
            return false;
        }
    }

    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView webView, int progress) {
            if(progress < 100 && progressBar.getVisibility() == ProgressBar.GONE){
                progressBar.setVisibility(ProgressBar.VISIBLE);
            }
            progressBar.setProgress(progress);
            if(progress == 100) {
                progressBar.setVisibility(ProgressBar.GONE);
            }
        }
    }

    public BrowserFragment newInstance(Submission post) {
        BrowserFragment newInstance = new BrowserFragment();
        newInstance.post = post;
        newInstance.url = post.getURL();
        newInstance.domain = post.getDomain();

        return newInstance;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        post = (Submission) activity.getIntent().getSerializableExtra("post");
        if (post != null) {
            url = post.getUrl();
            domain = post.getDomain();
        } else {
            url = activity.getIntent().getStringExtra("url");
            domain = activity.getIntent().getStringExtra("domain");
        }
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
        try {
            activity.getSupportActionBar().setTitle(domain);
            if (post != null)
                activity.getSupportActionBar().setSubtitle(post.getCommentCount() + " comments");
        } catch (NullPointerException e) {}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browser, container, false);

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar4);
        webView = (WebView) view.findViewById(R.id.webView);

        webView.setWebViewClient(new MyWebViewClient());
        WebSettings settings = webView.getSettings();
        //if(activity instanceof OAuthActivity) settings.setAppCacheEnabled(false);
        //else settings.setAppCacheEnabled(true);
        //settings.setAppCacheMaxSize(20 * 1024 * 1024);
        //settings.setAppCachePath(activity.getCacheDir().getAbsolutePath());
        settings.setAllowFileAccess(true);
        settings.setAppCacheEnabled(true);
        //if(!GeneralUtils.isNetworkAvailable(activity) && MainActivity.offlineModeEnabled) settings.setCacheMode(WebSettings.LOAD_CACHE_ONLY);
        //else settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setJavaScriptEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setDisplayZoomControls(false);
        settings.setBuiltInZoomControls(true);
        settings.setSaveFormData(false);
        settings.setSavePassword(false);
        settings.setDomStorageEnabled(true);

        if(webViewBundle == null) {
            webView.setWebChromeClient(new MyWebChromeClient());
            webView.loadUrl(url);
        }
        else {
            webView.restoreState(webViewBundle);
        }
        return view;
    }

    @Override
    public void onPause() {
        webView.onPause();
        webViewBundle = new Bundle();
        webView.saveState(webViewBundle);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    public void onDestroy() {
        webView.destroy();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_back:
                webView.goBack();
                return true;
            case R.id.action_forward:
                webView.goForward();
                return true;
            case R.id.action_open_browser:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(webView.getUrl())));
                return true;
            case R.id.action_refresh:
                webView.reload();
                return true;
            case R.id.action_comments:
                MainActivity.dualPaneActive = false; //set to false to open comments in a new activity
                Intent intent = new Intent(activity, PostActivity.class);
                intent.putExtra("post", post);
                startActivity(intent);
                return true;
            case R.id.action_load_from_cache:
                loadCachedCopy();
                return true;
            case R.id.action_load_live:
                loadLiveVersion();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadCachedCopy() {
        ((BrowserActivity) activity).loadFromCache = true;
        activity.invalidateOptionsMenu();
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ONLY);
        webView.reload();
    }

    private void loadLiveVersion() {
        ((BrowserActivity) activity).loadFromCache = false;
        activity.invalidateOptionsMenu();
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.reload();
    }

}
