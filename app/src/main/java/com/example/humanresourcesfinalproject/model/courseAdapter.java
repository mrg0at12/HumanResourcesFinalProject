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



    public courseAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List<Course> objects) {
        super(context, resource, textViewResourceId, objects);
        this.context=context;
        this.courseList=objects;
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {


        TextView tvCourseName;
        TextView tvStartDate;
        TextView tvEndDate;
        TextView tvPriceForPupil;
        TextView tvPriceForTeacher;

          SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());


            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.courserow, parent, false);




            tvStartDate = convertView.findViewById(R.id.tvStartdaterow);
            tvEndDate = convertView.findViewById(R.id.tvenddaterow);
            tvPriceForPupil = convertView.findViewById(R.id.tvPriceForPupil);
          tvPriceForTeacher = convertView.findViewById(R.id.tvPriceForTeacher);
           tvCourseName = convertView.findViewById(R.id.tvCourseName);




        Course course = courseList.get(position);

        // Set values to views
        tvCourseName.setText(course.getCourseName());
        if(course.getStartDate()!=null)
                tvStartDate.setText("Start: " + dateFormat.format(course.getStartDate()));
        if(course.getEndDate()!=null)
                         tvEndDate.setText("End: " + dateFormat.format(course.getEndDate()));
        tvPriceForPupil.setText("Pupil: $" + course.getPricePupil());
        tvPriceForTeacher.setText("Teacher: $" + course.getPriceTeach());

        return convertView;
    }


}
