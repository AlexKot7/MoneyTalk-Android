package ru.tinkoff.telegram.mt.ui;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.tinkoff.telegram.mt.network.Requests;

/**
 * @author a.shishkin1
 */


public class RegistrationFragment extends Fragment {

    public static final String PHONE_EXTRA = "phone";

    private IInteraction interaction;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        interaction = (IInteraction) activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = new View(getActivity());
        return view;
    }



    @Override
    public void onResume() {
        super.onResume();
        String phone = getArguments().getString(PHONE_EXTRA);
        interaction.execute(Requests.REQUEST_SIGN_UP.prepare(phone).buildOn(getActivity()));
    }
}
