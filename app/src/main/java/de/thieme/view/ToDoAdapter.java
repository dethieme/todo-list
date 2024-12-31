package de.thieme.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import org.dieschnittstelle.mobile.android.skeleton.R;
import org.dieschnittstelle.mobile.android.skeleton.databinding.ItemTodoBinding;

import java.util.List;

import de.thieme.model.ToDo;
import de.thieme.util.ImageViewUtil;

public class ToDoAdapter extends ArrayAdapter<ToDo> {

    public ToDoAdapter(Context owner, List<ToDo> todos) {
        super(owner, R.layout.item_todo, todos);
    }

    @NonNull
    @Override
    public View getView(int position, View recyclableToDoView, @NonNull ViewGroup parent) {
        ItemTodoBinding binding;
        View todoListView;
        ToDo todo = getItem(position);

        // Reuse convertView or inflate new view
        if (recyclableToDoView == null) {
            binding = DataBindingUtil
                    .inflate(LayoutInflater.from(getContext()), R.layout.item_todo, null, false);
            todoListView = binding.getRoot();
            todoListView.setTag(binding);
        } else {
            todoListView = recyclableToDoView;
            binding = (ItemTodoBinding) todoListView.getTag();
        }

        binding.setTodo(todo);

        // Data Binding
        TextView expiryView = todoListView.findViewById(R.id.todoExpiry);
        expiryView.setText(String.valueOf(todo.getExpiry()));

        ImageView favoriteImageView = todoListView.findViewById(R.id.todoIsFavorite);
        favoriteImageView.setOnClickListener(view -> {
            todo.setIsFavourite(!todo.isFavourite());
            ImageViewUtil.setFavoriteIcon(favoriteImageView, todo);
        });
        ImageViewUtil.setFavoriteIcon(favoriteImageView, todo);

        return todoListView;
    }
}
