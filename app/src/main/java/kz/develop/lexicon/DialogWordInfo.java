package kz.develop.lexicon;

import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Steve Fox on 18.08.2014.
 */
public class DialogWordInfo extends DialogFragment implements View.OnClickListener {

    private String title, message, transcription;
    private int setID, ID, colID, position;
    boolean added, read_only;
    public static final String KEY_ID = "id";
    public static final String KEY_TITLE = "title";
    public static final String KEY_TRANSCRIPTION = "transcription";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_SET_ID = "setid";
    public static final String KEY_COLLECTION_ID = "collection_id";
    public static final String KEY_ADDED = "added";
    public static final String KEY_READ_ONLY = "read_only";
    public static final String KEY_LIST_POSITION = "position";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.ID = getArguments().getInt(KEY_ID, 1);
        this.title = getArguments().getString(KEY_TITLE, "oops");
        this.transcription = getArguments().getString(KEY_TRANSCRIPTION, "oops");
        this.message = getArguments().getString(KEY_MESSAGE, "oops");
        this.setID = getArguments().getInt(KEY_SET_ID, 1);
        this.colID = getArguments().getInt(KEY_COLLECTION_ID, 1);
        this.added = getArguments().getBoolean(KEY_ADDED, false);
        this.read_only = getArguments().getBoolean(KEY_READ_ONLY, false);
        this.position = getArguments().getInt(KEY_LIST_POSITION, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_info, container);
        getDialog().setTitle(R.string.DialogWordInfo_title);
        Button btnAdd = (Button) view.findViewById(R.id.btnAdd);
        Button btnClose = (Button) view.findViewById(R.id.btnClose);
        Button btnSound = (Button) view.findViewById(R.id.btnSound);
        btnAdd.setOnClickListener(this);
        btnClose.setOnClickListener(this);
        btnSound.setOnClickListener(this);
        if (added) btnAdd.setEnabled(false);

        TextView tvTitle = (TextView) view.findViewById(R.id.tvTitle);
        tvTitle.setText(title);
        TextView tvTranscription = (TextView) view.findViewById(R.id.tvTranscription);
        Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Arial.ttf");
        tvTranscription.setTypeface(tf);
        if (!transcription.equals("")) tvTranscription.setText("[" + transcription + "]");
        TextView tvMessage = (TextView) view.findViewById(R.id.tvMessage);
        tvMessage.setText(message);
        LinearLayout llButtons = (LinearLayout) view.findViewById(R.id.llButtons);
        if (read_only) llButtons.setVisibility(View.GONE);

        //Changing divider color
        int titleDividerId = getActivity().getResources().getIdentifier("titleDivider", "id", "android");
        View titleDivider = getDialog().getWindow().getDecorView().findViewById(titleDividerId);
        titleDivider.setBackgroundColor(getResources().getColor(R.color.apptheme_color));
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnAdd:
                ContentValues cv = new ContentValues();
                cv.put(DB.COLUMN_SET, setID);
                cv.put(DB.COLUMN_COLLECTION_ID, colID);
                cv.put(DB.COLUMN_ID_LIST, ID);
                MainActivity.db.insert(DB.TABLE_LIST, null, cv);
                MainActivity.db.execSQL("update "+DB.TABLE_MAIN+" set "+DB.COLUMN_COUNT+" = "+DB.COLUMN_COUNT+" + 1"
                                    +" where "+DB.COLUMN_ID+" = "+String.valueOf(setID));
                Toast.makeText(getActivity().getApplicationContext(), R.string.toastAdded_text, Toast.LENGTH_LONG).show();
                WordAddActivity.setWordAdded(position, getActivity());
                getDialog().dismiss();
                break;
            case R.id.btnClose:
                getDialog().dismiss();
                break;
            case R.id.btnSound:
                MainActivity.speechHelper.speakText(title);
                break;
        }
    }
}
