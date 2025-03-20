package com.example.humanresourcesfinalproject.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.humanresourcesfinalproject.R;

import java.util.List;

public class UserAdapter extends ArrayAdapter<User> {

    private final Context context;
    private final List<User> userList;

    public UserAdapter(@NonNull Context context, int resource, @NonNull List<User> objects) {
        super(context, resource, objects);
        this.context = context;
        this.userList = objects;
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

        User user = userList.get(position);

        // Set values to views with emojis 🚀
        holder.rowUserName.setText("👤 " + user.getFname() + " " + user.getLname());
        holder.rowUserPhone.setText("📞 " + user.getPhone());
        holder.rowUserID.setText("🆔 " + user.getKidId());

        return convertView;
    }

    static class ViewHolder {
        TextView rowUserName;
        TextView rowUserPhone;
        TextView rowUserID;
    }
}
