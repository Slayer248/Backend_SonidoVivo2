package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Completa bases creadas antes de usar Flyway (cuando el esquema lo generaba Hibernate con ddl-auto: update)
 * a las que les falten columnas agregadas después. En una base nueva el V1 ya las trae y esta migración no hace nada.
 * Se hace con una revisión de columnas en Java porque MySQL no tiene ADD COLUMN IF NOT EXISTS.
 */
public class V2__completar_columnas_previas extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection c = context.getConnection();
        agregarSiFalta(c, "consultas", "usuario_id", "bigint");
        agregarSiFalta(c, "consultas", "respuesta", "varchar(1000)");
        agregarSiFalta(c, "consultas", "fecha_respuesta", "datetime(6)");
    }

    private static void agregarSiFalta(Connection c, String tabla, String columna, String definicion) throws Exception {
        try (ResultSet rs = c.getMetaData().getColumns(c.getCatalog(), null, tabla, columna)) {
            if (rs.next()) return;
        }
        try (Statement st = c.createStatement()) {
            st.execute("ALTER TABLE " + tabla + " ADD COLUMN " + columna + " " + definicion);
        }
    }
}
