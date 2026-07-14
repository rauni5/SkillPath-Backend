INSERT INTO career_roles (name, description) VALUES
 ('Backend Developer', 'Builds server-side APIs and services'),
 ('Full Stack Developer','Works across frontend and backend'),
 ('Mobile Developer', 'Builds iOS and Android applications'),
 ('DevOps Engineer', 'Manages infrastructure and CI/CD'),
 ('Data Engineer', 'Builds data pipelines and analytics');
INSERT INTO skills (name, category) VALUES
 ('Java','BACKEND'),('OOP','BACKEND'),('Collections','BACKEND'),
 ('Spring','BACKEND'),('Spring Boot','BACKEND'),('REST APIs','BACKEND'),
 ('PostgreSQL','DATABASE'),('SQL','DATABASE'),
 ('Docker','DEVOPS'),('Linux','DEVOPS'),('Git','DEVOPS'),
 ('AWS','CLOUD'),('HTML','FRONTEND'),('CSS','FRONTEND'),
 ('JavaScript','FRONTEND'),('React','FRONTEND'),
 ('Dart','MOBILE'),('Flutter','MOBILE');
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
 (s.name='Flutter' AND p.name='Dart');
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