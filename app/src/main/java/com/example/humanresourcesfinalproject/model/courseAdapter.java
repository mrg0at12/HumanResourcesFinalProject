package com.example.humanresourcesfinalproject.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.humanresourcesfinalproject.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class courseAdapter<p> extends ArrayAdapter<Course> {

    private final Context context;
    private final List<Course> courseList;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());


    public courseAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List<Course> objects) {
        super(context, resource, textViewResourceId, objects);
        this.context=context;
        this.courseList=objects;
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.courserow, parent, false);

            holder = new ViewHolder();
            holder.tvCourseName = convertView.findViewById(R.id.tvCourseName);
            holder.tvStartDate = convertView.findViewById(R.id.tvStartdaterow);
            holder.tvEndDate = convertView.findViewById(R.id.tvenddaterow);
            holder.tvPriceForPupil = convertView.findViewById(R.id.tvPriceForPupil);
            holder.tvPriceForTeacher = convertView.findViewById(R.id.tvPriceForTeacher);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Course course = courseList.get(position);

        // Set values to views
        holder.tvCourseName.setText(course.getCourseName());
        holder.tvStartDate.setText("Start: " + dateFormat.format(course.getStartDate()));
        holder.tvEndDate.setText("End: " + dateFormat.format(course.getEndDate()));
        holder.tvPriceForPupil.setText("Pupil: $" + course.getPricePupil());
        holder.tvPriceForTeacher.setText("Teacher: $" + course.getPriceTeach());

        return convertView;
    }

    static class ViewHolder {
        TextView tvCourseName;
        TextView tvStartDate;
        TextView tvEndDate;
        TextView tvPriceForPupil;
        TextView tvPriceForTeacher;
    }

}
