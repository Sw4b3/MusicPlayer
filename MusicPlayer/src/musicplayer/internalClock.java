/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package musicplayer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.sound.sampled.Clip;
import javax.swing.JLabel;
import javax.swing.JSlider;


public class internalClock extends Thread {
	private DateFormat dateFormater = new SimpleDateFormat("HH:mm:ss");	
	private boolean isRunning = false;
	private boolean isPause = false;
	private boolean isReset = false;
	private long startTime;
	private long pauseTime;
	
	private JLabel start;
	private JSlider sliderTime;
	private Clip audioClip;
	
	public void setAudioClip(Clip audioClip) {
		this.audioClip = audioClip;
	}

	internalClock(JLabel start, JSlider sliderTime) {
		this.start =start;
		this.sliderTime = sliderTime;
              
	}
	
        @Override
	public void run() {
		isRunning = true;
		startTime = System.currentTimeMillis();
		
		while (isRunning) {
			try {   
				Thread.sleep(100);
				if (!isPause) {
					if (audioClip != null && audioClip.isRunning()) {
						start.setText(toTimeString());
						int currentSecond = (int) audioClip.getMicrosecondPosition() / 1000000; 
						sliderTime.setValue(currentSecond);
					}
				} else {
					pauseTime += 100;
				}
			} catch (InterruptedException ex) {
				if (isReset) {
					sliderTime.setValue(0);
					start.setText("00:00:00");
					isRunning = false;		
					break;
				}
			}
		}
	}
	
	
	void reset() {
		isReset = true;
		isRunning = false;
	}
	
	void pauseTimer() {
		isPause = true;
	}
	
	void resumeTimer() {
		isPause = false;
	}
	

	private String toTimeString() {
		long now = System.currentTimeMillis();
		Date current = new Date(now - startTime - pauseTime);
		dateFormater.setTimeZone(TimeZone.getTimeZone("GMT"));
		String timeCounter = dateFormater.format(current);
		return timeCounter;
	}
}