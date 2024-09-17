--liquibase formatted sql

--changeset bbrendagaioo@gmail.com:001-create-agile-table

-- Tabela: tb_epic
CREATE TABLE tb_epic (
                         cod_epic BIGINT PRIMARY KEY
);

-- Tabela: tb_sprint
CREATE TABLE tb_sprint (
                           cod_sprint BIGINT PRIMARY KEY
);

-- Tabela: tb_component
CREATE TABLE tb_component (
                              cod_component BIGINT PRIMARY KEY
);

-- Tabela: tb_issue
CREATE TABLE tb_issue (
                          cod_issue BIGINT PRIMARY KEY,
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
                          CONSTRAINT fk_epic FOREIGN KEY (cod_epic) REFERENCES tb_epic(cod_epic),
                          CONSTRAINT fk_sprint FOREIGN KEY (cod_sprint) REFERENCES tb_sprint(cod_sprint)
);

-- Tabela: tb_issue_components (tabela de relacionamento)
CREATE TABLE tb_issue_components (
                                     cod_issue BIGINT,
                                     cod_component BIGINT,
                                     PRIMARY KEY (cod_issue, cod_component),
                                     CONSTRAINT fk_issue FOREIGN KEY (cod_issue) REFERENCES tb_issue(cod_issue),
                                     CONSTRAINT fk_component FOREIGN KEY (cod_component) REFERENCES tb_component(cod_component)
);