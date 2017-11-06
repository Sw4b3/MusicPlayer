package musicplayer;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class functionLibrary implements LineListener {

    private int hourConversation = 60 * 60;
    private int minuteConversation = 60;
    private boolean playCompleted;
    private boolean playContinue;
    private boolean isStopped;
    private boolean isPaused;
    private boolean repeated;
    private Clip audioClip;

    public void load(String audioFilePath) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        File audioFile = new File(audioFilePath);
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
        AudioFormat format = audioStream.getFormat();
        DataLine.Info info = new DataLine.Info(Clip.class, format);
        audioClip = (Clip) AudioSystem.getLine(info);
        audioClip.addLineListener(this);
        audioClip.open(audioStream);
    }

    void play() throws IOException {
        playCompleted = false;
        isStopped = false;
        
                
        if (repeated) {
            audioClip.loop(1);
            audioClip.start();
        } else {
            audioClip.start();
        }
        
        while (!playCompleted) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                playContinue =false;
                if (isStopped) {
                    audioClip.stop();
                    break;
                }
                if (isPaused) {
                    audioClip.stop();
                } else {
                    audioClip.start();
                }
            }
        } 
        
        if (isStopped == false){
            playContinue=true;
           
        }
        playCompleted = true;
        audioClip.close();
    }

    public boolean getPlayComplete() {
        return playCompleted;
    }
    
     public boolean getPlayContinue() {
        return playContinue;
    }
    
    public void setRepeatTrue() {
        repeated=true;
    }
    public void setRepeatFalse() {
        repeated=false;
    }

    public void stop() {
        isStopped = true;
    }

    public void pause() {
        isPaused = true;
    }

    public void resume() {
        isPaused = false;
    }

    public Clip getAudioClip() {
        return audioClip;
    }

    public long getClipSecondLength() {
        return audioClip.getMicrosecondLength() / 1000000;
    }

    public String getLength() {
        String length = "";
        long hour = 0;
        long minute = 0;
        long seconds = audioClip.getMicrosecondLength() / 1000000;

        if (seconds >= hourConversation) {
            hour = seconds / hourConversation;
            length = String.format("%02d:", hour);
        } else {
            length += "00:";
        }

        minute = seconds - hour * hourConversation;
        if (minute >= minuteConversation) {
            minute = minute / minuteConversation;
            length += String.format("%02d:", minute);
        } else {
            minute = 0;
            length += "00:";
        }
        long second = seconds - hour * hourConversation - minute * minuteConversation;
        length += String.format("%02d", second);
        return length;
    }

    @Override
    public void update(LineEvent event) {
        LineEvent.Type type = event.getType();
        if (type == LineEvent.Type.STOP) {
            if (isStopped || !isPaused) {
                playCompleted = true;
            }
        }
    }

}
