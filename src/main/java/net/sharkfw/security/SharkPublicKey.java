package net.sharkfw.security;

import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.system.SharkException;

import java.security.PublicKey;

/**
 * Created by j4rvis on 2/8/17.
 */
public interface SharkPublicKey {

    String INFO_OWNER_PUBLIC_KEY = "INFO_OWNER_PUBLIC_KEY";
    String INFO_VALIDITY = "INFO_VALIDITY";
    String INFO_RECEIVE_DATE = "INFO_RECEIVE_DATE";

    /**
     * @return PublicKey of the owner.
     */
    PublicKey getOwnerPublicKey();

    /**
     * @return PeerSemanticTag of the owner.
     */
    PeerSemanticTag getOwner();

    /**
     * @return Validity of the certificate.
     */
    long getValidity();

    /**
     * @return Fingerprint byte-array
     */
    byte[] getFingerprint();

    long receiveDate();

    void delete();
}
