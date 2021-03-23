package kz.develop.lexicon;

import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Rav_4 on 08.07.2014.
 */
public class WordFragment extends Fragment {

    static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";

    int pageNumber;
    TextView tvWord, tvTranscription, tvTranslation, tvPage;
    String word;

    static WordFragment newInstance(int page) {
        WordFragment wordFragment = new WordFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARGUMENT_PAGE_NUMBER, page);
        wordFragment.setArguments(arguments);
        return wordFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageNumber = getArguments().getInt(ARGUMENT_PAGE_NUMBER);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.word_fragment, null);
        tvWord = (TextView) view.findViewById(R.id.tvWord);
        tvTranscription = (TextView) view.findViewById(R.id.tvTranscription);
        Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Arial.ttf");
        tvTranscription.setTypeface(tf);
        tvTranslation = (TextView) view.findViewById(R.id.tvTranslation);
        tvPage = (TextView) view.findViewById(R.id.tvPage);
        Cursor c = WordActivity.cursor;
        pageNumber++;
        tvPage.setText(String.valueOf(pageNumber)+" "+getResources().getString(R.string.tvOf)+" "+String.valueOf(c.getCount()));
        if (c.moveToFirst()) {
            int j = 0;
            do {
                j++;
                if (j == pageNumber) {
                    final int wordID = c.getInt(c.getColumnIndex(DB.COLUMN_ID_LIST));
                    final int col_id = c.getInt(c.getColumnIndex(DB.COLUMN_COLLECTION_ID));
                    Cursor c2 = FinalDB.getWordColumnsByID(wordID,col_id);
                    c2.moveToFirst();
                    word = c2.getString(c2.getColumnIndex(FinalDB.COLUMN_WORD));
                    String transcription = FinalDB.getWordTranscription(c2, col_id);
                    if (!transcription.equals("")) tvTranscription.setText("[" + transcription + "]");
                    String partofSpeech = FinalDB.getPartOfSpeech(c2, col_id);
                    if (partofSpeech.equals(FinalDB.PART_VERB)) tvWord.setText("to " + word);
                     else tvWord.setText(word);
                    tvTranslation.setText(FinalDB.getTranslation(wordID, col_id));
                }
            } while (c.moveToNext());
        }

        Button btnSound = (Button) view.findViewById(R.id.btnSound);
        btnSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                  MainActivity.speechHelper.speakText(word);
            }
        });
        return view;
    }
}
