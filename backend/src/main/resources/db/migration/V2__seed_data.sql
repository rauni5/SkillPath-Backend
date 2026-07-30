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
 ('Dart','MOBILE'),('Flutter','MOBILE'),
 ('CI/CD','DEVOPS'),('Kubernetes','DEVOPS'),('Terraform','CLOUD'),
 ('State Management','MOBILE'),
 ('Python','DATA_ENGINEERING'),('Pandas','DATA_ENGINEERING'),
 ('ETL Pipelines','DATA_ENGINEERING'),('Data Warehousing','DATA_ENGINEERING'),
 ('Apache Airflow','DATA_ENGINEERING');
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
 (s.name='Apache Airflow' AND p.name='ETL Pipelines');
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