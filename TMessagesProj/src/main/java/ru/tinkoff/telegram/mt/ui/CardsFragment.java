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

import java.util.ArrayList;
import java.util.List;

import ru.tinkoff.telegram.mt.R;
import ru.tinkoff.telegram.mt.entities.Card;
import ru.tinkoff.telegram.mt.network.Network;
import ru.tinkoff.telegram.mt.network.Requests;
import ru.tinkoff.telegram.mt.ui.dialogs.DeleteCardDialogImpl;
import ru.tinkoff.telegram.mt.views.PrimaryShowCardView;

/**
 * @author a.shishkin1
 */


public class CardsFragment extends Fragment implements AdapterView.OnItemClickListener, ICardInterest, AdapterView.OnItemLongClickListener {

    private List<Card> cards = new ArrayList<>();
    private IInteraction interaction;
    private BaseAdapter adapter;
    private int itemHeight;
    private int measure;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        interaction = (IInteraction) activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Context context = getActivity();
        LinearLayout llFrame = new LinearLayout(context);
        llFrame.setOrientation(LinearLayout.VERTICAL);
        ListView listView = new ListView(getActivity());
        DisplayMetrics dm = getActivity().getResources().getDisplayMetrics();
        itemHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, dm);
        measure = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, dm);
        int dp12 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, dm);
        adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return cards.size();
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
                Card card = cards.get(position);
                if(convertView == null) {
                    convertView = new PrimaryShowCardView(getActivity());
                    AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight);
                    convertView.setLayoutParams(lp);
                    convertView.setPadding(measure, 0, measure, 0);
                }
                ((PrimaryShowCardView)convertView).show(card.getCardName(), card.getValue(), card.isPrimary() && getCount() > 1);

                return convertView;
            }
        };
        listView.setAdapter(adapter);
        listView.setOnItemLongClickListener(this);
        View topView = new View(context);
        topView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp12));
        topView.setBackgroundResource(R.drawable.greydivider);
        View bottomView = new View(context);
        LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        lllp.weight = 1;
        bottomView.setLayoutParams(lllp);
        bottomView.setBackgroundResource(R.drawable.greydivider);

        llFrame.addView(topView);
        llFrame.addView(listView);
        llFrame.addView(bottomView);
        return llFrame;
    }

    public void showCardAsPrimary(String cardId) {
        for(Card card : cards) {
            card.setPrimary(card.getId().equals(cardId));
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        interaction.execute(Requests.REQUEST_ACCOUNTS_FLAT.prepare().buildOn(getActivity()));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Card card = cards.get(position);
        if(card.isPrimary())
            return;
        String cardId = card.getId();
        Network.RequestImpl request = Requests.REQUEST_SET_LINKED_CARD_PRIMARY.prepare(cardId).buildOn(getActivity());
        Bundle bundle = new Bundle();
        bundle.putString(BaseActivity.EXTRA_CARD_ID, cardId);
        request.setAdditions(bundle);
        interaction.execute(request, Requests.REQUEST_ACCOUNTS_FLAT.prepare().buildOn(getActivity()));
    }


    @Override
    public void setCards(List<Card> cards) {
        this.cards = cards;
        adapter.notifyDataSetChanged();
    }

    private IDeleteCardDialog createDeleteCardDialog() {
        return new DeleteCardDialogImpl(getActivity());
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        IDeleteCardDialog deleteCardDialog = createDeleteCardDialog();
        final Card card = cards.get(position);
        deleteCardDialog.setActions(new IDeleteCardDialog.IActions() {
            @Override
            public void onDelete(IDeleteCardDialog dialog) {
                interaction.execute(
                        Requests.REQUEST_DETACH_CARD.prepare(card.getId()).buildOn(getActivity()),
                        Requests.REQUEST_ACCOUNTS_FLAT.prepare().buildOn(getActivity()));
                dialog.dismiss();
            }

            @Override
            public void onCancel(IDeleteCardDialog dialog) {
                dialog.dismiss();
            }
        });
        deleteCardDialog.show();
        return true;
    }


    public interface IDeleteCardDialog {
        void show();
        void dismiss();
        void setActions(IActions actions);

        interface IActions {
            void onDelete(IDeleteCardDialog dialog);
            void onCancel(IDeleteCardDialog dialog);
        }
    }


}
