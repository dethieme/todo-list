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

public class ToDoAdapter extends ArrayAdapter<ToDo> {

    public ToDoAdapter(Context context, List<ToDo> todos) {
        super(context, 0, todos);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // Reuse convertView or inflate new view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.todo_item, parent, false);
        }

        ToDo toDo = getItem(position);

        // Find views
        CheckBox doneCheckBox = convertView.findViewById(R.id.todoDoneCheckBox);
        TextView nameView = convertView.findViewById(R.id.todoName);
        TextView expiryView = convertView.findViewById(R.id.todoExpiry);
        ImageView favoriteImageView = convertView.findViewById(R.id.todoFavouriteIcon);

        // Set data in views
        nameView.setText(toDo.getName());
        expiryView.setText(String.valueOf(toDo.getExpiry()));
        doneCheckBox.setChecked(toDo.isDone());

        favoriteImageView.setOnClickListener(view -> {
            toDo.setFavourite(!toDo.isFavourite());

            if (toDo.isFavourite()) {
                favoriteImageView.setImageResource(R.drawable.baseline_star_24);
            } else {
                favoriteImageView.setImageResource(R.drawable.baseline_star_border_24);
            }
        });

        // Set favourite icon
        if (toDo.isFavourite()) {
            favoriteImageView.setImageResource(R.drawable.baseline_star_24);
        } else {
            favoriteImageView.setImageResource(R.drawable.baseline_star_border_24);
        }

        return convertView;
    }
}
