package kz.develop.lexicon;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Steve Fox on 20.08.2014.
 */
public class DialogDeleteOne extends DialogFragment implements View.OnClickListener {

    private long id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.id = getArguments().getLong(DialogContext.KEY_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.alert_dialog, container);
        getDialog().setTitle(R.string.dlgConfirm_title);
        TextView tvMessage = (TextView) view.findViewById(R.id.tvMessage);
        tvMessage.setText(R.string.dlgConfirm_message);
        Button btnPositive = (Button) view.findViewById(R.id.btnPositive);
        btnPositive.setOnClickListener(this);
        Button btnNegative = (Button) view.findViewById(R.id.btnNegative);
        btnNegative.setOnClickListener(this);

        //Changing divider color
        int titleDividerId = getActivity().getResources().getIdentifier("titleDivider", "id", "android");
        View titleDivider = getDialog().getWindow().getDecorView().findViewById(titleDividerId);
        titleDivider.setBackgroundColor(getResources().getColor(R.color.apptheme_color));
        return  view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnPositive:
                MainActivity.deleteSet(id);
                getActivity().getLoaderManager().getLoader(0).forceLoad();
                getDialog().dismiss();
                break;
            case R.id.btnNegative:
                getDialog().dismiss();
                break;
        }
    }
}
