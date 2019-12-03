package com.liuguilin.iot_bt.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.liuguilin.iot_bt.R;
import com.liuguilin.iot_bt.model.BtListModel;

import java.util.List;


public class BtListAdapter extends RecyclerView.Adapter<BtListAdapter.ViewHolder> {

    private Context mContext;
    private List<BtListModel> mList;
    private LayoutInflater inflater;

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public BtListAdapter(Context mContext, List<BtListModel> mList) {
        this.mContext = mContext;
        this.mList = mList;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public BtListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.layout_bt_list_item, null);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(BtListAdapter.ViewHolder viewHolder, final int i) {
        BtListModel model = mList.get(i);
        viewHolder.tvName.setText(model.getName());
        viewHolder.tvAddress.setText(model.getAddress());

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.OnClick(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvName;
        private TextView tvAddress;

        public ViewHolder(View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tv_name);
            tvAddress = itemView.findViewById(R.id.tv_address);
        }
    }

    public interface  OnItemClickListener{
        void OnClick(int i);
    }
}
