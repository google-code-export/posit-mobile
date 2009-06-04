/**
 * 
 */
package org.hfoss.posit;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

/**
 * @author rmorelli
 *
 */
public class MyItemizedOverlay extends ItemizedOverlay {
		private static final String TAG = "ItemizedOverlay";

        private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
        private Context mContext;
        
        /**
         * @param defaultMarker
         */
        public MyItemizedOverlay(Drawable defaultMarker, Context c) {
                super(boundCenterBottom(defaultMarker));
                mContext = c;
                // TODO Auto-generated constructor stub
        }

        /* (non-Javadoc)
         * @see com.google.android.maps.ItemizedOverlay#createItem(int)
         */
        @Override
        protected OverlayItem createItem(int i) {
          return mOverlays.get(i);
        }

        /* (non-Javadoc)
         * @see com.google.android.maps.ItemizedOverlay#size()
         */
        @Override
        public int size() {
                return mOverlays.size();
        }

        public void addOverlay(OverlayItem overlay) {
            mOverlays.add(overlay);
            populate();
        }
        
        /**
         * Called when the user clicks on one of the Find icons
         *   in the map. It shows a description of the Find
         * @param pIndex is the Find's index in the ArrayList
         */
        @Override
        protected boolean onTap(int pIndex) {
                // show the description
//               Toast.makeText(mContext, mOverlays.get(pIndex).getSnippet(),
//                        Toast.LENGTH_LONG).show();
                Intent intent = new Intent(mContext, FindActivity.class);
                intent.setAction(Intent.ACTION_EDIT);
                long itemId = Long.parseLong(mOverlays.get(pIndex).getTitle());
    			Log.i(TAG, "rowID = " + itemId);

                intent.putExtra(MyDBHelper.KEY_ID, itemId); // Pass the RowID to FindActivity
                mContext.startActivity(intent);
                return true;
        }
}
