package com.example.magda.meetupbuffer.agent;

import android.util.Pair;

/**
 * Created by magda on 17.05.16.
 */
public interface AgentInterface {
    public void sendMessage(String s, int acceptProposal);
    public String[] getAllActive();
}