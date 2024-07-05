CREATE SCHEMA video_streaming AUTHORIZATION dvpsqluser;

CREATE TABLE video_streaming.videos (
	title varchar(255) NOT NULL,
	url varchar(255) NOT NULL,
	file_path varchar(255) NULL,
	uploaded_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	CONSTRAINT videos_pk PRIMARY KEY (url)
);

CREATE TABLE video_streaming.cache_status (
	is_cached bool DEFAULT false NULL,
	cached_at timestamp NULL,
	url varchar NOT NULL,
	CONSTRAINT cache_status_pk PRIMARY KEY (url),
	CONSTRAINT cache_status_videos_fk FOREIGN KEY (url) REFERENCES video_streaming.videos(url) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE video_streaming.upload_logs (
	status varchar(20) NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	url varchar NOT NULL,
	CONSTRAINT upload_logs_pk PRIMARY KEY (url),
	CONSTRAINT upload_logs_status_check CHECK (((status)::text = ANY (ARRAY[('in_progress'::character varying)::text, ('completed'::character varying)::text, ('failed'::character varying)::text]))),
	CONSTRAINT upload_logs_videos_fk FOREIGN KEY (url) REFERENCES video_streaming.videos(url) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE video_streaming.users (
	id serial4 NOT NULL,
	username varchar(50) NOT NULL,
	password_hash varchar(255) NOT NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	CONSTRAINT users_pkey PRIMARY KEY (id)
);

INSERT INTO video_streaming.users (id, username, password_hash, created_at) VALUES(-999, 'no-name', 'hashhash', '2024-01-01 00:00:00.000');