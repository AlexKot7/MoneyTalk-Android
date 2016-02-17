package ru.tinkoff.telegram.mt.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import ru.tinkoff.telegram.mt.R;

import java.util.ArrayList;

import ru.tinkoff.telegram.mt.entities.User;

/**
 * @author a.shishkin1
 */


public class ChooseUserFragment extends Fragment implements AdapterView.OnItemClickListener {

    public static final String EXTRA_USERS = "users";

    private IInteraction interaction;
    private ArrayList<User> users;
    private int padding = 0;
    private BaseAdapter adapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return users.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new TextView(getActivity());
                TextView tv = (TextView) convertView;
                tv.setPadding(padding, padding, padding, padding);
            }
            User current = users.get(position);
            ((TextView) convertView).setText(current.getName());
            convertView.setTag(current);
            return convertView;
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        interaction = (IInteraction) activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        DisplayMetrics dm = getActivity().getResources().getDisplayMetrics();
        users = getArguments().getParcelableArrayList(EXTRA_USERS);
        padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, dm);
        int emptyItemHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, dm);
        Context context = getActivity();
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);

        View header = new View(context);
        View footer = new View(context);
        header.setBackgroundResource(R.drawable.greydivider);
        footer.setBackgroundResource(R.drawable.greydivider);

        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, emptyItemHeight);
        header.setLayoutParams(lp);
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        lp2.weight = 1;
        footer.setLayoutParams(lp2);
        footer.setMinimumHeight(emptyItemHeight);

        ListView listView = new ListView(context);

        listView.addHeaderView(header);
        ll.addView(listView);
        ll.addView(footer);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        return ll;
    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        interaction.onUserSelected((User)view.getTag());
    }
}
