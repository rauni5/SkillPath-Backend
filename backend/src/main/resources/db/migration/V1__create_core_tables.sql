CREATE TABLE users (
 id BIGSERIAL PRIMARY KEY,
 firebase_uid VARCHAR(128) UNIQUE NOT NULL,
 email VARCHAR(255),
 name VARCHAR(120),
 bio TEXT,
 experience_level VARCHAR(20) CHECK (experience_level IN
('BEGINNER','INTERMEDIATE','ADVANCED')),
 availability BOOLEAN NOT NULL DEFAULT TRUE,
 avatar_url VARCHAR(500),
 created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
 is_admin BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE TABLE skills (
 id BIGSERIAL PRIMARY KEY,
 name VARCHAR(100) UNIQUE NOT NULL,
 category VARCHAR(50) NOT NULL,
 description TEXT,
 created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TABLE skill_dependencies (
 skill_id BIGINT NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
 prerequisite_id BIGINT NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
 PRIMARY KEY (skill_id, prerequisite_id)
);
CREATE TABLE user_skills (
 user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 skill_id BIGINT NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
 proficiency VARCHAR(20) CHECK (proficiency IN
('BEGINNER','INTERMEDIATE','ADVANCED')),
 added_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
 PRIMARY KEY (user_id, skill_id)
);
CREATE TABLE career_roles (
 id BIGSERIAL PRIMARY KEY,
 name VARCHAR(100) UNIQUE NOT NULL,
 description TEXT
);
CREATE TABLE role_required_skills (
 role_id BIGINT NOT NULL REFERENCES career_roles(id) ON DELETE CASCADE,
 skill_id BIGINT NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
 importance INT NOT NULL CHECK (importance BETWEEN 1 AND 10),
 PRIMARY KEY (role_id, skill_id)
);
CREATE TABLE user_career_goals (
 user_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
 role_id BIGINT NOT NULL REFERENCES career_roles(id),
 set_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TABLE projects (
 id BIGSERIAL PRIMARY KEY,
 name VARCHAR(200) NOT NULL,
 description TEXT,
 difficulty VARCHAR(20) CHECK (difficulty IN
('BEGINNER','INTERMEDIATE','ADVANCED')),
 team_size INT,
 status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
 owner_id BIGINT REFERENCES users(id),
 link VARCHAR(500),
 created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TABLE project_required_skills (
 project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
 skill_id BIGINT NOT NULL REFERENCES skills(id),
 PRIMARY KEY (project_id, skill_id)
);
CREATE TABLE project_members (
 project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
 user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 role VARCHAR(100),
 status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
 invited_by_owner BOOLEAN NOT NULL DEFAULT FALSE,
 joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
 PRIMARY KEY (project_id, user_id)
);
CREATE TABLE roadmap_steps (
 id BIGSERIAL PRIMARY KEY,
 user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 skill_id BIGINT REFERENCES skills(id),
 step_order INT NOT NULL,
 status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
 completed_at TIMESTAMPTZ
);
CREATE TABLE portfolio_items (
 id BIGSERIAL PRIMARY KEY,
 user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 project_id BIGINT REFERENCES projects(id),
 github_url VARCHAR(500),
 description TEXT,
 user_role VARCHAR(100),
 created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TABLE project_required_roles (
 project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
 role_id BIGINT NOT NULL REFERENCES career_roles(id),
 PRIMARY KEY (project_id, role_id)
);
CREATE TABLE user_device_tokens (
 id BIGSERIAL PRIMARY KEY,
 user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 token VARCHAR(255) UNIQUE NOT NULL,
 platform VARCHAR(20) NOT NULL DEFAULT 'UNKNOWN',
 created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
 last_used_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TABLE chat_messages (
 id BIGSERIAL PRIMARY KEY,
 user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 skill_id BIGINT NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
 role VARCHAR(10) NOT NULL CHECK (role IN ('USER','ASSISTANT')),
 content TEXT NOT NULL,
 created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TABLE skill_check_attempts (
 id BIGSERIAL PRIMARY KEY,
 user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 skill_id BIGINT NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
 questions_json TEXT NOT NULL,
 status VARCHAR(20) NOT NULL DEFAULT 'GENERATED' CHECK (status IN ('GENERATED','SUBMITTED')),
 score INT,
 proficiency VARCHAR(20) CHECK (proficiency IN ('BEGINNER','INTERMEDIATE','ADVANCED')),
 created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
 submitted_at TIMESTAMPTZ
);
CREATE INDEX idx_chat_messages_user_skill ON chat_messages(user_id, skill_id, created_at);
CREATE INDEX idx_skill_check_attempts_user_skill ON skill_check_attempts(user_id, skill_id);
CREATE INDEX idx_user_device_tokens_user_id ON user_device_tokens(user_id);
CREATE INDEX idx_user_skills_user ON user_skills(user_id);
CREATE INDEX idx_roadmap_user ON roadmap_steps(user_id);
CREATE INDEX idx_project_status ON projects(status);
CREATE INDEX idx_skill_category ON skills(category);
