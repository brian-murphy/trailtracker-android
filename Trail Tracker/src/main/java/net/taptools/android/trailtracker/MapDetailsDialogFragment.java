package net.taptools.android.trailtracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import static net.taptools.android.trailtracker.TTSQLiteOpenHelper.*;

/**
 * Created by Brian Murphy on 5/18/2014.
 */
public class MapDetailsDialogFragment extends DialogFragment {
    private EditText nameEditText;
    private EditText notesEditText;

    public static final String KEY_MODE = "map details mode key";
    public static final String MODE_FINISH_TRACKING = "we just finished tracking";
    public static final String MODE_RESULTS_EDIT = "editing the details from results";

    private SQLiteDatabase writableDatabase;

    public interface MapDetailsChangeListener {
        public void onCancel();
        public void onQuitWithoutSaving();
        public void onSaveNewDetails();
    }

    private MapDetailsChangeListener listener;

    public static MapDetailsDialogFragment instanceOf(String mode, long mapId, MapDetailsChangeListener listener){
        MapDetailsDialogFragment frag = new MapDetailsDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_MODE,mode);
        bundle.putLong(TrailTrackingService.KEY_MAP_ID,mapId);
        frag.setArguments(bundle);
        frag.listener= listener;
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        writableDatabase = ((MyApplication)getActivity().getApplication())
                .getDatabaseHelper().getWritableDatabase();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = MapDetailsDialogFragment.this.getArguments();
        final long mapId = args.getLong(TrailTrackingService.KEY_MAP_ID,-1);
        String mode = args.getString(KEY_MODE,MODE_RESULTS_EDIT);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_map_details, null);
        nameEditText = (EditText)layout.findViewById(R.id.mapNameEditText);
        notesEditText = (EditText)layout.findViewById(R.id.notesEditText);
        builder.setView(layout);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onSaveNewDetails();
                ContentValues values = new ContentValues();
                values.put(COLUMN_NAME,nameEditText.getText().toString());
                values.put(COLUMN_NOTES,notesEditText.getText().toString());
                writableDatabase.update(TABLE_MAPS,values,COLUMN_ID+" = "+mapId,null);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onCancel();
            }
        });
        if(mode == MODE_FINISH_TRACKING) {
            builder.setNeutralButton("Quit Without Saving", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    listener.onQuitWithoutSaving();
                }
            });
        }
        return builder.create();
    }
}
