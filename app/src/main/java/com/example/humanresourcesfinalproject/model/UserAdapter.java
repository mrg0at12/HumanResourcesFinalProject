package com.example.humanresourcesfinalproject.model;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.humanresourcesfinalproject.R;
import com.example.humanresourcesfinalproject.UserInfo;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends ArrayAdapter<User> {

    public interface OnUserInteractionListener {
        void onUserClick(User user);
        void onUserLongClick(User user);
    }

    private final Context context;
    private List<User> userList;
    private List<User> filteredUserList;
    private OnUserInteractionListener interactionListener;

    public UserAdapter(@NonNull Context context, int resource, @NonNull List<User> objects) {
        super(context, 0, objects);
        this.context = context;
        this.userList = new ArrayList<>(objects);
        this.filteredUserList = new ArrayList<>(objects);
    }

    public void setOnUserInteractionListener(OnUserInteractionListener listener) {
        this.interactionListener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.userrow, parent, false);

            holder = new ViewHolder();
            holder.rowUserName = convertView.findViewById(R.id.rowUserName);
            holder.rowUserPhone = convertView.findViewById(R.id.rowUserPhone);
            holder.rowUserID = convertView.findViewById(R.id.rowUserID);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        User user = filteredUserList.get(position);
        holder.rowUserName.setText("👤 " + user.getFname() + " " + user.getLname());
        holder.rowUserPhone.setText("📞 " + user.getPhone());
        holder.rowUserID.setText("🆔 " + user.getKidId());

        // Set both click and long click listeners
        if (interactionListener != null) {
            convertView.setOnClickListener(v -> interactionListener.onUserClick(user));
            convertView.setOnLongClickListener(v -> {
                interactionListener.onUserLongClick(user);
                return true;
            });
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return filteredUserList.size();
    }

    @Nullable
    @Override
    public User getItem(int position) {
        return filteredUserList.get(position);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<User> filteredList = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(userList);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    for (User user : userList) {
                        if (user.getFname().toLowerCase().contains(filterPattern) ||
                                user.getLname().toLowerCase().contains(filterPattern) ||
                                user.getPhone().contains(filterPattern) ||
                                user.getKidId().contains(filterPattern)) {
                            filteredList.add(user);
                        }
                    }
                }

                results.values = filteredList;
                results.count = filteredList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredUserList.clear();
                filteredUserList.addAll((List<User>) results.values);
                notifyDataSetChanged();
            }
        };
    }

    public void updateList(List<User> newList) {
        userList.clear();
        userList.addAll(newList);
        filteredUserList.clear();
        filteredUserList.addAll(newList);
        notifyDataSetChanged();
    }

    static class ViewHolder {
        TextView rowUserName;
        TextView rowUserPhone;
        TextView rowUserID;
    }
}
