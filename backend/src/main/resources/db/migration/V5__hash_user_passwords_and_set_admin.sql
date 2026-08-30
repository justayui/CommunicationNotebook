UPDATE users SET password = '$2a$10$qv7NKSUgMkZxcSqmg3T7R.SEz83EbA/0Korj1et6PgoTQsnaOQmiC'
WHERE employee_id IN ('E001', 'E002');

UPDATE users SET is_admin = true
WHERE employee_id = 'E001';
