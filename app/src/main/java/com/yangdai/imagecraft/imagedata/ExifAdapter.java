package com.yangdai.imagecraft.imagedata;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.yangdai.imagecraft.R;
import com.yangdai.imagecraft.utils.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ExifAdapter extends RecyclerView.Adapter<ExifAdapter.ExifViewHolder> {

    private final List<ExifItem> newExifInfoList;
    private final List<ExifItem> oldExifInfoList = new ArrayList<>();
    private final WeakReference<Activity> weakReference;

    public ExifAdapter(List<ExifItem> newExifInfoList, Activity activity) {
        newExifInfoList.sort((item1, item2) -> {
            String name1 = item1.name();
            String name2 = item2.name();
            return name1.compareToIgnoreCase(name2);
        });
        this.newExifInfoList = newExifInfoList;
        oldExifInfoList.addAll(newExifInfoList);
        weakReference = new WeakReference<>(activity);
    }

    public List<ExifItem> getNewExifInfoList() {
        return newExifInfoList;
    }

    @NonNull
    @Override
    public ExifViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ExifViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExifViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ExifItem exifItem = newExifInfoList.get(holder.getBindingAdapterPosition());
        holder.bind(exifItem);
        holder.contentTextView.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                String newData = textView.getText().toString();
                ExifItem item = new ExifItem(newExifInfoList.get(holder.getBindingAdapterPosition()).name(), newData);
                newExifInfoList.set(holder.getBindingAdapterPosition(), item);
                updateList();
                Utils.closeKeyboard(weakReference.get());
                holder.contentTextView.clearFocus();
                return true;
            }
            return false;
        });
        holder.removeButton.setOnClickListener(v -> removeItem(holder.getBindingAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return newExifInfoList.size();
    }

    public void removeItem(int position) {
        newExifInfoList.remove(position);
        updateList();
    }

    public void addItem(ExifItem exifItem) {
        newExifInfoList.add(exifItem);
        sortItemsByName();
    }

    public void updateList() {
        ExifDiffCallback diffCallback = new ExifDiffCallback(oldExifInfoList, newExifInfoList);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        oldExifInfoList.clear();
        oldExifInfoList.addAll(newExifInfoList);
        diffResult.dispatchUpdatesTo(this);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void sortItemsByName() {
        newExifInfoList.sort((item1, item2) -> {
            String name1 = item1.name();
            String name2 = item2.name();
            return name1.compareToIgnoreCase(name2);
        });
        updateList();
    }

    static class ExifViewHolder extends RecyclerView.ViewHolder {
        final TextView titleTextView;
        final EditText contentTextView;
        final ImageView removeButton;

        ExifViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.title);
            contentTextView = itemView.findViewById(R.id.content);
            removeButton = itemView.findViewById(R.id.remove);
        }

        public void bind(ExifItem exifItem) {
            titleTextView.setText(exifItem.name());
            contentTextView.setText(exifItem.data());
        }
    }

    private static class ExifDiffCallback extends DiffUtil.Callback {
        private final List<ExifItem> oldList;
        private final List<ExifItem> newList;

        ExifDiffCallback(List<ExifItem> oldList, List<ExifItem> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        //老数据集size
        @Override
        public int getOldListSize() {
            return oldList != null ? oldList.size() : 0;
        }

        //新数据集size
        @Override
        public int getNewListSize() {
            return newList != null ? newList.size() : 0;
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).name().equals(newList.get(newItemPosition).name());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).data().equals(newList.get(newItemPosition).data());
        }
    }
}