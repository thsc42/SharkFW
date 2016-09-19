package net.sharkfw.knowledgeBase.sync;

import net.sharkfw.asip.engine.ASIPConnection;
import net.sharkfw.asip.engine.ASIPInMessage;
import net.sharkfw.asip.engine.ASIPSerializer;
import net.sharkfw.peer.ContentPort;
import net.sharkfw.peer.SharkEngine;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import net.sharkfw.knowledgeBase.PeerSTSet;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkCSAlgebra;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;

/**
 * Created by j4rvis on 19.07.16.
 * 
 * @author thsc
 */
public class SyncMergeKP extends ContentPort {

    private SyncManager syncManager;

    public SyncMergeKP(SharkEngine se, SyncManager syncManager) {
        super(se);
        this.syncManager = syncManager;
    }

    @Override
    protected boolean handleRaw(ASIPInMessage message, ASIPConnection connection, InputStream inputStream) {

        if(!SharkCSAlgebra.identical(message.getType(), SyncManager.SHARK_SYNC_MERGE_TAG)) return false;

        SyncComponent component = syncManager.getComponentByName(message.getTopic());

        if(component == null) return false;

        SyncKB syncKB = component.getKb();

        // check allowed sender .. better make that with black-/whitelist
        // deserialize kb from content

        String text;
        try (Scanner scanner = new Scanner(message.getRaw(), StandardCharsets.UTF_8.name())) {
            text = scanner.useDelimiter("\\A").next();
        }
        SharkKB changes;

        try {
            changes = (SharkKB) ASIPSerializer.deserializeASIPKnowledge(text);
            syncKB.putChanges(changes);
        } catch (SharkKBException e) {
            e.printStackTrace();
        }

        return true;
    }
}