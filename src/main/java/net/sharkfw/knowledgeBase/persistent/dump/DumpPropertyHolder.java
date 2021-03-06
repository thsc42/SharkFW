package net.sharkfw.knowledgeBase.persistent.dump;

import net.sharkfw.knowledgeBase.PropertyHolder;
import net.sharkfw.knowledgeBase.SharkKBException;

import java.util.Enumeration;

/**
 * Created by j4rvis on 2/27/17.
 */
public class DumpPropertyHolder implements PropertyHolder {

    protected final DumpSharkKB kb;
    private final PropertyHolder propertyHolder;

    public DumpPropertyHolder(DumpSharkKB dumpSharkKB, PropertyHolder propertyHolder) {
        this.kb = dumpSharkKB;
        this.propertyHolder = propertyHolder;
    }

    @Override
    public void setProperty(String name, String value) throws SharkKBException {
        this.propertyHolder.setProperty(name, value);
        this.kb.persist();
    }

    @Override
    public String getProperty(String name) throws SharkKBException {
        return this.propertyHolder.getProperty(name);
    }

    @Override
    public void setProperty(String name, String value, boolean transfer) throws SharkKBException {
        this.propertyHolder.setProperty(name, value, transfer);
        this.kb.persist();
    }

    @Override
    public void removeProperty(String name) throws SharkKBException {
        this.propertyHolder.removeProperty(name);
        this.kb.persist();
    }

    @Override
    public Enumeration<String> propertyNames() throws SharkKBException {
        return this.propertyHolder.propertyNames();
    }

    @Override
    public Enumeration<String> propertyNames(boolean all) throws SharkKBException {
        return this.propertyHolder.propertyNames(all);
    }
}
