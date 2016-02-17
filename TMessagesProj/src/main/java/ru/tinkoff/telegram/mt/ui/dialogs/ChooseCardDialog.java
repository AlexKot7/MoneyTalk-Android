package ru.tinkoff.telegram.mt.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import ru.tinkoff.telegram.mt.NotImplementedYetException;
import ru.tinkoff.telegram.mt.R;
import ru.tinkoff.telegram.mt.base.ICallback;
import ru.tinkoff.telegram.mt.entities.Card;
import ru.tinkoff.telegram.mt.views.ShowCardView;

/**
 * @author a.shishkin1
 */


public class ChooseCardDialog extends Dialog implements AdapterView.OnItemClickListener {


    private List<Card> cards;
    private ICallback<Card> onCardSelectedListener;
    private CardsAdapter adapter;
    private int itemHeight;
    private int padding;

    public ChooseCardDialog(Context context, List<Card> cards, ICallback<Card> onCardSelectedListener) {
        super(context);
        this.cards = cards;
        this.onCardSelectedListener = onCardSelectedListener;
        ListView view = new ListView(context);
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        this.itemHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, dm);
        this.padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, dm);
        this.adapter = new CardsAdapter();
        view.setAdapter(adapter);
        view.setOnItemClickListener(this);
        setContentView(view);
        setTitle(R.string.mt_choose_your_card);

    }



    private class CardsAdapter extends BaseAdapter {



        @Override
        public int getCount() {
            return cards.size() + 1; // first item "add new card"
        }

        @Override
        public Card getItem(int position) {
            return cards.get(position - 1);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int type = getItemViewType(position);
            if(type == 0) {
                TextView textView;
                if(convertView == null) {
                    textView = new TextView(getContext());
                    textView.setLayoutParams(createLayoutParams());
                    textView.setGravity(Gravity.CENTER_VERTICAL);
                    textView.setPadding(padding, 0, padding, 0);
                    convertView = textView;
                } else {
                    textView = (TextView) convertView;
                }
                textView.setText(R.string.mt_transfer_new_card);
            } else if (type == 1) {
                ShowCardView scvCard;
                if(convertView == null) {
                    scvCard = new ShowCardView(getContext());
                    scvCard.setLayoutParams(createLayoutParams());
                    scvCard.setGravity(Gravity.CENTER_VERTICAL);
                    scvCard.setPadding(padding, 0, padding, 0);
                    convertView = scvCard;
                } else {
                    scvCard = (ShowCardView)convertView;
                }
                Card card = cards.get(position - 1);
                scvCard.show(card.getCardName(), card.getValue());
            }
            return convertView;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            return position == 0 ? 0 : 1;
        }

        private AbsListView.LayoutParams createLayoutParams() {
            return new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, itemHeight);
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(position == 0) {
            onCardSelectedListener.onResult(null);
        } else {
            onCardSelectedListener.onResult(adapter.getItem(position));
        }
        dismiss();
    }
}
