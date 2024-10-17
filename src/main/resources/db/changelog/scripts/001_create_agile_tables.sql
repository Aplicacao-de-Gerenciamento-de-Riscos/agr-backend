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
                            name VARCHAR(255),
                            state VARCHAR(500),
                            start_date TIMESTAMP,
                            end_date TIMESTAMP,
                            complete_date TIMESTAMP,
                            goal VARCHAR(1000)
);

-- Tabela: tb_component
CREATE TABLE tb_component (
                              cod_component BIGINT PRIMARY KEY,
                              name VARCHAR(255)
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
                            total INTEGER
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
                                  created TIMESTAMP,
                                  updated TIMESTAMP,
                                  time_spent VARCHAR(255),
                                  cod_worklog BIGINT,
                                  CONSTRAINT fk_worklog FOREIGN KEY (cod_worklog) REFERENCES tb_worklog(cod_worklog)
);

-- Tabela: tb_project (tabela de projeto)
CREATE TABLE tb_project (
                           cod_project BIGINT PRIMARY KEY,
                           key VARCHAR(255),
                            board_id BIGINT
);

INSERT INTO tb_project (cod_project, key, board_id) VALUES (10016, 'MFM', 121), (10035, 'WSWE', 191), (10061, 'WSMTEMP', 232);

-- Tabela: tb_version
CREATE TABLE tb_version (
                           cod_version BIGINT PRIMARY KEY,
                           description VARCHAR(10000),
                           name VARCHAR(255),
                           archived BOOLEAN,
                           released BOOLEAN,
                           start_date TIMESTAMP,
                           release_date TIMESTAMP,
                           overdue BOOLEAN,
                           user_start_date VARCHAR(255),
                           user_release_date VARCHAR(255),
                           cod_project BIGINT,
                           CONSTRAINT fk_project FOREIGN KEY (cod_project) REFERENCES tb_project(cod_project)
);

-- Tabela: tb_issue
CREATE TABLE tb_issue (
                          cod_issue BIGINT PRIMARY KEY,
                          key VARCHAR(255),
                          time_original_estimate BIGINT,
                          time_estimate BIGINT,
                          work_ratio BIGINT,
                          cod_worklog BIGINT, -- Corrigir o nome da coluna de work_log para cod_worklog
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
                          CONSTRAINT fk_parent FOREIGN KEY (cod_parent) REFERENCES tb_issue(cod_issue),
                          CONSTRAINT fk_worklog FOREIGN KEY (cod_worklog) REFERENCES tb_worklog(cod_worklog) -- Adicionar a FK para worklog
);

-- Tabela: tb_issue_components (tabela de relacionamento)
CREATE TABLE tb_issue_components (
                                     cod_issue BIGINT,
                                     cod_component BIGINT,
                                     PRIMARY KEY (cod_issue, cod_component),
                                     CONSTRAINT fk_issue FOREIGN KEY (cod_issue) REFERENCES tb_issue(cod_issue),
                                     CONSTRAINT fk_component FOREIGN KEY (cod_component) REFERENCES tb_component(cod_component)
);

-- Tabela: tb_version_issue (tabela de relacionamento)
CREATE SEQUENCE seq_version_issue
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;
CREATE TABLE tb_version_issue (
                                 cod_version_issue BIGINT PRIMARY KEY DEFAULT nextval('seq_version_issue'),
                                 cod_version BIGINT,
                                 cod_issue BIGINT,
                                 CONSTRAINT fk_version FOREIGN KEY (cod_version) REFERENCES tb_version(cod_version),
                                 CONSTRAINT fk_issue FOREIGN KEY (cod_issue) REFERENCES tb_issue(cod_issue)
);