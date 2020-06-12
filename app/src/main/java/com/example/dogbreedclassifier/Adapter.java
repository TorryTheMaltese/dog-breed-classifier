package com.example.dogbreedclassifier;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.daimajia.swipe.SwipeLayout;

import java.util.ArrayList;

public class Adapter extends androidx.recyclerview.widget.RecyclerView.Adapter<Adapter.ViewHolder> {
    private ArrayList<RecyclerView> mData = null;

    Adapter(ArrayList<RecyclerView> list) {
        mData = list;
    }

    @NonNull
    @Override
    public Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.dog_list, parent, false);
        Adapter.ViewHolder vh = new Adapter.ViewHolder(view);

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final RecyclerView item = mData.get(position);

        holder.image.setImageURI(item.getDog_image());
        holder.dogName.setText(item.getDog_name());
        holder.dogAge.setText(item.getDog_age());
        holder.dogWeight.setText(item.getDog_weight());

        holder.swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
        holder.swipeLayout.addDrag(SwipeLayout.DragEdge.Right, holder.bottom_wrapper);
        holder.swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onStartOpen(SwipeLayout layout) {

            }

            @Override
            public void onOpen(SwipeLayout layout) {

            }

            @Override
            public void onStartClose(SwipeLayout layout) {

            }

            @Override
            public void onClose(SwipeLayout layout) {

            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {

            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {

            }
        });

//        holder.imageBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.e("TEST", "IMAGE BTN CLICKED !");
//                mData.remove(position);
//                notifyItemRemoved(position);
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void deleteItem(int position){

    }


    public class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        ImageView image;
        TextView dogName, dogAge, dogWeight;
        SwipeLayout swipeLayout;
        FrameLayout bottom_wrapper, dog_info_card;
        ImageButton imageBtn;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.saved_dog_image);
            dogName = itemView.findViewById(R.id.saved_dog_name);
            dogAge = itemView.findViewById(R.id.saved_dog_age);
            dogWeight = itemView.findViewById(R.id.saved_dog_weight);

            imageBtn = itemView.findViewById(R.id.imagebtn);
            swipeLayout = (SwipeLayout) itemView.findViewById(R.id.sample1);
            bottom_wrapper = itemView.findViewById(R.id.bottom_wrapper);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e("TEST","test");
                }
            });
        }
    }
}
