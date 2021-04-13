CREATE TABLE sshkey (
    id UUID PRIMARY KEY NOT NULL,
    name VARCHAR(255) UNIQUE NOT NULL,
    value VARCHAR(1000) NOT NULL,
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