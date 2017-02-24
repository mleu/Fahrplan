package io.github.imsmobile.fahrplan;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.gson.Gson;

import java.text.DateFormat;
import java.util.Date;

import ch.schoeb.opendatatransport.model.Connection;
import ch.schoeb.opendatatransport.model.Journey;
import ch.schoeb.opendatatransport.model.Section;

public class ConnectionActivity extends AppCompatActivity {

    private static final DateFormat DF = DateFormat.getTimeInstance(DateFormat.SHORT);
    private static final String TRAM_ICON = "\uD83D\uDE8B";
    private static final String BUS_ICON = "\uD83D\uDE8C";
    private static final String ARROW = "\u279C";

    private Connection connection;
    private StringBuilder text;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.label_connection));

        Gson gson = new Gson();
        Intent intent = getIntent();

        connection = gson.fromJson(intent.getStringExtra(SearchResultActivity.CONNECTION), Connection.class);

        buildText();

        TextView textView = (TextView) findViewById(R.id.connection);
        textView.setText(text.toString());

    }

    private void buildText() {
        text = new StringBuilder();
        for(Section s : connection.getSections()) {
            if(s.getJourney() != null) {
                text.append(String.format("%s %s %s\n", s.getDeparture().getStation().getName(), ARROW, s.getArrival().getStation().getName()));

                text.append(String.format("%s - %s\n", DF.format(new Date(s.getDeparture().getDepartureTimestamp().longValue() * 1000)), DF.format(new Date(Long.parseLong(s.getArrival().getArrivalTimestamp()) * 1000))));

                Journey journey = s.getJourney();
                String category =  journey.getCategory();
                switch(category) {
                    case "S":
                        text.append(category).append(journey.getNumber());
                        break;
                    case "T":
                        text.append(TRAM_ICON).append(journey.getNumber());
                        break;
                    case "BUS":
                    case "NFB":
                        text.append(BUS_ICON).append(journey.getNumber());
                        break;
                    default:
                        text.append(journey.getName());
                }
                if(!s.getDeparture().getPlatform().isEmpty()) {
                    text.append(String.format(" %s %s ", getString(R.string.connection_on_track), s.getDeparture().getPlatform()));
                }
                text.append(String.format("%s %s\n\n", getString(R.string.connection_heading_to), journey.getTo()));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.share, menu);

        MenuItem item = menu.findItem(R.id.action_share);
        ShareActionProvider shareAction = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        Intent send = new Intent();
        send.setAction(Intent.ACTION_SEND);
        send.putExtra(Intent.EXTRA_TEXT, text.toString());
        send.setType("text/plain");
        if(shareAction != null) {
            shareAction.setShareIntent(send);
        }

        return super.onCreateOptionsMenu(menu);
    }
}