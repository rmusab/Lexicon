package kz.develop.lexicon;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

/**
 * Created by Steve Fox on 20.08.2014.
 */
public class DialogContext extends DialogFragment implements DialogInterface.OnClickListener {

    public static final String KEY_ID = "id";
    public static final int MENU_DELETE_ONE = 0;
    public static final int MENU_DELETE_ALL = 1;

    private long id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.id = getArguments().getLong(KEY_ID);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String[] data = {getResources().getString(R.string.menu_delete), getResources().getString(R.string.menu_delete_all)};
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.simple_item, data);
        adb.setAdapter(adapter, this);
        return adb.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int position) {
        DialogFragment dialog;
        switch (position) {
            case MENU_DELETE_ONE:
                dialog = new DialogDeleteOne();
                Bundle arg = new Bundle();
                arg.putLong(KEY_ID, id);
                dialog.setArguments(arg);
                dialog.setCancelable(false);
                dialog.show(getFragmentManager(), "dlg_delete_one");
                getDialog().dismiss();
                break;
            case MENU_DELETE_ALL:
                dialog = new DialogDeleteAll();
                dialog.setCancelable(false);
                dialog.show(getFragmentManager(), "dlg_delete_all");
                getDialog().dismiss();
                break;
        }
    }
}
