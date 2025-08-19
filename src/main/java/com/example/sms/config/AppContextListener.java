package com.example.sms.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@WebListener
public class AppContextListener implements ServletContextListener {

    public static final String DATA_SOURCE_ATTR = "DATA_SOURCE";
    public static final String EXECUTOR_ATTR = "EXECUTOR";
    private static final Logger log = LoggerFactory.getLogger(AppContextListener.class);

    private HikariDataSource dataSource;
    private ExecutorService executorService;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        String url = getEnvOrParam(ctx, "DB_URL");
        String user = getEnvOrParam(ctx, "DB_USER");
        String pass = getEnvOrParam(ctx, "DB_PASSWORD");

        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(url);
        cfg.setUsername(user);
        cfg.setPassword(pass);
        cfg.setMaximumPoolSize(10);
        cfg.setMinimumIdle(2);
        cfg.setPoolName("sms-pool");
        cfg.addDataSourceProperty("cachePrepStmts", "true");
        cfg.addDataSourceProperty("prepStmtCacheSize", "250");
        cfg.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        dataSource = new HikariDataSource(cfg);
        executorService = Executors.newFixedThreadPool(Math.max(4, Runtime.getRuntime().availableProcessors()));

        ctx.setAttribute(DATA_SOURCE_ATTR, dataSource);
        ctx.setAttribute(EXECUTOR_ATTR, executorService);
        log.info("AppContext initialized. DB URL: {}", url);

        try {
            initSchema();
            seedDefaults();
        } catch (Exception e) {
            log.error("Initialization error", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        Object ds = ctx.getAttribute(DATA_SOURCE_ATTR);
        if (ds instanceof DataSource) {
            try {
                dataSource.close();
            } catch (Exception e) {
                log.warn("Error closing datasource", e);
            }
        }
        Object ex = ctx.getAttribute(EXECUTOR_ATTR);
        if (ex instanceof ExecutorService) {
            try {
                ((ExecutorService) ex).shutdownNow();
            } catch (Exception e) {
                log.warn("Error shutting down executor", e);
            }
        }
        log.info("AppContext destroyed");
    }

    private String getEnvOrParam(ServletContext ctx, String key) {
        String env = System.getenv(key);
        if (env != null && !env.isEmpty()) return env;
        String param = ctx.getInitParameter(key);
        return param != null ? param : "";
    }

    private void initSchema() throws Exception {
        try (Connection c = dataSource.getConnection()) {
            InputStream in = getClass().getClassLoader().getResourceAsStream("schema.sql");
            if (in == null) {
                log.warn("schema.sql not found on classpath");
                return;
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line).append('\n');
                for (String stmt : sb.toString().split(";")) {
                    String sql = stmt.trim();
                    if (sql.isEmpty()) continue;
                    try (Statement s = c.createStatement()) {
                        s.execute(sql);
                    }
                }
            }
        }
    }

    private void seedDefaults() throws Exception {
        try (Connection c = dataSource.getConnection()) {
            // Seed admin user
            String check = "SELECT id FROM users WHERE admission_number=?";
            try (PreparedStatement ps = c.prepareStatement(check)) {
                ps.setString(1, "ADM-0001");
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        String insert = "INSERT INTO users(admission_number, email, full_name, password_hash, role) VALUES(?,?,?,?,?)";
                        try (PreparedStatement ins = c.prepareStatement(insert)) {
                            ins.setString(1, "ADM-0001");
                            ins.setString(2, "admin@example.com");
                            ins.setString(3, "System Admin");
                            ins.setString(4, org.mindrot.jbcrypt.BCrypt.hashpw("admin1234", org.mindrot.jbcrypt.BCrypt.gensalt(12)));
                            ins.setString(5, "ADMIN");
                            ins.executeUpdate();
                        }
                    }
                }
            }
            // Seed teacher user
            try (PreparedStatement ps = c.prepareStatement(check)) {
                ps.setString(1, "TEA-0001");
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        String insert = "INSERT INTO users(admission_number, email, full_name, password_hash, role) VALUES(?,?,?,?,?)";
                        try (PreparedStatement ins = c.prepareStatement(insert)) {
                            ins.setString(1, "TEA-0001");
                            ins.setString(2, "teacher@example.com");
                            ins.setString(3, "Default Teacher");
                            ins.setString(4, org.mindrot.jbcrypt.BCrypt.hashpw("teacher1234", org.mindrot.jbcrypt.BCrypt.gensalt(12)));
                            ins.setString(5, "TEACHER");
                            ins.executeUpdate();
                        }
                    }
                }
            }
            // Seed a sample exam
            String checkExam = "SELECT id FROM exams WHERE name=? AND term=? AND year=?";
            try (PreparedStatement ps = c.prepareStatement(checkExam)) {
                ps.setString(1, "Opening Exam");
                ps.setInt(2, 1);
                ps.setInt(3, 2025);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        String insert = "INSERT INTO exams(name, term, year) VALUES(?,?,?)";
                        try (PreparedStatement ins = c.prepareStatement(insert)) {
                            ins.setString(1, "Opening Exam");
                            ins.setInt(2, 1);
                            ins.setInt(3, 2025);
                            ins.executeUpdate();
                        }
                    }
                }
            }
        }
    }
}
