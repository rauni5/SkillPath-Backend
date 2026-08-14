INSERT INTO career_roles (name, description) VALUES
 ('Backend Developer', 'Builds server-side APIs and services'),
 ('Full Stack Developer','Works across frontend and backend'),
 ('Mobile Developer', 'Builds iOS and Android applications'),
 ('DevOps Engineer', 'Manages infrastructure and CI/CD'),
 ('Data Engineer', 'Builds data pipelines and analytics'),
 ('Frontend Developer', 'Builds user-facing web interfaces and client-side experiences');
INSERT INTO skills (name, category) VALUES
 ('Java','BACKEND'),('OOP','BACKEND'),('Collections','BACKEND'),
 ('Spring','BACKEND'),('Spring Boot','BACKEND'),('REST APIs','BACKEND'),
 ('PostgreSQL','DATABASE'),('SQL','DATABASE'),
 ('Docker','DEVOPS'),('Linux','DEVOPS'),('Git','DEVOPS'),
 ('AWS','CLOUD'),('HTML','FRONTEND'),('CSS','FRONTEND'),
 ('JavaScript','FRONTEND'),('React','FRONTEND'),
 ('Dart','MOBILE'),('Flutter','MOBILE'),
 ('CI/CD','DEVOPS'),('Kubernetes','DEVOPS'),('Terraform','CLOUD'),
 ('State Management','MOBILE'),
 ('Python','DATA_ENGINEERING'),('Pandas','DATA_ENGINEERING'),
 ('ETL Pipelines','DATA_ENGINEERING'),('Data Warehousing','DATA_ENGINEERING'),
 ('Apache Airflow','DATA_ENGINEERING'),
 ('Node.js','BACKEND'),('Express.js','BACKEND'),
 ('MongoDB','DATABASE'),('Django','BACKEND'),
 ('Swift','MOBILE'),('Kotlin','MOBILE'),
 ('Scala','DATA_ENGINEERING'),('Apache Spark','DATA_ENGINEERING'),
 ('TypeScript','FRONTEND');
INSERT INTO skill_dependencies(skill_id, prerequisite_id)
SELECT s.id, p.id FROM skills s JOIN skills p ON true WHERE
 (s.name='OOP' AND p.name='Java') OR
 (s.name='Collections' AND p.name='OOP') OR
 (s.name='Spring' AND p.name='Collections') OR
 (s.name='Spring Boot' AND p.name='Spring') OR
 (s.name='REST APIs' AND p.name='Spring Boot') OR
 (s.name='Docker' AND p.name='Linux') OR
 (s.name='AWS' AND p.name='Docker') OR
 (s.name='CSS' AND p.name='HTML') OR
 (s.name='JavaScript' AND p.name='CSS') OR
 (s.name='React' AND p.name='JavaScript') OR
 (s.name='Flutter' AND p.name='Dart') OR
 (s.name='PostgreSQL' AND p.name='SQL') OR
 (s.name='CI/CD' AND p.name='Git') OR
 (s.name='CI/CD' AND p.name='Docker') OR
 (s.name='Kubernetes' AND p.name='Docker') OR
 (s.name='Terraform' AND p.name='AWS') OR
 (s.name='State Management' AND p.name='Flutter') OR
 (s.name='Pandas' AND p.name='Python') OR
 (s.name='ETL Pipelines' AND p.name='SQL') OR
 (s.name='ETL Pipelines' AND p.name='Python') OR
 (s.name='Data Warehousing' AND p.name='SQL') OR
 (s.name='Apache Airflow' AND p.name='ETL Pipelines') OR
 (s.name='Node.js' AND p.name='JavaScript') OR
 (s.name='Express.js' AND p.name='Node.js') OR
 (s.name='MongoDB' AND p.name='Node.js') OR
 (s.name='Django' AND p.name='Python') OR
 (s.name='Apache Spark' AND p.name='Python') OR
 (s.name='TypeScript' AND p.name='JavaScript');
 
INSERT INTO role_required_skills(role_id, skill_id, importance)
SELECT r.id, s.id,
 CASE s.name WHEN 'Java' THEN 10 WHEN 'Spring Boot' THEN 10
 WHEN 'REST APIs' THEN 9 WHEN 'OOP' THEN 9
 WHEN 'PostgreSQL' THEN 8 WHEN 'Git' THEN 8
 WHEN 'Docker' THEN 7 ELSE 6 END
FROM career_roles r CROSS JOIN skills s
WHERE r.name='Backend Developer'
 AND s.name IN ('Java','OOP','Collections','Spring Boot',
 'REST APIs','PostgreSQL','SQL','Docker','Git');

INSERT INTO role_required_skills(role_id, skill_id, importance)
SELECT r.id, s.id,
 CASE s.name WHEN 'Java' THEN 9 WHEN 'JavaScript' THEN 9
 WHEN 'Spring Boot' THEN 8 WHEN 'REST APIs' THEN 8 WHEN 'React' THEN 8
 WHEN 'OOP' THEN 7 WHEN 'HTML' THEN 7 WHEN 'Git' THEN 7
 WHEN 'PostgreSQL' THEN 7 ELSE 6 END
FROM career_roles r CROSS JOIN skills s
WHERE r.name='Full Stack Developer'
 AND s.name IN ('Java','OOP','Spring Boot','REST APIs','HTML','CSS',
 'JavaScript','React','PostgreSQL','Git');

INSERT INTO role_required_skills(role_id, skill_id, importance)
SELECT r.id, s.id,
 CASE s.name WHEN 'Dart' THEN 10 WHEN 'Flutter' THEN 10
 WHEN 'State Management' THEN 8 WHEN 'REST APIs' THEN 7
 ELSE 6 END
FROM career_roles r CROSS JOIN skills s
WHERE r.name='Mobile Developer'
 AND s.name IN ('Dart','Flutter','State Management','REST APIs','Git');

INSERT INTO role_required_skills(role_id, skill_id, importance)
SELECT r.id, s.id,
 CASE s.name WHEN 'Linux' THEN 10 WHEN 'Docker' THEN 10
 WHEN 'CI/CD' THEN 9 WHEN 'Kubernetes' THEN 8 WHEN 'AWS' THEN 8
 ELSE 7 END
FROM career_roles r CROSS JOIN skills s
WHERE r.name='DevOps Engineer'
 AND s.name IN ('Linux','Docker','CI/CD','Kubernetes','Git','AWS','Terraform');

INSERT INTO role_required_skills(role_id, skill_id, importance)
SELECT r.id, s.id,
 CASE s.name WHEN 'Python' THEN 10 WHEN 'SQL' THEN 9
 WHEN 'ETL Pipelines' THEN 9 WHEN 'Pandas' THEN 8
 WHEN 'PostgreSQL' THEN 7 WHEN 'Data Warehousing' THEN 7
 ELSE 6 END
FROM career_roles r CROSS JOIN skills s
WHERE r.name='Data Engineer'
 AND s.name IN ('Python','SQL','PostgreSQL','Pandas','ETL Pipelines',
 'Data Warehousing','Apache Airflow','Git');

INSERT INTO role_required_skills(role_id, skill_id, importance)
SELECT r.id, s.id,
 CASE s.name WHEN 'JavaScript' THEN 9 WHEN 'React' THEN 8 WHEN 'TypeScript' THEN 7
 WHEN 'HTML' THEN 7 WHEN 'CSS' THEN 7 ELSE 6 END
FROM career_roles r CROSS JOIN skills s
WHERE r.name='Frontend Developer'
 AND s.name IN ('HTML','CSS','JavaScript','TypeScript','React','Git');

INSERT INTO role_branches(role_id, name, description)
SELECT r.id, b.name, b.description
FROM career_roles r
CROSS JOIN (VALUES
 ('MERN','MongoDB, Express.js, React, and Node.js'),
 ('Django','Python and Django on the backend, with a JS/HTML/CSS frontend'),
 ('Spring','Java and Spring Boot on the backend, with a JS/HTML/CSS frontend')
) AS b(name, description)
WHERE r.name='Full Stack Developer';

INSERT INTO branch_required_skills(branch_id, skill_id, importance)
SELECT br.id, s.id,
 CASE s.name WHEN 'JavaScript' THEN 9 WHEN 'React' THEN 9
 WHEN 'Node.js' THEN 9 WHEN 'Express.js' THEN 8 WHEN 'MongoDB' THEN 8
 WHEN 'HTML' THEN 6 WHEN 'CSS' THEN 6 ELSE 6 END
FROM role_branches br JOIN career_roles r ON r.id=br.role_id AND r.name='Full Stack Developer'
CROSS JOIN skills s
WHERE br.name='MERN'
 AND s.name IN ('JavaScript','React','Node.js','Express.js','MongoDB','HTML','CSS','Git');

INSERT INTO branch_required_skills(branch_id, skill_id, importance)
SELECT br.id, s.id,
 CASE s.name WHEN 'Python' THEN 10 WHEN 'Django' THEN 9
 WHEN 'PostgreSQL' THEN 7 WHEN 'HTML' THEN 6 WHEN 'CSS' THEN 6 ELSE 6 END
FROM role_branches br JOIN career_roles r ON r.id=br.role_id AND r.name='Full Stack Developer'
CROSS JOIN skills s
WHERE br.name='Django'
 AND s.name IN ('Python','Django','PostgreSQL','HTML','CSS','Git');

INSERT INTO branch_required_skills(branch_id, skill_id, importance)
SELECT br.id, s.id,
 CASE s.name WHEN 'Java' THEN 9 WHEN 'Spring Boot' THEN 9
 WHEN 'OOP' THEN 7 WHEN 'Collections' THEN 6 WHEN 'Spring' THEN 7
 WHEN 'REST APIs' THEN 7 WHEN 'PostgreSQL' THEN 7
 WHEN 'HTML' THEN 5 WHEN 'CSS' THEN 5 ELSE 6 END
FROM role_branches br JOIN career_roles r ON r.id=br.role_id AND r.name='Full Stack Developer'
CROSS JOIN skills s
WHERE br.name='Spring'
 AND s.name IN ('Java','OOP','Collections','Spring','Spring Boot','REST APIs',
 'PostgreSQL','HTML','CSS','Git');

INSERT INTO role_branches(role_id, name, description)
SELECT r.id, b.name, b.description
FROM career_roles r
CROSS JOIN (VALUES
 ('Java / Spring','Java and the Spring ecosystem'),
 ('Python / Django','Python with the Django framework'),
 ('Node.js / Express','JavaScript on the server with Node.js and Express')
) AS b(name, description)
WHERE r.name='Backend Developer';

INSERT INTO branch_required_skills(branch_id, skill_id, importance)
SELECT br.id, s.id,
 CASE s.name WHEN 'Java' THEN 9 WHEN 'Spring Boot' THEN 9 WHEN 'OOP' THEN 7
 WHEN 'Collections' THEN 6 WHEN 'Spring' THEN 7 WHEN 'REST APIs' THEN 8
 WHEN 'PostgreSQL' THEN 7 ELSE 6 END
FROM role_branches br JOIN career_roles r ON r.id=br.role_id AND r.name='Backend Developer'
CROSS JOIN skills s
WHERE br.name='Java / Spring'
 AND s.name IN ('Java','OOP','Collections','Spring','Spring Boot','REST APIs','PostgreSQL','SQL','Git');

INSERT INTO branch_required_skills(branch_id, skill_id, importance)
SELECT br.id, s.id,
 CASE s.name WHEN 'Python' THEN 10 WHEN 'Django' THEN 9 WHEN 'PostgreSQL' THEN 7 ELSE 6 END
FROM role_branches br JOIN career_roles r ON r.id=br.role_id AND r.name='Backend Developer'
CROSS JOIN skills s
WHERE br.name='Python / Django'
 AND s.name IN ('Python','Django','PostgreSQL','SQL','Git');

INSERT INTO branch_required_skills(branch_id, skill_id, importance)
SELECT br.id, s.id,
 CASE s.name WHEN 'Node.js' THEN 9 WHEN 'Express.js' THEN 9 WHEN 'JavaScript' THEN 8
 WHEN 'MongoDB' THEN 7 WHEN 'REST APIs' THEN 7 ELSE 6 END
FROM role_branches br JOIN career_roles r ON r.id=br.role_id AND r.name='Backend Developer'
CROSS JOIN skills s
WHERE br.name='Node.js / Express'
 AND s.name IN ('Node.js','Express.js','JavaScript','MongoDB','REST APIs','SQL','Git');

INSERT INTO role_branches(role_id, name, description)
SELECT r.id, b.name, b.description
FROM career_roles r
CROSS JOIN (VALUES
 ('Flutter','Cross-platform apps with Dart and Flutter'),
 ('Native iOS','iOS apps with Swift'),
 ('Native Android','Android apps with Kotlin')
) AS b(name, description)
WHERE r.name='Mobile Developer';

INSERT INTO branch_required_skills(branch_id, skill_id, importance)
SELECT br.id, s.id,
 CASE s.name WHEN 'Dart' THEN 10 WHEN 'Flutter' THEN 10 WHEN 'State Management' THEN 8
 WHEN 'REST APIs' THEN 7 ELSE 6 END
FROM role_branches br JOIN career_roles r ON r.id=br.role_id AND r.name='Mobile Developer'
CROSS JOIN skills s
WHERE br.name='Flutter'
 AND s.name IN ('Dart','Flutter','State Management','REST APIs','Git');

INSERT INTO branch_required_skills(branch_id, skill_id, importance)
SELECT br.id, s.id,
 CASE s.name WHEN 'Swift' THEN 10 WHEN 'REST APIs' THEN 7 ELSE 6 END
FROM role_branches br JOIN career_roles r ON r.id=br.role_id AND r.name='Mobile Developer'
CROSS JOIN skills s
WHERE br.name='Native iOS'
 AND s.name IN ('Swift','REST APIs','Git');

INSERT INTO branch_required_skills(branch_id, skill_id, importance)
SELECT br.id, s.id,
 CASE s.name WHEN 'Kotlin' THEN 10 WHEN 'REST APIs' THEN 7 ELSE 6 END
FROM role_branches br JOIN career_roles r ON r.id=br.role_id AND r.name='Mobile Developer'
CROSS JOIN skills s
WHERE br.name='Native Android'
 AND s.name IN ('Kotlin','REST APIs','Git');

INSERT INTO role_branches(role_id, name, description)
SELECT r.id, b.name, b.description
FROM career_roles r
CROSS JOIN (VALUES
 ('Python / Airflow','Batch pipelines with Python, Pandas, and Airflow'),
 ('Spark / Big Data','Large-scale data processing with Spark')
) AS b(name, description)
WHERE r.name='Data Engineer';

INSERT INTO branch_required_skills(branch_id, skill_id, importance)
SELECT br.id, s.id,
 CASE s.name WHEN 'Python' THEN 10 WHEN 'Pandas' THEN 8 WHEN 'ETL Pipelines' THEN 9
 WHEN 'Apache Airflow' THEN 8 WHEN 'SQL' THEN 8 WHEN 'PostgreSQL' THEN 6 ELSE 6 END
FROM role_branches br JOIN career_roles r ON r.id=br.role_id AND r.name='Data Engineer'
CROSS JOIN skills s
WHERE br.name='Python / Airflow'
 AND s.name IN ('Python','Pandas','ETL Pipelines','Apache Airflow','SQL','PostgreSQL','Git');

INSERT INTO branch_required_skills(branch_id, skill_id, importance)
SELECT br.id, s.id,
 CASE s.name WHEN 'Python' THEN 8 WHEN 'Scala' THEN 8 WHEN 'Apache Spark' THEN 10
 WHEN 'SQL' THEN 7 WHEN 'Data Warehousing' THEN 7 ELSE 6 END
FROM role_branches br JOIN career_roles r ON r.id=br.role_id AND r.name='Data Engineer'
CROSS JOIN skills s
WHERE br.name='Spark / Big Data'
 AND s.name IN ('Python','Scala','Apache Spark','SQL','Data Warehousing','Git');

INSERT INTO role_branches(role_id, name, description)
SELECT r.id, 'Cloud Infrastructure', 'Linux, containers, CI/CD, and AWS-based infrastructure'
FROM career_roles r WHERE r.name='DevOps Engineer';

INSERT INTO branch_required_skills(branch_id, skill_id, importance)
SELECT br.id, s.id,
 CASE s.name WHEN 'Linux' THEN 10 WHEN 'Docker' THEN 10
 WHEN 'CI/CD' THEN 9 WHEN 'Kubernetes' THEN 8 WHEN 'AWS' THEN 8
 ELSE 7 END
FROM role_branches br JOIN career_roles r ON r.id=br.role_id AND r.name='DevOps Engineer'
CROSS JOIN skills s
WHERE br.name='Cloud Infrastructure'
 AND s.name IN ('Linux','Docker','CI/CD','Kubernetes','Git','AWS','Terraform');

INSERT INTO role_branches(role_id, name, description)
SELECT r.id, 'React', 'Modern JavaScript/TypeScript frontends built with React'
FROM career_roles r WHERE r.name='Frontend Developer';

INSERT INTO branch_required_skills(branch_id, skill_id, importance)
SELECT br.id, s.id,
 CASE s.name WHEN 'JavaScript' THEN 9 WHEN 'React' THEN 8 WHEN 'TypeScript' THEN 7
 WHEN 'HTML' THEN 7 WHEN 'CSS' THEN 7 ELSE 6 END
FROM role_branches br JOIN career_roles r ON r.id=br.role_id AND r.name='Frontend Developer'
CROSS JOIN skills s
WHERE br.name='React'
 AND s.name IN ('HTML','CSS','JavaScript','TypeScript','React','Git');
 
INSERT INTO achievements (code, title, description, icon, category, criteria_type, criteria_value) VALUES
  ('FIRST_STEP', 'First Step', 'Complete your first roadmap step', 'flag', 'roadmap', 'ROADMAP_STEPS_COMPLETED', 1),
  ('FIVE_STEPS', 'Building Momentum', 'Complete 5 roadmap steps', 'trending_up', 'roadmap', 'ROADMAP_STEPS_COMPLETED', 5),
  ('TEN_STEPS', 'On A Roll', 'Complete 10 roadmap steps', 'rocket_launch', 'roadmap', 'ROADMAP_STEPS_COMPLETED', 10),
  ('HALFWAY_THERE', 'Halfway There', 'Reach 50% of your roadmap', 'timelapse', 'roadmap', 'ROADMAP_PERCENT_COMPLETE', 50),
  ('ROADMAP_MASTER', 'Roadmap Master', 'Complete 100% of your roadmap', 'emoji_events', 'roadmap', 'ROADMAP_PERCENT_COMPLETE', 100),
  ('FIRST_SKILL_CHECK', 'Quiz Taker', 'Pass your first skill check', 'quiz', 'skill_check', 'SKILL_CHECKS_PASSED', 1),
  ('SKILL_CHECK_ACE', 'Skill Check Ace', 'Pass 5 skill checks', 'workspace_premium', 'skill_check', 'SKILL_CHECKS_PASSED', 5),
  ('STREAK_3', 'Warming Up', 'Reach a 3-day activity streak', 'local_fire_department', 'streak', 'STREAK_DAYS', 3),
  ('STREAK_7', 'On Fire', 'Reach a 7-day activity streak', 'whatshot', 'streak', 'STREAK_DAYS', 7),
  ('STREAK_30', 'Unstoppable', 'Reach a 30-day activity streak', 'bolt', 'streak', 'STREAK_DAYS', 30),
  ('TEAM_PLAYER', 'Team Player', 'Join your first project', 'groups', 'project', 'PROJECTS_JOINED', 1),
  ('PROJECT_LEADER', 'Project Leader', 'Create your first project', 'campaign', 'project', 'PROJECTS_CREATED', 1),
  ('TUTOR_CHATTER', 'Curious Mind', 'Send 10 messages to the AI tutor', 'forum', 'chat', 'TUTOR_MESSAGES_SENT', 10);
