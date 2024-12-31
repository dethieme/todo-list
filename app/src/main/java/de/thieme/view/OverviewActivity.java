package de.thieme.view;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import org.dieschnittstelle.mobile.android.skeleton.R;
import org.dieschnittstelle.mobile.android.skeleton.databinding.ItemTodoBinding;

import java.util.List;

import de.thieme.model.IToDoCRUDOperations;
import de.thieme.model.RoomToDoCRUDOperations;
import de.thieme.model.ToDo;
import de.thieme.util.ImageViewUtil;
import de.thieme.viewmodel.OverviewViewModel;

public class OverviewActivity extends AppCompatActivity {

    private ToDoAdapter todoListViewAdapter;
    private OverviewViewModel viewModel;

    private ProgressBar progressBar;
    private ListView todoListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        IToDoCRUDOperations crudOperations = new RoomToDoCRUDOperations(this);
        // Handle the progress bar.
        progressBar = findViewById(R.id.progressBar);

        // Use the view model
        viewModel = new ViewModelProvider(this).get(OverviewViewModel.class);
        viewModel.setCrudOperations(crudOperations);
        viewModel.getProcessingState().observe(this, processingState -> {
            switch (processingState) {
                case RUNNING_LONG:
                    progressBar.setVisibility(View.VISIBLE);
                    break;
                case DONE:
                    progressBar.setVisibility(View.GONE);
                    todoListViewAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        } );

        if (!viewModel.isInitialized()) {
            viewModel.readAll();
            viewModel.setInitialized(true);
        }

        todoListViewAdapter = new ToDoAdapter(this, viewModel.getToDos());
        todoListView = findViewById(R.id.todoListView);
        todoListView.setAdapter(todoListViewAdapter);

        todoListView.setOnItemClickListener((adapterView, view, position, id) -> {
            ToDo selectedTodo = todoListViewAdapter.getItem(position);
            Intent callDetailViewIntent = new Intent(OverviewActivity.this, DetailViewActivity.class);

            callDetailViewIntent.putExtra(DetailViewActivity.ARG_TODO, selectedTodo);
            detailViewForEditLauncher.launch(callDetailViewIntent);
        });

        // Handle the add button.
        findViewById(R.id.addTodoActionButton).setOnClickListener(view -> {
            detailViewForCreateLauncher.launch(new Intent(OverviewActivity.this, DetailViewActivity.class));
        });
    }

    protected void showMessage(String message) {
        Snackbar.make(findViewById(R.id.rootView), message, Snackbar.LENGTH_SHORT).show();
    }

    protected ActivityResultLauncher<Intent> detailViewForCreateLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == DetailViewActivity.RESULT_CODE_EDITED_OR_CREATED) {
                    ToDo todoToBeCreated = (ToDo) result.getData().getSerializableExtra(DetailViewActivity.ARG_TODO);
                    viewModel.create(todoToBeCreated);
                } else {
                    showMessage("Fehler");
                }
            }
    );

    protected ActivityResultLauncher<Intent> detailViewForEditLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getData() == null) {
                    showMessage("Fehler: Keine Daten verfügbar");
                    return;
                }

                ToDo todo = (ToDo) result.getData().getSerializableExtra(DetailViewActivity.ARG_TODO);
                if (todo == null) {
                    showMessage("Fehler: Ungültige ToDo-Daten");
                    return;
                }

                switch (result.getResultCode()) {
                    case DetailViewActivity.RESULT_CODE_EDITED_OR_CREATED:
                        viewModel.update(todo);
                        break;
                    case DetailViewActivity.RESULT_CODE_DELETED:
                        viewModel.delete(todo.getId());
                        break;
                    default:
                        showMessage("Unbekannter Fehler");
                        break;
                }
            }
    );

    protected class ToDoAdapter extends ArrayAdapter<ToDo> {

        public ToDoAdapter(Context owner, List<ToDo> todos) {
            super(owner, R.layout.item_todo, todos);
        }

        @NonNull
        @Override
        public View getView(int position, View recyclableToDoView, @NonNull ViewGroup parent) {
            ItemTodoBinding binding;
            View todoListView;
            ToDo todo = getItem(position);

            // Reuse recyclableToDoView or inflate new view
            if (recyclableToDoView == null) {
                binding = DataBindingUtil
                        .inflate(LayoutInflater.from(getContext()), R.layout.item_todo, null, false);
                todoListView = binding.getRoot();
                todoListView.setTag(binding);
                binding.setViewmodel(viewModel);
            } else {
                todoListView = recyclableToDoView;
                binding = (ItemTodoBinding) todoListView.getTag();
                binding.setViewmodel(viewModel);
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
}
