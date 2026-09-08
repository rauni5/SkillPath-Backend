CREATE TABLE users (
 id BIGSERIAL PRIMARY KEY,
 firebase_uid VARCHAR(128) UNIQUE NOT NULL,
 email VARCHAR(255),
 name VARCHAR(120),
 bio TEXT,
 phone_number VARCHAR(30),
 location VARCHAR(255),
 active boolean NOT NULL DEFAULT true,
 soft_skills TEXT,
 experience_level VARCHAR(20) CHECK (experience_level IN ('BEGINNER','INTERMEDIATE','ADVANCED')),
 availability BOOLEAN NOT NULL DEFAULT TRUE,
 avatar_url VARCHAR(500),
 github_url VARCHAR(255),
 linkedin_url VARCHAR(255),
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
 proficiency VARCHAR(20) CHECK (proficiency IN ('BEGINNER','INTERMEDIATE','ADVANCED')),
 added_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
 PRIMARY KEY (user_id, skill_id)
);
CREATE TABLE career_roles (
 id BIGSERIAL PRIMARY KEY,
 name VARCHAR(100) UNIQUE NOT NULL,
 description TEXT
);
CREATE TABLE role_branches (
 id BIGSERIAL PRIMARY KEY,
 role_id BIGINT NOT NULL REFERENCES career_roles(id) ON DELETE CASCADE,
 name VARCHAR(100) NOT NULL,
 description TEXT,
 UNIQUE(role_id, name)
);

CREATE TABLE branch_required_skills (
 branch_id BIGINT NOT NULL REFERENCES role_branches(id) ON DELETE CASCADE,
 skill_id BIGINT NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
 importance INT NOT NULL CHECK (importance BETWEEN 1 AND 10),
 PRIMARY KEY (branch_id, skill_id)
);
CREATE TABLE user_career_goals (
 user_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
 role_id BIGINT NOT NULL REFERENCES career_roles(id),
 branch_id BIGINT REFERENCES role_branches(id),
 set_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TABLE projects (
 id BIGSERIAL PRIMARY KEY,
 name VARCHAR(200) NOT NULL,
 description TEXT,
 difficulty VARCHAR(20) CHECK (difficulty IN ('BEGINNER','INTERMEDIATE','ADVANCED')),
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
CREATE TABLE dashboard_summaries (
 user_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
 content TEXT NOT NULL,
 generated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TABLE assistant_sessions (
 id BIGSERIAL PRIMARY KEY,
 user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 title VARCHAR(120),
 created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TABLE assistant_messages (
 id BIGSERIAL PRIMARY KEY,
 session_id BIGINT NOT NULL REFERENCES assistant_sessions(id) ON DELETE CASCADE,
 role VARCHAR(10) NOT NULL CHECK (role IN ('USER','ASSISTANT')),
 content TEXT NOT NULL,
 created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TABLE achievements (
 id BIGSERIAL PRIMARY KEY,
 code VARCHAR(50) UNIQUE NOT NULL,
 title VARCHAR(100) NOT NULL,
 description VARCHAR(255) NOT NULL,
 icon VARCHAR(50) NOT NULL,
 category VARCHAR(30) NOT NULL,
 criteria_type VARCHAR(40) NOT NULL DEFAULT 'ROADMAP_STEPS_COMPLETED',
 criteria_value INT NOT NULL DEFAULT 1,
 enabled BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE user_achievements (
 id BIGSERIAL PRIMARY KEY,
 user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 achievement_id BIGINT NOT NULL REFERENCES achievements(id) ON DELETE CASCADE,
 unlocked_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
 UNIQUE (user_id, achievement_id)
);
CREATE TABLE user_streaks (
 user_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
 current_streak INT NOT NULL DEFAULT 0,
 longest_streak INT NOT NULL DEFAULT 0,
 last_activity_date DATE
);
CREATE TABLE project_posts (
 id BIGSERIAL PRIMARY KEY,
 project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
 channel VARCHAR(10) NOT NULL CHECK (channel IN ('PUBLIC','TEAM')),
 author_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 tag VARCHAR(20) NOT NULL DEFAULT 'GENERAL' CHECK (tag IN ('GENERAL','QUESTION','UPDATE','ANNOUNCEMENT')),
 title VARCHAR(200) NOT NULL,
 body TEXT NOT NULL,
 like_count INT NOT NULL DEFAULT 0,
 comment_count INT NOT NULL DEFAULT 0,
 created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
 updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TABLE project_comments (
 id BIGSERIAL PRIMARY KEY,
 post_id BIGINT NOT NULL REFERENCES project_posts(id) ON DELETE CASCADE,
 author_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 body TEXT NOT NULL,
 like_count INT NOT NULL DEFAULT 0,
 created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
 updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TABLE project_post_likes (
 post_id BIGINT NOT NULL REFERENCES project_posts(id) ON DELETE CASCADE,
 user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 PRIMARY KEY (post_id, user_id)
);
CREATE TABLE project_comment_likes (
 comment_id BIGINT NOT NULL REFERENCES project_comments(id) ON DELETE CASCADE,
 user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 PRIMARY KEY (comment_id, user_id)
);
CREATE TABLE education (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    institution VARCHAR(200) NOT NULL,
    degree VARCHAR(150),
    field_of_study VARCHAR(150),
    start_date DATE,
    end_date DATE,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TABLE certifications (
 id BIGSERIAL PRIMARY KEY,
 user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 name VARCHAR(200) NOT NULL,
 issuer VARCHAR(200),
 credential_url VARCHAR(500),
 earned_on DATE,
 created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TABLE skill_check_question_sets (
 id BIGSERIAL PRIMARY KEY,
 skill_id BIGINT NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
 questions_json TEXT NOT NULL,
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
 question_set_id BIGINT REFERENCES skill_check_question_sets(id),
 created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
 submitted_at TIMESTAMPTZ
);
CREATE TABLE skill_tutor_intros (
 skill_id BIGINT PRIMARY KEY REFERENCES skills(id) ON DELETE CASCADE,
 intro_text TEXT NOT NULL,
 created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_skill_check_question_sets_skill ON skill_check_question_sets(skill_id);
CREATE INDEX idx_education_user_id ON education(user_id);
CREATE INDEX idx_project_posts_board ON project_posts(project_id, channel, created_at);
CREATE INDEX idx_project_comments_post ON project_comments(post_id, created_at);
CREATE INDEX idx_user_achievements_user ON user_achievements(user_id);
CREATE INDEX idx_assistant_sessions_user ON assistant_sessions(user_id, created_at);
CREATE INDEX idx_assistant_messages_session ON assistant_messages(session_id, created_at);
CREATE INDEX idx_certifications_user_id ON certifications(user_id);
CREATE INDEX idx_chat_messages_user_skill ON chat_messages(user_id, skill_id, created_at);
CREATE INDEX idx_skill_check_attempts_user_skill ON skill_check_attempts(user_id, skill_id);
CREATE INDEX idx_user_device_tokens_user_id ON user_device_tokens(user_id);
CREATE INDEX idx_user_skills_user ON user_skills(user_id);
CREATE INDEX idx_roadmap_user ON roadmap_steps(user_id);
CREATE INDEX idx_project_status ON projects(status);
CREATE INDEX idx_skill_category ON skills(category);