CREATE TABLE sshkey (
    id UUID PRIMARY KEY NOT NULL,
    name VARCHAR(255) UNIQUE NOT NULL,
    value MEDIUMTEXT NOT NULL,
    create_time TIMESTAMP NOT NULL,
    update_time TIMESTAMP,
    owner_id VARCHAR(255) NOT NULL
);

CREATE TABLE sshkey_site (
  sshkey_id UUID,
  site_id UUID,
  PRIMARY KEY (sshkey_id, site_id),
  FOREIGN KEY (sshkey_id) REFERENCES sshkey(id) ON DELETE CASCADE,
  FOREIGN KEY (site_id) REFERENCES site(id) ON DELETE CASCADE
);

CREATE TABLE installed_ssh_key (
    id UUID PRIMARY KEY NOT NULL,
   	site_Id UUID NOT NULL,
	sshkey_Id UUID NOT NULL,
  	value MEDIUMTEXT NOT NULL,
  	UNIQUE (sshkey_id, site_id),
  	FOREIGN KEY (sshkey_id) REFERENCES sshkey(id),
	FOREIGN KEY (site_id) REFERENCES site(id)
);

CREATE TABLE ssh_key_operation_job (
	id UUID PRIMARY KEY NOT NULL,
	correlation_Id UUID NOT NULL,
	site_Id UUID NOT NULL,
	sshkey_Id UUID NOT NULL,
	status VARCHAR(255) NOT NULL,
	operation VARCHAR(255) NOT NULL,
	error VARCHAR(255),
	origination_time TIMESTAMP NOT NULL,
	UNIQUE (sshkey_id, site_id),
	FOREIGN KEY (sshkey_id) REFERENCES sshkey(id),
	FOREIGN KEY (site_id) REFERENCES site(id)
);

ALTER TABLE site ADD COLUMN SSH_KEY_HISTORY_LENGTH INT;

CREATE TABLE ssh_key_history (
	id UUID PRIMARY KEY NOT NULL,
	sshkey_owner_id VARCHAR(255) NOT NULL,
	site_Id UUID NOT NULL,
	sshkey_fingerprint VARCHAR(255) NOT NULL,
	origination_time TIMESTAMP NOT NULL,
	FOREIGN KEY (site_id) REFERENCES site(id) ON DELETE CASCADE
);