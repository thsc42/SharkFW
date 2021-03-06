package net.sharkfw.knowledgeBase.persistent.sql;

import net.sharkfw.asip.ASIPInformation;
import net.sharkfw.asip.ASIPInformationSpace;
import net.sharkfw.asip.ASIPSpace;
import net.sharkfw.knowledgeBase.SharkKBException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

public class SqlAsipInformationSpace implements ASIPInformationSpace {

    private final SqlSharkKB sharkKB;
    private List<SqlAsipInformation> infos = new ArrayList<>();
    private SqlAsipSpace space;

    public SqlAsipInformationSpace(SqlSharkKB sharkKB, SqlAsipSpace space) {
        this.sharkKB = sharkKB;
        this.space = space;
        this.space.addInformationSpace(this);
        this.space.addSharkKb(sharkKB);
    }

    protected void addInformation(SqlAsipInformation sqlAsipInformation){
        infos.add(sqlAsipInformation);
    }

    @Override
    public ASIPSpace getASIPSpace() throws SharkKBException {
        return space;
    }

    @Override
    public int numberOfInformations() {
        return infos.size();
    }

    @Override
    public Iterator<ASIPInformation> informations() throws SharkKBException {
        return ((List<ASIPInformation>) (List<?>) infos).iterator();
    }
}
