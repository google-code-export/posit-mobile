package org.hfoss.posit;
import org.hfoss.posit.db.PositData;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Menu.Item;
import android.view.View.OnClickListener;
import android.widget.Button;

public class PositMain extends Activity {
	public PositMain(){

	}
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);
        Button newFind = (Button)findViewById(R.id.NewFind);
        newFind.setOnClickListener (new OnClickListener(){
    	public void onClick(View v){
    		openNewFind();
    	}});

        Button listFind = (Button) findViewById (R.id.ListFinds);
        listFind.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		openListFind();
        	}
        });
    }

    private void openNewFind(){
    	Intent i = new Intent(this, Mapper.class);
    	i.setData(PositData.Photos.CONTENT_URI);
    	startSubActivity(i,0);
    	
    }

    private void openListFind(){
    	Intent i = new Intent(this, ListFind.class);
    	startSubActivity(i,0);
    }
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, Menu.FIRST, "test");
		return true;
		
	}
	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.Menu.Item)
	 */
	@Override
	public boolean onOptionsItemSelected(Item item) {
		switch (item.getId()){
		case Menu.FIRST:
			openNewFind();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
}