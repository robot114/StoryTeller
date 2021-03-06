package com.zsm.storyteller.ui;

import java.util.List;

import android.R.color;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.zsm.storyteller.R;
import com.zsm.storyteller.play.PlayController;

class MediaInfoListAdapter extends BaseExpandableListAdapter {

	private List<Uri> data;
	private Context context;
	private PlayController player;
	
	private LayoutInflater inflater;
	private ExpandableListView listView;

	MediaInfoListAdapter( Context context, ExpandableListView listView ) {
		
		this.context = context;
		this.listView = listView;
		
		inflater
			= (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void setPlayer( PlayController player ) {
		this.player = player;
	}

	public void setData( List<Uri> data ) {
		this.data = data;
	}
	
	@Override
	public int getGroupCount() {
		return data == null ? 0 : data.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return 1;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return getMediaItem(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return getMediaItem(groupPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(final int groupPosition, final boolean isExpanded,
							 View convertView, ViewGroup parent) {
		View view = convertView;
	    TextView viewText = null;
	    ImageView viewImage = null;
	    if(view == null) {
	    	view = inflater.inflate( R.layout.media_item, (ViewGroup)null );
	    	viewText = initTextView(view);
		    viewImage = initImageView(groupPosition, isExpanded, view);
		    
		    Object[] tags = new Object[] { viewText, viewImage };
		    view.setTag( tags );
	    } else {
	    	Object[] tags = (Object[]) view.getTag();
	    	viewText = (TextView) tags[0];
	    	viewImage = (ImageView) tags[1];
	    	updateImageTag(groupPosition, isExpanded, viewImage);
	    }
	    viewText.setText(getMediaItem(groupPosition).getLastPathSegment());
	    viewText.setTag( groupPosition );
	    if( isExpanded) {
	    	viewImage.setImageResource( R.drawable.to_collapse );
	    } else {
	    	viewImage.setImageResource( R.drawable.to_exapand );
	    }
	    
	    System.out.println( listView.getSelectedItemPosition() + ", " + listView.getSelectedPosition() + ", " + groupPosition );
	    if( listView.getSelectedItemPosition() == groupPosition ) {
	        view.setBackgroundResource(color.darker_gray);
	    }else{
	        view.setBackgroundColor(color.transparent);
	    }
	    
	    return view;
	}

	private void updateImageTag(int groupPosition, boolean isExpanded,
								ImageView viewImage) {
		
		Object[] imageTag = (Object[]) viewImage.getTag();
		imageTag[0] = isExpanded;
		imageTag[1] = groupPosition;
	}

	private ImageView initImageView(final int groupPosition,
									final boolean isExpanded, View view) {
		
		ImageView viewImage;
		viewImage = (ImageView)view.findViewById( R.id.imageViewExpand );
		viewImage.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				Object[] tag = (Object[]) v.getTag();
				boolean expanded = !((boolean) tag[0]);
				int position = (int) tag[1];
				if( expanded ) {
					listView.expandGroup(position);
				} else {
					listView.collapseGroup(position);
				}
				tag[0] = expanded;
			}
		} );
		Object[] imageTag = new Object[]{ isExpanded, groupPosition };
		viewImage.setTag(imageTag);
		return viewImage;
	}

	private TextView initTextView(View view) {
		TextView viewText;
		viewText = (TextView)view.findViewById( R.id.textViewItem );
		viewText.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				player.play( data.get((int) v.getTag()), 0 );
			}
		} );
		return viewText;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
							 boolean isLastChild, View convertView,
							 ViewGroup parent) {
		
		MediaInfoView view = (MediaInfoView)convertView;
		if( view == null ) {
			view = new MediaInfoView(context, true);
		}
		
		view.setDataSource( getMediaItem(groupPosition) );
		return view;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}
	
	private Uri getMediaItem(int groupPosition) {
		return data == null ? null : data.get(groupPosition);
	}

	public int getPositionOf(Uri uri) {
		return data.indexOf(uri);
	}

}
