package de.thieme.view;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
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
import android.widget.ListView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import org.dieschnittstelle.mobile.android.skeleton.R;
import org.dieschnittstelle.mobile.android.skeleton.databinding.ActivityDetailViewBinding;
import org.dieschnittstelle.mobile.android.skeleton.databinding.ItemContactBinding;

import java.util.List;

import de.thieme.model.Contact;
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

            int hasReadContactPermission = checkSelfPermission(Manifest.permission.READ_CONTACTS);

            if (hasReadContactPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 42);
            } else {
                this.viewModel.getToDo().getContacts().add(String.valueOf(internalContactId));
                contactListViewAdapter.notifyDataSetChanged();
            }
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

        private final String QUERY_PATTERN = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?";

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

            Contact contact = readContact(getItem(position));
            //contact.setName(readDetail(contact.getId(), ContactsContract.Contacts.DISPLAY_NAME));
          //  contact.setMailAddress(readDetail(contact.getId(), ContactsContract.CommonDataKinds.Email.ADDRESS));

            binding.setContact(contact);
            binding.setViewmodel(viewModel);

            binding.contactViaMail.setOnClickListener(view -> {
                Log.i("NAME", contact.getName());
                Log.i("MAIL", contact.getMailAddress());
            });
            binding.contactViaSms.setOnClickListener(view -> {

            });
            binding.deleteContact.setOnClickListener(view -> {
                viewModel.getToDo().getContacts().removeIf(c -> c.equals(contact.getId()));
                notifyDataSetChanged();
            });

            return contactListView;
        }

        private String readDetail(String contactId, String columnName) {
            Cursor cursor = getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    QUERY_PATTERN,
                    new String[]{contactId},
                    null
            );

            if (cursor != null && cursor.moveToNext()) {
                int columnIndex = cursor.getColumnIndex(columnName);
                String displayName = cursor.getString(columnIndex);
                cursor.close();

                return displayName;
            } else {
                return "";
            }
        }

        public  Contact readContact(String contactId) {
            Contact contact = new Contact(contactId);

            // Query the ContactsContract.Data table
            Cursor cursor = getContentResolver().query(
                    ContactsContract.Data.CONTENT_URI,
                    new String[]{
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.Contacts.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NUMBER,
                            ContactsContract.CommonDataKinds.Phone.TYPE,
                            ContactsContract.CommonDataKinds.Email.DATA
                    },
                    ContactsContract.Data.CONTACT_ID + " = ?",
                    new String[]{contactId},
                    null
            );

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    // Retrieve and set the contact's name
                    if (contact.getName() == null) {
                        String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        contact.setName(name);
                    }

                    // Check the MIME type
                    String mimeType = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.MIMETYPE));

                    if (mimeType.equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
                        // Check if the phone number is mobile
                        int phoneNumberType = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                        if (phoneNumberType == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) {
                            String mobileNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            contact.setMobileNumber(mobileNumber);
                        }
                    } else if (mimeType.equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {
                        String email = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                        contact.setMailAddress(email);
                    }

                    // Break early if all fields are populated
                    if (contact.getName() != null && contact.getMobileNumber() != null && contact.getMailAddress() != null) {
                        break;
                    }
                }
                cursor.close();
            }

            return contact;
        }
    }

    /**
     *  int phoneNumberColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
     *                 int phoneNumberTypeColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
     *
     *                 String phoneNumber = cursor.getString(phoneNumberColumnIndex);
     *                 int phoneNumberType = cursor.getInt(phoneNumberTypeColumnIndex);
     *
     *                 boolean isMobile = phoneNumberType == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;
     *
     */
}
