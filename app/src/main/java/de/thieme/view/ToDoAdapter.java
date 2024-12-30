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
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.item_todo,
                    parent,
                    false
            );
        }

        ToDo toDo = getItem(position);

        // Find views
        CheckBox doneCheckBox = convertView.findViewById(R.id.todoIsDone);
        TextView nameView = convertView.findViewById(R.id.todoName);
        TextView expiryView = convertView.findViewById(R.id.todoExpiry);
        ImageView favoriteImageView = convertView.findViewById(R.id.todoIsFavorite);

        favoriteImageView.setOnClickListener(view -> {
            toDo.setIsFavourite(!toDo.isFavourite());
            ImageViewUtil.setFavoriteIcon(favoriteImageView, toDo);
        });

        // Populate views
        nameView.setText(toDo.getName());
        expiryView.setText(String.valueOf(toDo.getExpiry()));
        doneCheckBox.setChecked(toDo.isDone());
        ImageViewUtil.setFavoriteIcon(favoriteImageView, toDo);

        return convertView;
    }
}
