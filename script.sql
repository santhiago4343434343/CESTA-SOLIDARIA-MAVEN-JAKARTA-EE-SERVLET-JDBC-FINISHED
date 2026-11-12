-- ============================================================
--  Banco de dados do projeto Cesta Solidaria
--  Executado automaticamente pelo Docker no 1o boot do banco,
--  ou rode manualmente: mysql -u root -p < script.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS `tomcat_database`
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `tomcat_database`;

-- ------------------------------------------------------------
-- Tabela `pessoa`
--   senha  -> hash BCrypt (nunca em texto puro)
--   status -> fila da cesta: 'aguardando' | 'retirado'
--   papel  -> autorizacao: 'USER' (beneficiario) | 'ADMIN'
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `pessoa` (
    `idPessoa`   INT(11)      NOT NULL AUTO_INCREMENT,
    `nomePessoa` VARCHAR(100) DEFAULT NULL,
    `email`      VARCHAR(100) DEFAULT NULL UNIQUE,
    `senha`      VARCHAR(100) DEFAULT NULL,
    `status`     VARCHAR(20)  NOT NULL DEFAULT 'aguardando',
    `papel`      VARCHAR(20)  NOT NULL DEFAULT 'USER',
    `reset_token`       VARCHAR(100) DEFAULT NULL,
    `reset_expira`      DATETIME     DEFAULT NULL,
    `email_confirmado`  TINYINT(1)   DEFAULT 0,
    `confirmacao_token` VARCHAR(100) DEFAULT NULL,
    PRIMARY KEY (`idPessoa`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Se você JÁ tinha a tabela criada antes, rode uma vez cada linha que faltar:
--   ALTER TABLE `pessoa` ADD COLUMN `status` VARCHAR(20) NOT NULL DEFAULT 'aguardando';
--   ALTER TABLE `pessoa` ADD COLUMN `papel`  VARCHAR(20) NOT NULL DEFAULT 'USER';
--   ALTER TABLE `pessoa` ADD COLUMN `reset_token`  VARCHAR(100) DEFAULT NULL;
--   ALTER TABLE `pessoa` ADD COLUMN `reset_expira` DATETIME     DEFAULT NULL
-- ------------------------------------------------------------
-- Administrador inicial (idempotente: só cria se ainda não existir).
--   e-mail: admin@cesta.local
--   senha:  admin123   (hash BCrypt abaixo)
-- Troque a senha depois pela tela "Meu cadastro".
-- ------------------------------------------------------------
INSERT INTO `pessoa` (`nomePessoa`, `email`, `senha`, `status`, `papel`, `email_confirmado`)
SELECT 'Administrador', 'admin@cesta.local',
       '$2a$10$J/qAvmVCoYLr83JizvUJp.nSb.NCjha7/cq75EXVz8AQPqVaJgh/a',
       'aguardando', 'ADMIN', 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `pessoa` WHERE `email` = 'admin@cesta.local');
