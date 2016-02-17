package ru.tinkoff.telegram.mt.ui.dialogs;

import android.content.Context;
import android.content.DialogInterface;

import org.telegram.ui.ActionBar.BottomSheet;

import java.lang.ref.WeakReference;

import ru.tinkoff.telegram.mt.R;
import ru.tinkoff.telegram.mt.ui.CardsFragment;

/**
 * @author a.shishkin1
 */


public class DeleteCardDialogImpl implements CardsFragment.IDeleteCardDialog {

    private IActions actions;
    private Context context;
    private WeakReference<BottomSheet> dialogRef;

    public DeleteCardDialogImpl(Context context) {
        this.context = context;
    }


    @Override
    public void show() {
        BottomSheet.Builder builder = new BottomSheet.Builder(context);
        builder.setItems(new String[]{context.getString(R.string.mt_delete), context.getString(R.string.mt_cancel)}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0) {
                    actions.onDelete(DeleteCardDialogImpl.this);
                }
                if(which == 1) {
                    actions.onCancel(DeleteCardDialogImpl.this);
                }
            }
        });
        BottomSheet bs = builder.show();
        dialogRef = new WeakReference<BottomSheet>(bs);
    }

    @Override
    public void dismiss() {
        BottomSheet bs = dialogRef.get();
        if(bs != null) {
            bs.dismiss();
        }
    }

    @Override
    public void setActions(IActions actions) {
        this.actions = actions;
    }
}
