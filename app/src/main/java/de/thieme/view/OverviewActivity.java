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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.android.material.snackbar.Snackbar;

import org.dieschnittstelle.mobile.android.skeleton.R;
import org.dieschnittstelle.mobile.android.skeleton.databinding.ItemTodoBinding;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import de.thieme.ToDoApplication;
import de.thieme.model.IToDoCRUDOperations;
import de.thieme.model.ToDo;
import de.thieme.viewmodel.OverviewViewModel;

public class OverviewActivity extends AppCompatActivity {

    private static final String LOG_TAG = OverviewActivity.class.getName();

    private ToDoAdapter todoListViewAdapter;
    private OverviewViewModel viewModel;
    private ProgressBar progressBar;
    private ListView todoListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        Future<IToDoCRUDOperations> future = ((ToDoApplication) getApplication()).getCRUDOperations();
        IToDoCRUDOperations crudOperations;

        try {
            crudOperations = future.get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e(LOG_TAG, "No CRUD operation implementation available.");
            throw new RuntimeException(e);
        }

        if (((ToDoApplication) getApplication()).isOffline()) {
            showMessage("Applikation ist offline.");
        } else {
            loginViewLauncher.launch(new Intent(OverviewActivity.this, LoginActivity.class));
        }

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
                    viewModel.sortTodos();
                    todoListViewAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        });

        if (!viewModel.isInitialized()) {
            viewModel.readAll();
            viewModel.setInitialized(true);
        }

        todoListViewAdapter = new ToDoAdapter(this, viewModel.getTodos());
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actitivity_overview_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.sortTodos) {
            this.viewModel.switchSortMode();
            todoListViewAdapter.notifyDataSetChanged();
            return true;
        } else if (item.getItemId() == R.id.deleteAllLocalTodos) {
            this.viewModel.deleteAllLocalTodos();
            return true;
        } else if (item.getItemId() == R.id.deleteAllRemoteTodos) {
            this.viewModel.deleteAllRemoteTodos();
            return true;
        } else if (item.getItemId() == R.id.synchronizeTodos) {
            this.viewModel.synchronizeTodos();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    protected ActivityResultLauncher<Intent> loginViewLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                showMessage("Eingeloggt");
            }
    );

    protected ActivityResultLauncher<Intent> detailViewForCreateLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == DetailViewActivity.RESULT_CODE_EDITED_OR_CREATED) {
                    ToDo newTodo = (ToDo) result.getData().getSerializableExtra(DetailViewActivity.ARG_TODO);
                    viewModel.create(newTodo);
                } else {
                    showMessage("Error: Unable to create ToDo.");
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

            // Reuse recyclableToDoView or inflate new view
            if (recyclableToDoView == null) {
                binding = DataBindingUtil.inflate(
                        LayoutInflater.from(getContext()),
                        R.layout.item_todo,
                        null,
                        false
                );
                todoListView = binding.getRoot();
                todoListView.setTag(binding);
            } else {
                todoListView = recyclableToDoView;
                binding = (ItemTodoBinding) todoListView.getTag();
            }

            ToDo todo = getItem(position);
            binding.setTodo(todo);
            binding.setViewmodel(viewModel);

            binding.todoIsFavorite.setOnClickListener(view -> {
                todo.setIsFavourite(!todo.isFavourite());
                viewModel.update(todo);
            });

            return todoListView;
        }
    }
}
