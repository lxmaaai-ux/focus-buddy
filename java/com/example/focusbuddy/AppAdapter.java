package com.example.focusbuddy;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.*;
import android.widget.*;

import java.util.*;

public class AppAdapter extends BaseAdapter {

    Context context;
    List<AppModel> appList;

    public AppAdapter(Context context, List<AppModel> appList) {
        this.context = context;
        this.appList = appList;
    }

    @Override
    public int getCount() {
        return appList.size();
    }

    @Override
    public Object getItem(int i) {
        return appList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_app, parent, false);
        }

        ImageView icon = view.findViewById(R.id.appIcon);
        TextView name = view.findViewById(R.id.appName);
        Switch toggle = view.findViewById(R.id.appSwitch);

        AppModel app = appList.get(i);

        // ✅ Set icon + name
        icon.setImageResource(app.icon);
        name.setText(app.appName);

        // ✅ Get saved apps
        SharedPreferences prefs = context.getSharedPreferences("block", Context.MODE_PRIVATE);
        Set<String> savedApps = prefs.getStringSet("apps", new HashSet<>());

        // ✅ Set toggle state
        toggle.setOnCheckedChangeListener(null); // important fix
        toggle.setChecked(savedApps.contains(app.packageName));

        // ✅ Handle toggle change
        toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {

            Set<String> set = new HashSet<>(prefs.getStringSet("apps", new HashSet<>()));

            if (isChecked) {
                set.add(app.packageName);
            } else {
                set.remove(app.packageName);
            }

            prefs.edit().putStringSet("apps", set).apply();
        });

        return view;
    }
}
