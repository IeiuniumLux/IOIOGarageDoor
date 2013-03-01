package ioio.garagedoor;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

import java.util.Timer;
import java.util.TimerTask;

import ioio.garagedoor.garagedoor.R;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class GarageDoorActivity extends IOIOActivity {

	private static final int DOOR_PIN = 44;
	private static final int PULSE_PERIOD = 200;

	private ImageView mDoorButton;
	private boolean mDoorState = false;
	private Timer mTimer = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		this.mDoorButton = (ImageView) findViewById(R.id.btn1);
		this.mDoorButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				vib.vibrate(45);
				pulsePin(DOOR_PIN);
			}
		});
	}
	
	/**
	 * This is the thread on which all the IOIO activity happens. It will be run
	 * every time the application is resumed and aborted when it is paused. The
	 * method setup() will be called right after a connection with the IOIO has
	 * been established (which might happen several times!). Then, loop() will
	 * be called repetitively until the IOIO gets disconnected.
	 */
	class Looper extends BaseIOIOLooper {
		private DigitalOutput mDoorPin;

		/**
		 * Called every time a connection with IOIO has been established.
		 * Typically used to open pins.
		 * 
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 * 
		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#setup()
		 */
		@Override
		protected void setup() throws ConnectionLostException {
			mDoorPin = ioio_.openDigitalOutput(DOOR_PIN, false);
		}

		/**
		 * Called repetitively while the IOIO is connected.
		 * 
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 * 
		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#loop()
		 */
		@Override
		public void loop() throws ConnectionLostException {
			mDoorPin.write(mDoorState);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
	}

	/**
	 * A method to create our IOIO thread.
	 * 
	 * @see ioio.lib.util.AbstractIOIOActivity#createIOIOThread()
	 */
	@Override
	protected IOIOLooper createIOIOLooper() {
		return new Looper();
	}

	private void turnPinOn(int pin) {
		if (pin == DOOR_PIN) {
			mDoorState = true;
		}
	}

	private void turnPinOff(int pin) {
		if (pin == DOOR_PIN) {
			mDoorState = false;
		}
	}

	private void pulsePin(int pin) {
		turnPinOn(pin);
		mTimer = new Timer();
		mTimer.schedule(new PinOffTask(pin), PULSE_PERIOD);
	}

	private class PinOffTask extends TimerTask {
		int pin;

		public PinOffTask(int pin) {
			super();
			this.pin = pin;
		}

		public void run() {
			turnPinOff(pin);
		}
	}
}