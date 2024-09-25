--liquibase formatted sql

--changeset bbrendagaioo@gmail.com:001-create-agile-table

-- Tabela: tb_epic
CREATE TABLE tb_epic (
                         cod_epic BIGINT PRIMARY KEY,
                         name VARCHAR(255),
                         summary VARCHAR(255),
                         key VARCHAR(255)
);

-- Tabela: tb_sprint
CREATE TABLE tb_sprint (
                           cod_sprint BIGINT PRIMARY KEY,
                            name VARCHAR(255)
);

-- Tabela: tb_component
CREATE TABLE tb_component (
                              cod_component BIGINT PRIMARY KEY,
                              name VARCHAR(255)
);

-- Tabela: tb_issue
CREATE TABLE tb_issue (
                          cod_issue BIGINT PRIMARY KEY,
                          key VARCHAR(255),
                          time_original_estimate BIGINT,
                          time_estimate BIGINT,
                          work_ratio BIGINT,
                          work_log BIGINT,
                          status VARCHAR(255),
                          timespent BIGINT,
                          resolution_date TIMESTAMP,
                          updated TIMESTAMP,
                          created TIMESTAMP,
                          flagged BOOLEAN,
                          assignee VARCHAR(255),
                          priority VARCHAR(255),
                          issuetype VARCHAR(255),
                          summary VARCHAR(255),
                          cod_epic BIGINT,
                          cod_sprint BIGINT,
                          cod_parent BIGINT,
                          CONSTRAINT fk_epic FOREIGN KEY (cod_epic) REFERENCES tb_epic(cod_epic),
                          CONSTRAINT fk_sprint FOREIGN KEY (cod_sprint) REFERENCES tb_sprint(cod_sprint),
                          CONSTRAINT fk_parent FOREIGN KEY (cod_parent) REFERENCES tb_issue(cod_issue)
);

-- Tabela: tb_issue_components (tabela de relacionamento)
CREATE TABLE tb_issue_components (
                                     cod_issue BIGINT,
                                     cod_component BIGINT,
                                     PRIMARY KEY (cod_issue, cod_component),
                                     CONSTRAINT fk_issue FOREIGN KEY (cod_issue) REFERENCES tb_issue(cod_issue),
                                     CONSTRAINT fk_component FOREIGN KEY (cod_component) REFERENCES tb_component(cod_component)
);

-- Sequência: seq_worklog
CREATE SEQUENCE seq_worklog
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

-- Tabela: tb_worklog
CREATE TABLE tb_worklog (
                            cod_worklog BIGINT PRIMARY KEY DEFAULT nextval('seq_worklog'),
                            start_at INTEGER,
                            max_results INTEGER,
                            total INTEGER,
                            cod_issue BIGINT,
                            CONSTRAINT fk_issue FOREIGN KEY (cod_issue) REFERENCES tb_issue(cod_issue)
);

-- Sequência: seq_worklog_entry
CREATE SEQUENCE seq_worklog_entry
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

-- Tabela: tb_worklog_entry
CREATE TABLE tb_worklog_entry (
                                  cod_worklog_entry BIGINT PRIMARY KEY DEFAULT nextval('seq_worklog_entry'),
                                  self VARCHAR(255),
                                  author VARCHAR(255),
                                  created VARCHAR(255),
                                  updated VARCHAR(255),
                                  time_spent VARCHAR(255),
                                  cod_worklog BIGINT,
                                  CONSTRAINT fk_worklog FOREIGN KEY (cod_worklog) REFERENCES tb_worklog(cod_worklog)
);