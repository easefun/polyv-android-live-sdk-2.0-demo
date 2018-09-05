package com.easefun.polyvsdk.live.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.easefun.polyvsdk.live.R;
import com.easefun.polyvsdk.live.vo.PolyvLiveBitrateVO;
import com.easefun.polyvsdk.live.vo.PolyvLiveDefinitionVO;

import java.util.List;

public class PolyvBitAdapter extends RecyclerView.Adapter<PolyvBitAdapter.BitViewHolder> {
    private List<PolyvLiveDefinitionVO> definitions;
    private Context context;
    private View.OnClickListener onClickListener;

    public PolyvBitAdapter(PolyvLiveBitrateVO polyvLiveBitrateVO, Context context) {
        this.context = context;
        if(polyvLiveBitrateVO != null){
            definitions = polyvLiveBitrateVO.getDefinitions();
        }

    }

    @Override
    public BitViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View child = View.inflate(context, R.layout.polyv_bit_item, null);
        return new BitViewHolder(child);
    }

    @Override
    public void onBindViewHolder(BitViewHolder holder, int position) {

        if (definitions == null || definitions.size() == 0) {
            return;
        }

        if (position > definitions.size()) {
            holder.setBitName(context.getString(R.string.unknow));
            return;
        }

        holder.setBitName(definitions.get(position).definition);
        holder.bitName.setTag(position);
        holder.bitName.setSelected(definitions.get(position).hasSelected);

        if(onClickListener != null){
            holder.bitName.setOnClickListener(onClickListener);
        }
    }

    @Override
    public int getItemCount() {
        if(definitions ==  null || definitions.isEmpty()){
            return 0;
        }
        return definitions.size();
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void updateBitrates(PolyvLiveBitrateVO polyvLiveBitrateVO){
        if(polyvLiveBitrateVO != null){
            definitions = polyvLiveBitrateVO.getDefinitions();
        }
    }
    class BitViewHolder extends RecyclerView.ViewHolder {

        public TextView bitName;

        public BitViewHolder(View itemView) {
            super(itemView);
            bitName = (TextView) itemView.findViewById(R.id.live_bit_name);
        }

        public void setBitName(String name) {
            bitName.setText(name);
        }
    }
}
