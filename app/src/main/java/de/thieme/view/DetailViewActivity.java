package de.thieme.view;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.Manifest;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TimePicker;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import org.dieschnittstelle.mobile.android.skeleton.R;
import org.dieschnittstelle.mobile.android.skeleton.databinding.ActivityDetailViewBinding;
import org.dieschnittstelle.mobile.android.skeleton.databinding.ItemContactBinding;
import org.dieschnittstelle.mobile.android.skeleton.databinding.ItemTodoBinding;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.thieme.model.ToDo;
import de.thieme.util.BindingUtils;
import de.thieme.viewmodel.DetailViewViewModel;

public class DetailViewActivity extends AppCompatActivity {

    protected static final String ARG_TODO = "todo";
    protected static final int RESULT_CODE_EDITED_OR_CREATED = 200;
    protected static final int RESULT_CODE_DELETED = 400;

    private static final int REQUEST_CONTACT_PERMISSIONS = 42;
    private DetailViewViewModel viewModel;
    private ContactAdapter contactListViewAdapter;
    private ListView contactListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Instantiate or reuse the view model.
        viewModel = new ViewModelProvider(this).get(DetailViewViewModel.class);

        if (viewModel.getToDo() == null) {
            ToDo todo = (ToDo) getIntent().getSerializableExtra(ARG_TODO);

            if (todo == null) {
                todo = new ToDo();
            }

            viewModel.setToDo(todo);
        }

        // Register the activity as observer.
        viewModel.getToDoValidOnSave().observe(this, valid -> {
            if (valid) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra(ARG_TODO, this.viewModel.getToDo());

                this.setResult(RESULT_CODE_EDITED_OR_CREATED, returnIntent);
                this.finish();
            }
        });

        // Instantiate the view and pass the view model to it.
        ActivityDetailViewBinding binding = DataBindingUtil
                .setContentView(this, R.layout.activity_detail_view);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);

        binding.todoIsFavorite.setOnClickListener(view -> {
            viewModel.getToDo().setIsFavourite(!viewModel.getToDo().isFavourite());
            BindingUtils.setFavoriteIcon(binding.todoIsFavorite, viewModel.getToDo().isFavourite());
        });
        binding.todoExpiry.setOnClickListener(view -> {
            showDatePickerDialog();
        });

        contactListViewAdapter = new ContactAdapter(this, viewModel.getToDo().getContacts());
        binding.contactListView.setAdapter(contactListViewAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_detail_view_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.addContact) {
            addContact();
            return true;
        } else if (item.getItemId() == R.id.deleteTodo) {
            deleteTodo();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void showDatePickerDialog() {
        DetailViewViewModel.DateHelper dateHelper = viewModel.getDateHelper().getValue();

        new DatePickerDialog(
                this,
                (datePicker, year, month, day) -> {
                    new TimePickerDialog(
                            this,
                            (timePicker, hourOfDay, minute) -> {
                                dateHelper.setDate(year, month, day, hourOfDay, minute);
                            },
                            dateHelper.getHourOfDay(),
                            dateHelper.getMinute(),
                            true
                    ).show();
                },
                dateHelper.getYear(),
                dateHelper.getMonth(),
                dateHelper.getDayOfMonth()
        ).show();
    }

    public void deleteTodo() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Löschen");
        alert.setMessage("Willst du dieses To Do wirklich löschen?");

        alert.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            Intent returnIntent = new Intent();
            returnIntent.putExtra(ARG_TODO, this.viewModel.getToDo());

            this.setResult(RESULT_CODE_DELETED, returnIntent);
            this.finish();
        });
        alert.setNegativeButton(android.R.string.no, (dialog, which) -> {
            dialog.cancel();
        });

        alert.show();
    }

    private void addContact() {
        Intent selectContactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        selectContactLauncher.launch(selectContactIntent);
    }

    private void readContactDetails(Uri contactUri) {
        Cursor cursor = getContentResolver()
                .query(contactUri, null, null, null, null);

        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
            long internalContactId = cursor.getLong(columnIndex);

            this.viewModel.getToDo().getContacts().add(String.valueOf(internalContactId));
            contactListViewAdapter.notifyDataSetChanged();
        }

        cursor.close();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CONTACT_PERMISSIONS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                addContact();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private ActivityResultLauncher<Intent> selectContactLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == DetailViewActivity.RESULT_OK) {
                    readContactDetails(result.getData().getData());
                }
            }
    );

    protected class ContactAdapter extends ArrayAdapter<String> {

        public ContactAdapter(Context owner, List<String> contacts) {
            super(owner, R.layout.item_contact, contacts);
        }

        @NonNull
        @Override
        public View getView(int position, View recyclableContactView, @NonNull ViewGroup parent) {
            ItemContactBinding binding;
            View contactListView;

            // Reuse recyclableToDoView or inflate new view
            if (recyclableContactView == null) {
                binding = DataBindingUtil.inflate(
                        LayoutInflater.from(getContext()),
                        R.layout.item_contact,
                        null,
                        false
                );
                contactListView = binding.getRoot();
                contactListView.setTag(binding);
            } else {
                contactListView = recyclableContactView;
                binding = (ItemContactBinding) contactListView.getTag();
            }

            String contactId = getItem(position);

            binding.setContact(readContactDetailsForInternalId(contactId));
            binding.setViewmodel(viewModel);

            return contactListView;
        }

        private String readContactDetailsForInternalId(String contactId) {
            int hasReadContactPermission = checkSelfPermission(Manifest.permission.READ_CONTACTS);

            if (hasReadContactPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 42);
                return "";
            }

            String queryPattern = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?";
            Cursor cursor = getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    queryPattern,
                    new String[]{contactId},
                    null
            );

            while (cursor.moveToNext()) {
                int displayNameColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                String displayName = cursor.getString(displayNameColumnIndex);

                int phoneNumberColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                int phoneNumberTypeColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);

                String phoneNumber = cursor.getString(phoneNumberColumnIndex);
                int phoneNumberType = cursor.getInt(phoneNumberTypeColumnIndex);

                boolean isMobile = phoneNumberType == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;
                return displayName;
            }

            cursor.close();
            return "";
        }
    }
}
