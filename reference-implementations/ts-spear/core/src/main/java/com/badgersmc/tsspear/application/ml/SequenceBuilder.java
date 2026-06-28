package com.badgersmc.tsspear.application.ml;

import com.badgersmc.tsspear.domain.model.sample.CombatSample;
import com.badgersmc.tsspear.domain.model.sample.MovementSample;
import com.badgersmc.tsspear.domain.model.sample.PacketSample;

import java.util.List;

public final class SequenceBuilder {
    public float[][] buildMovementSequence(List<MovementSample> samples) {
        float[][] seq = new float[samples.size()][6];
        for (int i = 0; i < samples.size(); i++) {
            MovementSample s = samples.get(i);
            seq[i][0] = (float) s.position().x();
            seq[i][1] = (float) s.position().y();
            seq[i][2] = (float) s.position().z();
            seq[i][3] = (float) s.velocity().length();
            seq[i][4] = s.onGround() ? 1f : 0f;
            seq[i][5] = s.pingMs();
        }
        return seq;
    }

    public float[][] buildCombatSequence(List<CombatSample> samples) {
        float[][] seq = new float[samples.size()][4];
        for (int i = 0; i < samples.size(); i++) {
            CombatSample s = samples.get(i);
            seq[i][0] = (float) s.distance();
            seq[i][1] = (float) s.damage();
            seq[i][2] = s.critical() ? 1f : 0f;
            seq[i][3] = s.pingMs();
        }
        return seq;
    }

    public float[][] buildPacketSequence(List<PacketSample> samples) {
        float[][] seq = new float[samples.size()][5];
        for (int i = 0; i < samples.size(); i++) {
            PacketSample s = samples.get(i);
            seq[i][0] = (float) s.positionDelta();
            seq[i][1] = s.pingMs();
            seq[i][2] = (float) s.millisSinceLastPacket();
            seq[i][3] = s.reportedPosition() == null ? 0f : (float) s.reportedPosition().x();
            seq[i][4] = s.serverPosition() == null ? 0f : (float) s.serverPosition().x();
        }
        return seq;
    }
}