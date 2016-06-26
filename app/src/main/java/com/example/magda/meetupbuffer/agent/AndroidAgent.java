package com.example.magda.meetupbuffer.agent;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.example.magda.meetupbuffer.R;
import com.example.magda.meetupbuffer.activities.MainActivity;
import com.example.magda.meetupbuffer.fragments.ProposeDestinationFragment;
import com.example.magda.meetupbuffer.fragments.WaitForFriendsFragment;
import com.example.magda.meetupbuffer.parsers.XMLRead;

import java.util.ArrayList;
import java.util.List;

import jade.content.ContentManager;
import jade.content.lang.sl.SLCodec;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.*;
import jade.lang.acl.MessageTemplate;

/**
 * Created by magda on 06.05.16.
 */
public class AndroidAgent extends Agent  implements AgentInterface{
    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
    int performative = ACLMessage.INFORM;
    String agentID = "Server";
    AID receiver = new AID(agentID,AID.ISLOCALNAME);
    AMSAgentDescription [] agents = null;
    int counter=0;
    String activeAgentsNames [] = new String[]{"Sorry no one is available!"};
    private Context context;
    String location = null;
    String initiator = null;
    List friends;
    protected void setup(){

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            if (args[0] instanceof Context) {
                context = (Context) args[0];
            }
        }
        ContentManager cm = getContentManager();
        cm.registerLanguage(new SLCodec());
        cm.registerOntology(FIPAManagementOntology.getInstance());
        cm.setValidationMode(false);
        registerO2AInterface(AgentInterface.class, this);
        addBehaviour(new CyclicBehaviour() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void action() {
                {
                    MessageTemplate mt = MessageTemplate.MatchAll();
                    ACLMessage rec = receive(mt);
                    if (rec != null) {
                        //System.out.println(this.getAgent().getName() + " recieved from " + rec.getSender().getName());
                        XMLRead read = new XMLRead();
                        read.Read(rec.getContent());
                        switch(read.type) {
                            case "0": //Proposal of meeting
                                initiator = read.id;
                                friends = new ArrayList(read.friends);
                                //Log.d("Destination", read.location);
                                NotificationManager notificationManager = (NotificationManager)
                                        context.getSystemService(Context.NOTIFICATION_SERVICE);
                                Intent intent = new Intent(context, MainActivity.class);
                                intent.setAction(Intent.ACTION_MAIN);
                                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.putExtra("proposemeeting", initiator);
                                Bundle bundle = new Bundle();
                                bundle.putString("proposemeeting", initiator);
                                intent.putExtras(bundle);
                                PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                                String name = MainActivity.friendsDictionary.get(Long.parseLong(initiator));
                                Notification notification = new Notification.Builder(context)
                                        .setContentTitle("Meet Up Buffer :)")
                                        .setContentText("You have been ivited by " + name)
                                        .setSmallIcon(R.drawable.com_facebook_profile_picture_blank_square)
                                        .setContentIntent(pIntent).setAutoCancel(true)
                                        .build();

                                notificationManager.notify(1, notification);
                                break;
                            case "1": //Proposal of place
                                location = read.location;
                                Log.d("Destination", read.location);
                                NotificationManager notificationManager1 = (NotificationManager)
                                        context.getSystemService(Context.NOTIFICATION_SERVICE);
                                Intent intent1 = new Intent(context, MainActivity.class);
                                intent1.setAction(Intent.ACTION_MAIN);
                                intent1.addCategory(Intent.CATEGORY_LAUNCHER);
                                intent1.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent1.putExtra("origin", location);
                                Bundle bundle1 = new Bundle();
                                bundle1.putString("origin", location);
                                intent1.putExtras(bundle1);
                                PendingIntent pIntent1 = PendingIntent.getActivity(context, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
                                Notification notification1 = new Notification.Builder(context)
                                        .setContentText("You have been ivited")
                                        .setSmallIcon(R.drawable.com_facebook_profile_picture_blank_square)
                                        .setContentIntent(pIntent1).setAutoCancel(true)
                                        .build();
                                notificationManager1.notify(1, notification1);
                                break;
                            case "2": //Friends agreed
                                location = read.location;
                                String id = read.id;
                                WaitForFriendsFragment.addFriend(id, location);
                                break;
                            case "3": //Propose destination
                                String placeId = read.placeId;
                                WaitForFriendsFragment.locationFound(placeId);
                                break;
                            case "4": //Propose destination
                                String __placeId = read.placeId;
                                ProposeDestinationFragment.nextlocationFound(__placeId);
                                break;
                            case "5":
                                String _placeId = read.placeId;
                                if(counter==0)
                                {
                                    ProposeDestinationFragment.locationFound(_placeId);
                                    counter++;
                                }
                                break;
                        }
                    } else
                        block();
                }
            }
        });
    }

    public void sendMessage(String s, int messagePerformative) {
        // Add a ChatSpeaker behaviour that INFORMs all participants about
        // the spoken sentence
        Log.d("Behavour","Add behaviour");
        addBehaviour(new SendMessageBehaviour(this, s));
        performative = messagePerformative;
    }

    @Override
    public String[] getAllActive() {
        try {
            SearchConstraints c = new SearchConstraints();
            c.setMaxResults ( new Long(-1) );
            agents = AMSService.search( this, new AMSAgentDescription (), c );
        }
        catch (Exception e) {}
        if (agents.length!=0) {
            activeAgentsNames = new String[agents.length];
            for (int i = 0; i < agents.length; i++) {
                AID agentID = agents[i].getName();
                activeAgentsNames[i] = agents[i].getName().getLocalName();
                System.out.println(agentID.getLocalName());
            }
        }
        return activeAgentsNames;
    }

    private class SendMessageBehaviour extends OneShotBehaviour {
        private static final long serialVersionUID = -1426033904935339194L;
        private String content;

        private SendMessageBehaviour(Agent a, String c) {
            super(a);
            content = c;
        }

        public void action() {

            Log.d("Content before send", content);
            msg.setContent(content);
            msg.setPerformative(performative);
            msg.addReceiver(receiver);
            send(msg);
            Log.d("Message", "Send message");
        }
    }

}
