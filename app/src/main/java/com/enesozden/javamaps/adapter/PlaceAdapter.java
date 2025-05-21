package com.enesozden.javamaps.adapter;


import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import java.util.List;
import com.enesozden.javamaps.model.Place;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.enesozden.javamaps.databinding.RecyclerRowBinding;
import com.enesozden.javamaps.view.MapsActivity;


public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceHolder> {

    private List<Place> placeList;

    public PlaceAdapter(List<Place> placeList) {
        this.placeList = placeList;
    }

    @NonNull
    @Override
    public PlaceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new PlaceHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceHolder holder, int position) {
        // Veriyi TextView'e yazdır
        holder.binding.recyclerViewTextView.setText(placeList.get(position).name);

        // Satıra tıklanabilirlik ekle
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), MapsActivity.class);
            intent.putExtra("place", placeList.get(position)); // Seçilen veri gönderiliyor
            intent.putExtra("info", "old"); // Bilgi etiketi
            holder.itemView.getContext().startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return placeList.size();
    }

    public static class PlaceHolder extends RecyclerView.ViewHolder {
        RecyclerRowBinding binding;

        public PlaceHolder(RecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

