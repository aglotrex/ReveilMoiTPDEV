package com.trigg.alarmclock;

import android.content.Context;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;

/**
 * Class contenant les elements necessaires
 * a l'utilisation du tts pico d'android
 */
public class Speaker implements OnInitListener {

    private TextToSpeech tts;
    private boolean ready = false;
    private boolean allowed = true;

    public Speaker(Context context){
        tts = new TextToSpeech(context, this);
    }

    public boolean isAllowed(){
        return allowed;
    }

    public void allow(boolean allowed){
        this.allowed = allowed;
    }

    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS){
            tts.setLanguage(Locale.FRENCH);
            ready = true;
        } else {
            ready = false;
        }
    }

    public void speak(String text){
        if(ready && allowed) {
            HashMap<String, String> hash = new HashMap<String,String>();
            hash.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_NOTIFICATION));
            tts.speak(text, TextToSpeech.QUEUE_ADD, hash);
        }
    }

    public void pause(int duration){
        tts.playSilence(duration, TextToSpeech.QUEUE_ADD, null);
    }

    public void onDestroy() {
        if(tts != null) {
            if (tts.isSpeaking())
                tts.stop();
            tts.shutdown();
        }
    }
}
