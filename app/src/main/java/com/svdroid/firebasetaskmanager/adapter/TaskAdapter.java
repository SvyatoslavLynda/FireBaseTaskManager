package com.svdroid.firebasetaskmanager.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.client.utilities.Pair;
import com.svdroid.firebasetaskmanager.App;
import com.svdroid.firebasetaskmanager.R;
import com.svdroid.firebasetaskmanager.model.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TViewHolder>
	implements ValueEventListener
{
	private Context mContext;
	private final List<Pair<String, Task>> mTasksData;
	private OnItemClickListener mOnClickListener;

	public TaskAdapter(Context context, OnItemClickListener listener)
	{
		mContext = context;
		mTasksData = new ArrayList<>();
		((App) ((Activity) context).getApplication()).getTasksFireBase().addValueEventListener(this);
		mOnClickListener = listener;
		setHasStableIds(true);
	}

	@Override
	public TViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		View view = LayoutInflater.from(mContext).inflate(R.layout.task_item, parent, false);
		return new TViewHolder(view);
	}

	@Override
	public void onBindViewHolder(TViewHolder holder, int position)
	{
		holder.set();
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public int getItemCount()
	{
		return mTasksData.size();
	}

	@Override
	public void onDataChange(DataSnapshot dataSnapshot)
	{
		mTasksData.clear();

		for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
			mTasksData.add(new Pair<>(snapshot.getKey(), snapshot.getValue(Task.class)));
		}

		notifyDataSetChanged();
	}

	@Override
	public void onCancelled(FirebaseError firebaseError)
	{
	}

	protected class TViewHolder extends RecyclerView.ViewHolder
	{
		private TextView mTitleExecuted;
		private TextView mTitle;
		private TextView mNote;
		private ImageView mDelete;

		public TViewHolder(View itemView)
		{
			super(itemView);
			mTitleExecuted = (TextView) itemView.findViewById(R.id.is_executed);
			mTitle = (TextView) itemView.findViewById(R.id.title);
			mNote = (TextView) itemView.findViewById(R.id.note);
			mDelete = (ImageView) itemView.findViewById(R.id.delete);
		}

		public void set()
		{
			Task task = mTasksData.get(getLayoutPosition()).getSecond();
			mTitleExecuted.setVisibility(task.isExecuted() ? View.VISIBLE : View.GONE);
			mTitle.setText(task.getTitle());
			mNote.setText(task.getNote());

			if (mOnClickListener != null) {
				addListener();
			}
		}

		private void addListener()
		{
			itemView.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					final int position = getLayoutPosition();
					mOnClickListener.onItemClick(position, mTasksData.get(position));
				}
			});

			mDelete.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					final int position = getLayoutPosition();
					((App) ((Activity) mContext).getApplication()).getTasksFireBase()
						.child(mTasksData.get(position).getFirst())
						.removeValue();
				}
			});
		}
	}

	public interface OnItemClickListener
	{
		void onItemClick(int position, Pair item);
	}
}
