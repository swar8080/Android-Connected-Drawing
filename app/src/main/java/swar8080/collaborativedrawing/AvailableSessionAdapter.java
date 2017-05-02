package swar8080.collaborativedrawing;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import swar8080.collaborativedrawing.connection.AvailableSession;

/**
 *
 */

public class AvailableSessionAdapter extends RecyclerView.Adapter<AvailableSessionAdapter.AvailableSessionViewHolder> {

    private ArrayList<AvailableSession> mAvailableSessions;
    private final OnSessionClickHandler mSessionClickHandler;

    public interface OnSessionClickHandler {
        void onSessionSelected(AvailableSession session);
    }


    public AvailableSessionAdapter(OnSessionClickHandler onSessionClickHandler){
        this.mSessionClickHandler = onSessionClickHandler;
        this.mAvailableSessions = new ArrayList<>();
    }


    public void updateOrInsertSession(AvailableSession availableSession){
        AvailableSession existingSession;
        int length = mAvailableSessions.size();

        //update session with new information if it already exists
        for (int i=0; i<length; i++){
            existingSession = mAvailableSessions.get(i);
            if (availableSession.getHostId().equals(existingSession.getHostId())
                    && availableSession.getSessionName().equals(existingSession.getSessionName())){
                mAvailableSessions.set(i, existingSession);
                notifyItemChanged(i);
                return;
            }
        }

        //otherwise add the new session
        mAvailableSessions.add(availableSession);
        notifyItemInserted(mAvailableSessions.size()-1);
    }

    public void removeSession(String hostId){
        int sessionCount = mAvailableSessions.size();
        for (int i=0; i<sessionCount; i++ ){
            if (hostId.equals(mAvailableSessions.get(i).getHostId())){
                mAvailableSessions.remove(i);
                notifyItemRemoved(i);
            }
        }
    }

    @Override
    public AvailableSessionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.available_session, parent, false);

        AvailableSessionViewHolder viewHolder = new AvailableSessionViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(AvailableSessionViewHolder sessionViewHolder, int position) {
        AvailableSession session = mAvailableSessions.get(position);
        sessionViewHolder.bindSessionName(session.getSessionName());
        sessionViewHolder.bindParticipantCount(session.getPlayerCount());
    }

    @Override
    public int getItemCount() {
        return mAvailableSessions.size();
    }

    class AvailableSessionViewHolder extends RecyclerView.ViewHolder{

        private TextView mSessionNameView, mSessionCountView;

        public AvailableSessionViewHolder(View itemView) {
            super(itemView);

            mSessionNameView = (TextView)itemView.findViewById(R.id.availableSessionName);
            mSessionCountView = (TextView)itemView.findViewById(R.id.session_participant_count);

            itemView.findViewById(R.id.join_session_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AvailableSession selectedSession = mAvailableSessions.get(getAdapterPosition());
                    mSessionClickHandler.onSessionSelected(selectedSession);
                }
            });
        }

        public void bindSessionName(String sessionName){
            mSessionNameView.setText(sessionName);
        }

        public void bindParticipantCount(int count){
            mSessionCountView.setText(String.valueOf(count));
        }

    }
}
