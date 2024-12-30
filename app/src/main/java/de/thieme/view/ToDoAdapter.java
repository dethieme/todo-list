package de.thieme.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.dieschnittstelle.mobile.android.skeleton.R;

import java.util.List;

import de.thieme.model.ToDo;
import de.thieme.util.ImageViewUtil;

public class ToDoAdapter extends ArrayAdapter<ToDo> {

    public ToDoAdapter(Context context, List<ToDo> todos) {
        super(context, 0, todos);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // Reuse convertView or inflate new view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_todo, null);
        }

        ToDo todo = getItem(position);

        // Data Binding
        CheckBox doneCheckBox = convertView.findViewById(R.id.todoIsDone);
        doneCheckBox.setChecked(todo.isDone());
        doneCheckBox.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            todo.setIsDone(isChecked);
        });

        TextView nameView = convertView.findViewById(R.id.todoName);
        nameView.setText(todo.getName());

        TextView expiryView = convertView.findViewById(R.id.todoExpiry);
        expiryView.setText(String.valueOf(todo.getExpiry()));

        ImageView favoriteImageView = convertView.findViewById(R.id.todoIsFavorite);
        favoriteImageView.setOnClickListener(view -> {
            todo.setIsFavourite(!todo.isFavourite());
            ImageViewUtil.setFavoriteIcon(favoriteImageView, todo);
        });
        ImageViewUtil.setFavoriteIcon(favoriteImageView, todo);

        return convertView;
    }
}
