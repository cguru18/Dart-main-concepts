module studentmanagement {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    
    requires java.sql;
    requires java.desktop;
    
    requires org.slf4j;
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;
    
    requires jbcrypt;
    requires org.apache.commons.lang3;
    requires com.fasterxml.jackson.databind;
    
    requires kernel;
    requires layout;
    requires io;
    
    opens com.studentmanagement to javafx.fxml;
    opens com.studentmanagement.controller to javafx.fxml;
    opens com.studentmanagement.model to javafx.base;
    
    exports com.studentmanagement;
    exports com.studentmanagement.controller;
    exports com.studentmanagement.model;
    exports com.studentmanagement.service;
    exports com.studentmanagement.util;
}