package net.sharkfw.knowledgeBase.persistent.sql;

import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.SpatialSemanticTag;
import net.sharkfw.knowledgeBase.geom.SharkGeometry;
import net.sharkfw.knowledgeBase.geom.inmemory.InMemoSharkGeometry;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.inline;
import static org.jooq.impl.DSL.table;

/**
 * Created by Dustin Feurich on 18.04.2017.
 */
public class SqlSpatialSemanticTag extends SqlSemanticTag implements SpatialSemanticTag {

    private String wkt;

    public SqlSpatialSemanticTag(String[] sis, String name, SqlSharkKB sharkKB, String wkt) throws SQLException {
        super(sis, name, "spatial");
        this.wkt = wkt;
        try {
            Class.forName(sharkKB.getDialect());
            connection = DriverManager.getConnection(sharkKB.getDbAddress());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        StringBuilder sql = new StringBuilder();
        sql.append("PRAGMA foreign_keys = ON; ");
        sql.append("INSERT INTO semantic_tag (name, tag_kind, wkt) VALUES "
                + "(\'" + this.getName() + "\'" + ",\"" + this.getTagKind()
                + "\",\"" + this.wkt + "\");");
        SqlHelper.executeSQLCommand(connection, sql.toString());
        this.setId(SqlHelper.getLastCreatedEntry(connection, "semantic_tag"));
        SqlHelper.executeSQLCommand(connection, this.getSqlForSIs());

        DSLContext create = DSL.using(connection, SQLDialect.SQLITE);
        String update = create.update(table("semantic_tag")).set(field("system_property"), inline(Integer.toString(this.getId()))).where(field("id").eq(inline(Integer.toString(this.getId())))).getSQL();

        try {
            SqlHelper.executeSQLCommand(connection, update);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public SqlSpatialSemanticTag(int id, SqlSharkKB sharkKB) throws SharkKBException {
        super(id, sharkKB);
        DSLContext getEntry = DSL.using(connection, SQLDialect.SQLITE);
        String sql = getEntry.selectFrom(table("semantic_tag")).where(field("id").eq(inline(getId()))).getSQL();
        ResultSet rs = null;
        try {
            rs = SqlHelper.executeSQLCommandWithResult(connection, sql);
            if (rs != null) {
                wkt = rs.getString("wkt");
            }
        } catch (SQLException e) {
            throw new SharkKBException(e.toString());
        }
    }

    public SqlSpatialSemanticTag(String si, SqlSharkKB sharkKB) throws SharkKBException {
        super(si, sharkKB);
        DSLContext getEntry = DSL.using(connection, SQLDialect.SQLITE);
        String sql = null;
        if (si != null) {
            sql = getEntry.selectFrom(table("semantic_tag").join("subject_identifier")
                    .on(field("identifier").eq(inline(si)))).where(field("semantic_tag.id").eq(field("tag_id"))).getSQL();
        }
        else {
            throw new SharkKBException();
        }
        try (ResultSet rs = SqlHelper.executeSQLCommandWithResult(connection, sql)) {

            if (rs != null) {
                wkt = rs.getString("wkt");
            }
        } catch (SQLException e) {
            throw new SharkKBException(e.toString());
        }
    }



    @Override
    public SharkGeometry getGeometry() {
        try {
            return InMemoSharkGeometry.createGeomByWKT(wkt);
        } catch (SharkKBException e) {
            e.printStackTrace();
            return null;
        }
    }
}
