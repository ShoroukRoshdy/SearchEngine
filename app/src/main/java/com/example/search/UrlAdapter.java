package com.example.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class UrlAdapter extends RecyclerView.Adapter<UrlAdapter.ExampleViewHolder> {
    private ArrayList<UrlItem> thecontent ;

    public static class  ExampleViewHolder extends RecyclerView.ViewHolder

    {
        public TextView text1;
        public TextView text2;

        public ExampleViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            text1=itemView.findViewById(R.id.headline);
            text2=itemView.findViewById(R.id.urlofstite);
        }
    }

    @NotNull

    public UrlAdapter(ArrayList<UrlItem> list)
    {
        thecontent=list;
    }

    @Override
    public ExampleViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.card,parent,false);
        ExampleViewHolder hold =new ExampleViewHolder(v);
        return hold;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull UrlAdapter.ExampleViewHolder holder, int position)
    {
        UrlItem currentItem=thecontent.get(position);

        holder.text1.setText(currentItem.getTitle());
        holder.text2.setText(currentItem.getText());
    }


    @Override
    public int getItemCount() {
        return thecontent.size();
    }
}
