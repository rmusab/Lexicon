package kz.develop.lexicon;

import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Steve Fox on 29.08.2014.
 */
public class TestFragment extends Fragment {

    public static final String KEY_ID = "id";
    public static final String KEY_COLLECTION_ID = "collection_id";
    public static final String KEY_COUNT = "count";

    private int id, colID, count, rightVar;
    private TrainingActivity.OnNextFragmentListener myNextFragmentListener;
    private String word = "";

    public static TestFragment getInstance(int id, int collection_id, int count) {
        TestFragment fragment = new TestFragment();
        Bundle arg = new Bundle();
        arg.putInt(KEY_ID, id);
        arg.putInt(KEY_COLLECTION_ID, collection_id);
        arg.putInt(KEY_COUNT, count);
        fragment.setArguments(arg);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.id = getArguments().getInt(KEY_ID);
        this.colID = getArguments().getInt(KEY_COLLECTION_ID);
        this.count = getArguments().getInt(KEY_COUNT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.test, container, false);
        TextView tvWord = (TextView) view.findViewById(R.id.tvWord);
        word = FinalDB.getWordByID(id, colID);
        tvWord.setText(String.valueOf(TrainingActivity.lastIndex + 1) + ". " + word);
        TextView tvTranscription = (TextView) view.findViewById(R.id.tvTranscription);
        Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Arial.ttf");
        tvTranscription.setTypeface(tf);
        final String transcription = FinalDB.getWordTranscription(FinalDB.getWordColumnsByID(id, colID), colID);
        if (!transcription.equals("")) tvTranscription.setText("[" + transcription + "]");
        Button btnSound = (Button) view.findViewById(R.id.btnSound);
        btnSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.speechHelper.speakText(word);
            }
        });
        LinearLayout llVariants = (LinearLayout) view.findViewById(R.id.llVariants);
        final ArrayList<String> variants = new ArrayList<String>();
        Random random = new Random();
        rightVar = random.nextInt(5);
        String[] wrongVar = FinalDB.getVariantsByID(id, colID);
        int j = 0;
        for (int i = 0; i < 5; i++) {
            if (i == rightVar) variants.add(FinalDB.getFirstWordTranslationByID(id, colID));
                else {
                    variants.add(wrongVar[j]);
                    j++;
                }
        }
        for (int i = 0; i < 5; i++) {
            Button btnVariant = new Button(getActivity());
            LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            btnVariant.setText(variants.get(i));
            btnVariant.setBackgroundDrawable(getResources().getDrawable(R.drawable.variant));
            btnVariant.setTextColor(getResources().getColorStateList(R.color.variant_color));
            if (i == rightVar) btnVariant.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TrainingActivity.main[TrainingActivity.MODE_TEST][1]++;
                    myNextFragmentListener.setTempResult(true, word, ((Button) view).getText().toString(),
                            FinalDB.getTranslation(id, colID));
                }
            });
            else btnVariant.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TrainingActivity.main[TrainingActivity.MODE_TEST][0]++;
                    myNextFragmentListener.setTempResult(false, word, variants.get(rightVar),
                            FinalDB.getTranslation(id, colID));
                }
            });
            llVariants.addView(btnVariant, lParams);
            if (i != 4) {
                View separator = new View(getActivity());
                lParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
                separator.setBackgroundDrawable(getResources().getDrawable(R.drawable.separator));
                llVariants.addView(separator, lParams);
            }
        }
        return view;
    }

    public void setNextFragmentListener(TrainingActivity.OnNextFragmentListener listener) {
        myNextFragmentListener = listener;
    }
}
