create table shedlock
(
    name       varchar(64)  not null,
    lock_until timestamp(3) not null,
    locked_at  timestamp(3) not null default current_timestamp(3),
    locked_by  varchar(255) not null,
    primary key (name)
);

CREATE TABLE IF NOT EXISTS organizations (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  company_id VARCHAR(64) NOT NULL,
  org_id VARCHAR(64) NOT NULL,
  org_name VARCHAR(255) NOT NULL,
  parent_id VARCHAR(64) NULL,
  tree_level VARCHAR(64) NULL,

  UNIQUE KEY uk_org (org_id)
);

CREATE TABLE IF NOT EXISTS employees (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  uuid VARCHAR(36) NOT NULL,

  first_name VARCHAR(128),
  last_name VARCHAR(128),
  work_mobile VARCHAR(64),
  work_phone VARCHAR(64),
  work_title VARCHAR(255),
  org_id VARCHAR(64),
  email VARCHAR(255),
  manager_id VARCHAR(255),
  manager_code VARCHAR(255),

  active_employee BOOLEAN NOT NULL DEFAULT TRUE,

  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT fk_employee_org FOREIGN KEY (org_id)
    REFERENCES organizations(org_id)
    ON DELETE RESTRICT
);


