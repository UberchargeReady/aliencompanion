package com.george.redditreader.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.george.redditreader.Activities.MainActivity;
import com.george.redditreader.ClickListeners.NavDrawerListeners.AccountListener;
import com.george.redditreader.ClickListeners.NavDrawerListeners.HeaderListener;
import com.george.redditreader.ClickListeners.NavDrawerListeners.MenuItemListener;
import com.george.redditreader.ClickListeners.NavDrawerListeners.NavDrawerListener;
import com.george.redditreader.ClickListeners.NavDrawerListeners.SubredditItemListener;
import com.george.redditreader.ClickListeners.NavDrawerListeners.SubredditsListener;
import com.george.redditreader.Models.NavDrawer.NavDrawerAccount;
import com.george.redditreader.Models.NavDrawer.NavDrawerItem;
import com.george.redditreader.Models.NavDrawer.NavDrawerMenuItem;
import com.george.redditreader.Models.NavDrawer.NavDrawerSubredditItem;
import com.george.redditreader.Models.SavedAccount;
import com.george.redditreader.R;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by George on 6/25/2015.
 */
public class NavDrawerAdapter extends RecyclerView.Adapter {

    public static final int VIEW_TYPE_HEADER = 0;

    public static final int VIEW_TYPE_MENU_ITEM = 1;

    public static final int VIEW_TYPE_SUBREDDITS = 2;

    public static final int VIEW_TYPE_SUBREDDIT_ITEM = 3;

    public static final int VIEW_TYPE_ACCOUNT = 4;

    //public static final int VIEW_TYPE_SEPARATOR = 5;

    private final MainActivity activity;

    private List<NavDrawerItem> items;

    private boolean accountItemsVisible;
    private boolean subredditItemsVisible;

    private List<NavDrawerItem> subredditItems;
    public List<NavDrawerAccount> accountItems;

    public static int currentAccountIndex;

    public NavDrawerAdapter(MainActivity activity) {
        items = new ArrayList<>();
        this.activity = activity;
        subredditItemsVisible = true;
        accountItemsVisible = false;
        importAccounts();
        //Log.d("current account index", "at init : " + currentAccountIndex);
    }

    public void updateSubredditItems(List<String> subredditNames) {
        subredditItems = new ArrayList<>();
        for(NavDrawerItem item : items) {
            if(item instanceof NavDrawerSubredditItem) subredditItems.add(item);
        }
        items.removeAll(subredditItems);
        subredditItems.clear();
        subredditItems.add(new NavDrawerSubredditItem());
        for(String name : subredditNames) {
            subredditItems.add(new NavDrawerSubredditItem(name));
        }
        items.addAll(subredditItems);
        notifyDataSetChanged();
    }

    private void importAccounts() {
        currentAccountIndex = MainActivity.prefs.getInt("currentAccountIndex", 0);
        List<SavedAccount> savedAccounts = readAccounts();

        accountItems = new ArrayList<>();
        accountItems.add(new NavDrawerAccount(1));
        accountItems.add(new NavDrawerAccount(0));
        if(savedAccounts != null) {
            for(SavedAccount account : savedAccounts) {
                accountItems.add(new NavDrawerAccount(account));
            }
            SavedAccount currentAccount = accountItems.get(currentAccountIndex).savedAccount;
            MainActivity.currentAccount = currentAccount;
        }

    }

    private List<SavedAccount> readAccounts() {
        try {
            FileInputStream fis = activity.openFileInput(MainActivity.SAVED_ACCOUNTS_FILENAME);
            ObjectInputStream is = new ObjectInputStream(fis);
            List<SavedAccount> savedAccounts = (List<SavedAccount>) is.readObject();
            is.close();
            fis.close();
            return savedAccounts;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void saveAccounts(List<SavedAccount> updatedAccounts) {
        try {
            FileOutputStream fos = activity.openFileOutput(MainActivity.SAVED_ACCOUNTS_FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(updatedAccounts);
            os.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteAccount(String accountToDelete) {
        //Log.d("current account index", "before delete : " + currentAccountIndex);
        List<SavedAccount> accounts = readAccounts();
        for(SavedAccount account : accounts) {
            if(account.getUsername().equals(accountToDelete)) {
                accounts.remove(account);
                break;
            }
        }
        saveAccounts(accounts);
        int i=0;
        for(NavDrawerAccount account : accountItems) {
            if(account.getName().equals(accountToDelete)) {
                accountItems.remove(account);
                items.remove(account);
                if(currentAccountIndex == i) currentAccountIndex = 0;
                else if(currentAccountIndex > i) currentAccountIndex -=1;
                break;
            }
            i++;
        }
        SharedPreferences.Editor editor = MainActivity.prefs.edit();
        editor.putInt("currentAccountIndex", currentAccountIndex);
        editor.apply();
        if(currentAccountIndex==0) {
            activity.getDrawerLayout().closeDrawers();
            activity.changeCurrentUser(null);
        }
        else notifyDataSetChanged();
        //Log.d("current account index", "after delete : " + currentAccountIndex);
    }

    public void add(NavDrawerItem item) {
        items.add(item);
    }

    public void add(int location, NavDrawerItem item) {
        items.add(location, item);
    }

    public void addAll(List<NavDrawerItem> items) {
        this.items.addAll(items);
    }

    public NavDrawerItem getItemAt(int position) {
        return items.get(position);
    }

    public void toggleAccountItems() {
        if(accountItemsVisible) collapseAccountItems();
        else expandAccountItems();
    }

    private void collapseAccountItems() {
        items.removeAll(accountItems);
        accountItemsVisible = false;
        //notifyDataSetChanged();
        notifyItemChanged(0);
        notifyItemRangeRemoved(1, accountItems.size());
    }

    private void expandAccountItems() {
        int i = 1;
        for(NavDrawerAccount accountItem : accountItems) {
            items.add(i, accountItem);
            i++;
        }
        accountItemsVisible = true;
        //notifyDataSetChanged();
        notifyItemChanged(0);
        notifyItemRangeInserted(1, accountItems.size());
    }

    public void toggleSubredditItems() {
        if(subredditItemsVisible) collapseSubredditItems();
        else expandSubredditItems();
    }

    private void collapseSubredditItems() {
        subredditItems = new ArrayList<>();
        for(NavDrawerItem item : items) {
            if(item instanceof NavDrawerSubredditItem) subredditItems.add(item);
        }
        items.removeAll(subredditItems);
        subredditItemsVisible = false;
        //notifyDataSetChanged();
        notifyItemChanged(items.size()-1);
        notifyItemRangeRemoved(items.size(), subredditItems.size());
    }

    private void expandSubredditItems() {
        int position = items.size();
        items.addAll(subredditItems);
        subredditItemsVisible = true;
        //notifyDataSetChanged();
        notifyItemChanged(position-1);
        notifyItemRangeInserted(position, subredditItems.size());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        RecyclerView.ViewHolder viewHolder = null;
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                int resource = R.layout.drawer_header;
                NavDrawerListener listener = new HeaderListener(activity);
                v = LayoutInflater.from(parent.getContext())
                        .inflate(resource, parent, false);
                v.setOnClickListener(listener);
                //v.setOnClickListener(new HeaderListener(activity));
                viewHolder = new HeaderViewHolder(v);
                break;
            case VIEW_TYPE_MENU_ITEM:
                resource = R.layout.drawer_menu_row;
                listener = new MenuItemListener(activity);
                v = LayoutInflater.from(parent.getContext())
                        .inflate(resource, parent, false);
                v.setOnClickListener(listener);
                //v.setOnClickListener(new MenuItemListener(activity));
                viewHolder = new MenuRowViewHolder(v);
                break;
            case VIEW_TYPE_SUBREDDITS:
                resource = R.layout.drawer_subreddits;
                v = LayoutInflater.from(parent.getContext())
                        .inflate(resource, parent, false);
                viewHolder = new SubredditsViewHolder(v);
                break;
            case VIEW_TYPE_SUBREDDIT_ITEM:
                resource = R.layout.drawer_subreddit_row;
                listener = new SubredditItemListener(activity);
                v = LayoutInflater.from(parent.getContext())
                        .inflate(resource, parent, false);
                v.setOnClickListener(listener);
                //v.setOnClickListener(new SubredditItemListener(activity));
                viewHolder = new SubredditRowViewHolder(v);
                break;
            case VIEW_TYPE_ACCOUNT:
                resource = R.layout.drawer_account_row;
                listener = new AccountListener(activity);
                v = LayoutInflater.from(parent.getContext())
                        .inflate(resource, parent, false);
                v.setOnClickListener(listener);
                v.setOnLongClickListener(listener);
                //v.setOnClickListener(new AccountListener(activity));
                viewHolder = new SubredditRowViewHolder(v);
                break;
            default:
                throw new IllegalStateException("unknown viewType");
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                HeaderViewHolder headerViewHolder = (HeaderViewHolder) viewHolder;
                //NavDrawerHeader header = (NavDrawerHeader) getItemAt(position);
                headerViewHolder.currentAccount.setText(accountItems.get(currentAccountIndex).getName());
                if(accountItemsVisible) headerViewHolder.toggle.setImageResource(R.mipmap.ic_action_collapse);
                else headerViewHolder.toggle.setImageResource(R.mipmap.ic_action_expand);
                break;
            case VIEW_TYPE_MENU_ITEM:
                MenuRowViewHolder menuRowViewHolder= (MenuRowViewHolder) viewHolder;
                NavDrawerMenuItem menuItem = (NavDrawerMenuItem) getItemAt(position);
                menuRowViewHolder.name.setText(menuItem.getMenuType().value());
                switch (menuItem.getMenuType()) {
                    case profile:
                        menuRowViewHolder.image.setImageResource(R.mipmap.ic_action_profile_grey);
                        break;
                    case messages:
                        menuRowViewHolder.image.setImageResource(R.mipmap.ic_action_messages_grey);
                        break;
                    case user:
                        menuRowViewHolder.image.setImageResource(R.mipmap.ic_action_user_grey);
                        break;
                    case subreddit:
                        menuRowViewHolder.image.setImageResource(R.mipmap.ic_action_subreddit);
                        break;
                    case settings:
                        menuRowViewHolder.image.setImageResource(R.mipmap.ic_action_settings_grey);
                        break;
                    case cached:
                        menuRowViewHolder.image.setImageResource(R.mipmap.ic_action_cached_grey);
                        break;
                }
                break;
            case VIEW_TYPE_SUBREDDITS:
                SubredditsViewHolder subredditsViewHolder = (SubredditsViewHolder) viewHolder;
                //NavDrawerSubreddits subreddits = (NavDrawerSubreddits) getItemAt(position);
                if(subredditItemsVisible) subredditsViewHolder.imgToggle.setImageResource(R.mipmap.ic_action_collapse);
                else subredditsViewHolder.imgToggle.setImageResource(R.mipmap.ic_action_expand);
                SubredditsListener listener = new SubredditsListener(activity);
                subredditsViewHolder.layoutToggle.setOnClickListener(listener);
                subredditsViewHolder.layoutEdit.setOnClickListener(listener);
                break;
            case VIEW_TYPE_SUBREDDIT_ITEM:
                SubredditRowViewHolder subredditRowViewHolder = (SubredditRowViewHolder) viewHolder;
                NavDrawerSubredditItem subredditItem = (NavDrawerSubredditItem) getItemAt(position);
                String subreddit = (subredditItem.getName() != null) ? subredditItem.getName() : "Frontpage";
                subredditRowViewHolder.name.setText(subreddit);
                //Highlight current subreddit
                String currentSubreddit = activity.getListFragment().subreddit;
                if(subreddit.equals(currentSubreddit) ||
                        (subredditItem.getName() == null && currentSubreddit == null)) {
                    subredditRowViewHolder.name.setTextColor(Color.BLUE);
                    subredditRowViewHolder.layout.setBackgroundColor(Color.parseColor("#E7E7E8"));
                }
                else {
                    subredditRowViewHolder.name.setTextColor(Color.BLACK);
                    subredditRowViewHolder.layout.setBackground(activity.getResources().getDrawable(R.drawable.touch_selector));
                }
                break;
            case VIEW_TYPE_ACCOUNT:
                SubredditRowViewHolder accountViewHolder = (SubredditRowViewHolder) viewHolder;
                NavDrawerAccount account = (NavDrawerAccount) getItemAt(position);
                accountViewHolder.name.setText(account.getName());
                if(account == accountItems.get(currentAccountIndex)) accountViewHolder.name.setTextColor(Color.parseColor("#C2C2FF"));
                else accountViewHolder.name.setTextColor(Color.WHITE);
                break;
            default:
                throw new IllegalStateException("unknown viewType");
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return getItemAt(position).getType();
    }

    public static class MenuRowViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView name;

        public MenuRowViewHolder(View row) {
            super(row);
            image = (ImageView) row.findViewById(R.id.imgView_option);
            name = (TextView) row.findViewById(R.id.txtView_option);
        }
    }

    public static class SubredditRowViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public LinearLayout layout;

        public SubredditRowViewHolder(View row) {
            super(row);
            name = (TextView) row.findViewById(R.id.txtView_subreddit);
            layout = (LinearLayout) row.findViewById(R.id.subredditRowLayout);
        }
    }

    public static class SubredditsViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout layoutToggle;
        public LinearLayout layoutEdit;
        public ImageView imgToggle;

        public SubredditsViewHolder(View row) {
            super(row);
            layoutToggle = (LinearLayout) row.findViewById(R.id.layoutToggle);
            layoutEdit = (LinearLayout) row.findViewById(R.id.layoutEdit);
            imgToggle = (ImageView) row.findViewById(R.id.imgView_toggle);
        }
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public TextView currentAccount;
        public CircleImageView circleImageView;
        public LinearLayout accountLayout;
        public ImageView toggle;

        public HeaderViewHolder(View row) {
            super(row);
            currentAccount = (TextView) row.findViewById(R.id.txtView_currentAccount);
            circleImageView = (CircleImageView) row.findViewById(R.id.circleView);
            accountLayout = (LinearLayout) row.findViewById(R.id.layout_account);
            toggle = (ImageView) row.findViewById(R.id.imgView_toggle);
        }
    }
}
