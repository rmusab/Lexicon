package kz.develop.lexicon;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Steve Fox on 02.09.2014.
 */
public class TempResultFragment extends Fragment {

    public static final String KEY_RIGHT = "right";
    public static final String KEY_WORD = "word";
    public static final String KEY_TRANSL = "transl";
    public static final String KEY_FULL_TRANSL = "full_transl";

    private static final int NEXT_PAGE = 0;
    private static final long SHOW_TIME = 2000;

    private TrainingActivity.OnNextFragmentListener myNextFragmentListener;
    private boolean right = false;
    private String word, translation, fullTransl;

    private Handler myHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case NEXT_PAGE:
                    myNextFragmentListener.nextFragment();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public static TempResultFragment getInstance(boolean right, String word, String translation, String fullTransl) {
        TempResultFragment fragment = new TempResultFragment();
        Bundle arg = new Bundle();
        arg.putBoolean(KEY_RIGHT, right);
        arg.putString(KEY_WORD, word);
        arg.putString(KEY_TRANSL, translation);
        arg.putString(KEY_FULL_TRANSL, fullTransl);
        fragment.setArguments(arg);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.right = getArguments().getBoolean(KEY_RIGHT);
        this.word = getArguments().getString(KEY_WORD);
        this.translation = getArguments().getString(KEY_TRANSL);
        this.fullTransl = getArguments().getString(KEY_FULL_TRANSL);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.temp_result, container, false);
        ImageView imgCheck = (ImageView) view.findViewById(R.id.imgCheck);
        if (right) imgCheck.setImageDrawable(getResources().getDrawable(R.drawable.right));
        TextView tvWord = (TextView) view.findViewById(R.id.tvWord);
        tvWord.setText(word);
        TextView tvTranslation = (TextView) view.findViewById(R.id.tvTranslation);
        tvTranslation.setText(translation);
        Button btnNext = (Button) view.findViewById(R.id.btnNext);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myNextFragmentListener.nextFragment();
            }
        });
        btnNext.setVisibility(right ? View.GONE : View.VISIBLE);
        View separator = view.findViewById(R.id.Separator2);
        separator.setVisibility(right ? View.GONE : View.VISIBLE);
        TextView tvWrong = (TextView) view.findViewById(R.id.tvWrong);
        tvWrong.setVisibility(right ? View.GONE : View.VISIBLE);
        TextView tvFullTransl = (TextView) view.findViewById(R.id.tvFullTransl);
        tvFullTransl.setText(fullTransl);
        if (right) {
            Message msg = new Message();
            msg.what = NEXT_PAGE;
            myHandler.sendMessageDelayed(msg, SHOW_TIME);
        }
        MainActivity.speechHelper.speakText(word);
        return view;
    }

    public void setNextFragmentListener(TrainingActivity.OnNextFragmentListener listener) {
        myNextFragmentListener = listener;
    }
}
