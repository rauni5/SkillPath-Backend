CREATE TABLE app_notifications (
 id BIGSERIAL PRIMARY KEY,
 user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 type VARCHAR(64) NOT NULL,
 title VARCHAR(200) NOT NULL,
 body VARCHAR(1000) NOT NULL,
 project_id BIGINT REFERENCES projects(id) ON DELETE SET NULL,
 post_id BIGINT REFERENCES project_posts(id) ON DELETE SET NULL,
 read BOOLEAN NOT NULL DEFAULT FALSE,
 created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_app_notifications_user ON app_notifications(user_id, created_at);
CREATE INDEX idx_app_notifications_user_unread ON app_notifications(user_id) WHERE read = FALSE;