package com.george.redditreader.Models;

import com.george.redditreader.Adapters.RedditItemListAdapter;

/**
 * Created by sound on 8/28/2015.
 */
public class ShowMore implements RedditItem {

    public ShowMore() {

    }

    public int getViewType() {
        return RedditItemListAdapter.VIEW_TYPE_SHOW_MORE;
    }

    public String getThumbnail() {
        return null;
    }

    public void setThumbnailObject(Thumbnail thumbnailObject) {

    }
}
