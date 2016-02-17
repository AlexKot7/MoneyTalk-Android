package ru.tinkoff.telegram.mt.ui;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Arrays;
import java.util.List;

import ru.tinkoff.telegram.mt.NotImplementedYetException;
import ru.tinkoff.telegram.mt.network.Requests;

/**
 * @author a.shishkin1
 */


public class LoaderFragment extends Fragment {

    public static final String ACTIONS_EXTRA = "actions_extra";

    public static final String ANONYMOUS_SESSION = "anonymous_session";

    private IInteraction interaction;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        interaction = (IInteraction) activity;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        new NotImplementedYetException().printStackTrace();
        return new View(getActivity());
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String[] actions = getArguments().getStringArray(ACTIONS_EXTRA);
        if(actions == null) {
            throw new NullPointerException("actions[] == null. no actions for load");
        }
        List<String> actionsList = Arrays.asList(actions);
        if(actionsList.contains(ANONYMOUS_SESSION)) {
            interaction.execute(Requests.REQUEST_SESSION.prepare().buildOn(getActivity()));
        }
    }
}
