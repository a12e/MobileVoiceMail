package com.alcatel.mobilevoicemail;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alcatel.mobilevoicemail.opentouch.Identifier;
import com.alcatel.mobilevoicemail.opentouch.OpenTouchClient;
import com.alcatel.mobilevoicemail.opentouch.Voicemail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ThreadActivity extends ActionBarActivity {
    public static final String INTENT_EXTRA_IDENTIFIER = "identifier";

    private Identifier mIdentifier;
    private ListView mVoicemailListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread);
        mVoicemailListView = (ListView)findViewById(R.id.thread_list_view);

        // On récupère le phoneNumber depuis l'Intent
        if(!getIntent().hasExtra(INTENT_EXTRA_IDENTIFIER))
            throw new IllegalArgumentException("Please specify the INTENT_EXTRA_IDENTIFIER in the Intent");
        else
            mIdentifier = (Identifier)getIntent().getSerializableExtra(INTENT_EXTRA_IDENTIFIER);

        setTitle("Conversation avec le " + mIdentifier.getDisplayName());

        // Affichage de l'icone "retour" dans l'ActionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final ImageButton recordButton = (ImageButton)findViewById(R.id.record_button);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ThreadActivity.this, RecordMessageActivity.class);
                intent.putExtra(RecordMessageActivity.INTENT_EXTRA_DESTINATION, mIdentifier);
                startActivity(intent);
            }
        });

        updateVoicemailListView();
    }

    private class VoicemailAdapter extends ArrayAdapter<BaseVoicemail> {
        public VoicemailAdapter(List<BaseVoicemail> identifiers) {
            super(ThreadActivity.this, 0, identifiers);
        }

        // Ce sac contient les vues associées à un item
        class ThreadViewBag {
            public ImageView photo;
            public TextView identifier;
            public TextView date;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Android nous fournit un convertView null lorsqu'il nous demande de la créer
            // dans le cas contraire, cela veux dire qu'il nous fournit une vue recyclée
            if(convertView == null) {
                // Nous récupérons notre threads_list_item via un LayoutInflater,
                // qui va charger un layout xml dans un objet View
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.voicemail_list_item, parent, false);
            }

            ThreadViewBag viewBag = (ThreadViewBag)convertView.getTag();
            if(viewBag == null) {
                viewBag = new ThreadViewBag();
                viewBag.date = (TextView)convertView.findViewById(R.id.date_text_view);
                convertView.setTag(viewBag);
            }

            // Remplissage de la vue avec les données du modèle (Identifier)
            BaseVoicemail voicemail = getItem(position);
            viewBag.date.setText(DateUtils.getRelativeTimeSpanString(getContext(),
                    voicemail.getDate().getTime()));

            // nous renvoyons notre vue à l'adapter, afin qu'il l'affiche
            // et qu'il puisse la mettre à recycler lorsqu'elle sera sortie de l'écran
            return convertView;
        }
    }

    private void updateVoicemailListView() {
        Log.i(getClass().getSimpleName(), "Updating voicemails list of " + mIdentifier);

        ArrayList<LocalVoicemail> sentVoicemails = SentMailbox.getInstance().getVoicemails();
        ArrayList<Voicemail> receivedVoicemails = OpenTouchClient.getInstance().getDefaultMailbox().getVoicemails();

        // liste de tous les messages (envoyés + reçus)
        ArrayList<BaseVoicemail> allVoicemails = new ArrayList<>();
        allVoicemails.addAll(sentVoicemails);
        allVoicemails.addAll(receivedVoicemails);

        // on garde les messages de ce contact uniquement
        ArrayList<BaseVoicemail> contactVoicemails = new ArrayList<>();
        for(BaseVoicemail voicemail: allVoicemails) {
            // message envoyé
            if(voicemail instanceof LocalVoicemail) {
                if(voicemail.getDestination().getPhoneNumber().equals(mIdentifier.getPhoneNumber())) {
                    contactVoicemails.add(voicemail);
                }
            }
            // message reçu
            else if(voicemail instanceof Voicemail) {
                if(voicemail.getFrom().getPhoneNumber().equals(mIdentifier.getPhoneNumber())) {
                    contactVoicemails.add(voicemail);
                }
            }
            // aucun des deux -> erreur
            else {
                throw new RuntimeException("This message is neither LocalVoicemail or Voicemail!");
            }
        }

        // Tri par date décroissante
        Collections.sort(contactVoicemails, new Comparator<BaseVoicemail>() {
            @Override
            public int compare(BaseVoicemail lhs, BaseVoicemail rhs) {
                return rhs.getDate().compareTo(lhs.getDate());
            }
        });

        mVoicemailListView.setAdapter(new VoicemailAdapter(contactVoicemails));

        // S'il n'y a aucun message, on affiche un petit texte pour le dire
        TextView noMessageTextView = (TextView)findViewById(R.id.no_message_text_view);
        if(contactVoicemails.size() == 0)
            noMessageTextView.setVisibility(View.VISIBLE);
        else
            noMessageTextView.setVisibility(View.INVISIBLE);
    }
}
