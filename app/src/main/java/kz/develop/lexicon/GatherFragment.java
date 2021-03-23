package kz.develop.lexicon;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Steve Fox on 06.09.2014.
 */
public class GatherFragment extends Fragment {

    public static final String KEY_ID = "id";
    public static final String KEY_COLLECTION_ID = "collection_id";
    public static final String KEY_COUNT = "count";

    private static final String ATTRIBUTE_CHAR = "char";
    private static final String ATTRIBUTE_SELECTED = "selected";
    private static final String ATTRIBUTE_X = "x";
    private static final String ATTRIBUTE_Y = "y";

    private static final int DISAPPEAR = 0;
    private static final long DELAY_TIME = 3000;

    private int id, colID, count;
    private Button btnOK;
    private View separator;
    private TextView tvOrder;
    private ArrayList<Map<String, Object>> letters;
    private TrainingActivity.OnNextFragmentListener myNextFragmentListener;

    private Handler myHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DISAPPEAR:
                    Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.vanish);
                    tvOrder.startAnimation(anim);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public static GatherFragment getInstance(int id, int collection_id, int count) {
        GatherFragment fragment = new GatherFragment();
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

    private ArrayList<String> createAndShuffleArray(String data) {
        ArrayList<String> result = new ArrayList<String>();
        for (int i = 0; i < data.length(); i++)
            result.add(Character.toString(data.charAt(i)));
        final Random random = new Random();
        for (int i = 0; i < result.size(); i++) {
            int randomIndex = random.nextInt(result.size());
            String temp = result.get(i);
            result.set(i, result.get(randomIndex));
            result.set(randomIndex, temp);
        }
        return result;
    }

    private int getAvailableBoxIndex(ArrayList<Integer> data) {
        int result = -1;
        for (int i = 0; i < data.size(); i++)
            if (data.get(i) == -1) {
                result = i;
                break;
            }
        return result;
    }

    private void setOkVisiblity(ArrayList<Integer> data) {
        boolean visible = true;
        for (int i = 0; i < data.size(); i++)
            if (data.get(i) == -1) {
                visible = false;
                break;
            }
        if (visible) {
            btnOK.setVisibility(View.VISIBLE);
            separator.setVisibility(View.VISIBLE);
        } else {
            btnOK.setVisibility(View.GONE);
            separator.setVisibility(View.GONE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gather, container, false);
        tvOrder = (TextView) view.findViewById(R.id.tvOrder);
        Message msg = new Message();
        msg.what = DISAPPEAR;
        myHandler.sendMessageDelayed(msg, DELAY_TIME);
        HorizontalFlowLayout flowContainer = (HorizontalFlowLayout) view.findViewById(R.id.flowContainer);
        HorizontalFlowLayout flowLetters = (HorizontalFlowLayout) view.findViewById(R.id.flowLetters);
        final String word = FinalDB.getWordByID(id, colID);
        final String transl = FinalDB.getFirstWordTranslationByID(id, colID);
        TextView tvTranslation = (TextView) view.findViewById(R.id.tvTranslation);
        tvTranslation.setText(transl);
        letters = new ArrayList<Map<String, Object>>();
        ArrayList<String> temp = createAndShuffleArray(word);
        final ArrayList<View> boxes = new ArrayList<View>();
        final ArrayList<Integer> boxesLetterID = new ArrayList<Integer>();
        final ArrayList<View> llLetters = new ArrayList<View>();
        Random random = new Random();
        for (int i = 0; i < word.length(); i++) {
            View box = new View(getActivity());
            int boxColor = Color.argb(128, random.nextInt(255), random.nextInt(255), random.nextInt(255));
            box.setBackgroundColor(boxColor);
            LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(45, 70);
            LinearLayout llBox = new LinearLayout(getActivity());
            llBox.addView(box, lParams);
            llBox.setPadding(0, 5, 5, 0);
            llBox.setTag(i);
            llBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Integer tag = (Integer) view.getTag();
                    if (boxesLetterID.get(tag) == -1) return;
                    int letterID = boxesLetterID.get(tag);
                    View viewLetter = llLetters.get(letterID);
                    ObjectAnimator animX = ObjectAnimator.ofFloat(viewLetter, "translationX", 0);
                    ObjectAnimator animY = ObjectAnimator.ofFloat(viewLetter, "translationY", 0);
                    AnimatorSet animSetXY = new AnimatorSet();
                    animSetXY.playTogether(animX, animY);
                    animSetXY.start();
                    letters.get(letterID).put(ATTRIBUTE_SELECTED, false);
                    boxesLetterID.set(tag, -1);
                    setOkVisiblity(boxesLetterID);
                }
            });
            flowContainer.addView(llBox);
            boxes.add(llBox);
            boxesLetterID.add(-1);

            final TextView letter = new TextView(getActivity());
            letter.setText(temp.get(i));
            letter.setTextSize(20);
            letter.setGravity(Gravity.CENTER);
            letter.setTag(i);
            letter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Integer tag = (Integer) view.getTag();
                    if ((Boolean) letters.get(tag).get(ATTRIBUTE_SELECTED)) return;
                    View viewLetter = llLetters.get(tag);
                    final int boxIndex = getAvailableBoxIndex(boxesLetterID);
                    View viewBox = boxes.get(boxIndex);
                    int[] boxXY = new int[2];
                    viewBox.getLocationOnScreen(boxXY);
                    int[] letterXY = new int[2];
                    viewLetter.getLocationOnScreen(letterXY);
                    letters.get(tag).put(ATTRIBUTE_X, letterXY[0]);
                    letters.get(tag).put(ATTRIBUTE_Y, letterXY[1]);
                    ObjectAnimator animX = ObjectAnimator.ofFloat(viewLetter, "translationX", boxXY[0] - letterXY[0]);
                    ObjectAnimator animY = ObjectAnimator.ofFloat(viewLetter, "translationY", boxXY[1] - letterXY[1]);
                    AnimatorSet animSetXY = new AnimatorSet();
                    animSetXY.playTogether(animX, animY);
                    animSetXY.start();
                    letters.get(tag).put(ATTRIBUTE_SELECTED, true);
                    boxesLetterID.set(boxIndex, tag);
                    setOkVisiblity(boxesLetterID);
                }
            });
            LinearLayout llLetter = new LinearLayout(getActivity());
            llLetter.addView(letter, lParams);
            llLetter.setPadding(0, 5, 5, 0);
            flowLetters.addView(llLetter);
            llLetters.add(llLetter);
            Map<String, Object> m = new HashMap<String, Object>();
            m.put(ATTRIBUTE_CHAR, temp.get(i));
            m.put(ATTRIBUTE_SELECTED, false);
            letters.add(m);
        }
        btnOK = (Button) view.findViewById(R.id.btnOK);
        separator = view.findViewById(R.id.Separator2);
        setOkVisiblity(boxesLetterID);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userWord = "";
                for (int i = 0; i < boxesLetterID.size(); i++)
                    userWord += letters.get(boxesLetterID.get(i)).get(ATTRIBUTE_CHAR);
                if (userWord.equals(word)) {
                    TrainingActivity.main[TrainingActivity.MODE_GATHER][1]++;
                    myNextFragmentListener.setTempResult(true, word, transl,
                            FinalDB.getTranslation(id, colID));
                } else {
                    TrainingActivity.main[TrainingActivity.MODE_GATHER][0]++;
                    myNextFragmentListener.setTempResult(false, word, transl,
                            FinalDB.getTranslation(id, colID));
                }
            }
        });
        return view;
    }

    public void setNextFragmentListener(TrainingActivity.OnNextFragmentListener listener) {
        myNextFragmentListener = listener;
    }
}
