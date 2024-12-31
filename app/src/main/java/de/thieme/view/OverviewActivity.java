package de.thieme.view;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.android.material.snackbar.Snackbar;

import org.dieschnittstelle.mobile.android.skeleton.R;

import java.util.ArrayList;
import java.util.List;

import de.thieme.model.LocalRoomToDoCRUDOperations;
import de.thieme.model.ToDo;
import de.thieme.viewmodel.OverviewActivityViewModel;

public class OverviewActivity extends AppCompatActivity {

    private ArrayAdapter<ToDo> todoListViewAdapter;
    private LocalRoomToDoCRUDOperations crudOperations;
    private OverviewActivityViewModel viewModel;

    private ProgressBar progressBar;
    private ListView todoListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        // Use the view model
        viewModel = new ViewModelProvider(this).get(OverviewActivityViewModel.class);
        crudOperations = new LocalRoomToDoCRUDOperations(this);
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
        findViewById(R.id.addTodoActionButton).setOnClickListener(view ->
                detailViewForCreateLauncher.launch(new Intent(this, DetailViewActivity.class))
        );

        // Handle the progress bar.
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(ListView.VISIBLE);

        if (!viewModel.isInitialized()) {
            // Load to do list.
            new Thread(() -> {
                List<ToDo> todos = crudOperations.readAll();
                viewModel.getToDos().addAll(todos);

                runOnUiThread(() -> {
                    progressBar.setVisibility(ListView.GONE);
                    todoListViewAdapter.notifyDataSetChanged();
                    viewModel.setInitialized(true);
                });
            }).start();
        }
    }

    protected void showMessage(String message) {
        Snackbar.make(findViewById(R.id.rootView), message, Snackbar.LENGTH_SHORT).show();
    }

    protected ActivityResultLauncher<Intent> detailViewForCreateLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == DetailViewActivity.RESULT_CODE_EDITED_OR_CREATED) {
                    ToDo todoToBeCreated = (ToDo) result.getData().getSerializableExtra(DetailViewActivity.ARG_TODO);

                    new Thread(() -> {
                        ToDo createdTodo = crudOperations.create(todoToBeCreated);
                        runOnUiThread(() -> todoListViewAdapter.add(createdTodo));
                    }).start();
                } else {
                    showMessage("Fehler");
                }
            }
    );

    protected ActivityResultLauncher<Intent> detailViewForEditLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == DetailViewActivity.RESULT_CODE_EDITED_OR_CREATED) {
                    ToDo todoToBeEdited = (ToDo) result.getData().getSerializableExtra(DetailViewActivity.ARG_TODO);

                    new Thread(() -> {
                        boolean updated = crudOperations.update(todoToBeEdited);

                        runOnUiThread(() -> {
                            if (updated) {
                                int todoPosition = viewModel.getToDos().indexOf(todoToBeEdited);
                                ToDo existingToDo = viewModel.getToDos().get(todoPosition);

                                existingToDo.setName(todoToBeEdited.getName());
                                existingToDo.setDescription(todoToBeEdited.getDescription());
                                existingToDo.setExpiry(todoToBeEdited.getExpiry());
                                existingToDo.setIsFavourite(todoToBeEdited.isFavourite());
                                existingToDo.setContacts(todoToBeEdited.getContacts());

                                todoListViewAdapter.notifyDataSetChanged();
                            }
                        });
                    }).start();
                } else {
                    showMessage("Fehler");
                }
            }
    );
}
