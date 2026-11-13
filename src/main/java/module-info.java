module org.example.proyectohibernate {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.naming;
    requires org.postgresql.jdbc;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires org.jboss.logging;
    requires jakarta.xml.bind;
    requires net.bytebuddy;
    requires jakarta.transaction;

    exports org.example.proyectohibernate;
    opens org.example.proyectohibernate.controller to javafx.fxml;
    opens org.example.proyectohibernate.model to org.hibernate.orm.core, javafx.base;
}
