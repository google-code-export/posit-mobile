package hfoss.android.demos;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;

public class LeapingAndroids extends Activity {
	
	public static final boolean LEFT_ANDROID = true;
	public static final boolean RIGHT_ANDROID = false;
	public static final boolean BLANK_SPACE = true;
	public static final int SOLVED = 0;
	public static final int LEGAL = 1;
	public static final int ILLEGAL = 2;
	public static final long DELAY = 3000;
	private static final int INSTRUCTIONS_DIALOG = 0;
	
	private ImageAdapter mImageAdapter;
	private GridView mGridView;
	private int mNMoves = 0;
    private int mBlank = 3;
 
    /**
     *  References to the image files. (drawable resources)
     */
    private int[] mThumbIds;

    /**
     * Stores the state. The goal state is [RIGHT, RIGHT, RIGHT, BLANK, LEFT, LEFT, LEFT].
     */
    private boolean[] mState;
    
    /** 
     * Called when the activity is first created. Initialize the state
     *  and display the grid.
     */
    @Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);
	    initState();
	    displayGrid();
	}
	
    /** 
     * Called when the activity is resumed. Display the grid. 
     */
    @Override
   public void onResume() {
    	super.onResume();
    	displayGrid();
    }
    
    

    /** 
     * Saves the state. Called automatically when the keyboard is 
     *  is opened or the orientation changes. 
     * @param outState is a Bundle (associative array) where state is saved
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putIntArray("Grid", mThumbIds);
		outState.putBooleanArray("State", mState);
		outState.putInt("Blank", mBlank);
		outState.putInt("Moves", mNMoves);
		super.onSaveInstanceState(outState);
	}


    /** 
     * Restores the state when the orientation changes as a result of
     *  opening the keyboard. 
     * @param savedInstanceState is a Bundle (associative array) that contains the state
	 */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mThumbIds = savedInstanceState.getIntArray("Grid");
		mState = savedInstanceState.getBooleanArray("State");
		mBlank = savedInstanceState.getInt("Blank");
		mNMoves = savedInstanceState.getInt("Moves");
	}

	/**
     * Creates the App's menu items from a resource file.
     */
      @Override
      public boolean onCreateOptionsMenu(Menu menu) {
  		MenuInflater inflater = getMenuInflater();
  		inflater.inflate(R.menu.main, menu);
  		return true;
      }

      /**
       * onOptionsItemSelected() handles menu selections
       */
      @Override
      public boolean onOptionsItemSelected(MenuItem item) {
          switch (item.getItemId()) {
          case R.id.reset_menu_item:
          	  initState();
          	  displayGrid();
              return true;
          case R.id.instructions_menu_item:
  		      showDialog(INSTRUCTIONS_DIALOG);
              //setContentView(R.layout.directions);
              return true;
          }
          return super.onOptionsItemSelected(item);
      }    
 
      /**
       * Displays the instructions in a dialog.
       */
      @Override
      protected Dialog onCreateDialog(int id) {
          switch (id) {
          case INSTRUCTIONS_DIALOG:
          	return new AlertDialog.Builder(this)
              .setIcon(R.drawable.ic_menu_help)
              .setTitle(R.string.help_menu_item)
              .setMessage(R.string.instructions)
              .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int whichButton) {
                      // User clicked OK so do some stuff 
          				dialog.cancel();
          			}
              })
              .create();
          default:
          	return null;
          } // switch
      }
          
    private void displayGrid() {
        setContentView(R.layout.main);
	    mGridView = (GridView) findViewById(R.id.gridview);
        mImageAdapter = new ImageAdapter(this, mThumbIds);
        mGridView.setAdapter(mImageAdapter);
        mGridView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
            	doMove(position);
            }
        }); 
    }

    /**
     * Defines the initial state. The goal state is [RIGHT, RIGHT, RIGHT, BLANK, LEFT, LEFT, LEFT].
     * @return
     */
    private void initState() {
       	mThumbIds = new int[7];
    	mThumbIds[0]=R.drawable.android_64;
    	mThumbIds[1]=R.drawable.android_64;
    	mThumbIds[2]=R.drawable.android_64;
    	mThumbIds[3]=R.drawable.green;
    	mThumbIds[4]=R.drawable.android_r_64;
    	mThumbIds[5]=R.drawable.android_r_64;
    	mThumbIds[6]=R.drawable.android_r_64;
    	mState = new boolean[7];
    	mState[0] = LEFT_ANDROID;
    	mState[1] = LEFT_ANDROID;
    	mState[2] = LEFT_ANDROID;
    	mState[3] = BLANK_SPACE;
    	mState[4] = RIGHT_ANDROID;
    	mState[5] = RIGHT_ANDROID;
    	mState[6] = RIGHT_ANDROID;
    	mBlank = 3;
    	mNMoves = 0;
    }
    
    /**
     * Attempts to do a legal move by either moving an Android into the empty (blank)
     *  position or leaping over another Android into the blank position.  
     * @param position is the position of the Android being moved.
     */
    private void doMove(int position) {
 //   	 Toast.makeText(this, "Position= " + position, Toast.LENGTH_SHORT).show();
     	int result = swap(position);

    	if  (result == ILLEGAL) {
    		// Beep or something	
    		MediaPlayer mp = MediaPlayer.create(this, R.raw.ribbit);
    		mp.start(); 
    		mp.release();
       } else {
      		MediaPlayer mp = MediaPlayer.create(this, R.raw.frog_chirp);
       	    mp.start(); 
       	    mp.release();
       }
       ++mNMoves; 
       mGridView.setAdapter(mImageAdapter);  // Redisplay the grid
       
      if (isSolved()) {
      		MediaPlayer mp = MediaPlayer.create(this, R.raw.frogcelebrate);
      	    mp.start();
      	    celebrate();
        	initState();
      	    return;
       	}
    }
    
    /**
     * Performs an animation of the celebrating Android. It loads
     *  the ImageView that will host the animation and 
     *  sets its background to an animation XML.
     */
    private void celebrate() {
        setContentView(R.layout.gameover);
    	 // Load the ImageView that will host the animation and
    	 // set its background to our AnimationDrawable XML resource.
    	 ImageView img = (ImageView)findViewById(R.id.simple_anim);
    	 if (img != null) {
    		 img.setBackgroundResource(R.drawable.animation);
    		 
    	 MyAnimationRoutine mar = new MyAnimationRoutine();
    	 MyAnimationRoutine2 mar2 = new MyAnimationRoutine2();
    	        
    	 Timer t = new Timer(false);
    	 t.schedule(mar, 100);
    	 Timer t2 = new Timer(false);
    	 t2.schedule(mar2, 10000);    		 
    	 }
    }
    
    /**
     * Swaps the image at the selected position with the mBlank, the position of the blank
     * @param position is the selected position
     */
    public int swap(int position) {
    	if (isLegalSwap(position)) {
    		Integer temp = mThumbIds[position];
    		mThumbIds[position] = mThumbIds[mBlank];
    		mThumbIds[mBlank] = temp;
    		boolean b = mState[position];
    		mState[position] = mState[mBlank];
    		mState[mBlank] = b;
    		mBlank = position;    // Remember new position of the blank
     		return this.LEGAL;	
    	}
    	else {
    		return this.ILLEGAL;
    	}
    }    
    
    /**
     * isSolved() checks whether user has reached the goal state. The
     *  goal state is [RIGHT_ANDROID, RIGHT, RIGHT, BLANK, LEFT, LEFT, LEFT_ANDROID],
     *  where RIGHT=FALSE and LEFT=TRUE. 
     * @return true of false
     */
    public boolean isSolved(){
    	return !mState[0] && !mState[1] && !mState[2] && 
    	mState[4] && mState[5] && mState[6] && mBlank == 3;
    }
    
    
    /**
     * isLegalSwap() returns true if the proposed move is legal.  A L Android must
     *  always move right.  A R Android must always move left.  Then can jump over
     *  at most one other Android.
     * @param position is the position of the selected Android
     * @return  false if the move is illegal
     */
        private boolean isLegalSwap(int position) {
        	return position != mBlank && Math.abs(position - mBlank) <= 2 &&
        		((mState[position] && position < mBlank)
        				|| (!mState[position] && position > mBlank));
    	}
        
        
        
/**
 * These classes are used to create timers for the animation.  
 * @see http://www.twintechs.com/blog/?p=35        
 *
 */
        class MyAnimationRoutine extends TimerTask
        {
        	
        	public void run()
        	{
            	ImageView img = (ImageView)findViewById(R.id.simple_anim);
                // Get the background, which has been compiled to an AnimationDrawable object.
                AnimationDrawable frameAnimation = (AnimationDrawable) img.getBackground();

                // Start the animation (looped playback by default).
                frameAnimation.start();
        	}
        }


        class MyAnimationRoutine2 extends TimerTask
        {
        	public void run()
        	{
            	ImageView img = (ImageView)findViewById(R.id.simple_anim);
                // Get the background, which has been compiled to an AnimationDrawable object.
                AnimationDrawable frameAnimation = (AnimationDrawable) img.getBackground();

                // stop the animation (looped playback by default).
                frameAnimation.stop();
        	}
        }        
        
}