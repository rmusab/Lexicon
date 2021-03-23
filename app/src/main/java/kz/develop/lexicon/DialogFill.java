package kz.develop.lexicon;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

/**
 * Created by Rav_4 on 06.07.2014.
 */
public class DialogFill extends DialogFragment {

    ProgressBar pbDialogFill;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(R.string.DialogFill_title);
        View v = inflater.inflate(R.layout.dialog_fill, null);
        pbDialogFill = (ProgressBar) v.findViewById(R.id.pbDialogFill);

        //Changing divider color
        int titleDividerId = getActivity().getResources().getIdentifier("titleDivider", "id", "android");
        View titleDivider = getDialog().getWindow().getDecorView().findViewById(titleDividerId);
        titleDivider.setBackgroundColor(getResources().getColor(R.color.apptheme_color));
        return v;
    }

    public void updateProgress(int p){
        pbDialogFill.setProgress(p);
    }
}
