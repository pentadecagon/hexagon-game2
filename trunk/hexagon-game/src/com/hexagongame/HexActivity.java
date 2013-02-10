package com.hexagongame;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class HexActivity extends Activity {

	//work around for non functioning FLAG_ACTIVITY_CLEAR_TASK flag in Android 2.3.3
	public static HexActivity instance = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.d("hex", "HexActivity.onCreate called");
        super.onCreate(savedInstanceState);
        
        //work around for non functioning FLAG_ACTIVITY_CLEAR_TASK flag in Android 2.3.3
        instance = this;
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    	initializeLayout();
    }
    
    private void initializeLayout()
    {
    	setContentView(R.layout.main);	

        UiView uiView = (UiView) findViewById(R.id.boardview);

		//add a view for the "phone is thinking" message (for "play-against-phone" mode), which will be hidden initially
		LinearLayout phoneThinkingNotification = (LinearLayout) findViewById(R.id.phonethinkingtext);
		uiView.setPhoneThinkingNotification(phoneThinkingNotification);
		
		setBackground(uiView);
		
		ImageView [] imageViews = {(ImageView) findViewById(R.id.tileimage0), (ImageView) findViewById(R.id.tileimage1)};
		uiView.setTurnImageViews(imageViews);
    }
    
    private void setBackground(UiView uiView)
    {
		if (ChooseBoardActivity.config.boardShape == 1)
		{
			uiView.setBackgroundResource(R.drawable.square_background);
		} else
		{
			switch (ChooseBoardActivity.config.boardSize)
			{
				case 0:
					uiView.setBackgroundResource(R.drawable.hex_background_1);
					break;
				case 1:
					uiView.setBackgroundResource(R.drawable.hex_background_2);
					break;
				case 2:
					uiView.setBackgroundResource(R.drawable.hex_background_3);
					break;
			}			
		}	
    }
}