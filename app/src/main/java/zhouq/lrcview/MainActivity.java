package zhouq.lrcview;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;


public class MainActivity extends Activity implements LoaderManager
        .LoaderCallbacks<Cursor> {

    private ListView listView;

    CursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.list);
        mAdapter = new MyListAdapter(this, R.layout
                .list_item, null);
        listView.setAdapter(mAdapter);

        getLoaderManager().initLoader(0, null, this);
        listView.setOnItemClickListener(itemClickListener);
    }

    private String songPath;
    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Cursor cursor = (Cursor) mAdapter.getItem(position);
            songPath = cursor.getString(cursor.getColumnIndex(MediaStore
                    .Audio.Media.DATA));
            int duration = cursor.getInt(cursor.getColumnIndex(MediaStore
                    .Audio.Media.DURATION));
            Intent intent = new Intent(MainActivity.this , PlayerActivity
                    .class);
            intent.putExtra("path",songPath);
            intent.putExtra("duration",duration);
            MainActivity.this.startActivity(intent);
        }
    };
    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String[] projection = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA
        };

        return new CursorLoader(this, uri, projection, null, null, MediaStore
                .Audio.Media.TITLE + "  COLLATE LOCALIZED ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mAdapter.swapCursor(null);
    }

    public class MyListAdapter extends ResourceCursorAdapter {

        private TextView mTitle;
        private TextView mDuration;

        public MyListAdapter(Context context, int layout, Cursor c) {
            super(context, layout, c, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            mTitle = (TextView) view.findViewById(R.id.title);
            mDuration = (TextView) view.findViewById(R.id.duration);

            String titleStr = cursor.getString(cursor.getColumnIndex
                    (MediaStore.Audio.Media.TITLE));
            int durationInt = cursor.getInt(cursor.getColumnIndex(MediaStore
                    .Audio.Media.DURATION));

            String durationStr = formatDuration(durationInt);

            mTitle.setText(titleStr);
            mDuration.setText(durationStr);
        }

        private String formatDuration(int durationInt) {
            durationInt /= 1000;

            StringBuilder sb = new StringBuilder();
            int mins = durationInt / 60;
            int seconds = durationInt % 60;
            sb.append(mins).append(":").append(seconds);

            return sb.toString();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
