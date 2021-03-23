package kz.develop.lexicon;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import java.util.Locale;

/**
 * Created by Steve Fox on 19.08.2014.
 */
public class SpeechHelper {

    private Context myContext;
    private TextToSpeech tts;
    private boolean isInitialized = false, hasSpoken = false;

    public SpeechHelper(Context context) {
        this.myContext = context;
        Destroy();
        tts = new TextToSpeech(myContext, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                            result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(myContext, "This Language is not supported", Toast.LENGTH_LONG).show();
                        isInitialized = false;
                    } else isInitialized = true;
                } else {
                    isInitialized = false;
                    Toast.makeText(myContext, "Text-to-speech initilization failed!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void speakText(String text) {
        if (!isInitialized) return;
        tts.speak(text, TextToSpeech.QUEUE_ADD, null);
        hasSpoken = true;
    }

    public void Destroy() {
        if (tts != null) {
            if (hasSpoken) tts.stop();
            tts.shutdown();
        }
    }
}
