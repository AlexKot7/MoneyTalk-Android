package ru.tinkoff.telegram.mt.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
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
import java.util.Collection;
import java.util.List;

import ru.tinkoff.telegram.mt.entities.Card;
import ru.tinkoff.telegram.mt.entities.MtConfig;
import ru.tinkoff.telegram.mt.mtpart.MtAppPart;
import ru.tinkoff.telegram.mt.network.Network;
import ru.tinkoff.telegram.mt.network.Requests;

/**
 * @author a.shishkin1
 */


public class SettingsFragment extends Fragment implements AdapterView.OnItemClickListener, ICardInterest {

    public static final String GOAL = "goal";

    public static final String SETTINGS = "settings";
    public static final String RESET = "reset";
    public static final String OFFER = "offer";

    private final int EMPTY_RAW = 0;
    private final int SIMPLE_RAW = 1;

    private List<Item> menuItems = new ArrayList<>();
    private ItemAdapter adapter = new ItemAdapter();
    private IInteraction interaction;
    private DisplayMetrics dm;
    private int emptyItemHeight;
    private int textItemHeight;
    private int measure;
    private MtConfig config;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        interaction = (IInteraction)activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Context context = getActivity();
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        ListView listView = new ListView(context);
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        emptyItemHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, dm);
        textItemHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, dm);
        measure = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, dm);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        linearLayout.addView(listView);

        View view = new View(context);
        view.setBackgroundResource(R.drawable.greydivider);
        linearLayout.addView(view);
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
        lp.height = 0;
        lp.weight = 1;
        return linearLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        String goal = bundle.getString(GOAL);
        if(goal == null) {
            throw new NullPointerException("goal of settings is null");
        }
        switch (goal) {
            case SETTINGS:
                final Network.RequestImpl accountsRequest = Requests.REQUEST_ACCOUNTS_FLAT.prepare().buildOn(getActivity());
                accountsRequest.setShowDialog(true);
                interaction.execute(accountsRequest);
                break;
            case RESET:
                menuItems = initResetMenu();
                break;
            case OFFER:
                menuItems = initOfferMenu();
                break;
            default:
                throw new IllegalArgumentException("unknown goal: \"" + goal + "\" for Settings");
        }
        adapter.notifyDataSetChanged();
        config = MtAppPart.getMtAppInstance(getActivity()).getConfig();
    }

    private class ItemAdapter extends BaseAdapter {

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            return getItem(position).action != null;
        }

        @Override
        public int getCount() {
            return menuItems.size();
        }

        @Override
        public Item getItem(int position) {
            return menuItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Item item = getItem(position);
            int type = item.type;
            if(type == EMPTY_RAW) {
                View view = new View(getActivity());
                AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, emptyItemHeight);
                view.setBackgroundResource(R.drawable.greydivider);
                view.setLayoutParams(lp);
                return view;
            } else if(type == SIMPLE_RAW) {
                AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, textItemHeight);
                TextView tv = new TextView(getActivity());
                tv.setGravity(Gravity.CENTER_VERTICAL);
                tv.setPadding(measure, 0, measure, 0);
                tv.setLayoutParams(lp);
                tv.setText(item.text);
                return tv;
            }

            throw new IllegalArgumentException("unknown item type");
        }

        @Override
        public int getViewTypeCount() {
            return 10;
        }

        @Override
        public int getItemViewType(int position) {
            return getItem(position).type;
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Item item = adapter.getItem(position);
        if(item.action != null) {
            item.action.run();
        }
    }

    @Override
    public void setCards(List<Card> cards) {
        if(cards.size() == 0) {
            menuItems = initSettingsMenuWithoutCards();
        } else {
            menuItems = initSettingsMenuWithCards();
        }
        adapter.notifyDataSetChanged();
    }

    private class Item {
        int type;
        String text;
        Runnable action;

        public Item() {
            type = EMPTY_RAW;
            text = null;
            action = null;
        }

        public Item(int type, String text, Runnable action) {
            this.type = type;
            this.text = text;
            this.action = action;
        }

        public Item addTo(Collection<Item> items) {
            items.add(this);
            return this;
        }
    }

    private List<Item> initSettingsMenuWithCards() {
        ArrayList<Item> items = new ArrayList<>();
        new Item().addTo(items);
        new Item(SIMPLE_RAW, getString(R.string.mt_settings_your_cards), new Runnable() {
            @Override
            public void run() {
                interaction.showCards();
            }
        }).addTo(items);
        new Item().addTo(items);
        new Item(SIMPLE_RAW, getString(R.string.mt_settings_offer), new Runnable() {
            @Override
            public void run() {
                interaction.showOfferMenu();
            }
        }).addTo(items);
        new Item(SIMPLE_RAW, getString(R.string.mt_settings_reset), new Runnable() {
            @Override
            public void run() {
                interaction.showResetMenu();
            }
        }).addTo(items);

        return items;
    }

    private List<Item> initSettingsMenuWithoutCards() {
        ArrayList<Item> items = new ArrayList<>();
        new Item().addTo(items);
        new Item(SIMPLE_RAW, getString(R.string.mt_settings_offer), new Runnable() {
            @Override
            public void run() {
                interaction.showOfferMenu();
            }
        }).addTo(items);
        new Item(SIMPLE_RAW, getString(R.string.mt_settings_reset), new Runnable() {
            @Override
            public void run() {
                interaction.showResetMenu();
            }
        }).addTo(items);

        return items;
    }

    private List<Item> initResetMenu() {
        ArrayList<Item> items = new ArrayList<>();
        new Item().addTo(items);
        new Item(SIMPLE_RAW, getString(R.string.mt_settings_change_pin), new Runnable() {
            @Override
            public void run() {
                interaction.showChangePinConfirm();
            }
        }).addTo(items);
        new Item(SIMPLE_RAW, getString(R.string.mt_settings_reset_all_settings), new Runnable() {
            @Override
            public void run() {
                interaction.resetSettings();
            }
        }).addTo(items);

        return items;
    }

    private List<Item> initOfferMenu() {
        ArrayList<Item> items = new ArrayList<>();
        new Item().addTo(items);
        new Item(SIMPLE_RAW, getString(R.string.mt_settings_offer_offer), new Runnable() {
            @Override
            public void run() {
                String url = config.getString("mtEula.ofertaUrl");
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                getActivity().startActivity(intent);
            }
        }).addTo(items);
        new Item(SIMPLE_RAW, getString(R.string.mt_settings_offer_transfers), new Runnable() {
            @Override
            public void run() {
                String url = config.getString("mtEula.transferConditionsUrl");
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                getActivity().startActivity(intent);
            }
        }).addTo(items);

        return items;
    }



}
